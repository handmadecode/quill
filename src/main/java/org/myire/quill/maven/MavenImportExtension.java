/*
 * Copyright 2017-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import static java.util.Objects.requireNonNull;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.file.FileCollection;

import org.myire.quill.common.ProjectAware;
import org.myire.quill.common.Resolver;


/**
 * Gradle project extension for specifying a Maven settings file, a mapping from Maven scopes to
 * Gradle configurations, and the Maven libraries to use when importing from a Maven pom file.
 */
public class MavenImportExtension extends ProjectAware
{
    static final String EXTENSION_NAME = "mavenImport";

    // Default Maven library version.
    // Note: versions 3.5.0 - 3.6.0 are affected by https://issues.apache.org/jira/browse/MNG-5995
    static private final String DEFAULT_MAVEN_VERSION = "3.6.1";

    static private final Resolver<ArtifactRepository> cResolver =
        new Resolver<>(ArtifactRepository.class);


    private final Configuration fConfiguration;

    private File fMavenSettingsFile;
    private String fMavenVersion = DEFAULT_MAVEN_VERSION;
    private FileCollection fMavenClassPath;
    private Object fDefaultRepository;

    // Mapping from Maven scope name to Gradle configuration name. Scopes not explicitly mapped are
    // implicitly mapped to a configuration with the same name, e.g. &quot;runtime&quot;.
    private final Map<String, String> fScopeToConfiguration = defaultScopeToConfiguration();

    // The set of configurations for which the beforeResolve and afterResolve actions have been
    // installed.
    private final Set<Configuration> fMavenClassPathConfigurations = new HashSet<>();

    // Mapping from configuration to repository, an entry in this map means the configuration has
    // installed the repository in its beforeResolve action, and that the repository should be
    // removed in the configuration's afterResolve action.
    private final Map<Configuration, ArtifactRepository> fInstalledRepositories = new HashMap<>();


    /**
     * Create a new {@code MavenImportExtension}.
     *
     * @param pProject          The project that owns the extension.
     * @param pConfiguration    The {@code mavenImport} configuration.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    public MavenImportExtension(Project pProject, Configuration pConfiguration)
    {
        super(pProject);

        fConfiguration = requireNonNull(pConfiguration);

        // The default value for the Maven class path is the configuration.
        fMavenClassPath = fConfiguration;
        maybeAddResolveActions(pConfiguration);

        // The default repository for resolving the configuration's dependencies is Maven central.
        fDefaultRepository = (Supplier<ArtifactRepository>) this::getMavenCentralRepository;
    }


    /**
     * Get the Maven settings file to use when importing from a pom file. If this file is null, the
     * default Maven settings will be used.
     *
     * @return  The Maven settings file specification, possibly null.
     */
    public File getSettingsFile()
    {
        return fMavenSettingsFile;
    }


    /**
     * Set the Maven settings file to use when importing from a pom file. This file will be resolved
     * relative to the project directory.
     */
    public void setSettingsFile(Object pFile)
    {
        fMavenSettingsFile = pFile != null ? getProject().file(pFile) : null;
    }


    /**
     * Get the version of the Maven libraries to specify in the dependencies of the
     * {@code mavenImport} configuration.
     *
     * @return  The Maven version string, never null.
     */
    public String getMavenVersion()
    {
        return fMavenVersion;
    }


    /**
     * Set the version of the Maven libraries to specify in the dependencies of the
     * {@code mavenImport} configuration.
     *
     * @param pMavenVersion The Maven library version. Passing null will cause the default value to
     *                      be returned from {@code getMavenVersion}.
     */
    public void setMavenVersion(String pMavenVersion)
    {
        fMavenVersion = pMavenVersion != null ? pMavenVersion : DEFAULT_MAVEN_VERSION;
    }


    /**
     * Get the class path containing the Maven related classes used by the Maven import. The initial
     * value is the {@code mavenImport} configuration.
     */
    public FileCollection getMavenClassPath()
    {
        return fMavenClassPath;
    }


    public void setMavenClassPath(FileCollection pMavenClassPath)
    {
        fMavenClassPath = pMavenClassPath != null ? pMavenClassPath : fConfiguration;

        if (pMavenClassPath instanceof Configuration)
            maybeAddResolveActions((Configuration) pMavenClassPath);
    }


    /**
     * Get the temporary repository to use for resolving the dependencies of the {@code mavenImport}
     * configuration.
     *
     * @return  The default repository, possibly null.
     */
    public ArtifactRepository getClassPathRepository()
    {
        // Remember the resolved repository to avoid evaluating e.g. closures on each call to this
        // method. Calling resolve() on an already resolved repository will simply return the
        // resolved instance.
        ArtifactRepository aRepository = cResolver.resolve(fDefaultRepository);
        fDefaultRepository = aRepository;
        return aRepository;
    }


    /**
     * Set the temporary repository to use for resolving the dependencies of the {@code mavenImport}
     * configuration.
     *
     * @param pRepository   An {@code ArtifactRepository} instance or a {@code Closure} or
     *                      {@code Supplier} that returns an {@code ArtifactRepository} instance.
     */
    public void setClassPathRepository(Object pRepository)
    {
        fDefaultRepository = pRepository;
    }


    /**
     * Get the mapping from Maven scope name to Gradle configuration name.
     *
     * @return  The scope to configuration mapping. The returned map is the instance used by the
     *          extension; modifications to it may affect the behaviour of other callers.
     */
    public Map<String, String> getScopeToConfiguration()
    {
        return fScopeToConfiguration;
    }


    /**
     * Get the Maven central repository. This method is primarily intended to be used in lambda
     * expressions.
     *
     * @return  The Maven central repository.
     */
    private ArtifactRepository getMavenCentralRepository()
    {
        return getProject().getRepositories().mavenCentral();
    }


    /**
     * Add resolve actions for a configuration's incoming dependencies if the configuration hasn't
     * had them added before.
     *
     * @param pConfiguration    The configuration.
     */
    private void maybeAddResolveActions(Configuration pConfiguration)
    {
        synchronized(fMavenClassPathConfigurations)
        {
            if (fMavenClassPathConfigurations.add(pConfiguration))
            {
                // The configuration hasn't had the actions added, add a beforeResolve and an
                // afterResolve actions that install and remove the class path repository if
                // necessary.
                pConfiguration.getIncoming().beforeResolve(d -> installClassPathRepositoryIntoProject(pConfiguration));
                pConfiguration.getIncoming().afterResolve(d -> removeClassPathRepositoryFromProject(pConfiguration));
            }
        }
    }


    /**
     * Install the extension's class path repository, if non-null, into the project's repository
     * handler if there are no other repositories defined.
     *
     * @param pConfiguration    The configuration to associate with the repository.
     */
    private void installClassPathRepositoryIntoProject(Configuration pConfiguration)
    {
        synchronized(fInstalledRepositories)
        {
            RepositoryHandler aRepositories = getProject().getRepositories();
            if (aRepositories.isEmpty())
            {
                // No repositories defined for the project, get the class path repository.
                ArtifactRepository aRepo = getClassPathRepository();

                // Only add the repo explicitly if not already present in the project. Even though
                // there were no repositories in the check above, getting the temporary repo may
                // cause it to be added to the project (e.g. through a closure creating the repo by
                // calling RepositoryHandler.maven()).
                if (aRepo != null && !aRepositories.contains(aRepo))
                    aRepositories.add(aRepo);

                // Associate the installed repo (if any) with the configuration so it can be removed
                // in removeClassPathRepositoryFromProject().
                ArtifactRepository aReplaced = fInstalledRepositories.put(pConfiguration, aRepo);
                if (aReplaced != null)
                    aRepositories.remove(aReplaced);
            }
        }
    }


    /**
     * Remove any earlier installed repository for a configuration from the project's repository
     * handler.
     *
     * @param pConfiguration    The configuration.
     */
    private void removeClassPathRepositoryFromProject(Configuration pConfiguration)
    {
        synchronized(fInstalledRepositories)
        {
            ArtifactRepository aRepo = fInstalledRepositories.remove(pConfiguration);
            if (aRepo != null)
                getProject().getRepositories().remove(aRepo);
        }
    }


    static private Map<String, String> defaultScopeToConfiguration()
    {
        Map<String, String> aScopeToConfiguration = new HashMap<>();
        aScopeToConfiguration.put("test", "testCompile");
        aScopeToConfiguration.put("provided", "compileOnly");
        return aScopeToConfiguration;
    }
}
