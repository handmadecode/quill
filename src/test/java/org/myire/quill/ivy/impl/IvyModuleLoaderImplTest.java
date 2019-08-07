/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.myire.quill.configuration.ConfigurationSpec;
import org.myire.quill.dependency.ArtifactSpec;
import org.myire.quill.dependency.ExclusionSpec;
import org.myire.quill.dependency.ModuleDependencySpec;
import org.myire.quill.ivy.IvyModuleLoader;

import org.myire.quill.test.ExpandedJar;
import org.myire.quill.test.ExternalToolTest;


/**
 * Unit tests for {@code IvyModuleLoaderImpl}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class IvyModuleLoaderImplTest extends ExternalToolTest<IvyModuleLoader>
{
    static private ExpandedJar cIvyToolJar;


    public IvyModuleLoaderImplTest()
    {
        super(
            IvyModuleLoader.class,
            "org.myire.quill.ivy.impl",
            "IvyModuleLoaderImpl",
            cIvyToolJar.toFileCollection());
    }


    /**
     * Expand the jar containing the Ivy jar file into a temporary directory.
     *
     * @throws IOException  if expanding the jar file fails.
     */
    @BeforeClass
    static public void expandIvyToolJar() throws IOException
    {
        cIvyToolJar = new ExpandedJar("/external-tools/", "ivyJars.jar");
    }


    /**
     * Delete the temporary directory with the Ivy jar file.
     */
    @AfterClass
    static public void deleteExpandedJars()
    {
        cIvyToolJar.deleteExtractedFiles();
    }


    /**
     * Calling {@code init} with a null Ivy module file argument should throw a
     * {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void initThrowsForNullIvyFile()
    {
        newToolProxy().init(null, new File("ivy.settings"));
    }


    /**
     * The organisation specified in the Ivy module's <i>info</i> element should be loaded from the
     * file and returned by {@code getOrganisation}.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void organisationIsLoaded() throws IOException
    {
        // Given
        String aOrganisation = "org.myire";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"" + aOrganisation + "\" module=\"quill\"/>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        String aLoadedOrganisation = aLoader.getOrganisation();

        // Then
        assertEquals(aOrganisation, aLoadedOrganisation);
    }


    /**
     * The module name specified in the Ivy module's <i>info</i> element should be loaded from the
     * file and returned by {@code getModuleName}.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void moduleNameIsLoaded() throws IOException
    {
        // Given
        String aModuleName = "quill";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\""+ aModuleName + "\"/>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        String aLoadedName = aLoader.getModuleName();

        // Then
        assertEquals(aModuleName, aLoadedName);
    }


    /**
     * The revision specified in the Ivy module's <i>info</i> element should be loaded from the
     * file and returned by {@code getModuleName}.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void revisionIsLoaded() throws IOException
    {
        // Given
        String aRevision = "3.14";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"quill\" revision=\""+ aRevision + "\"/>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        String aLoadedRevision = aLoader.getRevision();

        // Then
        assertEquals(aRevision, aLoadedRevision);
    }


    /**
     * A property from the Ivy settings file used in the Ivy module file should be propagated when
     * loaded.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void propertyFromSettingsFileIsLoaded() throws IOException
    {
        // Given
        String aRevision = "3.14";
        File aSettingsFile = createIvySettingsFile(
            "<property name=\"revision-prop\" value=\"" + aRevision + "\"/>"
        );
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"quill\" revision=\"${revision-prop}\"/>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, aSettingsFile);

        // When
        String aLoadedRevision = aLoader.getRevision();

        // Then
        assertEquals(aRevision, aLoadedRevision);
    }


    /**
     * An Ivy file with no configurations should not load any configurations.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void fileWithoutConfigurationsLoadsNoConfiguration() throws IOException
    {
        // Given
        File aModuleFile = createIvyModuleFile("<info organisation=\"org.myire\" module=\"test\"/>");

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ConfigurationSpec> aConfigs = aLoader.getConfigurations();

        // Then
        assertEquals(0, aConfigs.size());
    }


    /**
     * An Ivy configuration with only a name should be loaded as a {@code ConfigurationSpec} with
     * that name and default values for all other properties.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void simpleConfigurationHasDefaultValues() throws IOException
    {
        // Given
        String aConfigurationName = "cfg";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations>",
            "<conf name=\"" + aConfigurationName + "\"/>",
            "</configurations>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ConfigurationSpec> aConfigs = aLoader.getConfigurations();

        // Then
        assertEquals(1, aConfigs.size());
        ConfigurationSpec aConfigurationSpec = aConfigs.iterator().next();
        assertEquals(aConfigurationName, aConfigurationSpec.getName());
        assertTrue(aConfigurationSpec.isTransitive());
        assertTrue(aConfigurationSpec.isVisible());
        assertNull(aConfigurationSpec.getDescription());
        assertEquals(0, aConfigurationSpec.getNumExtendedConfigurations());
    }


    /**
     * An Ivy configuration's description should be loaded into the {@code ConfigurationSpec}
     * description.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void configurationDescriptionIsLoaded() throws IOException
    {
        // Given
        String aDescription = "Some kind of configuration";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations>",
            "<conf name=\"cfg\" description=\"" + aDescription + "\"/>",
            "</configurations>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ConfigurationSpec> aConfigs = aLoader.getConfigurations();

        // Then
        assertEquals(aDescription, aConfigs.iterator().next().getDescription());
    }


    /**
     * An Ivy configuration with public visibility should be loaded as a {@code ConfigurationSpec}
     * with visibility set to true.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void publicConfigurationIsVisible() throws IOException
    {
        // Given
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations>",
            "<conf name=\"cfg\" visibility=\"public\"/>",
            "</configurations>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ConfigurationSpec> aConfigs = aLoader.getConfigurations();

        // Then
        assertTrue(aConfigs.iterator().next().isVisible());
    }


    /**
     * An Ivy configuration with private visibility should be loaded as a {@code ConfigurationSpec}
     * with visibility set to false.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void privateConfigurationIsNotVisible() throws IOException
    {
        // Given
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations>",
            "<conf name=\"cfg\" visibility=\"private\"/>",
            "</configurations>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ConfigurationSpec> aConfigs = aLoader.getConfigurations();

        // Then
        assertFalse(aConfigs.iterator().next().isVisible());
    }


    /**
     * An Ivy configuration with its transitive attribute set to true should be loaded as a
     * {@code ConfigurationSpec} with transitivity set to true.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void transitiveConfigurationIsTransitive() throws IOException
    {
        // Given
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations>",
            "<conf name=\"cfg\" transitive=\"true\"/>",
            "</configurations>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ConfigurationSpec> aConfigs = aLoader.getConfigurations();

        // Then
        assertTrue(aConfigs.iterator().next().isTransitive());
    }


    /**
     * An Ivy configuration with its transitive attribute set to false should be loaded as a
     * {@code ConfigurationSpec} with transitivity set to false.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void nonTransitiveConfigurationIsNotTransitive() throws IOException
    {
        // Given
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations>",
            "<conf name=\"cfg\" transitive=\"false\"/>",
            "</configurations>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ConfigurationSpec> aConfigs = aLoader.getConfigurations();

        // Then
        assertFalse(aConfigs.iterator().next().isTransitive());
    }


    /**
     * An Ivy configuration's <i>extends</i> configurations should be loaded into the
     * {@code ConfigurationSpec} <i>extends</i> property.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void parentConfigurationsAreLoaded() throws IOException
    {
        // Given
        String aExtendsA = "cfgA", aExtendsB = "cfgB";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations>",
            "<conf name=\"cfgA\"/>",
            "<conf name=\"cfgB\"/>",
            "<conf name=\"cfg\" extends=\"" + aExtendsA + "," + aExtendsB + "\"/>",
            "</configurations>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ConfigurationSpec> aConfigs = aLoader.getConfigurations();

        // Then
        List<String> aLoadedExtends = new ArrayList<>();
        for (ConfigurationSpec aConfigurationSpec : aConfigs)
            if (aConfigurationSpec.getNumExtendedConfigurations() > 0)
                aConfigurationSpec.forEachExtendedConfiguration(aLoadedExtends::add);

        assertEquals(Arrays.asList(aExtendsA, aExtendsB), aLoadedExtends);
    }


    /**
     * A dependency with organisation, name, revision, and configuration should be loaded as a
     * {@code ModuleDependencySpec} with those values and default values for all other attributes.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void simpleDependencyIsLoaded() throws IOException
    {
        // Given
        String aConfiguration = "config";
        String aOrganisation = "com.org";
        String aName = "the-artifact";
        String aRevision = "3.141";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations><conf name=\"" + aConfiguration + "\"/></configurations>",
            "<dependencies>",
            "<dependency org=\"" + aOrganisation + "\" name=\"" + aName + "\" rev=\"" + aRevision + "\" conf=\"" + aConfiguration + "\"/>",
            "</dependencies>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ModuleDependencySpec> aDependencies =  aLoader.getDependencies();

        // Then
        assertEquals(1, aDependencies.size());
        ModuleDependencySpec aDependencySpec = aDependencies.iterator().next();
        assertEquals(aOrganisation, aDependencySpec.getGroup());
        assertEquals(aName, aDependencySpec.getName());
        assertEquals(aRevision, aDependencySpec.getVersion());
        assertEquals(aConfiguration, aDependencySpec.getConfiguration());
        assertNull(aDependencySpec.getClassifier());
        assertNull(aDependencySpec.getExtension());
        assertTrue(aDependencySpec.isTransitive());
        assertFalse(aDependencySpec.isChanging());
        assertFalse(aDependencySpec.isForce());
        assertEquals(0, aDependencySpec.getNumArtifacts());
        assertEquals(0, aDependencySpec.getNumExclusions());
    }


    /**
     * A dependency with no explicit configuration should get the default configuration loaded into
     * the {@code ModuleDependencySpec}.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void defaultConfigurationIsAppliedToDependency() throws IOException
    {
        // Given
        String aConfiguration = "config";
        String aOrganisation = "com.org";
        String aName = "the-artifact";
        String aRevision = "3.141";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations><conf name=\"" + aConfiguration + "\"/></configurations>",
            "<dependencies defaultconf=\"" + aConfiguration + "\">",
            "<dependency org=\"" + aOrganisation + "\" name=\"" + aName + "\" rev=\"" + aRevision + "\"/>",
            "</dependencies>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ModuleDependencySpec> aDependencies =  aLoader.getDependencies();

        // Then
        assertEquals(1, aDependencies.size());
        ModuleDependencySpec aDependencySpec = aDependencies.iterator().next();
        assertEquals(aOrganisation, aDependencySpec.getGroup());
        assertEquals(aName, aDependencySpec.getName());
        assertEquals(aRevision, aDependencySpec.getVersion());
        assertEquals(aConfiguration, aDependencySpec.getConfiguration());
        assertNull(aDependencySpec.getClassifier());
        assertNull(aDependencySpec.getExtension());
        assertTrue(aDependencySpec.isTransitive());
        assertFalse(aDependencySpec.isChanging());
        assertFalse(aDependencySpec.isForce());
        assertEquals(0, aDependencySpec.getNumArtifacts());
        assertEquals(0, aDependencySpec.getNumExclusions());
    }


    /**
     * A dependency with no explicit configuration should get the '*' configuration if there are no
     * configurations specified.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void nonExistingDefaultConfigurationIsAppliedToDependency() throws IOException
    {
        // Given
        String aConfiguration = "*";
        String aOrganisation = "org.net";
        String aName = "biz";
        String aRevision = "4.712";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<dependencies>",
            "<dependency org=\"" + aOrganisation + "\" name=\"" + aName + "\" rev=\"" + aRevision + "\"/>",
            "</dependencies>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ModuleDependencySpec> aDependencies =  aLoader.getDependencies();

        // Then
        assertEquals(1, aDependencies.size());
        ModuleDependencySpec aDependencySpec = aDependencies.iterator().next();
        assertEquals(aOrganisation, aDependencySpec.getGroup());
        assertEquals(aName, aDependencySpec.getName());
        assertEquals(aRevision, aDependencySpec.getVersion());
        assertEquals(aConfiguration, aDependencySpec.getConfiguration());
        assertNull(aDependencySpec.getClassifier());
        assertNull(aDependencySpec.getExtension());
        assertTrue(aDependencySpec.isTransitive());
        assertFalse(aDependencySpec.isChanging());
        assertFalse(aDependencySpec.isForce());
        assertEquals(0, aDependencySpec.getNumArtifacts());
        assertEquals(0, aDependencySpec.getNumExclusions());
    }


    /**
     * A dependency with the transitive, changing, and force attributes should be loaded as a
     * {@code ModuleDependencySpec} with the correct values for those attributes.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void complexDependencyIsLoaded() throws IOException
    {
        // Given
        String aConfiguration = "config";
        String aOrganisation = "com.org";
        String aName = "the-artifact";
        String aRevision = "3.141";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations><conf name=\"" + aConfiguration + "\"/></configurations>",
            "<dependencies>",
            "<dependency org=\"" + aOrganisation + "\" name=\"" + aName + "\" rev=\"" + aRevision + "\"",
            "            conf=\"" + aConfiguration + "\" transitive=\"false\" changing=\"true\" force=\"true\"/>",
            "</dependencies>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ModuleDependencySpec> aDependencies =  aLoader.getDependencies();

        // Then
        assertEquals(1, aDependencies.size());
        ModuleDependencySpec aDependencySpec = aDependencies.iterator().next();
        assertEquals(aOrganisation, aDependencySpec.getGroup());
        assertEquals(aName, aDependencySpec.getName());
        assertEquals(aRevision, aDependencySpec.getVersion());
        assertEquals(aConfiguration, aDependencySpec.getConfiguration());
        assertNull(aDependencySpec.getClassifier());
        assertNull(aDependencySpec.getExtension());
        assertFalse(aDependencySpec.isTransitive());
        assertTrue(aDependencySpec.isChanging());
        assertTrue(aDependencySpec.isForce());
        assertEquals(0, aDependencySpec.getNumArtifacts());
        assertEquals(0, aDependencySpec.getNumExclusions());
    }


    /**
     * A dependency with an exclusion should be loaded as a {@code ModuleDependencySpec} containing
     * that exclusion.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void dependencyWithExclusionIsLoaded() throws IOException
    {
        // Given
        String aExcluded1Org = "se.anonymous";
        String aExcluded1Module = "notthisone";
        String aExcluded2Org = "net.dubious";
        String aExcluded2Module = "neither";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations><conf name=\"cfg\"/></configurations>",
            "<dependencies>",
            "<dependency org=\"org.x\" name=\"nombre\" rev=\"1.2\" conf=\"cfg\">",
            "<exclude org=\"" + aExcluded1Org + "\" module=\"" + aExcluded1Module + "\" />",
            "<exclude org=\"" + aExcluded2Org + "\" module=\"" + aExcluded2Module + "\" />",
            "</dependency>",
            "</dependencies>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ModuleDependencySpec> aDependencies =  aLoader.getDependencies();

        // Then
        assertEquals(1, aDependencies.size());
        ModuleDependencySpec aDependencySpec = aDependencies.iterator().next();
        assertEquals(2, aDependencySpec.getNumExclusions());
        List<ExclusionSpec> aExclusions = new ArrayList<>();
        aDependencySpec.forEachExclusion(aExclusions::add);
        assertEquals(aExcluded1Org, aExclusions.get(0).getGroup());
        assertEquals(aExcluded1Module, aExclusions.get(0).getModule());
        assertEquals(aExcluded2Org, aExclusions.get(1).getGroup());
        assertEquals(aExcluded2Module, aExclusions.get(1).getModule());
    }


    /**
     * A dependency with an artifact should be loaded as a {@code ModuleDependencySpec} containing
     * that artifact.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void dependencyWithArtifactIsLoaded() throws IOException
    {
        // Given
        String aArtifact1Name = "a1";
        String aArtifact1Type = "t1";
        String aArtifact1Ext = ".ext1";
        String aArtifact1Url = "http://u1";
        String aArtifact2Name = "a2";
        String aArtifact2Type = "t2";
        File aModuleFile = createIvyModuleFile(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations><conf name=\"cfg\"/></configurations>",
            "<dependencies>",
            "<dependency org=\"org.x\" name=\"nombre\" rev=\"1.2\" conf=\"cfg\">",
            "<artifact name=\"" + aArtifact1Name + "\" type=\"" + aArtifact1Type + "\" ext=\"" + aArtifact1Ext + "\" url=\""+ aArtifact1Url+ "\"/>",
            "<artifact name=\"" + aArtifact2Name + "\" type=\"" + aArtifact2Type + "\"/>",
            "</dependency>",
            "</dependencies>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ModuleDependencySpec> aDependencies =  aLoader.getDependencies();

        // Then
        assertEquals(1, aDependencies.size());
        ModuleDependencySpec aDependencySpec = aDependencies.iterator().next();
        assertEquals(2, aDependencySpec.getNumArtifacts());
        List<ArtifactSpec> aArtifacts = new ArrayList<>();
        aDependencySpec.forEachArtifact(aArtifacts::add);
        assertEquals(aArtifact1Name, aArtifacts.get(0).getName());
        assertEquals(aArtifact1Type, aArtifacts.get(0).getType());
        assertEquals(aArtifact1Ext, aArtifacts.get(0).getExtension());
        assertNull(aArtifacts.get(0).getClassifier());
        assertEquals(aArtifact1Url, aArtifacts.get(0).getUrl());

        assertEquals(aArtifact2Name, aArtifacts.get(1).getName());
        assertEquals(aArtifact2Type, aArtifacts.get(1).getType());
        assertEquals(aArtifact2Type, aArtifacts.get(1).getExtension());
        assertNull(aArtifacts.get(1).getClassifier());
        assertNull(aArtifacts.get(1).getUrl());
    }


    /**
     * A dependency with a Maven namespace classifier should be loaded as a
     * {@code ModuleDependencySpec} with the classifier attribute set.
     *
     * @throws IOException  if creating a test file fails.
     */
    @Test
    public void dependencyWithMavenClassifierIsLoaded() throws IOException
    {
        // Given
        String aConfiguration = "B";
        String aOrganisation = "org.organization";
        String aName = "n";
        String aRevision = "0.07";
        String aClassifier = "classy";
        File aModuleFile = createIvyModuleFileWithMavenNameSpace(
            "<info organisation=\"org.myire\" module=\"test\"/>",
            "<configurations><conf name=\"" + aConfiguration + "\"/></configurations>",
            "<dependencies defaultconf=\"" + aConfiguration + "\">",
            "<dependency org=\"" + aOrganisation + "\" name=\"" + aName + "\" rev=\"" + aRevision + "\" m:classifier=\"" + aClassifier + "\"/>",
            "</dependencies>"
        );

        // Given
        IvyModuleLoader aLoader = newToolProxy();
        aLoader.init(aModuleFile, null);

        // When
        Collection<ModuleDependencySpec> aDependencies =  aLoader.getDependencies();

        // Then
        assertEquals(1, aDependencies.size());
        ModuleDependencySpec aDependencySpec = aDependencies.iterator().next();
        assertEquals(aOrganisation, aDependencySpec.getGroup());
        assertEquals(aName, aDependencySpec.getName());
        assertEquals(aRevision, aDependencySpec.getVersion());
        assertEquals(aConfiguration, aDependencySpec.getConfiguration());
        assertEquals(aClassifier, aDependencySpec.getClassifier());
        assertNull(aDependencySpec.getExtension());
        assertTrue(aDependencySpec.isTransitive());
        assertFalse(aDependencySpec.isChanging());
        assertFalse(aDependencySpec.isForce());
        assertEquals(0, aDependencySpec.getNumArtifacts());
        assertEquals(0, aDependencySpec.getNumExclusions());
    }


    private File createIvyModuleFile(String... pContents) throws IOException
    {
        Collection<String> aLines = new ArrayList<>();
        aLines.add("<ivy-module version=\"2.0\">");
        Collections.addAll(aLines, pContents);
        aLines.add("</ivy-module>");

        return createTemporaryFile("ivy", ".xml", aLines).toFile();
    }


    private File createIvyModuleFileWithMavenNameSpace(String... pContents) throws IOException
    {
        Collection<String> aLines = new ArrayList<>();
        aLines.add("<ivy-module version=\"2.0\" xmlns:m=\"http://ant.apache.org/ivy/maven\">");
        Collections.addAll(aLines, pContents);
        aLines.add("</ivy-module>");

        return createTemporaryFile("ivy", ".xml", aLines).toFile();
    }


    private File createIvySettingsFile(String... pContents) throws IOException
    {
        Collection<String> aLines = new ArrayList<>();
        aLines.add("<ivy-settings>");
        Collections.addAll(aLines, pContents);
        aLines.add("</ivy-settings>");

        return createTemporaryFile("ivy-settings", ".xml", aLines).toFile();
    }
}
