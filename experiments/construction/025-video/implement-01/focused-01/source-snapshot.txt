package pt.uminho.esr.ott.server;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

/** Synchronous, single-caller reader of five-digit-length-prefixed JPEG records. */
public final class VideoSource implements AutoCloseable
{
    //* === Atributos e Variáveis === *
    private static final int PREFIX_BYTES = 5;
    private static final int MAX_FRAME_BYTES = 99999;
    private static final int MAX_DIMENSION = 4096;
    private static final long MAX_PIXELS = 4194304L;
    private static final long MAX_DESTINATION_RASTER_BYTES = 16777216L;
    private static final int MAX_DIAGNOSTIC_CHARS = 256;

    private final String sourceId;
    private final int maxFrameBytes;
    private final ReaderFactory readerFactory;
    private InputStream input;
    private State state = State.OPEN;
    private Throwable terminal;
    private long recordIndex;
    private long prefixOffset;
    private Stage stage = Stage.OPEN;
    private long consumedInStage;



    //* === Construtores === *

    private VideoSource(InputStream input, String sourceId, int maxFrameBytes,
            ReaderFactory readerFactory)
    {
        this.input = Objects.requireNonNull(input, "input");
        this.sourceId = bounded(Objects.requireNonNull(sourceId, "sourceId"));
        validateLimit(maxFrameBytes);
        this.maxFrameBytes = maxFrameBytes;
        this.readerFactory = Objects.requireNonNull(readerFactory, "readerFactory");
    }


    public static VideoSource open(Path mediaBase, Path file, int maxFrameBytes)
            throws SourceException
    {
        Objects.requireNonNull(mediaBase, "mediaBase");
        Objects.requireNonNull(file, "file");
        validateLimit(maxFrameBytes);
        String label = bounded(file.toString());
        InputStream opened;
        try
        {
            Path base = mediaBase.toRealPath();
            Path candidate = file.isAbsolute() ? file : base.resolve(file);
            Path real = candidate.toRealPath();
            if(real.equals(base) || !real.startsWith(base))
                throw new IOException("Source is not contained in mediaBase");

            BasicFileAttributes attributes = Files.readAttributes(real,
                    BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if(!attributes.isRegularFile())
                throw new IOException("Source is not a regular file");
            opened = Files.newInputStream(real, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);

        }catch(IOException failure){
            throw new SourceException(new Diagnostic(label, 0, 0, Stage.OPEN,
                    Reason.OPEN_IO, -1, -1, 0, "PATH_ADMISSION"), failure);
        }

        try
        {
            return fromOwnedInput(opened, label, maxFrameBytes);

        }catch(RuntimeException | Error primary){
            try
            {
                opened.close();

            }catch(IOException | RuntimeException | Error secondary){
                suppress(primary, secondary);
            }
            throw primary;
        }
    }


    static VideoSource fromOwnedInput(InputStream input, String sourceId, int maxFrameBytes)
    {
        return fromOwnedInput(input, sourceId, maxFrameBytes, ignored ->
        {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
            return readers.hasNext() ? readers.next() : null;
        });
    }


    static VideoSource fromOwnedInput(InputStream input, String sourceId, int maxFrameBytes,
            ReaderFactory readerFactory)
    {
        return new VideoSource(input, sourceId, maxFrameBytes, readerFactory);
    }



    //* === Getters === *

    public State state()
    {
        return state;
    }



    //* === Lógica === *

    public Optional<Frame> nextFrame() throws SourceException
    {
        if(state == State.END)
            return Optional.empty();
        if(state == State.FAILED)
            throw propagate(terminal);
        if(state == State.CLOSED)
            throw failure(Reason.CLOSED_USE, -1, -1, "SOURCE_CLOSED", null);

        try
        {
            stage = Stage.PREFIX;
            consumedInStage = 0;
            byte[] prefix = new byte[PREFIX_BYTES];
            if(read(prefix, 0, 1) == -1)
            {
                Throwable closeFailure = releaseInput(null);
                if(closeFailure != null)
                    throw propagate(closeFailure);
                state = State.END;
                return Optional.empty();
            }

            long nextIndex;
            long bodyOffset;
            try
            {
                nextIndex = checkedNextIndex(recordIndex);
                bodyOffset = checkedEnd(prefixOffset, PREFIX_BYTES);

            }catch(ArithmeticException failure){
                throw failure(Reason.POSITION_LIMIT, -1, -1, "PREFIX_POSITION", failure);
            }

            int length = digit(prefix[0], 0);
            int prefixRead = 1;
            while(prefixRead < PREFIX_BYTES)
            {
                int count = read(prefix, prefixRead, PREFIX_BYTES - prefixRead);
                if(count == -1)
                    throw failure(Reason.TRUNCATED_PREFIX, PREFIX_BYTES, prefixRead, "PREFIX_EOF", null);
                for(int index = prefixRead; index < prefixRead + count; index++)
                {
                    length = length * 10 + digit(prefix[index], index);
                }
                prefixRead += count;
            }

            if(length == 0)
                throw failure(Reason.INVALID_LENGTH, 1, 0, "ZERO_LENGTH", null);
            if(length > maxFrameBytes)
                throw failure(Reason.FRAME_LIMIT, maxFrameBytes, length, "COMPRESSED_BYTES", null);

            long nextOffset;
            try
            {
                nextOffset = checkedEnd(bodyOffset, length);

            }catch(ArithmeticException failure){
                throw failure(Reason.POSITION_LIMIT, -1, length, "BODY_POSITION", failure);
            }

            stage = Stage.BODY;
            consumedInStage = 0;
            byte[] bytes = new byte[length];
            int bodyRead = 0;
            while(bodyRead < length)
            {
                int count = read(bytes, bodyRead, length - bodyRead);
                if(count == -1)
                    throw failure(Reason.TRUNCATED_BODY, length, bodyRead, "BODY_EOF", null);
                bodyRead += count;
            }

            stage = Stage.JPEG;
            consumedInStage = 0;
            validateJpeg(bytes);
            Frame frame = new Frame(recordIndex, prefixOffset, bytes);
            recordIndex = nextIndex;
            prefixOffset = nextOffset;
            return Optional.of(frame);

        }catch(SourceException | RuntimeException | Error primary){
            terminal = primary;
            state = State.FAILED;
            releaseInput(primary);
            throw propagate(primary);
        }
    }


    @Override
    public void close() throws SourceException
    {
        if(state != State.OPEN)
            return;
        Throwable closeFailure = releaseInput(null);
        if(closeFailure != null)
        {
            terminal = closeFailure;
            state = State.FAILED;
            throw propagate(closeFailure);
        }
        state = State.CLOSED;
    }



    //* === Auxiliar === *

    private int read(byte[] bytes, int offset, int length) throws SourceException
    {
        int count;
        try
        {
            count = input.read(bytes, offset, length);

        }catch(IOException cause){
            throw failure(Reason.READ_IO, length, -1, "INPUT_READ", cause);
        }
        if(count == 0)
            throw failure(Reason.READ_IO, length, 0, "NO_PROGRESS", null);
        if(count < -1 || count > length)
            throw failure(Reason.READ_IO, length, Math.max(-1, count), "INVALID_READ_COUNT=" + count, null);
        if(count > 0)
            consumedInStage += count;
        return count;
    }


    private int digit(byte value, int index) throws SourceException
    {
        int unsigned = value & 0xff;
        if(unsigned < '0' || unsigned > '9')
            throw failure(Reason.INVALID_PREFIX, -1, unsigned, "BYTE_INDEX=" + index, null);
        return unsigned - '0';
    }


    static long checkedEnd(long offset, int length)
    {
        if(offset < 0 || length < 0)
            throw new IllegalArgumentException("Negative position");
        return Math.addExact(offset, length);
    }


    static long checkedNextIndex(long index)
    {
        if(index < 0)
            throw new IllegalArgumentException("Negative index");
        return Math.incrementExact(index);
    }


    private Dimensions scan(byte[] bytes) throws SourceException
    {
        if(bytes.length < 4 || unsigned(bytes, 0) != 0xff || unsigned(bytes, 1) != 0xd8)
            throw invalidJpeg("MISSING_SOI");
        int cursor = 2;
        boolean entropy = false;
        boolean sawScan = false;
        Dimensions dimensions = null;
        while(cursor < bytes.length)
        {
            if(entropy)
            {
                while(cursor < bytes.length && unsigned(bytes, cursor) != 0xff)
                {
                    cursor++;
                }
            }
            if(cursor >= bytes.length || unsigned(bytes, cursor++) != 0xff)
                throw invalidJpeg("EXPECTED_MARKER");
            while(cursor < bytes.length && unsigned(bytes, cursor) == 0xff)
            {
                cursor++;
            }
            if(cursor >= bytes.length)
                throw invalidJpeg("TRUNCATED_MARKER");
            int marker = unsigned(bytes, cursor++);
            if(entropy && (marker == 0 || marker >= 0xd0 && marker <= 0xd7 || marker == 1))
                continue;
            entropy = false;
            if(marker == 0xd9)
            {
                if(dimensions == null || !sawScan || cursor != bytes.length)
                    throw invalidJpeg("EOI_BOUNDARY");
                return dimensions;
            }
            if(marker == 1)
                continue;
            if(!hasSegmentLength(marker) || cursor + 2 > bytes.length)
                throw invalidJpeg("INVALID_MARKER");
            int segmentLength = unsignedShort(bytes, cursor);
            if(segmentLength < 2 || segmentLength > bytes.length - cursor)
                throw invalidJpeg("SEGMENT_LENGTH");
            if(isStartOfFrame(marker))
            {
                if(segmentLength < 8 || unsigned(bytes, cursor + 7) == 0
                        || segmentLength != 8 + 3 * unsigned(bytes, cursor + 7))
                    throw invalidJpeg("SOF_LENGTH");
                Dimensions observed = new Dimensions(unsignedShort(bytes, cursor + 5),
                        unsignedShort(bytes, cursor + 3));
                validateDimensions(observed);
                if(dimensions != null && !dimensions.equals(observed))
                    throw invalidJpeg("INCONSISTENT_SOF");
                dimensions = observed;
            }
            if(marker == 0xda)
            {
                if(dimensions == null || segmentLength < 6 || unsigned(bytes, cursor + 2) == 0
                        || unsigned(bytes, cursor + 2) > 4
                        || segmentLength != 6 + 2 * unsigned(bytes, cursor + 2))
                    throw invalidJpeg("SOS_HEADER");
                sawScan = true;
                entropy = true;
            }
            cursor += segmentLength;
        }
        throw invalidJpeg("MISSING_EOI");
    }


    private static boolean hasSegmentLength(int marker)
    {
        return marker >= 0xc0 && marker <= 0xcf && marker != 0xc8
                || marker >= 0xda && marker <= 0xdf || marker >= 0xe0 && marker <= 0xef
                || marker == 0xfe;
    }


    private static boolean isStartOfFrame(int marker)
    {
        return marker >= 0xc0 && marker <= 0xcf && marker != 0xc4 && marker != 0xc8 && marker != 0xcc;
    }


    private static int unsigned(byte[] bytes, int index)
    {
        return bytes[index] & 0xff;
    }


    private static int unsignedShort(byte[] bytes, int index)
    {
        return unsigned(bytes, index) * 256 + unsigned(bytes, index + 1);
    }


    private void validateDimensions(Dimensions dimensions) throws SourceException
    {
        if(dimensions.width <= 0 || dimensions.height <= 0)
            throw invalidJpeg("NON_POSITIVE_DIMENSIONS");
        if(dimensions.width > MAX_DIMENSION)
            throw failure(Reason.IMAGE_LIMIT, MAX_DIMENSION, dimensions.width, "WIDTH", null);
        if(dimensions.height > MAX_DIMENSION)
            throw failure(Reason.IMAGE_LIMIT, MAX_DIMENSION, dimensions.height, "HEIGHT", null);
        long pixels = (long) dimensions.width * dimensions.height;
        if(pixels > MAX_PIXELS)
            throw failure(Reason.IMAGE_LIMIT, MAX_PIXELS, pixels, "PIXELS", null);
    }


    private void validateJpeg(byte[] bytes) throws SourceException
    {
        Dimensions dimensions = scan(bytes);
        CleanupScope resources = new CleanupScope();
        Throwable primary = null;
        try
        {
            ByteArrayInputStream payload = new ByteArrayInputStream(bytes);
            resources.own(payload, payload::close);
            MemoryCacheImageInputStream cache = new MemoryCacheImageInputStream(payload);
            resources.own(cache, cache::close);
            ImageReader reader;
            try
            {
                reader = readerFactory.create(cache);

            }catch(IOException cause){
                throw failure(Reason.DECODER_UNAVAILABLE, -1, -1, "READER_CREATION", cause);
            }
            if(reader == null)
                throw failure(Reason.DECODER_UNAVAILABLE, -1, -1, "NO_JPEG_PROVIDER", null);
            resources.own(reader, reader::dispose);
            decode(reader, cache, dimensions, resources);

        }catch(SourceException | RuntimeException | Error failure){
            primary = failure;
        }
        primary = resources.finish(primary);
        if(primary != null)
            throw propagate(primary);
    }


    private void decode(ImageReader reader, ImageInputStream cache, Dimensions dimensions,
            CleanupScope resources) throws SourceException
    {
        try
        {
            reader.setInput(cache, true, true);
            if(!"JPEG".equalsIgnoreCase(reader.getFormatName()))
                throw new IllegalStateException("ReaderFactory did not supply a JPEG reader");
            Warning warning = new Warning();
            reader.addIIOReadWarningListener((ignored, message) -> warning.receive(message));
            Dimensions reported = new Dimensions(reader.getWidth(0), reader.getHeight(0));
            validateDimensions(reported);
            if(!dimensions.equals(reported))
                throw invalidJpeg("DECODER_DIMENSIONS_DIFFER");
            int type = destinationType(reader, dimensions);
            BufferedImage destination = new BufferedImage(dimensions.width, dimensions.height, type);
            resources.own(destination, destination::flush);
            ImageReadParam parameters = Objects.requireNonNull(reader.getDefaultReadParam(), "read parameters");
            parameters.setSourceRegion(null);
            parameters.setSourceSubsampling(1, 1, 0, 0);
            parameters.setSourceBands(null);
            parameters.setDestinationBands(null);
            parameters.setDestinationOffset(new Point(0, 0));
            parameters.setSourceProgressivePasses(0, Integer.MAX_VALUE);
            parameters.setDestination(destination);
            BufferedImage returned = reader.read(0, parameters);
            if(returned != null)
                resources.own(returned, returned::flush);
            if(returned != destination)
                throw new IllegalStateException("JPEG reader did not return the supplied destination");
            if(destination.getWidth() != dimensions.width || destination.getHeight() != dimensions.height)
                throw new IllegalStateException("JPEG reader changed destination dimensions");
            if(warning.received)
                throw failure(Reason.INVALID_JPEG, -1, -1, "DECODER_WARNING: " + warning.first, null);

        }catch(IOException cause){
            if(cause instanceof SourceException sourceFailure)
                throw sourceFailure;
            throw failure(Reason.INVALID_JPEG, -1, -1, "JPEG_READ", cause);
        }
    }


    private int destinationType(ImageReader reader, Dimensions dimensions) throws IOException
    {
        Iterator<ImageTypeSpecifier> types = Objects.requireNonNull(reader.getImageTypes(0), "image types");
        if(!types.hasNext())
            throw new IllegalStateException("JPEG reader supplied no image types");
        while(types.hasNext())
        {
            ImageTypeSpecifier offered = Objects.requireNonNull(types.next(), "image type");
            int type = offered.getBufferedImageType();
            int bytesPerPixel;
            switch(type)
            {
                case BufferedImage.TYPE_BYTE_GRAY:
                    bytesPerPixel = 1;
                    break;

                case BufferedImage.TYPE_3BYTE_BGR:
                    bytesPerPixel = 3;
                    break;

                case BufferedImage.TYPE_INT_RGB:
                    bytesPerPixel = 4;
                    break;

                default:
                    continue;
            }
            if(!ImageTypeSpecifier.createFromBufferedImageType(type).equals(offered))
                continue;
            long rasterBytes = Math.multiplyExact((long) dimensions.width * dimensions.height, bytesPerPixel);
            if(rasterBytes <= MAX_DESTINATION_RASTER_BYTES)
                return type;
        }
        throw failure(Reason.IMAGE_LIMIT, MAX_DESTINATION_RASTER_BYTES, -1, "DESTINATION_TYPE_OR_RASTER", null);
    }


    private Throwable releaseInput(Throwable primary)
    {
        InputStream owned = input;
        input = null;
        if(owned == null)
            return primary;
        try
        {
            owned.close();

        }catch(IOException | RuntimeException | Error secondary){
            return mergeCleanup(primary, secondary);
        }
        return primary;
    }


    private Throwable mergeCleanup(Throwable primary, Throwable secondary)
    {
        if(primary != null)
        {
            suppress(primary, secondary);
            return primary;
        }
        if(secondary instanceof IOException)
            return new SourceException(new Diagnostic(sourceId, recordIndex, prefixOffset,
                    Stage.CLOSE, Reason.CLOSE_IO, -1, -1, 0, "RESOURCE_CLEANUP"), secondary);
        return secondary;
    }


    private static void suppress(Throwable primary, Throwable secondary)
    {
        if(causeContains(primary, secondary) || causeContains(secondary, primary))
            return;
        for(Throwable previous : primary.getSuppressed())
        {
            if(previous == secondary)
                return;
        }
        primary.addSuppressed(secondary);
    }


    private static boolean causeContains(Throwable root, Throwable target)
    {
        Throwable slow = root;
        Throwable fast = root;
        do
        {
            if(slow == target)
                return true;
            slow = slow.getCause();
            fast = fast == null ? null : fast.getCause();
            fast = fast == null ? null : fast.getCause();
            if(slow == null)
                return false;
        }while(slow != fast);
        Throwable cycle = slow;
        do
        {
            if(slow == target)
                return true;
            slow = slow.getCause();
        }while(slow != cycle);
        return false;
    }


    private SourceException invalidJpeg(String detail)
    {
        return failure(Reason.INVALID_JPEG, -1, -1, detail, null);
    }


    private SourceException failure(Reason reason, long expected, long observed, String detail, Throwable cause)
    {
        return new SourceException(new Diagnostic(sourceId, recordIndex, prefixOffset,
                stage, reason, expected, observed, consumedInStage, detail), cause);
    }


    private static SourceException propagate(Throwable failure)
    {
        if(failure instanceof SourceException checked)
            return checked;
        if(failure instanceof RuntimeException unchecked)
            throw unchecked;
        if(failure instanceof Error fatal)
            throw fatal;
        throw new AssertionError("Unclassified terminal failure", failure);
    }


    private static void validateLimit(int limit)
    {
        if(limit < 1 || limit > MAX_FRAME_BYTES)
            throw new IllegalArgumentException("maxFrameBytes must be in 1..99999");
    }


    private static String bounded(String text)
    {
        return text.length() <= MAX_DIAGNOSTIC_CHARS ? text : text.substring(0, MAX_DIAGNOSTIC_CHARS);
    }



    //* === Tipos === *

    public enum State
    {
        OPEN, END, FAILED, CLOSED
    }


    public enum Stage
    {
        OPEN, PREFIX, BODY, JPEG, CLOSE
    }


    public enum Reason
    {
        OPEN_IO, INVALID_PREFIX, TRUNCATED_PREFIX, INVALID_LENGTH, FRAME_LIMIT,
        TRUNCATED_BODY, INVALID_JPEG, IMAGE_LIMIT, DECODER_UNAVAILABLE, READ_IO,
        CLOSE_IO, POSITION_LIMIT, CLOSED_USE
    }


    public record Diagnostic(String sourceId, long recordIndex, long prefixOffset,
            Stage stage, Reason reason, long expected, long observed, long consumedInStage, String detail)
    {
        //* === Atributos e Variáveis === *



        //* === Construtores === *

        public Diagnostic
        {
            sourceId = bounded(Objects.requireNonNull(sourceId, "sourceId"));
            detail = bounded(Objects.requireNonNull(detail, "detail"));
            Objects.requireNonNull(stage, "stage");
            Objects.requireNonNull(reason, "reason");
            if(recordIndex < 0 || prefixOffset < 0 || consumedInStage < 0 || expected < -1 || observed < -1)
                throw new IllegalArgumentException("Invalid diagnostic counters");
        }
    }


    public static final class Frame
    {
        //* === Atributos e Variáveis === *
        private final long index;
        private final long prefixOffset;
        private final byte[] bytes;



        //* === Construtores === *

        private Frame(long index, long prefixOffset, byte[] bytes)
        {
            this.index = index;
            this.prefixOffset = prefixOffset;
            this.bytes = bytes.clone();
        }



        //* === Getters === *

        public long index()
        {
            return index;
        }


        public long prefixOffset()
        {
            return prefixOffset;
        }


        public int length()
        {
            return bytes.length;
        }


        public byte[] bytes()
        {
            return bytes.clone();
        }
    }


    public static final class SourceException extends IOException
    {
        //* === Atributos e Variáveis === *
        private static final long serialVersionUID = 1L;
        private final Diagnostic diagnostic;



        //* === Construtores === *

        private SourceException(Diagnostic diagnostic, Throwable cause)
        {
            super(diagnostic.reason() + " source=" + diagnostic.sourceId() + " record="
                    + diagnostic.recordIndex() + " offset=" + diagnostic.prefixOffset()
                    + " stage=" + diagnostic.stage() + " " + diagnostic.detail(), cause);
            this.diagnostic = diagnostic;
        }



        //* === Getters === *

        public Diagnostic diagnostic()
        {
            return diagnostic;
        }


        public Reason reason()
        {
            return diagnostic.reason();
        }
    }


    @FunctionalInterface
    interface ReaderFactory
    {
        ImageReader create(ImageInputStream input) throws IOException;
    }


    @FunctionalInterface
    interface Cleanup
    {
        void run() throws IOException;
    }


    final class CleanupScope
    {
        //* === Atributos e Variáveis === *
        private final Object[] identities = new Object[5];
        private final Cleanup[] actions = new Cleanup[5];
        private int size;



        //* === Lógica === *

        void own(Object identity, Cleanup action)
        {
            Objects.requireNonNull(identity, "resource");
            Objects.requireNonNull(action, "cleanup");
            for(int index = 0; index < size; index++)
            {
                if(identities[index] == identity)
                    return;
            }
            if(size == identities.length)
                throw new IllegalStateException("Transient resource bound exceeded");
            identities[size] = identity;
            actions[size++] = action;
        }


        Throwable finish(Throwable primary)
        {
            while(size > 0)
            {
                int index = --size;
                Cleanup action = actions[index];
                identities[index] = null;
                actions[index] = null;
                try
                {
                    action.run();

                }catch(IOException | RuntimeException | Error secondary){
                    primary = mergeCleanup(primary, secondary);
                }
            }
            return primary;
        }
    }


    private record Dimensions(int width, int height)
    {
    }


    private static final class Warning
    {
        //* === Atributos e Variáveis === *
        private boolean received;
        private String first;



        //* === Lógica === *

        private void receive(String message)
        {
            if(!received)
                first = bounded(String.valueOf(message));
            received = true;
        }
    }
}
