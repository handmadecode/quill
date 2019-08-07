/*
 * Copyright 2017-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin

import org.myire.quill.dependency.DependencySpec
import org.myire.quill.repository.RepositorySpec


/**
 * Gradle plugin for importing from a Maven pom file.
 *<p>
 * Dependencies can be added to a project through the dynamic method {@code fromPomFile} that is
 * added to the project's {@code DependencyHandler}.
 *<p>
 * Repositories can be added to a project through the dynamic method {@code fromPomFile} that is
 * added to the project's {@code RepositoryHandler}. The Maven local repository can be added through
 * the dynamic method {@code mavenLocalFromSettings}.
 *<p>
 * A project's group ID and version can be through the dynamic methods {@code applyGroupFromPomFile}
 * and {@code applyVersionFromPomFile} that are added to the project.
 *<p>
 * Dependencies and repositories can be written to a file on Gradle notation with the
 * {@code PomConvertTask} task added to the project.
 *<p>
 * The plugin also creates a configuration that specifies the default classpath to use when running
 * the Maven import.
 */
class MavenImportPlugin implements Plugin<Project>
{
    static private final Logger cLogger = Logging.getLogger(MavenImportPlugin.class)

    static final String CONFIGURATION_NAME = 'mavenImport'

    // The default artifacts the import classpath should depend on.
    static private final String MAVEN_EMBEDDER_GROUP_ARTIFACT_ID = 'org.apache.maven:maven-embedder'
    static private final String AETHER_BASIC_GROUP_ARTIFACT_ID = 'org.eclipse.aether:aether-connector-basic'
    static private final String AETHER_WAGON_GROUP_ARTIFACT_ID = 'org.eclipse.aether:aether-transport-wagon'
    static private final String WAGON_FILE_GROUP_ARTIFACT_ID = 'org.apache.maven.wagon:wagon-file'
    static private final String WAGON_HTTP_GROUP_ARTIFACT_ID = 'org.apache.maven.wagon:wagon-http'
    static private final String WAGON_PROVIDER_GROUP_ARTIFACT_ID = 'org.apache.maven.wagon:wagon-provider-api'
    static private final String SLF4J_NOOP_DEPENDENCY = 'org.slf4j:slf4j-nop:1.7.26'


    private Project fProject
    private Configuration fConfiguration
    private MavenImportExtension fExtension
    private PomConvertTask fTask


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Make sure the standard java plugin is applied, otherwise the standard configurations to
        // map Maven scopes to will not be available.
        pProject.plugins.apply(JavaPlugin.class);

        // Clear the cache of PomImporter instances; it may be populated from a previous run with
        // the Gradle daemon that still has the PomImporter class loaded.
        PomImporter.clearInstanceCache();

        // Create the Maven import configuration and add it to the project. The default classpath
        // for the Maven import is specified through this configuration's dependencies.
        fConfiguration = createConfiguration();

        // Add the extension that allows configuring the location of the Maven settings file, the
        // scope mapping, and the class path of the Maven libraries.
        fExtension = pProject.extensions.create(
                MavenImportExtension.EXTENSION_NAME,
                MavenImportExtension.class,
                pProject,
                fConfiguration);

        // Create the task.
        fTask = createTask();

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

        // Add a dynamic method to the project's repository handler that allows build scripts to
        // specify that the Maven local repository should be imported from the Maven settings file.
        pProject.repositories.metaClass.mavenLocalFromSettings
        {
            importMavenLocalRepository();
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
        File aPomFile = asPomFile(pPomFile);
        cLogger.debug('Importing Maven dependencies from \'{}\' to project {}',
                      aPomFile.absolutePath,
                      fProject.name);

        Collection<DependencySpec> aDependencies = getPomImporter(aPomFile).importDependencies();
        for (aDependency in aDependencies)
        {
            cLogger.debug('Adding dependency {}', aDependency.toDependencyNotation());
            aDependency.addTo(fProject);
        }
    }


    /**
     * Import repositories from a Maven pom file and add them to the project's repository handler.
     *
     * @param pPomFile  The pom file to import repositories from. If null, a file called
     *                  &quot;pom.xml&quot; in the project directory will be used.
     */
    void importMavenRepositories(Object pPomFile)
    {
        File aPomFile = asPomFile(pPomFile);
        cLogger.debug('Importing Maven repositories from \'{}\' to project {}',
                      aPomFile.absolutePath,
                      fProject.name);

        Collection<RepositorySpec> aRepositories = getPomImporter(aPomFile).importRepositories();
        for (aRepository in aRepositories)
        {
            cLogger.debug('Adding repository {}', aRepository.url);
            fProject.repositories.maven({ MavenArtifactRepository r -> copyValues(aRepository, r) } );
        }
    }


    /**
     * Import the Maven local repository from the import extension's Maven settings file and add it
     * to the project's repository handler.
     */
    void importMavenLocalRepository()
    {
        RepositorySpec aRepository = getPomImporter(asPomFile(null)).importLocalRepository();
        cLogger.debug('Adding Maven local repository {}', aRepository.url);
        fProject.repositories.maven({ MavenArtifactRepository r -> configureMavenLocalRepo(r, aRepository.url) } );
    }


    /**
     * Set the project's {@code group} property to the group ID specified in a pom file.
     *
     * @param pPomFile  The pom file to get the group ID from. If null, a file called
     *                  &quot;pom.xml&quot; in the project directory will be used.
     */
    void applyGroupToProject(Object pPomFile)
    {
        File aPomFile = asPomFile(pPomFile);
        cLogger.debug('Setting group for project {} from \'{}\'',
                      fProject.name,
                      aPomFile.absolutePath);
        fProject.setGroup(getPomImporter(aPomFile).groupId);
    }


    /**
     * Set the project's {@code version} property to the version string specified in a pom file.
     *
     * @param pPomFile  The pom file to get the version string from. If null, a file called
     *                  &quot;pom.xml&quot; in the project directory will be used.
     */
    void applyVersionToProject(Object pPomFile)
    {
        File aPomFile = asPomFile(pPomFile);
        cLogger.debug('Setting version for project {} from \'{}\'',
                      fProject.name,
                      aPomFile.absolutePath);
        fProject.setVersion(getPomImporter(aPomFile).versionString);
    }


    /**
     * Create the Maven import configuration if not already present in the project and define it to
     * depend on the default artifacts unless explicit dependencies have been defined.
     *
     * @return  The Maven import configuration.
     */
    private Configuration createConfiguration()
    {
        Configuration aConfiguration = fProject.configurations.maybeCreate(CONFIGURATION_NAME);

        aConfiguration.with {
            visible = false;
            transitive = true;
            description = 'The default classes used by the Maven pom import';
        }

        // Add an action that adds the default dependencies to the configuration if it is empty at
        // resolve time.
        aConfiguration.incoming.beforeResolve { setupDependencies() }

        return aConfiguration;
    }


    /**
     * Create a new {@code PomConvertTask}.
     *
     * @return  The created {@code PomConvertTask} instance.
     */
    private PomConvertTask createTask()
    {
        PomConvertTask aTask = fProject.tasks.create(PomConvertTask.TASK_NAME, PomConvertTask.class);
        aTask.init(fExtension);
        return aTask;
    }


    /**
     * Get the {@code PomImporter} for a pom file.
     *
     * @param pPomFile  The pom file.
     *
     * @return The {@code PomImporter} for the specified file.
     */
    private PomImporter getPomImporter(File pPomFile)
    {
        return PomImporter.getInstance(fProject, pPomFile);
    }


    /**
     * Add the default dependencies to the configuration if it has no dependencies.
     */
    private void setupDependencies()
    {
        if (fConfiguration.dependencies.empty)
        {
            addDependency("${MAVEN_EMBEDDER_GROUP_ARTIFACT_ID}:${fExtension.mavenVersion}");
            addDependency("${AETHER_BASIC_GROUP_ARTIFACT_ID}:${fExtension.aetherVersion}");
            addDependency("${AETHER_WAGON_GROUP_ARTIFACT_ID}:${fExtension.aetherVersion}");
            addDependency("${WAGON_FILE_GROUP_ARTIFACT_ID}:${fExtension.wagonVersion}");
            addDependency("${WAGON_HTTP_GROUP_ARTIFACT_ID}:${fExtension.wagonVersion}");
            addDependency("${WAGON_PROVIDER_GROUP_ARTIFACT_ID}:${fExtension.wagonVersion}");
            addDependency(SLF4J_NOOP_DEPENDENCY);
        }
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


    /**
     * Resolve an object to a pom file specification relative to the project directory. If the
     * object is null, a file called &quot;pom.xml&quot; in the project directory is returned.
     *
     * @param pValue    The object to resolve.
     *
     * @return  The resolved object.
     */
    private File asPomFile(Object pValue)
    {
        return fProject.file(pValue ?: 'pom.xml');
    }


    /**
     * Configure a Maven local repository.
     *
     * @param pRepo     The repository to configure.
     * @param pLocation The path to the local repository's location.
     */
    static private void configureMavenLocalRepo(MavenArtifactRepository pRepo, String pLocation)
    {
        pRepo.name = 'MavenLocal';
        pRepo.url = pLocation;
    }


    /**
     * Copy the values from a {@code RepositorySpec} to a {@code MavenArtifactRepository}.
     *
     * @param pFrom The instance to copy the values from.
     * @param pTo   The instance to copy the values to.
     */
    static private void copyValues(RepositorySpec pFrom, MavenArtifactRepository pTo)
    {
        pTo.name = pFrom.name;
        pTo.url = pFrom.url;

        if (pFrom.credentials?.userName)
            pTo.credentials({ PasswordCredentials c -> copyValues(pFrom, c) });
    }


    /**
     * Copy the credential values from a {@code RepositorySpec} to a {@code PasswordCredentials}.
     *
     * @param pFrom The instance to copy the values from.
     * @param pTo   The instance to copy the values to.
     */
    static private void copyValues(RepositorySpec pFrom, PasswordCredentials pTo)
    {
        pTo.username = pFrom.credentials?.userName;
        pTo.password = pFrom.credentials?.password;
    }
}
