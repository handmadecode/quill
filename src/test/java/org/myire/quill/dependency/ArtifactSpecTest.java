/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.myire.quill.common.PrettyPrintableTests.assertPrettyPrint;


/**
 * Unit tests for {@code org.myire.quill.dependency.ArtifactSpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ArtifactSpecTest
{
    /**
     * The constructor should throw a {@code NullPointerException} when passed a null name argument.
     */
    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullName()
    {
        new ArtifactSpec(null, "type", "extension", "classifier", "url");
    }


    /**
     * {@code getName()} should return the name passed to the constructor.
     */
    @Test
    public void getNameReturnsValuePassedToCtor()
    {
        // Given
        String aName = "arty";

        // When
        ArtifactSpec aSpec = new ArtifactSpec(aName, null, null, null, null);

        // Then
        assertEquals(aName, aSpec.getName());
    }


    /**
     * {@code getType()} should return the type passed to the constructor.
     */
    @Test
    public void getTypeReturnsValuePassedToCtor()
    {
        // Given
        String aType = "typ";

        // When
        ArtifactSpec aSpec = new ArtifactSpec("n", aType, null, null, null);

        // Then
        assertEquals(aType, aSpec.getType());

        // When
        aSpec = new ArtifactSpec("n", null, null, null, null);

        // Then
        assertNull(aSpec.getType());
    }


    /**
     * {@code getExtension()} should return the extension passed to the constructor.
     */
    @Test
    public void getExtensionReturnsValuePassedToCtor()
    {
        // Given
        String aExtension = "war";

        // When
        ArtifactSpec aSpec = new ArtifactSpec("n", null, aExtension, null, null);

        // Then
        assertEquals(aExtension, aSpec.getExtension());

        // When
        aSpec = new ArtifactSpec("n", null, null, null, null);

        // Then
        assertNull(aSpec.getExtension());
    }


    /**
     * {@code getClassifier()} should return the classifier passed to the constructor.
     */
    @Test
    public void getClassifierReturnsValuePassedToCtor()
    {
        // Given
        String aClassifier = "cl";

        // When
        ArtifactSpec aSpec = new ArtifactSpec("n", null, null, aClassifier, null);

        // Then
        assertEquals(aClassifier, aSpec.getClassifier());

        // When
        aSpec = new ArtifactSpec("n", null, null, null, null);

        // Then
        assertNull(aSpec.getClassifier());
    }


    /**
     * {@code getUrl()} should return the url passed to the constructor.
     */
    @Test
    public void getUrlReturnsValuePassedToCtor()
    {
        // Given
        String aUrl = "/secret-artifacts";

        // When
        ArtifactSpec aSpec = new ArtifactSpec("n", null, null, null, aUrl);

        // Then
        assertEquals(aUrl, aSpec.getUrl());

        // When
        aSpec = new ArtifactSpec("n", null, null, null, null);

        // Then
        assertNull(aSpec.getUrl());
    }


    /**
     * {@code prettyPrint()} should print a closure with all artifact properties.
     */
    @Test
    public void prettyPrintPrintsAllProperties()
    {
        // Given
        String aName = "artsy", aType = "shady", aExtension = "exo", aClassifier = "classy", aUrl = "curly";
        ArtifactSpec aSpec = new ArtifactSpec(aName, aType, aExtension, aClassifier, aUrl);

        String[] aExpected = {
            "artifact",
            "{",
            "  name = '" + aName + '\'',
            "  type = '" + aType + '\'',
            "  extension = '" + aExtension + '\'',
            "  classifier = '" + aClassifier + '\'',
            "  url = '" + aUrl + '\'',
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrint()} should omit properties with a null value from the closure.
     */
    @Test
    public void prettyPrintOmitsNullProperties()
    {
        // Given
        String aName = "artsy", aType = "shady", aExtension = "exo";
        ArtifactSpec aSpec = new ArtifactSpec(aName, aType, aExtension, null, null);

        // Then
        String[] aExpected = {
            "artifact",
            "{",
            "  name = '" + aName + '\'',
            "  type = '" + aType + '\'',
            "  extension = '" + aExtension + '\'',
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }
}
