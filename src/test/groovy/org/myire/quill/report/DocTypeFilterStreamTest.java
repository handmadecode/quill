/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * JUnit tests for {@code DocTypeFilterStream}.
 */
public class DocTypeFilterStreamTest
{
    // The filter looks for the start of the DOCTYPE tag
    static private final byte[] cDocTypeStartBytes = "<!DOCTYPE".getBytes();
    static private final byte cDocTypeEndByte = '>';
    static private final byte cReplacementByte = ' ';

    static private Random cRandom = new Random();


    @Test
    public void streamDoesNotSupportMark() throws IOException
    {
        // Given
        DocTypeFilterStream aStream = createDocTypeFilterStream(new byte[10]);

        // Then
        assertFalse(aStream.markSupported());
    }


    @Test
    public void availableReturnsNumberOfBytesInStream() throws IOException
    {
        // Given
        int aNumBytes = randomLength(256);
        DocTypeFilterStream aStream = createDocTypeFilterStream(new byte[aNumBytes]);

        // Then
        assertEquals(aNumBytes, aStream.available());
    }


    @Test
    public void availableReturnsNumberOfBytesInStreamAfterRead() throws IOException
    {
        // Given
        int aNumBytes = randomLength(256);
        DocTypeFilterStream aStream = createDocTypeFilterStream(new byte[aNumBytes]);
        aStream.read();

        // Then
        assertEquals(aNumBytes - 1, aStream.available());
    }


    @Test
    public void skipDiscardsBytes() throws IOException
    {
        // Given: create a new stream and read a byte to trigger a load from the underlying stream.
        byte[] aBytes = randomNonMatchingBytes(256);
        DocTypeFilterStream aStream = createDocTypeFilterStream(aBytes);
        int aNumSkipped = 1;
        aStream.read();

        // When: skip half of the bytes
        aNumSkipped += (int) aStream.skip(aBytes.length/2);
        byte[] aReadBytes = new byte[aBytes.length - aNumSkipped];
        int aNumRead = aStream.read(aReadBytes);

        // Then: skipped + read + available bytes should equal the total number of bytes.
        assertEquals(aBytes.length, aNumRead + aNumSkipped + aStream.available());

        // Then: the read bytes should be the ones after the skipped bytes.
        byte[] aExpectedBytes = new byte[aReadBytes.length];
        System.arraycopy(aBytes, aNumSkipped, aExpectedBytes, 0, aNumRead);
        assertArrayEquals(aExpectedBytes, aReadBytes);
    }


    @Test
    public void skipDoesNotSkipMoreThanAvailable() throws IOException
    {
        // Given: create a new stream and read a byte to trigger a load from the underlying stream.
        byte[] aBytes = randomNonMatchingBytes(256);
        DocTypeFilterStream aStream = createDocTypeFilterStream(aBytes);
        int aNumSkipped = 1;
        aStream.read();

        // When: skip one byte more than available
        aNumSkipped += (int) aStream.skip(aBytes.length + 1);

        // Then: skipped + available bytes should <= the total number of bytes.
        assertTrue(aNumSkipped + aStream.available() <= aBytes.length);
    }


    @Test
    public void readReturnsEndOfStreamForExhaustedStream() throws IOException
    {
        // Given
        byte aByte = 17;
        DocTypeFilterStream aStream = createDocTypeFilterStream(new byte[] {aByte});

        // When
        int aReadByte = aStream.read();

        // Then
        assertEquals(aByte, aReadByte);
        assertEquals(-1, aStream.read());
    }


    @Test
    public void readArrayReturnsEndOfStreamForExhaustedStream() throws IOException
    {
        // Given
        byte aByte = 47;
        DocTypeFilterStream aStream = createDocTypeFilterStream(new byte[] {aByte});

        // When
        byte[] aBytes = new byte[10];
        int aNumReadBytes = aStream.read(aBytes);

        // Then
        assertEquals(1, aNumReadBytes);
        assertEquals(aByte, aBytes[0]);
        assertEquals(-1, aStream.read(aBytes));
    }


    @Test
    public void emptyStreamDoesNotFilter() throws IOException
    {
        // When
        byte[] aResult = filter(new byte[0]);

        // Then
        assertEquals(0, aResult.length);
    }


    @Test
    public void streamWithoutMatchDoesNotFilter() throws IOException
    {
        // Given
        byte[] aStreamBytes = randomNonMatchingBytes(randomLength(100000));

        // When
        byte[] aResult = filter(aStreamBytes);

        // Then
        assertArrayEquals(aStreamBytes, aResult);
    }


    @Test
    public void streamWithOnlyMatchingBytesIsFiltered() throws IOException
    {
        // Given
        byte[] aStreamBytes = randomDocTypeTag(randomLength(12));

        // When
        byte[] aResult = filter(aStreamBytes);

        // Then
        byte[] aExpected = expectedReplacement(aStreamBytes);
        assertArrayEquals(aExpected, aResult);
    }


    @Test
    public void streamWithOneMatchIsFiltered() throws IOException
    {
        // Given
        byte[] aDocTypeBytes = randomDocTypeTag(randomLength(9));
        byte[] aPrefixBytes = randomNonMatchingBytes(randomLength(500));
        byte[] aSuffixBytes = randomNonMatchingBytes(randomLength(250));
        byte[] aStreamBytes = concatenate(aPrefixBytes, aDocTypeBytes, aSuffixBytes);

        // When
        byte[] aResult = filter(aStreamBytes);

        // Then
        byte[] aExpected = concatenate(aPrefixBytes, expectedReplacement(aDocTypeBytes), aSuffixBytes);
        assertArrayEquals(aExpected, aResult);
    }


    @Test
    public void streamStartingWithMatchIsFiltered() throws IOException
    {
        // Given
        byte[] aDocTypeBytes = randomDocTypeTag(randomLength(9));
        byte[] aSuffixBytes = randomNonMatchingBytes(randomLength(4096));
        byte[] aStreamBytes = concatenate(aDocTypeBytes, aSuffixBytes);

        // When
        byte[] aResult = filter(aStreamBytes);

        // Then
        byte[] aExpected = concatenate(expectedReplacement(aDocTypeBytes), aSuffixBytes);
        assertArrayEquals(aExpected, aResult);
    }


    @Test
    public void streamEndingWithMatchIsFiltered() throws IOException
    {
        // Given
        byte[] aDocTypeBytes = randomDocTypeTag(randomLength(100));
        byte[] aPrefixBytes = randomNonMatchingBytes(randomLength(100));
        byte[] aStreamBytes = concatenate(aPrefixBytes, aDocTypeBytes);

        // When
        byte[] aResult = filter(aStreamBytes);

        // Then
        byte[] aExpected = concatenate(aPrefixBytes, expectedReplacement(aDocTypeBytes));
        assertArrayEquals(aExpected, aResult);
    }


    @Test
    public void streamWithMultipleMatchesIsFiltered() throws IOException
    {
        // Given
        byte[] aDocTypeBytes1 = randomDocTypeTag(randomLength(20));
        byte[] aDocTypeBytes2 = randomDocTypeTag(randomLength(30));
        byte[] aPrefixBytes = randomNonMatchingBytes(randomLength(700));
        byte[] aInfixBytes = randomNonMatchingBytes(randomLength(1000));
        byte[] aSuffixBytes = randomNonMatchingBytes(randomLength(600));
        byte[] aStreamBytes = concatenate(aPrefixBytes, aDocTypeBytes1, aInfixBytes, aDocTypeBytes2, aSuffixBytes);

        // When
        byte[] aResult = filter(aStreamBytes);

        // Then
        byte[] aReplacementBytes1 = expectedReplacement(aDocTypeBytes1);
        byte[] aReplacementBytes2 = expectedReplacement(aDocTypeBytes2);
        byte[] aExpected = concatenate(aPrefixBytes, aReplacementBytes1, aInfixBytes, aReplacementBytes2, aSuffixBytes);
        assertArrayEquals(aExpected, aResult);
    }


    @Test
    public void streamWithPartialMatchIsNotFiltered() throws IOException
    {
        for (int i=1; i<cDocTypeStartBytes.length; i++)
        {
            // Given
            byte[] aPartialMatch = new byte[i];
            System.arraycopy(cDocTypeStartBytes, 0, aPartialMatch, 0, i);
            byte[] aPrefixBytes = randomNonMatchingBytes(randomLength(300));
            byte[] aSuffixBytes = randomNonMatchingBytes(randomLength(300));
            byte[] aStreamBytes = concatenate(aPrefixBytes, aPartialMatch, aSuffixBytes);

            // When
            byte[] aResult = filter(aStreamBytes);

            // Then
            assertArrayEquals(aStreamBytes, aResult);
        }
    }


    @Test
    public void streamWithPartialMatchAndFullMatchIsFiltered() throws IOException
    {
        // Given
        byte[] aDocTypeBytes = randomDocTypeTag(randomLength(5));
        byte[] aPartialMatch = new byte[cDocTypeStartBytes.length - 2];
        System.arraycopy(cDocTypeStartBytes, 0, aPartialMatch, 0, aPartialMatch.length);
        byte[] aPrefixBytes = randomNonMatchingBytes(randomLength(600));
        byte[] aSuffixBytes = randomNonMatchingBytes(randomLength(10000));
        byte[] aStreamBytes = concatenate(aPrefixBytes, aPartialMatch, aDocTypeBytes, aSuffixBytes);

        // When
        byte[] aResult = filter(aStreamBytes);

        // Then
        byte[] aReplacementBytes = expectedReplacement(aDocTypeBytes);
        byte[] aExpected = concatenate(aPrefixBytes, aPartialMatch, aReplacementBytes, aSuffixBytes);
        assertArrayEquals(aExpected, aResult);
    }


    @Test
    public void streamWithMatchSpanningBufferIsFiltered() throws IOException
    {
        final int aInternalBufferSize = 8192;
        byte[] aDocTypeBytes = randomDocTypeTag(randomLength(2));
        byte[] aReplacementBytes = expectedReplacement(aDocTypeBytes);

        for (int i=1; i<aDocTypeBytes.length; i++)
        {
            // Given
            byte[] aPrefixBytes = randomNonMatchingBytes(aInternalBufferSize - i);
            byte[] aSuffixBytes = randomNonMatchingBytes(randomLength(300));
            byte[] aStreamBytes = concatenate(aPrefixBytes, aDocTypeBytes, aSuffixBytes);

            // When
            byte[] aResult = filter(aStreamBytes);

            // Then
            byte[] aExpected = concatenate(aPrefixBytes, aReplacementBytes, aSuffixBytes);
            assertArrayEquals(aExpected, aResult);
        }
    }


    @Test
    public void streamWithPartialMatchSpanningBufferIsNotFiltered() throws IOException
    {
        final int aInternalBufferSize = 8192;

        byte[] aPartialMatch = new byte[cDocTypeStartBytes.length - 1];
        System.arraycopy(cDocTypeStartBytes, 0, aPartialMatch, 0, aPartialMatch.length);

        for (int i=1; i<aPartialMatch.length - 1; i++)
        {
            // Given
            byte[] aPrefixBytes = randomNonMatchingBytes(aInternalBufferSize - i);
            byte[] aSuffixBytes = randomNonMatchingBytes(randomLength(300));
            byte[] aStreamBytes = concatenate(aPrefixBytes, aPartialMatch, aSuffixBytes);

            // When
            byte[] aResult = filter(aStreamBytes);

            // Then
            assertArrayEquals(aStreamBytes, aResult);
        }
    }


    @Test
    public void streamEndingWithPartialMatchIsNotFiltered() throws IOException
    {
        // Given
        byte[] aPartialMatch = new byte[cDocTypeStartBytes.length - 1];
        System.arraycopy(cDocTypeStartBytes, 0, aPartialMatch, 0, aPartialMatch.length);
        byte[] aPrefixBytes = randomNonMatchingBytes(randomLength(300));
        byte[] aStreamBytes = concatenate(aPrefixBytes, aPartialMatch);

        // When
        byte[] aResult = filter(aStreamBytes);

        // Then
        assertArrayEquals(aStreamBytes, aResult);
    }


    /**
     * Create a new {@code DocTypeFilterStream} containing the specified bytes.
     *
     * @param pStreamBytes  The bytes to return from the created stream.
     *
     * @return  A new {@code DocTypeFilterStream}.
     */
    static private DocTypeFilterStream createDocTypeFilterStream(byte[] pStreamBytes)
    {
        return new DocTypeFilterStream(new ByteArrayInputStream(pStreamBytes));
    }


    /**
     * Filter a sequence of bytes by wrapping them in a {@code DocTypeFilterStream}.
     *
     * @param pBytes    The bytes to filter.
     *
     * @return  The bytes read from the {@code DocTypeFilterStream}.
     *
     * @throws IOException  if the test fails unexpectedly.
     */
    static private byte[] filter(byte[] pBytes) throws IOException
    {
        byte[] aBuffer = new byte[64];
        DocTypeFilterStream aIn = createDocTypeFilterStream(pBytes);
        ByteArrayOutputStream aOut = new ByteArrayOutputStream(8192);
        do
        {
            int aNumBytes = aIn.read(aBuffer);
            if (aNumBytes >= 0)
                aOut.write(aBuffer, 0, aNumBytes);
            else
                break;
        }
        while (true);

        return aOut.toByteArray();
    }


    /**
     * Concatenate byte arrays into one large byte array.
     *
     * @param pBytes    The byte arrays to concatenate.
     *
     * @return  The resulting array.
     */
    static private byte[] concatenate(byte[]... pBytes)
    {
        int aTotalLength = 0;
        for (byte[] aBytes : pBytes)
            aTotalLength += aBytes.length;

        byte[] aConcatenated = new byte[aTotalLength];
        int aNumBytesCopied = 0;
        for (byte[] aBytes : pBytes)
        {
            System.arraycopy(aBytes, 0, aConcatenated, aNumBytesCopied, aBytes.length);
            aNumBytesCopied += aBytes.length;
        }

        return aConcatenated;
    }


    /**
     * Create some random content inside &quot;DOCTYPE&quot; tag.
     *
     * @param pNumBytes The number of random bytes to generate.
     *
     * @return  A byte array with the bytes &quot;&lt;!DOCTYPExxxx&gt;&quot; where &quot;xxxx&quot;
     *          are the random bytes.
     */
    static private byte[] randomDocTypeTag(int pNumBytes)
    {
        byte[] aDocTypeTag = new byte[cDocTypeStartBytes.length + pNumBytes + 1];
        byte[] aRandomTagBytes = randomNonMatchingBytes(pNumBytes);
        System.arraycopy(cDocTypeStartBytes, 0, aDocTypeTag, 0, cDocTypeStartBytes.length);
        System.arraycopy(aRandomTagBytes, 0, aDocTypeTag, cDocTypeStartBytes.length, aRandomTagBytes.length);
        aDocTypeTag[aDocTypeTag.length - 1] = cDocTypeEndByte;
        return aDocTypeTag;
    }


    /**
     * Create the expected replacement for the bytes of &quot;DOCTYPE&quot; tag.
     *
     * @param pDocTypeBytes The bytes to get the expected replacement for.
     *
     * @return  A byte array of the same length as {@code pDocTypeBytes} filled with spaces.
     */
    static private byte[] expectedReplacement(byte[] pDocTypeBytes)
    {
        byte[] aReplacement = new byte[pDocTypeBytes.length];
        Arrays.fill(aReplacement, cReplacementByte);
        return aReplacement;
    }


    /**
     * Get a random length of a byte sequence to use as test data.
     *
     * @param pLength The maximum length (inclusive).
     *
     * @return  A random number in the range {@code [1..pLength]}.
     */
    static private int randomLength(int pLength)
    {
        return cRandom.nextInt(pLength) + 1;
    }


    /**
     * Get an array of random bytes to use as test data. The generated bytes are guaranteed to not
     * contain the sequence &quot;<!DOCTYPE>&quot;
     *
     * @param pNumBytes The number of bytes to generate.
     *
     * @return  An array of random bytes.
     */
    static private byte[] randomNonMatchingBytes(int pNumBytes)
    {
        byte[] aBytes = new byte[pNumBytes];
        cRandom.nextBytes(aBytes);

        // Remove all 'E' (the last byte in "<!DOCTYPE") to ensure the random bytes don't
        // contain the sequence to filter out. Also remove all '>' to ensure a <!DOCTYPE> tag being
        // filtered isn't closed prematurely.
        for (int i=0; i<aBytes.length; i++)
            if (aBytes[i] == 'E' || aBytes[i] == '>')
                aBytes[i] = 'x';

        return aBytes;
    }


    /**
     * Invoke {@code org.junit.Assert.assertArrayEquals} and add the entire byte arrays to the
     * message in case of failure.
     *
     * @param pExpected The expected bytes.
     * @param pActual   The actual bytes.
     */
    static private void assertArrayEquals(byte[] pExpected, byte[] pActual)
    {
        try
        {
            org.junit.Assert.assertArrayEquals(pExpected, pActual);
        }
        catch (AssertionError ae)
        {
            String aMessage =
                            ae.getMessage() +
                            System.getProperty("line.separator") +
                            "expected = '" +
                            byteArrayToString(pExpected) +
                            ", actual = '" +
                            byteArrayToString(pActual) +
                            '\'';
            System.err.println(aMessage);
            throw new ArrayComparisonFailure(aMessage, ae, 0);
        }
    }


    static private String byteArrayToString(byte[] pBytes)
    {
        StringBuilder aBuffer = new StringBuilder(pBytes.length * 2);
        for (byte b : pBytes)
            aBuffer.append(Integer.toHexString(b));

        return aBuffer.toString();
    }
}
