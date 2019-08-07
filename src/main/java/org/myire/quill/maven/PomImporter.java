/*
 * Copyright 2017-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import org.myire.quill.common.ExternalToolLoader;
import org.myire.quill.common.ProjectAware;
import org.myire.quill.common.Projects;
import org.myire.quill.dependency.Dependencies;
import org.myire.quill.dependency.DependencySpec;
import org.myire.quill.dependency.ModuleDependencySpec;
import org.myire.quill.dependency.ProjectDependencySpec;
import org.myire.quill.repository.MavenRepositorySpec;
import org.myire.quill.repository.RepositorySpec;


/**
 * A {@code PomImporter} imports entities from a Maven effective pom.
 */
class PomImporter extends ProjectAware
{
    // The EffectivePomLoader implementation class to load lazily.
    static private final String IMPLEMENTATION_PACKAGE = "org.myire.quill.maven.impl.";
    static private final String IMPLEMENTATION_CLASS = "EffectivePomLoaderImpl";

    // Cache of PomImporter instances.
    static private final Map<File, PomImporter> cCache = new HashMap<>();


    // The pom file to import from.
    private final File fPomFile;

    // The extension to get the Maven class path, settings file and scope mappings from.
    private final MavenImportExtension fExtension;

    // Lazily instantiated EffectivePomLoader implementation class.
    private EffectivePomLoader fPomLoader;


    /**
     * Get the {@code PomImporter} for a pom file, possibly creating it first.
     *
     * @param pProject  The project to import into.
     * @param pPomFile  The pom file to import from.
     *
     * @return  The {@code PomImporter} for the specified pom file.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static PomImporter getInstance(Project pProject, File pPomFile)
    {
        synchronized(cCache)
        {
            return cCache.computeIfAbsent(pPomFile, f -> new PomImporter(pProject, f));
        }
    }


    /**
     * Clear the internal cache of {@code PomImporter} instances. The next call to
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
     * Create a new {@code PomImporter}.
     *
     * @param pProject  The project to import into.
     * @param pPomFile  The pom file to import from.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    private PomImporter(Project pProject, File pPomFile)
    {
        super(pProject);

        fPomFile = Objects.requireNonNull(pPomFile);
        fExtension = Projects.getExtension(
            pProject,
            MavenImportExtension.EXTENSION_NAME,
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
     * Import dependency specifications from the pom file specified in the constructor.
     *
     * @return  A collection with the imported dependency specifications. The returned collection
     *          may be empty but will never be null.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    Collection<DependencySpec> importDependencies()
    {
        // Load the dependency specifications.
        Collection<ModuleDependencySpec> aDependencies = maybeCreatePomLoader().getDependencies();

        // Map each dependency spec's Maven scope to a Gradle configuration, filtering out those
        // with unmapped scopes and converting any dependency spec referring to a sub-project to a
        // ProjectDependencySpec.
        return aDependencies.stream()
            .map(this::convertDependency)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
    }


    /**
     * Import repository specifications from the pom file specified in the constructor.
     *
     * @return  A collection with the imported repository specifications. The returned collection
     *          may be empty but will never be null.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    Collection<RepositorySpec> importRepositories()
    {
        return maybeCreatePomLoader().getRepositories();
    }


    /**
     * Import the local repository specification from the Maven settings file specified in the
     * import extension.
     *
     * @return  The local repository specification.
     *
     * @throws GradleException  if loading the settings file fails.
     */
    RepositorySpec importLocalRepository()
    {
        return maybeCreatePomLoader().getLocalRepository();
    }


    /**
     * Get the group ID from the pom file specified in the constructor.
     *
     * @return  The group ID from the pom file.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    String getGroupId()
    {
        return maybeCreatePomLoader().getGroupId();
    }


    /**
     * Get the version string from the pom file specified in the constructor.
     *
     * @return  The version string from the pom file.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    String getVersionString()
    {
        return maybeCreatePomLoader().getVersion();
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
        if (fPomLoader == null)
        {
            // Load the implementation class and create a new instance of that class.
            fPomLoader = createEffectivePomLoader();

            // Initialize the pom loader with the pom file and the extension's settings file, if any.
            fPomLoader.init(fPomFile, fExtension != null ? fExtension.getSettingsFile() : null);
        }

        return fPomLoader;
    }


    /**
     * Map the configuration name of a {@code ModuleDependencySpec} from a Maven scope mapping to a
     * Gradle configuration name. If the specification's group, name and version match a project in
     * the same hierarchy as the project passed to the constructor, a {@code ProjectDependencySpec}
     * will be returned.
     *
     * @param pDependency The dependency to convert.
     *
     * @return  A dependency with the scope replaced with the appropriate configuration name, or
     *          null if there is no mapped configuration for the specification's Maven scope.
     *
     * @throws NullPointerException if {@code pDependency} is null.
     */
    private DependencySpec convertDependency(ModuleDependencySpec pDependency)
    {
        String aConfiguration = scopeToConfiguration(pDependency.getConfiguration());
        if (aConfiguration != null)
        {
            // The Maven scope is mapped to a configuration, this dependency should be imported.
            // Check if it refers to a project rather than to an external dependency,.
            Project aProject = Dependencies.findMatchingProject(pDependency, getProject());
            if (aProject != null)
            {
                // A dependency on another project in the project hierarchy, return a
                // ProjectDependencySpec with the same values as the module dependency.
                ProjectDependencySpec aProjectDependency =
                    new ProjectDependencySpec(aConfiguration, aProject.getPath());
                aProjectDependency.setValues(pDependency);
                return aProjectDependency;
            }
            else
            {
                // External module dependency, replace the Maven scope with the configuration name.
                pDependency.setConfiguration(aConfiguration);
                return pDependency;
            }
        }
        else
        {
            // No scope mapping, the dependency should be filtered out.
            getProjectLogger().info(
                    "Scope '{}' has no mapping, skipping dependency '{}'",
                    pDependency.getConfiguration(),
                    pDependency.toDependencyNotation());

            return null;
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
        Map<String, String> aMapping = fExtension != null ? fExtension.getScopeToConfiguration() : null;
        if (aMapping == null)
            // No mapping, scopes are mapped to themselves.
            return pScope;

        String aConfiguration = aMapping.get(pScope);
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


    /**
     * Create a new {@code EffectivePomLoader} by loading the implementation class and creating a
     * new instance of that class.
     *
     * @return  A new instance of the loaded {@code EffectivePomLoader} implementation, never null.
     */
    private EffectivePomLoader createEffectivePomLoader()
    {
        try
        {
            ExternalToolLoader<EffectivePomLoader> aLoader =
                new ExternalToolLoader<>(
                    EffectivePomLoader.class,
                    IMPLEMENTATION_PACKAGE,
                    IMPLEMENTATION_CLASS,
                    fExtension::getMavenClassPath);

            return aLoader.createToolProxy();
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
        {
            getProjectLogger().error(
                "Could not create an instance of '{}.{}'",
                IMPLEMENTATION_PACKAGE,
                IMPLEMENTATION_CLASS,
                e);

            return EmptyEffectivePomLoader.INSTANCE;
        }
    }


    /**
     * An {@code EffectivePomLoader} that returns empty values.
     */
    static private class EmptyEffectivePomLoader implements EffectivePomLoader
    {
        static final EmptyEffectivePomLoader INSTANCE = new EmptyEffectivePomLoader();

        @Override
        public void init(File pPomFile, File pSettingsFile)
        {
            // No-op
        }

        @Override
        public Collection<ModuleDependencySpec> getDependencies()
        {
            return Collections.emptyList();
        }

        @Override
        public Collection<RepositorySpec> getRepositories()
        {
            return Collections.emptyList();
        }

        @Override
        public RepositorySpec getLocalRepository()
        {
            return new MavenRepositorySpec("local", "");
        }

        @Override
        public String getGroupId()
        {
            return "";
        }

        @Override
        public String getArtifactId()
        {
            return "";
        }

        @Override
        public String getVersion()
        {
            return "";
        }
    }
}
