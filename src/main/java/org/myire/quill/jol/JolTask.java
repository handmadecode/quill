/*
 * Copyright 2020-2021 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import static java.util.Objects.requireNonNull;

import groovy.lang.Closure;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.util.ConfigureUtil;

import org.myire.quill.common.ExternalToolLoader;
import org.myire.quill.common.Projects;
import org.myire.quill.report.ReportingEntity;
import org.myire.quill.report.Reports;
import org.myire.quill.report.TransformingReport;


/**
 * Task for object layout analysis using the Jol tool.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class JolTask extends DefaultTask implements ReportingEntity<JolReports>
{
    // The default version of Jol to use.
    static private final String DEFAULT_TOOL_VERSION = "0.10";

    // Fully qualified name of the JolRunner implementation class to use.
    static private final String IMPLEMENTATION_PACKAGE = "org.myire.quill.jol.impl.";
    static private final String IMPLEMENTATION_CLASS = "JolRunnerImpl";


    private JolParameters.Layout fLayout;
    private JolParameters.DataModel fDataModel;
    private int fAlignment = 8;

    private String fToolVersion;

    private Collection<String> fClasses = new ArrayList<>();
    private FileCollection fAnalysisClassPath;
    private FileCollection fToolClassPath;

    private JolReportsImpl fReports;

    private final Collection<ClassesDirectory> fClassesDirectories = new ArrayList<>();


    /**
     * Get the layout parameter for the Jol analysis. Default is
     * {@link JolParameters.Layout#HOTSPOT}.
     *
     * @return  The layout parameter.
     */
    @Input
    public JolParameters.Layout getLayout()
    {
        return fLayout != null ? fLayout : JolParameters.Layout.HOTSPOT;
    }


    public void setLayout(JolParameters.Layout pLayout)
    {
        fLayout = pLayout;
    }


    /**
     * Get the data model parameter for the Jol analysis. Default is
     * {@link JolParameters.DataModel#x86_64_COMPRESSED}.
     *
     * @return  The data model parameter.
     */
    @Input
    public JolParameters.DataModel getDataModel()
    {
        return fDataModel != null ? fDataModel : JolParameters.DataModel.x86_64_COMPRESSED;
    }


    public void setDataModel(JolParameters.DataModel pDataModel)
    {
        fDataModel = pDataModel;
    }


    /**
     * Get the alignment parameter for the Jol analysis. Default is {@code 8}.
     *
     * @return  The alignment parameter.
     */
    @Input
    public int getAlignment()
    {
        return fAlignment;
    }


    public void setAlignment(int pAlignment)
    {
        fAlignment = pAlignment;
    }


    /**
     * Get the version of Jol to use. Default is &quot;0.10&quot;.
     *
     * @return  The Jol version string.
     */
    @Input
    public String getToolVersion()
    {
        return fToolVersion != null ? fToolVersion : DEFAULT_TOOL_VERSION;
    }


    public void setToolVersion(String pToolVersion)
    {
        fToolVersion = pToolVersion;
    }


    /**
     * Get the fully qualified names of classes to analyze.
     *
     * @return  The classes to analyze, never null.
     */
    @Input
    public Collection<String> getClasses()
    {
        return fClasses;
    }


    public void setClasses(Collection<String> pClasses)
    {
        fClasses = pClasses != null ? pClasses : new ArrayList<>();
    }


    public void setClasses(String... pClasses)
    {
        fClasses = Arrays.asList(pClasses);
    }


    /**
     * Get the class path from which to load any classes referenced by the classes to analyze. The
     * classes in the returned collection will not be analyzed but are required when loading the
     * classes to analyze.
     *
     * @return  The analysis class path, never null.
     */
    @InputFiles
    public FileCollection getAnalysisClassPath()
    {
        return fAnalysisClassPath != null ? fAnalysisClassPath : getProject().files();
    }


    public void setAnalysisClassPath(Object pAnalysisClassPath)
    {
        if (pAnalysisClassPath != null)
            fAnalysisClassPath = getProject().files(pAnalysisClassPath);
        else
            fAnalysisClassPath = null;
    }


    private void addToAnalysisClassPath(FileCollection pClassPath)
    {
        if (fAnalysisClassPath != null)
            fAnalysisClassPath = fAnalysisClassPath.plus(pClassPath);
        else
            fAnalysisClassPath = pClassPath;
    }


    /**
     * Get the class path from which to load the Jol classes.
     *
     * @return  The tool class path.
     */
    @InputFiles
    public FileCollection getToolClassPath()
    {
        return fToolClassPath != null ? fToolClassPath : getProject().files();
    }


    public void setToolClassPath(FileCollection pToolClassPath)
    {
        fToolClassPath = pToolClassPath;
    }


    /**
     * Get the directories containing class files to analyze.
     *
     * @return  The class file directories, never null.
     */
    @InputFiles
    public Collection<File> getClassesDirectories()
    {
        return fClassesDirectories
            .stream()
            .map(d -> d.fDirectory)
            .collect(Collectors.toList());
    }


    /**
     * Add a directory containing classes to analyze.
     *
     * @param pDirectoryPath    An object that resolves to a directory. See the documentation of
     *                          {@code org.gradle.api.Project::file()} for the possible types that
     *                          can be resolved.
     *
     * @throws NullPointerException if {@code pDirectoryPath} is null.
     */
    public void classesDirectory(Object pDirectoryPath)
    {
        classesDirectory(pDirectoryPath, null);
    }


    /**
     * Add a directory containing classes to analyze. The class files in the directory can be
     * filtered through a {@code Closure} that configures a {@code PatternFilterable}.
     *
     * @param pDirectoryPath    An object that resolves to a directory. See the documentation of
     *                          {@code org.gradle.api.Project::file()} for the possible types that
     *                          can be resolved.
     * @param pFilterClosure    A closure that configures a {@code PatternFilterable}. Pass null to
     *                          include all class files in the directory.
     *
     * @throws NullPointerException if {@code pDirectoryPath} is null.
     */
    public void classesDirectory(Object pDirectoryPath, Closure<?> pFilterClosure)
    {
        // Add the directory to the analysis class path; the classes to analyze will be loaded from
        // that directory.
        File aDirectory = getProject().file(pDirectoryPath);
        addToAnalysisClassPath(getProject().files(aDirectory));

        // Add the directory to the collection of class file directories from which the classes to
        // analyze will be retrieved.
        getLogger().debug("Adding classes directory {}", aDirectory.getAbsolutePath());
        fClassesDirectories.add(new ClassesDirectory(aDirectory, createFilter(pFilterClosure)));
    }


    /**
     * Specify a source set to analyze classes from. The source set's classes directories will be
     * added to the classes directories and the source set's runtime class path will be added to the
     * analysis class path.
     *<p>
     * By default this task analyzes the main source set, if available.
     *
     * @param pSourceSet    The source set.
     *
     * @throws NullPointerException if {@code pSourceSet} is null.
     */
    public void sourceSet(SourceSet pSourceSet)
    {
        sourceSet(pSourceSet, null);
    }


    /**
     * Specify a source set to analyze classes from. The source set's classes directories will be
     * added to the classes directories and the source set's runtime class path will be added to the
     * analysis class path. The class files in the classes directories can be filtered through a
     * {@code Closure} that configures a {@code PatternFilterable}.
     *<p>
     * By default this task analyzes the main source set, if available.
     *
     * @param pSourceSet        The source set.
     * @param pFilterClosure    A closure that configures a {@code PatternFilterable}. Pass null to
     *                          include all class files in the source set's classes directories.
     *
     * @throws NullPointerException if {@code pSourceSet} is null.
     */
    public void sourceSet(SourceSet pSourceSet, Closure<?> pFilterClosure)
    {
        getLogger().debug("Adding source set {}", pSourceSet.getName());

        // Add the source set's output directories as classes directories.
        for (File aClassesDirectory : pSourceSet.getOutput().getClassesDirs())
            classesDirectory(aClassesDirectory, pFilterClosure);

        // Add the source set's runtime classpath to the classpath used for loading the classes to
        // analyze. There are no classes to analyze in the runtime classpath, but those classes may
        // be referenced by the classes to analyze.
        addToAnalysisClassPath(pSourceSet.getRuntimeClasspath());
    }


    /**
     * Get the reports produced by this task.
     *
     * @return  The reports.
     */
    @Override
    @Nested
    public JolReports getReports()
    {
        return fReports;
    }


    /**
     * Configure this task's reports.
     *
     * @param pClosure  A closure that configures the reports.
     *
     * @return  This task's reports.
     */
    @Override
    public JolReports reports(Closure pClosure)
    {
        fReports.configure(pClosure);
        return fReports;
    }


    /**
     * Configure this task's reports.
     *
     * @param pAction   An action that configures the reports.
     *
     * @return  This task's reports.
     *
     * @throws NullPointerException if {@code pAction} is null.
     */
    @Override
    public JolReports reports(Action<? super JolReports> pAction)
    {
        pAction.execute(fReports);
        return fReports;
    }


    /**
     * Analyze the layout of the specified classes and produce the enabled report(s).
     */
    @TaskAction
    public void run()
    {
        SingleFileReport aXmlReport = fReports.getXml();
        if (Reports.isRequired(aXmlReport))
        {
            // Run the analysis and create the XML report.
            JolResult aResult = runJolAnalysis();
            if (aResult != null)
            {
                createXmlReport(aResult, aXmlReport);

                // Create the HTML report if enabled.
                TransformingReport aHtmlReport = fReports.getHtml();
                if (Reports.isRequired(aHtmlReport))
                    aHtmlReport.transform();
            }
        }
        else
            getLogger().info("Jol XML report is disabled, no analysis will be performed");
    }


    /**
     * Create the task's report container and specify the report related inputs and outputs.
     */
    void setupReports()
    {
        fReports = new JolReportsImpl(this);

        // Only execute the task if its XML report is enabled, as the HTML report is created from
        // the XML report.
        onlyIf(_ignore -> Reports.isRequired(getReports().getXml()));

        // Add the reports to the task's input and output properties.
        fReports.setInputsAndOutputs(this);
    }


    /**
     * Configure the classes to analyze with the classes from the main source set if no explicit
     * classes or classes directories have been configured.
     */
    void maybeConfigureDefaultClasses()
    {
        if (fClasses.isEmpty() && fClassesDirectories.isEmpty())
        {
            getLogger().debug("No classes explicitly configured, defaulting to the main source set");
            SourceSet aMainSourceSet = Projects.getSourceSet(getProject(), SourceSet.MAIN_SOURCE_SET_NAME);
            if (aMainSourceSet != null)
                sourceSet(aMainSourceSet);
        }
    }


    /**
     * Run a Jol analysis on the classes specified in the task's configuration.
     *
     * @return  The result of the analysis, or null if the {@code olRunner} implementation could
     *          not be loaded.
     */
    private JolResult runJolAnalysis()
    {
        try
        {
            // Add the classes from any specified classes directories to the list of classes to
            // analyze.
            for (ClassesDirectory aClassesDirectory : fClassesDirectories)
            {
                getLogger().debug("Adding classes for analysis from {}", aClassesDirectory);
                aClassesDirectory.addClassNames(fClasses, getProject());
            }

            // Create the JolRunner instance and run the analysis with the analysis parameters
            // specified in this task's properties.
            JolParameters aParameters = new JolParameters(getLayout(), getDataModel(), getAlignment());
            return loadJolRunner().analyze(fClasses, aParameters);
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
        {
            getLogger().error("Could not create an instance of '{}{}'",
                              IMPLEMENTATION_PACKAGE,
                              IMPLEMENTATION_CLASS,
                              e);
            return null;
        }
    }


    /**
     * Create an XML report from the result of a Jol analysis.
     *
     * @param pResult       The result.
     * @param pXmlReport    The report specification.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    private void createXmlReport(JolResult pResult, SingleFileReport pXmlReport)
    {
        try (JolXmlReportWriter aWriter = new JolXmlReportWriter(pXmlReport))
        {
            aWriter.writeReport(pResult);
        }
        catch (IOException ioe)
        {
            getLogger().error("Failed to create Jol report {}", Reports.getOutputLocation(pXmlReport), ioe);
        }
    }


    /**
     * Load the {@code JolRunner} implementation.
     *
     * @return  A new instance of the loaded {@code JolRunner} implementation.
     *
     * @throws ClassNotFoundException   if the implementation class or any class it refers to could
     *                                  not be found.
     * @throws InstantiationException   if the implementation class is abstract or doesn't have a
     *                                  no-args constructor (or can't be instantiated for some other
     *                                  reason).
     * @throws IllegalAccessException   if the implementation class or its no-args constructor
     *                                  isn't accessible.
     */
    private JolRunner loadJolRunner()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        ExternalToolLoader<JolRunner> aLoader =
            new ExternalToolLoader<>(
                JolRunner.class,
                IMPLEMENTATION_PACKAGE,
                IMPLEMENTATION_CLASS,
                this::createJolRunnerClassPath);

        JolRunner aJolRunner = aLoader.createToolProxy();
        aJolRunner.init(getToolVersion());
        return aJolRunner;
    }


    private FileCollection createJolRunnerClassPath()
    {
        return getToolClassPath().plus(getAnalysisClassPath());
    }


    static private PatternFilterable createFilter(Closure<?> pFilterClosure)
    {
        if (pFilterClosure != null)
            return ConfigureUtil.configure(pFilterClosure, new PatternSet());
        else
            return null;
    }


    /**
     * Specification of a directory containing class files with an optional
     * {@code PatternFilterable} specifying which files to include and/or exclude.
     */
    static public class ClassesDirectory
    {
        final File fDirectory;
        final PatternFilterable fFilter;

        ClassesDirectory(File pDirectory, PatternFilterable pFilter)
        {
            fDirectory = requireNonNull(pDirectory);
            fFilter = pFilter;
        }

        /**
         * Add the fully qualified class names of the classes in the directory matching the filter
         * to a collection.
         *
         * @param pClassNames   The collection to add the class names to.
         * @param pProject      The project to resolve the files with.
         *
         * @throws NullPointerException if any of the parameters is null.
         */
        void addClassNames(Collection<String> pClassNames, Project pProject)
        {
            if (!fDirectory.exists())
                return;

            // Get the individual files from the directory, applying any specified filter.
            FileTree aClassFiles = pProject.files(fDirectory).getAsFileTree();
            if (fFilter != null)
                aClassFiles = aClassFiles.matching(fFilter);

            // Convert all class files to fully qualified class names and add them to the
            // provided collection.
            Path aParentPath = fDirectory.toPath();
            for (File aClassFile : aClassFiles)
            {
                if ("module-info.class".equals(aClassFile.getName()))
                    // Skip the module-info file; trying to load it will throw a
                    // NoClassDefFoundError.
                    continue;

                String aRelativePath = aParentPath.relativize(aClassFile.toPath()).toString();
                int aClassSuffixPos = aRelativePath.indexOf(".class");
                if (aClassSuffixPos >= 0)
                    aRelativePath = aRelativePath.substring(0, aClassSuffixPos);

                pClassNames.add(aRelativePath.replace(File.separatorChar, '.'));
            }
        }

        @Override
        public String toString()
        {
            return fDirectory.getAbsolutePath();
        }
    }
}
