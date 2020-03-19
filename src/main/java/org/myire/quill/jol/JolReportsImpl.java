/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jol;

import org.myire.quill.report.AbstractXmlHtmlReportSet;


/**
 * Default implementation of {@code JolReports}.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class JolReportsImpl extends AbstractXmlHtmlReportSet<JolReports> implements JolReports
{
    static private final String REPORT_SET_NAME = "Jol";
    static private final String BUILTIN_XSL = "/org/myire/quill/rsrc/report/jol/jol.xsl";


    /**
     * Create a new {@code JolReportsImpl}.
     *
     * @param pTask The task that owns this reports instance.
     *
     * @throws NullPointerException if {@code pTask} is null.
     */
    JolReportsImpl(JolTask pTask)
    {
        super(pTask, REPORT_SET_NAME, BUILTIN_XSL);
    }


    @Override
    protected JolReports self()
    {
        return this;
    }
}
