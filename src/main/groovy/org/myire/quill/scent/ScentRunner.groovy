/*
 * Copyright 2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent

import java.nio.charset.Charset
import java.nio.file.Files

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import org.myire.scent.metrics.AggregatedMetrics
import org.myire.scent.metrics.PackageMetrics


/**
 * Delegate class for running Scent and converting the collected metrics to XML. This class
 * encapsulates all dependencies on the Scent library, making it possible to easily control when
 * those classes are first referenced and loaded.
 */
class ScentRunner
{
    private final Logger fLogger = Logging.getLogger(ScentRunner.class);


    /**
     * Scan the directories and files in a collection of Java files, collect source code metrics
     * for them, and return an XML representation of the collected metrics.
     *
     * @param pFiles    The files and/or directories to collect Java file metrics from.
     * @param pCharset  The charset the Java files are encoded with.
     *
     * @return  A {@code Node} with the collected metrics, never null.
     */
    Node collectMetricsAsXml(Collection<File> pFiles, Charset pCharset)
    {
        LoggingFileMetricsCollector aCollector = new LoggingFileMetricsCollector(pCharset);
        pFiles.each
        {
            try
            {
                Files.walkFileTree(it.toPath(), aCollector);
            }
            catch (IOException e)
            {
                // Log and continue with the next file.
                fLogger.error('Error when collecting metrics from ' + it + ': ' + e.getMessage());
            }
        }

        return toXml(aCollector.collectedMetrics);
    }


    /**
     * Marshal a sequence of {@code PackageMetrics} into the root node of a Scent source code
     * metrics report.
     *
     * @param pPackages The metrics to marshal.
     *
     * @return  A new {@code Node}, never null.
     */
    static private Node toXml(Iterable<PackageMetrics> pPackages)
    {
        Node aRootNode = new Node(null, 'scent-report', createRootNodeAttributes());

        // Create a summary node with the aggregated metrics of all the packages and add it to the
        // root node.
        Node aChild = MetricsXmlMarshaller.createSummaryNode(AggregatedMetrics.of(pPackages));
        if (aChild != null)
            aRootNode.append(aChild);

        // Create a 'packages' node containing an element for each package and add it to the root
        // node.
        aChild = MetricsXmlMarshaller.PackageMetricsMarshaller.SINGLETON.marshal(pPackages);
        if (aChild != null)
            aRootNode.append(aChild);

        return aRootNode;
    }


    /**
     * Create attributes for the XML root node. These attributes contain the report timestamp and,
     * if available, the Scent version.
     *
     * @return  A map with the root node attributes, never null.
     */
    static private Map<?, ?> createRootNodeAttributes()
    {
        Date aNow = new Date();
        Map<?, ?> aAttributes = ['date' : aNow.format("yyyy-MM-dd"), 'time' : aNow.format("HH:mm:ss")];

        String aVersion = org.myire.scent.Main.class.getPackage()?.implementationVersion;
        if (aVersion != null)
            aAttributes['version'] = aVersion;

        return aAttributes;
    }
}
