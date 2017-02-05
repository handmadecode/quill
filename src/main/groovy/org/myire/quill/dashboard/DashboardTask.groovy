/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard

import org.gradle.api.Task
import org.gradle.api.internal.AbstractTask
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import org.myire.quill.common.Projects
import org.myire.quill.report.ReportBuilder

import java.nio.file.Path


/**
 * A task that creates a summary report of a collection of reports generated during a build. The
 * summary report consists of one {@code DashboardSection} for each report included in the summary.
 *<p>
 * By default the task generates a summary of the reports from the Test, Cobertura, Scent, JDepend,
 * FindBugs, Checkstyle, PMD, and CPD tasks that are available in the project. Build scripts can
 * access and modify these standard  sections through the {@code sections} property, which is a map
 * from task name to {@code DashboardSection} instance. New sections can also be added to this map.
 *<p>
 * The sections are logically output as a matrix where the number of columns can be configured.
 * The HTML code that starts and ends the entire matrix, each row and each cell can also be
 * configured.
 */
class DashboardTask extends AbstractTask implements Reporting<DashboardReports>
{
    static final String HTML_RESOURCE_REPORT_CSS = '/org/myire/quill/rsrc/report/report.css'
    static final String XSL_RESOURCE_CHILD_PROJECTS = '/org/myire/quill/rsrc/report/child_projects.xsl'


    private DashboardSectionFactory fSectionFactory;
    private boolean fHasAddedDefaultSections;
    private List<DashboardSectionSnapshot> fInputSections;

    // Properties accessed through getter and setter only.
    private DashboardReports fReports;
    private Map<String, DashboardSection> fSections = [:]
    private File fHeaderHtmlFile;
    private File fFooterHtmlFile;
    private Closure<String> fHeadlineHtmlCode = { this.defaultHeadlineHtmlCode() };
    private Closure<String> fCellStartHtmlCode = {  this.defaultCellStartHtmlCode() };


    /**
     * Should the path to the report file be logged after it has been created?
     */
    boolean verbose = true

    /**
     * The HTML title of the report. Default is &quot;&lt;project name> build reports summary&quot;
     * This string will be placed inside the {@code &lt;title>} tag in the HTML head.
     */
    @Input
    String htmlTitle

    /**
     * The HTML code to start the sections matrix with. Default is {@code &lt;table width="100%">}.
     */
    @Input
    String sectionsStartHtmlCode = '<table width="100%">';

    /**
     * The HTML code to end the sections matrix with. Default is {@code &lt;/table>}.
     */
    @Input
    String sectionsEndHtmlCode = '</table>';

    /**
     * The HTML code to start each row of sections with. Default is {@code &lt;tr>}.
     */
    @Input
    String rowStartHtmlCode = '<tr>';

    /**
     * The HTML code to end each row of sections with. Default is {@code &lt;/tr>}.
     */
    @Input
    String rowEndHtmlCode = '</tr>';

    /**
     * The HTML code to end each section cell with. Default is {@code &lt;/td>}.
     */
    @Input
    String cellEndHtmlCode = '</td>';

    /**
     * The number of columns to present the sections in. Default is 2.
     */
    @Input
    int columns = 2


    /**
     * Initialize the task.
     */
    void init()
    {
        // Create the task's report container and add the container's report's destination as output
        // of this task.
        fReports = new DashboardReportsImpl(this);
        outputs.file({ -> this.reports.getHtml().destination });

        // Create the factory for default dashboard sections.
        fSectionFactory = new DashboardSectionFactory(this);

        htmlTitle = project.name.capitalize() + ' build reports summary';
    }


    /**
     * Get the reports produced by this task.
     *
     * @return The reports.
     */
    @Override
    DashboardReports getReports()
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
    DashboardReports reports(Closure pClosure)
    {
        return fReports.configure(pClosure);
    }


    /**
     * Get the dashboard section snapshots that act as input for the task. The map property
     * {@code sections} is not suitable for input as the values are {@code DashboardSection}
     * instances which cannot be serialized (due to {@code Report} implementations not being
     * serializable).
     *
     * @return The dashboard section snapshots.
     */
    @Input
    List<DashboardSectionSnapshot> getInputSections()
    {
        if (fInputSections == null)
            fInputSections = getSections().values().collect { new DashboardSectionSnapshot(it) };

        return fInputSections;
    }


    /**
     * Get the map with the individual dashboard sections that will be included in the report.
     *
     * @return The report's sections.
     */
    Map<String, DashboardSection> getSections()
    {
        maybeAddDefaultSections();
        return fSections;
    }


    void setSections(Map<String, DashboardSection> pSections)
    {
        fSections = pSections ?: [:];
    }


    /**
     * Add or modify the dashboard section with a specific name.
     *
     * @param pName     The name of the dashboard section.
     * @param pReport   The XML report to transform.
     * @param pXslFile  The XSL file to transform the report with.
     */
    void addSection(String pName, Report pReport, Object pXslFile)
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
    void addSection(String pName, Report pReport, Report pDetailedReport, Object pXslFile)
    {
        fSections.put(pName, new DashboardSection(this, pName, pReport, pDetailedReport, project.file(pXslFile)));
    }


    /**
     * Add a built-in dashboard section for a task.
     *
     * @param pTask The task to add a dashboard section for.
     *
     * @return  True if a built-in section was added, false if there is no built-in section for the
     *          task's type.
     */
    boolean addBuiltInSection(Task pTask)
    {
        return fSectionFactory.addBuiltInSection(fSections, pTask);
    }


    /**
     * Get the HTML file that will be put before the sections in the report file. If this file is
     * not specified, a default header file will be used.
     *
     * @return The header HTML file to use, possibly null.
     */
    @InputFile
    @Optional
    File getHeaderHtmlFile()
    {
        return fHeaderHtmlFile;
    }


    void setHeaderHtmlFile(Object pFile)
    {
        fHeaderHtmlFile = pFile ? project.file(pFile) : null;
    }


    /**
     * Get the HTML file that will be put after the sections in the report file. If this file is not
     * specified, a default footer file will be used.
     *
     * @return The footer HTML file to use, possibly null.
     */
    @InputFile
    @Optional
    File getFooterHtmlFile()
    {
        return fFooterHtmlFile;
    }


    void setFooterHtmlFile(Object pFile)
    {
        fFooterHtmlFile = pFile ? project.file(pFile) : null;
    }


    /**
     * Get the HTML code for report headline to display before the matrix with all sections. Default
     * is {@code &lt;p class=&quot;headline&quot;>htmlTitle&lt;p>} where &quot;htmlTitle&quot; is
     * the property {@code htmlTitle}.
     *
     * @return  The headline HTML code.
     */
    @Input
    String getHeadlineHtmlCode()
    {
        return fHeadlineHtmlCode?.call();
    }


    void setHeadlineHtmlCode(String pCode)
    {
        fHeadlineHtmlCode = pCode ? { pCode } : null;
    }


    /**
     * Get the default headline HTML code.
     *
     * @return  The default headline HTML code.
     */
    String defaultHeadlineHtmlCode()
    {
        return '<p class="headline">' + htmlTitle + '</p>';
    }


    /**
     * Get the HTML code to end each section cell with. Default is
     * {@code &lt;td width="x%" valign="top">} where &quot;x%&quot; is the number of columns
     * specified in the property {@code columns} divided by 100%.
     *
     * @return  The cell start HTML code.
     */
    @Input
    String getCellStartHtmlCode()
    {
        return fCellStartHtmlCode?.call();
    }


    void setCellStartHtmlCode(String pCode)
    {
        fCellStartHtmlCode = pCode ? { pCode } : null;
    }


    /**
     * Get the default cell start HTML code.
     *
     * @return  The default cell start HTML code.
     */
    String defaultCellStartHtmlCode()
    {
        int aWidthPercent = 100 / columns;
        return '<td width="' + aWidthPercent + '%" valign="top">';
    }


    /**
     * Add the default sections before configuring the task.
     *
     * @param pClosure  The closure to configure the task with.
     *
     * @return  This task.
     */
    @Override
    Task configure(Closure pClosure)
    {
        maybeAddDefaultSections();
        return super.configure(pClosure);
    }


    /**
     * Create the dashboard report with all sections that have been added to the task.
     */
    @TaskAction
    void createReport()
    {
        // The report consists of the header, all sections, and the footer.
        ReportBuilder aReportBuilder = new ReportBuilder(fReports.getHtml().destination);
        writeHeader(aReportBuilder);
        writeHeadline(aReportBuilder);
        writeSectionsMatrix(aReportBuilder);
        writeChildProjectsLinks(aReportBuilder);
        writeFooter(aReportBuilder);
        aReportBuilder.close();

        if (verbose)
            logger.lifecycle('Created reports dashboard {}', aReportBuilder.getDestination().absolutePath);
    }


    /**
     * Add all default dashboard sections to the task if that hasn't been done.
     */
    private void maybeAddDefaultSections()
    {
        if (!fHasAddedDefaultSections)
        {
            fSections += fSectionFactory.createAvailableDefaultSections();
            fHasAddedDefaultSections = true;
        }
    }


    /**
     * Copy the report header file to a report builder. If no file has been specified the default
     * header will be written to the report builder.
     *
     * @param pBuilder The report builder to write the header to.
     */
    private void writeHeader(ReportBuilder pBuilder)
    {
        if (fHeaderHtmlFile == null)
        {
            // No header HTML file specified, use the default.
            pBuilder.write('<html><head>');
            if (htmlTitle != null)
            {
                pBuilder.write('<title>');
                pBuilder.write(htmlTitle);
                pBuilder.write('</title>');
            }

            pBuilder.write('<style type="text/css">');
            pBuilder.copy(HTML_RESOURCE_REPORT_CSS);
            pBuilder.write('</style></head><body>');
        }
        else
            pBuilder.copy(fHeaderHtmlFile);
    }


    /**
     * Copy the report footer file to a report builder. If no file has been specified the default
     * footer will be written.
     *
     * @param pBuilder The report builder to write the footer to.
     */
    private void writeFooter(ReportBuilder pBuilder)
    {
        if (fFooterHtmlFile != null)
            pBuilder.copy(fFooterHtmlFile);
        else
            pBuilder.write('</body></html>');
    }


    /**
     * Write the {@code headlineHtmlCode} property's value to a report builder.
     *
     * @param pBuilder The report builder to write the headline to.
     */
    private void writeHeadline(ReportBuilder pBuilder)
    {
        String aHeadline = headlineHtmlCode;
        if (aHeadline != null)
            pBuilder.write(aHeadline);
    }


    /**
     * Write the matrix with all sections to a report builder.
     *
     * @param pBuilder The report builder to write the sections to.
     */
    private void writeSectionsMatrix(ReportBuilder pBuilder)
    {
        // Get the HTML code configured to start each cell.
        String aCellStartHtmlCode = fCellStartHtmlCode ? fCellStartHtmlCode.call() : '';

        // Start the sections matrix with the configured HTML code.
        pBuilder.write(sectionsStartHtmlCode);

        // Write the sections in the specified number of columns.
        Collection<DashboardSection> aSections = getSections().values();
        int aLastSection = aSections.size() - 1;
        aSections.eachWithIndex
        {
            aSection, aIndex ->

                // Write start of row if the current section has column position 0.
                int aColumnPos = aIndex % columns;
                if (aColumnPos == 0)
                    pBuilder.write(rowStartHtmlCode);

                // Write the section.
                pBuilder.write(aCellStartHtmlCode);
                aSection.writeTo(pBuilder);
                pBuilder.write(cellEndHtmlCode);

                // Write end of row if the current section has column position last in row or is
                // the last section no matter its position.
                if (aColumnPos == (columns-1) || aIndex == aLastSection)
                    pBuilder.write(rowEndHtmlCode);
        }

        // End the sections sequence with the configured HTML code.
        pBuilder.write(sectionsEndHtmlCode);
    }


    /**
     * Write a section with links to the dashboard reports of any child projects.
     *
     * @param pBuilder The report builder to write the child project report links to.
     */
    private void writeChildProjectsLinks(ReportBuilder pBuilder)
    {
        Node aXml = createChildProjectsXml();
        if (aXml != null)
        {
            // Create a temporary file with the XML representation of the child project reports.
            File aXmlFile = File.createTempFile('quill-child-dashboard', '.xml');
            aXmlFile.withPrintWriter
            {
                new XmlNodePrinter(it).print(aXml);
            }

            // Transform the temporary XML file with the built-in resource and then delete the file.
            pBuilder.transform(aXmlFile, XSL_RESOURCE_CHILD_PROJECTS);
            aXmlFile.delete();
        }
    }


    /**
     * Create an XML representation of the child projects that have a {@code DashboardTask}. The
     * XML representation will be on the form
     *<pre>
     *  <child-projects>
     *    <child-project name="..." report="..."/>
     *    ...
     *  </child-projects>
     *</pre>
     *
     * where the attribute {@code name} contains the name of the child project and the attribute
     * {@code report} contains the path to the dashboard report of the child project. The path is
     * relative to this task's report destination.
     *
     * @return  The child projects XML representation, or null if the task's project has no child
     *          projects with a {@code DashboardTask}.
     */
    private Node createChildProjectsXml()
    {
        // Get the DashboardTask for all child projects of this task's project.
        Collection<DashboardTask> aChildTasks = project.childProjects.values().collect()
        {
            Projects.getTask(it, DashboardPlugin.DASHBOARD_TASK_NAME, DashboardTask.class)
        }
        .findAll
        {
            // Filter out all projects that returned a null task.
            it != null
        }

        if (aChildTasks.isEmpty())
            return null;

        Path aBasePath = getReports().getHtml().getDestination().toPath().getParent();
        Node aRootNode = new Node(null, 'child-projects');

        // Create a node for each child DashboardTask's report.
        aChildTasks.each {
            Path aChildReportPath = it.getReports().getHtml().getDestination().toPath();
            String aRelativePath = aBasePath.relativize(aChildReportPath).toString();
            aRootNode.append(new Node(null, 'child-project', [name: it.project.name, report: aRelativePath]));
        }

        return aRootNode;
    }


    /**
     * A snapshot of a {@code DashboardSection} that can be serialized and is suitable for a task's
     * input. {@code DashboardSection} instances are not serializable due to some {@code Report}
     * implementations not being serializable.
     */
    static private class DashboardSectionSnapshot implements Serializable
    {
        static private final long serialVersionUID = 1L;

        private final String fName;
        private final FileSnapshot fInputReportFile;
        private final FileSnapshot fDetailedReportFile;
        private final FileSnapshot fXslFile;

        DashboardSectionSnapshot(DashboardSection pSection)
        {
            fName = pSection.name;
            fInputReportFile = createReportFileSnapshot(pSection.report);
            fDetailedReportFile = createReportFileSnapshot(pSection.detailedReport);
            fXslFile = pSection.xslFile ? new FileSnapshot(pSection.xslFile ) : null;
        }

        @Override
        public boolean equals(Object pObject)
        {
            if (pObject.is(this))
                return true;
            if (!(pObject instanceof DashboardSectionSnapshot))
                return false;

            DashboardSectionSnapshot aOther = (DashboardSectionSnapshot) pObject;
            return fName == aOther.fName &&
                   fInputReportFile == aOther.fInputReportFile &&
                   fDetailedReportFile == aOther.fDetailedReportFile &&
                   fXslFile == aOther.fXslFile;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(fName, fInputReportFile, fDetailedReportFile, fXslFile);
        }

        static private FileSnapshot createReportFileSnapshot(Report pReport)
        {
            File aFile = pReport != null && pReport.enabled ? pReport.destination : null;
            return aFile ? new FileSnapshot(aFile) : null;
        }
    }


    /**
     * A snapshot of a {@code File} that is suitable for a task's input.
     */
    static private class FileSnapshot implements Serializable
    {
        static private final long serialVersionUID = 1L;

        private final String fAbsolutePath;
        private final boolean fExists;
        private final long fLastModified;

        FileSnapshot(File pFile)
        {
            fAbsolutePath = pFile.absolutePath;
            fExists = pFile.exists();
            fLastModified = pFile.lastModified();
        }

        @Override
        public boolean equals(Object pObject)
        {
            if (pObject.is(this))
                return true;
            if (!(pObject instanceof FileSnapshot))
                return false;

            FileSnapshot aOther = (FileSnapshot) pObject;
            return fAbsolutePath == aOther.fAbsolutePath &&
                   fExists == aOther.fExists &&
                   fLastModified == aOther.fLastModified;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(fLastModified, fExists, fLastModified);
        }
    }
}
