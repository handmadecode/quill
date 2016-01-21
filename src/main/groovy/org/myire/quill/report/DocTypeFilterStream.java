/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * A filtering input stream that replaces any XML DOCTYPE declaration with whitespace.
 */
public class DocTypeFilterStream extends FilterInputStream
{
    static private final byte[] cDocTypeStartBytes = "<!DOCTYPE".getBytes();
    static private final byte END_OF_DECLARATION = (byte)'>';
    static private final byte SPACE = (byte)' ';


    // Internal buffer used to hold and filter data from the underlying stream.
    private final byte[] fBuffer = new byte[8192];

    // The position in the internal buffer where the next read operation will start.
    private int fPos;

    // The logical length of the internal buffer.
    private int fLength;

    // Has end of stream been reached in the underlying stream?
    private boolean fEndOfStreamReached;

    // Is the stream currently being filtered, i.e. has the start of the DOCTYPE declaration been
    // found but not the end of it?
    private boolean fIsFiltering;

    // If non-zero, part of the start of the DOCTYPE declaration has been matched at the end of the
    // internal buffer but is not included in the logical length of the buffer. When more data is
    // loaded from the underlying stream, this many bytes from cDocTypeStartBytes must be prepended
    // to the loaded data.
    private int fPartialMatchLength;


    /**
     * Create a new {@code DoctypeFilterStream}.
     *
     * @param pInputStream  The stream to filter.
     */
    public DocTypeFilterStream(InputStream pInputStream)
    {
        super(pInputStream);
    }


    @Override
    public int read() throws IOException
    {
        // The contract for read specifies that it blocks until at least one byte is available or
        // the end of the stream is reached.
        while (!hasBufferedData() && !fEndOfStreamReached)
            load();

        return fEndOfStreamReached ? -1 : fBuffer[fPos++];
    }


    @Override
    public int read(byte[] pBuffer) throws IOException
    {
        return read(pBuffer, 0, pBuffer.length);
    }


    @Override
    public int read(byte[] pBuffer, int pOffset, int pLength) throws IOException
    {
        // Load from the underlying stream if the internal buffer has no data.
        if (!hasBufferedData())
            load();

        int aNumAvailableBytes = numBytesInBuffer();
        if (aNumAvailableBytes > 0)
        {
            // Read as many bytes as specified, but never more than are available in the internal
            // buffer.
            int aNumBytesToRead = aNumAvailableBytes;
            if (aNumBytesToRead > pLength)
                aNumBytesToRead = pLength;

            // Copy from the internal buffer to the specified buffer and advance the position.
            System.arraycopy(fBuffer, fPos, pBuffer, pOffset, aNumBytesToRead);
            fPos += aNumBytesToRead;

            return aNumBytesToRead;
        }
        else if (fEndOfStreamReached)
            return -1;
        else
            return 0;
    }


    @Override
    public long skip(long pNumBytes)
    {
        // Don't skip more bytes than are available in the internal buffer.
        long aNumBytesToSkip = numBytesInBuffer();
        if (aNumBytesToSkip > pNumBytes)
            aNumBytesToSkip = pNumBytes;

        fPos += aNumBytesToSkip;
        return aNumBytesToSkip;
    }


    @Override
    public int available() throws IOException
    {
        // The available number of bytes are what's available in the internal buffer plus what's
        // available in the underlying stream.
        return numBytesInBuffer() + super.available();
    }


    @Override
    public boolean markSupported()
    {
        return false;
    }


    /**
     * Check if the internal buffer contains at least one byte.
     *
     * @return  True if at least one byte is available in the internal buffer, false if it is empty.
     */
    private boolean hasBufferedData()
    {
        return fPos < fLength;
    }


    /**
     * Get the number of bytes available in the internal buffer.
     *
     * @return  The number of bytes available in the internal buffer.
     */
    private int numBytesInBuffer()
    {
        return fLength - fPos;
    }


    /**
     * Load more data into the internal buffer from the underlying stream and replace any DOCTYPE
     * declaration with spaces.
     *
     * @throws IOException  if loading from the underlying stream fails.
     */
    private void load() throws IOException
    {
        // Compact the internal buffer, saving any unread data at the beginning.
        int aRemaining = numBytesInBuffer();
        System.arraycopy(fBuffer, fPos, fBuffer, 0, aRemaining);
        fPos = 0;
        fLength = aRemaining;

        // Put any partial match of the DOCTYPE pattern back into the internal buffer.
        restorePartialMatch();

        if (!fEndOfStreamReached)
        {
            // Load data from the underlying stream and append it to the internal buffer.
            int aNumReadBytes = super.read(fBuffer, fLength, fBuffer.length - fLength);
            if (aNumReadBytes > 0)
                fLength += aNumReadBytes;
            else if (aNumReadBytes < 0)
                fEndOfStreamReached = true;

            // Filter out any DOCTYPE XML declaration
            filter();

            // Done filtering, if the stream is exhausted any partial match must be put back since
            // it cannot be a full match.
            if (fEndOfStreamReached)
                restorePartialMatch();
        }
    }


    /**
     * Put any bytes from a partial match of the DOCTYPE pattern back into the internal buffer.
     */
    private void restorePartialMatch()
    {
        if (fPartialMatchLength > 0 && fBuffer.length - fLength >= fPartialMatchLength)
        {
            System.arraycopy(cDocTypeStartBytes, 0, fBuffer, fLength, fPartialMatchLength);
            fLength += fPartialMatchLength;
            fPartialMatchLength = 0;
        }
    }


    private void filter()
    {
        // Start filtering at the current position in the internal buffer.
        int aFilterPos = fPos;
        while (aFilterPos < fLength)
        {
            // If the start of of the DOCTYPE declaration has been found every byte is replaced with
            // a space until the closing '>' is found. The closing '>' is also replaced..
            while (fIsFiltering && aFilterPos < fLength)
            {
                if (fBuffer[aFilterPos] == END_OF_DECLARATION)
                    fIsFiltering = false;

                fBuffer[aFilterPos++] = SPACE;
            }

            // Not filtering (any more), find the start of any DOCTYPE declaration in the internal
            // buffer.
            int aDoctypeStartPos = findDoctypeStart();
            if (aDoctypeStartPos < 0)
                // No match in the internal buffer, done filtering.
                break;

            int aMatchLength = fLength - aDoctypeStartPos;
            if (aMatchLength >= cDocTypeStartBytes.length)
            {
                // Found a full match, start filtering at the start of the match.
                fIsFiltering = true;
                aFilterPos = aDoctypeStartPos;
            }
            else
            {
                // There is only a partial match, shorten the internal buffer to end just before
                // that partial match. Remember the length of the partial match so it can be put
                // back into the buffer the next time data is loaded.
                fLength = aDoctypeStartPos;
                fPartialMatchLength = aMatchLength;
                aFilterPos = fLength;
            }
        }
    }


    /**
     * Find the first position in the internal buffer where a DOCTYPE declaration starts. The
     * returned position may be a partial match only.
     *
     * @return  The position in the internal buffer where (a partial match of) the next DOCTYPE
     *          declaration starts. A negative value indicates that no possible match was found in
     *          the internal buffer.
     */
    private int findDoctypeStart()
    {
        // Search the internal buffer using the Knuth-Morris-Pratt algorithm with the observation
        // that the pattern (<!DOCTYPE) is non-repetitive. This means that the so called next array
        // contains only zeroes.

        // Loop until we reach the end of the internal buffer, in which case we can't compare any
        // more, or until we reach the end of the pattern, in which case we have found a match.
        int aBufferPos = fPos;
        int aPatternPos = 0;
        while (aBufferPos < fLength && aPatternPos < cDocTypeStartBytes.length)
        {
            // Check if the internal buffer and pattern match each other at their current positions.
            if (fBuffer[aBufferPos] == cDocTypeStartBytes[aPatternPos])
            {
                // We have a match this far, compare the next byte in the internal buffer with the
                // next byte in the pattern.
                aBufferPos++;
                aPatternPos++;
            }
            else
            {
                // We have a match up to here, but not at this position.
                if (aPatternPos == 0)
                    // The mismatch occurred at the pattern's first byte, continue comparing the
                    // next byte in the internal buffer with the start of the pattern (since the
                    // pattern's current byte is the first one we only need to advance the internal
                    // buffer's position).
                    aBufferPos++;
                else
                    // Mismatch after a partial match. Let the internal buffer's position be
                    // unchanged and compare it to the position in the pattern indicated by the next
                    // array, which as observed above always is 0.
                    aPatternPos = 0;
            }
        }

        // If the pattern position is non-zero we found at least a partial match.
        if (aPatternPos > 0)
            return aBufferPos - aPatternPos;
        else
            return -1;
    }
}
