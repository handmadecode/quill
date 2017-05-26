/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven

import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.cli.configuration.SettingsXmlConfigurationProcessor
import org.apache.maven.execution.DefaultMavenExecutionRequest
import org.apache.maven.execution.MavenExecutionRequest
import org.apache.maven.execution.MavenExecutionRequestPopulator
import org.apache.maven.model.Dependency
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuilder
import org.apache.maven.project.ProjectBuildingRequest
import org.apache.maven.settings.Mirror
import org.apache.maven.settings.Server
import org.apache.maven.settings.Settings
import org.apache.maven.settings.building.DefaultSettingsBuilder
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest
import org.apache.maven.settings.building.SettingsBuildingRequest

import org.codehaus.plexus.ContainerConfiguration
import org.codehaus.plexus.DefaultContainerConfiguration
import org.codehaus.plexus.DefaultPlexusContainer
import org.codehaus.plexus.PlexusConstants
import org.codehaus.plexus.PlexusContainer
import org.codehaus.plexus.PlexusContainerException
import org.codehaus.plexus.classworlds.ClassWorld
import org.codehaus.plexus.classworlds.realm.ClassRealm

import org.myire.quill.dependency.DependencySpec
import org.myire.quill.repository.MavenRepositorySpec


/**
 * Implementation of {@code EffectivePomLoader} based on the Maven libraries. This class should not
 * be loaded before the Maven libraries are available on the class path.
 */
class EffectivePomLoaderImpl implements EffectivePomLoader
{
    private final Properties fSystemProperties = copySystemProperties();

    // Pom and settings file, initialized to default values.
    private File fPomFile = new File('pom.xml');
    private File fSettingsFile = SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE;

    // The Maven settings, lazily loaded.
    private Settings fSettings;

    // The effective pom, lazily loaded.
    private MavenProject fProject;


    /**
     * Specify the pom file to load and any settings file to apply when loading the effective pom.
     *
     * @param pPomFile      The pom file.
     * @param pSettingsFile Any settings file to use. If null the default Maven settings will be
     *                      used.
     *
     * @throws NullPointerException if {@code pPomFile} is null.
     */
    void init(File pPomFile, File pSettingsFile)
    {
        fPomFile = Objects.requireNonNull(pPomFile);
        if (pSettingsFile != null)
            fSettingsFile = pSettingsFile;
    }


    /**
     * Get the dependencies from the effective pom. This will cause the effective pom to be loaded
     * from the file specified in {@code init} if that hasn't been done before.
     *
     * @return  A collection of {@code DependencySpec}, one for each dependency in the effective
     *          pom. The returned collection will never be null but may be empty. The Maven scope
     *          names will be used as configuration names in the specs.
     */
    Collection<DependencySpec> getDependencies()
    {
        // Load the project containing the effective pom if needed.
        MavenProject aProject = maybeLoadProject();

        // Convert each Maven dependency to a DependencySpec.
        return aProject.getModel().getDependencies().collect { toDependencySpec(it) }
    }


    /**
     * Get the repositories from the effective pom.
     *
     * @return  A collection of {@code MavenRepositorySpec}, one for each repository in the pom. The
     *          returned collection will never be null but may be empty.
     */
    Collection<MavenRepositorySpec> getRepositories()
    {
        // Get the mirrors from the settings and create a matcher to find out which repositories
        // that have a mirror.
        Settings aSettings = maybeLoadSettings();
        MirrorMatcher aMirrorMatcher = new MirrorMatcher(aSettings.mirrors);

        // Filter out the repositories in the Maven project that have a mirror and convert those
        // that don't to a MavenRepositorySpec.
        Collection<MavenRepositorySpec> aRepositories =
                maybeLoadProject().getRemoteArtifactRepositories().findAll {
                    !aMirrorMatcher.matches(it)
                }
                .collect {
                    toRepositorySpec(it)
                }

        // Add a MavenRepositorySpec for each mirror.
        aSettings.mirrors.each { aRepositories.add(toRepositorySpec(it)) }

        // Pick up any login credentials from the settings' server definitions.
        aRepositories.each { setCredentials(it, aSettings.servers) }

        return aRepositories;
    }


    /**
     * Get the group ID from the effective pom. This will cause the effective pom to be loaded
     * from the file specified in {@code init} if that hasn't been done before.
     *
     * @return  The project model's group ID.
     */
    String getGroupId()
    {
        MavenProject aProject = maybeLoadProject();
        return aProject.getModel().getGroupId();
    }


    /**
     * Get the artifact ID from the effective pom. This will cause the effective pom to be loaded
     * from the file specified in {@code init} if that hasn't been done before.
     *
     * @return  The project model's artifact ID.
     */
    String getArtifactId()
    {
        MavenProject aProject = maybeLoadProject();
        return aProject.getModel().getArtifactId();
    }


    /**
     * Get the version string from the effective pom. This will cause the effective pom to be loaded
     * from the file specified in {@code init} if that hasn't been done before.
     *
     * @return  The project model's version string.
     */
    String getVersion()
    {
        MavenProject aProject = maybeLoadProject();
        return aProject.getModel().getVersion();
    }


    /**
     * Load the effective pom as a {@code MavenProject} from the pom file specified in {@code init}
     * if that hasn't be done already.
     *
     * @return  The effective pom as a {@code MavenProject}.
     */
    private MavenProject maybeLoadProject()
    {
        if (fProject != null)
            // Already loaded.
            return fProject;

        // Maven project loading based on
        // org.gradle.buildinit.plugins.internal.maven.MavenProjectsCreator#createNow.

        // Create a container to lookup the Maven entities needed for the load.
        PlexusContainer aContainer = createContainer();

        MavenExecutionRequest aExecutionRequest = new DefaultMavenExecutionRequest();
        aExecutionRequest.setSystemProperties(fSystemProperties);

        MavenExecutionRequestPopulator aRequestPopulator = aContainer.lookup(MavenExecutionRequestPopulator.class);
        aRequestPopulator.populateFromSettings(aExecutionRequest, maybeLoadSettings());
        aRequestPopulator.populateDefaults(aExecutionRequest);

        ProjectBuildingRequest aBuildingRequest = aExecutionRequest.getProjectBuildingRequest();
        aBuildingRequest.setProcessPlugins(false);

        ProjectBuilder aProjectBuilder = aContainer.lookup(ProjectBuilder.class);
        fProject = aProjectBuilder.build(fPomFile, aBuildingRequest).getProject();
        return fProject;
    }


    /**
     * Load the Maven settings if that hasn't be done already.
     *
     * @return  The Maven settings.
     */
    private Settings maybeLoadSettings()
    {
        if (fSettings != null)
            return fSettings;

        // Settings not loaded, create a request to do so with the user settings file passed to the
        // init() method.
        SettingsBuildingRequest aRequest = new DefaultSettingsBuildingRequest();
        aRequest.setUserSettingsFile(fSettingsFile);
        aRequest.setGlobalSettingsFile(SettingsXmlConfigurationProcessor.DEFAULT_GLOBAL_SETTINGS_FILE);
        aRequest.setSystemProperties(fSystemProperties);

        DefaultSettingsBuilder aBuilder = new DefaultSettingsBuilderFactory().newInstance();
        fSettings = aBuilder.build(aRequest).getEffectiveSettings();
        return fSettings;
    }


    /**
     * Create a {@code DependencySpec} from a Maven {@code Dependency}.
     *
     * @param pMavenDependency  The Maven dependency.
     *
     * @return  A new {@code DependencySpec}.
     */
    static private DependencySpec toDependencySpec(Dependency pMavenDependency)
    {
        DependencySpec aSpec = new DependencySpec(
                pMavenDependency.scope,
                pMavenDependency.groupId,
                pMavenDependency.artifactId,
                pMavenDependency.version);

        aSpec.setClassifier(pMavenDependency.classifier);

        pMavenDependency.exclusions.each
        {
            aSpec.addExclusion(it.groupId, it.artifactId);
        }

        return aSpec;
    }


    /**
     * Create a {@code MavenRepositorySpec} from a Maven {@code ArtifactRepository}.
     *
     * @param pArtifactRepository   The Maven artifact repository.
     *
     * @return  A new {@code MavenRepositorySpec}.
     */
    static private MavenRepositorySpec toRepositorySpec(ArtifactRepository pArtifactRepository)
    {
        MavenRepositorySpec aSpec = new MavenRepositorySpec(
                pArtifactRepository.id,
                pArtifactRepository.url);

        if (pArtifactRepository.authentication != null)
            aSpec.setCredentials(pArtifactRepository.authentication.username, pArtifactRepository.authentication.password);

        return aSpec;
    }


    /**
     * Create a {@code MavenRepositorySpec} from a Maven {@code Mirror}.
     *
     * @param pMirror   The Maven mirror.
     *
     * @return  A new {@code MavenRepositorySpec}.
     */
    static private MavenRepositorySpec toRepositorySpec(Mirror pMirror)
    {
        return new MavenRepositorySpec(pMirror.id, pMirror.url);
    }


    /**
     * Set the credentials for a {@code MavenRepositorySpec} from a Maven {@code Server} whose id
     * matches the repository's name.
     *
     * @param pRepository   The repository.
     * @param pServers      The list to find the matching server in.
     */
    static private void setCredentials(MavenRepositorySpec pRepository, List<Server> pServers)
    {
        if (pRepository.credentials == null)
        {
            Server aServer = pServers.find { it.id == pRepository.name }
            if (aServer != null)
                pRepository.setCredentials(aServer.username, aServer.password);
        }
    }


    /**
     * Create a container to lookup Maven entities in.
     *
     * @return  A new {@code PlexusContainer}.
     *
     * @throws PlexusContainerException if creating the container fails
     */
    static private PlexusContainer createContainer()
    {
        String aRealmID = 'plexus.core';
        ClassWorld aClassWorld = new ClassWorld(aRealmID, ClassWorld.class.getClassLoader());
        ClassRealm aClassRealm = aClassWorld.getRealm(aRealmID);
        ContainerConfiguration aContainerConfiguration =
                new DefaultContainerConfiguration()
                        .setClassWorld(aClassWorld)
                        .setRealm(aClassRealm)
                        .setClassPathScanning(PlexusConstants.SCANNING_INDEX)
                        .setAutoWiring(true)
                        .setName("mavenCore");
        return new DefaultPlexusContainer(aContainerConfiguration);
    }


    /**
     * Create a copy of the system properties.
     *
     * @return  A new {@code Properties} with a snapshot of the current system properties.
     */
    static private Properties copySystemProperties()
    {
        Properties aOriginal = System.getProperties();
        Properties aCopy = new Properties();

        // The entrySet() iterator in Properties is not thread safe; use the thread-safe
        // stringPropertyNames() to get all keys and copy the properties.
        aOriginal.stringPropertyNames().each
        {
            String aValue = aOriginal.getProperty(it);
            if (aValue != null)
                aCopy.put(it, aValue);
        }

        return aCopy;
    }
}
