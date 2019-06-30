/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.myire.quill.dependency.ExclusionSpec;
import org.myire.quill.dependency.ModuleDependencySpec;
import org.myire.quill.maven.EffectivePomLoader;

import org.myire.quill.repository.CredentialsSpec;
import org.myire.quill.repository.RepositorySpec;
import org.myire.quill.test.ExpandedJar;
import org.myire.quill.test.ExternalToolTest;


/**
 * Unit tests for {@code EffectivePomLoaderImpl}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class EffectivePomLoaderImplTest extends ExternalToolTest<EffectivePomLoader>
{
    static private ExpandedJar cMavenToolJar;


    public EffectivePomLoaderImplTest()
    {
        super(
            EffectivePomLoader.class,
            "org.myire.quill.maven.impl",
            "EffectivePomLoaderImpl",
            cMavenToolJar.toFileCollection());
    }


    /**
     * Expand the jar containing the Maven jar files into a temporary directory.
     *
     * @throws IOException  if expanding the jar file fails.
     */
    @BeforeClass
    static public void expandMavenToolJar() throws IOException
    {
        cMavenToolJar = new ExpandedJar("/external-tools/", "mavenJars.jar");
    }


    /**
     * Delete the temporary directory with the Maven jar files.
     */
    @AfterClass
    static public void deleteExpandedJars()
    {
        cMavenToolJar.deleteExtractedFiles();
    }


    /**
     * Calling {@code init} with a null pom file argument should throw a
     * {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void initThrowsForNullPomFile()
    {
        newToolProxy().init(null, new File("maven.settings"));
    }


    /**
     * The group ID specified in the pom file should be loaded from the file and returned by
     * {@code getGroupId}.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void groupIdIsLoaded() throws IOException
    {
        // Given
        String aGroupId = "org.myire";
        File aPomFile = createPomFile(
            "<artifactId>arty</artifactId>",
            "<groupId>" + aGroupId + "</groupId>",
            "<version>3.13</version>"
        );

        // Given
        EffectivePomLoader aLoader = newToolProxy();
        aLoader.init(aPomFile, null);

        // When
        String aLoadedGroupId = aLoader.getGroupId();

        // Then
        assertEquals(aGroupId, aLoadedGroupId);
    }


    /**
     * The artifact ID specified in the pom file should be loaded from the file and returned by
     * {@code getArtifactId}.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void artifactIdIsLoaded() throws IOException
    {
        // Given
        String aArtifactId = "art";
        File aPomFile = createPomFile(
            "<artifactId>" + aArtifactId + "</artifactId>",
            "<groupId>org.grp</groupId>",
            "<version>3.13.1</version>"
        );

        // Given
        EffectivePomLoader aLoader = newToolProxy();
        aLoader.init(aPomFile, null);

        // When
        String aLoadedArtifactId = aLoader.getArtifactId();

        // Then
        assertEquals(aArtifactId, aLoadedArtifactId);
    }


    /**
     * The version specified in the pom file should be loaded from the file and returned by
     * {@code getVersion}.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void versionIsLoaded() throws IOException
    {
        // Given
        String aVersion = "47.11";
        File aPomFile = createPomFile(
            "<artifactId>xyz</artifactId>",
            "<groupId>org.grp</groupId>",
            "<version>" + aVersion + "</version>"
        );

        // Given
        EffectivePomLoader aLoader = newToolProxy();
        aLoader.init(aPomFile, null);

        // When
        String aLoadedVersion = aLoader.getVersion();

        // Then
        assertEquals(aVersion, aLoadedVersion);
    }


    /**
     * A dependency with only artifact ID, group ID, and version should be loaded as a
     * {@code ModuleDependencySpec} with those values and default values for all other properties.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void simpleDependencyIsLoaded() throws IOException
    {
        // Given
        String aGroup = "org.myire";
        String aName = "thrice";
        String aVersion = "3.0.1";
        File aPomFile = createPomFile(
            "<artifactId>xyz</artifactId>",
            "<groupId>org.grp</groupId>",
            "<version>1</version>",
            "<dependencies>",
            " <dependency>",
            "<groupId>" + aGroup + "</groupId>",
            "<artifactId>" + aName + "</artifactId>",
            "<version>" + aVersion + "</version>",
            "</dependency>",
            "</dependencies>"
        );

        // Given
        EffectivePomLoader aLoader = newToolProxy();
        aLoader.init(aPomFile, null);

        // When
        Collection<ModuleDependencySpec> aDependencies = aLoader.getDependencies();

        // Then
        assertEquals(1, aDependencies.size());
        ModuleDependencySpec aDependencySpec = aDependencies.iterator().next();
        assertEquals(aGroup, aDependencySpec.getGroup());
        assertEquals(aName, aDependencySpec.getName());
        assertEquals(aVersion, aDependencySpec.getVersion());
        assertEquals("compile", aDependencySpec.getConfiguration());
        assertNull(aDependencySpec.getClassifier());
        assertNull(aDependencySpec.getExtension());
        assertTrue(aDependencySpec.isTransitive());
        assertFalse(aDependencySpec.isChanging());
        assertFalse(aDependencySpec.isForce());
        assertEquals(0, aDependencySpec.getNumArtifacts());
        assertEquals(0, aDependencySpec.getNumExclusions());
    }


    /**
     * A dependency with all possible values should be loaded as a {@code ModuleDependencySpec} with
     * those values.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void complexDependencyIsLoaded() throws IOException
    {
        // Given
        String aGroup = "com.acme";
        String aArtifact = "tools";
        String aVersion = "2.71";
        String aClassifier = "debug";
        String aType = "war";
        String aScope = "test";
        String aExcludedGroup = "org.noway";
        String aExcludedArtifact = "unwanted";
        File aPomFile = createPomFile(
            "<artifactId>xyz</artifactId>",
            "<groupId>org.grp</groupId>",
            "<version>1</version>",
            "<dependencies>",
            "<dependency>",
            "<groupId>" + aGroup + "</groupId>",
            "<artifactId>" + aArtifact + "</artifactId>",
            "<version>" + aVersion + "</version>",
            "<classifier>" + aClassifier + "</classifier>",
            "<type>" + aType + "</type>",
            "<scope>" + aScope + "</scope>",
            "<exclusions>",
            "<exclusion>",
            "<groupId>" + aExcludedGroup + "</groupId>",
            "<artifactId>" + aExcludedArtifact + "</artifactId>",
            "</exclusion>",
            "</exclusions>",
            "</dependency>",
            "</dependencies>"
        );

        // Given
        EffectivePomLoader aLoader = newToolProxy();
        aLoader.init(aPomFile, null);

        // When
        Collection<ModuleDependencySpec> aDependencies = aLoader.getDependencies();

        // Then
        assertEquals(1, aDependencies.size());
        ModuleDependencySpec aDependencySpec = aDependencies.iterator().next();
        assertEquals(aGroup, aDependencySpec.getGroup());
        assertEquals(aArtifact, aDependencySpec.getName());
        assertEquals(aVersion, aDependencySpec.getVersion());
        assertEquals(aScope, aDependencySpec.getConfiguration());
        assertEquals(aClassifier, aDependencySpec.getClassifier());
        assertEquals(aType, aDependencySpec.getExtension());
        assertTrue(aDependencySpec.isTransitive());
        assertFalse(aDependencySpec.isChanging());
        assertFalse(aDependencySpec.isForce());
        assertEquals(0, aDependencySpec.getNumArtifacts());
        assertEquals(1, aDependencySpec.getNumExclusions());

        List<ExclusionSpec> aExclusions = new ArrayList<>();
        aDependencySpec.forEachExclusion(aExclusions::add);
        ExclusionSpec aExclusionSpec = aExclusions.get(0);
        assertEquals(aExcludedGroup, aExclusionSpec.getGroup());
        assertEquals(aExcludedArtifact, aExclusionSpec.getModule());
    }


    /**
     * A repository without credentials should be loaded as a {@code MavenRepositorySpec} with
     * the non-credential values.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void repositoryWithoutCredentialsIsLoaded() throws IOException
    {
        // Given
        String aID = "myire-releases";
        String aUrl = "https://releases.maven.myire.org/maven2";
        File aPomFile = createPomFile(
            "<artifactId>xyz</artifactId>",
            "<groupId>org.grp</groupId>",
            "<version>1</version>"
        );
        File aSettingsFile = createSettingsFile(
            "<profiles>",
            "<profile>",
            "<id>default</id>",
            "<repositories>",
            "<repository>",
            "<id>" + aID + "</id>",
            "<url>" + aUrl + "</url>",
            "</repository>",
            "</repositories>",
            "</profile>",
            "</profiles>",
            "<activeProfiles><activeProfile>default</activeProfile></activeProfiles>"
        );

        // Given
        EffectivePomLoader aLoader = newToolProxy();
        aLoader.init(aPomFile, aSettingsFile);

        // When
        Collection<RepositorySpec> aRepos = aLoader.getRepositories();

        // Then
        assertEquals(1, aRepos.size());
        RepositorySpec aRepositorySpec = aRepos.iterator().next();
        assertEquals(aID, aRepositorySpec.getName());
        assertEquals(aUrl, aRepositorySpec.getUrl());
        assertNull(aRepositorySpec.getCredentials());
    }


    /**
     * A repository with credentials should be loaded as a {@code MavenRepositorySpec} with all
     * applicable values.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void repositoryWithCredentialsIsLoaded() throws IOException
    {
        // Given
        String aID = "password-protected";
        String aUrl = "https://authenticatred.maven.myire.org/maven2";
        String aUserName = "ussr";
        String aPassword = "secretSauce";

        File aPomFile = createPomFile(
            "<artifactId>xyz</artifactId>",
            "<groupId>org.grp</groupId>",
            "<version>1</version>"
        );
        File aSettingsFile = createSettingsFile(
            "<profiles>",
            "<profile>",
            "<id>default</id>",
            "<repositories>",
            "<repository>",
            "<id>" + aID + "</id>",
            "<url>" + aUrl + "</url>",
            "</repository>",
            "</repositories>",
            "</profile>",
            "</profiles>",
            "<servers>",
            "<server>",
            "<id>" + aID + "</id>",
            "<username>" + aUserName + "</username>",
            "<password>" + aPassword + "</password>\n",
            "</server>",
            "</servers>",
            "<activeProfiles><activeProfile>default</activeProfile></activeProfiles>"
        );

        // Given
        EffectivePomLoader aLoader = newToolProxy();
        aLoader.init(aPomFile, aSettingsFile);

        // When
        Collection<RepositorySpec> aRepos = aLoader.getRepositories();

        // Then
        assertEquals(1, aRepos.size());
        RepositorySpec aRepositorySpec = aRepos.iterator().next();
        assertEquals(aID, aRepositorySpec.getName());
        assertEquals(aUrl, aRepositorySpec.getUrl());
        CredentialsSpec aCredentials =  aRepositorySpec.getCredentials();
        assertEquals(aUserName, aCredentials.getUserName());
        assertEquals(aPassword, aCredentials.getPassword());
    }


    private File createPomFile(String... pContents) throws IOException
    {
        Collection<String> aLines = new ArrayList<>();
        aLines.add("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"");
        aLines.add(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        aLines.add(" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">");

        aLines.add(" <modelVersion>4.0.0</modelVersion>");
        Collections.addAll(aLines, pContents);
        aLines.add("</project>");

        return createTemporaryFile("pom", ".xml", aLines).toFile();
    }


    private File createSettingsFile(String... pContents) throws IOException
    {
        Collection<String> aLines = new ArrayList<>();
        aLines.add("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        aLines.add("<settings xmlns=\"http://maven.apache.org/POM/4.0.0\"");
        aLines.add(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        aLines.add(" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">");

        Collections.addAll(aLines, pContents);
        aLines.add("</settings>");

        return createTemporaryFile("settings", ".xml", aLines).toFile();
    }
}
