/*
 * Copyright 2015, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.myire.quill.common.Projects;


/**
 * A report builder incrementally writes the contents of a report to its destination.
 */
public class ReportBuilder
{
    static private final Logger cLogger = Logging.getLogger(ReportBuilder.class);

    static private final TransformerFactory cFactory = TransformerFactory .newInstance();


    // The report file this builder is creating.
    private final File fDestination;

    // An open (unless close() has been called) output stream to the destination file.
    private final OutputStream fOutputStream;


    /**
     * Create a new {@code ReportBuilder}.
     *
     * @param pDestination  The file to write the report to.
     *
     * @throws FileNotFoundException  if {@code pDestination} cannot be created or opened.
     * @throws NullPointerException if {@code pDestination} is null.
     */
    public ReportBuilder(File pDestination) throws FileNotFoundException
    {
        fDestination = pDestination;
        Projects.ensureParentExists(pDestination);
        fOutputStream = new FileOutputStream(pDestination);
    }


    /**
     * Get the file this builder is creating.
     *
     * @return  The report file being created.
     */
    public File getDestination()
    {
        return fDestination;
    }


    /**
     * Close the builder's underlying destination. Future calls to other methods on this instance
     * will fail.
     */
    public void close()
    {
        try
        {
            fOutputStream.close();
        }
        catch (IOException ioe)
        {
            cLogger.error("Failed to close report file", ioe);
        }
    }


    /**
     * Write a string to this builder's destination. The string will be encoded with the platform's
     * default charset.
     *
     * @param pString   The string to write.
     */
    public void write(String pString)
    {
        write(pString, Charset.defaultCharset());
    }


    /**
     * Write a string to this builder's destination.
     *
     * @param pString   The string to write.
     * @param pCharset  The charset to encode the string with.
     */
    public void write(String pString, Charset pCharset)
    {
        try
        {
            fOutputStream.write(pString.getBytes(pCharset));
        }
        catch (IOException ioe)
        {
            cLogger.error("Failed to write string '{}'", pString, ioe);
        }
    }


    /**
     * Copy the contents of a file to this builder's destination.
     *
     * @param pFile The file to copy.
     */
    public void copy(File pFile)
    {
        try
        {
            Files.copy(pFile.toPath(), fOutputStream);
        }
        catch (IOException ioe)
        {
            cLogger.error("Failed to copy file '{}'", pFile.getAbsolutePath(), ioe);
        }
    }


    /**
     * Copy the contents of a classpath resource to this builder's destination. The resource will be
     * accessed through the class loader of this builder.
     *
     * @param pResource The name of the resource to copy.
     */
    public void copy(String pResource)
    {
        try (InputStream aResourceStream = getClass().getResourceAsStream(pResource))
        {
            if (aResourceStream != null)
            {
                int aNumBytes;
                byte[] aBuffer = new byte[8192];
                while ((aNumBytes = aResourceStream.read(aBuffer)) >= 0)
                    fOutputStream.write(aBuffer, 0, aNumBytes);
            }
            else
                cLogger.debug("Resource '{}' is not available, skipping", pResource);
        }
        catch (IOException ioe)
        {
            cLogger.error("Failed to copy resource '{}'", pResource, ioe);
        }
    }


    /**
     * Transform an XML file by applying the style sheet from an XSL file and write the result to
     * this builder's destination.
     *
     * @param pXmlFile      The XML file to transform.
     * @param pXslFile      The XSL file with the style sheet to apply.
     * @param pParameters   Any XSL parameters to pass to the transformation.
     */
    public void transform(File pXmlFile, File pXslFile, Map<String, Object> pParameters)
    {
        Transformer aTransformer = createTransformer(pXslFile);
        if (aTransformer != null)
            doTransform(aTransformer, pXmlFile, pParameters);
    }


    /**
     * Transform an XML file by applying the style sheet from an XSL resource and write the result
     * to this builder's destination. The resource will be accessed through the class loader of this
     * builder.
     *
     * @param pXmlFile      The XML file to transform.
     * @param pXslResource  The XSL resource with the style sheet to apply.
     * @param pParameters   Any XSL parameters to pass to the transformation.
     */
    public void transform(File pXmlFile, String pXslResource, Map<String, Object> pParameters)
    {
        Transformer aTransformer = createTransformer(pXslResource);
        if (aTransformer != null)
            doTransform(aTransformer, pXmlFile, pParameters);
    }


    /**
     * Transform an XML string by applying the style sheet from an XSL resource and write the result
     * to this builder's destination. The resource will be accessed through the class loader of this
     * builder.
     *
     * @param pXml          The XML string to transform.
     * @param pXslResource  The XSL resource with the style sheet to apply.
     * @param pParameters   Any XSL parameters to pass to the transformation.
     */
    public void transform(String pXml, String pXslResource, Map<String, Object> pParameters)
    {
        Transformer aTransformer = createTransformer(pXslResource);
        if (aTransformer != null)
            doTransform(aTransformer, pXml, pParameters);
    }


    /**
     * Perform an XSL transformation.
     *
     * @param pTransformer  The transformer to use.
     * @param pXmlFile      The XML file to transform.
     * @param pParameters   Any XSL parameters to pass to the transformation.
     */
    private void doTransform(Transformer pTransformer, File pXmlFile, Map<String, Object> pParameters)
    {
        // Set the XSL parameters holding the date and time of the XML file's last modification
        // timestamp, those parameters are used by the built-in XSL style sheets.
        LocalDateTime aLastModified =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(pXmlFile.lastModified()), ZoneId.systemDefault());
        pTransformer.setParameter("xml-modified-date", DateTimeFormatter.ISO_LOCAL_DATE.format(aLastModified));
        pTransformer.setParameter("xml-modified-time", DateTimeFormatter.ISO_LOCAL_TIME.format(aLastModified));

        // Set any additional XSL parameters.
        if (pParameters != null)
            pParameters.forEach(pTransformer::setParameter);

        // Wrap the XML file in a stream that filters out any DOCTYPE XML declaration to avoid
        // potential network access when validating the input XML.
        try (InputStream aFileStream = new DocTypeFilterStream(new FileInputStream(pXmlFile)))
        {
            pTransformer.transform(new StreamSource(aFileStream), new StreamResult(fOutputStream));
        }
        catch (IOException e)
        {
            cLogger.error("Failed to access XML file '{}'", pXmlFile.getAbsolutePath(), e);
        }
        catch (TransformerException e)
        {
            cLogger.error("Failed to transform XML file '{}'", pXmlFile.getAbsolutePath(), e);
        }
    }


    /**
     * Perform an XSL transformation.
     *
     * @param pTransformer  The transformer to use.
     * @param pXml          The XML string to transform.
     * @param pParameters   Any XSL parameters to pass to the transformation.
     */
    private void doTransform(Transformer pTransformer, String pXml, Map<String, Object> pParameters)
    {
        // Set any additional XSL parameters.
        if (pParameters != null)
            pParameters.forEach(pTransformer::setParameter);

        // Wrap the XML file in a stream that filters out any DOCTYPE XML declaration to avoid
        // potential network access when validating the input XML.
        try
        {
            pTransformer.transform(new StreamSource(pXml), new StreamResult(fOutputStream));
        }
        catch (TransformerException e)
        {
            cLogger.error("Failed to transform XML '{}'", pXml, e);
        }
    }


    /**
     * Create a {@code Transformer} from an XSL file.
     *
     * @param pXslFile  The XSL file.
     *
     * @return  A new {@code Transformer}, or null if an error occurs.
     */
    static private Transformer createTransformer(File pXslFile)
    {
        try
        {
            cLogger.debug("Creating transformer from file '{}'", pXslFile.getAbsolutePath());
            return cFactory.newTransformer(new StreamSource(pXslFile));
        }
        catch (TransformerException e)
        {
            cLogger.error("Failed to create a transformer from file '{}'", pXslFile.getAbsolutePath(), e);
            return null;
        }
    }


    /**
     * Create a {@code Transformer} from an XSL resource on the classpath. The resource will be
     * accessed using the class loader of {@code ReportBuilder}.
     *
     * @param pXslResource  The name of the XSL resource.
     *
     * @return  A new {@code Transformer}, or null if an error occurs.
     */
    static private Transformer createTransformer(String pXslResource)
    {
        cLogger.debug("Loading XSL resource '{}'", pXslResource);

        // Try to load the resource.
        try (InputStream aResourceStream = ReportBuilder.class.getResourceAsStream(pXslResource))
        {
            if (aResourceStream != null)
            {
                // Resource loaded, create a transformer from it.
                cLogger.debug("Creating transformer from XSL resource '{}'", pXslResource);
                return cFactory.newTransformer(new StreamSource(aResourceStream));
            }
            else
                // Resource not found.
                cLogger.error("Could not load XSL resource '{}'", pXslResource);
        }
        catch (IOException ioe)
        {
            cLogger.error("Could not load XSL resource '{}'", pXslResource, ioe);
        }
        catch (TransformerException te)
        {
            // Malformed XSL in the resource.
            cLogger.error("Failed to create a transformer from resource '{}'", pXslResource, te);
        }

        return null;
    }
}
