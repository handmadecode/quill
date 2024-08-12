/*
 * Copyright 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.util.Scanner;
import java.util.regex.Pattern;


/**
 * A version number is a triplet consisting of a major, minor, and patch integer value.
 */
public class VersionNumber implements Comparable<VersionNumber>
{
    static private final Pattern NOT_A_DIGIT = Pattern.compile("\\D");
    private final int fMajor;
    private final int fMinor;
    private final int fPatch;


    /**
     * Create a new {@code VersionNumber}.
     *
     * @param pMajor    The major version value.
     * @param pMinor    The minor version value.
     * @param pPatch    The patch version value.
     */
    public VersionNumber(int pMajor, int pMinor, int pPatch)
    {
        fMajor = pMajor;
        fMinor = pMinor;
        fPatch = pPatch;
    }


    /**
     * Create a new {@code VersionNumber} from a string on dotted notation.
     *
     * @param pVersionNumber    The version number on dotted notation.
     *
     * @throws NullPointerException if {@code pVersionNumber} is null.
     */
    public VersionNumber(String pVersionNumber)
    {
        Scanner aScanner = new Scanner(pVersionNumber).useDelimiter(NOT_A_DIGIT);
        fMajor = aScanner.hasNextInt() ? aScanner.nextInt() : 0;
        fMinor = aScanner.hasNextInt() ? aScanner.nextInt() : 0;
        fPatch = aScanner.hasNextInt() ? aScanner.nextInt() : 0;
        aScanner.close();
    }


    public int getMajor()
    {
        return fMajor;
    }


    public int getMinor()
    {
        return fMinor;
    }


    public int getPatch()
    {
        return fPatch;
    }


    @Override
    public int compareTo(VersionNumber pOther)
    {
        int aResult = fMajor - pOther.fMajor;
        if (aResult == 0)
        {
            aResult = fMinor - pOther.fMinor;
            if (aResult == 0)
                aResult = fPatch - pOther.fPatch;
        }

        return aResult;
    }


    @Override
    public String toString()
    {
        return fMajor + "." + fMinor + "." + fPatch;
    }
}
