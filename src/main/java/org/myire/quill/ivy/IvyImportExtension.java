/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy;

import java.io.File;
import static java.util.Objects.requireNonNull;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;

import org.myire.quill.common.ProjectAware;


/**
 * Gradle project extension for specifying an Ivy settings file and the Ivy libraries to use when
 * importing from an Ivy file.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class IvyImportExtension extends ProjectAware
{
    static final String EXTENSION_NAME = "ivyImport";

    static private final String DEFAULT_IVY_VERSION = "2.5.0";
    static private final String DEFAULT_WILDCARD_CONFIGURATION = "compile";


    private final Configuration fConfiguration;

    private File fIvySettingsFile;
    private String fIvyVersion = DEFAULT_IVY_VERSION;
    private FileCollection fIvyClassPath;
    private String fWildcardConfiguration = DEFAULT_WILDCARD_CONFIGURATION;


    /**
     * Create a new {@code IvyImportExtension}.
     *
     * @param pProject          The project that owns the extension.
     * @param pConfiguration    The {@code ivyImport} configuration.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    public IvyImportExtension(Project pProject, Configuration pConfiguration)
    {
        super(pProject);

        // The default value for the Ivy class path is the configuration.
        fConfiguration = requireNonNull(pConfiguration);
        fIvyClassPath = fConfiguration;
    }


    /**
     * Get the Ivy settings file to use when importing from an Ivy file. If this file is null, the
     * default Ivy settings will be used.
     *
     * @return  The Ivy settings file specification, possibly null.
     */
    public File getSettingsFile()
    {
        return fIvySettingsFile;
    }


    /**
     * Set the Ivy settings file to use when importing from an Ivy file. This file will be resolved
     * relative to the project directory.
     *
     * @param pFile The settings file, possibly null
     */
    public void setSettingsFile(Object pFile)
    {
        fIvySettingsFile = pFile != null ? getProject().file(pFile) : null;
    }


    /**
     * Get the version of the Ivy libraries to use when importing from an Ivy file.
     *
     * @return  The Ivy version string, never null.
     */
    public String getIvyVersion()
    {
        return fIvyVersion;
    }


    /**
     * Set the version of the Ivy libraries to use when importing from an Ivy file.
     *
     * @param pIvyVersion   The Ivy library version. Passing null will cause the default value to be
     *                      returned from {@code getIvyVersion}.
     */
    public void setIvyVersion(String pIvyVersion)
    {
        fIvyVersion = pIvyVersion != null ? pIvyVersion : DEFAULT_IVY_VERSION;
    }


    /**
     * Get the class path containing the Ivy related classes used by the Ivy import. The initial
     * value is the {@code ivyImport} configuration.
     *
     * @return  The Ivy classpath.
     */
    public FileCollection getIvyClassPath()
    {
        return fIvyClassPath;
    }


    public void setIvyClassPath(FileCollection pIvyClassPath)
    {
        fIvyClassPath = pIvyClassPath != null ? pIvyClassPath : fConfiguration;
    }


    /**
     * Get the name of the configuration to map the wildcard Ivy configuration to.
     *
     * @return  The wildcard configuration string, never null.
     */
    public String getWildcardConfiguration()
    {
        return fWildcardConfiguration;
    }


    /**
     * Set the name of the configuration to map the wildcard Ivy configuration to.
     *
     * @param pWildcardConfiguration    The wildcard configuration name. Passing null will cause the
     *                                  default value to be returned from
     *                                  {@code getWildcardConfiguration}.
     */
    public void setWildcardConfiguration(String pWildcardConfiguration)
    {
        fWildcardConfiguration = pWildcardConfiguration != null ? pWildcardConfiguration : DEFAULT_WILDCARD_CONFIGURATION;
    }
}
