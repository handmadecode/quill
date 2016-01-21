/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyHandler


/**
 * A plugin that extends its project with functionality to load configurations and dependencies from
 * an Ivy module file.
 */
class IvyPlugin implements Plugin<Project>
{
    static private final String EXTENSION_NAME = 'ivyModule';


    private Project fProject;
    private IvyModuleExtension fExtension;
    private IvyModuleFileLoader fIvyLoader;


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Add the extension that allows configuring the location of the Ivy files.
        fExtension = pProject.extensions.create(EXTENSION_NAME, IvyModuleExtension.class, pProject);

        // Add a dynamic method to the configuration container that allows build scripts to specify
        // that additional configurations should be loaded from the Ivy module descriptor file.
        pProject.configurations.metaClass.fromIvyModule
        {
            loadIvyConfigurations((ConfigurationContainer) delegate);
        }

        // Add a dynamic method to the dependency handler that allows build scripts to specify that
        // additional dependencies should be loaded from the Ivy module descriptor file.
        pProject.dependencies.metaClass.fromIvyModule
        {
            loadIvyDependencies((DependencyHandler) delegate);
        }
    }


    private void loadIvyConfigurations(ConfigurationContainer pConfigurations)
    {
        getIvyLoader().loadIvyConfigurations(pConfigurations);
    }


    private void loadIvyDependencies(DependencyHandler pDependencies)
    {
        getIvyLoader().loadIvyDependencies(pDependencies);
    }


    private IvyModuleFileLoader getIvyLoader()
    {
        if (fIvyLoader == null)
            fIvyLoader = new IvyModuleFileLoader(fProject, fExtension.from, fExtension.settings);

        return fIvyLoader;
    }
}
