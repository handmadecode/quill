/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@code IvyImportExtension}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class IvyImportExtensionTest
{
    static private final String DEFAULT_IVY_VERSION = "2.5.0";
    static private final String DEFAULT_WILDCARD_CONFIGURATION = "compile";


    private final Project fMockProject = mock(Project.class);
    private final Configuration fMockConfiguration = mock(Configuration.class);


    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullProject()
    {
        new IvyImportExtension(null, fMockConfiguration);
    }


    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullConfiguration()
    {
        new IvyImportExtension(fMockProject, null);
    }


    @Test
    public void getSettingsFileReturnsNullByDefault()
    {
        // Given
        IvyImportExtension aExtension = new IvyImportExtension(fMockProject, fMockConfiguration);

        // Then
        assertNull(aExtension.getSettingsFile());
    }


    @Test
    public void getSettingsFileReturnsValueFromSetter()
    {
        // Given
        File aSettingsFile = new File("settings");
        when(fMockProject.file(any())).thenReturn(aSettingsFile);
        IvyImportExtension aExtension = new IvyImportExtension(fMockProject, fMockConfiguration);

        // When
        aExtension.setSettingsFile(aSettingsFile);

        // Then
        assertSame(aSettingsFile, aExtension.getSettingsFile());

        // When
        aExtension.setSettingsFile(null);

        // Then
        assertNull(aExtension.getSettingsFile());
    }


    @Test
    public void getIvyVersionReturnsDefaultValueIfNotSet()
    {
        // Given
        IvyImportExtension aExtension = new IvyImportExtension(fMockProject, fMockConfiguration);

        // Then
        assertEquals(DEFAULT_IVY_VERSION, aExtension.getIvyVersion());
    }


    @Test
    public void getIvyVersionReturnsValueFromSetter()
    {
        // Given
        String aVersion = "2.3.0";
        IvyImportExtension aExtension = new IvyImportExtension(fMockProject, fMockConfiguration);

        // When
        aExtension.setIvyVersion(aVersion);

        // Then
        assertEquals(aVersion, aExtension.getIvyVersion());

        // When
        aExtension.setIvyVersion(null);

        // Then
        assertEquals(DEFAULT_IVY_VERSION, aExtension.getIvyVersion());
    }


    @Test
    public void getIvyClassPathReturnsConfigurationByDefault()
    {
        // Given
        IvyImportExtension aExtension = new IvyImportExtension(fMockProject, fMockConfiguration);

        // Then
        assertSame(fMockConfiguration, aExtension.getIvyClassPath());
    }


    @Test
    public void getIvyClassPathReturnsValueFromSetter()
    {
        // Given
        FileCollection aClasspath = mock(FileCollection.class);
        IvyImportExtension aExtension = new IvyImportExtension(fMockProject, fMockConfiguration);

        // When
        aExtension.setIvyClassPath(aClasspath);

        // Then
        assertSame(aClasspath, aExtension.getIvyClassPath());

        // When
        aExtension.setIvyClassPath(null);

        // Then
        assertSame(fMockConfiguration, aExtension.getIvyClassPath());
    }


    @Test
    public void getWildcardConfigurationReturnsDefaultValueIfNotSet()
    {
        // Given
        IvyImportExtension aExtension = new IvyImportExtension(fMockProject, fMockConfiguration);

        // Then
        assertEquals(DEFAULT_WILDCARD_CONFIGURATION, aExtension.getWildcardConfiguration());
    }


    @Test
    public void getWildcardConfigurationReturnsValueFromSetter()
    {
        // Given
        String aConfig = "runtime";
        IvyImportExtension aExtension = new IvyImportExtension(fMockProject, fMockConfiguration);

        // When
        aExtension.setWildcardConfiguration(aConfig);

        // Then
        assertEquals(aConfig, aExtension.getWildcardConfiguration());

        // When
        aExtension.setWildcardConfiguration(null);

        // Then
        assertEquals(DEFAULT_WILDCARD_CONFIGURATION, aExtension.getWildcardConfiguration());
    }
}
