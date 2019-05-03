/*
 * Copyright 2017-2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import static java.util.Objects.requireNonNull;

import org.myire.quill.common.GradlePrettyPrinter;
import org.myire.quill.common.PrettyPrintable;


/**
 * Specification of a dependency artifact.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ArtifactSpec implements PrettyPrintable
{
    static private final String CLOSURE_ARTIFACT = "artifact";
    static private final String ATTRIBUTE_NAME ="name";
    static private final String ATTRIBUTE_TYPE ="type";
    static private final String ATTRIBUTE_EXTENSION ="extension";
    static private final String ATTRIBUTE_CLASSIFIER ="classifier";
    static private final String ATTRIBUTE_URL ="url";

    private final String fName;
    private final String fType;
    private final String fExtension;
    private final String fClassifier;
    private final String fUrl;


    /**
     * Create a new {@code ArtifactSpec}.
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
    public ArtifactSpec(String pName, String pType, String pExtension, String pClassifier, String pUrl)
    {
        fName = requireNonNull(pName);
        fType =pType;
        fExtension = pExtension;
        fClassifier = pClassifier;
        fUrl = pUrl;
    }


    public String getName()
    {
        return fName;
    }


    public String getType()
    {
        return fType;
    }


    public String getExtension()
    {
        return fExtension;
    }


    public String getClassifier()
    {
        return fClassifier;
    }


    public String getUrl()
    {
        return fUrl;
    }


    @Override
    public void prettyPrint(GradlePrettyPrinter pPrinter)
    {
        pPrinter.printClosure(CLOSURE_ARTIFACT, this::printAttributes);
    }


    /**
     * Pretty print the body of a closure that sets the values of the artifact's non-null.
     * attributes.
     *
     * @param pPrinter  The printer to print with.
     *
     * @throws NullPointerException if {@code pPrinter} is null.
     */
    private void printAttributes(GradlePrettyPrinter pPrinter)
    {
        pPrinter.printAttribute(ATTRIBUTE_NAME, fName);
        pPrinter.printAttribute(ATTRIBUTE_TYPE, fType);
        pPrinter.printAttribute(ATTRIBUTE_EXTENSION, fExtension);
        pPrinter.printAttribute(ATTRIBUTE_CLASSIFIER, fClassifier);
        pPrinter.printAttribute(ATTRIBUTE_URL, fUrl);
    }
}
