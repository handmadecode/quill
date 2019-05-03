/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import java.util.Collections;
import java.util.Map;
import static java.util.Objects.requireNonNull;

import org.gradle.api.Project;

import org.myire.quill.common.GradlePrettyPrinter;
import org.myire.quill.common.PrettyPrintable;


/**
 * Specification of a dependency on a Gradle project in the current build.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ProjectDependencySpec extends DependencySpec
{
    static private final String ATTRIBUTE_PATH ="path";

    private final String fProjectPath;


    /**
     * Create a new {@code ProjectDependencySpec}.
     *
     * @param pConfiguration    The name of the dependency's configuration.
     * @param pProjectPath      The path to the project.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    public ProjectDependencySpec(String pConfiguration, String pProjectPath)
    {
        super(pConfiguration);
        fProjectPath = requireNonNull(pProjectPath);
    }


    public String getProjectPath()
    {
        return fProjectPath;
    }


    public Map<String, String> toMap()
    {
        return Collections.singletonMap(ATTRIBUTE_PATH, fProjectPath);
    }


    @Override
    public String toDependencyNotation()
    {
        return "project(" + PrettyPrintable.quote(fProjectPath) + ")";
    }


    @Override
    public boolean addTo(Project pProject)
    {
        return Dependencies.addDependency(pProject, this);
    }


    @Override
    public void prettyPrint(GradlePrettyPrinter pPrinter)
    {
        if (hasNonDefaultAttribute())
        {
            // Dependency with non-default attribute(s), print those attributes in a nameless
            // closure.
            pPrinter.printMethodCall(getConfiguration(), toDependencyNotation(), false, true);
            pPrinter.printClosure(null, this::printClosureBody);
        }
        else
            // Only default attributes, simply print the configuration name and project dependency
            // notation.
            pPrinter.printMethodCall(getConfiguration(), toDependencyNotation(), false, false);
    }
}
