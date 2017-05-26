/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin

import org.myire.quill.dependency.Dependencies
import org.myire.quill.dependency.DependencySpec
import org.myire.quill.repository.MavenRepositorySpec


/**
 * Gradle plugin for importing from a Maven pom file.
 *<p>
 * Dependencies can be added to a project through the dynamic method {@code fromPomFile} that is
 * added to the project's {@code DependencyHandler}.
 *<p>
 * Repositories can be added to a project through the dynamic method {@code fromPomFile} that is
 * added to the project's {@code RepositoryHandler}.
 *<p>
 * Dependencies and repositories can be written to a file on Gradle notation with the
 * {@code PomConvertTask} task added to the project.
 *<p>
 * The plugin also creates a configuration that specifies the classpath to use when running the
 * Maven import.
 */
class MavenImportPlugin implements Plugin<Project>
{
    static private final Logger cLogger = Logging.getLogger(MavenImportPlugin.class)

    static final String TASK_NAME = 'convertPom'
    static final String EXTENSION_NAME = 'mavenImport'
    static final String CONFIGURATION_NAME = 'mavenImport'

    // The default artifacts the import classpath should depend on.
    // Partly taken from https://github.com/mizdebsk/gradle/commit/01ab8ba
    static private final String MAVEN_CORE_GROUP_ARTIFACT_ID = 'org.apache.maven:maven-core'
    static private final String MAVEN_EMBEDDER_GROUP_ARTIFACT_ID = 'org.apache.maven:maven-embedder'
    static private final String SISU_PLEXUS_GROUP_ARTIFACT_ID = 'org.eclipse.sisu:org.eclipse.sisu.plexus'
    static private final String SISU_PLEXUS_EXT = 'jar'
    static private final String SISU_GUICE_GROUP_ARTIFACT_ID = 'org.sonatype.sisu:sisu-guice'
    static private final String SISU_GUICE_CLASSIFIER = 'no_aop'
    static private final String SISU_GUICE_EXT = 'jar'
    static private final String AETHER_CONNECTOR_BASIC_GROUP_ARTIFACT_ID = 'org.eclipse.aether:aether-connector-basic'
    static private final String AETHER_TRANSPORT_FILE_GROUP_ARTIFACT_ID = 'org.eclipse.aether:aether-transport-file'
    static private final String AETHER_TRANSPORT_HTTP_GROUP_ARTIFACT_ID = 'org.eclipse.aether:aether-transport-http'


    private Project fProject
    private MavenImportExtension fExtension
    private Configuration fConfiguration
    private PomConvertTask fTask
    private Map<File, PomImporter> fImporters


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Make sure the standard java plugin is applied, otherwise the standard configurations to
        // map Maven scopes to will not be available.
        pProject.plugins.apply(JavaPlugin.class);

        // Add the extension that allows configuring the location of the Maven settings file, the
        // scope mapping, and the versions of the third-party libraries.
        fExtension = pProject.extensions.create(EXTENSION_NAME, MavenImportExtension.class, pProject);

        // Create the Maven import configuration and add it to the project. The classpath for the
        // Maven import is specified through this configuration's dependencies.
        fConfiguration = createConfiguration();

        // Create the task.
        fTask = createTask();

        // Create the map from pom file to MavenImporter instance.
        fImporters = [:].withDefault { File f -> createPomImporter(f) };

        // Add a dynamic method to the project's dependency handler that allows build scripts to
        // specify that (additional) dependencies should be imported from a Maven pom file.
        pProject.dependencies.metaClass.fromPomFile
        {
            Object pPomFile -> importMavenDependencies(pPomFile);
        }

        // Add a dynamic method to the project's repository handler that allows build scripts to
        // specify that (additional) repositories should be imported from a Maven pom file.
        pProject.repositories.metaClass.fromPomFile
        {
            Object pPomFile -> importMavenRepositories(pPomFile);
        }

        // Add a dynamic method to the project that sets the project's group from a pom file's group
        // ID.
        pProject.metaClass.applyGroupFromPomFile
        {
            Object pPomFile -> applyGroupToProject(pPomFile);
        }

        // Add a dynamic method to the project that sets the project's version string from a pom
        // file's version string.
        pProject.metaClass.applyVersionFromPomFile
        {
            Object pPomFile -> applyVersionToProject(pPomFile);
        }
    }


    /**
     * Import dependencies from a Maven pom file and add them to the project's dependencies.
     *
     * @param pPomFile  The pom file to import dependencies from. If null, a file called
     *                  &quot;pom.xml&quot; in the project directory will be used.
     */
    void importMavenDependencies(Object pPomFile)
    {
        File aPomFile = fProject.file(pPomFile ?: 'pom.xml');
        cLogger.debug('Importing Maven dependencies from \'{}\' to project {}',
                      aPomFile.absolutePath,
                      fProject.name);

        Collection<DependencySpec> aDependencies = fImporters[aPomFile].importDependencies();
        aDependencies.each { Dependencies.addDependency(fProject, it) }
    }


    /**
     * Import repositories from a Maven pom file and add them to the project's repository handler.
     *
     * @param pPomFile  The pom file to import repositories from. If null, a file called
     *                  &quot;pom.xml&quot; in the project directory will be used.
     */
    void importMavenRepositories(Object pPomFile)
    {
        File aPomFile = fProject.file(pPomFile ?: 'pom.xml');
        cLogger.debug('Importing Maven repositories from \'{}\' to project {}',
                      aPomFile.absolutePath,
                      fProject.name);

        Collection<MavenRepositorySpec> aRepositories = fImporters[aPomFile].importRepositories();
        for (aRepository in aRepositories)
        {
            cLogger.debug('Adding repository {}', aRepository.url);
            fProject.repositories.maven(aRepository);
        }
    }


    /**
     * Set the project's {@code group} property to the group ID specified in a pom file.
     *
     * @param pPomFile  The pom file to get the group ID from. If null, a file called
     *                  &quot;pom.xml&quot; in the project directory will be used.
     */
    void applyGroupToProject(Object pPomFile)
    {
        File aPomFile = fProject.file(pPomFile ?: 'pom.xml');
        cLogger.debug('Setting group for project {} from \'{}\'',
                      fProject.name,
                      aPomFile.absolutePath);
        fProject.setGroup(fImporters[aPomFile].groupId);
    }


    /**
     * Set the project's {@code version} property to the version string specified in a pom file.
     *
     * @param pPomFile  The pom file to get the version string from. If null, a file called
     *                  &quot;pom.xml&quot; in the project directory will be used.
     */
    void applyVersionToProject(Object pPomFile)
    {
        File aPomFile = fProject.file(pPomFile ?: 'pom.xml');
        cLogger.debug('Setting version for project {} from \'{}\'',
                      fProject.name,
                      aPomFile.absolutePath);
        fProject.setVersion(fImporters[aPomFile].versionString);
    }


    /**
     * Create the Maven dependency import configuration if not already present in the project and
     * define it to depend on the default artifacts unless explicit dependencies have been defined.
     *
     * @return  The Maven dependency import configuration.
     */
    private Configuration createConfiguration()
    {
        Configuration aConfiguration = fProject.configurations.maybeCreate(CONFIGURATION_NAME);

        aConfiguration.with {
            visible = false;
            transitive = true;
            description = 'The classes used by the Maven dependency import';
        }

        // Add an action that adds the default dependencies to the configuration if it is empty at
        // resolve time, and adds the default repository to the project if no other repositories
        // have been set up.
        aConfiguration.incoming.beforeResolve { setupDependencies() }

        // Add an action that removes the default repository after resolve time if it was added
        // before resolve time.
        aConfiguration.incoming.afterResolve { removeTemporaryRepository() }

        return aConfiguration;
    }


    /**
     * Create a new {@code MavenDependencyConvertTask}.
     *
     * @return  The created {@code MavenDependencyConvertTask} instance.
     */
    private PomConvertTask createTask()
    {
        PomConvertTask aTask = fProject.tasks.create(TASK_NAME, PomConvertTask.class);
        aTask.description = 'Imports dependencies and/or repositories from a Maven pom file and writes them to a Gradle file';
        aTask.mavenClasspath = fConfiguration;
        aTask.init(fExtension);

        return aTask;
    }


    /**
     * Create a {@code PomImporter} for a pom file.
     *
     * @param pPomFile  The pom file.
     *
     * @return  A new {@code PomImporter}.
     */
    private PomImporter createPomImporter(File pPomFile)
    {
        return new PomImporter(fProject, pPomFile, { fConfiguration });
    }


    /**
     * Add the default dependencies to the configuration if it is empty, and add the extension's
     * default repository if the project's repository handler is empty. The latter is to ensure that
     * the dependencies to load the pom file with can be resolved when repositories are to be loaded
     * from the pom file.
     */
    private void setupDependencies()
    {
        if (fConfiguration.dependencies.empty)
        {
            addDependency("${MAVEN_CORE_GROUP_ARTIFACT_ID}:${fExtension.mavenVersion}");
            addDependency("${MAVEN_EMBEDDER_GROUP_ARTIFACT_ID}:${fExtension.mavenVersion}");
            addDependency("${SISU_PLEXUS_GROUP_ARTIFACT_ID}:${fExtension.sisuPlexusVersion}@${SISU_PLEXUS_EXT}");
            addDependency("${SISU_GUICE_GROUP_ARTIFACT_ID}:${fExtension.sisuGuiceVersion}:${SISU_GUICE_CLASSIFIER}@${SISU_GUICE_EXT}");
            addDependency("${AETHER_CONNECTOR_BASIC_GROUP_ARTIFACT_ID}:${fExtension.aetherVersion}");
            addDependency("${AETHER_TRANSPORT_FILE_GROUP_ARTIFACT_ID}:${fExtension.aetherVersion}");
            addDependency("${AETHER_TRANSPORT_HTTP_GROUP_ARTIFACT_ID}:${fExtension.aetherVersion}");
        }

        if (fProject.repositories.empty)
        {
            // No repositories, get the temporary repo specified in the extension, if any.
            ArtifactRepository aRepo = fExtension.classpathRepository;

            // Only add the repo explicitly if not already present in the project. Even though
            // there were no repositories earlier, getting the temporary repo may cause it to be
            // added to the project (e.g. through a closure creating the repo by calling
            // e.g. RepositoryHandler.maven()).
            if (aRepo != null && !fProject.repositories.contains(aRepo))
                fProject.repositories.add(aRepo);
        }
    }


    /**
     * Remove the extension's temporary repository, if non-null, from the project's repository
     * handler.
     */
    private void removeTemporaryRepository()
    {
        ArtifactRepository aRepo = fExtension.classpathRepository;
        if (aRepo != null)
            fProject.repositories.remove(aRepo);
    }


    /**
     * Add a dependency to the {@code mavenImport} configuration.
     *
     * @param pDependencyNotation   The dependency notation.
     */
    private void addDependency(String pDependencyNotation)
    {
        fConfiguration.dependencies.add(fProject.dependencies.create(pDependencyNotation));
    }
}
