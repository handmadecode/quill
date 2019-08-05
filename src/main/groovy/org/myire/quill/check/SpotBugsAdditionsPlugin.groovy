/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * Gradle plugin for adding enhancements to the SpotBugs extension and tasks, as well as configuring
 * them with opinionated defaults. The plugin requires that the SpotBugs plugin has been applied to
 * the project.
 */
class SpotBugsAdditionsPlugin implements Plugin<Project>
{
    @Override
    void apply(Project pProject)
    {
        if (pProject.plugins.hasPlugin('com.github.spotbugs'))
            // Enhance the plugin's extension and tasks.
            new SpotBugsEnhancer(pProject).enhance();
        else
            pProject.logger.warn('SpotBugs plugin not available in project, cannot apply \'org.myire.quill.spotbugs\'');
    }
}
