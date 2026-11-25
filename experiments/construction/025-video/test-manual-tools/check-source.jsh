import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;
import pt.uminho.esr.ott.server.VideoSource;

class SourceChecks
{
    static void require(boolean condition, String detail)
    {
        if(!condition)
            throw new AssertionError(detail);
    }

    static String hex(byte[] bytes) throws Exception
    {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    static void frame(VideoSource.Frame value, long index, long offset, byte[] expected)
    {
        require(value.index() == index, "frame index");
        require(value.prefixOffset() == offset, "prefix offset");
        require(value.length() == expected.length, "frame length");
        require(Arrays.equals(value.bytes(), expected), "original JPEG bytes");
    }

    static void local(Path directory, String mode) throws Exception
    {
        byte[] rgb = Files.readAllBytes(directory.resolve("RGB.jpg"));
        byte[] gray = Files.readAllBytes(directory.resolve("GRAY.jpg"));
        if(mode.equals("frames"))
        {
            try(VideoSource source = VideoSource.open(directory, Path.of("two.Mjpeg"), 99999))
            {
                frame(source.nextFrame().orElseThrow(), 0, 0, rgb);
                frame(source.nextFrame().orElseThrow(), 1, 5 + rgb.length, gray);
                require(source.nextFrame().isEmpty(), "EOF");
                require(source.state() == VideoSource.State.END, "END");
                require(source.nextFrame().isEmpty(), "repeated EOF");
                source.close();
                source.close();
                require(source.state() == VideoSource.State.END, "close after EOF");
            }
            System.out.println("VS_CHECK_PASS frames=2 originalBytes=true EOF=true state=END");
            return;
        }
        if(mode.equals("errors"))
        {
            String[] names = {"bad-prefix", "short-prefix", "short-body", "oversize", "bad-jpeg"};
            VideoSource.Reason[] reasons = {VideoSource.Reason.INVALID_PREFIX,
                    VideoSource.Reason.TRUNCATED_PREFIX, VideoSource.Reason.TRUNCATED_BODY,
                    VideoSource.Reason.FRAME_LIMIT, VideoSource.Reason.INVALID_JPEG};
            for(int index = 0; index < names.length; index++)
            {
                try(VideoSource source = VideoSource.open(directory, Path.of(names[index] + ".Mjpeg"), rgb.length))
                {
                    VideoSource.SourceException first;
                    try
                    {
                        source.nextFrame();
                        throw new AssertionError("Expected " + reasons[index]);
                    }catch(VideoSource.SourceException expected){
                        first = expected;
                    }
                    require(first.reason() == reasons[index], "typed negative " + names[index]);
                    require(source.state() == VideoSource.State.FAILED, "FAILED");
                    source.close();
                    try
                    {
                        source.nextFrame();
                        throw new AssertionError("Expected same terminal");
                    }catch(VideoSource.SourceException repeated){
                        require(repeated == first, "same primary identity");
                    }
                    System.out.println("VS_EXPECTED_NEGATIVE case=" + names[index] + " reason="
                            + first.reason() + " primaryStable=true detail=" + first.diagnostic().detail());
                }
            }
            return;
        }
        if(mode.equals("isolation"))
        {
            try(VideoSource left = VideoSource.open(directory, Path.of("two.Mjpeg"), 99999);
                    VideoSource right = VideoSource.open(directory, Path.of("two.Mjpeg"), 99999))
            {
                VideoSource.Frame first = left.nextFrame().orElseThrow();
                byte[] altered = first.bytes();
                Arrays.fill(altered, (byte) 0);
                frame(first, 0, 0, rgb);
                left.close();
                frame(right.nextFrame().orElseThrow(), 0, 0, rgb);
                frame(right.nextFrame().orElseThrow(), 1, 5 + rgb.length, gray);
                require(right.nextFrame().isEmpty(), "independent EOF");
            }
            System.out.println("VS_CHECK_PASS isolation=true immutableFrame=true");
            return;
        }
        if(mode.equals("lifecycle"))
        {
            VideoSource closed = VideoSource.open(directory, Path.of("two.Mjpeg"), 99999);
            closed.close();
            closed.close();
            try
            {
                closed.nextFrame();
                throw new AssertionError("Expected CLOSED_USE");
            }catch(VideoSource.SourceException expected){
                require(expected.reason() == VideoSource.Reason.CLOSED_USE, "CLOSED_USE");
            }
            try(VideoSource empty = VideoSource.open(directory, Path.of("empty.Mjpeg"), 99999))
            {
                require(empty.nextFrame().isEmpty(), "empty boundary EOF");
                require(empty.state() == VideoSource.State.END, "empty END");
                empty.close();
                require(empty.nextFrame().isEmpty(), "empty repeated EOF");
            }
            System.out.println("VS_CHECK_PASS explicitClose=true CLOSED_USE=true emptyBoundary=true");
            return;
        }
        throw new IllegalArgumentException("Unknown check " + mode);
    }

    static long number(String json, String key)
    {
        Matcher match = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)").matcher(json);
        require(match.find(), "audit numeric field " + key);
        return Long.parseLong(match.group(1));
    }

    static String hashField(String json)
    {
        Matcher match = Pattern.compile("\"jpeg_sha256\"\\s*:\\s*\"([0-9a-f]{64})\"").matcher(json);
        require(match.find(), "audit JPEG hash");
        return match.group(1);
    }

    static class CountedInput extends FilterInputStream
    {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        long bytes;
        int closeAttempts;
        boolean closed;

        CountedInput(Path path) throws Exception
        {
            super(Files.newInputStream(path));
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException
        {
            int count = in.read(buffer, offset, length);
            if(count > 0)
            {
                bytes += count;
                digest.update(buffer, offset, count);
            }
            return count;
        }

        @Override
        public int read() throws IOException
        {
            int value = in.read();
            if(value >= 0)
            {
                bytes++;
                digest.update((byte) value);
            }
            return value;
        }

        @Override
        public void close() throws IOException
        {
            closeAttempts++;
            in.close();
            closed = true;
        }
    }

    static void full() throws Exception
    {
        Path path = Path.of(System.getenv("VS_MEDIA"));
        Path audit = Path.of(System.getenv("VS_AUDIT"));
        long expectedFrames = Long.parseLong(System.getenv("VS_FRAMES"));
        long expectedBytes = Long.parseLong(System.getenv("VS_BYTES"));
        String expectedHash = System.getenv("VS_SHA256");
        CountedInput input = new CountedInput(path);
        VideoSource source = null;
        long frames = 0;
        long offset = 0;
        try
        {
            var factory = VideoSource.class.getDeclaredMethod("fromOwnedInput", InputStream.class, String.class, int.class);
            factory.setAccessible(true);
            source = (VideoSource) factory.invoke(null, input, path.toString(), 99999);
            try(VideoSource owned = source; BufferedReader lines = Files.newBufferedReader(audit))
            {
                Optional<VideoSource.Frame> next;
                while((next = owned.nextFrame()).isPresent())
                {
                    VideoSource.Frame value = next.orElseThrow();
                    String expected = lines.readLine();
                    require(expected != null, "audit exhausted before source");
                    require(value.index() == frames && value.index() == number(expected, "index"), "full index");
                    require(value.prefixOffset() == offset && offset == number(expected, "offset"), "full offset");
                    require(value.length() == number(expected, "length"), "full record length");
                    require(hex(value.bytes()).equals(hashField(expected)), "full JPEG SHA256");
                    frames++;
                    offset += 5L + value.length();
                    if(frames % 1000 == 0)
                        System.out.println("VS_PROGRESS frames=" + frames + " bytes=" + input.bytes);
                }
                require(lines.readLine() == null, "source exhausted before audit");
                require(frames == expectedFrames, "approved frame count");
                require(offset == expectedBytes && input.bytes == expectedBytes, "approved bytes");
                require(owned.state() == VideoSource.State.END && owned.nextFrame().isEmpty(), "full EOF END");
                require(input.closeAttempts == 1 && input.closed, "input closed exactly once at EOF");
                owned.close();
                owned.close();
                require(input.closeAttempts == 1, "no duplicate close");
                require(HexFormat.of().formatHex(input.digest.digest()).equals(expectedHash), "hash of bytes consumed by VideoSource");
            }
            System.out.println("VS_FULL_PASS frames=" + frames + " bytes=" + input.bytes
                    + " hash=" + expectedHash + " EOF=true state=END inputCloseAttempts=" + input.closeAttempts
                    + " inputClosed=" + input.closed + " perFrameAudit=true");
        }finally{
            if(source == null)
                input.close();
            System.out.println("VS_CLEANUP_RECORDED frames=" + frames + " bytes=" + input.bytes
                    + " inputCloseAttempts=" + input.closeAttempts + " inputClosed=" + input.closed);
        }
    }
}

try
{
    String mode = System.getenv("VS_MODE");
    if(mode.equals("full"))
        SourceChecks.full();
    else
        SourceChecks.local(Path.of(System.getenv("VS_FIXTURES")), mode);
    System.out.println("VS_SCRIPT_EXIT=0");
    System.exit(0);
}catch(Throwable failure){
    failure.printStackTrace();
    System.out.println("VS_SCRIPT_EXIT=1");
    System.exit(1);
}
