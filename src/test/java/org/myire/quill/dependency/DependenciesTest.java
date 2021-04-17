/*
 * Copyright 2018, 2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;


/**
 * Unit tests for {@code org.myire.quill.dependency.Dependencies}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class DependenciesTest
{
    /**
     * {@code findMatchingProject()} should return the starting project if it matches the dependency
     * specification.
     */
    @Test
    public void findMatchingProjectFindsStartingProject()
    {
        // Given
        String aConfig = "cfg", aGroup = "x.y.x", aName = "nnn", aVersion = "666";
        ModuleDependencySpec aDependencySpec = new ModuleDependencySpec(aConfig, aGroup, aName, aVersion);
        Project aProject = ProjectBuilder.builder().withName(aName).build();
        aProject.setGroup(aGroup);
        aProject.setVersion(aVersion);

        // When
        Project aFound = Dependencies.findMatchingProject(aDependencySpec, aProject);

        // Then
        assertSame(aProject, aFound);
    }


    /**
     * {@code findMatchingProject()} should return a sub-project of the starting project if it
     * matches the dependency specification.
     */
    @Test
    public void findMatchingProjectFindsSubProject()
    {
        // Given
        String aConfig = "cfg", aGroup = "x.y.z", aName = "nnn", aVersion = "666";
        ModuleDependencySpec aDependencySpec = new ModuleDependencySpec(aConfig, aGroup, aName, aVersion);
        Project aParent = ProjectBuilder.builder().withName("parent").build();
        Project aNonMatching = ProjectBuilder.builder().withParent(aParent).withName(aName + "x").build();
        aNonMatching.setGroup(aGroup);
        aNonMatching.setVersion(aVersion);
        Project aProject = ProjectBuilder.builder().withParent(aParent).withName(aName).build();
        aProject.setGroup(aGroup);
        aProject.setVersion(aVersion);

        // When
        Project aFound = Dependencies.findMatchingProject(aDependencySpec, aParent);

        // Then
        assertSame(aProject, aFound);
    }


    /**
     * {@code findMatchingProject()} should return a sub-project of the starting project if it
     * matches the dependency specification.
     */
    @Test
    public void findMatchingProjectFindsParentProject()
    {
        // Given
        String aConfig = "cfg", aGroup = "x.y.x", aName = "nnn", aVersion = "666";
        ModuleDependencySpec aDependencySpec = new ModuleDependencySpec(aConfig, aGroup, aName, aVersion);
        Project aProject = ProjectBuilder.builder().withName(aName).build();
        aProject.setGroup(aGroup);
        aProject.setVersion(aVersion);
        Project aSubProject = ProjectBuilder.builder().withParent(aProject).withName("sub").build();

        // When
        Project aFound = Dependencies.findMatchingProject(aDependencySpec, aSubProject);

        // Then
        assertSame(aProject, aFound);
    }


    /**
     * {@code findMatchingProject()} should return null if there is no match.
     */
    @Test
    public void findMatchingProjectReturnsNullForNoMatch()
    {
        // Given
        String aConfig = "cfg", aGroup = "x.y.x", aName = "nnn", aVersion = "666";
        ModuleDependencySpec aDependencySpec = new ModuleDependencySpec(aConfig, aGroup, aName, aVersion);
        Project aProject = ProjectBuilder.builder().withName(aName).build();

        // When
        Project aFound = Dependencies.findMatchingProject(aDependencySpec, aProject);

        // Then
        assertNull(aFound);
    }


    /**
     * Calling {@code addDependency()} with a {@code ModuleDependencySpec} specifying an external
     * module dependency should add that dependency to the project.
     */
    @Test
    public void externalModuleDependencyIsAdded()
    {
        // Given
        String aConfig = "cfg", aGroup = "x.y.x", aName = "nnn", aVersion = "666";
        ModuleDependencySpec aDependencySpec = new ModuleDependencySpec(aConfig, aGroup, aName, aVersion);
        aDependencySpec.setTransitive(false);
        aDependencySpec.setChanging(true);
        aDependencySpec.setForce(true);
        Project aProject = ProjectBuilder.builder().build();
        Configuration aConfiguration = aProject.getConfigurations().create(aConfig);

        // When
        boolean aResult = Dependencies.addDependency(aProject , aDependencySpec);

        // Then
        assertTrue(aResult);

        Dependency aDependency = aConfiguration.getDependencies().iterator().next();
        assertEquals(aGroup, aDependency.getGroup());
        assertEquals(aName, aDependency.getName());
        assertEquals(aVersion, aDependency.getVersion());
        assertFalse(((ModuleDependency) aDependency).isTransitive());
        assertTrue(((ExternalModuleDependency) aDependency).isChanging());
        assertEquals(aVersion, ((ExternalModuleDependency) aDependency).getVersionConstraint().getStrictVersion());
    }


    /**
     * Calling {@code addDependency()} with a {@code ModuleDependencySpec} specifying an external
     * module dependency with a configuration that does not exist in the project should not add the
     * dependency to the project.
     */
    @Test
    public void externalModuleDependencyIsNotAddedIfConfigurationIsMissing()
    {
        // Given
        ModuleDependencySpec aDependencySpec = new ModuleDependencySpec("cfg", "g", "n", "v");
        Project aProject = ProjectBuilder.builder().build();

        // When
        boolean aResult = Dependencies.addDependency(aProject, aDependencySpec);

        // Then
        assertFalse(aResult);
    }


    /**
     * Calling {@code addDependency()} with a {@code ProjectDependencySpec} should add that
     * dependency to the project.
     */
    @Test
    public void projectDependencyIsAdded()
    {
        // Given
        Project aProject = ProjectBuilder.builder().withName("parent").build();
        Project aSubProject = ProjectBuilder.builder().withParent(aProject).withName("sub").build();
        String aConfig = "cfg";
        Configuration aConfiguration = aProject.getConfigurations().create(aConfig);
        ProjectDependencySpec aDependencySpec = new ProjectDependencySpec(aConfig, aSubProject.getPath());
        aDependencySpec.setTransitive(false);
        String aArtifactName = "artsy";
        aDependencySpec.addArtifact(aArtifactName, null, null, null, null);

        // When
        boolean aResult = Dependencies.addDependency(aProject, aDependencySpec);

        // Then
        assertTrue(aResult);

        ProjectDependency aDependency = (ProjectDependency) aConfiguration.getDependencies().iterator().next();
        assertSame(aSubProject, aDependency.getDependencyProject());
        assertFalse(aDependency.isTransitive());
        assertEquals(aArtifactName, aDependency.getArtifacts().iterator().next().getName());
    }


    /**
     * Calling {@code addDependency()} with a {@code ProjectDependencySpec} specifying a project
     * dependency with a configuration that does not exist in the project should not add the
     * dependency to the project.
     */
    @Test
    public void projectIsNotAddedIfConfigurationIsMissing()
    {
        // Given
        ProjectDependencySpec aDependencySpec = new ProjectDependencySpec("cfg", ":path");
        Project aProject = ProjectBuilder.builder().build();

        // When
        boolean aResult = Dependencies.addDependency(aProject, aDependencySpec);

        // Then
        assertFalse(aResult);
    }
}
