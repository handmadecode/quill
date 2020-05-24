/*
 * Copyright 2016, 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.pom

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

import org.myire.quill.common.Projects


/**
 * Gradle plugin for creating pom files outside the context of uploading to a Maven repo. The plugin
 * adds a task called &quot;createPom&quot; when applied to a project.
 */
class PomPlugin implements Plugin<Project>
{
    static final String TASK_NAME = 'createPom'
    static final String PUBLICATION_NAME = TASK_NAME


    @Override
    void apply(Project pProject)
    {
        // Make sure the Maven Publish plugin is applied.
        pProject.plugins.apply(MavenPublishPlugin.class);

        MavenPublication aPublication = createPomFilePublication(pProject)
        if (aPublication != null)
        {
            // Create the pom file task.
            PomFileTask aTask = pProject.tasks.create(TASK_NAME, PomFileTask.class);
            aTask.description = 'Create a stand-alone pom file';
            aTask.setPom(aPublication.pom);

            // Trigger the pom file task when the build task has executed.
            Projects.getTask(pProject, 'build', Task.class)?.finalizedBy(aTask);

            // Disable the tasks created by the publication
            pProject.tasks.matching {it.name.contains(PUBLICATION_NAME.capitalize()) }.each {it.enabled = false }
        }
    }


    /**
     * Create a {@code MavenPublication} with the {@code MavenPom} instance that will be used for
     * creating the pom file.
     *
     * @param pProject  The project to create the publication in.
     *
     * @return  The created publication, or null if the publishing extension isn't available.
     */
    static private MavenPublication createPomFilePublication(Project pProject)
    {
        PublishingExtension aExtension =
                Projects.getExtension(pProject, PublishingExtension.NAME, PublishingExtension.class);
        if (aExtension != null)
        {
            MavenPublication aPublication =
                    aExtension.publications.create(PUBLICATION_NAME, MavenPublication.class);

            // Set the 'java' software component as the source for the pom's content.
            SoftwareComponent aComponent = pProject.components.findByName('java');
            if (aComponent != null)
                aPublication.from(aComponent);

            if (aPublication.respondsTo('setModuleDescriptorGenerator'))
                // Suppress generating comments about Gradle meta data in the pom file.
                aPublication.setModuleDescriptorGenerator(null);

            return aPublication;
        }
        else
        {
            pProject.logger.warn('PublishingExtension not available, cannot generate pom files');
            return null;
        }
    }
}
