/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import static org.myire.quill.common.PrettyPrintableTests.assertPrettyPrint;
import static org.myire.quill.common.PrettyPrintableTests.assertPrintAction;


/**
 * Unit tests for {@code org.myire.quill.configuration.ConfigurationSpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ConfigurationSpecTest
{
    /**
     * The constructor should throw a {@code NullPointerException} when passed a null name argument.
     */
    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullName()
    {
        new ConfigurationSpec(null);
    }


    /**
     * {@code getName()} should return the name passed to the constructor.
     */
    @Test
    public void getNameReturnsNamePassedToCtor()
    {
        // Given
        String aName = "cfgX";

        // When
        ConfigurationSpec aSpec = new ConfigurationSpec(aName);

        // Then
        assertEquals(aName, aSpec.getName());
    }


    /**
     * {@code isTransitive()} should return true by default and the last value passed to
     * {@code setTransitive()}.
     */
    @Test
    public void isTransitiveReturnsTheExpectedValue()
    {
        // Given
        ConfigurationSpec aSpec = new ConfigurationSpec("xx");

        // Then (a configuration is transitive by default).
        assertTrue(aSpec.isTransitive());

        // When
        aSpec.setTransitive(false);

        // Then
        assertFalse(aSpec.isTransitive());

        // When
        aSpec.setTransitive(true);

        // Then
        assertTrue(aSpec.isTransitive());
    }


    /**
     * {@code isVisible()} should return true by default and the last value passed to
     * {@code setVisible()}.
     */
    @Test
    public void isVisibleReturnsTheExpectedValue()
    {
        // Given
        ConfigurationSpec aSpec = new ConfigurationSpec("yz");

        // Then (a configuration is visible by default).
        assertTrue(aSpec.isVisible());

        // When
        aSpec.setVisible(false);

        // Then
        assertFalse(aSpec.isVisible());

        // When
        aSpec.setVisible(true);

        // Then
        assertTrue(aSpec.isVisible());
    }


    /**
     * {@code getDescription()} should return null by default and the last value passed to
     * {@code setDescription()}.
     */
    @Test
    public void getDescriptionReturnsTheExpectedValue()
    {
        // Given
        String aDescription = "Some config";
        ConfigurationSpec aSpec = new ConfigurationSpec("zx");

        // Then (a configuration has no description by default).
        assertNull(aSpec.getDescription());

        // When
        aSpec.setDescription(aDescription);

        // Then
        assertEquals(aDescription, aSpec.getDescription());

        // When
        aSpec.setDescription(null);

        // Then
        assertNull(aSpec.getDescription());
    }


    /**
     * {@code getNumExtendedConfigurations()} should return 0 by default and the number of
     * configuration names passed to {@code addExtendedConfiguration()}.
     */
    @Test
    public void getNumExtendedConfigurationsReturnsTheExpectedValue()
    {
        // Given
        ConfigurationSpec aSpec = new ConfigurationSpec("nnn");

        // Then (a configuration does not extend any other configurations by default).
        assertEquals(0, aSpec.getNumExtendedConfigurations());

        // When
        aSpec.addExtendedConfiguration("c1");

        // Then
        assertEquals(1, aSpec.getNumExtendedConfigurations());

        // When
        aSpec.addExtendedConfiguration("c2");

        // Then
        assertEquals(2, aSpec.getNumExtendedConfigurations());
    }


    /**
     * {@code forEachExtendedConfiguration()} should pass all extended configuration names to the
     * specified action.
     */
    @Test
    public void forEachExtendedConfigurationPassesAllConfigurationsToAction()
    {
        // Given
        AtomicInteger aCount = new AtomicInteger();
        Consumer<String> aAction = s -> aCount.incrementAndGet();
        ConfigurationSpec aSpec = new ConfigurationSpec("??");

        // When
        aSpec.forEachExtendedConfiguration(aAction);

        // Then (a configuration does not extend any other configurations by default).
        assertEquals(0, aCount.get());

        // Given
        aSpec.addExtendedConfiguration("c1");

        // When
        aSpec.forEachExtendedConfiguration(aAction);

        // Then
        assertEquals(1, aCount.get());

        // Given
        aCount.set(0);
        aSpec.addExtendedConfiguration("c2");

        // When
        aSpec.forEachExtendedConfiguration(aAction);

        // Then
        assertEquals(2, aCount.get());
    }


    /**
     * {@code prettyPrint()} should print only the configuration name for a configuration with
     * default values.
     */
    @Test
    public void prettyPrintPrintsNameOnlyForDefaultConfiguration()
    {
        // Given
        String aName = "anyCfg";
        ConfigurationSpec aSpec = new ConfigurationSpec(aName);

        // Then
        assertPrettyPrint(aSpec, aName);
    }


    /**
     * {@code prettyPrint()} should print a closure with the transitive value for a configuration
     * with a non-default transitive value.
     */
    @Test
    public void prettyPrintPrintsNonDefaultTransitiveValue()
    {
        // Given
        String aName = "someCfg";
        ConfigurationSpec aSpec = new ConfigurationSpec(aName);
        aSpec.setTransitive(false);

        String[] aExpected = {
            aName,
            "{",
            "  transitive = false",
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrint()} should print a closure with the visible value for a configuration with
     * a non-default visible value.
     */
    @Test
    public void prettyPrintPrintsNonDefaultVisibleValue()
    {
        // Given
        String aName = "wazqx";
        ConfigurationSpec aSpec = new ConfigurationSpec(aName);
        aSpec.setVisible(false);

        String[] aExpected = {
            aName,
            "{",
            "  visible = false",
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrint()} should print a closure with the configuration's description if it has
     * one.
     */
    @Test
    public void prettyPrintPrintsDescription()
    {
        // Given
        String aName = "xyzzy", aDescription = "This is a configuration";
        ConfigurationSpec aSpec = new ConfigurationSpec(aName);
        aSpec.setDescription(aDescription);

        String[] aExpected = {
            aName,
            "{",
            "  description = '" + aDescription + '\'',
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrint()} should print a closure with the configuration's extended configuration
     * names if it has any.
     */
    @Test
    public void prettyPrintPrintsExtendedConfigurations()
    {
        // Given
        String aName = "cname", aExt1 = "e1", aExt2 = "e2";
        ConfigurationSpec aSpec = new ConfigurationSpec(aName);
        aSpec.addExtendedConfiguration(aExt1);
        aSpec.addExtendedConfiguration(aExt2);

        String[] aExpected = {
            aName,
            "{",
            "  extendsFrom " + aExt1 + ", " + aExt2,
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrint()} should print a closure with all configuration properties that have non-
     * default values.
     */
    @Test
    public void prettyPrintPrintsAllNonDefaultValues()
    {
        // Given
        String aName = "custom", aDescription = "All custom values", aExtended = "parentCfg";
        ConfigurationSpec aSpec = new ConfigurationSpec(aName);
        aSpec.setTransitive(false);
        aSpec.setVisible(false);
        aSpec.setDescription(aDescription);
        aSpec.addExtendedConfiguration(aExtended);

        String[] aExpected = {
            aName,
            "{",
            "  transitive = false",
            "  visible = false",
            "  description = '" + aDescription + '\'',
            "  extendsFrom " + aExtended,
            "}"
        };

        // Then
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code prettyPrintConfigurations()} should print a closure with all configuration
     * specifications passed to the method.
     */
    @Test
    public void prettyPrintConfigurationsPrintsTheExpectedClosure()
    {
        // Given
        List<ConfigurationSpec> aSpecs = new ArrayList<>();

        String aConfig1 = "cfgA";
        aSpecs.add(new ConfigurationSpec(aConfig1));

        String aConfig2 = "cfgB", aDescription2 = "desc";
        ConfigurationSpec aSpec = new ConfigurationSpec(aConfig2);
        aSpec.setDescription(aDescription2);
        aSpec.setVisible(false);
        aSpecs.add(aSpec);

        String aConfig3 = "cfgC";
        aSpec = new ConfigurationSpec(aConfig3);
        aSpec.setTransitive(false);
        aSpec.addExtendedConfiguration(aConfig1);
        aSpec.addExtendedConfiguration(aConfig2);
        aSpecs.add(aSpec);

        // Then
        String[] aExpected = {
            "configurations",
            "{",
            "  " + aConfig1,
            "  " + aConfig2,
            "  {",
            "    visible = false",
            "    description = '" + aDescription2 + '\'',
            "  }",
            "  " + aConfig3,
            "  {",
            "    transitive = false",
            "    extendsFrom " + aConfig1 + ", " + aConfig2,
            "  }",
            "}"
        };

        assertPrintAction(p -> ConfigurationSpec.prettyPrintConfigurations(p, aSpecs), aExpected);
    }
}
