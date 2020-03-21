/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jol;

import static java.util.Objects.requireNonNull;


/**
 * Parameters for an analysis performed by Jol.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class JolParameters
{
    public final Layout fLayout;
    public final DataModel fDataModel;
    public final int fAlignment;


    /**
     * Create a new {@code JolParameters} instance.
     *
     * @param pLayout       The layout to use in the Jol analysis.
     * @param pDataModel    The data model to use in the Jol analysis. Ignored by the
     *                      {@link Layout#CURRENT} layout.
     * @param pAlignment    The alignment to use in the Jol analysis. This value is currently only
     *                      used by the {@link Layout#HOTSPOT} layout.
     *
     * @throws NullPointerException if {@code pLayout} or {@code pDataModel} is null.
     * */
    public JolParameters(Layout pLayout, DataModel pDataModel, int pAlignment)
    {
        fLayout = requireNonNull(pLayout);
        fDataModel = requireNonNull(pDataModel);
        fAlignment = pAlignment;
    }


    /**
     * The object layout simulation modes supported by Jol.
     */
    public enum Layout
    {
        /** Simulated HotSpot VM object layout. */
        HOTSPOT,

        /**
         * Simulated object layout which packs all the fields together, regardless of the alignment.
         */
        RAW,

        /** The executing VM's object layout. */
        CURRENT
    }


    /**
     * The data model modes supported by Jol. The data model is used to determine e.g. the sizes of
     * basic types.
     */
    public enum DataModel
    {
        /** 32 bit x86 data model. */
        x86_32,

        /** 64 bit x86 data model with normal object references. */
        x86_64,

        /** 64 bit x86 data model with compressed object references. */
        x86_64_COMPRESSED
    }
}
