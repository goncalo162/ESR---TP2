package pt.uminho.esr.ott.server;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pt.uminho.esr.ott.server.VideoSource.Diagnostic;
import pt.uminho.esr.ott.server.VideoSource.Frame;
import pt.uminho.esr.ott.server.VideoSource.Reason;
import pt.uminho.esr.ott.server.VideoSource.SourceException;
import pt.uminho.esr.ott.server.VideoSource.Stage;
import pt.uminho.esr.ott.server.VideoSource.State;

class VideoSourceTest
{
    //* === Atributos e Variáveis === *
    // Independent Pillow 9.0.1 fixtures; provenance/hashes are preserved in implement-01.
    private static final byte[] RGB = Base64.getDecoder().decode("/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAUDBAQEAwUEBAQFBQUGBwwIBwcHBw8LCwkMEQ8SEhEPERETFhwXExQaFRERGCEYGh0dHx8fExciJCIeJBweHx7/2wBDAQUFBQcGBw4ICA4eFBEUHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh7/wAARCAADAAQDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwCWw0XSNK0jT7ew0y0hia3DlfJVuckZ5z6CiiiuzLoRlhYOSu7H4tmvFGd0MXOnSxlWMVayVSaS0WyTP//Z");
    private static final byte[] GRAY = Base64.getDecoder().decode("/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAUDBAQEAwUEBAQFBQUGBwwIBwcHBw8LCwkMEQ8SEhEPERETFhwXExQaFRERGCEYGh0dHx8fExciJCIeJBweHx7/wAALCAACAAMBAREA/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/9oACAEBAAA/APN/BMEP/CKad+5j/wBSP4R6mv/Z");
    private static final byte[] PROGRESSIVE = Base64.getDecoder().decode("/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAUDBAQEAwUEBAQFBQUGBwwIBwcHBw8LCwkMEQ8SEhEPERETFhwXExQaFRERGCEYGh0dHx8fExciJCIeJBweHx7/2wBDAQUFBQcGBw4ICA4eFBEUHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh7/wgARCAASABQDASIAAhEBAxEB/8QAGAABAQEBAQAAAAAAAAAAAAAAAAQDBQL/xAAZAQACAwEAAAAAAAAAAAAAAAABBAIDBQb/2gAMAwEAAhADEAAAAZ7POAGyIxPuSFS/IGl0v//EABwQAAICAgMAAAAAAAAAAAAAAAIDARMEEgURI//aAAgBAQABBQJYLSS4ZaE9y3YGsrtygmzAWvfmiJIq9Y//xAAmEQABAQQKAwAAAAAAAAAAAAABAAIRITIDBBMxM0FRcXKBobLw/9oACAEDAQE/AZhqHb/QPhW4F9CCeREc4ORnZ79WlUsBlf/EACIRAAECBAcBAAAAAAAAAAAAAAEAAgQRccESITIzNGGC0f/aAAgBAgEBPwHVkRfyLlNwETMRLqTviHKbVyg9hlF//8QAJxAAAgEDAQcFAQAAAAAAAAAAAQIDABESIQQTIiMxM2FBUVJxgaL/2gAIAQEABj8CMSLs/CvojG1/I/KZI1mxyw4YVtT4toGsMtqsaV2MoEhyyeXd9PFKeVnhkXN5Lk+RQESMVCjtxC39UeWvb9vul3RMd+uOnxotLxnI6trX/8QAHhABAAIDAAIDAAAAAAAAAAAAAREhADFBYYGxweH/2gAIAQEAAT8hqoI+h5YfxjVCMIQbQm43u8WQTbgCiTjWskqQAVrRrSZEwbsuiDhwrIhNNL8s88fOGNwsnb0znmBYifOf/9oADAMBAAIAAwAAABAo4D7/xAAeEQEAAgEEAwAAAAAAAAAAAAABESEAEDFBUXGhwf/aAAgBAwEBPxB7lN1VIF3kIomSao4VOVKh7EoueXzoN633P//EABwRAQEAAgIDAAAAAAAAAAAAAAERACFRYYGh8P/aAAgBAgEBPxDYoaBNeFBq1lQYiUEajwQOhI11nx+TPWZ//8QAHBABAQEAAwEBAQAAAAAAAAAAAREhADFhQVGR/9oACAEBAAE/EFd4BfIkwTFIU6VjbELgL2PYPsO+3bIi7G1yFpjeCK56hdKrupG2la2XCY9CRBpV0d481RwhSVCN7e03H4KdVjv6eOKNiRbCVxZX+vPK4zBsGmVWevP/2Q==");

    private static final byte[] PROFILE_A = Base64.getDecoder().decode("/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAUDBAQEAwUEBAQFBQUGBwwIBwcHBw8LCwkMEQ8SEhEPERETFhwXExQaFRERGCEYGh0dHx8fExciJCIeJBweHx7/wAALCAEgAYABAREA/8QAFAABAAAAAAAAAAAAAAAAAAAAAP/EABQQAQAAAAAAAAAAAAAAAAAAAAD/2gAIAQEAAD8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/9k=");
    private static final byte[] PROFILE_BCD = Base64.getDecoder().decode("/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAUDBAQEAwUEBAQFBQUGBwwIBwcHBw8LCwkMEQ8SEhEPERETFhwXExQaFRERGCEYGh0dHx8fExciJCIeJBweHx7/2wBDAQUFBQcGBw4ICA4eFBEUHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh7/wAARCAFoAoADASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAf/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFgEBAQEAAAAAAAAAAAAAAAAAAAYH/8QAFBEBAAAAAAAAAAAAAAAAAAAAAP/aAAwDAQACEQMRAD8AmIDQEOAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/9k=");

    @TempDir
    Path temporary;



    //* === Lógica === *

    @Test
    void M01ReadsIndependentPayloadsWithExactPositions() throws Exception
    {
        assertEquals("c34d121b290aeca37488d68e5d2833343c617517687de7eda1807fd2cfe76f55", sha256(RGB));
        assertEquals("a7675704614c07524c547d988430461dac2ee2b8b0472e3354a4e308c8347e4e", sha256(GRAY));
        TrackingInput input = new TrackingInput(records(RGB, GRAY));
        try(VideoSource source = owned(input))
        {
            assertEquals(State.OPEN, source.state());
            assertFrame(source.nextFrame().orElseThrow(), 0, 0, RGB);
            assertFrame(source.nextFrame().orElseThrow(), 1, RGB.length + 5, GRAY);
            assertEquals(0, input.closeCalls);
            assertTrue(source.nextFrame().isEmpty());
            assertEquals(State.END, source.state());
        }
        assertEquals(1, input.closeCalls);
        assertEquals(0, input.singleCalls);
    }


    @Test
    void M02PositiveShortReadsPreserveBytesAndBoundaries() throws Exception
    {
        for(int chunk = 1; chunk <= 3; chunk++)
        {
            TrackingInput input = new TrackingInput(records(RGB, GRAY));
            input.maxChunk = chunk;
            try(VideoSource source = owned(input))
            {
                assertFrame(source.nextFrame().orElseThrow(), 0, 0, RGB);
                assertFrame(source.nextFrame().orElseThrow(), 1, RGB.length + 5, GRAY);
                assertTrue(source.nextFrame().isEmpty());
            }
            assertEquals(1, input.closeCalls);
            assertEquals(0, input.singleCalls);
        }
    }


    @Test
    void M03EmptyAndRecordBoundaryEofAreIdempotent() throws Exception
    {
        for(byte[] content : new byte[][] {new byte[0], records(GRAY)})
        {
            TrackingInput input = new TrackingInput(content);
            VideoSource source = owned(input);
            if(content.length > 0)
                assertFrame(source.nextFrame().orElseThrow(), 0, 0, GRAY);
            assertTrue(source.nextFrame().isEmpty());
            int reads = input.bulkCalls;
            assertTrue(source.nextFrame().isEmpty());
            source.close();
            source.close();
            assertTrue(source.nextFrame().isEmpty());
            assertEquals(State.END, source.state());
            assertEquals(reads, input.bulkCalls);
            assertEquals(1, input.closeCalls);
        }
    }


    @Test
    void M04EveryPartialPrefixFailsAtFirstOrSecondRecord() throws Exception
    {
        byte[] prefix = ascii("00684");
        for(int cut = 1; cut <= 4; cut++)
        {
            for(boolean second : new boolean[] {false, true})
            {
                byte[] partial = Arrays.copyOf(prefix, cut);
                TrackingInput input = new TrackingInput(second ? join(records(GRAY), partial) : partial);
                try(VideoSource source = owned(input))
                {
                    Frame previous = second ? source.nextFrame().orElseThrow() : null;
                    SourceException error = assertThrows(SourceException.class, source::nextFrame);
                    assertDiagnostic(error, Reason.TRUNCATED_PREFIX, Stage.PREFIX,
                            second ? 1 : 0, second ? GRAY.length + 5 : 0, 5, cut, cut);
                    assertTerminal(source, input, error);
                    if(previous != null)
                        assertFrame(previous, 0, 0, GRAY);
                }
            }
        }
    }


    @Test
    void M05RejectsEveryNonDecimalPositionWithoutReadingAgain() throws Exception
    {
        for(int position = 0; position < 5; position++)
        {
            for(int bad : new int[] {0, ' ', '\n', '\r', '+', '-', '/', ':', 0x80, 0xff})
            {
                byte[] prefix = ascii("00684");
                prefix[position] = (byte) bad;
                TrackingInput input = new TrackingInput(prefix);
                input.maxChunk = 1;
                try(VideoSource source = owned(input))
                {
                    SourceException error = assertThrows(SourceException.class, source::nextFrame);
                    assertEquals(Reason.INVALID_PREFIX, error.reason());
                    assertEquals(bad, error.diagnostic().observed());
                    assertEquals("BYTE_INDEX=" + position, error.diagnostic().detail());
                    assertEquals(position + 1, input.position);
                    assertTerminal(source, input, error);
                }
            }
        }
    }


    @Test
    void M06RejectsZeroAndOversizeBeforeBodyOrDecoder() throws Exception
    {
        for(int length : new int[] {0, RGB.length, 99999})
        {
            TrackingInput input = new TrackingInput(join(prefix(length), RGB));
            AtomicInteger creations = new AtomicInteger();
            try(VideoSource source = VideoSource.fromOwnedInput(input, "limit", RGB.length - 1, cache ->
            {
                creations.incrementAndGet();
                return new ReaderSpy(4, 3);
            }))
            {
                SourceException error = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(length == 0 ? Reason.INVALID_LENGTH : Reason.FRAME_LIMIT, error.reason());
                assertEquals(5, input.position);
                assertEquals(0, creations.get());
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M06AcceptsBMinusOneAndBAndValidJpegAt99999() throws Exception
    {
        for(int bound : new int[] {RGB.length, RGB.length + 1})
        {
            try(VideoSource source = VideoSource.fromOwnedInput(new TrackingInput(records(RGB)), "bound", bound))
            {
                assertFrame(source.nextFrame().orElseThrow(), 0, 0, RGB);
            }
        }
        byte[] padded = withComments(RGB, 99999);
        try(VideoSource source = owned(new TrackingInput(records(padded))))
        {
            assertFrame(source.nextFrame().orElseThrow(), 0, 0, padded);
            assertTrue(source.nextFrame().isEmpty());
        }
    }


    @Test
    void M07EveryBodyTruncationFailsWithoutPartialPublication() throws Exception
    {
        for(boolean second : new boolean[] {false, true})
        {
            byte[] payload = second ? GRAY : RGB;
            for(int cut = 0; cut < payload.length; cut++)
            {
                byte[] partial = join(prefix(payload.length), Arrays.copyOf(payload, cut));
                TrackingInput input = new TrackingInput(second ? join(records(RGB), partial) : partial);
                try(VideoSource source = owned(input))
                {
                    Frame previous = second ? source.nextFrame().orElseThrow() : null;
                    SourceException error = assertThrows(SourceException.class, source::nextFrame);
                    assertDiagnostic(error, Reason.TRUNCATED_BODY, Stage.BODY,
                            second ? 1 : 0, second ? RGB.length + 5 : 0, payload.length, cut, cut);
                    assertTerminal(source, input, error);
                    if(previous != null)
                        assertFrame(previous, 0, 0, RGB);
                }
            }
        }
    }


    @Test
    void M08RejectsMalformedJpegBoundariesBeforeProviderCreation() throws Exception
    {
        List<byte[]> malformed = List.of(new byte[] {1}, ascii("not jpeg"), new byte[] {-1, -40, -1, -39},
                Arrays.copyOf(RGB, RGB.length - 2), join(RGB, GRAY), join(RGB, new byte[] {0}),
                join(RGB, new byte[] {0, -1, -39}), new byte[] {-1, -40, -1, -32, 0, 1, -1, -39},
                new byte[] {-1, -40, -1, -32, 127, -1, -1, -39},
                new byte[] {-1, -40, -1, -40, -1, -39});
        for(byte[] payload : malformed)
        {
            AtomicInteger creations = new AtomicInteger();
            TrackingInput input = new TrackingInput(records(payload));
            try(VideoSource source = VideoSource.fromOwnedInput(input, "malformed", 99999, cache ->
            {
                creations.incrementAndGet();
                return new ReaderSpy(4, 3);
            }))
            {
                SourceException error = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(Reason.INVALID_JPEG, error.reason());
                assertEquals(Stage.JPEG, error.diagnostic().stage());
                assertEquals(0, creations.get());
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M08RealDecoderAcceptsProgressiveAndEmbeddedMarkerComments() throws Exception
    {
        assertEquals("98bf0848666780c4d1d53877c83648d6e152ea1380ea9f83f7db6d369b298c2d", sha256(PROGRESSIVE));
        byte[] commented = withComments(GRAY, GRAY.length + 128);
        try(VideoSource source = owned(new TrackingInput(records(PROGRESSIVE, commented))))
        {
            assertFrame(source.nextFrame().orElseThrow(), 0, 0, PROGRESSIVE);
            assertFrame(source.nextFrame().orElseThrow(), 1, PROGRESSIVE.length + 5, commented);
            assertTrue(source.nextFrame().isEmpty());
        }
    }


    @Test
    void M08ScannerRecognizesStuffingRestartsAndMultipleScans() throws Exception
    {
        byte[] structural = join(new byte[] {-1, -40, -1, -60, 0, 2, -1, -52, 0, 2},
                sof(4, 3), sos(), new byte[] {12, -1, 0, 34, -1, -48, 56},
                sos(), new byte[] {78, -1, 0, 90, -1, -41, -1, -39});
        ReaderSpy reader = new ReaderSpy(4, 3);
        try(VideoSource source = controlled(new TrackingInput(records(structural)), reader))
        {
            assertFrame(source.nextFrame().orElseThrow(), 0, 0, structural);
            assertEquals(1, reader.readCalls);
        }
        // This is a scanner oracle with a controlled decoder, not a real entropy/decode success.
    }


    @Test
    void M08RealDecoderRejectsStructurallyFramedButInvalidEntropy() throws Exception
    {
        byte[] structuralOnly = structuralJpeg(4, 3);
        TrackingInput input = new TrackingInput(records(structuralOnly));
        try(VideoSource source = owned(input))
        {
            SourceException error = assertThrows(SourceException.class, source::nextFrame);
            assertEquals(Reason.INVALID_JPEG, error.reason());
            assertInstanceOf(IOException.class, error.getCause());
            assertTerminal(source, input, error);
        }
    }


    @Test
    void M08MissingProviderAndCheckedFactoryFailureAreUnavailable() throws Exception
    {
        IOException failed = new IOException("factory checked");
        for(boolean throwsChecked : new boolean[] {false, true})
        {
            TrackingInput input = new TrackingInput(records(RGB));
            ImageInputStream[] borrowed = new ImageInputStream[1];
            try(VideoSource source = VideoSource.fromOwnedInput(input, "factory", 99999, cache ->
            {
                borrowed[0] = cache;
                if(throwsChecked)
                    throw failed;
                return null;
            }))
            {
                SourceException error = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(Reason.DECODER_UNAVAILABLE, error.reason());
                assertSame(throwsChecked ? failed : null, error.getCause());
                assertCacheClosed(borrowed[0]);
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M08CheckedDecodeFailureAndAnyWarningRejectPublication() throws Exception
    {
        for(boolean checked : new boolean[] {false, true})
        {
            TrackingInput input = new TrackingInput(records(RGB));
            ReaderSpy reader = new ReaderSpy(4, 3);
            IOException failure = new IOException("decode checked");
            reader.readFailure = checked ? failure : null;
            reader.warning = checked ? null : "unclassified warning ".repeat(100);
            try(VideoSource source = controlled(input, reader))
            {
                SourceException error = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(Reason.INVALID_JPEG, error.reason());
                assertSame(checked ? failure : null, error.getCause());
                assertTrue(error.diagnostic().detail().length() <= 256);
                assertReaderReleased(reader);
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M08NullAndDistinctImagesAreUnexpectedAndBothAreCleaned() throws Exception
    {
        for(int mode : new int[] {1, 2})
        {
            TrackingInput input = new TrackingInput(records(RGB));
            ReaderSpy reader = new ReaderSpy(4, 3);
            reader.returnMode = mode;
            reader.extra = new TrackingImage();
            try(VideoSource source = controlled(input, reader))
            {
                IllegalStateException error = assertThrows(IllegalStateException.class, source::nextFrame);
                assertTrue(error.getMessage().contains("supplied destination"));
                assertReaderReleased(reader);
                assertEquals(mode == 2 ? 1 : 0, reader.extra.flushCalls);
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M09ReadFailuresPreserveCauseAtPrefixAndBody() throws Exception
    {
        for(int faultAt : new int[] {0, 1, 4, 5, 7})
        {
            TrackingInput input = new TrackingInput(records(RGB));
            IOException failure = new IOException("read at " + faultAt);
            input.faultAt = faultAt;
            input.readFailure = failure;
            try(VideoSource source = owned(input))
            {
                SourceException error = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(Reason.READ_IO, error.reason());
                assertEquals(faultAt < 5 ? Stage.PREFIX : Stage.BODY, error.diagnostic().stage());
                assertEquals(faultAt < 5 ? faultAt : faultAt - 5, error.diagnostic().consumedInStage());
                assertSame(failure, error.getCause());
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M09CheckedPrimarySurvivesUncheckedCleanupAndTryWithResources() throws Exception
    {
        TrackingInput input = new TrackingInput(records(RGB));
        ReaderSpy reader = new ReaderSpy(4, 3);
        IOException original = new IOException("primary read");
        RuntimeException dispose = new IllegalStateException("secondary dispose");
        SyntheticFatal close = new SyntheticFatal("secondary close");
        reader.readFailure = original;
        reader.disposeFailure = dispose;
        input.closeFailure = close;
        VideoSource source = controlled(input, reader);
        SourceException error = assertThrows(SourceException.class, () ->
        {
            try(source)
            {
                source.nextFrame();
            }
        });
        assertEquals(Reason.INVALID_JPEG, error.reason());
        assertSame(original, error.getCause());
        assertArrayEquals(new Throwable[] {dispose, close}, error.getSuppressed());
        assertReaderReleased(reader);
        assertTerminal(source, input, error);
    }


    @Test
    void M09UncheckedReadAndProviderFailuresKeepIdentity() throws Exception
    {
        for(boolean atProvider : new boolean[] {false, true})
        {
            for(Throwable primary : new Throwable[] {new IllegalArgumentException("runtime"), new SyntheticFatal("fatal")})
            {
                TrackingInput input = new TrackingInput(records(RGB));
                ReaderSpy reader = new ReaderSpy(4, 3);
                if(atProvider)
                    reader.readFailure = primary;
                else
                    input.readFailure = primary;
                input.faultAt = atProvider ? Integer.MAX_VALUE : 0;
                IOException secondary = new IOException("secondary close");
                input.closeFailure = secondary;
                try(VideoSource source = controlled(input, reader))
                {
                    Throwable error = assertThrows(primary.getClass(), source::nextFrame);
                    assertSame(primary, error);
                    assertArrayEquals(new Throwable[] {secondary}, error.getSuppressed());
                    if(atProvider)
                        assertReaderReleased(reader);
                    assertTerminal(source, input, error);
                }
            }
        }
    }


    @Test
    void M09SameIdentityReadAndCloseDoesNotSelfSuppress() throws Exception
    {
        for(Throwable primary : new Throwable[] {new IOException("same checked"), new SyntheticFatal("same fatal")})
        {
            TrackingInput input = new TrackingInput(records(RGB));
            input.readFailure = primary;
            input.closeFailure = primary;
            input.faultAt = 0;
            VideoSource source = owned(input);
            Class<? extends Throwable> expected = primary instanceof IOException ? SourceException.class : SyntheticFatal.class;
            Throwable error = assertThrows(expected, () ->
            {
                try(source)
                {
                    source.nextFrame();
                }
            });
            if(primary instanceof IOException)
                assertSame(primary, error.getCause());
            else
                assertSame(primary, error);
            assertEquals(0, error.getSuppressed().length);
            assertTerminal(source, input, error);
        }
    }


    @Test
    void M09DistinctReturnedImageCleanupFailureNeverMasksPostcondition() throws Exception
    {
        TrackingInput input = new TrackingInput(records(RGB));
        ReaderSpy reader = new ReaderSpy(4, 3);
        reader.returnMode = 2;
        reader.extra = new TrackingImage();
        SyntheticFatal flush = new SyntheticFatal("extra flush");
        IllegalStateException dispose = new IllegalStateException("reader dispose");
        IOException close = new IOException("source close");
        reader.extra.flushFailure = flush;
        reader.disposeFailure = dispose;
        input.closeFailure = close;
        try(VideoSource source = controlled(input, reader))
        {
            IllegalStateException primary = assertThrows(IllegalStateException.class, source::nextFrame);
            assertTrue(primary.getMessage().contains("supplied destination"));
            assertArrayEquals(new Throwable[] {flush, dispose, close}, primary.getSuppressed());
            assertEquals(1, reader.extra.flushCalls);
            assertReaderReleased(reader);
            assertTerminal(source, input, primary);
        }
    }


    @Test
    void M09DuplicateSecondaryIdentityIsRecordedOnce() throws Exception
    {
        TrackingInput input = new TrackingInput(records(RGB));
        ReaderSpy reader = new ReaderSpy(4, 3);
        IOException read = new IOException("read");
        SyntheticFatal repeated = new SyntheticFatal("both cleanups");
        reader.readFailure = read;
        reader.disposeFailure = repeated;
        input.closeFailure = repeated;
        try(VideoSource source = controlled(input, reader))
        {
            SourceException primary = assertThrows(SourceException.class, source::nextFrame);
            assertSame(read, primary.getCause());
            assertArrayEquals(new Throwable[] {repeated}, primary.getSuppressed());
            assertReaderReleased(reader);
            assertTerminal(source, input, primary);
        }
    }


    @Test
    void M10CloseBeforeReadAndAfterFrameIsIdempotent() throws Exception
    {
        for(boolean afterFrame : new boolean[] {false, true})
        {
            TrackingInput input = new TrackingInput(records(RGB, GRAY));
            VideoSource source = owned(input);
            if(afterFrame)
                assertFrame(source.nextFrame().orElseThrow(), 0, 0, RGB);
            int reads = input.bulkCalls;
            source.close();
            source.close();
            assertEquals(State.CLOSED, source.state());
            assertEquals(Reason.CLOSED_USE, assertThrows(SourceException.class, source::nextFrame).reason());
            assertEquals(1, input.closeCalls);
            assertEquals(reads, input.bulkCalls);
        }
    }


    @Test
    void M10CloseAsFirstFailureKeepsCheckedOrUncheckedCategory() throws Exception
    {
        for(boolean eof : new boolean[] {false, true})
        {
            for(Throwable first : new Throwable[] {new IOException("close"),
                    new IllegalStateException("close runtime"), new SyntheticFatal("close fatal")})
            {
                TrackingInput input = new TrackingInput(new byte[0]);
                input.closeFailure = first;
                VideoSource source = owned(input);
                Class<? extends Throwable> expected = first instanceof IOException ? SourceException.class : first.getClass();
                Throwable error = assertThrows(expected, () ->
                {
                    if(eof)
                        source.nextFrame();
                    else
                        source.close();
                });
                if(first instanceof IOException)
                {
                    assertEquals(Reason.CLOSE_IO, ((SourceException) error).reason());
                    assertSame(first, error.getCause());
                    assertEquals(Stage.CLOSE, ((SourceException) error).diagnostic().stage());

                }else{
                    assertSame(first, error);
                }
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M10TransientCleanupAsFirstFailurePreventsFramePublication() throws Exception
    {
        for(Throwable first : new Throwable[] {new IllegalStateException("dispose first"), new SyntheticFatal("dispose first")})
        {
            TrackingInput input = new TrackingInput(records(RGB));
            ReaderSpy reader = new ReaderSpy(4, 3);
            reader.disposeFailure = first;
            try(VideoSource source = controlled(input, reader))
            {
                Throwable error = assertThrows(first.getClass(), source::nextFrame);
                assertSame(first, error);
                assertReaderReleased(reader);
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M10CleanupAttemptsAllDistinctResourcesOnceInReverseOrder() throws Exception
    {
        try(VideoSource source = owned(new TrackingInput(new byte[0])))
        {
            VideoSource.CleanupScope resources = source.new CleanupScope();
            List<Integer> cleaned = new ArrayList<>();
            TrackingImage destination = new TrackingImage();
            TrackingImage extra = new TrackingImage();
            Object payload = new Object();
            Object cache = new Object();
            Object reader = new Object();
            IOException first = new IOException("extra checked cleanup");
            IllegalStateException second = new IllegalStateException("reader cleanup");
            resources.own(payload, () -> cleaned.add(0));
            resources.own(cache, () -> cleaned.add(1));
            resources.own(reader, () ->
            {
                cleaned.add(2);
                throw second;
            });
            resources.own(destination, () ->
            {
                cleaned.add(3);
                destination.flush();
            });
            resources.own(destination, () -> fail("same destination must not be owned twice"));
            resources.own(extra, () ->
            {
                cleaned.add(4);
                extra.flush();
                throw first;
            });
            Throwable primary = resources.finish(null);
            assertInstanceOf(SourceException.class, primary);
            assertEquals(Reason.CLOSE_IO, ((SourceException) primary).reason());
            assertSame(first, primary.getCause());
            assertArrayEquals(new Throwable[] {second}, primary.getSuppressed());
            assertEquals(List.of(4, 3, 2, 1, 0), cleaned);
            assertEquals(1, destination.flushCalls);
            assertEquals(1, extra.flushCalls);
            assertSame(primary, resources.finish(primary));
            assertEquals(5, cleaned.size());
        }
    }


    @Test
    void M10FiveTransientAndOneSessionCleanupFailureStayBounded() throws Exception
    {
        TrackingInput input = new TrackingInput(new byte[0]);
        IOException closeFailure = new IOException("session close");
        input.closeFailure = closeFailure;
        VideoSource source = owned(input);
        IOException read = new IOException("first");
        List<Throwable> secondaries = new ArrayList<>();
        IOException observed = assertThrows(IOException.class, () ->
        {
            try(source)
            {
                VideoSource.CleanupScope resources = source.new CleanupScope();
                for(int index = 0; index < 5; index++)
                {
                    IOException secondary = new IOException("cleanup " + index);
                    secondaries.add(secondary);
                    resources.own(new Object(), () ->
                    {
                        throw secondary;
                    });
                }
                assertSame(read, resources.finish(read));
                assertEquals(5, read.getSuppressed().length);
                assertSame(read, resources.finish(read));
                throw read;
            }
        });
        assertSame(read, observed);
        assertEquals(6, read.getSuppressed().length);
        for(int index = 0; index < 5; index++)
        {
            assertSame(secondaries.get(4 - index), read.getSuppressed()[index]);
        }
        SourceException terminal = assertInstanceOf(SourceException.class, read.getSuppressed()[5]);
        assertEquals(Reason.CLOSE_IO, terminal.reason());
        assertSame(closeFailure, terminal.getCause());
        source.close();
        assertEquals(1, input.closeCalls);
    }


    @Test
    void M09CheckedProviderQueriesRemainFormatErrorsWithOriginalCause() throws Exception
    {
        for(String site : List.of("format", "width", "height", "types"))
        {
            TrackingInput input = new TrackingInput(records(RGB));
            ReaderSpy reader = new ReaderSpy(4, 3);
            IOException original = new IOException("checked " + site);
            reader.faultSite = site;
            reader.operationFailure = original;
            try(VideoSource source = controlled(input, reader))
            {
                SourceException primary = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(Reason.INVALID_JPEG, primary.reason());
                assertSame(original, primary.getCause());
                assertEquals(0, reader.readCalls);
                assertReaderReleased(reader);
                assertTerminal(source, input, primary);
            }
        }
    }


    @Test
    void M09CleanupDoesNotIntroduceCauseCyclesOrSelfSuppression() throws Exception
    {
        try(VideoSource source = owned(new TrackingInput(new byte[0])))
        {
            IOException primary = new IOException("primary");
            IOException containingPrimary = new IOException("secondary already caused by primary", primary);
            VideoSource.CleanupScope resources = source.new CleanupScope();
            resources.own(new Object(), () ->
            {
                throw primary;
            });
            resources.own(new Object(), () ->
            {
                throw containingPrimary;
            });
            assertSame(primary, resources.finish(primary));
            assertEquals(0, primary.getSuppressed().length);
        }
    }


    @Test
    void M10ReaderOwnershipPrecedesEveryProviderOperation() throws Exception
    {
        for(String site : List.of("input", "format", "width", "height", "types", "parameters", "read"))
        {
            TrackingInput input = new TrackingInput(records(RGB));
            ReaderSpy reader = new ReaderSpy(4, 3);
            SyntheticFatal primary = new SyntheticFatal(site);
            reader.faultSite = site;
            reader.operationFailure = primary;
            try(VideoSource source = controlled(input, reader))
            {
                assertSame(primary, assertThrows(SyntheticFatal.class, source::nextFrame));
                assertReaderReleased(reader);
                assertTerminal(source, input, primary);
            }
        }
    }


    @Test
    void M10BorrowedCacheClosesAfterUncheckedFactoryFailure() throws Exception
    {
        TrackingInput input = new TrackingInput(records(RGB));
        ImageInputStream[] cache = new ImageInputStream[1];
        SyntheticFatal primary = new SyntheticFatal("factory");
        try(VideoSource source = VideoSource.fromOwnedInput(input, "factory", 99999, borrowed ->
        {
            cache[0] = borrowed;
            throw primary;
        }))
        {
            assertSame(primary, assertThrows(SyntheticFatal.class, source::nextFrame));
            assertCacheClosed(cache[0]);
            assertTerminal(source, input, primary);
        }
    }


    @Test
    void M11FramesOwnCopiesEvenWhenInputRetainsReadBuffer() throws Exception
    {
        TrackingInput input = new TrackingInput(records(RGB, GRAY));
        try(VideoSource source = owned(input))
        {
            Frame first = source.nextFrame().orElseThrow();
            assertNotNull(input.retainedBody);
            Arrays.fill(input.retainedBody, (byte) 0);
            byte[] exposed = first.bytes();
            Arrays.fill(exposed, (byte) 1);
            assertFrame(first, 0, 0, RGB);
            assertNotSame(first.bytes(), first.bytes());
            assertFrame(source.nextFrame().orElseThrow(), 1, RGB.length + 5, GRAY);
            assertFrame(first, 0, 0, RGB);
        }
    }


    @Test
    void M12InterleavedSessionsDoNotSharePositionFailureOrClose() throws Exception
    {
        TrackingInput leftInput = new TrackingInput(join(records(RGB), ascii("00")));
        TrackingInput rightInput = new TrackingInput(records(GRAY, RGB));
        try(VideoSource left = owned(leftInput); VideoSource right = owned(rightInput))
        {
            assertFrame(left.nextFrame().orElseThrow(), 0, 0, RGB);
            assertFrame(right.nextFrame().orElseThrow(), 0, 0, GRAY);
            assertEquals(Reason.TRUNCATED_PREFIX, assertThrows(SourceException.class, left::nextFrame).reason());
            assertEquals(1, leftInput.closeCalls);
            assertEquals(0, rightInput.closeCalls);
            assertEquals(State.OPEN, right.state());
            assertFrame(right.nextFrame().orElseThrow(), 1, GRAY.length + 5, RGB);
            assertTrue(right.nextFrame().isEmpty());
            assertEquals(State.FAILED, left.state());
        }
        assertEquals(1, rightInput.closeCalls);
    }


    @Test
    void M12TwoSourcesCanOpenSameFileIndependently() throws Exception
    {
        Path file = temporary.resolve("stream.Mjpeg");
        Files.write(file, records(RGB, GRAY));
        try(VideoSource left = VideoSource.open(temporary, file, 99999);
                VideoSource right = VideoSource.open(temporary, file.getFileName(), 99999))
        {
            assertFrame(left.nextFrame().orElseThrow(), 0, 0, RGB);
            left.close();
            assertFrame(right.nextFrame().orElseThrow(), 0, 0, RGB);
            assertFrame(right.nextFrame().orElseThrow(), 1, RGB.length + 5, GRAY);
            assertTrue(right.nextFrame().isEmpty());
        }
    }


    @Test
    void M13RejectsDimensionsBeforeCreatingProviderIncludingLongProduct() throws Exception
    {
        for(int[] size : new int[][] {{0, 3}, {4, 0}, {4097, 1}, {1, 4097},
                {4096, 1025}, {2049, 2048}, {65535, 65535}})
        {
            TrackingInput input = new TrackingInput(records(structuralJpeg(size[0], size[1])));
            AtomicInteger creations = new AtomicInteger();
            try(VideoSource source = VideoSource.fromOwnedInput(input, "dimensions", 99999, cache ->
            {
                creations.incrementAndGet();
                return new ReaderSpy(size[0], size[1]);
            }))
            {
                SourceException error = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(size[0] == 0 || size[1] == 0 ? Reason.INVALID_JPEG : Reason.IMAGE_LIMIT, error.reason());
                assertEquals(0, creations.get());
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M13RechecksDecoderDimensionsBeforeReadOrDestinationAllocation() throws Exception
    {
        for(int[] size : new int[][] {{5, 3}, {4, 4}, {0, 3}, {4097, 3}, {2049, 2048}})
        {
            TrackingInput input = new TrackingInput(records(RGB));
            ReaderSpy reader = new ReaderSpy(size[0], size[1]);
            try(VideoSource source = controlled(input, reader))
            {
                SourceException error = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(size[0] > 4096 || (long) size[0] * size[1] > 4194304
                        ? Reason.IMAGE_LIMIT : Reason.INVALID_JPEG, error.reason());
                assertEquals(0, reader.readCalls);
                assertNull(reader.destination);
                assertReaderReleased(reader);
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M13DestinationUsesCanonicalTypeAndFullDecodeParametersAtBoundaries() throws Exception
    {
        for(int[] size : new int[][] {{4096, 1}, {1, 4096}, {4096, 1024}, {2048, 2048}, {384, 288}, {640, 360}})
        {
            byte[] structural = structuralJpeg(size[0], size[1]);
            ReaderSpy reader = new ReaderSpy(size[0], size[1]);
            reader.imageTypes = List.of(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB));
            try(VideoSource source = controlled(new TrackingInput(records(structural)), reader))
            {
                assertFrame(source.nextFrame().orElseThrow(), 0, 0, structural);
                assertEquals(1, reader.readCalls);
                assertEquals(BufferedImage.TYPE_INT_RGB, reader.destination.getType());
                assertEquals(size[0], reader.destination.getWidth());
                assertEquals(size[1], reader.destination.getHeight());
                DataBuffer buffer = reader.destination.getRaster().getDataBuffer();
                long rasterBytes = (long) buffer.getSize() * buffer.getNumBanks() * 4;
                assertEquals((long) size[0] * size[1] * 4, rasterBytes);
                assertTrue(rasterBytes <= 16777216L);
                assertNull(reader.parameters.getSourceRegion());
                assertNull(reader.parameters.getSourceBands());
                assertNull(reader.parameters.getDestinationBands());
                assertEquals(1, reader.parameters.getSourceXSubsampling());
                assertEquals(1, reader.parameters.getSourceYSubsampling());
                assertEquals(new Point(0, 0), reader.parameters.getDestinationOffset());
                assertEquals(0, reader.parameters.getSourceMinProgressivePass());
                assertEquals(Integer.MAX_VALUE, reader.parameters.getSourceNumProgressivePasses());
                assertSame(reader.destination, reader.parameters.getDestination());
                assertReaderReleased(reader);
            }
        }
    }


    @Test
    void M13RealDecoderAcceptsIndependentFixturesAtApprovedMediaDimensions() throws Exception
    {
        assertEquals("5fa0c8d2e6a8423b597be0621a385111659137adc07451d07c121863d37986fe", sha256(PROFILE_A));
        assertEquals("1f93029e6ec587a9087d5c80d09685ca6512396493a47ddf2e09fb3f4e573a38", sha256(PROFILE_BCD));
        try(VideoSource source = owned(new TrackingInput(records(PROFILE_A, PROFILE_BCD))))
        {
            assertFrame(source.nextFrame().orElseThrow(), 0, 0, PROFILE_A);
            assertFrame(source.nextFrame().orElseThrow(), 1, PROFILE_A.length + 5, PROFILE_BCD);
            assertTrue(source.nextFrame().isEmpty());
        }
        // Same dimensions as the approved audits; these fixtures are not those four videos.
    }


    @Test
    void M13AllCanonicalDestinationTypesAreAcceptedAndOthersRejected() throws Exception
    {
        for(int type : new int[] {BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_3BYTE_BGR,
                BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_USHORT_GRAY})
        {
            ReaderSpy reader = new ReaderSpy(4, 3);
            reader.imageTypes = List.of(ImageTypeSpecifier.createFromBufferedImageType(type));
            TrackingInput input = new TrackingInput(records(RGB));
            try(VideoSource source = controlled(input, reader))
            {
                if(type == BufferedImage.TYPE_4BYTE_ABGR || type == BufferedImage.TYPE_USHORT_GRAY)
                {
                    SourceException error = assertThrows(SourceException.class, source::nextFrame);
                    assertEquals(Reason.IMAGE_LIMIT, error.reason());
                    assertEquals(0, reader.readCalls);
                    assertTerminal(source, input, error);

                }else{
                    assertFrame(source.nextFrame().orElseThrow(), 0, 0, RGB);
                    assertEquals(type, reader.destination.getType());
                }
                assertReaderReleased(reader);
            }
        }
    }


    @Test
    void M13InvalidProviderContractsStayUnexpected() throws Exception
    {
        for(String defect : List.of("format", "null types", "empty types", "null type", "null parameters"))
        {
            ReaderSpy reader = new ReaderSpy(4, 3);
            reader.defect = defect;
            TrackingInput input = new TrackingInput(records(RGB));
            try(VideoSource source = controlled(input, reader))
            {
                RuntimeException error = assertThrows(RuntimeException.class, source::nextFrame);
                assertFalse(error instanceof java.io.UncheckedIOException);
                assertEquals(0, reader.readCalls);
                assertReaderReleased(reader);
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M14ZeroProgressFailsOnceAtAnyReadStage() throws Exception
    {
        for(int after : new int[] {0, 1, 5, 8})
        {
            TrackingInput input = new TrackingInput(records(RGB));
            input.invalidAt = after;
            input.invalidCount = 0;
            try(VideoSource source = owned(input))
            {
                SourceException error = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(Reason.READ_IO, error.reason());
                assertEquals("NO_PROGRESS", error.diagnostic().detail());
                assertEquals(after < 5 ? Stage.PREFIX : Stage.BODY, error.diagnostic().stage());
                assertEquals(after < 5 ? after : after - 5, error.diagnostic().consumedInStage());
                assertEquals(1, input.invalidCalls);
                assertEquals(0, input.singleCalls);
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M14InvalidReadCountsFailWithoutFallbackOrFurtherRead() throws Exception
    {
        for(int invalid : new int[] {-2, Integer.MIN_VALUE, 2, Integer.MAX_VALUE})
        {
            TrackingInput input = new TrackingInput(records(RGB));
            input.invalidAt = 0;
            input.invalidCount = invalid;
            try(VideoSource source = owned(input))
            {
                SourceException error = assertThrows(SourceException.class, source::nextFrame);
                assertEquals(Reason.READ_IO, error.reason());
                assertTrue(error.diagnostic().detail().startsWith("INVALID_READ_COUNT="));
                assertEquals(1, input.bulkCalls);
                assertEquals(0, input.singleCalls);
                assertTerminal(source, input, error);
            }
        }
    }


    @Test
    void M14PositionArithmeticDoesNotWrap()
    {
        assertEquals(Long.MAX_VALUE, VideoSource.checkedEnd(Long.MAX_VALUE - 5, 5));
        assertEquals(Long.MAX_VALUE, VideoSource.checkedEnd(Long.MAX_VALUE - 99999, 99999));
        assertThrows(ArithmeticException.class, () -> VideoSource.checkedEnd(Long.MAX_VALUE - 4, 5));
        assertThrows(ArithmeticException.class, () -> VideoSource.checkedEnd(Long.MAX_VALUE - 99998, 99999));
        assertEquals(Long.MAX_VALUE, VideoSource.checkedNextIndex(Long.MAX_VALUE - 1));
        assertThrows(ArithmeticException.class, () -> VideoSource.checkedNextIndex(Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> VideoSource.checkedEnd(-1, 5));
        assertThrows(IllegalArgumentException.class, () -> VideoSource.checkedNextIndex(-1));
    }


    @Test
    void M09PathAdmissionRetainsIoCauseAndRejectsEscapesAndNonFiles() throws Exception
    {
        Path base = Files.createDirectory(temporary.resolve("base"));
        Path inside = Files.write(base.resolve("valid.Mjpeg"), records(RGB));
        Path outside = Files.write(temporary.resolve("outside.Mjpeg"), records(GRAY));
        Files.createSymbolicLink(base.resolve("escape.Mjpeg"), outside);
        Files.createSymbolicLink(base.resolve("inside-link.Mjpeg"), inside);
        for(Path rejected : List.of(Path.of("missing.Mjpeg"), Path.of("../outside.Mjpeg"), outside,
                Path.of("escape.Mjpeg"), Path.of(".")))
        {
            SourceException error = assertThrows(SourceException.class, () -> VideoSource.open(base, rejected, 99999));
            assertEquals(Reason.OPEN_IO, error.reason());
            assertInstanceOf(IOException.class, error.getCause());
            assertEquals(Stage.OPEN, error.diagnostic().stage());
        }
        Path directory = Files.createDirectory(base.resolve("directory"));
        assertEquals(Reason.OPEN_IO, assertThrows(SourceException.class,
                () -> VideoSource.open(base, directory, 99999)).reason());
        try(VideoSource source = VideoSource.open(base, Path.of("inside-link.Mjpeg"), 99999))
        {
            assertFrame(source.nextFrame().orElseThrow(), 0, 0, RGB);
        }
    }


    @Test
    void M10InvalidArgumentsDoNotAcquireOrTouchCallerInput() throws Exception
    {
        TrackingInput input = new TrackingInput(records(RGB));
        for(int bound : new int[] {Integer.MIN_VALUE, -1, 0, 100000, Integer.MAX_VALUE})
        {
            assertThrows(IllegalArgumentException.class, () -> VideoSource.fromOwnedInput(input, "invalid", bound));
            assertThrows(IllegalArgumentException.class, () -> VideoSource.open(temporary,
                    Path.of("does-not-exist"), bound));
        }
        assertThrows(NullPointerException.class, () -> VideoSource.fromOwnedInput(input, null, 1));
        assertThrows(NullPointerException.class, () -> VideoSource.fromOwnedInput(input, "invalid", 1, null));
        assertThrows(NullPointerException.class, () -> VideoSource.fromOwnedInput(null, "invalid", 1));
        assertThrows(NullPointerException.class, () -> VideoSource.open(null, temporary, 1));
        assertThrows(NullPointerException.class, () -> VideoSource.open(temporary, null, 1));
        assertEquals(0, input.bulkCalls);
        assertEquals(0, input.closeCalls);
        input.close();
    }


    @Test
    void M09DiagnosticsAreBoundedAndDoNotIncludePayload() throws Exception
    {
        TrackingInput input = new TrackingInput(ascii("bad!!"));
        try(VideoSource source = VideoSource.fromOwnedInput(input, "source".repeat(100), 99999))
        {
            SourceException error = assertThrows(SourceException.class, source::nextFrame);
            assertEquals(256, error.diagnostic().sourceId().length());
            assertTrue(error.diagnostic().detail().length() <= 256);
            assertFalse(error.getMessage().contains("bad!!"));
            assertTerminal(source, input, error);
        }
    }



    //* === Auxiliar === *

    private static VideoSource owned(TrackingInput input)
    {
        return VideoSource.fromOwnedInput(input, "fixture", 99999);
    }


    private static VideoSource controlled(TrackingInput input, ReaderSpy reader)
    {
        return VideoSource.fromOwnedInput(input, "controlled", 99999, cache ->
        {
            reader.borrowed = cache;
            return reader;
        });
    }


    private static void assertFrame(Frame frame, long index, long offset, byte[] expected)
    {
        assertEquals(index, frame.index());
        assertEquals(offset, frame.prefixOffset());
        assertEquals(expected.length, frame.length());
        assertArrayEquals(expected, frame.bytes());
    }


    private static void assertDiagnostic(SourceException failure, Reason reason, Stage stage,
            long index, long offset, long expected, long observed, long consumed)
    {
        Diagnostic diagnostic = failure.diagnostic();
        assertEquals(reason, diagnostic.reason());
        assertEquals(stage, diagnostic.stage());
        assertEquals(index, diagnostic.recordIndex());
        assertEquals(offset, diagnostic.prefixOffset());
        assertEquals(expected, diagnostic.expected());
        assertEquals(observed, diagnostic.observed());
        assertEquals(consumed, diagnostic.consumedInStage());
    }


    private static void assertTerminal(VideoSource source, TrackingInput input, Throwable primary) throws Exception
    {
        assertEquals(State.FAILED, source.state());
        int calls = input.bulkCalls;
        Throwable[] suppressed = primary.getSuppressed();
        for(int index = 0; index < 3; index++)
        {
            source.close();
            assertSame(primary, assertThrows(primary.getClass(), source::nextFrame));
        }
        assertEquals(calls, input.bulkCalls);
        assertEquals(1, input.closeCalls);
        assertArrayEquals(suppressed, primary.getSuppressed());
    }


    private static void assertReaderReleased(ReaderSpy reader)
    {
        assertEquals(1, reader.disposeCalls);
        assertCacheClosed(reader.borrowed);
    }


    private static void assertCacheClosed(ImageInputStream cache)
    {
        assertNotNull(cache);
        assertThrows(IOException.class, cache::getStreamPosition);
    }


    private static byte[] ascii(String text)
    {
        return text.getBytes(StandardCharsets.US_ASCII);
    }


    private static byte[] prefix(int length)
    {
        return ascii(String.format(java.util.Locale.ROOT, "%05d", length));
    }


    private static byte[] records(byte[]... payloads)
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for(byte[] payload : payloads)
        {
            result.writeBytes(prefix(payload.length));
            result.writeBytes(payload);
        }
        return result.toByteArray();
    }


    private static byte[] join(byte[]... parts)
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for(byte[] part : parts)
        {
            result.writeBytes(part);
        }
        return result.toByteArray();
    }


    private static byte[] withComments(byte[] original, int targetLength)
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.writeBytes(new byte[] {-1, -40});
        int remaining = targetLength - original.length;
        while(remaining > 0)
        {
            int total = Math.min(remaining, 65537);
            if(remaining - total > 0 && remaining - total < 4)
                total -= 4;
            if(total < 4)
                throw new IllegalArgumentException("Not enough room for a COM segment");
            int length = total - 2;
            result.writeBytes(new byte[] {-1, -2, (byte) (length >>> 8), (byte) length});
            byte[] comment = new byte[total - 4];
            if(comment.length >= 2)
            {
                comment[0] = -1;
                comment[1] = -39;
            }
            result.writeBytes(comment);
            remaining -= total;
        }
        result.writeBytes(Arrays.copyOfRange(original, 2, original.length));
        assertEquals(targetLength, result.size());
        return result.toByteArray();
    }


    private static byte[] sof(int width, int height)
    {
        return new byte[] {-1, -64, 0, 11, 8, (byte) (height >>> 8), (byte) height,
                (byte) (width >>> 8), (byte) width, 1, 1, 0x11, 0};
    }


    private static byte[] sos()
    {
        return new byte[] {-1, -38, 0, 8, 1, 1, 0, 0, 63, 0};
    }


    private static byte[] structuralJpeg(int width, int height)
    {
        return join(new byte[] {-1, -40}, sof(width, height), sos(), new byte[] {0, -1, -39});
    }


    private static String sha256(byte[] bytes) throws Exception
    {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }


    private static void throwConfigured(Throwable failure) throws IOException
    {
        if(failure instanceof IOException checked)
            throw checked;
        if(failure instanceof RuntimeException unchecked)
            throw unchecked;
        if(failure instanceof Error fatal)
            throw fatal;
    }


    private static void throwUnchecked(Throwable failure)
    {
        if(failure instanceof RuntimeException unchecked)
            throw unchecked;
        if(failure instanceof Error fatal)
            throw fatal;
    }



    //* === Tipos === *

    private static final class TrackingInput extends InputStream
    {
        //* === Atributos e Variáveis === *
        private final byte[] bytes;
        private int position;
        private int bulkCalls;
        private int singleCalls;
        private int closeCalls;
        private int maxChunk = Integer.MAX_VALUE;
        private int faultAt = Integer.MAX_VALUE;
        private int invalidAt = Integer.MAX_VALUE;
        private int invalidCount;
        private int invalidCalls;
        private Throwable readFailure;
        private Throwable closeFailure;
        private byte[] retainedBody;



        //* === Construtores === *

        private TrackingInput(byte[] bytes)
        {
            this.bytes = bytes.clone();
        }



        //* === Lógica === *

        @Override
        public int read() throws IOException
        {
            singleCalls++;
            throw new AssertionError("Bulk reads must not fall back to read()");
        }


        @Override
        public int read(byte[] target, int offset, int length) throws IOException
        {
            assertTrue(length > 0);
            bulkCalls++;
            if(position >= faultAt)
                throwConfigured(readFailure);
            if(position >= invalidAt)
            {
                invalidCalls++;
                return invalidCount;
            }
            if(position == bytes.length)
                return -1;
            int count = Math.min(length, Math.min(maxChunk, bytes.length - position));
            count = Math.min(count, Math.min(faultAt, invalidAt) - position);
            System.arraycopy(bytes, position, target, offset, count);
            position += count;
            if(target.length > 5)
                retainedBody = target;
            return count;
        }


        @Override
        public int available()
        {
            throw new AssertionError("available() is not an EOF oracle");
        }


        @Override
        public void close() throws IOException
        {
            closeCalls++;
            throwConfigured(closeFailure);
        }
    }


    private static final class ReaderSpy extends ImageReader
    {
        //* === Atributos e Variáveis === *
        private final int width;
        private final int height;
        private int readCalls;
        private int disposeCalls;
        private int returnMode;
        private String warning;
        private String faultSite;
        private String defect;
        private Throwable operationFailure;
        private Throwable readFailure;
        private Throwable disposeFailure;
        private ImageInputStream borrowed;
        private TrackingImage extra;
        private BufferedImage destination;
        private ImageReadParam parameters;
        private List<ImageTypeSpecifier> imageTypes = List.of(
                ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_3BYTE_BGR));



        //* === Construtores === *

        private ReaderSpy(int width, int height)
        {
            super(null);
            this.width = width;
            this.height = height;
        }



        //* === Lógica === *

        @Override
        public void setInput(Object input, boolean forwardOnly, boolean ignoreMetadata)
        {
            assertSame(borrowed, input);
            assertTrue(forwardOnly);
            assertTrue(ignoreMetadata);
            fault("input");
            super.setInput(input, forwardOnly, ignoreMetadata);
        }


        @Override
        public String getFormatName() throws IOException
        {
            checkedFault("format");
            return "format".equals(defect) ? "PNG" : "JPEG";
        }


        @Override
        public int getNumImages(boolean search)
        {
            fail("Do not search for more images through ImageReader");
            return 1;
        }


        @Override
        public int getWidth(int index) throws IOException
        {
            assertEquals(0, index);
            checkedFault("width");
            return width;
        }


        @Override
        public int getHeight(int index) throws IOException
        {
            assertEquals(0, index);
            checkedFault("height");
            return height;
        }


        @Override
        public Iterator<ImageTypeSpecifier> getImageTypes(int index) throws IOException
        {
            assertEquals(0, index);
            checkedFault("types");
            if("null types".equals(defect))
                return null;
            if("empty types".equals(defect))
                return List.<ImageTypeSpecifier>of().iterator();
            if("null type".equals(defect))
                return Arrays.<ImageTypeSpecifier>asList((ImageTypeSpecifier) null).iterator();
            return imageTypes.iterator();
        }


        @Override
        public ImageReadParam getDefaultReadParam()
        {
            fault("parameters");
            if("null parameters".equals(defect))
                return null;
            ImageReadParam parameters = new ImageReadParam();
            parameters.setSourceRegion(new Rectangle(0, 0, 1, 1));
            parameters.setSourceSubsampling(2, 2, 0, 0);
            parameters.setDestinationOffset(new Point(1, 1));
            parameters.setSourceProgressivePasses(1, 1);
            return parameters;
        }


        @Override
        public BufferedImage read(int index, ImageReadParam parameters) throws IOException
        {
            assertEquals(0, index);
            readCalls++;
            this.parameters = parameters;
            destination = parameters.getDestination();
            assertNotNull(destination);
            fault("read");
            throwConfigured(readFailure);
            if(warning != null)
                processWarningOccurred(warning);
            switch(returnMode)
            {
                case 1:
                    return null;

                case 2:
                    return extra;

                default:
                    return destination;
            }
        }


        @Override
        public IIOMetadata getStreamMetadata()
        {
            fail("Stream metadata is not needed");
            return null;
        }


        @Override
        public IIOMetadata getImageMetadata(int index)
        {
            fail("Image metadata is not needed");
            return null;
        }


        @Override
        public void dispose()
        {
            disposeCalls++;
            throwUnchecked(disposeFailure);
            super.dispose();
        }


        private void fault(String site)
        {
            if(site.equals(faultSite))
                throwUnchecked(operationFailure);
        }


        private void checkedFault(String site) throws IOException
        {
            if(site.equals(faultSite))
                throwConfigured(operationFailure);
        }
    }


    private static final class TrackingImage extends BufferedImage
    {
        //* === Atributos e Variáveis === *
        private int flushCalls;
        private Throwable flushFailure;



        //* === Construtores === *

        private TrackingImage()
        {
            super(4, 3, BufferedImage.TYPE_INT_RGB);
        }



        //* === Lógica === *

        @Override
        public void flush()
        {
            flushCalls++;
            throwUnchecked(flushFailure);
            super.flush();
        }
    }


    private static final class SyntheticFatal extends Error
    {
        //* === Atributos e Variáveis === *
        private static final long serialVersionUID = 1L;



        //* === Construtores === *

        private SyntheticFatal(String message)
        {
            super(message);
        }
    }
}
