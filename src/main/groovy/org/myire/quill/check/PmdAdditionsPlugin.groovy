/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * Gradle plugin for adding enhancements to the Pmd extension and tasks. The plugin will make sure
 * the Pmd plugin is applied to the project, configure its extension and tasks with opinionated
 * defaults, and add some functionality to the tasks.
 */
class PmdAdditionsPlugin implements Plugin<Project>
{
    @Override
    void apply(Project pProject)
    {
        // Apply the Pmd plugin and configure its extension and tasks.
        new PmdEnhancer(pProject).enhance();
    }
}
