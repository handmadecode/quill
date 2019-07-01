/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import static java.util.Objects.requireNonNull;

import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import org.myire.quill.configuration.ConfigurationSpec;
import org.myire.quill.dependency.ModuleDependencySpec;
import org.myire.quill.ivy.IvyModuleLoader;


/**
 * Implementation of {@code IvyModuleLoader} based on the Ivy libraries. This class should not be
 * loaded until the Ivy libraries are available on the class path.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class IvyModuleLoaderImpl implements IvyModuleLoader
{
    // Ivy module and settings file, the former initialized to its default value.
    private File fIvyModuleFile = new File("ivy.xml");
    private File fIvySettingsFile;

    // The Ivy module descriptor, lazily loaded.
    private ModuleDescriptor fIvyModuleDescriptor;

    // The name of the Ivy extra attribute that holds the dependency classifier, if any.
    private String fClassifierExtraAttribute;

    private final Logger fLogger = Logging.getLogger(IvyModuleLoaderImpl.class);


    static
    {
        // Only log errors and warnings, by default Ivy logs info messages such as the loaded
        // settings.
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_WARN));
    }


    @Override
    public void init(File pIvyModuleFile, File pIvySettingsFile)
    {
        fIvyModuleFile = requireNonNull(pIvyModuleFile);
        fIvySettingsFile = pIvySettingsFile;
    }


    @Override
    public Collection<ModuleDependencySpec> getDependencies()
    {
        ModuleDescriptor aModuleDescriptor = maybeLoadModuleDescriptor();

        // Convert each Ivy dependency to a DependencySpec.
        Collection<ModuleDependencySpec> aDependencies = new ArrayList<>();
        for (DependencyDescriptor aIvyDependency : aModuleDescriptor.getDependencies())
        {
            // Create one DependencySpec for each configuration.
            for (String aConfiguration : aIvyDependency.getModuleConfigurations())
            {
                ModuleDependencySpec aDependencySpec =
                    toDependencySpec(aIvyDependency, aConfiguration, fClassifierExtraAttribute);

                // Add any dependency artifacts to the dependency spec.
                for (DependencyArtifactDescriptor aIvyArtifact : aIvyDependency.getAllDependencyArtifacts())
                    addDependencyArtifact(aDependencySpec, aIvyArtifact);

                // Add any exclusions to the dependency spec.
                for (ExcludeRule aExcludeRule : aIvyDependency.getAllExcludeRules())
                    addExclusion(aDependencySpec, aExcludeRule);

                aDependencies.add(aDependencySpec);
            }
        }

        return aDependencies;
    }


    @Override
    public Collection<ConfigurationSpec> getConfigurations()
    {
        ModuleDescriptor aModuleDescriptor = maybeLoadModuleDescriptor();

        // Convert each Ivy configuration to a ConfigurationSpec.
        Collection<ConfigurationSpec> aConfigurations = new ArrayList<>();
        for (Configuration aIvyConfiguration : aModuleDescriptor.getConfigurations())
        {
            ConfigurationSpec aConfigurationSpec = new ConfigurationSpec(aIvyConfiguration.getName());
            aConfigurationSpec.setTransitive(aIvyConfiguration.isTransitive());
            aConfigurationSpec.setVisible((aIvyConfiguration.getVisibility() == Configuration.Visibility.PUBLIC));
            aConfigurationSpec.setDescription(aIvyConfiguration.getDescription());
            for (String aParent : aIvyConfiguration.getExtends())
                aConfigurationSpec.addExtendedConfiguration(aParent);

            aConfigurations.add(aConfigurationSpec);
        }

        return aConfigurations;
    }


    @Override
    public String getOrganisation()
    {
        return maybeLoadModuleDescriptor().getModuleRevisionId().getOrganisation();
    }


    @Override
    public String getModuleName()
    {
        return maybeLoadModuleDescriptor().getModuleRevisionId().getName();
    }


    @Override
    public String getRevision()
    {
        return maybeLoadModuleDescriptor().getModuleRevisionId().getRevision();
    }


    /**
     * Get the {@code ModuleDescriptor} loaded from the Ivy file. If the file hasn't been loaded
     * before it will be loaded.
     *
     * @return  The {@code ModuleDescriptor} loaded from the Ivy file.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    private ModuleDescriptor maybeLoadModuleDescriptor()
    {
        if (fIvyModuleDescriptor != null)
            return fIvyModuleDescriptor;

        try
        {
            fLogger.debug("Loading Ivy module file {}", fIvyModuleFile);
            XmlModuleDescriptorParser aParser = XmlModuleDescriptorParser.getInstance();
            fIvyModuleDescriptor =
                aParser.parseDescriptor(createSettings(), fIvyModuleFile.toURI().toURL(), false);

            // The classifier for a dependency can be specified as an extra attribute prefixed with
            // the maven namespace label.
            String aMavenNameSpaceLabel = getMavenNameSpaceLabel(fIvyModuleDescriptor);
            if (aMavenNameSpaceLabel != null)
                fClassifierExtraAttribute = aMavenNameSpaceLabel + ":classifier";

            return fIvyModuleDescriptor;
        }
        catch (IOException | ParseException e)
        {
            throw new GradleException("Could not load the Ivy module file " + fIvyModuleFile, e);
        }
    }


    /**
     * Create the Ivy settings, either by loading the settings file specified in the call to
     * {@link #init(File, File)} or by initializing the settings with default values.
     *
     * @return  The Ivy settings to use, never null.
     */
    private IvySettings createSettings()
    {
        IvySettings aIvySettings = new IvySettings();

        if (!loadSettingsFile(aIvySettings))
            // No settings file configured or it could not be loaded, use default settings.
            initWithDefaults(aIvySettings);

        return aIvySettings;
    }


    /**
     * Load the Ivy settings file specified in {@link #init(File, File)}, if any.
     *
     * @param pIvySettings  The Ivy settings to load the settings file into.
     *
     * @return  True if the file was loaded into the specified settings, false if no settings file
     *          has been specified or if loading the file fails.
     *
     * @throws NullPointerException if {@code pIvySettings} is null.
     */
    private boolean loadSettingsFile(IvySettings pIvySettings)
    {
        if (fIvySettingsFile == null)
            return false;

        try
        {
            fLogger.debug("Loading Ivy settings file {}", fIvySettingsFile);
            pIvySettings.load(fIvySettingsFile);
            return true;
        }
        catch (IOException | ParseException e)
        {
            fLogger.warn(
                "Could not load the Ivy settings file {} ({})",
                fIvySettingsFile,
                e.getMessage());

            return false;
        }
    }


    /**
     * Initialize an {@code IvySettings} instance with default values.
     *
     * @param pIvySettings  The Ivy settings to initialize with default values.
     *
     * @throws NullPointerException if {@code pIvySettings} is null.
     */
    private void initWithDefaults(IvySettings pIvySettings)
    {
        try
        {
            fLogger.debug("Initializing Ivy settings with default values");
            pIvySettings.defaultInit();
        }
        catch (IOException e)
        {
            fLogger.warn(
                "Could not create default Ivy settings, will try to use empty settings ({})",
                e.getMessage());
        }
    }


    /**
     * Get the label used for the Maven name space in an Ivy module descriptor, if any.
     *<p>
     * Example: given the module descriptor
     * {@code <ivy-module version="2.0" xmlns:m="http://ant.apache.org/ivy/maven">}, the label
     * &quot;m&quot; would be returned.
     *
     * @param pModuleDescriptor The module descriptor to get the Maven name space label from.
     *
     * @return  The label used for the Maven name space, or null if the module descriptor does not
     *          define a Maven name space.
     *
     * @throws NullPointerException if {@code pModuleDescriptor} is null.
     */
    static private String getMavenNameSpaceLabel(ModuleDescriptor pModuleDescriptor)
    {
        for (Map.Entry<String, String> aEntry : pModuleDescriptor.getExtraAttributesNamespaces().entrySet())
            if (aEntry.getValue().endsWith("/ivy/maven"))
                return aEntry.getKey();

        return null;
    }


    /**
     * Create a {@code DependencySpec} from the values in an Ivy {@code DependencyDescriptor}.
     *
     * @param pIvyDependency    The Ivy dependency to get the values from.
     * @param pConfiguration    The dependency spec's configuration name.
     * @param pClassifierExtraAttribute
     *                          If non-null, the Ivy dependency's extra attribute that holds the
     *                          dependency spec's <i>classifier</i> value.
     *
     * @return  A new {@code ModuleDependencySpec}.
     *
     * @throws NullPointerException if {@code pIvyDependency} is null.
     */
    static private ModuleDependencySpec toDependencySpec(
        DependencyDescriptor pIvyDependency,
        String pConfiguration,
        String pClassifierExtraAttribute)
    {
        ModuleDependencySpec aDependencySpec = new ModuleDependencySpec(
            pConfiguration,
            pIvyDependency.getDependencyRevisionId().getOrganisation(),
            pIvyDependency.getDependencyRevisionId().getName(),
            pIvyDependency.getDependencyRevisionId().getRevision());

        aDependencySpec.setTransitive(pIvyDependency.isTransitive());
        aDependencySpec.setChanging(pIvyDependency.isChanging());
        aDependencySpec.setForce(pIvyDependency.isForce());

        if (pClassifierExtraAttribute != null)
        {
            Object aClassifier = pIvyDependency.getQualifiedExtraAttributes().get(pClassifierExtraAttribute);
            if (aClassifier != null)
                aDependencySpec.setClassifier(aClassifier.toString());
        }

        return aDependencySpec;
    }


    /**
     * Add the values from an Ivy {@code DependencyArtifactDescriptor} to a
     * {@code ModuleDependencySpec}.
     *
     * @param pDependencySpec   The dependency spec to add the artifact specification to.
     * @param pIvyArtifact      The Ivy artifact to get the values from.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static private void addDependencyArtifact(
        ModuleDependencySpec pDependencySpec,
        DependencyArtifactDescriptor pIvyArtifact)
    {
        URL aURL = pIvyArtifact.getUrl();
        pDependencySpec.addArtifact(
            pIvyArtifact.getName(),
            pIvyArtifact.getType(),
            pIvyArtifact.getExt(),
            null,
            aURL != null ? aURL.toString() : null);
    }


    /**
     * Add the values from an Ivy {@code ExcludeRule} to a {@code ModuleDependencySpec}.
     *
     * @param pDependencySpec   The dependency spec to add the exclusion to.
     * @param pExcludeRule      The Ivy exclude rule to get the values from.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static private void addExclusion(ModuleDependencySpec pDependencySpec, ExcludeRule pExcludeRule)
    {
        pDependencySpec.addExclusion(
            pExcludeRule.getAttribute("organisation"),
            pExcludeRule.getAttribute("module"));
    }
}
