/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.configuration;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;


/**
 * Utility methods for Gradle configurations.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public final class Configurations
{
    /**
     * Private constructor to disallow instantiations of utility method class.
     */
    private Configurations()
    {
        // Empty default ctor, defined to override access scope.
    }


    /**
     * Add or update a configuration to/in a project.
     *
     * @param pProject              The project.
     * @param pConfigurationSpec    The specification of the configuration to add or update.
     *
     * @return  True if the configuration was added, false if a configuration with that name already
     *          existed in the project. In the latter case, the existing configuration's properties
     *          will have been updated with the values from {@code pConfigurationSpec}.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static public boolean maybeCreateConfiguration(Project pProject, ConfigurationSpec pConfigurationSpec)
    {
        String aName = pConfigurationSpec.getName();
        ConfigurationContainer aConfigurations = pProject.getConfigurations();

        Configuration aConfiguration = aConfigurations.findByName(aName);
        boolean aDidExist = aConfiguration != null;
        if (!aDidExist)
            // Configuration doesn't exist, must create it.
            aConfiguration = aConfigurations.create(aName);

        // Set the plain properties.
        aConfiguration.setDescription(pConfigurationSpec.getDescription());
        aConfiguration.setTransitive(pConfigurationSpec.isTransitive());
        aConfiguration.setVisible(pConfigurationSpec.isVisible());

        // Add any configuration that the new/updated configuration should extend from, possibly
        // creating the parent configuration first. The seemingly unnecessary extra local variable
        // is needed to make it effectively final and thus usable in a lambda.
        Configuration aExtendingConfig = aConfiguration;
        pConfigurationSpec.forEachExtendedConfiguration(
            e -> aExtendingConfig.extendsFrom(aConfigurations.maybeCreate(e)));

        return !aDidExist;
    }
}
