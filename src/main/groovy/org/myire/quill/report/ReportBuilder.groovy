/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report

import java.nio.charset.Charset

import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging


/**
 * A report builder incrementally writes the contents of a report to its destination.
 */
class ReportBuilder
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
     */
    ReportBuilder(File pDestination)
    {
        fDestination = pDestination;
        pDestination.parentFile?.mkdirs();
        fOutputStream = new FileOutputStream(pDestination);
    }


    /**
     * Get the file this builder is creating.
     *
     * @return  The report file being created.
     */
    File getDestination()
    {
        return fDestination;
    }


    /**
     * Close the builder's underlying destination. Future calls to other methods on this instance
     * will fail.
     */
    void close()
    {
        fOutputStream.close();
    }


    /**
     * Write a string to this builder's destination.
     *
     * @param pString   The string to write.
     */
    void write(String pString)
    {
        try
        {
            fOutputStream.write(pString.getBytes());
        }
        catch (IOException ioe)
        {
            cLogger.error('Failed to write string \'{}\'', pString, ioe);
        }
    }


    /**
     * Write a string to this builder's destination.
     *
     * @param pString   The string to write.
     * @param pCharset  The charset to encode the string with.
     */
    void write(String pString, Charset pCharset)
    {
        try
        {
            fOutputStream.write(pString.getBytes(pCharset));
        }
        catch (IOException ioe)
        {
            cLogger.error('Failed to write string \'{}\'', pString, ioe);
        }
    }


    /**
     * Copy the contents of a file to this builder's destination.
     *
     * @param pFile The file to copy.
     */
    void copy(File pFile)
    {
        FileInputStream aInputStream = null;
        try
        {
            aInputStream = new FileInputStream(pFile);
            fOutputStream << aInputStream;
        }
        catch (IOException ioe)
        {
            cLogger.error('Failed to copy file \'{}\'', pFile.absolutePath, ioe);
        }
        finally
        {
            aInputStream?.close();
        }
    }


    /**
     * Copy the contents of a classpath resource to this builder's destination. The resource will be
     * accessed through the class loader of this builder.
     *
     * @param pResource The name of the resource to copy.
     */
    void copy(String pResource)
    {
        InputStream aResourceStream = getClass().getResourceAsStream(pResource);
        try
        {
            if (aResourceStream != null)
                fOutputStream << aResourceStream;
            else
                cLogger.debug('Resource \'{}\' is not available, skipping', pResource);
        }
        catch (IOException ioe)
        {
            cLogger.error('Failed to copy resource \'{}\'', pResource, ioe);
        }
        finally
        {
            aResourceStream?.close();
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
    void transform(File pXmlFile, File pXslFile, Map<String, Object> pParameters = null)
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
    void transform(File pXmlFile, String pXslResource, Map<String, Object> pParameters = null)
    {
        Transformer aTransformer = createTransformer(pXslResource);
        if (aTransformer != null)
            doTransform(aTransformer, pXmlFile, pParameters);
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
        Date aLastModified = new Date(pXmlFile.lastModified());
        pTransformer.setParameter('xml-modified-date', aLastModified.format("yyyy-MM-dd"));
        pTransformer.setParameter('xml-modified-time', aLastModified.format("HH:mm:ss"));

        // Set any XSL parameters passed explicitly.
        pParameters?.each{ name, value -> pTransformer.setParameter(name, value) };

        InputStream aFileStream = null;
        try
        {
            // Wrap the XML file in a stream that filters out any DOCTYPE XML declaration to avoid
            // potential network access when validating the input XML.
            aFileStream = new DocTypeFilterStream(new FileInputStream(pXmlFile));
            pTransformer.transform(new StreamSource(aFileStream), new StreamResult(fOutputStream));
        }
        catch (IOException e)
        {
            cLogger.error('Failed to access file \'{}\'', pXmlFile.absolutePath, e);
        }
        catch (TransformerException e)
        {
            cLogger.error('Failed to transform file \'{}\'', pXmlFile.absolutePath, e);
        }
        finally
        {
            aFileStream?.close();
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
            cLogger.debug('Creating transformer from file \'{}\'', pXslFile.absolutePath);
            return cFactory.newTransformer(new StreamSource(pXslFile));
        }
        catch (TransformerException e)
        {
            cLogger.error('Failed to create a transformer from file \'{}\'', pXslFile.absolutePath, e);
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
        cLogger.debug('Loading XSL resource \'{}\'', pXslResource);

        InputStream aResourceStream = null;
        try
        {
            // Try to load the resource.
            aResourceStream = ReportBuilder.class.getResourceAsStream(pXslResource);
            if (aResourceStream != null)
            {
                // Resource loaded, create a transformer from it.
                cLogger.debug('Creating transformer from XSL resource \'{}\'', pXslResource);
                return cFactory.newTransformer(new StreamSource(aResourceStream));
            }
            else
                // Resource not found.
                cLogger.error('Could not load XSL resource \'{}\'', pXslResource);
        }
        catch (TransformerException e)
        {
            // Malformed XSL in the resource.
            cLogger.error('Failed to create a transformer from resource \'{}\'', pXslResource, e);
        }
        finally
        {
            aResourceStream?.close();
        }

        return null;
    }
}
