/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jol.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import org.openjdk.jol.datamodel.DataModel;
import org.openjdk.jol.datamodel.X86_32_DataModel;
import org.openjdk.jol.datamodel.X86_64_COOPS_DataModel;
import org.openjdk.jol.datamodel.X86_64_DataModel;
import org.openjdk.jol.info.ClassData;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.FieldLayout;
import org.openjdk.jol.layouters.CurrentLayouter;
import org.openjdk.jol.layouters.HotSpotLayouter;
import org.openjdk.jol.layouters.Layouter;
import org.openjdk.jol.layouters.RawLayouter;
import org.openjdk.jol.util.ClassUtils;

import org.myire.quill.jol.JolParameters;
import org.myire.quill.jol.JolResult;
import org.myire.quill.jol.JolRunner;


/**
 * Implementation of {@code JolRunner} based on the jol-core library. This class should not be
 * loaded before the jol-core classes are available on the class path.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class JolRunnerImpl implements JolRunner
{
    private String fToolVersion = "unknown";

    private final Logger fLogger = Logging.getLogger(JolRunnerImpl.class);


    @Override
    public void init(String pToolVersion)
    {
        fToolVersion = pToolVersion;
    }


    @Override
    public JolResult analyze(Collection<String> pClasses, JolParameters pParameters)
    {
        // Jol writes to System.out during execution, capture that output and log it properly after
        // the analysis has finished.
        ByteArrayOutputStream aJolOutputStream = new ByteArrayOutputStream(1024);
        PrintStream aJolSystemOut = new PrintStream(aJolOutputStream);
        PrintStream aOriginalSystemOut = System.out;
        System.setOut(aJolSystemOut);

        try
        {
            // Create the Layouter specified by the parameters.
            Layouter aLayouter = createLayouter(pParameters);

            // Load all classes to analyze and perform the layout analysis/simulation.
            JolResult aResult = new JolResult(fToolVersion, aLayouter.toString());
            for (String aClassName : pClasses)
            {
                Class<?> aClass = loadClass(aClassName);
                if (aClass != null)
                {
                    ClassLayout aClassLayout = aLayouter.layout(ClassData.parseClass(aClass));
                    aResult.add(toResult(aClass, aClassLayout));
                }
            }

            return aResult;
        }
        finally
        {
            // Restore System.out.
            System.setOut(aOriginalSystemOut);

            // Log any output captured from the Jol execution.
            aJolSystemOut.close();
            String aJolOutput = aJolOutputStream.toString();
            if (!aJolOutput.isEmpty())
                fLogger.debug("Jol output: {}", aJolOutput);
        }
    }


    /**
     * Load a class.
     *
     * @param pClassName    The fully qualified name of the class.
     *
     * @return  The loaded class, or null if an error occurs.
     */
    private Class<?> loadClass(String pClassName)
    {
        try
        {
            return ClassUtils.loadClass(pClassName);
        }
        catch (ClassNotFoundException |  NoClassDefFoundError cnfe)
        {
            fLogger.error("Could not load class {} for layout analysis", pClassName, cnfe);
            return null;
        }
    }



    /**
     * Create a {@code JolResult.ClassLayout} instance from a {@code Class} and a
     * {@code ClassLayout}.
     *
     * @param pClass        The analyzed class.
     * @param pClassLayout  The result of the class layout analysis.
     *
     * @return  A new {@code JolResult.ClassLayout}, never null.
     *
     * @throws NullPointerException if any of the parameter is null.
     */
    static private JolResult.ClassLayout toResult(Class<?> pClass, ClassLayout pClassLayout)
    {
        Collection<JolResult.FieldLayout> aFields =
            pClassLayout.fields()
                .stream()
                .map(JolRunnerImpl::toResult)
                .collect(Collectors.toList());

        return new JolResult.ClassLayout(
            pClass.getSimpleName(),
            getPackageName(pClass),
            getEnclosingClassName(pClass),
            pClassLayout.headerSize(),
            pClassLayout.instanceSize(),
            aFields);
    }


    /**
     * Create a {@code JolResult.FieldLayout} instance from a {@code FieldLayout}.
     *
     * @param pFieldLayout  The result of the field layout analysis.
     *
     * @return  A new {@code JolResult.FieldLayout}, never null.
     *
     * @throws NullPointerException if {@code pFieldLayout} is null.
     */
    static private JolResult.FieldLayout toResult(FieldLayout pFieldLayout)
    {
        return new JolResult.FieldLayout(
            pFieldLayout.name(),
            pFieldLayout.typeClass(),
            pFieldLayout.offset(),
            pFieldLayout.size()
        );
    }


    /**
     * Create a new {@code Layouter} instance.
     *
     * @param pParameters   The parameters determining the {@code Layouter} implementation to create
     *                      an instance of.
     *
     * @return  A new {@code Layouter} instance, never null.
     *
     * @throws NullPointerException if {@code pParameters} is null.
     * @throws IllegalArgumentException if {@code pParameters.fLayout} is null or has an unknown
     *                                  value.
     */
    static private Layouter createLayouter(JolParameters pParameters)
    {
        switch (pParameters.fLayout)
        {
            case RAW:       return new RawLayouter(createDataModel(pParameters));
            case HOTSPOT:   return new HotSpotLayouter(createDataModel(pParameters));
            case CURRENT:   return new CurrentLayouter();
            default:        throw new IllegalArgumentException("Invalid layout parameter: " +
                                                                   pParameters.fLayout);
        }
    }


    /**
     * Create a new {@code DataModel} instance.
     *
     * @param pParameters   The parameters determining the {@code DataModel} implementation to
     *                      create an instance of.
     *
     * @return  A new {@code DataModel} instance, never null.
     *
     * @throws NullPointerException if {@code pParameters} is null.
     * @throws IllegalArgumentException if {@code pParameters.fDataModel} is null or has an unknown
     *                                  value.
     */
    static private DataModel createDataModel(JolParameters pParameters)
    {
        switch (pParameters.fDataModel)
        {
            case x86_32:    return new X86_32_DataModel(pParameters.fAlignment);
            case x86_64:    return new X86_64_DataModel(pParameters.fAlignment);
            case x86_64_COMPRESSED:
                            return new X86_64_COOPS_DataModel(pParameters.fAlignment);
            default:        throw new IllegalArgumentException("Invalid data model parameter: " +
                                                                   pParameters.fDataModel);
        }
    }

    /**
     * Get the name of a class's package.
     *
     * @param pClass    The class.
     *
     * @return  The package name, or an empty string if the class has no package.
     *
     * @throws NullPointerException if {@code pClass} is null.
     */
    static private String getPackageName(Class<?> pClass)
    {
        Package aPackage = pClass.getPackage();
        return aPackage != null ? aPackage.getName() : "";
    }


    /**
     * Get the name of a class's enclosing class.
     *
     * @param pClass    The class.
     *
     * @return  The name of the enclosing class, or null if the class has no enclosing class.
     *
     * @throws NullPointerException if {@code pClass} is null.
     */
    static private String getEnclosingClassName(Class<?> pClass)
    {
        // Create a list of the hierarchy of enclosing classes with the top level class last in
        // the list.
        int aNumClassNameChars = 0;
        List<Class<?>> aClassHierarchy = new ArrayList<>();
        Class<?> aEnclosingClass = pClass.getEnclosingClass();
        while (aEnclosingClass != null)
        {
            aNumClassNameChars += aEnclosingClass.getSimpleName().length();
            aClassHierarchy.add(aEnclosingClass);
            aEnclosingClass = aEnclosingClass.getEnclosingClass();
        }

        if (aClassHierarchy.isEmpty())
            // No enclosing classes.
            return null;
        else if (aClassHierarchy.size() == 1)
            // One enclosing class.
            return aClassHierarchy.get(0).getSimpleName();

        // A hierarchy of at least two enclosing classes. Create a string of the enclosing class
        // names on the form X.Y.Z. The string's length is the number of chars in all class
        // names + the number of separating chars.
        StringBuilder aBuffer = new StringBuilder(aNumClassNameChars + aClassHierarchy.size());

        // Append the names of all but the immediately enclosing class (first in the list)
        // separated by dots ('.'), starting with the outermost class (last in the list).
        for (int i=aClassHierarchy.size()-1; i>0; i--)
            aBuffer.append(aClassHierarchy.get(i).getSimpleName()).append('.');

        // Append the immediately enclosing class without a trailing '.'.
        aBuffer.append(aClassHierarchy.get(0).getSimpleName());
        return aBuffer.toString();
    }
}
