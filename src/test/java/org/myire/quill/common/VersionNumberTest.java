/*
 * Copyright 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests for {@code org.myire.quill.common.VersionNumber}.
 */
public class VersionNumberTest
{
    @Test
    public void gettersReturnValuesFromConstructor()
    {
        // Given
        int aMajor = 1, aMinor = 2, aPatch = 3;

        // When
        VersionNumber aVersionNumber = new VersionNumber(aMajor, aMinor, aPatch);

        // Then
        assertEquals(aMajor, aVersionNumber.getMajor());
        assertEquals(aMinor, aVersionNumber.getMinor());
        assertEquals(aPatch, aVersionNumber.getPatch());
    }


    @Test
    public void versionNumberStringIsParsed()
    {
        // Given
        int aMajor = 2, aMinor = 9, aPatch = 1;

        // When
        VersionNumber aVersionNumber = new VersionNumber(aMajor + "." + aMinor + "." + aPatch);

        // Then
        assertEquals(aMajor, aVersionNumber.getMajor());
        assertEquals(aMinor, aVersionNumber.getMinor());
        assertEquals(aPatch, aVersionNumber.getPatch());
    }


    @Test
    public void emptyStringIsParsed()
    {
        // When
        VersionNumber aVersionNumber = new VersionNumber("");

        // Then
        assertEquals(0, aVersionNumber.getMajor());
        assertEquals(0, aVersionNumber.getMinor());
        assertEquals(0, aVersionNumber.getPatch());
    }


    @Test
    public void singleDigitStringIsParsed()
    {
        // Given
        int aMajor = 17;

        // When
        VersionNumber aVersionNumber = new VersionNumber(String.valueOf(aMajor));

        // Then
        assertEquals(aMajor, aVersionNumber.getMajor());
        assertEquals(0, aVersionNumber.getMinor());
        assertEquals(0, aVersionNumber.getPatch());
    }


    @Test
    public void stringEndingWithDotIsParsed()
    {
        // Given
        int aMajor = 42, aMinor = 93;

        // When
        VersionNumber aVersionNumber = new VersionNumber(aMajor + "." + aMinor + ".");

        // Then
        assertEquals(aMajor, aVersionNumber.getMajor());
        assertEquals(aMinor, aVersionNumber.getMinor());
        assertEquals(0, aVersionNumber.getPatch());
    }


    @Test
    public void stringEndingWithQualifierIsParsed()
    {
        // Given
        int aMajor = 712, aMinor = 0, aPatch = 16;

        // When
        VersionNumber aVersionNumber = new VersionNumber(aMajor + "." + aMinor + "." + aPatch + "-beta.2");

        // Then
        assertEquals(aMajor, aVersionNumber.getMajor());
        assertEquals(aMinor, aVersionNumber.getMinor());
        assertEquals(aPatch, aVersionNumber.getPatch());
    }


    @Test
    public void compareToDetectsMajorVersionDiff()
    {
        // Given
        VersionNumber aVersion1 = new VersionNumber(2, 9, 17);
        VersionNumber aVersion2 = new VersionNumber(3, 1, 0);

        // Then
        assertTrue(aVersion1.compareTo(aVersion2) < 0);
        assertTrue(aVersion2.compareTo(aVersion1) > 0);
    }


    @Test
    public void compareToDetectsMinorVersionDiff()
    {
        // Given
        VersionNumber aVersion1 = new VersionNumber(4, 2, 1);
        VersionNumber aVersion2 = new VersionNumber(4, 3, 0);

        // Then
        assertTrue(aVersion1.compareTo(aVersion2) < 0);
        assertTrue(aVersion2.compareTo(aVersion1) > 0);
    }


    @Test
    public void compareToDetectsPatchVersionDiff()
    {
        // Given
        VersionNumber aVersion1 = new VersionNumber(17, 0, 11);
        VersionNumber aVersion2 = new VersionNumber(17, 0, 26);

        // Then
        assertTrue(aVersion1.compareTo(aVersion2) < 0);
        assertTrue(aVersion2.compareTo(aVersion1) > 0);
    }


    @Test
    public void compareToDetectsEqualVersionNumbers()
    {
        // Given
        VersionNumber aVersion1 = new VersionNumber(2, 1, 0);
        VersionNumber aVersion2 = new VersionNumber("2.1");

        // Then
        assertEquals(0, aVersion1.compareTo(aVersion2));
        assertEquals(0, aVersion2.compareTo(aVersion1));
    }


    @Test
    public void toStringReturnsTheExpectedValue()
    {
        // Given
        int aMajor = 4, aMinor = 1, aPatch = 0;
        VersionNumber aVersionNumber = new VersionNumber(aMajor, aMinor, aPatch);
        String aExpected = aMajor + "." + aMinor + "." + aPatch;

        // Then
        assertEquals(aExpected, aVersionNumber.toString());
    }
}
