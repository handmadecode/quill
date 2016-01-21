/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common

import org.gradle.api.Project


/**
 * Base class for all entities that hold a reference to a Gradle project.
 */
class ProjectAware
{
    private final Project fProject;


    /**
     * Create a new {@code ProjectAware}.
     *
     * @param pProject  The project this entity belongs to.
     */
    ProjectAware(Project pProject)
    {
        fProject = pProject;
    }


    /**
     * Get this entity's associated project.
     *
     * @return  The project.
     */
    Project getProject()
    {
        return fProject;
    }
}
