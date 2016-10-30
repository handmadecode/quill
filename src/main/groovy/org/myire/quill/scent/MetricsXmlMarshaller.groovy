/*
 * Copyright 2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

package org.myire.quill.scent

import org.myire.scent.metrics.AggregatedMetrics
import org.myire.scent.metrics.CodeElementMetrics
import org.myire.scent.metrics.CommentMetrics
import org.myire.scent.metrics.CompilationUnitMetrics
import org.myire.scent.metrics.FieldMetrics
import org.myire.scent.metrics.MethodMetrics
import org.myire.scent.metrics.PackageMetrics
import org.myire.scent.metrics.StatementElementMetrics
import org.myire.scent.metrics.TypeMetrics


/**
 * Abstract base class for XML marshallers of source code metrics.
 *
 * @param <T>   The type of metrics the marshaller operates on.
 */
abstract class MetricsXmlMarshaller<T>
{
    private final String fNodeName;
    private final String fIterableNodeName;

    /**
     * Create a new {@code MetricsXmlMarshaller}.
     *
     * @param pNodeName         The name of nodes marshalled by this instance.
     * @param pIterableNodeName The name of wrapper nodes containing nodes marshalled by this
     *                          instance.
     */
    protected MetricsXmlMarshaller(String pNodeName, String pIterableNodeName)
    {
        fNodeName = pNodeName
        fIterableNodeName = pIterableNodeName
    }


    /**
     * Marshal a metrics instance.
     *
     * @param pMetrics  The instance to marshal.
     *
     * @return  A {@code Node} with the XML representation of {@code pMetrics}, never null.
     */
    Node marshal(T pMetrics)
    {
        Node aNode = new Node(null, fNodeName, createNodeAttributes(pMetrics));
        marshalChildren(aNode, pMetrics);
        return aNode;
    }


    /**
     * Marshal a sequence of metrics instances.
     *
     * @param pMetrics  The instances to marshal.
     *
     * @return  A {@code Node} containing one node per instance in {@code pMetrics}. If
     *          {@code pMetrics} doesn't contain any elements, null is returned.
     */
    Node marshal(Iterable<T> pMetrics)
    {
        Node aWrapperNode = null;
        String aNodeName = fIterableNodeName;
        pMetrics.each
        {
            if (aWrapperNode == null)
                aWrapperNode = new Node(null, aNodeName);

            // Marshal the T instance into a node and append that node to the wrapper node.
            aWrapperNode.append(marshal(it));
        }

        return aWrapperNode;
    }


    /**
     * Append child nodes marshalled from a {@code T} instance to a parent node. This base
     * implementation appends a summary node with the aggregation of the {@code T} instance as
     * returned by {@code createAggregatedMetrics()}.
     *
     * @param pNode     The parent node.
     * @param pMetrics  The metrics to marshal the children of.
     */
    void marshalChildren(Node pNode, T pMetrics)
    {
        maybeAppend(pNode, createSummaryNode(createAggregatedMetrics(pMetrics)));
    }


    /**
     * Create the attributes for the XML representation of a {@code T} instance.
     *
     * @param pMetrics  The {@code T} instance.
     *
     * @return  A map with the attributes, possibly empty, never null.
     */
    abstract Map<?, ?> createNodeAttributes(T pMetrics);

    /**
     * Create an {@code AggregatedMetrics} to use when creating the summary node for a marshalled
     * {@code T} instance.
     *
     * @param pMetrics  The {@code T} instance.
     *
     * @return  An {@code AggregatedMetrics} instance, or null if the marshalled node shouldn't have
     *          a summary node.
     */
    abstract AggregatedMetrics createAggregatedMetrics(T pMetrics);


    /**
     * Create a summary XML node from an {@code AggregatedMetrics}.
     *
     * @param pMetrics  The metrics to create the node from.
     *
     * @return  A new {@code Node}, or null if {@code pMetrics} is null.
     */
    static Node createSummaryNode(AggregatedMetrics pAggregation)
    {
        if (pAggregation == null)
            return null;

        Map<?, ?> aAttributes = [:];
        addNonZeroAttribute(aAttributes, 'packages', pAggregation.numPackages);
        addNonZeroAttribute(aAttributes, 'compilation-units', pAggregation.numCompilationUnits);
        addNonZeroAttribute(aAttributes, 'types', pAggregation.numTypes);
        addNonZeroAttribute(aAttributes, 'methods', pAggregation.numMethods);
        addNonZeroAttribute(aAttributes, 'fields', pAggregation.numFields);
        addNonZeroAttribute(aAttributes, 'statements', pAggregation.numStatements);
        addNonZeroAttribute(aAttributes, 'line-comments', pAggregation.numLineComments);
        addNonZeroAttribute(aAttributes, 'block-comments', pAggregation.numBlockComments);
        addNonZeroAttribute(aAttributes, 'block-comment-lines', pAggregation.numBlockCommentLines);
        addNonZeroAttribute(aAttributes, 'javadoc-comments', pAggregation.numJavaDocComments);
        addNonZeroAttribute(aAttributes, 'javadoc-lines', pAggregation.numJavaDocLines);

        return new Node(null, 'summary', aAttributes);
    }


    /**
     * Append a child node to a parent node if the former is non-null.
     *
     * @param pParent   The parent node.
     * @param pChild    The child node.
     */
    static void maybeAppend(Node pParent, Node pChild)
    {
        if (pChild != null)
            pParent.append(pChild);
    }


    /**
     * Add an integer attribute to an attributes map if the value is non-zero.
     *
     * @param pAttributes   The attributes map.
     * @param pKey          The attribute key.
     * @param pValue        The attribute value.
     */
    static void addNonZeroAttribute(Map<?, ?> pAttributes, String pKey, int pValue)
    {
        if (pValue != 0)
            pAttributes[pKey] = pValue;
    }


    /**
     * Abstract base class for marshallers of {@code CodeElementMetrics} subclasses.
     *
     * @param <T>   The {@code CodeElementMetrics} subclass the marshaller operates on.
     */
    abstract static class CodeElementMetricsMarshaller<T extends CodeElementMetrics> extends MetricsXmlMarshaller<T>
    {
        /**
         * Create a new {@code CodeElementMetricsMarshaller}.
         *
         * @param pNodeName         The name of nodes marshalled by this instance.
         * @param pIterableNodeName The name of wrapper nodes containing nodes marshalled by this
         *                          instance.
         */
        protected CodeElementMetricsMarshaller(String pNodeName, String pIterableNodeName)
        {
            super(pNodeName, pIterableNodeName)
        }

        @Override
        void marshalChildren(Node pNode, T pMetrics)
        {
            // Append the summary node from the superclass and a comments node if the comment
            // metrics are non-empty.
            super.marshalChildren(pNode, pMetrics);
            maybeAppend(pNode, createCommentsNode(pMetrics.comments));
        }

        /**
         * Create an XML node from a {@code CommentMetrics}.
         *
         * @param pComments The comment metrics.
         *
         * @return  A new {@code Node}, or null if the {@code CommentMetrics} is empty.
         */
        static private Node createCommentsNode(CommentMetrics pComments)
        {
            if (pComments.empty)
                return null;

            Map<?, ?> aAttributes = [:];
            addNonZeroAttribute(aAttributes, 'line-comments', pComments.numLineComments);
            addNonZeroAttribute(aAttributes, 'block-comments', pComments.numBlockComments);
            addNonZeroAttribute(aAttributes, 'block-comment-lines', pComments.numBlockCommentLines);
            addNonZeroAttribute(aAttributes, 'javadoc-comments', pComments.numJavaDocComments);
            addNonZeroAttribute(aAttributes, 'javadoc-lines', pComments.numJavaDocLines);

            return new Node(null, 'comments', aAttributes);
        }
    }


    /**
     * Abstract base class for marshallers of {@code StatementElementMetrics} subclasses.
     *
     * @param <T>   The {@code StatementElementMetrics} subclass the marshaller operates on.
     */
    abstract static class StatementElementMetricsMarshaller<T extends StatementElementMetrics>
            extends CodeElementMetricsMarshaller<T>
    {
        /**
         * Create a new {@code StatementElementMetricsMarshaller}.
         *
         * @param pNodeName         The name of nodes marshalled by this instance.
         * @param pIterableNodeName The name of wrapper nodes containing nodes marshalled by this
         *                          instance.
         */
        protected StatementElementMetricsMarshaller(String pNodeName, String pIterableNodeName)
        {
            super(pNodeName, pIterableNodeName)
        }

        @Override
        void marshalChildren(Node pNode, T pMetrics)
        {
            // Append the summary and comments nodes from the superclass.
            super.marshalChildren(pNode, pMetrics);

            // Append a statements node if the metrics has statements.
            int aNumStatements = pMetrics.statements.numStatements;
            if (aNumStatements > 0)
                pNode.append(new Node(null, 'statements', ['count' : aNumStatements]));
        }
    }


    /**
     * Marshaller of {@code PackageMetrics} instances.
     */
    static class PackageMetricsMarshaller extends MetricsXmlMarshaller<PackageMetrics>
    {
        // This class has no state.
        static PackageMetricsMarshaller SINGLETON = new PackageMetricsMarshaller();

        private PackageMetricsMarshaller()
        {
            super('package', 'packages');
        }

        @Override
        Map<?, ?> createNodeAttributes(PackageMetrics pPackage)
        {
            return ['name' : pPackage.name];
        }

        @Override
        AggregatedMetrics createAggregatedMetrics(PackageMetrics pPackage)
        {
            return AggregatedMetrics.ofChildren(pPackage);
        }

        @Override
        void marshalChildren(Node pNode, PackageMetrics pPackage)
        {
            // Append the summary node from the superclass and a node for the compilation units of
            // the package, if any.
            super.marshalChildren(pNode, pPackage);
            maybeAppend(pNode, CompilationUnitMetricsMarshaller.SINGLETON.marshal(pPackage.compilationUnits));
        }
    }


    /**
     * Marshaller of {@code CompilationUnitMetrics} instances.
     */
    static class CompilationUnitMetricsMarshaller extends CodeElementMetricsMarshaller<CompilationUnitMetrics>
    {
        // This class has no state.
        static CompilationUnitMetricsMarshaller SINGLETON = new CompilationUnitMetricsMarshaller();

        private CompilationUnitMetricsMarshaller()
        {
            super('compilation-unit', 'compilation-units');
        }

        @Override
        Map<?, ?> createNodeAttributes(CompilationUnitMetrics pCompilationUnit)
        {
            return ['name' : pCompilationUnit.name];
        }

        @Override
        AggregatedMetrics createAggregatedMetrics(CompilationUnitMetrics pCompilationUnit)
        {
            return AggregatedMetrics.ofChildren(pCompilationUnit);
        }

        @Override
        void marshalChildren(Node pNode, CompilationUnitMetrics pCompilationUnit)
        {
            // Append the summary and comments nodes from the superclass and a node for the types of
            // the compilation unit, if any.
            super.marshalChildren(pNode, pCompilationUnit);
            maybeAppend(pNode, TypeMetricsMarshaller.SINGLETON.marshal(pCompilationUnit.types));
        }
    }


    /**
     * Marshaller of {@code TypeMetrics} instances.
     */
    static class TypeMetricsMarshaller extends CodeElementMetricsMarshaller<TypeMetrics>
    {
        // Stateless class.
        static TypeMetricsMarshaller SINGLETON = new TypeMetricsMarshaller();

        private TypeMetricsMarshaller()
        {
            super('type', 'types');
        }

        @Override
        Map<?, ?> createNodeAttributes(TypeMetrics pType)
        {
            return ['name' : pType.name, 'kind' : pType.kind];
        }

        @Override
        AggregatedMetrics createAggregatedMetrics(TypeMetrics pType)
        {
            return AggregatedMetrics.ofChildren(pType);
        }

        @Override
        void marshalChildren(Node pNode, TypeMetrics pType)
        {
            // Append the summary and comments nodes from the superclass.
            super.marshalChildren(pNode, pType);

            // Append nodes for the fields, methods, and inner types of the type, if any.
            maybeAppend(pNode, MethodMetricsMarshaller.SINGLETON.marshal(pType.methods));
            maybeAppend(pNode, FieldMetricsMarshaller.SINGLETON.marshal(pType.fields));
            maybeAppend(pNode, marshal(pType.innerTypes));
        }
    }


    /**
     * Marshaller of {@code MethodMetrics} instances.
     */
    static class MethodMetricsMarshaller extends StatementElementMetricsMarshaller<MethodMetrics>
    {
        // This class has no state.
        static MethodMetricsMarshaller SINGLETON = new MethodMetricsMarshaller();

        private MethodMetricsMarshaller()
        {
            super('method', 'methods');
        }

        @Override
        Map<?, ?> createNodeAttributes(MethodMetrics pMethod)
        {
            return ['name' : pMethod.name, 'kind' : pMethod.kind];
        }

        @Override
        AggregatedMetrics createAggregatedMetrics(MethodMetrics pMethod)
        {
            // Only create a summary node if the method has local types, otherwise the summary is
            // trivial.
            return pMethod.numLocalTypes > 0 ? AggregatedMetrics.ofChildren(pMethod) : null;
        }

        @Override
        void marshalChildren(Node pNode, MethodMetrics pMethod)
        {
            // Append the summary, comments, and statements nodes from the superclass and nodes for
            // the local types of the method, if any.
            super.marshalChildren(pNode, pMethod);
            maybeAppend(pNode, TypeMetricsMarshaller.SINGLETON.marshal(pMethod.localTypes));
        }
    }


    /**
     * Marshaller of {@code FieldMetrics} instances.
     */
    static class FieldMetricsMarshaller extends StatementElementMetricsMarshaller<FieldMetrics>
    {
        // Stateless class.
        static FieldMetricsMarshaller SINGLETON = new FieldMetricsMarshaller();

        private FieldMetricsMarshaller()
        {
            super('field', 'fields');
        }

        @Override
        Map<?, ?> createNodeAttributes(FieldMetrics pField)
        {
            return ['name' : pField.name, 'kind' : pField.kind];
        }

        @Override
        AggregatedMetrics createAggregatedMetrics(FieldMetrics pField)
        {
            // No summary node for field metrics as the only children are statements and comments.
            return null;
        }
    }
}
