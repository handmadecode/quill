/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import org.myire.quill.common.ExternalToolLoader;
import org.myire.quill.common.ProjectAware;
import org.myire.quill.common.Projects;
import org.myire.quill.configuration.ConfigurationSpec;
import org.myire.quill.dependency.ModuleDependencySpec;


/**
 * An {@code IvyFileImporter} imports entities from an Ivy file.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class IvyFileImporter extends ProjectAware
{
    // The IvyModuleLoader implementation class to load lazily.
    static private final String IMPLEMENTATION_PACKAGE = "org.myire.quill.ivy.impl";
    static private final String IMPLEMENTATION_CLASS = "IvyModuleLoaderImpl";

    // Cache of IvyFileImporter instances.
    static private final Map<File, IvyFileImporter> cCache = new HashMap<>();


    // The Ivy file to import from.
    private final File fIvyFile;

    // The extension to get the Ivy settings file from.
    private final IvyImportExtension fExtension;

    // Lazily instantiated IvyModuleLoader implementation.
    private IvyModuleLoader fIvyModuleLoader;


    /**
     * Get the {@code IvyFileImporter} for an Ivy file, possibly creating it first.
     *
     * @param pProject  The project to import into.
     * @param pIvyFile  The Ivy file to import from.
     *
     * @return  The {@code IvyFileImporter} for the specified Ivy file.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static IvyFileImporter getInstance(Project pProject, File pIvyFile)
    {
        synchronized(cCache)
        {
            return cCache.computeIfAbsent(pIvyFile, f -> new IvyFileImporter(pProject, f));
        }
    }


    /**
     * Clear the internal cache of {@code IvyFileImporter} instances. The next call to
     * {@link #getInstance(Project, File)} is guaranteed to return a new instance when this method
     * has been called.
     */
    static void clearInstanceCache()
    {
        synchronized(cCache)
        {
            cCache.clear();
        }
    }


    /**
     * Create a new {@code IvyFileImporter}.
     *
     * @param pProject  The project to import into.
     * @param pIvyFile  The Ivy file to import from.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    private IvyFileImporter(Project pProject, File pIvyFile)
    {
        super(pProject);

        fIvyFile = requireNonNull(pIvyFile);
        fExtension = Projects.getExtension(
            pProject,
            IvyImportExtension.EXTENSION_NAME,
            IvyImportExtension.class);
    }


    /**
     * Get the Ivy file this importer operates on.
     *
     * @return  The Ivy file.
     */
    File getIvyFile()
    {
        return fIvyFile;
    }


    /**
     * Import configuration specifications from the Ivy file specified in the constructor.
     *
     * @return  A collection with the imported configuration specifications. The returned collection
     *          may be empty but will never be null.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    Collection<ConfigurationSpec> importConfigurations()
    {
        return maybeCreateIvyLoader().getConfigurations();
    }


    /**
     * Import dependency specifications from the Ivy file specified in the constructor.
     *
     * @return  A collection with the imported dependency specifications. The returned collection
     *          may be empty but will never be null.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    Collection<ModuleDependencySpec> importDependencies()
    {
        return maybeCreateIvyLoader().getDependencies();
    }


    /**
     * Get the organisation from the Ivy file specified in the constructor.
     *
     * @return  The organisation from the Ivy file.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    String getOrganisation()
    {
        return maybeCreateIvyLoader().getOrganisation();
    }


    /**
     * Get the revision from the Ivy file specified in the constructor.
     *
     * @return  The revision from the Ivy file.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    String getRevision()
    {
        return maybeCreateIvyLoader().getRevision();
    }


    /**
     * Create the {@code IvyModuleLoader} if that hasn't be done already. If needed, the
     * {@code IvyModuleLoader} implementation class will be loaded and a new instance of it will
     * be created and initialized with the Ivy file passed to the constructor and settings file
     * specified in the Ivy import extension.
     *
     * @return  The {@code IvyModuleLoader} instance.
     */
    private IvyModuleLoader maybeCreateIvyLoader()
    {
        if (fIvyModuleLoader == null)
        {
            // Load the implementation class and create a new instance of that class.
            fIvyModuleLoader = createIvyModuleLoader();

            // Initialize the Ivy loader with the Ivy file and the extension's settings file, if any.
            fIvyModuleLoader.init(fIvyFile, fExtension != null ? fExtension.getSettingsFile() : null);
        }

        return fIvyModuleLoader;
    }



    /**
     * Create a new {@code IvyModuleLoader} by loading the implementation class and creating a new
     * instance of that class.
     *
     * @return  A new instance of the loaded {@code IvyModuleLoader} implementation, never null.
     */
    private IvyModuleLoader createIvyModuleLoader()
    {
        try
        {
            ExternalToolLoader<IvyModuleLoader> aLoader =
                new ExternalToolLoader<>(
                    IvyModuleLoader.class,
                    IMPLEMENTATION_PACKAGE,
                    IMPLEMENTATION_CLASS,
                    fExtension::getIvyClassPath);

            return aLoader.createToolProxy();
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
        {
            getProjectLogger().error(
                "Could not create an instance of '{}.{}'",
                IMPLEMENTATION_PACKAGE,
                IMPLEMENTATION_CLASS,
                e);

            return EmptyIvyModuleLoader.INSTANCE;
        }
    }


    /**
     * An {@code IvyModuleLoader} that returns empty values.
     */
    static private class EmptyIvyModuleLoader implements IvyModuleLoader
    {
        static final IvyModuleLoader INSTANCE = new EmptyIvyModuleLoader();


        @Override
        public void init(File pIvyModuleFile, File pIvySettingsFile)
        {
            // No-op
        }


        @Override
        public Collection<ModuleDependencySpec> getDependencies()
        {
            return Collections.emptyList();
        }


        @Override
        public Collection<ConfigurationSpec> getConfigurations()
        {
            return Collections.emptyList();
        }


        @Override
        public String getOrganisation()
        {
            return "";
        }


        @Override
        public String getModuleName()
        {
            return "";
        }


        @Override
        public String getRevision()
        {
            return "";
        }
    }
}
