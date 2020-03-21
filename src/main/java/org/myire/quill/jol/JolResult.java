/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static java.util.Objects.requireNonNull;


/**
 * The result of a Jol analysis of a collection of class files. The result classes do not depend on
 * the jol-core classes and can thus be used from code executing with a class path that doesn't have
 * access to the jol-core classes.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class JolResult
{
    private final String fVersion;
    private final String fDescription;
    private final Map<String, PackageLayout> fPackages = new TreeMap<>();
    private long fInternalAlignmentGapSize;
    private long fExternalAlignmentGapSize;


    /**
     * Create a new {@code JolResult}.
     *
     * @param pVersion      The Jol version used for the analysis.
     * @param pDescription  A string describing the analysis parameters used to produce this result.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    public JolResult(String pVersion, String pDescription)
    {
        fVersion = requireNonNull(pVersion);
        fDescription = requireNonNull(pDescription);
    }


    /**
     * Get the version of Jol used for the analysis.
     *
     * @return  The Jol version string.
     */
    public String getVersion()
    {
        return fVersion;
    }


    /**
     * Get the description of the analysis parameters used to produce this result.
     *
     * @return  The analysis parameter description.
     */
    public String getDescription()
    {
        return fDescription;
    }


    /**
     * Get the number of packages in this result.
     *
     * @return  The number of packages.
     */
    public int getNumPackages()
    {
        return fPackages.size();
    }


    /**
     * Get an {@code Iterable} with the packages for which classes were analyzed. The packages will
     * be sorted on package name in ascending alphabetical order.
     *
     * @return  An {@code Iterable} of {@code PackageLayout} instances, possibly empty, never null.
     */
    public Iterable<PackageLayout> getPackages()
    {
        return fPackages.values();
    }


    /**
     * Get the sum of all detected internal alignment gap sizes in the analyzed classes.
     *
     * @return  The total internal alignment gap size.
     */
    public long getInternalAlignmentGapSize()
    {
        return fInternalAlignmentGapSize;
    }


    /**
     * Get the sum of all detected external alignment gap sizes in the analyzed classes.
     *
     * @return  The total external alignment gap size.
     */
    public long getExternalAlignmentGapSize()
    {
        return fExternalAlignmentGapSize;
    }


    /**
     * Add a {@code ClassLayout} to this result.
     *
     * @param pClassLayout  The {@code ClassLayout} to add.
     *
     * @throws NullPointerException if {@code pClassLayout} is null.
     */
    public void add(ClassLayout pClassLayout)
    {
        fPackages.computeIfAbsent(pClassLayout.getPackageName(), PackageLayout::new).add(pClassLayout);
        fInternalAlignmentGapSize += pClassLayout.getInternalAlignmentGapSize();
        fExternalAlignmentGapSize += pClassLayout.getExternalAlignmentGapSize();
    }


    /**
     * A {@code PackageLayout} contains a collection of {@code ClassLayout} for the classes in a
     * package.
     */
    static public class PackageLayout
    {
        private final String fName;
        private final List<ClassLayout> fClasses = new ArrayList<>();
        private long fInternalAlignmentGapSize;
        private long fExternalAlignmentGapSize;
        private boolean fIsSorted;

        /**
         * Create a new {@code PackageLayout}.
         *
         * @param pPackageName  The name of the package.
         *
         * @throws NullPointerException if {@code pPackageName} is null.
         */
        PackageLayout(String pPackageName)
        {
            fName = requireNonNull(pPackageName);
        }

        /**
         * Add the {@code ClassLayout} of an analyzed class to this instance.
         *
         * @param pClassLayout  The class layout.
         *
         * @throws NullPointerException if {@code pClassLayout} is null.
         */
        void add(ClassLayout pClassLayout)
        {
            fClasses.add(pClassLayout);
            fInternalAlignmentGapSize += pClassLayout.getInternalAlignmentGapSize();
            fExternalAlignmentGapSize += pClassLayout.getExternalAlignmentGapSize();
            fIsSorted = false;
        }


        /**
         * Get the name of this package.
         *
         * @return  The package name, never null.
         */
        public String getName()
        {
            return fName;
        }

        /**
         * Get the number of classes in this instance.
         *
         * @return  The number of classes.
         */
        public int getNumClasses()
        {
            return fClasses.size();
        }

        /**
         * Get an {@code Iterable} with the layouts of the classes in this package. The layouts will
         * be sorted on class name in ascending alphabetical order.
         *
         * @return  An {@code Iterable} of {@code ClassLayout} instances, possibly empty, never
         *          null.
         */
        public Iterable<ClassLayout> getClasses()
        {
            if (!fIsSorted)
            {
                fClasses.sort(Comparator.comparing(ClassLayout::getFullClassName));
                fIsSorted = true;
            }

            return fClasses;
        }

        /**
         * Get the sum of all detected internal alignment gap sizes in the package's analyzed
         * classes.
         *
         * @return  The total internal alignment gap size.
         */
        public long getInternalAlignmentGapSize()
        {
            return fInternalAlignmentGapSize;
        }

        /**
         * Get the sum of all detected external alignment gap sizes in the package's analyzed
         * classes.
         *
         * @return  The total external alignment gap size.
         */
        public long getExternalAlignmentGapSize()
        {
            return fExternalAlignmentGapSize;
        }
    }


    /**
     * Layout information for a class.
     */
    static public class ClassLayout
    {
        static private final String GAP = "gap";
        static private final String FIELD_ALIGNMENT = "internal field alignment";
        static private final String NEXT_OBJECT_ALIGNMENT = "next object alignment";

        private final String fPackageName;
        private final String fClassName;
        private final String fEnclosingClassName;
        private final String fFullClassName;
        private final String fFullyQualifiedName;
        private final Collection<FieldLayout> fFields;
        private final int fHeaderSize;
        private final long fInstanceSize;
        private final long fInternalAlignmentGapSize;
        private final long fExternalAlignmentGapSize;

        /**
         * Create a new {@code ClassLayout}.
         *
         * @param pClassName    The name of the class for which to hold layout info.
         * @param pPackageName  The name of the package the class belongs.
         * @param pEnclosingClassName
         *                      The name of any enclosing class.
         * @param pHeaderSize   The object header size of the class' instances.
         * @param pInstanceSize The size of the class' instances.
         * @param pFields       The layout of the class' individual fields.
         *
         * @throws NullPointerException if {@code pClassName}, {@code pPackageName}, or
         *                              {@code pFields} is null.
         */
        public ClassLayout(
            String pClassName,
            String pPackageName,
            String pEnclosingClassName,
            int pHeaderSize,
            long pInstanceSize,
            Collection<FieldLayout> pFields)
        {
            fClassName = requireNonNull(pClassName);
            fPackageName = requireNonNull(pPackageName);
            fEnclosingClassName = pEnclosingClassName;
            fFullClassName = fEnclosingClassName != null ? fEnclosingClassName + '.' + fClassName : fClassName;
            fFullyQualifiedName = fPackageName.isEmpty() ? fFullClassName : fPackageName + '.' + fFullClassName;

            fHeaderSize = pHeaderSize;
            fInstanceSize = pInstanceSize;
            fFields = new ArrayList<>(pFields.size());

            // Copy the field layout references to an internal list, and look for differences in
            // expected and actual offset. A difference indicates a gap caused by field alignment.
            long aNextExpectedOffset = pHeaderSize;
            long aTotalFieldAlignmentGapSizes = 0;
            for (FieldLayout aField : pFields)
            {
                long aGap = aField.getOffset() - aNextExpectedOffset;
                if (aGap > 0)
                {
                    // Field alignment gap detected, add a gap "field" to the internal list.
                    fFields.add(new FieldLayout(FIELD_ALIGNMENT, GAP, aNextExpectedOffset, aGap));
                    aTotalFieldAlignmentGapSizes += aGap;
                }

                // Add the field to the internal list.
                fFields.add(aField);
                aNextExpectedOffset = aField.getOffset() + aField.getSize();
            }

            if (pInstanceSize != aNextExpectedOffset)
            {
                // The instance size is greater than the offset of the last field's end, there is a
                // gap caused by alignment with another object that follows this one.
                fExternalAlignmentGapSize = pInstanceSize - aNextExpectedOffset;
                fFields.add(new FieldLayout(NEXT_OBJECT_ALIGNMENT, GAP, aNextExpectedOffset, fExternalAlignmentGapSize));
            }
            else
                fExternalAlignmentGapSize = 0;

            fInternalAlignmentGapSize = aTotalFieldAlignmentGapSizes;
        }

        /**
         * Get the name of the package the class belongs to. Classes in the default package have an
         * empty name.
         *
         * @return  The package name, never null.
         */
        public String getPackageName()
        {
            return fPackageName;
        }

        /**
         * Get the name of the class for which this instance contains layout info.
         *
         * @return  The class name, never null.
         */
        public String getClassName()
        {
            return fClassName;
        }

        /**
         * Get the name of the enclosing class of the class this instance contains layout info for.
         *
         * @return  The enclosing class name, or null if there is no enclosing class.
         */
        public String getEnclosingClassName()
        {
            return fEnclosingClassName;
        }

        /**
         * Get the class name prefixed with any enclosing class name.
         *
         * @return  The full class name, never null.
         */
        public String getFullClassName()
        {
            return fFullClassName;
        }

        /**
         * Get the full class name prefixed with the package name.
         *
         * @return  The fully qualified name, never null.
         */
        public String getFullyQualifiedName()
        {
            return fFullyQualifiedName;
        }

        /**
         * Get the number of fields in this class.
         *
         * @return  The number of fields.
         */
        public int getNumFields()
        {
            return fFields.size();
        }

        /**
         * Get an {@code Iterable} with the layouts of the fields in this class.
         *
         * @return  An {@code Iterable} of {@code FieldLayout} instances, possibly empty, never
         *          null.
         */
        public Iterable<FieldLayout> getFields()
        {
            return fFields;
        }

        /**
         * Get the object header size of the class.
         *
         * @return  The object header size.
         */
        public int getHeaderSize()
        {
            return fHeaderSize;
        }

        /**
         * Get the size of instances of the class.
         *
         * @return  The instance size.
         */
        public long getInstanceSize()
        {
            return fInstanceSize;
        }

        /**
         * Get the sum of all internal field alignment gap sizes.
         *
         * @return  The total field alignment gap size.
         */
        public long getInternalAlignmentGapSize()
        {
            return fInternalAlignmentGapSize;
        }

        /**
         * Get the size of any gap caused by the next object being aligned.
         *
         * @return  The external alignment gap size.
         */
        public long getExternalAlignmentGapSize()
        {
            return fExternalAlignmentGapSize;
        }
    }


    /**
     * Layout information for a field.
     */
    static public class FieldLayout
    {
        private final String fName;
        private final String fType;
        private final long fOffset;
        private final long fSize;

        /**
         * Create a new {@code FieldLayout}.
         *
         * @param pName     The name of the field.
         * @param pType     The field's type.
         * @param pOffset   The offset of the field from the object's start.
         * @param pSize     The size of the field.
         *
         * @throws NullPointerException if {@code pName} or {@code pType} is null.
         */
        public FieldLayout(String pName, String pType, long pOffset, long pSize)
        {
            fName = requireNonNull(pName);
            fType = requireNonNull(pType);
            fOffset = pOffset;
            fSize = pSize;
        }

        public String getName()
        {
            return fName;
        }

        public String getType()
        {
            return fType;
        }

        public long getOffset()
        {
            return fOffset;
        }

        public long getSize()
        {
            return fSize;
        }
    }
}
