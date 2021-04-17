/*
 * Copyright 2018-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;


/**
 * Common unit tests for subclasses of {@code org.myire.quill.dependency.DependencySpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
abstract public class DependencySpecTest
{
    /**
     * Create an instance of the subclass to test.
     *
     * @param pConfiguration    The configuration name to pass to the constructor.
     *
     * @return  A new instance of the subclass to test.
     */
    abstract protected DependencySpec createInstance(String pConfiguration);


    /**
     * The constructor should throw a {@code NullPointerException} when passed a null configuration
     * argument.
     */
    @Test(expected = NullPointerException.class)
    public void ctorThrowsForNullConfiguration()
    {
        createInstance(null);
    }


    /**
     * {@code getConfiguration()} should return the configuration name passed to the constructor or
     * the last value passed to {@code setConfiguration()}.
     */
    @Test
    public void getConfigurationReturnsTheExpectedValue()
    {
        // Given
        String aConfiguration1 = "cfgY", aConfiguration2 = "cfgZ";

        // When
        DependencySpec aSpec = createInstance(aConfiguration1);

        // Then
        assertEquals(aConfiguration1, aSpec.getConfiguration());

        // When
        aSpec.setConfiguration(aConfiguration2);

        // Then
        assertEquals(aConfiguration2, aSpec.getConfiguration());
    }


    /**
     * {@code setConfiguration()} should throw a {@code NullPointerException} when passed a null
     * argument.
     */
    @Test(expected = NullPointerException.class)
    public void setConfigurationThrowForNullArgument()
    {
        createInstance("abc").setConfiguration(null);
    }


    /**
     * {@code isTransitive()} should return true by default and then the last value passed to
     * {@code setTransitive}.
     */
    @Test
    public void isTransitiveReturnsTheExpectedValue()
    {
        // Given
        DependencySpec aSpec = createInstance("xyz");

        // Then (a dependency is transitive by default)
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
     * {@code getNumExclusions()} should return 0 by default and then the number of exclusion
     * specifications passed to {@code addExclusion()}.
     */
    @Test
    public void getNumExclusionsReturnsTheExpectedValue()
    {
        // Given
        DependencySpec aSpec = createInstance("xyz");

        // Then (a dependency does not have any exclusions by default).
        assertEquals(0, aSpec.getNumExclusions());

        // When
        aSpec.addExclusion("g1", "m1");

        // Then
        assertEquals(1, aSpec.getNumExclusions());

        // When
        aSpec.addExclusion("g2", "m2");

        // Then
        assertEquals(2, aSpec.getNumExclusions());
    }


    /**
     * {@code forEachExclusion()} should pass all exclusion specifications to the specified action.
     */
    @Test
    public void forEachExclusionPassesAllExclusionsToAction()
    {
        // Given
        String aGroup1 = "g1", aGroup2 = "g2", aGroup3 = null;
        String aModule1 = "m1", aModule2 = null, aModule3 = "m3";
        List<String> aGroups = new ArrayList<>();
        List<String> aModules = new ArrayList<>();

        DependencySpec aSpec = createInstance("zz");
        aSpec.addExclusion(aGroup1, aModule1);
        aSpec.addExclusion(aGroup2, aModule2);
        aSpec.addExclusion(aGroup3, aModule3);

        // When
        aSpec.forEachExclusion(
            e -> {
                aGroups.add(e.getGroup());
                aModules.add(e.getModule());
            }
        );

        // Then
        assertThat(aGroups, hasItems(aGroup1, aGroup2, aGroup3));
        assertThat(aModules, hasItems(aModule1, aModule2, aModule3));
    }


    /**
     * {@code getNumArtifacts()} should return 0 by default and then the number of artifact
     * specifications passed to {@code addArtifact()}.
     */
    @Test
    public void getNumArtifactsReturnsTheExpectedValue()
    {
        // Given
        DependencySpec aSpec = createInstance("wazqx");

        // Then (a dependency does not have any artifacts by default).
        assertEquals(0, aSpec.getNumArtifacts());

        // When
        aSpec.addArtifact("n", null, null, null, null);

        // Then
        assertEquals(1, aSpec.getNumArtifacts());

        // When
        aSpec.addArtifact("b", null, null, null, null);

        // Then
        assertEquals(2, aSpec.getNumArtifacts());
    }


    /**
     * {@code forEachArtifact()} should pass all artifact specifications to the specified action.
     */
    @Test
    public void forEachArtifactPassesAllArtifactsToAction()
    {
        // Given
        String aName1 = "n1", aName2 = "n2", aName3 = "n3";
        String aType1 = "t1", aType2 = null, aType3 = "t3";
        String aExtension1 = "e1", aExtension2 = null, aExtension3 = null;
        String aClassifier1 = "c1", aClassifier2 = "c2", aClassifier3 = null;
        String aUrl1 = "u1", aUrl2 = null, aUrl3 = "u3";
        List<String> aNames = new ArrayList<>();
        List<String> aTypes = new ArrayList<>();
        List<String> aExtensions = new ArrayList<>();
        List<String> aClassifiers = new ArrayList<>();
        List<String> aUrls = new ArrayList<>();

        DependencySpec aSpec = createInstance("zz");
        aSpec.addArtifact(aName1, aType1, aExtension1, aClassifier1, aUrl1);
        aSpec.addArtifact(aName2, aType2, aExtension2, aClassifier2, aUrl2);
        aSpec.addArtifact(aName3, aType3, aExtension3, aClassifier3, aUrl3);

        // When
        aSpec.forEachArtifact(
            a -> {
                aNames.add(a.getName());
                aTypes.add(a.getType());
                aExtensions.add(a.getExtension());
                aClassifiers.add(a.getClassifier());
                aUrls.add(a.getUrl());
            }
        );

        // Then
        assertThat(aNames, hasItems(aName1, aName2, aName3));
        assertThat(aTypes, hasItems(aType1, aType2, aType3));
        assertThat(aExtensions, hasItems(aExtension1, aExtension2, aExtension3));
        assertThat(aClassifiers, hasItems(aClassifier1, aClassifier2, aClassifier3));
        assertThat(aUrls, hasItems(aUrl1, aUrl2, aUrl3));
    }


    @Test
    public void valuesAreCopiedFromProjectDependency()
    {
        // Given
        ArtifactSpec aArtifact1 = new ArtifactSpec("n1", null, null, null, null);
        ArtifactSpec aArtifact2 = new ArtifactSpec("n2", null, null, null, null);
        ExclusionSpec aExclusion1 = new ExclusionSpec(null, "em1");
        ExclusionSpec aExclusion2 = new ExclusionSpec(null, "em2");
        ExclusionSpec aExclusion3 = new ExclusionSpec(null, "em3");
        ProjectDependencySpec aProjectDependency = new ProjectDependencySpec("c", "p");
        aProjectDependency.setTransitive(false);
        aProjectDependency.addArtifact(aArtifact1);
        aProjectDependency.addArtifact(aArtifact2);
        aProjectDependency.addExclusion(aExclusion1);
        aProjectDependency.addExclusion(aExclusion2);
        aProjectDependency.addExclusion(aExclusion3);

        // When
        DependencySpec aSpec = createInstance("c2");
        aSpec.setValues(aProjectDependency);

        // Then
        assertFalse(aSpec.isTransitive());

        List<ArtifactSpec> aCopiedArtifacts = new ArrayList<>();
        aSpec.forEachArtifact(aCopiedArtifacts::add);
        assertEquals(2, aCopiedArtifacts.size());
        assertEquals(aArtifact1.getName(), aCopiedArtifacts.get(0).getName());
        assertEquals(aArtifact2.getName(), aCopiedArtifacts.get(1).getName());

        List<ExclusionSpec> aCopiedExclusions = new ArrayList<>();
        aSpec.forEachExclusion(aCopiedExclusions::add);
        assertEquals(3, aCopiedExclusions.size());
        assertEquals(aExclusion1.getModule(), aCopiedExclusions.get(0).getModule());
        assertEquals(aExclusion2.getModule(), aCopiedExclusions.get(1).getModule());
        assertEquals(aExclusion3.getModule(), aCopiedExclusions.get(2).getModule());
    }


    @Test
    public void addToDoesNotAddDependencyToProjectWhenConfigurationIsMissing()
    {
        // Given
        Project aProject = ProjectBuilder.builder().build();

        // When
        DependencySpec aSpec = createInstance("does-not-exist");
        boolean aResult = aSpec.addTo(aProject);

        // Then
        assertFalse(aResult);
    }
}
