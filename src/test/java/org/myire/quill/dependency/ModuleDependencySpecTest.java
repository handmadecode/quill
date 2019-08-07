/*
 * Copyright 2018-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.myire.quill.common.PrettyPrintableTests.assertPrettyPrint;


/**
 * Unit tests for {@code org.myire.quill.dependency.ModuleDependencySpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ModuleDependencySpecTest extends DependencySpecTest
{
    @Override
    protected ModuleDependencySpec createInstance(String pConfiguration)
    {
        return new ModuleDependencySpec(pConfiguration, "g", "n", "1.0");
    }


    /**
     * The constructor should throw a {@code NullPointerException} when passed a null module name.
     */
    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullName()
    {
        new ModuleDependencySpec("", "", null, "");
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
        ModuleDependencySpec aSpec = new ModuleDependencySpec("", aGroup, "", "");

        // Then
        assertEquals(aGroup, aSpec.getGroup());

        // When
        aSpec = new ModuleDependencySpec("", null, "", "");

        // Then
        assertNull(aSpec.getGroup());
    }


    /**
     * {@code getName()} should return the name passed to the constructor.
     */
    @Test
    public void getNameReturnsValuePassedToCtor()
    {
        // Given
        String aName = "module-name";

        // When
        ModuleDependencySpec aSpec = new ModuleDependencySpec("", "", aName, "");

        // Then
        assertEquals(aName, aSpec.getName());
    }


    /**
     * {@code getVersion()} should return the version passed to the constructor.
     */
    @Test
    public void getVersionReturnsValuePassedToCtor()
    {
        // Given
        String aVersion = "2.71";

        // When
        ModuleDependencySpec aSpec = new ModuleDependencySpec("", "", "", aVersion);

        // Then
        assertEquals(aVersion, aSpec.getVersion());

        // When
        aSpec = new ModuleDependencySpec("", "", "", null);

        // Then
        assertNull(aSpec.getVersion());
    }


    /**
     * {@code getClassifier()} should return null by default and then the last value passed to
     * {@code setClassifier()}.
     */
    @Test
    public void getClassifierReturnsTheExpectedValue()
    {
        // Given
        String aClassifier = "special";
        ModuleDependencySpec aSpec = createInstance("ccc");

        // Then (a dependency has no classifier by default).
        assertNull(aSpec.getClassifier());

        // When
        aSpec.setClassifier(aClassifier);

        // Then
        assertEquals(aClassifier, aSpec.getClassifier());

        // When
        aSpec.setClassifier(null);

        // Then
        assertNull(aSpec.getClassifier());
    }


    /**
     * {@code getClassifier()} should return null by default and then the last value passed to
     * {@code setClassifier()}.
     */
    @Test
    public void getExtensionReturnsTheExpectedValue()
    {
        // Given
        String aExtension = ".exe";
        ModuleDependencySpec aSpec = createInstance("ccc");

        // Then (a dependency has no extension by default).
        assertNull(aSpec.getExtension());

        // When
        aSpec.setExtension(aExtension);

        // Then
        assertEquals(aExtension, aSpec.getExtension());

        // When
        aSpec.setExtension(null);

        // Then
        assertNull(aSpec.getExtension());
    }


    /**
     * {@code isChanging()} should return false by default and then the last value passed to
     * {@code setChanging}.
     */
    @Test
    public void isChangingReturnsTheExpectedValue()
    {
        // Given
        ModuleDependencySpec aSpec = createInstance("compile");

        // Then (a dependency is not changing by default)
        assertFalse(aSpec.isChanging());

        // When
        aSpec.setChanging(true);

        // Then
        assertTrue(aSpec.isChanging());

        // When
        aSpec.setChanging(false);

        // Then
        assertFalse(aSpec.isChanging());
    }


    /**
     * {@code isForce()} should return false by default and then the last value passed to
     * {@code setForce}.
     */
    @Test
    public void isForceReturnsTheExpectedValue()
    {
        // Given
        ModuleDependencySpec aSpec = createInstance("compile");

        // Then (a dependency has a false force attribute by default)
        assertFalse(aSpec.isForce());

        // When
        aSpec.setForce(true);

        // Then
        assertTrue(aSpec.isForce());

        // When
        aSpec.setForce(false);

        // Then
        assertFalse(aSpec.isForce());
    }


    /**
     * {@code toDependencyNotation()} should return the expected value based on the values passed to
     * the constructor.
     */
    @Test
    public void toDependencyNotationReturnsTheExpectedValue()
    {
        // Given
        String aGroup = "com.acme", aName = "scam", aVersion = "3.14";
        ModuleDependencySpec aSpec = new ModuleDependencySpec("", aGroup, aName, aVersion);

        // When
        String aNotation = aSpec.toDependencyNotation();

        // Then
        assertEquals(aGroup + ':' + aName + ':' + aVersion, aNotation);
    }


    /**
     * {@code prettyPrint()} should print the configuration name and dependency notation only if
     * all attributes have default values.
     */
    @Test
    public void prettyPrintPrintsNotationOnlyForDefaultDependency()
    {
        // Given
        String aConfiguration= "test";
        String aGroup = "com.acme", aName = "scam", aVersion = "3.14";
        ModuleDependencySpec aSpec = new ModuleDependencySpec(aConfiguration, aGroup, aName, aVersion);

        // Then
        assertPrettyPrint(aSpec, aConfiguration + " '" + aGroup + ':' + aName + ':' + aVersion + "'");
    }


    /**
     * {@code prettyPrint()} should print a closure with the non-default attribute values if the
     * dependency has any.
     */
    @Test
    public void prettyPrintPrintsClosureWithNonDefaultAttributes()
    {
        // Given
        String aConfiguration= "runtime", aGroup= "org.com", aModuleName = "io", aVersion = "47.11";
        String aExtension = "extn", aClassifier = "cls";
        String aExcludedModule ="ex-model";
        String aArtifactName = "arty", aArtifactExtension = "war";
        ModuleDependencySpec aSpec = new ModuleDependencySpec(aConfiguration, aGroup, aModuleName, aVersion);
        aSpec.setClassifier(aClassifier);
        aSpec.setExtension(aExtension);
        aSpec.setChanging(true);
        aSpec.setForce(true);
        aSpec.addExclusion(null, aExcludedModule);
        aSpec.addArtifact(aArtifactName, null, aArtifactExtension, null, null);

        // Then
        String[] aExpected = {
            aConfiguration + "('" + aGroup + ':' + aModuleName + ':' + aVersion + ':' + aClassifier + '@' + aExtension + "')",
            "{",
            "  changing = true",
            "  force = true",
            "  exclude module: '" + aExcludedModule + "'",
            "  artifact",
            "  {",
            "    name = '" + aArtifactName + "'",
            "    extension = '" + aArtifactExtension + "'",
            "  }",
            "}"
        };
        assertPrettyPrint(aSpec, aExpected);
    }


    /**
     * {@code matches()} should return the expected values when the module specification contains
     * a name but no group or version.
     */
    @Test
    public void matchesReturnsTheExpectedValueForSpecWithNameOnly()
    {
        // Given
        String aName = "mod-name";
        ModuleDependencySpec aSpec = new ModuleDependencySpec("cfg", null, aName, null);

        // Then
        assertTrue(aSpec.matches(null, aName, null));

        // No value matches
        assertFalse(aSpec.matches("g", null, "v"));
        assertFalse(aSpec.matches("g", "x" + aName, "v"));

        // Group and name do not match
        assertFalse(aSpec.matches("g", null, null));
        assertFalse(aSpec.matches("g", "x" + aName, null));

        // Group and version do not match
        assertFalse(aSpec.matches("g", aName, "v"));

        // Name and version do not match
        assertFalse(aSpec.matches(null, null, "v"));
        assertFalse(aSpec.matches(null, "x" + aName, "v"));

        // Group does not match
        assertFalse(aSpec.matches("g", aName, null));

        // Name does not match
        assertFalse(aSpec.matches(null, null, null));
        assertFalse(aSpec.matches(null, "x" + aName, null));

        // Version does not match
        assertFalse(aSpec.matches(null, aName, "v"));
    }


    /**
     * {@code matches()} should return the expected values when the module specification contains
     * a name and a group but no version.
     */
    @Test
    public void matchesReturnsTheExpectedValueForSpecWithNameAndGroup()
    {
        // Given
        String aGroup = "org.group";
        String aName = "mod-name";
        ModuleDependencySpec aSpec = new ModuleDependencySpec("cfg", aGroup, aName, null);

        // Then
        assertTrue(aSpec.matches(aGroup, aName, null));

        // No value matches
        assertFalse(aSpec.matches(null, null, "v"));
        assertFalse(aSpec.matches(null, "x" + aName, "v"));
        assertFalse(aSpec.matches("x" + aGroup, null, "v"));
        assertFalse(aSpec.matches("x" + aGroup, "x" + aName, "v"));

        // Group and name do not match
        assertFalse(aSpec.matches(null, null, null));
        assertFalse(aSpec.matches(null, "x" + aName, null));
        assertFalse(aSpec.matches("x" + aGroup, null, null));
        assertFalse(aSpec.matches("x" + aGroup, "x" + aName, null));

        // Group and version do not match
        assertFalse(aSpec.matches(null, aName, "v"));
        assertFalse(aSpec.matches("x" + aGroup, aName, "v"));

        // Name and version do not match
        assertFalse(aSpec.matches(aGroup, null, "v"));
        assertFalse(aSpec.matches(aGroup, "x" + aName, "v"));

        // Group does not match
        assertFalse(aSpec.matches(null, aName, null));
        assertFalse(aSpec.matches("x" + aGroup, aName, null));

        // Name does not match
        assertFalse(aSpec.matches(aGroup, null, null));
        assertFalse(aSpec.matches(aGroup, "x" + aName, null));

        // Version does not match
        assertFalse(aSpec.matches(aGroup, aName, "v"));
    }


    /**
     * {@code matches()} should return the expected values when the module specification contains
     * a name and a version but no group.
     */
    @Test
    public void matchesReturnsTheExpectedValueForSpecWithNameAndVersion()
    {
        // Given
        String aName = "mod-name";
        String aVersion = "2.71";
        ModuleDependencySpec aSpec = new ModuleDependencySpec("cfg", null, aName, aVersion);

        // Then
        assertTrue(aSpec.matches(null, aName, aVersion));

        // No value matches
        assertFalse(aSpec.matches("g", null, null));
        assertFalse(aSpec.matches("g", "x" + aName, null));
        assertFalse(aSpec.matches("g", null, "x" + aVersion));
        assertFalse(aSpec.matches("g", "x" + aName, "x" + aVersion));

        // Group and name do not match
        assertFalse(aSpec.matches("g", null, aVersion));
        assertFalse(aSpec.matches("g", "x" + aName, aVersion));

        // Group and version do not match
        assertFalse(aSpec.matches("g", aName, null));
        assertFalse(aSpec.matches("g", aName, "x" + aVersion));

        // Name and version do not match
        assertFalse(aSpec.matches(null, null, null));
        assertFalse(aSpec.matches(null, null, "x" + aVersion));
        assertFalse(aSpec.matches(null, "x" + aName, null));
        assertFalse(aSpec.matches(null, "x" + aName, "x" + aVersion));

        // Group does not match
        assertFalse(aSpec.matches("g", aName, aVersion));

        // Name does not match
        assertFalse(aSpec.matches(null, null, aVersion));
        assertFalse(aSpec.matches(null, "x" + aName, aVersion));

        // Version does not match
        assertFalse(aSpec.matches(null, aName, null));
        assertFalse(aSpec.matches(null, aName, "x" + aVersion));
    }


    /**
     * {@code matches()} should return the expected values when the module specification contains
     * a name, a group, and a version.
     */
    @Test
    public void matchesReturnsTheExpectedValueForSpecWithNameGroupAndVersion()
    {
        // Given
        String aGroup = "io.oi";
        String aName = "oiio";
        String aVersion = "3.14";
        ModuleDependencySpec aSpec = new ModuleDependencySpec("cfg", aGroup, aName, aVersion);

        // Then
        assertTrue(aSpec.matches(aGroup, aName, aVersion));

        // No value matches
        assertFalse(aSpec.matches(null, null, null));
        assertFalse(aSpec.matches(null, null, "x" + aVersion));
        assertFalse(aSpec.matches(null, "x" + aName, null));
        assertFalse(aSpec.matches(null, "x" + aName, "x" + aVersion));
        assertFalse(aSpec.matches("x" + aGroup, null, null));
        assertFalse(aSpec.matches("x" + aGroup, null, "x" + aVersion));
        assertFalse(aSpec.matches("x" + aGroup, "x" + aName, null));
        assertFalse(aSpec.matches("x" + aGroup, "x" + aName, "x" + aVersion));

        // Group and name do not match
        assertFalse(aSpec.matches(null, null, aVersion));
        assertFalse(aSpec.matches(null, "x" + aName, aVersion));
        assertFalse(aSpec.matches("x" + aGroup, null, aVersion));
        assertFalse(aSpec.matches("x" + aGroup, "x" + aName, aVersion));

        // Group and version do not match
        assertFalse(aSpec.matches(null, aName, null));
        assertFalse(aSpec.matches(null, aName, "x" + aVersion));
        assertFalse(aSpec.matches("x" + aGroup, aName, null));
        assertFalse(aSpec.matches("x" + aGroup, aName, "x" + aVersion));

        // Name and version do not match
        assertFalse(aSpec.matches(aGroup, null, null));
        assertFalse(aSpec.matches(aGroup, null, "x" + aVersion));
        assertFalse(aSpec.matches(aGroup, "x" + aName, null));
        assertFalse(aSpec.matches(aGroup, "x" + aName, "x" + aVersion));

        // Group does not match
        assertFalse(aSpec.matches(null, aName, aVersion));
        assertFalse(aSpec.matches("x" + aGroup, aName, aVersion));

        // Name does not match
        assertFalse(aSpec.matches(aGroup, null, aVersion));
        assertFalse(aSpec.matches(aGroup, "x" + aName, aVersion));

        // Version does not match
        assertFalse(aSpec.matches(aGroup, aName, null));
        assertFalse(aSpec.matches(aGroup, aName, "x" + aVersion));
    }


    @Test
    public void addToAddsDependencyToProject()
    {
        // Given
        String aConfigurationName = "cfgX";
        Project aProject = ProjectBuilder.builder().build();
        aProject.getConfigurations().create(aConfigurationName);

        // When
        DependencySpec aSpec = createInstance(aConfigurationName);
        boolean aResult = aSpec.addTo(aProject);

        // Then
        assertTrue(aResult);
        assertFalse(aProject.getConfigurations().getByName(aConfigurationName).getDependencies().isEmpty());
    }
}
