/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logging

import org.myire.quill.common.ClassLoaders
import org.myire.quill.common.ProjectAware
import org.myire.quill.common.Projects
import org.myire.quill.dependency.Dependencies
import org.myire.quill.dependency.DependencySpec
import org.myire.quill.repository.MavenRepositorySpec


/**
 * A {@code PomImporter} imports entities from a Maven effective pom.
 */
class PomImporter extends ProjectAware
{
    // The EffectivePomLoader implementation class to load lazily.
    static private final String IMPLEMENTATION_CLASS = 'org.myire.quill.maven.EffectivePomLoaderImpl';


    // The pom file to import from.
    private final File fPomFile;

    // Closure that returns the class path with which the EffectivePomLoader implementation class
    // should be loaded.
    private final Closure<FileCollection> fClassPathSource;

    // The extension to get the Maven settings file and scope mappings from.
    private final MavenImportExtension fExtension;

    // Lazily instantiated, implementation class lazily loaded.
    private EffectivePomLoader fPomLoader;


    /**
     * Create a new {@code MavenImporter}.
     *
     * @param pProject          The project to import to.
     * @param pPomFile          The pom file to import from.
     * @param pClassPathSource  A closure that will return the class path needed to load the
     *                          {@code EffectivePomLoader} implementation.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    PomImporter(Project pProject, File pPomFile, Closure<FileCollection> pClassPathSource)
    {
        super(pProject);

        fPomFile = Objects.requireNonNull(pPomFile);
        fClassPathSource = Objects.requireNonNull(pClassPathSource);

        fExtension = Projects.getExtension(
                pProject,
                MavenImportPlugin.EXTENSION_NAME,
                MavenImportExtension.class);
    }


    /**
     * Get the pom file this importer operates on.
     *
     * @return  The pom file.
     */
    File getPomFile()
    {
        return fPomFile;
    }


    /**
     * Import dependencies from the pom file specified in the constructor.
     */
    Collection<DependencySpec> importDependencies()
    {
        // Load the dependencies.
        Collection<DependencySpec> aDependencies = maybeCreatePomLoader().getDependencies();

        // Convert and return the dependencies for which there is a scope mapping.
        return aDependencies.findAll { convertDependency(it) }
    }


    /**
     * Import repositories from the pom file specified in the constructor.
     */
    Collection<MavenRepositorySpec> importRepositories()
    {
        return maybeCreatePomLoader().getRepositories();
    }


    /**
     * Get the group ID from the pom file specified in the constructor.
     *
     * @return  The group ID from the pom file.
     */
    String getGroupId()
    {
        return maybeCreatePomLoader().groupId;
    }


    /**
     * Get the version string from the pom file specified in the constructor.
     *
     * @return  The version string from the pom file.
     */
    String getVersionString()
    {
        return maybeCreatePomLoader().version;
    }


    /**
     * Create the {@code EffectivePomLoader} if that hasn't be done already. If needed, the
     * {@code EffectivePomLoader} implementation class will be loaded and a new instance of it will
     * be created and initialized with the pom file passed to the constructor and settings file
     * specified in the Maven import extension.
     *
     * @return  The {@code EffectivePomLoader} instance.
     */
    private EffectivePomLoader maybeCreatePomLoader()
    {
        if (fPomLoader != null)
            // Already created.
            return fPomLoader;

        // Inject the class path from the source provided in the constructor into this class' class
        // loader.
        ClassLoader aClassLoader = getClass().getClassLoader();
        ClassLoaders.inject(aClassLoader, fClassPathSource.call());

        // Load the implementation class and create a new instance of that class.
        fPomLoader = (EffectivePomLoader) aClassLoader.loadClass(IMPLEMENTATION_CLASS).newInstance();

        // Initialize the pom loader with the pom file and the extension's settings file, if any.
        fPomLoader.init(fPomFile, fExtension?.settingsFile);
        return fPomLoader;
    }


    /**
     * Convert a Maven dependency by mapping its scope to a configuration name and by setting the
     * project properties if the dependency is a project dependency.
     *
     * @param pDependency The dependency.
     *
     * @return  True if the dependency's scope could be mapped to a configuration, false if not.
     */
    private boolean convertDependency(DependencySpec pDependency)
    {
        String aConfiguration = scopeToConfiguration(pDependency.configuration);
        if (aConfiguration != null)
        {
            // Scope mapped, replace it with the configuration name.
            pDependency.configuration = aConfiguration;

            // Set the project related properties if the dependency is a project dependency.
            Project aProject = Dependencies.findProjectForDependency(project, pDependency);
            if (aProject)
                pDependency.setProject(aProject);

            return true;
        }
        else
        {
            // No scope mapping.
            Logging.getLogger(PomImporter.class).info(
                    'Scope {} has no mapping, skipping dependency {}',
                    pDependency.configuration,
                    pDependency.asStringNotation());

            return false;
        }
    }


    /**
     * Map a Maven scope to a Gradle configuration name using the mapping in the project extension.
     *
     * @param pScope    The scope name.
     *
     * @return  The configuration name for the scope, or null if the scope is mapped to null.
     */
    private String scopeToConfiguration(String pScope)
    {
        Map<String, String> aMapping = fExtension?.scopeToConfiguration;
        if (aMapping == null)
            // No mapping, scopes are mapped to themselves.
            return pScope;

        String aConfiguration = aMapping[pScope];
        if (aConfiguration != null)
            // Scope mapped to configuration name.
            return aConfiguration;

        if (aMapping.containsKey(pScope))
            // Scope mapped explicitly to null.
            return null;
        else
            // No explicit mapping for scope, default is to map it to itself.
            return pScope;
    }
}
