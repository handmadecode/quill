/*
 * Copyright 2015-2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.myire.quill.check.CheckstyleAdditionsPlugin
import org.myire.quill.check.FindBugsAdditionsPlugin
import org.myire.quill.check.JDependAdditionsPlugin
import org.myire.quill.check.PmdAdditionsPlugin
import org.myire.quill.cobertura.CoberturaPlugin
import org.myire.quill.cpd.CpdPlugin
import org.myire.quill.dashboard.DashboardPlugin
import org.myire.quill.ivy.IvyPlugin
import org.myire.quill.java.JavaAdditionsPlugin
import org.myire.quill.junit.JUnitAdditionsPlugin
import org.myire.quill.maven.MavenImportPlugin
import org.myire.quill.meta.ProjectMetaDataPlugin
import org.myire.quill.pom.PomPlugin
import org.myire.quill.scent.ScentPlugin


/**
 * Gradle project plugin that applies all Quill plugins.
 */
class QuillAllPlugin implements Plugin<Project>
{
    @Override
    void apply(Project pProject)
    {
        // Make sure all Quill plugins are applied.
        pProject.plugins.apply(CheckstyleAdditionsPlugin.class);
        pProject.plugins.apply(FindBugsAdditionsPlugin.class);
        pProject.plugins.apply(JDependAdditionsPlugin.class);
        pProject.plugins.apply(PmdAdditionsPlugin.class);
        pProject.plugins.apply(CoberturaPlugin.class);
        pProject.plugins.apply(CpdPlugin.class);
        pProject.plugins.apply(DashboardPlugin.class);
        pProject.plugins.apply(IvyPlugin.class);
        pProject.plugins.apply(JavaAdditionsPlugin.class);
        pProject.plugins.apply(JUnitAdditionsPlugin.class);
        pProject.plugins.apply(MavenImportPlugin.class);
        pProject.plugins.apply(ProjectMetaDataPlugin.class);
        pProject.plugins.apply(PomPlugin.class);
        pProject.plugins.apply(ScentPlugin.class);
    }
}
