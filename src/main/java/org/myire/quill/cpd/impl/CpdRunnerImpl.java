/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.function.Consumer;

import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CSVRenderer;
import net.sourceforge.pmd.cpd.CSVWithLinecountPerFileRenderer;
import net.sourceforge.pmd.cpd.Language;
import net.sourceforge.pmd.cpd.SimpleRenderer;
import net.sourceforge.pmd.cpd.VSRenderer;
import net.sourceforge.pmd.cpd.XMLRenderer;
import net.sourceforge.pmd.cpd.renderer.CPDRenderer;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import org.myire.quill.cpd.CpdParameters;
import org.myire.quill.cpd.CpdReports;
import org.myire.quill.cpd.CpdRunner;


/**
 * Delegate class for running CPD and creating a report. This class encapsulates all dependencies on
 * the CPD classes, making it possible to easily control when those classes are first referenced and
 * loaded.
 */
public class CpdRunnerImpl implements CpdRunner
{
    private final Logger fLogger = Logging.getLogger(CpdRunnerImpl.class);


    @Override
    public void runCpd(
        Collection<File> pFiles,
        File pReportFile,
        String pReportFormat,
        CpdParameters pParameters) throws IOException
    {
        // CPD scans the class path of the current thread's context class loader for language
        // implementations. This class loader must be the one that loads the CPD classes, and the
        // context class loader of the thread calling this method is normally not the CPD class
        // loader. For the language scan to work the context class loader must be temporarily
        // replaced.
        ClassLoader aContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Language.class.getClassLoader());

        // Create a CPDConfiguration from the parameter values and a CPD instance from the
        // configuration.
        CPDConfiguration aConfiguration = toConfiguration(pParameters);
        CPD aCpd = new CPD(aConfiguration);

        // Add and tokenize the files to analyze.
        fLogger.debug("Tokenizing files for copy-paste analysis");
        for (File aFile : pFiles)
            aCpd.add(aFile);

        // Detect copy-paste.
        fLogger.debug("Performing copy-paste analysis");
        aCpd.go();

        // Write the report.
        try (Writer aWriter = createReportWriter(pReportFile, aConfiguration.getEncoding()))
        {
            fLogger.debug("Writing copy-paste analysis report to {}", pReportFile.getAbsolutePath());
            createRenderer(pReportFormat).render(aCpd.getMatches(), aWriter);
        }
        finally
        {
            // Restore the current thread's context class loader.
            Thread.currentThread().setContextClassLoader(aContextClassLoader);
        }
    }


    /**
     * Create an {@code OutputStreamWriter} for a report file.
     *
     * @param pReportFile   The report file.
     * @param pEncoding     The encoding to write the file with, or null to use teh platform's
     *                      default encoding.
     * @return  A new {@code OutputStreamWriter}.
     *
     * @throws IOException  if creating the writer fails.
     */
    private OutputStreamWriter createReportWriter(File pReportFile, String pEncoding) throws IOException
    {
        if (pEncoding != null)
            return new OutputStreamWriter(new FileOutputStream(pReportFile), pEncoding);
        else
            return new OutputStreamWriter(new FileOutputStream(pReportFile));
    }


    /**
     * Create the appropriate {@code CPDRenderer} for a report file format.
     *
     * @param pReportFormat The desired report file format.
     *
     * @return  A new {@code CPDRenderer} for the specified format. If the format isn't supported,
     *          a renderer for the XML format will be returned.
     *
     * @throws NullPointerException if {@code pReportFormat} is null.
     */
    private CPDRenderer createRenderer(String pReportFormat)
    {
        switch (pReportFormat)
        {
            case CpdReports.FORMAT_XML:
                return new XMLRenderer();
            case CpdReports.FORMAT_TEXT:
                return new SimpleRenderer();
            case CpdReports.FORMAT_CSV:
                return new CSVRenderer();
            case CpdReports.FORMAT_CSV_LINECOUNT:
                return new CSVWithLinecountPerFileRenderer();
            case CpdReports.FORMAT_VS:
                return new VSRenderer();
            default:
                fLogger.warn("Unsupported report format {}, falling back to XML", pReportFormat);
                return new XMLRenderer();
        }
    }


    /**
     * Create a {@code CPDConfiguration} from the values in a {@code CpdParameters} instance.
     *
     * @param pParameters   The values to put into the configuration.
     *
     * @return  A new {@code CPDConfiguration}.
     *
     * @throws NullPointerException if {@code pParameters} is null.
     */
    private CPDConfiguration toConfiguration(CpdParameters pParameters)
    {
        CPDConfiguration aConfiguration = new CPDConfiguration();
        aConfiguration.setFailOnViolation(false);

        consumeIfNonNull(aConfiguration::setEncoding, pParameters.getEncoding());

        aConfiguration.setMinimumTileSize(pParameters.getMinimumTokenCount());

        aConfiguration.setIgnoreLiterals(pParameters.isIgnoreLiterals());
        aConfiguration.setIgnoreIdentifiers(pParameters.isIgnoreIdentifiers());
        aConfiguration.setIgnoreAnnotations(pParameters.isIgnoreAnnotations());
        aConfiguration.setIgnoreUsings(pParameters.isIgnoreUsings());

        aConfiguration.setSkipDuplicates(pParameters.isSkipDuplicateFiles());
        aConfiguration.setSkipLexicalErrors(pParameters.isSkipLexicalErrors());

        // Note that CPD uses a negative setter for skip blocks, hence the negation.
        aConfiguration.setNoSkipBlocks(!pParameters.isSkipBlocks());

        consumeIfNonNull(aConfiguration::setSkipBlocksPattern, pParameters.getSkipBlocksPattern());

        setLanguage(aConfiguration, pParameters.getLanguage());

        return aConfiguration;
    }


    /**
     * Set the language in a {@code CPDConfiguration} instance.
     *
     * @param pConfiguration    The configuration to set the language in.
     * @param pLanguage         The language as a string, for example &quot;cpp&quot;. Null is
     *                          interpreted as the default language, which is &quot;java&quot;.
     *
     * @throws NullPointerException if {@code pConfiguration} is null.
     */
    private void setLanguage(CPDConfiguration pConfiguration, String pLanguage)
    {
        if (pLanguage == null)
            pLanguage = CPDConfiguration.DEFAULT_LANGUAGE;

        pConfiguration.setLanguage(CPDConfiguration.getLanguageFromString(pLanguage));

        // The language handling needs some configuration values as properties as well.
        CPDConfiguration.setSystemProperties(pConfiguration);
    }


    private void consumeIfNonNull(Consumer<String> pSetter, String pValue)
    {
        if (pValue != null)
            pSetter.accept(pValue);
    }
}
