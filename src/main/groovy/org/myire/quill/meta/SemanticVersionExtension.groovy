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
 * Project extension holding the individual components of a semantic version number. See
 * <a href="http://semver.org/">.
 */
class SemanticVersionExtension extends ProjectAware
{
    /** The major component of this semantic version number. */
    int major

    /** The minor component of this semantic version number. */
    int minor

    /** The patch component of this semantic version number. */
    int patch

    /** Zero or more pre-release identifiers. Null means no pre-release identifiers.  */
    String[] preReleaseIdentifiers

    /** Zero or more build meta data strings. Null means no  build meta data.  */
    String[] buildMetaData


    SemanticVersionExtension(Project pProject)
    {
        super(pProject);
    }


    /**
     * Load the semantic version values from a JSON file.
     *
     * @param pJsonFile A specification of the file to load the values from.
     *
     * @return  This instance.
     */
    SemanticVersionExtension from(Object pJsonFile)
    {
        File aResolvedFile = project.file(pJsonFile);
        if (aResolvedFile.canRead())
        {
            project.logger.debug('Loading semantic version number from \'{}\'', aResolvedFile.absolutePath);

            def aValues = new JsonSlurper().parse(aResolvedFile);
            if (aValues.major != null)
                major = aValues.major;
            if (aValues.minor != null)
                minor = aValues.minor;
            if (aValues.patch != null)
                patch = aValues.patch;
            if (aValues.preReleaseIdentifiers != null)
                preReleaseIdentifiers = aValues.preReleaseIdentifiers;
            if (aValues.buildMetaData != null)
                buildMetaData = aValues.buildMetaData;
        }
        else
            project.logger.warn('File \'{}\' is not readable, using default values for the semantic version number',
                                aResolvedFile.absolutePath);

        return this;
    }


    /**
     * Set the project's {@code version} property to this instance's short version string.
     *
     * @return  This instance.
     */
    SemanticVersionExtension applyShortVersionToProject()
    {
        project.setVersion(getShortVersionString());
        return this;
    }


    /**
     * Set the project's {@code version} property to this instance's long version string.
     *
     * @return  This instance.
     */
    SemanticVersionExtension applyLongVersionToProject()
    {
        project.setVersion(getLongVersionString());
        return this;
    }


    /**
     * Get the main part of this semantic version number as a string. The main part is the major,
     * minor, and patch numbers, where the patch number is omitted if it has the value 0.
     *<p>
     * Examples: &quot;2.1.4&quot;, &quot;3.5&quot;, &quot;0.0.1&quot;, &quot;1.0&quot;
     *
     * @return  The main part of the semantic version number as a string, never null.
     */
    public String getShortVersionString()
    {
        def aVersionString = major + '.' + minor;
        if (patch != 0)
            aVersionString += '.' + patch;

        return aVersionString;
    }


    /**
     * Get the all parts of this semantic version number as a string. The main part is always
     * present, and it is optionally followed by the pre-release labels and/or build meta data.
     * The pre-release labels are preceded with a dash ('-') if they exist, and multiple labels are
     * separated with dots ('.'). The build meta data are preceded with a plus ('+') if they exist,
     * and meta data are separated with dots ('.').
     *<p>
     * Examples: &quot;2.1.4-alpha.2&quot;, &quot;3.5-rc.1+512&quot;, &quot;1.0.9+20150106T174539&quot;
     *
     * @return  The entire semantic version number as a string, never null.
     */
    public String getLongVersionString()
    {
        return getShortVersionString() +
               toDotSeparatedString(preReleaseIdentifiers, '-') +
               toDotSeparatedString(buildMetaData, '+');
    }


    /**
     * Create a dot separated string from an array of strings.
     *
     * @param pValues   The strings to concatenate with dots in between.
     * @param pPrefix   The prefix to prepend the string with.
     *
     * @return  The dot-separated concatenation of the strings in the array.
     */
    static private String toDotSeparatedString(String[] pValues, String pPrefix)
    {
        if (pValues == null || pValues.length == 0)
            return "";

        def aResult = pPrefix + pValues[0];
        for (int i=1; i<pValues.length; i++)
            aResult += '.' + pValues[i];

        return aResult;
    }
}
