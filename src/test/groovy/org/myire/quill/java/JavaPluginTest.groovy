/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.java

import org.junit.Test
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testfixtures.ProjectBuilder


class JavaPluginTest
{
    @Test
    public void pluginAppliesJavaPlugin()
    {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'org.myire.quill.java'

        assertNotNull(project.plugins.findPlugin(JavaPlugin.class));
    }


    @Test
    public void pluginAddsSourcesJarToProject()
    {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'org.myire.quill.java'

        assertTrue(project.tasks.sourcesJar instanceof Jar)
    }


    @Test
    public void pluginAddsJavaDocJarToProject()
    {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'org.myire.quill.java'

        assertTrue(project.tasks.javadocJar instanceof Jar)
    }
}
