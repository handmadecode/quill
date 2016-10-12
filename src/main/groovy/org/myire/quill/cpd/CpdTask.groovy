/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd

import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.util.VersionNumber

import org.myire.quill.common.Projects
import org.myire.quill.report.FormatChoiceReport
import org.myire.quill.report.TransformingReport


/**
 * Task for performing copy-paste detection by calling the CPD Ant task.
 */
class CpdTask extends SourceTask implements Reporting<CpdReports>
{
    static private final String DEFAULT_TOOL_VERSION = '5.5.1'

    static private final String PMD_EXTENSION_NAME = 'pmd'

    // CPD versions where certain properties were introduced.
    static private final PropertyVersion PROPERTY_VERSION_IGNORE_LITERALS =
            new PropertyVersion('ignoreLiterals', '2.2.0');
    static private final PropertyVersion PROPERTY_VERSION_IGNORE_IDENTIFIERS =
            new PropertyVersion('ignoreIdentifiers', '2.2.0');
    static private final PropertyVersion PROPERTY_VERSION_LANGUAGE =
            new PropertyVersion('language', '3.6.0');
    static private final PropertyVersion PROPERTY_VERSION_IGNORE_ANNOTATIONS =
            new PropertyVersion('ignoreAnnotations', '5.0.1');
    static private final PropertyVersion PROPERTY_VERSION_SKIP_LEXICAL_ERRORS =
            new PropertyVersion('skipLexicalErrors', '5.1.1');
    static private final PropertyVersion PROPERTY_VERSION_SKIP_DUPLICATE_FILES =
            new PropertyVersion('skipDuplicateFiles', '5.1.1');
    static private final PropertyVersion PROPERTY_VERSION_SKIP_BLOCKS =
            new PropertyVersion('skipBlocks', '5.2.2');
    static private final PropertyVersion PROPERTY_VERSION_SKIP_BLOCKS_PATTERN =
            new PropertyVersion('skipBlocksPattern', '5.2.2');
    static private final PropertyVersion PROPERTY_VERSION_IGNORE_USINGS =
            new PropertyVersion('ignoreUsings', '5.4.1');

    static private final VersionNumber VERSION_FORMAT_CSV_LINE_COUNT =
            VersionNumber.parse('5.3.0');


    // Property accessed through getter and setter only.
    private CpdReports fReports;


    /**
     * The version of CPD to use. Default is the version specified in
     * {@code PmdExtension.toolVersion}, or, if that extension isn't available, version
     * &quot;5.2.3&quot;.
     */
    @Input
    String toolVersion

    /**
     * Classpath containing the CPD classes used by the task. Default is the CPD configuration as
     * defined by the tool version.
     */
    @InputFiles
    FileCollection cpdClasspath

    /**
     * The encoding used by CPD to read the source files and to produce the report. The platform's
     * default encoding will be used if this property isn't specified.
     */
    @Input
    @Optional
    String encoding

    /**
     * The language of the source files to analyze, e.g. &quot;cpp&quot;, &quot;java&quot;,
     * &quot;php&quot;, &quot;ruby&quot;, or &quot;ecmascript&quot;. See
     * <a href="http://pmd.sourceforge.net">the CPD documentation</a> for the list of languages
     * supported by the different versions of CPD. The default is &quot;java&quot;.
     * <p>
     * Note that this property can only be used with CPD version 3.6 or newer.
     */
    @Input
    @Optional
    String language

    /**
     * The minimum duplicate size to be reported. Defaults to 100.
     */
    @Input
    int minimumTokenCount = 100

    /**
     * If true, CPD ignores literal value differences when evaluating a duplicate block. This means
     * that {@code foo=42;} and {@code foo=43;} will be seen as equivalent. Default is false.
     * <p>
     * Note that this property can only be used with CPD version 2.2 or newer.
     */
    @Input
    boolean ignoreLiterals

    /**
     * Similar to {@code ignoreLiterals}, differences in e.g. variable names and methods names
     * will be ignored. Default is false.
     * <p>
     * Note that this property can only be used with CPD version 2.2 or newer.
     */
    @Input
    boolean ignoreIdentifiers

    /**
     * If true, annotations will be ignored. This property can be useful when analyzing J2EE code
     * where annotations become very repetitive. Default is false.
     * <p>
     * Note that this property can only be used with CPD version 5.0.1 or newer.
     */
    boolean ignoreAnnotations

    /**
     * If true, CPD will ignore multiple copies of files of the same name and length in comparison.
     * Default is false.
     * <p>
     * Note that this property can only be used with CPD version 5.1.1 or newer.
     */
    @Input
    boolean skipDuplicateFiles

    /**
     * If true, CPD will skip files which can't be tokenized due to invalid characters instead of
     * aborting the analysis. Default is false.
     * <p>
     * Note that this property can only be used with CPD version 5.1.1 or newer.
     */
    @Input
    boolean skipLexicalErrors

    /**
     * If true, skipping of blocks is enabled with the patterns specified in the
     * {@code skipBlocksPattern} property. Default is false.
     * <p>
     * Note that this property can only be used with CPD version 5.2.2 or newer.
     */
    @Input
    boolean skipBlocks

    /**
     * Specifies the pattern to find the blocks to skip when {@code skipBlocks} is true. The string
     * value contains two parts, separated by |. The first part is the start pattern, the second
     * part is the ending pattern. The default value is &quot;#if 0|#endif&quot;.
     * <p>
     * Note that this property can only be used with CPD version 5.2.2 or newer.
     */
    @Input
    @Optional
    String skipBlocksPattern

    /**
     * If true, <i>using directives</i> in C# will be ignored when comparing text. Default is false.
     * <p>
     * Note that this property can only be used with CPD version 5.4.1 or newer.
     */
    @Input
    boolean ignoreUsings


    /**
     * Create the task's report container.
     */
    void setupReports()
    {
        fReports = new CpdReportsImpl(this);

        // Only execute the task if its primary report is enabled.
        onlyIf { reports.getPrimary().enabled }

        // If any of the reports' enabled flag is modified the task should be rerun.
        inputs.property('primaryReportEnabled', { -> this.reports.getPrimary().enabled })
        inputs.property('htmlReportEnabled', { -> this.reports.getHtml().enabled })

        // The XSL file used to create the HTML report is an input to the task.
        inputs.file({ -> this.reports.getHtml().xslFile })

        // Add both reports' destination as output of this task.
        outputs.file( { -> this.reports.getPrimary().destination } );
        outputs.file( { -> this.reports.getHtml().destination } );
    }


    /**
     * Get the reports produced by this task.
     *
     * @return  The reports.
     */
    @Override
    CpdReports getReports()
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
    CpdReports reports(Closure pClosure)
    {
        return fReports.configure(pClosure);
    }


    @Override
    @InputFiles
    @SkipWhenEmpty
    FileTree getSource()
    {
        FileTree aSource = super.getSource();
        if (aSource == null || aSource.empty)
        {
            aSource = Projects.getSourceSet(project, SourceSet.MAIN_SOURCE_SET_NAME)?.allJava;
            setSource(aSource);
        }

        return aSource;
    }


    String getToolVersion()
    {
        if (toolVersion == null)
        {
            PmdExtension aPmdExtension = Projects.getExtension(project, PMD_EXTENSION_NAME, PmdExtension.class);
            toolVersion = aPmdExtension ? aPmdExtension.toolVersion : DEFAULT_TOOL_VERSION;
        }

        return toolVersion;
    }


    /**
     * Add checks of the reports to the task's up-to-date check of its outputs.
     */
    void addUpToDateCheck()
    {
        outputs.upToDateWhen {
            // Let the HTML report decide if it is up to date.
            return getReports().getHtml().checkUpToDate();
        }
    }


    /**
     * Get the default destination for the task's primary report.
     *
     * @return  The primary report's destination.
     */
    File defaultPrimaryDestination()
    {
        String aExtension = reports.getPrimary().format;
        if (aExtension == CpdReports.FORMAT_CSV_LINECOUNT)
            aExtension = CpdReports.FORMAT_CSV;

        return new File(Projects.createReportDirectorySpec(project, 'cpd'), 'cpd.' + aExtension);
    }


    /**
     * Perform copy-paste detection on the sources and produces the enabled report(s).
     */
    @TaskAction
    void analyze()
    {
        SingleFileReport aPrimaryReport = fReports.getPrimary();
        if (aPrimaryReport.enabled)
        {
            // Perform the copy-paste detection and create the primary report.
            createPrimaryReport(aPrimaryReport);

            // Create the HTML report if enabled.
            TransformingReport aHtmlReport = fReports.getHtml();
            if (aHtmlReport.enabled)
                aHtmlReport.transform();
        }
        else
            logger.info('CPD primary is report disabled, skipping analysis');
    }


    private void createPrimaryReport(FormatChoiceReport pPrimaryReport)
    {
        // Define the Ant task from the CPD classes.
        ant.taskdef(name: 'cpd',
                    classname: 'net.sourceforge.pmd.cpd.CPDTask',
                    classpath: getCpdClasspath().asPath);

        // Some properties can only be used with certain versions of PMD.
        VersionNumber aVersion = VersionNumber.parse(getToolVersion());

        // The format "csv_with_linecount_per_file" was introduced in version 5.3.
        // The CPD ant task warns about this format not being supported, but seems to produce a
        // report anyway.
        String aFormat = pPrimaryReport.format;
        if (aVersion.compareTo(VERSION_FORMAT_CSV_LINE_COUNT) < 0 && aFormat == CpdReports.FORMAT_CSV_LINECOUNT)
        {
            logger.warn('Format \'{}\' is not supported in CPD version {}, falling back to format \'{}\'. Use version {} or later to enable this format.',
                        CpdReports.FORMAT_CSV_LINECOUNT,
                        getToolVersion(),
                        CpdReports.FORMAT_CSV,
                        VERSION_FORMAT_CSV_LINE_COUNT);

            aFormat = CpdReports.FORMAT_CSV;
        }

        // Create the argument list for the ant task.
        def aArguments = [minimumTokenCount: getMinimumTokenCount(),
                          format           : aFormat];

        // Specify the output file, creating its parent directories if necessary.
        File aOutputFile = pPrimaryReport.destination;
        if (aOutputFile != null)
        {
            aOutputFile.parentFile?.mkdirs();
            aArguments['outputFile'] = aOutputFile;
        }

        if (encoding != null)
            aArguments['encoding'] = encoding;

        // Add arguments the depend on a certain version of CPD.
        addPropertyIfSupported(aArguments, language, PROPERTY_VERSION_LANGUAGE, aVersion);
        addPropertyIfSupported(aArguments, ignoreLiterals, PROPERTY_VERSION_IGNORE_LITERALS, aVersion);
        addPropertyIfSupported(aArguments, ignoreIdentifiers, PROPERTY_VERSION_IGNORE_IDENTIFIERS, aVersion);
        addPropertyIfSupported(aArguments, ignoreAnnotations, PROPERTY_VERSION_IGNORE_ANNOTATIONS, aVersion);
        addPropertyIfSupported(aArguments, skipLexicalErrors, PROPERTY_VERSION_SKIP_LEXICAL_ERRORS, aVersion);
        addPropertyIfSupported(aArguments, skipDuplicateFiles, PROPERTY_VERSION_SKIP_DUPLICATE_FILES, aVersion);
        addPropertyIfSupported(aArguments, skipBlocks, PROPERTY_VERSION_SKIP_BLOCKS, aVersion);
        addPropertyIfSupported(aArguments, skipBlocksPattern, PROPERTY_VERSION_SKIP_BLOCKS_PATTERN, aVersion);
        addPropertyIfSupported(aArguments, ignoreUsings, PROPERTY_VERSION_IGNORE_USINGS, aVersion);

        logger.debug('Invoking CPD with arguments {}', aArguments);

        // Invoke the CPD ant task.
        ant.cpd(aArguments)
        {
            // Specify the sources as an Ant file set.
            getSource()?.addToAntBuilder(ant, "fileset", FileCollection.AntType.FileSet);
        }
    }


    /**
     * Add a string property to an argument map if the property in question is supported by the CPD
     * task in the specified version.
     *
     * @param pMap              The argument map.
     * @param pValue            The property value to conditionally add.
     * @param pPropertyVersion  The CPD version where the property was introduced.
     * @param pToolVersion      The version of CPD that the argument map will be used with.
     */
    static private void addPropertyIfSupported(Map<String, ?> pMap,
                                               String pValue,
                                               PropertyVersion pPropertyVersion,
                                               VersionNumber pToolVersion)
    {
        if (pValue != null && pToolVersion.compareTo(pPropertyVersion.fVersion) >= 0)
            pMap[pPropertyVersion.fPropertyName] = pValue;
    }


    /**
     * Add a boolean property to an argument map if the property in question is supported by the CPD
     * task in the specified version.
     *
     * @param pMap              The argument map.
     * @param pValue            The property value to conditionally add.
     * @param pPropertyVersion  The CPD version where the property was introduced.
     * @param pToolVersion      The version of CPD that the argument map will be used with.
     */
    static private void addPropertyIfSupported(Map<String, ?> pMap,
                                               boolean pValue,
                                               PropertyVersion pPropertyVersion,
                                               VersionNumber pToolVersion)
    {
        if (pToolVersion.compareTo(pPropertyVersion.fVersion) >= 0)
            pMap[pPropertyVersion.fPropertyName] = pValue;
    }


    /**
     * An association between a CPD property and the tool version in which it was introduced.
     */
    static private class PropertyVersion
    {
        final String fPropertyName;
        final VersionNumber fVersion;

        PropertyVersion(String pPropertyName, String pVersionNumber)
        {
            fPropertyName = pPropertyName
            fVersion = VersionNumber.parse(pVersionNumber);
        }
    }
}
