/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.myire.quill.common.PrettyPrintableTests.assertPrettyPrint;


/**
 * Unit tests for {@code org.myire.quill.dependency.ExclusionSpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ExclusionSpecTest
{
    /**
     * The constructor should throw a {@code NullPointerException} when both arguments are null.
     */
    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullArguments()
    {
        new ExclusionSpec(null, null);
    }


    /**
     * {@code getGroup()} should return the group passed to the constructor.
     */
    @Test
    public void getGroupReturnsValuePassedToCtor()
    {
        // Given
        String aGroup = "org.myire";

        // When
        ExclusionSpec aSpec = new ExclusionSpec(aGroup, null);

        // Then
        assertEquals(aGroup, aSpec.getGroup());

        // When
        aSpec = new ExclusionSpec(null, "m");

        // Then
        assertNull(aSpec.getGroup());
    }


    /**
     * {@code getModule()} should return the module passed to the constructor.
     */
    @Test
    public void getModuleReturnsValuePassedToCtor()
    {
        // Given
        String aModule = "modulo";

        // When
        ExclusionSpec aSpec = new ExclusionSpec(null, aModule);

        // Then
        assertEquals(aModule, aSpec.getModule());

        // When
        aSpec = new ExclusionSpec("g", null);

        // Then
        assertNull(aSpec.getModule());
    }


    /**
     * The map returned by {@code toMap()} should contain the group and the module if both are
     * present.
     */
    @Test
    public void toMapReturnsGroupAndModule()
    {
        // Given
        String aGroup = "org.myire", aModule = "quill";
        ExclusionSpec aSpec = new ExclusionSpec(aGroup, aModule);

        // When
        Map<String, String> aMap = aSpec.toMap();

        // Then
        assertEquals(aMap.get("group"), aGroup);
        assertEquals(aMap.get("module"), aModule);
    }


    /**
     * The map returned by {@code toMap()} should not contain the group if it is null.
     */
    @Test
    public void toMapOmitsNullGroup()
    {
        // Given
        String aModule = "quill";
        ExclusionSpec aSpec = new ExclusionSpec(null, aModule);

        // When
        Map<String, String> aMap = aSpec.toMap();

        // Then
        assertFalse(aMap.containsKey("group"));
        assertEquals(aMap.get("module"), aModule);
    }


    /**
     * The map returned by {@code toMap()} should not contain the module if it is null.
     */
    @Test
    public void toMapOmitsNullModule()
    {
        // Given
        String aGroup = "com.acme";
        ExclusionSpec aSpec = new ExclusionSpec(aGroup, null);

        // When
        Map<String, String> aMap = aSpec.toMap();

        // Then
        assertEquals(aMap.get("group"), aGroup);
        assertFalse(aMap.containsKey("module"));
    }


    /**
     * {@code prettyPrint()} should print the group and the module if both are present.
     */
    @Test
    public void prettyPrintPrintsGroupAndModule()
    {
        // Given
        String aGroup = "org.myire", aModule = "quill";
        ExclusionSpec aSpec = new ExclusionSpec(aGroup, aModule);

        // Then
        String aExpected = "exclude group: '" + aGroup + "', module: '" + aModule + '\'';
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrint()} should omit the group if not present.
     */
    @Test
    public void prettyPrintOmitsNullGroup()
    {
        // Given
        String aModule = "quill";
        ExclusionSpec aSpec = new ExclusionSpec(null, aModule);

        // Then
        String aExpected = "exclude module: '" + aModule + '\'';
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrint()} should omit the module if not present.
     */
    @Test
    public void prettyPrintOmitsNullModule()
    {
        // Given
        String aGroup = "g.g.g";
        ExclusionSpec aSpec = new ExclusionSpec(aGroup, null);

        // Then
        String aExpected = "exclude group: '" + aGroup + '\'';
        assertPrettyPrint(aSpec, aExpected);
    }
}
