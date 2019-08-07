/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy;

import java.io.File;
import java.util.Collection;

import org.gradle.api.GradleException;

import org.myire.quill.configuration.ConfigurationSpec;
import org.myire.quill.dependency.ModuleDependencySpec;


/**
 * An {@code IvyModuleLoader} loads an Ivy module from an Ivy file and retrieves (parts of) its
 * contents.

 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public interface IvyModuleLoader
{
    /**
     * Specify the Ivy module file to load and any settings file to apply when loading the file.
     *
     * @param pIvyModuleFile    The Ivy module file.
     * @param pIvySettingsFile  Any settings file to use. If null, the default Ivy settings will be
     *                          used.
     *
     * @throws NullPointerException if {@code pIvyModuleFile} is null.
     */
    void init(File pIvyModuleFile, File pIvySettingsFile);

    /**
     * Get the dependencies from the Ivy module, possibly loading the file first.
     *
     * @return  A collection of {@code ModuleDependencySpec}, one for each dependency in the Ivy
     *          module. The returned collection will never be null but may be empty.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    Collection<ModuleDependencySpec> getDependencies();

    /**
     * Get the configurations from the Ivy module, possibly loading the file first.
     *
     * @return  A collection of {@code ConfigurationSpec}, one for each configuration in the Ivy
     *          module. The returned collection will never be null but may be empty.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    Collection<ConfigurationSpec> getConfigurations();

    /**
     * Get the value of the <i>organisation</i> attribute from the Ivy module's <i>info</i> section,
     * possibly loading the file first.
     *
     * @return  The organisation.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    String getOrganisation();

    /**
     * Get the value of the <i>module</i> attribute from the Ivy module's <i>info</i> section,
     * possibly loading the file first.
     *
     * @return  The module name.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    String getModuleName();

    /**
     * Get the value of the <i>revision</i> attribute from the Ivy module's <i>info</i> section,
     * possibly loading the file first.
     *
     * @return  The revision.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    String getRevision();
}
