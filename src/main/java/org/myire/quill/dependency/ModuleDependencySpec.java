/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import java.util.Objects;

import org.gradle.api.Project;
import org.myire.quill.common.GradlePrettyPrinter;


/**
 * Specification of a dependency on an external module.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ModuleDependencySpec extends DependencySpec
{
    static private final String ATTRIBUTE_CHANGING = "changing";
    static private final String ATTRIBUTE_FORCE = "force";


    private final String fGroup;
    private final String fName;
    private final String fVersion;

    private String fClassifier;
    private String fExtension;

    private boolean fChanging;
    private boolean fForce;


    /**
     * Create a new {@code ModuleDependencySpec}.
     *
     * @param pConfiguration    The name of the dependency's configuration.
     * @param pGroup            The dependency's group.
     * @param pName             The dependency's name.
     * @param pVersion          The dependency's version.
     *
     * @throws NullPointerException if {@code pConfiguration} or {@code pName} is null.
     */
    public ModuleDependencySpec(String pConfiguration, String pGroup, String pName, String pVersion)
    {
        super(pConfiguration);
        fGroup = pGroup;
        fName = Objects.requireNonNull(pName);
        fVersion = pVersion;
    }


    public String getGroup()
    {
        return fGroup;
    }


    public String getName()
    {
        return fName;
    }


    public String getVersion()
    {
        return fVersion;
    }


    public String getClassifier()
    {
        return fClassifier;
    }


    public void setClassifier(String pClassifier)
    {
        fClassifier = pClassifier;
    }


    public String getExtension()
    {
        return fExtension;
    }


    public void setExtension(String pExtension)
    {
        fExtension = pExtension;
    }


    public boolean isChanging()
    {
        return fChanging;
    }


    public void setChanging(boolean pChanging)
    {
        fChanging =  pChanging;
    }


    public boolean isForce()
    {
        return fForce;
    }


    public void setForce(boolean pForce)
    {
        fForce = pForce;
    }


    /**
     * Get the string notation for this dependency.
     *
     * @return  A string on the format &quot;group:name:version:classifier@extension&quot;.
     */
    public String toDependencyNotation()
    {
        StringBuilder aNotation = new StringBuilder(256);

        if (fGroup != null)
            aNotation.append(fGroup).append(':');

        // Name must always be present.
        aNotation.append(fName);

        if (fVersion != null)
            aNotation.append(':').append(fVersion);

        if (fClassifier != null)
            aNotation.append(':').append(fClassifier);

        if (fExtension != null)
            aNotation.append('@').append(fExtension);

        return aNotation.toString();
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
            pPrinter.printMethodCall(getConfiguration(), toDependencyNotation(), true, true);
            pPrinter.printClosure(null, this::printClosureBody);
        }
        else
            // Only default attributes, simply print the configuration name and module dependency
            // notation.
            pPrinter.printMethodCall(getConfiguration(), toDependencyNotation(), true, false);
    }


    /**
     * Check if this dependency has at least one attribute with a non-default value.
     *
     * @return  True if at least one of the attributes has a non-default value, false if all
     *          attributes have default values.
     */
    @Override
    protected boolean hasNonDefaultAttribute()
    {
        return super.hasNonDefaultAttribute() || fChanging || fForce;
    }


    /**
     * Pretty print the body of a closure that sets the values of the dependency's non-default
     * attributes.
     *
     * @param pPrinter  The printer to print with.
     *
     * @throws NullPointerException if {@code pPrinter} is null.
     */
    @Override
    protected void printClosureBody(GradlePrettyPrinter pPrinter)
    {
        if (fChanging)
            pPrinter.printAttribute(ATTRIBUTE_CHANGING, fChanging);

        if (fForce)
            pPrinter.printAttribute(ATTRIBUTE_FORCE, fForce);

        super.printClosureBody(pPrinter);
    }


    /**
     * Check if this specification has a certain name, group, and version.
     *
     * @param pGroup    The group to match, or null to ignore the group value.
     * @param pName     The name to match.
     * @param pVersion  The version to match, or null to ignore the group value.
     *
     * @return  True if this instance matches the specified value(s), false if not.
     */
    boolean matches(String pGroup, String pName, String pVersion)
    {
        return
            // fName is guaranteed to be non-null by the constructor.
            fName.equals(pName)
            &&
            Objects.equals(fGroup, pGroup)
            &&
            Objects.equals(fVersion, pVersion);
    }
}
