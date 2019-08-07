/*
 * Copyright 2017-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import static java.util.Objects.requireNonNull;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.cli.configuration.SettingsXmlConfigurationProcessor;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulationException;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import org.myire.quill.dependency.ModuleDependencySpec;
import org.myire.quill.maven.EffectivePomLoader;
import org.myire.quill.repository.MavenRepositorySpec;
import org.myire.quill.repository.RepositorySpec;


/**
 * Implementation of {@code EffectivePomLoader} based on the Maven libraries. This class should not
 * be loaded before the Maven libraries are available on the class path.
 */
public class EffectivePomLoaderImpl implements EffectivePomLoader
{
    private final Properties fSystemProperties = copySystemProperties();

    // Pom and settings file, initialized to default values.
    private File fPomFile = new File("pom.xml");
    private File fSettingsFile = SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE;

    // The Maven settings, lazily loaded.
    private Settings fSettings;

    // The effective pom, lazily loaded.
    private MavenProject fProject;

    private final Logger fLogger = Logging.getLogger(EffectivePomLoaderImpl.class);


    /**
     * Specify the pom file to load and any settings file to apply when loading the effective pom.
     *
     * @param pPomFile      The pom file.
     * @param pSettingsFile Any settings file to use. If null the default Maven settings will be
     *                      used.
     *
     * @throws NullPointerException if {@code pPomFile} is null.
     */
    @Override
    public void init(File pPomFile, File pSettingsFile)
    {
        fPomFile = requireNonNull(pPomFile);
        if (pSettingsFile != null)
            fSettingsFile = pSettingsFile;
    }


    /**
     * Get the dependencies from the effective pom. This will cause the effective pom to be loaded
     * from the file specified in {@code init} if that hasn't been done before.
     *
     * @return  A collection of {@code ModuleDependencySpec}, one for each dependency in the
     *          effective pom. The returned collection will never be null but may be empty. The
     *          Maven scope names will be used as configuration names in the specs.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    @Override
    public Collection<ModuleDependencySpec> getDependencies()
    {
        // Load the project containing the effective pom if needed.
        MavenProject aProject = maybeLoadProject();

        // Convert each Maven dependency to a DependencySpec.
        return aProject.getModel()
            .getDependencies()
            .stream()
            .map(EffectivePomLoaderImpl::toDependencySpec)
            .collect(Collectors.toCollection(ArrayList::new));
    }


    /**
     * Get the repositories from the effective pom.
     *
     * @return  A collection of {@code RepositorySpec}, one for each repository in the pom. The
     *          returned collection will never be null but may be empty.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    @Override
    public Collection<RepositorySpec> getRepositories()
    {
        // Get the mirrors from the settings and create a matcher to find out which repositories
        // that have a mirror.
        Settings aSettings = maybeLoadSettings();
        MirrorMatcher aMirrorMatcher = new MirrorMatcher(aSettings.getMirrors());

        // Load the project containing the effective pom if needed.
        MavenProject aProject = maybeLoadProject();

        // Filter out the repositories in the Maven project that have a mirror or are default repos
        // (e.g. Maven Central) and convert all others to a RepositorySpec.
        Collection<RepositorySpec> aRepositories =
            aProject.getRemoteArtifactRepositories()
                .stream()
                .filter(r -> !aMirrorMatcher.matches(r))
                .filter(EffectivePomLoaderImpl::isUserDefinedRepo)
                .map(EffectivePomLoaderImpl::toRepositorySpec)
                .collect(Collectors.toCollection(ArrayList::new));

        // Add a RepositorySpec for each mirror.
        aSettings.getMirrors().forEach(m -> aRepositories.add(toRepositorySpec(m)));

        // Pick up any login credentials from the settings' server definitions.
        aRepositories.forEach(r -> setCredentials(r, aSettings.getServers()));

        return aRepositories;
    }


    /**
     * Get the local repository from the settings file, possibly loading it first.
     *
     * @return  A {@code RepositorySpec} with the path to the local repository.
     *
     * @throws GradleException  if loading the settings file fails.
     */
    @Override
    public RepositorySpec getLocalRepository()
    {
        return new MavenRepositorySpec("local", maybeLoadSettings().getLocalRepository());
    }


    /**
     * Get the group ID from the effective pom. This will cause the effective pom to be loaded
     * from the file specified in {@code init} if that hasn't been done before.
     *
     * @return  The project model's group ID.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    @Override
    public String getGroupId()
    {
        MavenProject aProject = maybeLoadProject();
        return aProject.getModel().getGroupId();
    }


    /**
     * Get the artifact ID from the effective pom. This will cause the effective pom to be loaded
     * from the file specified in {@code init} if that hasn't been done before.
     *
     * @return  The project model's artifact ID.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    @Override
    public String getArtifactId()
    {
        MavenProject aProject = maybeLoadProject();
        return aProject.getModel().getArtifactId();
    }


    /**
     * Get the version string from the effective pom. This will cause the effective pom to be loaded
     * from the file specified in {@code init} if that hasn't been done before.
     *
     * @return  The project model's version string.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    @Override
    public String getVersion()
    {
        MavenProject aProject = maybeLoadProject();
        return aProject.getModel().getVersion();
    }


    /**
     * Load the effective pom as a {@code MavenProject} from the pom file specified in {@code init}
     * if that hasn't be done already.
     *
     * @return  The effective pom as a {@code MavenProject}.
     *
     * @throws GradleException  if loading the project fails.
     */
    @SuppressWarnings("deprecation")
    private MavenProject maybeLoadProject()
    {
        if (fProject != null)
            // Already loaded.
            return fProject;

        fLogger.debug("Loading Pom file {}", fPomFile);

        // Maven project loading based on
        // org.gradle.buildinit.plugins.internal.maven.MavenProjectsCreator#createNow.
        try
        {
            // Create a container to lookup the Maven entities needed for the load.
            PlexusContainer aContainer = createContainer();

            MavenExecutionRequest aExecutionRequest = new DefaultMavenExecutionRequest();
            aExecutionRequest.setSystemProperties(fSystemProperties);

            MavenExecutionRequestPopulator aRequestPopulator =
                aContainer.lookup(MavenExecutionRequestPopulator.class);
            aRequestPopulator.populateFromSettings(aExecutionRequest, maybeLoadSettings());
            aRequestPopulator.populateDefaults(aExecutionRequest);

            ProjectBuildingRequest aBuildingRequest = aExecutionRequest.getProjectBuildingRequest();
            aBuildingRequest.setProcessPlugins(false);

            ProjectBuilder aProjectBuilder = aContainer.lookup(ProjectBuilder.class);
            fProject = aProjectBuilder.build(fPomFile, aBuildingRequest).getProject();
            return fProject;
        }
        catch (PlexusContainerException
            | NoSuchRealmException
            | ComponentLookupException
            | MavenExecutionRequestPopulationException
            | ProjectBuildingException e)
        {
            throw new GradleException("Could not load the Pom file " + fPomFile, e);
        }
    }


    /**
     * Load the Maven settings if that hasn't be done already.
     *
     * @return  The Maven settings.
     *
     * @throws GradleException  if loading the settings fails.
     */
    private Settings maybeLoadSettings()
    {
        if (fSettings != null)
            return fSettings;

        fLogger.debug("Loading Maven settings file {}", fSettingsFile);

        // Settings not loaded, create a request to do so with the user settings file passed to the
        // init() method.
        SettingsBuildingRequest aRequest = new DefaultSettingsBuildingRequest();
        aRequest.setUserSettingsFile(fSettingsFile);
        aRequest.setGlobalSettingsFile(SettingsXmlConfigurationProcessor.DEFAULT_GLOBAL_SETTINGS_FILE);
        aRequest.setSystemProperties(fSystemProperties);

        try
        {
            DefaultSettingsBuilder aBuilder = new DefaultSettingsBuilderFactory().newInstance();
            fSettings = aBuilder.build(aRequest).getEffectiveSettings();
            return fSettings;
        }
        catch (SettingsBuildingException sbe)
        {
            throw new GradleException("Could not load the Maven settings file " + fSettingsFile, sbe);
        }
    }


    /**
     * Create a {@code ModuleDependencySpec} from a Maven {@code Dependency}.
     *
     * @param pMavenDependency  The Maven dependency.
     *
     * @return  A new {@code ModuleDependencySpec}.
     */
    static private ModuleDependencySpec toDependencySpec(Dependency pMavenDependency)
    {
        ModuleDependencySpec aSpec = new ModuleDependencySpec(
                pMavenDependency.getScope(),
                pMavenDependency.getGroupId(),
                pMavenDependency.getArtifactId(),
                pMavenDependency.getVersion());

        aSpec.setClassifier(pMavenDependency.getClassifier());

        String aType = pMavenDependency.getType();
        if (!"jar".equals(aType))
            aSpec.setExtension(aType);

        pMavenDependency.getExclusions().forEach (e -> aSpec.addExclusion(e.getGroupId(), e.getArtifactId()));

        return aSpec;
    }


    /**
     * Create a {@code RepositorySpec} from a Maven {@code ArtifactRepository}.
     *
     * @param pArtifactRepository   The Maven artifact repository.
     *
     * @return  A new {@code RepositorySpec}.
     */
    static private RepositorySpec toRepositorySpec(ArtifactRepository pArtifactRepository)
    {
        RepositorySpec aSpec = new MavenRepositorySpec(
                pArtifactRepository.getId(),
                pArtifactRepository.getUrl());

        Authentication aAuthentication = pArtifactRepository.getAuthentication();
        if (aAuthentication != null)
            aSpec.setCredentials(aAuthentication.getUsername(), aAuthentication.getPassword());

        return aSpec;
    }


    /**
     * Check if an {@code ArtifactRepository} is user defined.
     *
     * @param pArtifactRepository   The artifact repository to check.
     *
     * @return  True if {@code pArtifactRepository} is user defined, false if it is a default
     *          repository (e.g. Maven central).
     */
    static private boolean isUserDefinedRepo(ArtifactRepository pArtifactRepository)
    {
        return !"central".equals(pArtifactRepository.getId());
    }


    /**
     * Create a {@code RepositorySpec} from a Maven {@code Mirror}.
     *
     * @param pMirror   The Maven mirror.
     *
     * @return  A new {@code RepositorySpec}.
     */
    static private RepositorySpec toRepositorySpec(Mirror pMirror)
    {
        return new MavenRepositorySpec(pMirror.getId(), pMirror.getUrl());
    }


    /**
     * Set the credentials for a {@code RepositorySpec} from a Maven {@code Server} whose id matches
     * the repository's name.
     *
     * @param pRepository   The repository.
     * @param pServers      The list to find the matching server in.
     */
    static private void setCredentials(RepositorySpec pRepository, List<Server> pServers)
    {
        if (pRepository.getCredentials() == null)
        {
            for (Server aServer : pServers)
                if (aServer.getId().equals(pRepository.getName()))
                    pRepository.setCredentials(aServer.getUsername(), aServer.getPassword());
        }
    }


    /**
     * Create a container to lookup Maven entities in.
     *
     * @return  A new {@code PlexusContainer}.
     *
     * @throws PlexusContainerException if creating the container fails
     */
    static private PlexusContainer createContainer() throws PlexusContainerException, NoSuchRealmException
    {
        String aRealmID = "plexus.core";
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
        for (String aPropertyName : aOriginal.stringPropertyNames())
            aCopy.put(aPropertyName, aOriginal.getProperty(aPropertyName));

        return aCopy;
    }
}
