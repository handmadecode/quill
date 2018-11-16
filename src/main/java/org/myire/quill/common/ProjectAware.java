/*
 * Copyright 2015, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import static java.util.Objects.requireNonNull;

import org.gradle.api.Project;


/**
 * Base class for all entities that hold a reference to a Gradle project.
 */
public class ProjectAware
{
    private final Project fProject;


    /**
     * Create a new {@code ProjectAware}.
     *
     * @param pProject  The project this entity belongs to.
     *
     * @throws NullPointerException if {@code pProject} is null.
     */
    public ProjectAware(Project pProject)
    {
        fProject = requireNonNull(pProject);
    }


    /**
     * Get this entity's associated project.
     *
     * @return  The project, never null.
     */
    public Project getProject()
    {
        return fProject;
    }
}
