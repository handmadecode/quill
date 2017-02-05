/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.ArtifactRepository

import org.myire.quill.common.ProjectAware
import org.myire.quill.common.Resolver


/**
 * Gradle project extension for specifying a Maven settings file, a mapping from Maven scopes to
 * Gradle configurations, and the third-party library versions to use when importing from a Maven
 * pom.
 */
class MavenImportExtension extends ProjectAware
{
    // Default external library versions.
    static private final String DEFAULT_MAVEN_VERSION = "3.3.9"
    static private final String DEFAULT_SISU_PLEXUS_VERSION = "0.3.3"
    static private final String DEFAULT_SISU_GUICE_VERSION = "3.2.6"
    static private final String DEFAULT_AETHER_VERSION = "1.1.0"

    static private final Resolver<ArtifactRepository> cResolver = new Resolver(ArtifactRepository.class)


    // Properties accessed through getter and setter only.
    private File fMavenSettingsFile
    private String fMavenVersion
    private String fSisuPlexusVersion
    private String fSisuGuiceVersion
    private String fAetherVersion
    private Object fDefaultRepository

    /**
     * Mapping from Maven scope name to Gradle configuration name. Scopes not explicitly mapped are
     * implicitly mapped to a configuration with the same name, e.g. &quot;runtime&quot;.
     */
    Map<String, String> scopeToConfiguration = [
            test: 'testCompile',
            provided: 'compileOnly',
    ];


    /**
     * Create a new {@code MavenImportExtension}.
     *
     * @param pProject  The project that owns the extension.
     */
    MavenImportExtension(Project pProject)
    {
        super(pProject);
        fDefaultRepository = { pProject.repositories.mavenCentral() }
    }


    /**
     * Get the Maven settings file to use when importing from a pom file. If this file is null, the
     * default Maven settings will be used.
     *
     * @return  The Maven settings file specification, possibly null.
     */
    File getSettingsFile()
    {
        return fMavenSettingsFile;
    }


    /**
     * Set the Maven settings file to use when importing from a pom file. This file will be resolved
     * relative to the project directory.
     */
    void setSettingsFile(Object pFile)
    {
        fMavenSettingsFile = pFile ? project.file(pFile) : null;
    }


    /**
     * Get the version of the Maven libraries to use when importing from a pom file.
     *
     * @return  The Maven version string, never null.
     */
    String getMavenVersion()
    {
        return fMavenVersion ?: DEFAULT_MAVEN_VERSION;
    }


    /**
     * Set the version of the Maven libraries to use when importing from a pom file.
     *
     * @param pMavenVersion The Maven library version. Passing null will restore the default value
     *                      the next time {@code getMavenVersion} is called.
     */
    void setMavenVersion(String pMavenVersion)
    {
        fMavenVersion = pMavenVersion;
    }


    /**
     * Get the version of the Sisu Plexus libraries to use when importing from a pom file.
     *
     * @return  The Sisu Plexus version string, never null.
     */
    String getSisuPlexusVersion()
    {
        return fSisuPlexusVersion ?: DEFAULT_SISU_PLEXUS_VERSION;
    }


    /**
     * Set the version of the Sisu Plexus libraries to use when importing from a pom file
     *
     * @param pSisuPlexusVersion    The Sisu Plexus library version. Passing null will restore the
     *                              default value the next time {@code getSisuPlexusVersion} is
     *                              called.
     */
    void setSisuPlexusVersion(String pSisuPlexusVersion)
    {
        fSisuPlexusVersion = pSisuPlexusVersion;
    }


    /**
     * Get the version of the Sisu Guice libraries to use when importing from a pom file.
     *
     * @return  The Sisu Guice version string, never null.
     */
    String getSisuGuiceVersion()
    {
        return fSisuGuiceVersion ?: DEFAULT_SISU_GUICE_VERSION;
    }


    /**
     * Set the version of the Sisu Guice libraries to use when importing from a pom file.
     *
     * @param pSisuGuiceVersion The Sisu Guice library version. Passing null will restore the
     *                          default value the next time {@code getSisuGuiceVersion} is called.
     */
    void setSisuGuiceVersion(String pSisuGuiceVersion)
    {
        fSisuGuiceVersion = pSisuGuiceVersion;
    }


    /**
     * Get the version of the Eclipse Aether libraries to use when importing from a pom file.
     *
     * @return  The Eclipse Aether version string, never null.
     */
    String getAetherVersion()
    {
        return fAetherVersion ?: DEFAULT_AETHER_VERSION;
    }


    /**
     * Set the version of the Eclipse Aether libraries to use when importing from a pom file.
     *
     * @param pAetherVersion    The Eclipse Aether library version. Passing null will restore the
     *                          default value the next time {@code getAetherVersion} is called.
     */
    void setAetherVersion(String pAetherVersion)
    {
        fAetherVersion = pAetherVersion;
    }


    /**
     * Get the temporary repository to use for resolving the dependencies of the {@code mavenImport}
     * configuration.
     *
     * @return  The default repository, possibly null.
     */
    ArtifactRepository getClasspathRepository()
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
     *                      {@code Callable} that returns an {@code ArtifactRepository} instance.
     */
    void setClasspathRepository(Object pRepository)
    {
        fDefaultRepository = pRepository;
    }
}
