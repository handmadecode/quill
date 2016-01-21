/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.meta

import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * Project plugin that adds a {@code ProjectMetaDataExtension} and a
 * {@code SemanticVersionExtension} to its project.
 */
class ProjectMetaDataPlugin implements Plugin<Project>
{
    static final String PROJECT_META_EXTENSION_NAME = 'projectMetaData'
    static final String SEMANTIC_VERSION_EXTENSION_NAME = 'semanticVersion'


    @Override
    void apply(Project pProject)
    {
        pProject.extensions.create(PROJECT_META_EXTENSION_NAME, ProjectMetaDataExtension.class, pProject);
        pProject.extensions.create(SEMANTIC_VERSION_EXTENSION_NAME, SemanticVersionExtension.class, pProject);
    }
}
