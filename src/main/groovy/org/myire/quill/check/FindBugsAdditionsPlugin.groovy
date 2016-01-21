/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * Gradle plugin for adding enhancements to the FindBugs extension and tasks. The plugin will make
 * sure the FindBugs plugin is applied to the project and also configure its extension and tasks
 * with opinionated defaults.
 */
class FindBugsAdditionsPlugin implements Plugin<Project>
{
    @Override
    void apply(Project pProject)
    {
        // Apply the FindBugs plugin and configure its extension and tasks.
        new FindBugsEnhancer(pProject).enhance();
    }
}
