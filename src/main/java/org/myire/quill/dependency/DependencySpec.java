/*
 * Copyright 2017-2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import static java.util.Objects.requireNonNull;

import org.gradle.api.Project;

import org.myire.quill.common.GradlePrettyPrinter;
import org.myire.quill.common.PrettyPrintable;


/**
 * Abstract base class for dependency specifications.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
abstract public class DependencySpec implements PrettyPrintable
{
    static private final String CLOSURE_DEPENDENCIES = "dependencies";
    static private final String ATTRIBUTE_TRANSITIVE = "transitive";


    private String fConfiguration;
    private boolean fTransitive = true;

    // The dependency's exclusions.
    private  final List<ExclusionSpec> fExclusions = new ArrayList<>();

    // The dependency's artifacts.
    private final List<ArtifactSpec> fArtifacts = new ArrayList<>();


    /**
     * Create a new {@code DependencySpec}.
     *
     * @param pConfiguration    The name of the dependency's configuration.
     *
     * @throws NullPointerException if {@code pConfiguration} is null.
     */
    protected DependencySpec(String pConfiguration)
    {
        fConfiguration = requireNonNull(pConfiguration);
    }


    public String getConfiguration()
    {
        return fConfiguration;
    }


    public void setConfiguration(String pConfiguration)
    {
        fConfiguration = requireNonNull(pConfiguration);
    }


    public boolean isTransitive()
    {
        return fTransitive;
    }


    public void setTransitive(boolean pTransitive)
    {
        fTransitive = pTransitive;
    }


    /**
     * Get the number of exclusion specifications added to this instance.
     *
     * @return  The number of exclusion specifications.
     */
    public int getNumExclusions()
    {
        return fExclusions.size();
    }


    /**
     * Perform an action for each exclusion specification.
     *
     * @param pAction   The action to apply to the exclusion specifications.
     *
     * @throws NullPointerException if {@code pAction} is null.
     */
    public void forEachExclusion(Consumer<? super ExclusionSpec> pAction)
    {
        fExclusions.forEach(pAction);
    }


    /**
     * Add an exclusion specification to this dependency.
     *
     * @param pExclusionSpec    The specification to add.
     */
    public void addExclusion(ExclusionSpec pExclusionSpec)
    {
        fExclusions.add(pExclusionSpec);
    }


    /**
     * Add an exclusion specification to this dependency.
     *
     * @param pGroup    The exclusion's group.
     * @param pModule   The exclusion's module.
     */
    public void addExclusion(String pGroup, String pModule)
    {
        fExclusions.add(new ExclusionSpec(pGroup, pModule));
    }


    /**
     * Get the number of artifact specifications added to this instance.
     *
     * @return  The number of artifact specifications.
     */
    public int getNumArtifacts()
    {
        return fArtifacts.size();
    }


    /**
     * Perform an action for each artifact specification.
     *
     * @param pAction   The action to apply to the artifact specifications.
     *
     * @throws NullPointerException if {@code pAction} is null.
     */
    public void forEachArtifact(Consumer<? super ArtifactSpec> pAction)
    {
        fArtifacts.forEach(pAction);
    }


    /**
     * Add an artifact specification to this dependency.
     *
     * @param pArtifactSpec The specification to add.
     */
    public void addArtifact(ArtifactSpec pArtifactSpec)
    {
        fArtifacts.add(pArtifactSpec);
    }


    /**
     * Add an artifact specification to this dependency.
     *
     * @param pName         The name of the artifact.
     * @param pType         The artifact's type. Normally the same value as the extension, e.g.
     *                      &quot;jar&quot;.
     * @param pExtension    The artifact's extension. Normally the same value as the type.
     * @param pClassifier   The artifact's classifier.
     * @param pUrl          The URL under which the artifact can be retrieved.
     *
     * @throws NullPointerException if {@code pName} is null.
     */
    public void addArtifact(String pName, String pType, String pExtension, String pClassifier, String pUrl)
    {
        fArtifacts.add(new ArtifactSpec(pName, pType, pExtension, pClassifier, pUrl));
    }


    /**
     * Set the values in this instance by copying them from another {@code DependencySpec}.
     *
     * @param pValues   The values to copy into this instance.
     *
     * @throws NullPointerException if {@code pValues} is null.
     */
    public void setValues(DependencySpec pValues)
    {
        fTransitive =  pValues.isTransitive();
        fArtifacts.addAll(pValues.fArtifacts);
        fExclusions.addAll(pValues.fExclusions);
    }


    /**
     * Get the string notation for this dependency.
     *
     * @return  The dependency's string notation.
     */
    abstract public String toDependencyNotation();


    /**
     * Add this dependency to a project.
     *
     * @param pProject  The project to add this dependency to.
     *
     * @return  True if the dependency was added, false if it's configuration doesn't exist in the
     *          project and it therefore wasn't added.
     *
     * @throws NullPointerException if {@code pProject} is null.
     */
    abstract public boolean addTo(Project pProject);


    /**
     * Pretty print a collection of dependency specifications.
     *
     * @param pPrinter      The printer to print with.
     * @param pDependencies The dependencies to print.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static public void prettyPrintDependencies(
        GradlePrettyPrinter pPrinter,
        Collection<? extends DependencySpec> pDependencies)
    {
        if (pDependencies.size() > 0)
        {
            pPrinter.printClosure(
                CLOSURE_DEPENDENCIES,
                p -> pDependencies.forEach(d -> d.prettyPrint(p))
            );
        }
    }


    /**
     * Check if this dependency has at least one attribute with a non-default value.
     *
     * @return  True if at least one of the attributes has a non-default value, false if all
     *          attributes have default values.
     */
    protected boolean hasNonDefaultAttribute()
    {
        return !(
            fTransitive
            &&
            fExclusions.isEmpty()
            &&
            fArtifacts.isEmpty()
        );
    }


    /**
     * Pretty print the body of a closure that sets the values of the dependency's non-default
     * attributes.
     *
     * @param pPrinter  The printer to print with.
     *
     * @throws NullPointerException if {@code pPrinter} is null.
     */
    protected void printClosureBody(GradlePrettyPrinter pPrinter)
    {
        if (!fTransitive)
            pPrinter.printAttribute(ATTRIBUTE_TRANSITIVE, fTransitive);

        // One exclusion per line.
        fExclusions.forEach(e -> e.prettyPrint(pPrinter));

        // Artifacts as closures.
        fArtifacts.forEach(a -> a.prettyPrint(pPrinter));
    }
}
