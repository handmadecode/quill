/*
 * Copyright 2017-2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import java.util.Objects;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact;


/**
 * Dependency related utility methods.
 */
public final class Dependencies
{
    /**
     * Private constructor to disallow instantiations of utility method class.
     */
    private Dependencies()
    {
        // Empty default ctor, defined to override access scope.
    }


    /**
     * Find a project with name, group and version matching the corresponding properties of a
     * {@code ModuleDependencySpec}. The project will be looked for starting with the specified
     * project, and then in all sub-projects of the starting project, then in all sub-projects of
     * the starting project's ancestors.
     *
     * @param pDependencySpec   The dependency spec to match the projects with.
     * @param pStartProject     The project to start looking in.
     *
     * @return  The first project that matches {@code pDependencySpec}, or null if no project
     *          matches.
     *
     * @throws NullPointerException if {@code pDependencySpec} is null.
     */
    static public Project findMatchingProject(ModuleDependencySpec pDependencySpec, Project pStartProject)
    {
        Project aProject = pStartProject;
        while (aProject != null)
        {
            if (matches(pDependencySpec, aProject))
                return aProject;

            // Look for a match in the project's sub-projects.
            for (Project aSubProject : aProject.getSubprojects())
                if (matches(pDependencySpec, aSubProject))
                    return aSubProject;

            // None of the sub-projects are a match, continue looking in the parent project.
            aProject = aProject.getParent();
        }

        return null;
    }


    /**
     * Add a dependency on an external module to a project.
     *
     * @param pProject          The project to add the dependency to.
     * @param pDependencySpec   The specification of the dependency to add.
     *
     * @return  True if the dependency was added, false if it's configuration doesn't exist in the
     *          project and it therefore wasn't added.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static public boolean addDependency(Project pProject, ModuleDependencySpec pDependencySpec)
    {
        Configuration aConfiguration = pProject.getConfigurations().findByName(pDependencySpec.getConfiguration());
        if (aConfiguration == null)
            // The dependency spec's configuration name is invalid.
            return false;

        // Create the dependency and set the common dependency values from the spec.
        Dependency aDependency = pProject.getDependencies().create(pDependencySpec.toDependencyNotation());
        copyValues(pDependencySpec, aDependency);

        // Copy the external module dependency values.
        if (aDependency instanceof ExternalDependency)
        {
            ((ExternalDependency) aDependency).setForce(pDependencySpec.isForce());

            if (aDependency instanceof ExternalModuleDependency)
                ((ExternalModuleDependency) aDependency).setChanging(pDependencySpec.isChanging());
        }

        // Add the dependency to its configuration.
        aConfiguration.getDependencies().add(aDependency);

        return true;
    }


    /**
     * Add a dependency on an another project to a project.
     *
     * @param pProject          The project to add the dependency to.
     * @param pDependencySpec   The specification of the dependency to add.
     *
     * @return  True if the dependency was added, false if it's configuration doesn't exist in the
     *          project and it therefore wasn't added.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static public boolean addDependency(Project pProject, ProjectDependencySpec pDependencySpec)
    {
        Configuration aConfiguration = pProject.getConfigurations().findByName(pDependencySpec.getConfiguration());
        if (aConfiguration == null)
            // The dependency spec's configuration name is invalid.
            return false;

        // Create the dependency and set the common dependency values from the spec.
        Dependency aDependency = pProject.getDependencies().project(pDependencySpec.toMap());
        copyValues(pDependencySpec, aDependency);

        // Add the dependency to its configuration.
        aConfiguration.getDependencies().add(aDependency);

        return true;
    }


    /**
     * Check if a {@code ModuleDependencySpec} has the same group, name, and version as a
     * {@code Project}.
     *
     * @param pDependencySpec   The dependency.
     * @param pProject          The project.
     *
     * @return  True if the two entities match, false if not.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static private boolean matches(ModuleDependencySpec pDependencySpec, Project pProject)
    {
        return pDependencySpec.matches(
            Objects.toString(pProject.getGroup(), null),
            pProject.getName(),
            Objects.toString(pProject.getVersion(), null));
    }


    /**
     * Copy the values from a {@code DependencySpec} to a {@code Dependency}.
     *
     * @param pDependencySpec   The values to copy.
     * @param pDependency       The instance to copy into.
     *
     * @throws NullPointerException if {@code pDependencySpec} is null.
     */
    static private void copyValues(DependencySpec pDependencySpec, Dependency pDependency)
    {
        if (pDependency instanceof ModuleDependency)
        {
            // Transitivity, exclusions and artifacts are applicable for a ModuleDependency.
            ModuleDependency aModuleDependency = (ModuleDependency) pDependency;
            aModuleDependency.setTransitive(pDependencySpec.isTransitive());
            pDependencySpec.forEachExclusion(e -> aModuleDependency.exclude(e.toMap()));
            pDependencySpec.forEachArtifact(a -> aModuleDependency.addArtifact(toDefaultDependencyArtifact(a)));
        }
    }


    /**
     * Create a {@code DefaultDependencyArtifact} from the values in an {@code ArtifactSpec}.
     *
     * @param pArtifactSpec The values to populate the {@code DefaultDependencyArtifact} with.
     *
     * @return  A new {@code DefaultDependencyArtifact}.
     */
    static private DefaultDependencyArtifact toDefaultDependencyArtifact(ArtifactSpec pArtifactSpec)
    {
        return new DefaultDependencyArtifact(
            pArtifactSpec.getName(),
            pArtifactSpec.getType(),
            pArtifactSpec.getExtension(),
            pArtifactSpec.getClassifier(),
            pArtifactSpec.getUrl()
        );
    }
}
