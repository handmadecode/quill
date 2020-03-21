/*
 * Copyright 2016, 2018-2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent;

import org.myire.quill.report.AbstractXmlHtmlReportSet;


/**
 * Default implementation of {@code ScentReports}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
class ScentReportsImpl extends AbstractXmlHtmlReportSet<ScentReports> implements ScentReports
{
    static private final String REPORT_SET_NAME = "Scent";
    static private final String BUILTIN_SCENT_XSL = "/org/myire/quill/rsrc/report/scent/scent.xsl";


    /**
     * Create a new {@code ScentReportsImpl}.
     *
     * @param pTask The task that owns this reports instance.
     *
     * @throws NullPointerException if {@code pTask} is null.
     */
    ScentReportsImpl(ScentTask pTask)
    {
        super(pTask, REPORT_SET_NAME, BUILTIN_SCENT_XSL);
    }


    @Override
    protected ScentReports self()
    {
        return this;
    }
}
