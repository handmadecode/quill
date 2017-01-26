/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact;


/**
 * Dependency related utility methods.
 */
class Dependencies
{
    /**
     * Find a project with name, group and version matching the corresponding properties of a
     * {@code DependencySpec}. The project will be looked for in all subprojects of a starting
     * project as well as in all subprojects of that project's ancestors.
     *
     * @param pProject          The project to start looking in.
     * @param pDependencySpec   The dependency spec to match the projects with.
     *
     * @return  The first project that matches {@code pDependencySpec}, or null if no project
     *          matches.
     */
    static Project findProjectForDependency(Project pProject, DependencySpec pDependencySpec)
    {
        Project aProject = pProject;
        while (aProject)
        {
            if (matches(pDependencySpec, aProject))
                return aProject;

            Project aSubProject = aProject.subprojects.find { matches(pDependencySpec, it) }
            if (aSubProject)
                return aSubProject;

            aProject = aProject.parent;
        }

        return null;
    }


    /**
     * Check if a {@code DependencySpec} has the same group, name, and version as a {@code Project}.
     *
     * @param pDependencySpec   The dependency.
     * @param pProject          The project.
     *
     * @return  True if the two entities match, false if not.
     */
    static boolean matches(DependencySpec pDependencySpec, Project pProject)
    {
        return pDependencySpec.matches(
                Objects.toString(pProject.group, null),
                pProject.name,
                Objects.toString(pProject.version, null));
    }


    /**
     * Add a dependency to a project.
     *
     * @param pProject          The project.
     * @param pDependencySpec   The specification of the dependency to add.
     *
     * @return  True if the dependency was added, false if its configuration doesn't exist in the
     *          project and it therefore wasn't added.
     */
    static boolean addDependency(Project pProject, DependencySpec pDependencySpec)
    {
        // Make sure the dependency's configuration exists.
        Configuration aConfig = pProject.configurations.findByName(pDependencySpec.configuration);
        if (aConfig == null)
        {
            pProject.logger.warn(
                    'Configuration \'{}\' does not exist, cannot add dependency {}',
                    pDependencySpec.configuration,
                    pDependencySpec.asStringNotation());

            return false;
        }

        pProject.logger.debug('Adding dependency {} to configuration {}',
                              pDependencySpec.asStringNotation(),
                              pDependencySpec.configuration);

        // Create the dependency and add it to the project's dependency handler.
        Dependency aDependency = createDependency(pProject.dependencies, pDependencySpec);

        // Add the dependency to the configuration.
        aConfig.dependencies.add(aDependency);

        return true;
    }


    /**
     * Create a dependency and add it to a dependency handler.
     *
     * @param pHandler          The dependency handler.
     * @param pDependencySpec   The specification of the dependency to create.
     *
     * @return  The created {@code Dependency}.
     */
    static private Dependency createDependency(DependencyHandler pHandler, DependencySpec pDependencySpec)
    {
        if (pDependencySpec.isProjectDependency())
            return pHandler.project(pDependencySpec.mapping);

        // Not a project dependency, create a normal dependency.
        Dependency aDependency = pHandler.add(pDependencySpec.configuration, pDependencySpec.mapping);
        if (aDependency instanceof ModuleDependency)
        {
            // Dependency exclusions and artifacts are applicable for a ModuleDependency.
            ModuleDependency aModuleDependency = (ModuleDependency) aDependency;

            for (aExclusion in pDependencySpec.exclusions)
                aModuleDependency.exclude(aExclusion.mapping);

            for (aArtifact in pDependencySpec.artifacts)
                aModuleDependency.addArtifact(new DefaultDependencyArtifact(aArtifact.mapping));
        }

        return aDependency;
    }
}
