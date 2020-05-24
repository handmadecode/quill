/*
 * Copyright 2016, 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.pom

import groovy.xml.QName

import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.tasks.GenerateMavenPom

import org.myire.quill.common.Projects


/**
 * Subclass of {@code GenerateMavenPom} that allows adding static content to the pom file and
 * filtering dependencies based on Maven scope.
 */
class PomFileTask extends GenerateMavenPom
{
    private final List<File> fStaticPomDataFiles = [];
    private final Set<String> fExcludedScopes = [];


    @Override
    File getDestination()
    {
        File aDestination = super.getDestination();
        return aDestination ?: createDefaultPomFileSpec();
    }


    @Override
    void setPom(MavenPom pPom)
    {
        super.setPom(pPom);
        pPom.withXml { modifyPomXml(it.asNode()) }
    }


    /**
     * Specify one or more file paths from which static pom data should be loaded in
     * {@code createPomFile()}.
     *
     * @param pPaths    The file path(s) to load the static pom data from. Each path will be
     *                  resolved relative to the project directory.
     *
     * @return  This instance.
     */
    PomFileTask from(Object... pPaths)
    {
        for (Object aPath : pPaths)
        {
            File aResolvedFile = project.file(aPath);
            fStaticPomDataFiles.add(aResolvedFile);
            inputs.file(aResolvedFile);
        }

        return this;
    }


    /**
     * Specify one or more dependency scopes to exclude from the pom file.
     *
     * @param pScopes   The name(s) of the scope(s) to exclude.
     *
     * @return  This instance.
     */
    PomFileTask withoutScope(String... pScopes)
    {
        for (String aScope : pScopes)
        {
            fExcludedScopes.add(aScope);
            inputs.property(aScope, Boolean.TRUE);
        }

        return this;
    }


    /**
     * Modify the pom file's XML representation by adding static XML data from the files specified
     * in {@code from}. Also filter out any dependency nodes with a scope that has been specified in
     * a call to {@code withoutScope}.
     *
     * @param pRootNode The pom's root node.
     */
    void modifyPomXml(Node pRootNode)
    {
        // Filter out dependencies with a scope specified in withoutScope().
        filterDependencies(pRootNode);

        // Add nodes from the static XML data files.
        addStaticPomData(pRootNode);
    }


    /**
     * Add static pom data from the file(s) specified in calls to {@code from} to the root node of a
     * pom's XML representation.
     *
     * @param pRootNode The pom's root node.
     */
    private void addStaticPomData(Node pRootNode)
    {
        XmlParser aParser = new XmlParser();
        fStaticPomDataFiles.each
        {
            // The root element of the file should not be added, only its child elements.
            aParser.parse(it).children().each { pRootNode.append((Node) it) }
        }
    }


    /**
     * Filter out any dependency nodes with a scope that has been specified in a call to
     * {@code withoutScope} from a pom file's XML representation.
     *
     * @param pRootNode The pom's root node.
     */
    private void filterDependencies(Node pRootNode)
    {
        if (fExcludedScopes.isEmpty())
            // No scopes to filter out
            return;

        // Apply the filter to all 'dependencies' nodes (although there should only be one).
        for (Object aChild : (NodeList) pRootNode.get('dependencies'))
        {
            Node aDependenciesNode = (Node) aChild;
            removeExcludedScopes(aDependenciesNode);
            if (aDependenciesNode.children().isEmpty())
                // All 'dependency' nodes were filtered out.
                pRootNode.remove(aDependenciesNode);

        }
    }


    /**
     * Remove all {@code dependency} nodes with a scope that has been specified in a call to
     * {@code withoutScope} from a {@code dependencies} node.
     *
     * @param pDependenciesNode The pom's {@code dependencies} node.
     */
    private void removeExcludedScopes(Node pDependenciesNode)
    {
        List<Node> aToRemove = [];
        pDependenciesNode.children().each
        {
            Node aDependency = (Node) it;
            if (aDependency.children().find { isExcludedScope((Node) it) } != null)
                // This dependency node has a scope child containing an excluded scope.
                aToRemove.add(aDependency);
        }

        aToRemove.each { pDependenciesNode.remove(it) }
    }


    /**
     * Check if an XML node is a 'scope' element specifying an excluded scope in its text.
     *
     * @param pNode The node to check.
     *
     * @return  True if the node is a scope element with an excluded scope, false if not.
     */
    boolean isExcludedScope(Node pNode)
    {
        return getLocalName(pNode.name()) == 'scope' && fExcludedScopes.contains(pNode.text());
    }


    /**
     * Create the default file specification for the pom file to create.
     *
     * @return  A specification for the default pom file.
     */
    private File createDefaultPomFileSpec()
    {
        File aPomDirectory = new File(project.buildDir, 'poms');

        BasePluginConvention aConvention =
                Projects.getConventionPlugin(project, 'base', BasePluginConvention.class);
        String aBaseName = aConvention?.archivesBaseName ?: project.name;
        String aFileName = aBaseName + '-' + project.version + '.pom';
        return new File(aPomDirectory, aFileName);
    }


    static private String getLocalName(Object pNodeName)
    {
        if (pNodeName instanceof QName)
            return ((QName) pNodeName).localPart;
        else if (pNodeName != null)
            return pNodeName.toString();
        else
            return '';
    }
}
