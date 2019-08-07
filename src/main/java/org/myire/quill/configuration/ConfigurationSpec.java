/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.configuration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import static java.util.Objects.requireNonNull;

import org.myire.quill.common.GradlePrettyPrinter;
import org.myire.quill.common.PrettyPrintable;


/**
 * Specification of a configuration.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ConfigurationSpec implements PrettyPrintable
{
    static private final String CLOSURE_CONFIGURATIONS = "configurations";
    static private final String ATTRIBUTE_TRANSITIVE ="transitive";
    static private final String ATTRIBUTE_VISIBLE ="visible";
    static private final String ATTRIBUTE_DESCRIPTION ="description";
    static private final String ATTRIBUTE_EXTENDS ="extendsFrom";


    private final String fName;
    private final Set<String> fExtendedConfigurations = new LinkedHashSet<>();

    private boolean fTransitive = true;
    private boolean fVisible = true;
    private String fDescription;


    /**
     * Create a new {@code ConfigurationSpec}.
     *
     * @param pName The name of the configuration.
     *
     * @throws NullPointerException if {@code pName} is null.
     */
    public ConfigurationSpec(String pName)
    {
        fName = requireNonNull(pName);
    }


    public String getName()
    {
        return fName;
    }


    public boolean isTransitive()
    {
        return fTransitive;
    }


    public void setTransitive(boolean pTransitive)
    {
        fTransitive = pTransitive;
    }


    public boolean isVisible()
    {
        return fVisible;
    }


    public void setVisible(boolean pVisible)
    {
        fVisible = pVisible;
    }


    public String getDescription()
    {
        return fDescription;
    }


    public void setDescription(String pDescription)
    {
       fDescription = pDescription;
    }


    /**
     * Get the number of configurations that are extended by this configuration.
     *
     * @return  The number of extended configurations.
     */
    public int getNumExtendedConfigurations()
    {
        return fExtendedConfigurations.size();
    }


    /**
     * Perform an action for the name of each configuration extended by this configuration.
     *
     * @param pAction   The action to apply to the names of the extended configurations.
     *
     * @throws NullPointerException if {@code pAction} is null.
     */
    public void forEachExtendedConfiguration(Consumer<? super String> pAction)
    {
        fExtendedConfigurations.forEach(pAction);
    }


    /**
     * Add the name of a configuration that is extended by this configuration.
     *
     * @param pConfigurationName    The name of the configuration that is extended by this
     *                              configuration.
     *
     * @throws NullPointerException if {@code pConfigurationName} is null.
     */
    public void addExtendedConfiguration(String pConfigurationName)
    {
        fExtendedConfigurations.add(requireNonNull(pConfigurationName));
    }


    @Override
    public void prettyPrint(GradlePrettyPrinter pPrinter)
    {
        if (hasNonDefaultAttribute())
            // One or more non-default values, print them in a closure.
            pPrinter.printClosure(fName, this::printClosureBody);
        else
            // Default values only, simply print the configuration's name.
            pPrinter.printIndentedLine(fName);
    }


    /**
     * Pretty print a collection of configuration specifications on Gradle repository closure format.
     *
     * @param pPrinter          The printer to print with.
     * @param pConfigurations   The configurations to print.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static public void prettyPrintConfigurations(
        GradlePrettyPrinter pPrinter,
        Collection<? extends ConfigurationSpec> pConfigurations)
    {
        if (pConfigurations.size() > 0)
        {
            pPrinter.printClosure(
                CLOSURE_CONFIGURATIONS,
                p -> pConfigurations.forEach(c -> c.prettyPrint(p))
            );
        }
    }


    /**
     * Pretty print the body of a closure that sets the values of the configuration's non-default
     * attributes.
     *
     * @param pPrinter  The printer to print with.
     *
     * @throws NullPointerException if {@code pPrinter} is null.
     */
    private void printClosureBody(GradlePrettyPrinter pPrinter)
    {
        if (!fTransitive)
            pPrinter.printAttribute(ATTRIBUTE_TRANSITIVE, fTransitive);

        if (!fVisible)
            pPrinter.printAttribute(ATTRIBUTE_VISIBLE, fVisible);

        pPrinter.printAttribute(ATTRIBUTE_DESCRIPTION, fDescription);
        pPrinter.printMethodCall(ATTRIBUTE_EXTENDS, extendedConfigurationsAsString(), false, false);
    }


    /**
     * Get a string with the comma separated names of the configurations that are extended by this
     * configuration.
     *
     * @return  A string with the extended configurations' names, or null if this configuration
     *          does not extend any configurations.
     */
    private String extendedConfigurationsAsString()
    {
        if (fExtendedConfigurations.isEmpty())
            return null;

        int aNumExtended = fExtendedConfigurations.size();
        StringBuilder aBuffer = new StringBuilder(aNumExtended * 8);
        for (String aExtended : fExtendedConfigurations)
        {
            aBuffer.append(aExtended);
            if (--aNumExtended  > 0)
                aBuffer.append(", ");
        }

        return aBuffer.toString();
    }


    /**
     * Check if this configuration has at least one attribute with a non-default value.
     *
     * @return  True if at least one of the attributes has a non-default value, false if all
     *          attributes have default values.
     */
    private boolean hasNonDefaultAttribute()
    {
        return !(
            fTransitive
            &&
            fVisible
            &&
            fDescription == null
            &&
            fExtendedConfigurations.isEmpty()
        );
    }
}
