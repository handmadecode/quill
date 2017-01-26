/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency

import org.gradle.api.Project


/**
 * Specification of a dependency, for example an external module dependency or a project dependency.
 */
class DependencySpec extends MapBasedSpec
{
    /**
     * The name of the dependency's configuration.
     */
    String configuration;

    /**
     * The dependency's exclusions.
     */
    final List<ExclusionSpec> exclusions = [];

    /**
     * The dependency's artifacts.
     */
    final List<ArtifactSpec> artifacts = [];


    /**
     * Create a new {@code DependencySpec}.
     *
     * @param pConfiguration    The name of the dependency's configuration.
     * @param pGroup            The dependency's group.
     * @param pName             The dependency's name.
     * @param pVersion          The dependency's version.
     */
    DependencySpec(String pConfiguration, String pGroup, String pName, String pVersion)
    {
        super(group: pGroup, name: pName, version: pVersion);
        configuration = pConfiguration;
    }


    /**
     * Set the value for the {@code classifier} key.
     *
     * @param pClassifier   The classifier value. If this value is null the key will be removed from
     *                      the specification's mapping.
     */
    void setClassifier(String pClassifier)
    {
        putValue('classifier', pClassifier);
    }


    /**
     * Set the value for the {@code ext} key.
     *
     * @param pExtension    The extension value. If this value is null the key will be removed from
     *                      the specification's mapping.
     */
    void setExtension(String pExtension)
    {
        putValue('ext', pExtension);
    }


    /**
     * Set the value for the {@code changing} key.
     *
     * @param pIsChanging   The changing value.
     */
    void setChanging(boolean pIsChanging)
    {
        putBoolean('changing', pIsChanging);
    }


    /**
     * Set the value for the {@code force} key.
     *
     * @param pIsForce  The force value.
     */
    void setForce(boolean pIsForce)
    {
        putBoolean('force', pIsForce);
    }


    /**
     * Set the value for the {@code transitive} key.
     *
     * @param pIsTransitive The transitive value.
     */
    void setTransitive(boolean pIsTransitive)
    {
        putBoolean('transitive', pIsTransitive);
    }


    /**
     * Set the values of a project dependency. This will remove any keys related to external module
     * dependencies.
     *
     * @param pProject  The project of this dependency.
     */
    void setProject(Project pProject)
    {
        mapping['path'] = pProject.path;
        mapping.remove('group');
        mapping.remove('name');
        mapping.remove('version');
    }


    /**
     * Check if the keys indicating a project dependency have values.
     *
     * @return  True if the project dependency key(s) have values, false if not.
     */
    boolean isProjectDependency()
    {
        return mapping['path'] != null;
    }


    /**
     * Add an exclusion specification to this dependency.
     *
     * @param pGroup    The exclusion's group.
     * @param pModule   The exclusion's module.
     */
    void addExclusion(String pGroup, String pModule)
    {
        exclusions.add(new ExclusionSpec(pGroup, pModule));
    }


    /**
     * Add an artifact specification to this dependency.
     *
     * @param pName         The name of the artifact.
     * @param pType         The artifact's type. Normally the same value as the extension, e.g.
     *                      &quot;jar&quot;.
     * @param pExtension    The artifact's extension. Normally the same value as the type.
     * @param pClassifier   The artifact's classifier.
     * @param pUrl          The URL under which the artifact can be retrieved.
     */
    void addArtifact(String pName, String pType, String pExtension, String pClassifier, String pUrl)
    {
        artifacts.add(new ArtifactSpec(pName, pType, pExtension, pClassifier, pUrl));
    }


    /**
     * Get the map notation for this specification.
     *
     * @return  A string on the format &quot;key: 'value', ...&quot;.
     */
    String asMapNotation()
    {
        if (isProjectDependency())
            return 'project(' + super.asMapNotation() + ')';
        else
            return super.asMapNotation();
    }


    /**
     * Get the string notation for this dependency.
     *
     * @return  A string on the format &quot;group:name:version:classifier@extension&quot;.
     */
    String asStringNotation()
    {
        StringBuilder aNotation = new StringBuilder(256);
        if (isProjectDependency())
        {
            aNotation.append('project(\'')
            aNotation.append(mapping['path']);
            aNotation.append('\')');
        }
        else
        {
            if (mapping['group'])
                aNotation.append(mapping['group']).append(':');

            // Name must always be present.
            aNotation.append(mapping['name']);

            if (mapping['version'])
                aNotation.append(':').append(mapping['version']);
            if (mapping['classifier'])
                aNotation.append(':').append(mapping['classifier']);
            if (mapping['ext'])
                aNotation.append('@').append(mapping['ext']);
        }

        return aNotation;
    }


    /**
     * Check if this specification has a certain name, group, and version.
     *
     * @param pGroup    The group to match, or null to ignore the group value.
     * @param pName     The name to match.
     * @param pVersion  The version to match, or null to ignore the group value.
     *
     * @return  True if this instance matches the specified value(s), false if not.
     */
    boolean matches(String pGroup, String pName, String pVersion)
    {
        return pName == mapping['name'] &&
               (pGroup == null || pGroup == mapping['group']) &&
               (pVersion == null || pVersion == mapping['version']);
    }
}
