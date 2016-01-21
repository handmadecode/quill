/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy

import java.text.ParseException

import org.apache.ivy.core.module.descriptor.Configuration
import org.apache.ivy.core.module.descriptor.DependencyArtifactDescriptor
import org.apache.ivy.core.module.descriptor.DependencyDescriptor
import org.apache.ivy.core.module.descriptor.ExcludeRule
import org.apache.ivy.core.module.descriptor.ModuleDescriptor
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message

import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyArtifact
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.internal.artifacts.DefaultExcludeRule
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact


/**
 * Loads configurations and dependencies from an Ivy module file and adds them to Gradle
 * configurations and dependencies.
 */
class IvyModuleFileLoader
{
    private final Project fProject
    private final File fIvyModuleFile
    private final File fIvySettingsFile

    private ModuleDescriptor fIvyModuleDescriptor;
    private String fMavenNameSpaceLabel;
    private boolean fLoadAttempted;


    static
    {
        // Only log errors and warnings, by default Ivy logs info messages such as the loaded
        // settings.
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_WARN));
    }


    /**
     * Create a new {@code IvyModuleFileLoader}.
     *
     * @param pProject          The project to load configurations and dependencies into.
     * @param pIvyModuleFile    The Ivy module file to load from.
     * @param pIvySettingsFile  The Ivy settings file, possibly null.
     */
    IvyModuleFileLoader(Project pProject, File pIvyModuleFile, File pIvySettingsFile)
    {
        fProject = pProject;
        fIvyModuleFile = pIvyModuleFile;
        fIvySettingsFile = pIvySettingsFile;
    }


    /**
     * Load configurations from the Ivy module file and add them to the specified container.
     *
     * @param pConfigurations   The container to add the loaded configurations to.
     */
    void loadIvyConfigurations(ConfigurationContainer pConfigurations)
    {
        // Lazily load the module descriptor file.
        if (fIvyModuleDescriptor == null)
            fIvyModuleDescriptor = loadModuleDescriptorFile();

        // Add all Ivy configurations found in the file to the configuration container.
        fIvyModuleDescriptor?.configurations?.each
        {
            addConfiguration(pConfigurations, it);
        }
    }


    /**
     * Load dependencies from the Ivy module file and add them to the specified handler.
     *
     * @param pDependencies The handler to add the loaded dependencies to.
     */
    void loadIvyDependencies(DependencyHandler pDependencies)
    {
        // Lazily load the module descriptor file.
        if (fIvyModuleDescriptor == null)
            fIvyModuleDescriptor = loadModuleDescriptorFile();

        // Get the label used for the maven namespace in the dependencies, if any.
        fMavenNameSpaceLabel = getMavenNameSpaceLabel(fIvyModuleDescriptor);

        // Add all Ivy dependencies to the dependency handler.
        fIvyModuleDescriptor?.dependencies?.each
        {
            addDependency(pDependencies, it);
        }
    }


    private ModuleDescriptor loadModuleDescriptorFile()
    {
        if (fIvyModuleFile == null || fLoadAttempted)
            // The module descriptor file path was explicitly set to null, or the load has already
            // been attempted but failed, do not attempt to load the file.
            return null;

        XmlModuleDescriptorParser aParser = XmlModuleDescriptorParser.getInstance();
        try
        {
            fLoadAttempted = true;
            return aParser.parseDescriptor(createSettings(), fIvyModuleFile.toURI().toURL(), false);
        }
        catch (IOException | ParseException e)
        {
            fProject.logger.error('Could not load the Ivy module descriptor file {} ({})',
                                  fIvyModuleFile,
                                  e.message);
            return null;
        }
    }


    /**
     * Create the Ivy settings, either by loading the settings file specified in the extension or by
     * initializing the settings with default values.
     *
     * @return  The Ivy settings to use.
     */
    private IvySettings createSettings()
    {
        IvySettings aIvySettings = new IvySettings();

        if (fIvySettingsFile != null)
        {
            fProject.logger.debug("Loading Ivy settings file {}", fIvySettingsFile);
            try
            {
                aIvySettings.load(fIvySettingsFile);
                return aIvySettings;
            }
            catch (IOException | ParseException e)
            {
                fProject.logger.warn('Could not load the Ivy settings file {}, using default settings ({})',
                                     fIvySettingsFile,
                                     e.message);
            }
        }

        // No settings file configured or it could not be loaded, use default settings.
        aIvySettings.defaultInit();
        return aIvySettings;
    }


    private void addConfiguration(ConfigurationContainer pConfigurations, Configuration pIvyConfig)
    {
        fProject.logger.debug("Adding Ivy configuration {} to project", pIvyConfig.name);

        // Get the Gradle configuration with the same name as the Ivy configuration, optionally
        // creating it if it doesn't exist.
        org.gradle.api.artifacts.Configuration aGradleConfig =
                getOrCreateConfiguration(pConfigurations, pIvyConfig.name);

        // Copy the inheritance hierarchy from the Ivy config to the Gradle config by ensuring the
        // Gradle configuration has at least the same parents as the Ivy configuration.
        pIvyConfig.extends.each
        {
            pParentConfigName ->
                aGradleConfig.extendsFrom(getOrCreateConfiguration(pConfigurations, pParentConfigName));
        }

        // Copy properties from the Ivy config to the Gradle config.
        aGradleConfig.transitive = pIvyConfig.transitive
        aGradleConfig.visible = (pIvyConfig.visibility == Configuration.Visibility.PUBLIC)
        if (aGradleConfig.description == null)
            aGradleConfig.description = pIvyConfig.description;
    }


    private void addDependency(DependencyHandler pDependencyHandler, DependencyDescriptor pIvyDependency)
    {
        // The dependency notation is the same for all configurations.
        def aDependencyNotation = createDependencyNotation(pIvyDependency);

        // Add the dependency to all of its configurations that are present in the Gradle project.
        pIvyDependency.moduleConfigurations.each
        {
            org.gradle.api.artifacts.Configuration aGradleConfig = getConfiguration(fProject.configurations, it);
            if (aGradleConfig != null)
                addDependency(pDependencyHandler, pIvyDependency, aDependencyNotation, aGradleConfig);
            else
                fProject.logger.warn('Configuration {} does not exist, cannot add dependency {} to it',
                                     it,
                                     aDependencyNotation);
        }
    }


    private void addDependency(DependencyHandler pDependencyHandler,
                               DependencyDescriptor pIvyDependency,
                               Object pDependencyNotation,
                               org.gradle.api.artifacts.Configuration pGradleConfig)
    {
        fProject.logger.debug('Adding Ivy dependency {} to configuration {}',
                              pDependencyNotation,
                              pGradleConfig.name);

        Dependency aGradleDependency = pDependencyHandler.add(pGradleConfig.name, pDependencyNotation);

        // Ivy dependency transitivity, artifacts, and exclude rules are mappable to a Gradle
        // ModuleDependency
        if (aGradleDependency instanceof ModuleDependency)
        {
            ModuleDependency aModuleDependency = (ModuleDependency) aGradleDependency;
            aModuleDependency.transitive = pIvyDependency.transitive;

            pIvyDependency.allDependencyArtifacts.each {
                aModuleDependency.addArtifact(createDependencyArtifact(it));
            }

            pIvyDependency.allExcludeRules?.each {
                addExcludeRule(aModuleDependency, it);
            }
        }

        // Ivy dependency changing property is mappable to a Gradle ExternalModuleDependency
        if (aGradleDependency instanceof ExternalModuleDependency)
            aGradleDependency.changing = pIvyDependency.changing;

        // Add the dependency to the configuration.
        pGradleConfig.dependencies.add(aGradleDependency);
    }


    /**
     * Get the label used for the Maven name space in an Ivy module descriptor, if any.
     *<p>
     * Example: given the module descriptor
     * <pre>&lt;ivy-module version="2.0" xmlns:m="http://ant.apache.org/ivy/maven"&gt;</pre>, the
     * label "m" would be returned.
     *
     * @param pModuleDescriptor The module descriptor to get the Maven name space label from,
     *                          possibly null.
     *
     * @return  The label used for the Maven name space, or null if the module descriptor does not
     *          define a Maven name space.
     */
    static private String getMavenNameSpaceLabel(ModuleDescriptor pModuleDescriptor)
    {
        for (aEntry in pModuleDescriptor?.extraAttributesNamespaces)
            if (aEntry.value.toString().endsWith('/ivy/maven'))
                return aEntry.key;

        return null;
    }

    /**
     * Create a Gradle dependency notation from an Ivy dependency descriptor.
     *
     * @param pIvyDependency    The Ivy dependency descriptor.
     *
     * @return  A Gradle dependency notation.
     */
    private Object createDependencyNotation(DependencyDescriptor pIvyDependency)
    {
        def aDependencyNotation = [
                group: pIvyDependency.dependencyRevisionId.organisation,
                name : pIvyDependency.dependencyRevisionId.name,
                version: pIvyDependency.dependencyRevisionId.revision
        ]

        if (fMavenNameSpaceLabel != null)
        {
            String aClassifier = pIvyDependency.qualifiedExtraAttributes.get(fMavenNameSpaceLabel + ':classifier');
            if (aClassifier != null)
                aDependencyNotation['classifier'] = aClassifier;
        }

        return aDependencyNotation;
    }


    /**
     * Create a Gradle dependency artifact from an Ivy dependency artifact descriptor.
     *
     * @param pDescriptor   The Ivy dependency artifact descriptor.
     *
     * @return  A Gradle dependency artifact.
     */
    static private DependencyArtifact createDependencyArtifact(DependencyArtifactDescriptor pDescriptor)
    {
        return new DefaultDependencyArtifact(pDescriptor.name,
                                             pDescriptor.type,
                                             pDescriptor.ext,
                                             null,
                                             pDescriptor.url?.toString());
    }

    /**
     * Create a Gradle exclude rule from an Ivy exclude rule and add it to a Gradle module
     * dependency.
     *
     * @param pDependency   The Gradle module dependency.
     * @param pRule         The Ivy exclude rule.
     */
    static private void addExcludeRule(ModuleDependency pDependency, ExcludeRule pRule)
    {
        def aAttributes = [:];
        pRule.attributes.each
        {
                // Ivy rule attribute key 'organisation' maps to Gradle key 'group'.
            k, v ->
                if (k == 'organisation')
                    aAttributes['group'] = v;
                else if (k == 'module')
                    aAttributes['module'] = v;
        }

        if (aAttributes.size() > 0)
            pDependency.excludeRules.add(new DefaultExcludeRule(aAttributes));
    }


    /**
     * Get a Gradle configuration from a configuration container, optionally creating it if it
     * doesn't exist.
     *
     * @param pConfigurations   The configuration container.
     * @param pConfigName       The name of the configuration.
     *
     * @return  The Gradle configuration with the specified name.
     */
    static private org.gradle.api.artifacts.Configuration getOrCreateConfiguration(ConfigurationContainer pConfigurations,
                                                                                   String pConfigName)
    {
        org.gradle.api.artifacts.Configuration aConfig = getConfiguration(pConfigurations, pConfigName)
        if (aConfig == null)
            aConfig = pConfigurations.create(pConfigName)

        return aConfig
    }


    /**
     * Get a Gradle configuration from a configuration container.
     *
     * @param pConfigurations   The configuration container.
     * @param pConfigName       The name of the configuration.
     *
     * @return  The Gradle configuration with the specified name, or null if it doesn't exist.
     */
    static private org.gradle.api.artifacts.Configuration getConfiguration(ConfigurationContainer pConfigurations,
                                                                           String pConfigName)
    {
        try
        {
            return pConfigurations.getByName(pConfigName)
        }
        catch (UnknownConfigurationException ignore)
        {
            return null
        }
    }
}
