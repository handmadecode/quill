/*
 * Copyright 2015-2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.meta

import groovy.json.JsonSlurper

import org.gradle.api.Project

import org.myire.quill.common.ProjectAware


/**
 * Project extension for specifying the short name, long name, group, description, and main package
 * of a project.
 */
class ProjectMetaDataExtension extends ProjectAware
{
    /** The project's short name, for example &quot;Quill&quot;. */
    String shortName

    /** The project's long name, for example &quot;Quill Gradle plugins&quot;. */
    String longName

    /** The project's group, for example &quot;org.myire&quot;. */
    String group

    /**
     * The project's description,for example &quot;Additional tasks and opinionated defaults for
     * Gradle build scripts&quot;.
     */
    String description

    /**
     * The main package name on a format suitable for a {@code java.lang.Package} specification in
     * a manifest file, for example &quot;/org/myire/quill&quot;.
     */
    String mainPackage


    ProjectMetaDataExtension(Project pProject)
    {
        super(pProject);
        shortName = pProject.name;
        longName = pProject.name;
        group = pProject.group;
        description = pProject.description;
    }


    /**
     * Load the project meta data values from a JSON file.
     *
     * @param pJsonFile A specification of the file to load the values from.
     *
     * @return  This instance.
     */
    ProjectMetaDataExtension from(String pJsonFile)
    {
        File aResolvedFile = project.file(pJsonFile)
        if (aResolvedFile.canRead())
        {
            project.logger.debug('Loading project meta data from \'{}\'',  aResolvedFile.absolutePath);

            def aValues = new JsonSlurper().parse(aResolvedFile);
            if (aValues.shortName != null)
                shortName = aValues.shortName;
            if (aValues.longName != null)
                longName = aValues.longName;
            if (aValues.group != null)
                group = aValues.group;
            if (aValues.description != null)
                description = aValues.description;
            if (aValues.mainPackage != null)
                mainPackage = aValues.mainPackage;
        }
        else
            project.logger.warn('File \'{}\' is not readable, using default values for the project meta data',
                                aResolvedFile.absolutePath);

        return this;
    }


    /**
     * Set the project's {@code group} property to this instance's {@code group} property, given
     * that the latter is non-null.
     *
     * @return  This instance.
     */
    ProjectMetaDataExtension applyGroupToProject()
    {
        if (group)
            project.setGroup(group);

        return this;
    }
}
