/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.report;

import java.lang.reflect.Method;

import org.gradle.api.Task;
import org.gradle.api.internal.DefaultDomainObjectCollection;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.internal.TaskReportContainer;


/**
 * A report container that allows reports to be added after creation.
 */
public class MutableReportContainer extends TaskReportContainer<Report>
{
    static private final Method cAddReportMethod = getAddReportMethod();


    /**
     * Create a new {@code AdditionalReportContainerImpl}.
     *
     * @param pTask The task that owns this reports instance.
     */
    public MutableReportContainer(Task pTask)
    {
        super(ConfigurableReport.class, pTask);
    }


    /**
     * Add a report to this container
     *
     * @param pReport   The report to add.
     *
     * @return  True if the report was added, false if the operation failed for some reason.
     */
    public boolean addReport(Report pReport)
    {
        if (cAddReportMethod == null)
            return false;

        try
        {
            cAddReportMethod.invoke(this, pReport);
            return true;
        }
        catch (Exception ignore)
        {
            return false;
        }
    }


    /**
     * Get the method to invoke when adding reports.
     *
     * @return  The method to invoke when adding reports, or null if not found or accessible.
     */
    static private Method getAddReportMethod()
    {
        try
        {
            // The method DefaultDomainObjectCollection.doAdd() bypasses the mutability check, but
            // is private and must be made accessible.
            Method aDoAddMethod = DefaultDomainObjectCollection.class.getDeclaredMethod("doAdd", Object.class);
            if (aDoAddMethod != null)
                // Change the private access and call the method to add the report.
                aDoAddMethod.setAccessible(true);

            return aDoAddMethod;
        }
        catch (Exception ignore)
        {
            // Fail silently.
            return null;
        }
    }
}
