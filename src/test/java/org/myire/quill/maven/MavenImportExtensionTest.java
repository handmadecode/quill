/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven;

import java.io.File;

import org.gradle.api.Project;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.FileCollection;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@code  MavenImportExtension}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class MavenImportExtensionTest
{
    static private final String DEFAULT_MAVEN_VERSION = "3.6.1";

    private final Project fMockProject = mock(Project.class);
    private final Configuration fMockConfiguration = mock(Configuration.class);
    private final RepositoryHandler fMockRepositoryHandler = mock(RepositoryHandler.class);


    {
        when(fMockProject.getRepositories()).thenReturn(fMockRepositoryHandler);
        when(fMockConfiguration.getIncoming()).thenReturn(mock(ResolvableDependencies.class));
    }


    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullProject()
    {
        new MavenImportExtension(null, fMockConfiguration);
    }


    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullConfiguration()
    {
        new MavenImportExtension(fMockProject, null);
    }


    @Test
    public void getSettingsFileReturnsNullByDefault()
    {
        // Given
        MavenImportExtension aExtension = new MavenImportExtension(fMockProject, fMockConfiguration);

        // Then
        assertNull(aExtension.getSettingsFile());
    }


    @Test
    public void getSettingsFileReturnsValueFromSetter()
    {
        // Given
        File aSettingsFile = new File("settings");
        when(fMockProject.file(any())).thenReturn(aSettingsFile);
        MavenImportExtension aExtension = new MavenImportExtension(fMockProject, fMockConfiguration);

        // When
        aExtension.setSettingsFile(new Object());

        // Then
        assertSame(aSettingsFile, aExtension.getSettingsFile());

        // When
        aExtension.setSettingsFile(null);

        // Then
        assertNull(aExtension.getSettingsFile());
    }


    @Test
    public void getMavenVersionReturnsDefaultValueIfNotSet()
    {
        // Given
        MavenImportExtension aExtension = new MavenImportExtension(fMockProject, fMockConfiguration);

        // Then
        assertEquals(DEFAULT_MAVEN_VERSION, aExtension.getMavenVersion());
    }


    @Test
    public void getMavenVersionReturnsValueFromSetter()
    {
        // Given
        String aVersion = "2.5.0";
        MavenImportExtension aExtension = new MavenImportExtension(fMockProject, fMockConfiguration);

        // When
        aExtension.setMavenVersion(aVersion);

        // Then
        assertEquals(aVersion, aExtension.getMavenVersion());

        // When
        aExtension.setMavenVersion(null);

        // Then
        assertEquals(DEFAULT_MAVEN_VERSION, aExtension.getMavenVersion());
    }


    @Test
    public void getMavenClassPathReturnsConfigurationByDefault()
    {
        // Given
        MavenImportExtension aExtension = new MavenImportExtension(fMockProject, fMockConfiguration);

        // Then
        assertSame(fMockConfiguration, aExtension.getMavenClassPath());
    }


    @Test
    public void getMavenClassPathReturnsValueFromSetter()
    {
        // Given
        FileCollection aClasspath = mock(FileCollection.class);
        MavenImportExtension aExtension = new MavenImportExtension(fMockProject, fMockConfiguration);

        // When
        aExtension.setMavenClassPath(aClasspath);

        // Then
        assertSame(aClasspath, aExtension.getMavenClassPath());

        // When
        aExtension.setMavenClassPath(null);

        // Then
        assertSame(fMockConfiguration, aExtension.getMavenClassPath());
    }


    @Test
    public void getClasspathRepositoryReturnsMavenCentralByDefault()
    {
        // Given
        MavenArtifactRepository aMavenCentral = mock(MavenArtifactRepository.class);
        RepositoryHandler aRepositories = mock(RepositoryHandler.class);
        when(aRepositories.mavenCentral()).thenReturn(aMavenCentral);

        when(fMockProject.getRepositories()).thenReturn(aRepositories);

        MavenImportExtension aExtension = new MavenImportExtension(fMockProject, fMockConfiguration);

        // Then
        assertSame(aMavenCentral, aExtension.getClassPathRepository());
    }


    @Test
    public void getClasspathRepositoryReturnsValueFromSetter()
    {
        // Given
        ArtifactRepository aRepository = mock(ArtifactRepository.class);
        MavenImportExtension aExtension = new MavenImportExtension(fMockProject, fMockConfiguration);

        // When
        aExtension.setClassPathRepository(aRepository);

        // Then
        assertSame(aRepository, aExtension.getClassPathRepository());

        // When
        aExtension.setClassPathRepository(null);

        // Then
        assertNull(aExtension.getClassPathRepository());
    }
}
