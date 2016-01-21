/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.javancss

import org.gradle.api.file.RelativePath
import org.gradle.api.internal.file.pattern.PatternMatcherFactory
import org.gradle.api.logging.LogLevel
import org.gradle.api.specs.AndSpec
import org.gradle.api.specs.NotSpec
import org.gradle.api.specs.OrSpec
import org.gradle.api.specs.Spec
import org.gradle.process.ExecResult


/**
 * Class that executes JavaNCSS in a separate JVM with arguments taken from a JavaNcssTask.
 */
class JavaNcssExec
{
    static private final String MAIN_CLASS = 'javancss.Main'


    private final JavaNcssTask fTask;


    /**
     * Create a new {@code JavaNcssExec}.
     *
     * @param pTask The task to get the execution arguments from.
     */
    JavaNcssExec(JavaNcssTask pTask)
    {
        fTask = pTask;
    }


    /**
     * Execute JavaNCSS with the classpath and arguments from the task specified in the constructor.
     */
    void execute()
    {
        // Get the paths to the source files that match the include and exclude patterns.
        List<String> aSourceFilePaths = createSourceFilePaths();
        if (aSourceFilePaths.isEmpty())
        {
            fTask.logger.info('Cannot run JavaNCSS since no source files match the include/exclude patterns');
            return;
        }

        // Prepend the arguments to the source file path list.
        List<String> aArguments = createArguments(aSourceFilePaths);

        // Execute JavaNCSS in a forked JVM.
        fTask.logger.debug('Executing JavaNCSS with arguments {}', aArguments);
        ExecResult aResult = fTask.project.javaexec {
            main = MAIN_CLASS
            classpath fTask.javancssClasspath
            args aArguments
        }

        LogLevel aLogLevel = aResult.exitValue == 0 ? LogLevel.DEBUG : LogLevel.WARN;
        fTask.logger.log(aLogLevel, 'JavaNCSS exited with code {}', aResult.exitValue);
    }


    /**
     * Filter the task's source files through ist include and exclude patterns.
     *
     * @return  A list with the paths to the source files that were accepted by the include and
     *          exclude patterns.
     */
    private List<String> createSourceFilePaths()
    {
        def aPaths = [];
        Spec<RelativePath> aMatcher = createFileMatcher();
        fTask.source.files.each
        {
            if (matches(aMatcher, it))
                aPaths += it.absolutePath
            else
                fTask.logger.debug('File {} does not match the include/exclude patterns, removing from JavaNCSS analysis',
                                   it);
        }

        return aPaths;
    }


    /**
     * Create the list of command line arguments to the JavaNCSS main class from the JavaNCSS task.
     *
     * @param pSourceFilePaths  The list of source files to add last to the argument list.
     *
     * @return  The command line arguments to pass to the JavaNCSS main class.
     */
    private List<String> createArguments(List<String> pSourceFilePaths)
    {
        def aArguments = [];

        // Add the applicable boolean options.
        if (fTask.ncss)
            aArguments += "-ncss"
        if (fTask.packageMetrics)
            aArguments += "-package"
        if (fTask.classMetrics)
            aArguments += "-object"
        if (fTask.functionMetrics)
            aArguments += "-function"
        if (fTask.reports.getPrimary().format == 'xml')
            aArguments += "-xml"

        // Add the primary report file unless it specifies console as destination.
        File aReportFile = fTask.reports.getPrimary().destination;
        if (aReportFile != null && !aReportFile.name.endsWith('console'))
        {
            aReportFile.parentFile?.mkdirs();
            aArguments += "-out"
            aArguments += aReportFile.absolutePath
        }

        // Append the source files.
        if (aArguments.isEmpty())
            // Nothing added, the source file path list is the entire argument list.
            aArguments = pSourceFilePaths;
        else
            aArguments.addAll(pSourceFilePaths);

        return aArguments;
    }


    /**
     * Create a matcher for source file paths given the include and exclude patterns in the JavaNCSS
     * task.
     *
     * @return  A matcher for source files.
     */
    private Spec<RelativePath> createFileMatcher()
    {
        if (fTask.includes.isEmpty())
        {
            if (fTask.excludes.isEmpty())
                // Neither include not exclude patterns.
                return null;
            else
                // Exclude patterns only.
                return createExcludeMatcher(fTask.excludes);
        }
        else if (fTask.excludes.isEmpty())
        {
            // Include patterns only
            return createIncludeMatcher(fTask.includes);

        }
        else
            // Both include and exclude patterns.
            return createCombinedMatcher(fTask.includes, fTask.excludes);
    }


    /**
     * Return whether a file's path matches the criteria of a matcher.
     *
     * @param pMatcher  The matcher.
     * @param pFile     The file to check.
     *
     * @return  True if the file's path matches the matcher's criteria, false if not.
     */
    static private boolean matches(Spec<RelativePath> pMatcher, File pFile)
    {
        return pMatcher == null || pMatcher.isSatisfiedBy(RelativePath.parse(pFile.isFile(), pFile.path));
    }


    /**
     * Create a matcher for source file paths given a collection of include patterns and a
     * collection of exclude patterns. The returned matcher will operate on the conjunction of the
     * two pattern collections.
     *
     * @param pIncludePatterns  The inclusions patterns.
     * @param pExcludePatterns  The exclusion patterns.
     *
     * @return  A combined inclusion/exclusion matcher for source files.
     */
    static private Spec<RelativePath> createCombinedMatcher(Collection<String> pIncludePatterns,
                                                            Collection<String> pExcludePatterns)
    {
        return new AndSpec<>(createIncludeMatcher(pIncludePatterns), createExcludeMatcher(pExcludePatterns));
    }


    /**
     * Create a matcher for source file paths given a collection of include patterns. The returned
     * matcher will operate on the disjunction of the specified patterns.
     *
     * @param pPatterns The inclusions patterns.
     *
     * @return  An inclusion matcher for source files.
     */
    static private Spec<RelativePath> createIncludeMatcher(Collection<String> pPatterns)
    {
        Collection<Spec<RelativePath>> aMatchers = new ArrayList<>();
        pPatterns.each {
            aMatchers.add(PatternMatcherFactory.getPatternMatcher(true, true, it));
        }

        return new OrSpec<>(aMatchers);
    }


    /**
     * Create a matcher for source file paths given a collection of exclude patterns. The returned
     * matcher will operate on the negated disjunction of the specified patterns.
     *
     * @param pPatterns The exclusion patterns.
     *
     * @return  An exclusion matcher for source files.
     */
    static private Spec<RelativePath> createExcludeMatcher(Collection<String> pPatterns)
    {
        Collection<Spec<RelativePath>> aMatchers = new ArrayList<>();
        pPatterns.each {
            aMatchers.add(PatternMatcherFactory.getPatternMatcher(true, true, it));
        }

        return new NotSpec<>(new OrSpec<>(aMatchers));
    }
}
