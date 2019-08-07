/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.configuration;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests for {@code org.myire.quill.configuration.Configurations}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ConfigurationsTest
{
    /**
     * {@code maybeCreateConfiguration()} should create a configuration if it doesn't exist in the
     * project.
     */
    @Test
    public void maybeCreateConfigurationCreatesNonExistingConfiguration()
    {
        // Given
        Project aProject = ProjectBuilder.builder().build();
        String aName = "CfgXY";
        String aDescription = "Some config";
        ConfigurationSpec aSpec = new ConfigurationSpec(aName);
        aSpec.setVisible(false);
        aSpec.setDescription(aDescription);

        // When
        boolean aResult = Configurations.maybeCreateConfiguration(aProject, aSpec);

        // Then
        assertTrue(aResult);

        // Then
        Configuration aConfiguration = aProject.getConfigurations().getByName(aName);
        assertEquals(aName, aConfiguration.getName());
        assertEquals(aDescription, aConfiguration.getDescription());
        assertTrue(aConfiguration.isTransitive());
        assertFalse(aConfiguration.isVisible());
    }


    /**
     * {@code maybeCreateConfiguration()} should update a configuration if it already exist in the
     * project.
     */
    @Test
    public void maybeCreateConfigurationUpdatesExistingConfiguration()
    {
        // Given
        Project aProject = ProjectBuilder.builder().build();
        String aBaseName = "baseConfig";
        String aExtendedName = "alreadyExists";
        Configuration aBaseConfiguration = aProject.getConfigurations().create(aBaseName);
        aProject.getConfigurations().create(aExtendedName);

        ConfigurationSpec aSpec = new ConfigurationSpec(aExtendedName);
        aSpec.addExtendedConfiguration(aBaseName);

        // When
        boolean aResult = Configurations.maybeCreateConfiguration(aProject, aSpec);

        // Then
        assertFalse(aResult);

        // Then
        Configuration aUpdatedConfiguration = aProject.getConfigurations().getByName(aExtendedName);
        assertEquals(aExtendedName, aUpdatedConfiguration.getName());
        assertTrue(aUpdatedConfiguration.getExtendsFrom().contains(aBaseConfiguration));
    }
}
