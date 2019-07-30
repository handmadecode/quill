/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import groovy.lang.Closure;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;

import org.gradle.util.ConfigureUtil;
import org.myire.quill.common.Projects;
import org.myire.quill.common.Tasks;
import org.myire.quill.report.ReportBuilder;


/**
 * A task that creates a summary report of a collection of reports generated during a build. The
 * summary report consists of one {@code DashboardSection} for each report included in the summary.
 *<p>
 * By default the task generates a summary of the reports from the Test, Cobertura, Scent, JDepend,
 * FindBugs, Checkstyle, PMD, and CPD tasks that are available in the project. Build scripts can
 * access and modify these standard sections through the {@code sections} property, which is a map
 * from task name to {@code DashboardSection} instance. New sections can also be added to this map.
 *<p>
 * The layout of the dashboard sections can be configured through the task's {@code layout}
 * property.
 */
public class DashboardTask extends DefaultTask implements Reporting<DashboardReports>
{
    static final String TASK_NAME = "reportsDashboard";

    private boolean fVerbose = true;

    private DashboardLayout fLayout;
    private DashboardReports fReports;

    private Map<String, DashboardSection> fSections = new LinkedHashMap<>();
    private boolean fHasAddedDefaultSections;

    private DashboardSectionFactory fSectionFactory;


    /**
     * Add the default sections before configuring the task.
     *
     * @param pClosure  The closure to configure the task with.
     *
     * @return  This task.
     */
    @Override
    public Task configure(Closure pClosure)
    {
        maybeAddDefaultSections();
        return super.configure(pClosure);
    }


    /**
     * Should the path to the report file be logged after it has been created?
     *
     * @return  True if the path should be logged, false if not.
     */
    @Input
    public boolean isVerbose()
    {
        return fVerbose;
    }


    public void setVerbose(boolean pVerbose)
    {
        fVerbose = pVerbose;
    }


    /**
     * Get the layout of the dashboard.
     *
     * @return  The dashboard's layout.
     */
    @Nested
    public DashboardLayout getLayout()
    {
        return fLayout;
    }


    /**
     * Configure the dashboard's layout.
     *
     * @param pClosure A closure that configures the layout.
     *
     * @return  The dashboard's layout.
     */
    public DashboardLayout layout(Closure pClosure)
    {
        return ConfigureUtil.configureSelf(pClosure, fLayout);
    }


    /**
     * Get the reports produced by this task.
     *
     * @return The reports.
     */
    @Override
    @Nested
    public DashboardReports getReports()
    {
        return fReports;
    }


    /**
     * Configure this task's reports.
     *
     * @param pClosure A closure that configures the reports.
     *
     * @return This task's reports.
     */
    @Override
    public DashboardReports reports(Closure pClosure)
    {
        fReports.configure(pClosure);
        return fReports;
    }


    /**
     * Configure this task's reports.
     *
     * @param pAction   An action that configures the reports.
     *
     * @return  This task's reports.
     */
    @Override
    public DashboardReports reports(Action<? super DashboardReports> pAction)
    {
        pAction.execute(fReports);
        return fReports;
    }


    /**
     * Get the map with the individual dashboard sections that will be included in the report.
     *
     * @return The report's sections.
     */
    @Input
    public List<DashboardSectionSpec> getInputSections()
    {
        maybeAddDefaultSections();
        return fSections.values()
            .stream()
            .map(DashboardSectionSpec::new)
            .collect(Collectors.toList());
    }


    /**
     * Get the map with the individual dashboard sections that will be included in the report.
     *
     * @return The report's sections.
     */
    @Internal
    public Map<String, DashboardSection> getSections()
    {
        maybeAddDefaultSections();
        return fSections;
    }


    /**
     * Add or modify the dashboard section with a specific name.
     *
     * @param pName     The name of the dashboard section.
     * @param pReport   The XML report to transform.
     * @param pXslFile  The XSL file to transform the report with.
     */
    public void addSection(String pName, Report pReport, Object pXslFile)
    {
        addSection(pName, pReport, null, pXslFile);
    }


    /**
     * Add or modify the dashboard section with a specific name.
     *
     * @param pName             The name of the dashboard section.
     * @param pReport           The XML report to transform.
     * @param pDetailedReport   The detailed report to refer to from the dashboard section.
     * @param pXslFile          The XSL file to transform the report with.
     */
    public void addSection(String pName, Report pReport, Report pDetailedReport, Object pXslFile)
    {
        File aXslFile = getProject().file(pXslFile);
        fSections.put(pName, new DashboardSection(getProject(), pName, pReport, pDetailedReport, aXslFile));
    }


    /**
     * Add a built-in dashboard section for a task. The XSL used for transforming the task's
     * report(s) will be the XSL resource bundled with the Quill distribution..
     *
     * @param pTask The task to add a dashboard section for.
     *
     * @return  True if a built-in section was added, false if there is no built-in section for the
     *          task's type.
     */
    public boolean addBuiltInSection(Task pTask)
    {
        DashboardSection aSection = fSectionFactory.create(pTask);
        if (aSection != null)
        {
            fSections.put(aSection.getName(), aSection);
            return true;
        }
        else
            return false;
    }


    /**
     * Remove a dashboard section with a specific name.
     *
     * @param pName The name of the dashboard section.
     *
     * @return  The removed section, or null if there was no section with the specified name.
     */
    public DashboardSection removeSection(String pName)
    {
        return fSections.remove(pName);
    }


    /**
     * Create the dashboard report with all sections that have been added to the task.
     */
    @TaskAction
    public void createReport()
    {
        try
        {
            ReportBuilder aReportBuilder = new ReportBuilder(fReports.getHtml().getDestination());
            fLayout.write(aReportBuilder, fSections.values(), findChildProjectDashboards());
            aReportBuilder.close();

            if (fVerbose)
                getLogger().lifecycle("Created reports dashboard {}", aReportBuilder.getDestination().getAbsolutePath());
        }
        catch (IOException ioe)
        {
            getLogger().error("Could not create reports dashboard", ioe);
        }
    }


    /**
     * Initialize the task.
     */
    void init()
    {
        fSectionFactory = new DashboardSectionFactory(getProject());

        fLayout = new DashboardLayout(getProject());

        // Create the task's report container and add the container's report's destination as output
        // of this task.
        fReports = new DashboardReportsImpl(this);
        Tasks.outputFile(this, () -> this.getReports().getHtml().getDestination());
    }


    /**
     * Add all available default dashboard sections to the task if that hasn't been done.
     */
    private void maybeAddDefaultSections()
    {
        if (fHasAddedDefaultSections)
            return;

        for (DashboardSection aSection : fSectionFactory.createAvailableDefaultSections())
        {
            getLogger().debug("Adding default reports dashboard section {}", aSection.getName());
            fSections.put(aSection.getName(), aSection);
        }

        fHasAddedDefaultSections = true;
    }


    /**
     * Find all child projects that have a {@code DashboardTask} and return a mapping from the
     * project name to the path of the dashboard report.
     *
     * @return  The child projects with dashboard reports, or an empty map if the task's project has
     *          no child projects with a {@code DashboardTask}.
     */
    private Map<String, String> findChildProjectDashboards()
    {
        // Get the DashboardTask for all child projects of this task's project.
        Collection<DashboardTask> aChildTasks =
            getProject()
                .getChildProjects()
                .values()
                .stream()
                .map(p -> Projects.getTask(p, TASK_NAME, DashboardTask.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (aChildTasks.isEmpty())
            return Collections.emptyMap();

        // Create a map from child project name to dashboard report file path.
        Map<String, String> aChildProjectsDashboards = new HashMap<>();
        Path aBasePath = getReports().getHtml().getDestination().toPath().getParent();

        for (DashboardTask aChildTask : aChildTasks)
        {
            Path aChildReportPath = aChildTask.getReports().getHtml().getDestination().toPath();
            String aRelativePath = aBasePath.relativize(aChildReportPath).toString();
            aChildProjectsDashboards.put(aChildTask.getProject().getName(), aRelativePath);
        }

        return aChildProjectsDashboards;
    }


    /**
     * A snapshot of a {@code DashboardSection} that can be serialized and is suitable for a task's
     * input. Some {@code DashboardSection} instances are not serializable due to some
     * {@code Report} implementations not being serializable (e.g.
     * org.gradle.api.reporting.internal.SimpleReport, which is the base class of many standard
     * reports, has some PropertyState instance variables that aren't serializable).
     */
    static private class DashboardSectionSpec implements Serializable
    {
        static private final long serialVersionUID = 1L;

        private final String fName;
        private final File fInputReportFile;
        private final File fDetailedReportFile;
        private final File fXslFile;

        DashboardSectionSpec(DashboardSection pSection)
        {
            fName = pSection.getName();
            fInputReportFile = getReportFileSpec(pSection.getReport());
            fDetailedReportFile = getReportFileSpec(pSection.getDetailedReport());
            fXslFile = pSection.getXslFile();
        }

        @Override
        public boolean equals(Object pObject)
        {
            if (pObject == this)
                return true;
            if (!(pObject instanceof DashboardSectionSpec))
                return false;

            DashboardSectionSpec aOther = (DashboardSectionSpec) pObject;
            return
                fName.equals(aOther.fName) &&
                Objects.equals(fInputReportFile, aOther.fInputReportFile) &&
                Objects.equals(fDetailedReportFile, aOther.fDetailedReportFile) &&
                Objects.equals(fXslFile, aOther.fXslFile);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(fName, fInputReportFile, fDetailedReportFile, fXslFile);
        }

        static private File getReportFileSpec(Report pReport)
        {
            return pReport != null && pReport.isEnabled() ? pReport.getDestination() : null;
        }
    }
}
