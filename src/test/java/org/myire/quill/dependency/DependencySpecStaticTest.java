/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.myire.quill.common.PrettyPrintableTests.assertPrintAction;


/**
 * Unit tests for static methods in {@code DependencySpec}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class DependencySpecStaticTest
{
    /**
     * {@code prettyPrintDependencies()} should print a closure with all dependency specifications
     * passed to the method.
     */
    @Test
    public void prettyPrintDependenciesPrintsTheExpectedClosure()
    {
        // Given
        List<DependencySpec> aSpecs = new ArrayList<>();

        String aProjectConfig = "compile";
        String aPath = ":root:sub:subsub";
        aSpecs.add(new ProjectDependencySpec(aProjectConfig, aPath));

        String aModuleConfig = "test";
        String aGroup = "org.test", aModuleName = "tryouts", aVersion = "47.11";
        String aExcludedGroup = "com.flawed", aExcludedModuleName = "crappy";
        ModuleDependencySpec aSpec = new ModuleDependencySpec(aModuleConfig, aGroup, aModuleName, aVersion);
        aSpec.addExclusion(aExcludedGroup, aExcludedModuleName);
        aSpecs.add(aSpec);

        // Then
        String[] aExpected = {
            "dependencies",
            "{",
            "  " + aProjectConfig + " project('" + aPath + "')",
            "  " + aModuleConfig + "('" + aGroup + ':' + aModuleName + ':' + aVersion + "')",
            "  {",
            "    exclude group: '" + aExcludedGroup + "', module: '" + aExcludedModuleName + '\'',
            "  }",
            "}"
        };

        assertPrintAction(p -> DependencySpec.prettyPrintDependencies(p, aSpecs), aExpected);
    }
}
