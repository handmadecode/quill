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
import static org.junit.Assert.assertTrue;

import static org.myire.quill.common.PrettyPrintableTests.assertPrettyPrint;


/**
 * Unit tests for {@code org.myire.quill.dependency.ProjectDependencySpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ProjectDependencySpecTest extends DependencySpecTest
{
    @Override
    protected DependencySpec createInstance(String pConfiguration)
    {
        return new ProjectDependencySpec(pConfiguration, ":projectA");
    }


    /**
     * The constructor should throw a {@code NullPointerException} for a project path argument.
     */
    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullProjectPath()
    {
        new ProjectDependencySpec("cfg", null);
    }


    /**
     * {@code getProjectPath()} should return the project path passed to the constructor.
     */
    @Test
    public void getProjectPathReturnsValuePassedToCtor()
    {
        // Given
        String aPath = ":p1:p2";

        // When
        ProjectDependencySpec aSpec = new ProjectDependencySpec("zzz", aPath);

        // Then
        assertEquals(aPath, aSpec.getProjectPath());
    }


    /**
     * {@code toDependencyNotation()} should return the expected value based on the project path
     * passed to the constructor.
     */
    @Test
    public void toDependencyNotationReturnsTheExpectedValue()
    {
        // Given
        String aPath = ":p1:p2";
        ProjectDependencySpec aSpec = new ProjectDependencySpec("zzz", aPath);

        // When
        String aNotation = aSpec.toDependencyNotation();
        // Then
        assertEquals("project('" + aPath + "')", aNotation);
    }


    /**
     * {@code prettyPrint()} should print the configuration name and dependency notation only if
     * all attributes have default values.
     */
    @Test
    public void prettyPrintPrintsNotationOnlyForDefaultDependency()
    {
        // Given
        String aConfiguration= "config";
        String aPath= ":p:pp:ppp";
        ProjectDependencySpec aSpec = new ProjectDependencySpec(aConfiguration, aPath);

        // Then
        assertPrettyPrint(aSpec, aConfiguration + " project('" + aPath + "')");
    }


    /**
     * {@code prettyPrint()} should print a closure with the non-default attribute values if the
     * dependency has any.
     */
    @Test
    public void prettyPrintPrintsClosureWithNonDefaultAttributes()
    {
        // Given
        String aConfiguration= "compile", aPath= ":quill";
        String aExcludedGroup ="exg";
        String aArtifactName = "aname", aArtifactType = "jar";
        ProjectDependencySpec aSpec = new ProjectDependencySpec(aConfiguration, aPath);
        aSpec.setTransitive(false);
        aSpec.addExclusion(aExcludedGroup, null);
        aSpec.addArtifact(aArtifactName, aArtifactType, null, null, null);

        // Then
        String[] aExpected = {
            aConfiguration + "(project('" + aPath + "'))",
            "{",
            "  transitive = false",
            "  exclude group: '" + aExcludedGroup + "'",
            "  artifact",
            "  {",
            "    name = '" + aArtifactName + "'",
            "    type = '" + aArtifactType + "'",
            "  }",
            "}"
        };
        assertPrettyPrint(aSpec, aExpected);
    }


    @Test
    public void addToAddsDependencyToProject()
    {
        // Given
        String aConfigurationName = "cfgX";
        String aSubProjectName = "projectX";
        Project aProject = ProjectBuilder.builder().build();
        aProject.getConfigurations().create(aConfigurationName);
        ProjectBuilder.builder().withParent(aProject).withName(aSubProjectName).build();

        // When
        ProjectDependencySpec aSpec = new ProjectDependencySpec(aConfigurationName, ':' + aSubProjectName);
        boolean aResult = aSpec.addTo(aProject);

        // Then
        assertTrue(aResult);
        assertFalse(aProject.getConfigurations().getByName(aConfigurationName).getDependencies().isEmpty());
    }
}
