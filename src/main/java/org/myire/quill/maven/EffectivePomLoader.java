/*
 * Copyright 2017-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven;

import java.io.File;
import java.util.Collection;

import org.gradle.api.GradleException;

import org.myire.quill.dependency.ModuleDependencySpec;
import org.myire.quill.repository.RepositorySpec;


/**
 * An {@code EffectivePomLoader} loads the effective pom from a Maven pom file and retrieves (parts
 * of) its contents.
 */
public interface EffectivePomLoader
{
    /**
     * Specify the pom file to load and any settings file to apply when loading the effective pom.
     *
     * @param pPomFile      The pom file.
     * @param pSettingsFile Any settings file to use. If null the default Maven settings will be
     *                      used.
     *
     * @throws NullPointerException if {@code pPomFile} is null.
     */
    void init(File pPomFile, File pSettingsFile);

    /**
     * Get the dependencies from the effective pom, possibly loading it first.
     *
     * @return  A collection of {@code ModuleDependencySpec}, one for each dependency in the
     *          effective pom. The returned collection will never be null but may be empty. The
     *          Maven scope names will be used as configuration names in the specs.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    Collection<ModuleDependencySpec> getDependencies();

    /**
     * Get the repositories from the effective pom, possibly loading it first.
     *
     * @return  A collection of {@code RepositorySpec}, one for each repository in the pom. The
     *          returned collection will never be null but may be empty.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    Collection<RepositorySpec> getRepositories();

    /**
     * Get the local repository from the settings file, possibly loading it first.
     *
     * @return  A {@code RepositorySpec} with the path to the local repository.
     *
     * @throws GradleException  if loading the settings file fails.
     */
    RepositorySpec getLocalRepository();

    /**
     * Get the group ID from the effective pom, possibly loading it first.
     *
     * @return  The project model's group ID.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    String getGroupId();

    /**
     * Get the artifact ID from the effective pom, possibly loading it first.
     *
     * @return  The project model's artifact ID.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    String getArtifactId();

    /**
     * Get the version string from the effective pom, possibly loading it first.
     *
     * @return  The project model's version string.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    String getVersion();
}
