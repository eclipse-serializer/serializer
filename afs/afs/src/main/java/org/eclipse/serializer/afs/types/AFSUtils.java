package org.eclipse.serializer.afs.types;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.io.XIO;
import org.eclipse.serializer.memory.XMemory;

public final class AFSUtils {

    public static void executeWriting(
            final AFile file ,
            final Consumer<? super AWritableFile> logic
    )
    {
        executeWriting(file, file.defaultUser(), logic);
    }

    public static void executeWriting(
            final AFile                           file ,
            final Object                          user ,
            final Consumer<? super AWritableFile> logic
    )
    {
        // no locking needed, here since the implementation of #useWriting has to cover that
        final AWritableFile writableFile = file.useWriting(user);
        try
        {
            logic.accept(writableFile);
        }
        finally
        {
            writableFile.release();
        }
    }


    public static void close(final AReadableFile file, final Throwable cause)
    {
        if(file == null)
        {
            return;
        }

        try
        {
            file.close();
        }
        catch(final Throwable t)
        {
            if(cause != null)
            {
                t.addSuppressed(cause);
            }
            throw t;
        }
    }

    public static void execute(
            final AFile                           file ,
            final Consumer<? super AReadableFile> logic
    )
    {
        final AReadableFile rFile = file.useReading();
        try
        {
            logic.accept(rFile);
        }
        finally
        {
            rFile.release();
        }
    }

    public static final long writeString(final AFile file, final String string)
    {
        return writeString(file, string, XChars.standardCharset());
    }

    public static final long writeString(final AFile file, final String string, final Charset charset)
    {
        final byte[] bytes = string.getBytes(charset);

        return write_bytes(file, bytes);
    }

    public static long writeBytes(
            final AFile      file ,
            final ByteBuffer bytes
    )
    {
        final AWritableFile wFile = file.useWriting();
        try
        {
            return wFile.writeBytes(bytes);
        }
        finally
        {
            wFile.release();
        }
    }

    public static final long write_bytes(final AFile file, final byte[] bytes)
    {
        final ByteBuffer dbb = XIO.wrapInDirectByteBuffer(bytes);
        final Long writeCount = writeBytes(file, dbb);
        XMemory.deallocateDirectByteBuffer(dbb);

        return writeCount;
    }

    public static <R> R apply(
            final AFile                              file ,
            final Function<? super AReadableFile, R> logic
    )
    {
        final AReadableFile rFile = file.useReading();
        try
        {
            return logic.apply(rFile);
        }
        finally
        {
            rFile.release();
        }
    }

    public static String readString(final AFile file)
    {
        return readString(file, XChars.standardCharset());
    }

    public static String readString(final AFile file, final Charset charSet)
    {
        final byte[] bytes = AFS.read_bytes(file);

        return XChars.String(bytes, charSet);
    }

    ///////////////////////////////////////////////////////////////////////////
    // constructors //
    /////////////////

    /**
     * Dummy constructor to prevent instantiation of this static-only utility class.
     *
     * @throws UnsupportedOperationException when called
     */
    private AFSUtils()
    {
        // static only
        throw new UnsupportedOperationException();
    }
}
