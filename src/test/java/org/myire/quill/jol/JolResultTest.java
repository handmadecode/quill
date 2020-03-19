/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


/**
 * Unit tests for {@code JolResult}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class JolResultTest
{
    @Test(expected = NullPointerException.class)
    public void constructorThrowsForNullVersion()
    {
        new JolResult(null, "");
    }


    @Test(expected = NullPointerException.class)
    public void constructorThrowsForNullDescription()
    {
        new JolResult("", null);
    }


    @Test
    public void getVersionReturnsValueFromConstructor()
    {
        // Given
        String aVersion = "0.9";

        // When
        JolResult aResult = new JolResult(aVersion, "");

        // Then
        assertEquals(aVersion, aResult.getVersion());
    }


    @Test
    public void getDescriptionReturnsValueFromConstructor()
    {
        // Given
        String aDescription = "Hotspot simulation";

        // When
        JolResult aResult = new JolResult("", aDescription);

        // Then
        assertEquals(aDescription, aResult.getDescription());
    }


    @Test
    public void newInstanceHasZeroValues()
    {
        // Given
        JolResult aResult = new JolResult("v", "d");

        // Then
        assertEquals(0, aResult.getNumPackages());
        assertEquals(0, aResult.getInternalAlignmentGapSize());
        assertEquals(0, aResult.getExternalAlignmentGapSize());
    }


    @Test
    public void classLayoutHasValuesFromConstructor()
    {
        // Given
        String aClassName = "c";
        String aPackageName = "p.q.r";
        String aEnclosingClassName = "o1.o2";
        int aHeaderSize = 12;
        long aInstanceSize = 12;

        // When
        JolResult.ClassLayout aClassLayout =
            new JolResult.ClassLayout(
                aClassName,
                aPackageName,
                aEnclosingClassName,
                aHeaderSize,
                aInstanceSize,
                Collections.emptyList()
            );

        // Then
        assertEquals(aClassName, aClassLayout.getClassName());
        assertEquals(aPackageName, aClassLayout.getPackageName());
        assertEquals(aEnclosingClassName, aClassLayout.getEnclosingClassName());
        assertEquals(aEnclosingClassName + '.' + aClassName, aClassLayout.getFullClassName());
        assertEquals(aPackageName + '.' + aEnclosingClassName + '.' + aClassName, aClassLayout.getFullyQualifiedName());
        assertEquals(0, aClassLayout.getInternalAlignmentGapSize());
        assertEquals(0, aClassLayout.getExternalAlignmentGapSize());
    }


    @Test
    public void fieldLayoutHasValuesFromConstructor()
    {
        // Given
        String aFieldName = "f";
        String aType = "double";
        long aOffset = 12;
        long aSize = 8;

        // When
        JolResult.FieldLayout aFieldLayout =
            new JolResult.FieldLayout(
                aFieldName,
                aType,
                aOffset,
                aSize);

        // Then
        assertEquals(aFieldName, aFieldLayout.getName());
        assertEquals(aType, aFieldLayout.getType());
        assertEquals(aOffset, aFieldLayout.getOffset());
        assertEquals(aSize, aFieldLayout.getSize());
    }


    @Test
    public void addingClassLayoutCreatesTheExpectedPackageLayouts()
    {
        // Given
        int aHeaderSize = 8;
        String aPackageName1 = "p1";
        String aPackageName2 = "p2";

        // Given (a class with internal alignment gap)
        JolResult.FieldLayout aField1 = new JolResult.FieldLayout("f1", "boolean", aHeaderSize, 1);
        JolResult.FieldLayout aField2 = new JolResult.FieldLayout("f2", "long", aHeaderSize + 8, 8);
        JolResult.ClassLayout aClass1 =
            new JolResult.ClassLayout("c1", aPackageName1, null, aHeaderSize, 24, Arrays.asList(aField1, aField2));

        // Given (a class with external alignment gap)
        JolResult.FieldLayout aField3 = new JolResult.FieldLayout("f1", "int", aHeaderSize, 4);
        JolResult.ClassLayout aClass2 =
            new JolResult.ClassLayout("c2", aPackageName1, null, aHeaderSize, 16, Collections.singletonList(aField3));

        // Given (a class in another package)
        JolResult.ClassLayout aClass3 =
            new JolResult.ClassLayout("c3", aPackageName2, null, aHeaderSize, aHeaderSize, Collections.emptyList());

        // When
        JolResult aResult = new JolResult("v", "d");
        aResult.add(aClass1);
        aResult.add(aClass2);
        aResult.add(aClass3);

        // Then
        assertEquals(2, aResult.getNumPackages());
        assertEquals(7, aResult.getInternalAlignmentGapSize());
        assertEquals(4, aResult.getExternalAlignmentGapSize());

        List<JolResult.PackageLayout> aPackages = new ArrayList<>();
        for (JolResult.PackageLayout aPackage : aResult.getPackages())
            aPackages.add(aPackage);
        assertEquals(aResult.getNumPackages(), aPackages.size());

        assertEquals(aPackageName1, aPackages.get(0).getName());
        assertEquals(2, aPackages.get(0).getNumClasses());
        assertEquals(7, aPackages.get(0).getInternalAlignmentGapSize());
        assertEquals(4, aPackages.get(0).getExternalAlignmentGapSize());

        assertEquals(aPackageName2, aPackages.get(1).getName());
        assertEquals(1, aPackages.get(1).getNumClasses());
        assertEquals(0, aPackages.get(1).getInternalAlignmentGapSize());
        assertEquals(0, aPackages.get(1).getExternalAlignmentGapSize());
    }
}
