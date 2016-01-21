/*
 * Copyright 2014-2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cobertura

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction


/**
 * Task for Cobertura instrumentation based on the Ant task provided in the Cobertura distribution.
 * The task produces instrumented versions of the Java classes specified in the input property
 * {@code inputClasses}, and a file containing meta data about these instrumented classes, specified
 * by the property {@code dataFile}.
 *<p>
 * Note that all properties are read-only; they are configured in the task's context.
 *<p>
 * The properties of this task are to a large extent identical to the ones of the
 * {@code Cobertura-Instrument} Ant task, see the
 * <a href="https://github.com/cobertura/cobertura/wiki/Ant-Task-Reference">reference</> for more
 * information.
 */
class CoberturaInstrumentTask extends AbstractCoberturaTask
{
    /**
     * Get the classes to instrument.
     *
     * @return  The classes to instrument.
     */
    @InputFiles
    FileCollection getInputClasses()
    {
        return context.getInputClasses();
    }


    /**
     * Get the flag specifying whether trivial methods should be ignored or not. If this value is
     * true, constructors/methods that contain one line of code will be excluded from the
     * instrumentation. Examples include constructors only calling a super constructor, and
     * getters/setters.
     *
     * @return True if trivial methods should be ignored, false if not.
     */
    @Input
    boolean getIgnoreTrivial()
    {
        return context.ignoreTrivial;
    }


    /**
     * Get the list of regular expressions specifying methods names to be excluded from the
     * instrumentation. Note that the classes containing the methods will still be instrumented.
     *
     * @return The ignore method names regular expressions.
     */
    @Input
    List<String> getIgnoreMethodNames()
    {
        return context.ignoreMethodNames;
    }


    /**
     * Get the  list of fully qualified names of annotations with which methods that should be
     * excluded from instrumentation are annotated with.
     *
     * @return The ignore method annotations.
     */
    @Input
    List<String> getIgnoreMethodAnnotations()
    {
        return context.ignoreMethodAnnotations;
    }


    /**
     * Get the path containing any classes that shouldn't be instrumented but are needed by the
     * instrumentation.
     *
     * @return  The  auxiliary class path, possibly null.
     */
    @InputFiles
    @Optional
    FileCollection getAuxClassPath()
    {
        return context.auxClassPath;
    }


    /**
     * Get the directory where to create the instrumented classes.
     *
     * @return  The instrumented classes directory.
     */
    @OutputDirectory
    File getInstrumentedClassesDir()
    {
        return context.getInstrumentedClassesDir();
    }


    /**
     * Get the file to write instrumentation meta data to.
     *
     * @return  The instrumentation data file.
     */
    @OutputFile
    File getDataFile()
    {
        return context.getInstrumentationDataFile();
    }


    /**
     * Perform instrumentation of the classes in the {@code inputClasses} file collection and write
     * the resulting classes to {@code instrumentedClassesDir}. Also create the instrumentation meta
     * data file {@code dataFile}.
     */
    @TaskAction
    void instrument()
    {
        // Define the Ant task from the Cobertura classes.
        ant.taskdef(name: 'coberturaInstrument',
                    classname: 'net.sourceforge.cobertura.ant.InstrumentTask',
                    classpath: getCoberturaClassPath().asPath);

        // Delete any old data file and instrumented classes directory.
        File aInstrumentedClassesDir = getInstrumentedClassesDir();
        File aDataFile = getDataFile();
        project.delete(aInstrumentedClassesDir, aDataFile);

        // Create the directory to put the instrumented classes into.
        project.mkdir(aInstrumentedClassesDir);

        // Create the argument list for the ant task.
        def aArguments = [todir: aInstrumentedClassesDir,
                          datafile: aDataFile,
                          ignoreTrivial: getIgnoreTrivial()];

        // Add the auxiliary class path if specified.
        FileCollection aAuxClassPath = getAuxClassPath();
        if (aAuxClassPath != null)
            aArguments['auxClasspath'] = aAuxClassPath.asPath;

        // Execute the ant task.
        ant.coberturaInstrument(aArguments)
        {
            // Specify the classes to instrument as Ant file sets.
            getInputClasses()?.each {
                project.fileTree(it).addToAntBuilder(ant, "fileset", FileCollection.AntType.FileSet);
            }

            // Add the task's nested elements from the corresponding extension properties.
            getIgnoreMethodNames().each { ignore(regex: it)  }
            getIgnoreMethodAnnotations().each { ignoreMethodAnnotation(annotationName: it) }
        }
    }
}
