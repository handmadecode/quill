/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import org.myire.quill.configuration.ConfigurationSpec
import org.myire.quill.configuration.Configurations
import org.myire.quill.dependency.ModuleDependencySpec


/**
 * Gradle plugin for importing from an Ivy file. It provides the following functionality:
 *<ul>
 * <li>Configurations can be added to a project through the dynamic method {@code fromIvyFile} that
 *     is added to the project's {@code ConfigurationContainer}.</li>
 * <li>Dependencies can be added to a project through the dynamic method {@code fromIvyFile} that is
 *     added to the project's {@code DependencyHandler}.</li>
 * <li>A project's group ID and version can be set through the dynamic methods
 *     {@code applyGroupFromIvyFile} and {@code applyVersionFromIvyFile} that are added to the
 *     project.</li>
 * <li>Configurations and dependencies can be written to a file on Gradle notation with the
 *     {@code IvyFileConvertTask} task added to the project.</li>
 *</ul>
 * The plugin also creates a configuration that specifies the classpath to use when running the
 * Ivy import.
 */
class IvyImportPlugin implements Plugin<Project>
{
    static private final Logger cLogger = Logging.getLogger(IvyImportPlugin.class)

    static final String CONFIGURATION_NAME = 'ivyImport'

    // The default artifact the import classpath should depend on.
    static private final String IVY_GROUP_ARTIFACT_ID = 'org.apache.ivy:ivy'


    private Project fProject
    private Configuration fConfiguration
    private IvyImportExtension fExtension
    private IvyFileConvertTask fTask


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Clear the cache of IvyFileImporter instances; it may still be populated from a previous
        // run with a Gradle daemon that keeps the IvyFileImporter class loaded.
        IvyFileImporter.clearInstanceCache();

        // Create the Ivy import configuration and add it to the project. The default classpath for
        // the Ivy import is specified through this configuration's dependencies.
        fConfiguration = createConfiguration();

        // Add the extension that allows configuring the location of the Ivy files and specifying
        // the Ivy libraries to use.
        fExtension = pProject.extensions.create(
                IvyImportExtension.EXTENSION_NAME,
                IvyImportExtension.class,
                pProject,
                fConfiguration);

        // Create the task.
        fTask = createTask();

        // Add a dynamic method to the configuration container that allows build scripts to specify
        // that additional configurations should be loaded from an Ivy file.
        pProject.configurations.metaClass.fromIvyFile
        {
            Object pIvyFile -> importIvyConfigurations(pIvyFile);
        }

        // Add a dynamic method to the dependency handler that allows build scripts to specify that
        // additional dependencies should be loaded from any Ivy file.
        pProject.dependencies.metaClass.fromIvyFile
        {
            Object pIvyFile -> importIvyDependencies(pIvyFile);
        }

        // Add a dynamic method to the project that sets the project's group from an Ivy file's
        // module organisation.
        pProject.metaClass.setGroupFromIvyFile
        {
            Object pIvyFile -> setGroupFromIvyOrganisation(pIvyFile);
        }

        // Add a dynamic method to the project that sets the project's version string from an Ivy
        // file's module revision.
        pProject.metaClass.setVersionFromIvyFile
        {
            Object pIvyFile -> setVersionFromIvyRevision(pIvyFile);
        }
    }


    /**
     * Import configurations from an Ivy file and add them to the project's configurations.
     *
     * @param pIvyFile  The Ivy file to import configurations from. If null, a file called
     *                  &quot;ivy.xml&quot; in the project directory will be used.
     */
    void importIvyConfigurations(Object pIvyFile)
    {
        File aIvyFile = asIvyFile(pIvyFile);
        cLogger.debug('Importing Ivy configurations from \'{}\' to project {}',
                      aIvyFile.absolutePath,
                      fProject.name);

        Collection<ConfigurationSpec> aConfigurations = getIvyFileImporter(aIvyFile).importConfigurations();
        for (aConfiguration in aConfigurations)
        {
            cLogger.debug('Adding configuration {}', aConfiguration.name);
            Configurations.maybeCreateConfiguration(fProject, aConfiguration);
        }
    }


    /**
     * Import dependencies from an Ivy file and add them to the project's dependencies.
     *
     * @param pIvyFile  The Ivy file to import dependencies from. If null, a file called
     *                  &quot;ivy.xml&quot; in the project directory will be used.
     */
    void importIvyDependencies(Object pIvyFile)
    {
        File aIvyFile = asIvyFile(pIvyFile);
        cLogger.debug('Importing Ivy dependencies from \'{}\' to project {}',
                      aIvyFile.absolutePath,
                      fProject.name);

        Collection<ModuleDependencySpec> aDependencies = getIvyFileImporter(aIvyFile).importDependencies();
        for (aDependency in aDependencies)
        {
            cLogger.debug('Adding dependency {}', aDependency.toDependencyNotation());
            aDependency.addTo(fProject);
        }
    }


    /**
     * Set the project's {@code group} property to the organisation specified in an Ivy file.
     *
     * @param pIvyFile  The Ivy file to get the organisation from. If null, a file called
     *                  &quot;ivy.xml&quot; in the project directory will be used.
     */
    void setGroupFromIvyOrganisation(Object pIvyFile)
    {
        File aIvyFile = asIvyFile(pIvyFile);
        cLogger.debug('Setting group for project {} from \'{}\'',
                      fProject.name,
                      aIvyFile.absolutePath);
        fProject.setGroup(getIvyFileImporter(aIvyFile).organisation);
    }


    /**
     * Set the project's {@code version} property to the revision string specified in an Ivy file.
     *
     * @param pIvyFile  The Ivy file to get the revision string from. If null, a file called
     *                  &quot;ivy.xml&quot; in the project directory will be used.
     */
    void setVersionFromIvyRevision(Object pIvyFile)
    {
        File aIvyFile = asIvyFile(pIvyFile);
        cLogger.debug('Setting version for project {} from \'{}\'',
                      fProject.name,
                      aIvyFile.absolutePath);
        fProject.setVersion(getIvyFileImporter(aIvyFile).revision);
    }


    /**
     * Create the Ivy import configuration if not already present in the project and define it to
     * depend on the default artifact unless explicit dependencies have been defined.
     *
     * @return  The Ivy import configuration.
     */
    private Configuration createConfiguration()
    {
        Configuration aConfiguration = fProject.configurations.maybeCreate(CONFIGURATION_NAME);

        aConfiguration.with {
            visible = false;
            transitive = true;
            description = 'The default classes used by the Ivy module import';
        }

        // Add an action that adds the default dependencies to the configuration if it is empty at
        // resolve time.
        aConfiguration.incoming.beforeResolve { setupDependencies() }

        return aConfiguration;
    }


    /**
     * Create a new {@code IvyFileConvertTask}.
     *
     * @return  The created {@code IvyFileConvertTask} instance.
     */
    private IvyFileConvertTask createTask()
    {
        IvyFileConvertTask aTask = fProject.tasks.create(IvyFileConvertTask.TASK_NAME, IvyFileConvertTask.class);
        aTask.init(fExtension);
        return aTask;
    }


    /**
     * Get the {@code IvyFileImporter} for an Ivy file.
     *
     * @param pIvyFile  The Ivy file.
     *
     * @return  The {@code IvyFileImporter} for the specified file.
     */
    private IvyFileImporter getIvyFileImporter(File pIvyFile)
    {
        return IvyFileImporter.getInstance(fProject, pIvyFile);
    }


    /**
     * Add the default dependencies to the configuration if it is empty.
     */
    private void setupDependencies()
    {
        if (fConfiguration.dependencies.empty)
            addDependency("${IVY_GROUP_ARTIFACT_ID}:${fExtension.ivyVersion}");
    }


    /**
     * Add a dependency to the {@code ivyImport} configuration.
     *
     * @param pDependencyNotation   The dependency notation.
     */
    private void addDependency(String pDependencyNotation)
    {
        fConfiguration.dependencies.add(fProject.dependencies.create(pDependencyNotation));
    }


    /**
     * Resolve an object to an Ivy file specification relative to the project directory. If the
     * object is null, a file called &quot;ivy.xml&quot; in the project directory is returned.
     *
     * @param pValue    The object to resolve.
     *
     * @return  The resolved object.
     */
    private File asIvyFile(Object pValue)
    {
        return fProject.file(pValue ?: 'ivy.xml');
    }
}
