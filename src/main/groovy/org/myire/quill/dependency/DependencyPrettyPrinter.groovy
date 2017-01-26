/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency

import org.myire.quill.common.GradlePrettyPrinter


/**
 * Pretty printer for {@code DependencySpec}.
 */
class DependencyPrettyPrinter extends GradlePrettyPrinter
{
    /**
     * Create a new {@code DependencyPrettyPrinter}.
     *
     * @param pWriter   The writer to delegate the printing to.
     *
     * @throws NullPointerException if {@code pWriter} is null.
     */
    DependencyPrettyPrinter(PrintWriter pWriter)
    {
        super(pWriter);
    }


    /**
     * Pretty print a collection of dependency specifications on Gradle map notation.
     *
     * @param pDependencies The dependencies to print.
     */
    void printDependencies(Collection<DependencySpec> pDependencies)
    {
        if (pDependencies.empty)
            return;

        printClosureStart('dependencies');

        for (aDependency in pDependencies)
            printDependency(aDependency);

        printClosureEnd();
    }


    /**
     * Pretty print a {@code DependencySpec} on map notation.
     *
     * @param pDependency   The dependency.
     */
    void printDependency(DependencySpec pDependency)
    {
        // Must enclose map notation in parentheses if exclusions and/or artifacts will follow in a
        // closure.
        boolean aWithClosure = !(pDependency.exclusions.empty && pDependency.artifacts.empty);
        if (aWithClosure)
            printNameAndArgs(pDependency.configuration, pDependency.asMapNotation());
        else
            printKeyValue(pDependency.configuration, pDependency.asMapNotation());

        // Closure with exclusions and/or artifacts.
        if (aWithClosure)
        {
            // Closure has no name.
            printClosureStart(null);

            // One exclusion per line.
            pDependency.exclusions.each { printKeyValue('exclude', it.asMapNotation()) }

            // Artifacts as closures.
            pDependency.artifacts.each { printArtifact(it) }

            printClosureEnd();
        }
    }


    /**
     * Pretty print an {@code ArtifactSpec} on closure format.
     *
     * @param pArtifact The artifact.
     */
    void printArtifact(ArtifactSpec pArtifact)
    {
        printClosureStart('artifact');
        pArtifact.mapping.each { printKeyValue(it.key, quote(it.value)) }
        printClosureEnd();
    }
}
