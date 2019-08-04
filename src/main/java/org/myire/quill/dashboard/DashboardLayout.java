/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

import org.myire.quill.report.ReportBuilder;


/**
 * The layout of the dashboard sections matrix. The sections are logically output as a matrix where
 * the number of columns can be configured. The HTML code that starts and ends the entire matrix,
 * each row and each cell can also be configured.
 *<p>
 * All HTML code properties can be specified as either strings or closures returning strings.
 */
public class DashboardLayout
{
    static private final String HTML_RESOURCE_REPORT_CSS = "/org/myire/quill/rsrc/report/report.css";
    static private final String XSL_RESOURCE_CHILD_PROJECTS = "/org/myire/quill/rsrc/report/child_projects.xsl";

    static private final Supplier<String> EMPTY_STRING_SUPPLIER = () -> "";

    private final Project fProject;
    private final String fDefaultTitle;

    private int fNumColumns = 2;

    private Supplier<String> fHeadlineHtmlCode = this::defaultHeadlineHtmlCode;
    private Supplier<String> fSectionsStartHtmlCode = () -> "<table width=\"100%\">";
    private Supplier<String> fSectionsEndHtmlCode = () -> "</table>";
    private Supplier<String> fRowStartHtmlCode = () -> "<tr>";
    private Supplier<String> fRowEndHtmlCode = () -> "</tr>";
    private Supplier<String> fCellStartHtmlCode = this::defaultCellStartHtmlCode;
    private Supplier<String> fCellEndHtmlCode = () -> "</td>";

    private File fHeaderHtmlFile;
    private File fFooterHtmlFile;


    /**
     * Create a new {@code DashboardLayout}.
     *
     * @param pProject  The project that the dashboard contains report summaries for.
     *
     * @throws NullPointerException if {@code pProject} is null.
     */
    DashboardLayout(Project pProject)
    {
        fProject = pProject;
        fDefaultTitle = pProject.getName() + " build reports summary";
    }


    /**
     * Get the HTML file that will be put before the sections in the report file. If this file is
     * not specified, a default header will be used.
     *
     * @return The header HTML file to use, possibly null.
     */
    @InputFile
    @Optional
    public File getHeaderHtmlFile()
    {
        return fHeaderHtmlFile;
    }


    public void setHeaderHtmlFile(Object pFile)
    {
        fHeaderHtmlFile = pFile != null ? fProject.file(pFile) : null;
    }


    /**
     * Get the HTML file that will be put after the sections in the report file. If this file is not
     * specified, a default footer will be used.
     *
     * @return The footer HTML file to use, possibly null.
     */
    @InputFile
    @Optional
    public File getFooterHtmlFile()
    {
        return fFooterHtmlFile;
    }


    public void setFooterHtmlFile(Object pFile)
    {
        fFooterHtmlFile = pFile != null ? fProject.file(pFile) : null;
    }


    /**
     * Get the HTML code for the headline to display before the matrix with all sections.
     *
     * @return  The headline HTML code.
     */
    @Input
    public String getHeadlineHtmlCode()
    {
        return fHeadlineHtmlCode.get();
    }


    public void setHeadlineHtmlCode(Object pCode)
    {
        fHeadlineHtmlCode = asStringSupplier(pCode);
    }


    /**
     * Get the HTML code to start the sections matrix with.
     *
     * @return  The sections matrix start HTML code.
     */
    @Input
    public String getSectionsStartHtmlCode()
    {
        return fSectionsStartHtmlCode.get();
    }


    public void setSectionsStartHtmlCode(Object pCode)
    {
        fSectionsStartHtmlCode = asStringSupplier(pCode);
    }


    /**
     * Get the HTML code to end the sections matrix with.
     *
     * @return  The sections matrix end HTML code.
     */
    @Input
    public String getSectionsEndHtmlCode()
    {
        return fSectionsEndHtmlCode.get();
    }


    public void setSectionsEndHtmlCode(Object pCode)
    {
        fSectionsEndHtmlCode = asStringSupplier(pCode);
    }


    /**
     * Get the HTML code to start each row of sections with.
     *
     * @return  The section row start HTML code.
     */
    @Input
    public String getRowStartHtmlCode()
    {
        return fRowStartHtmlCode.get();
    }


    public void setRowStartHtmlCode(Object pCode)
    {
        fRowStartHtmlCode = asStringSupplier(pCode);
    }


    /**
     * Get the HTML code to end each row of sections with.
     *
     * @return  The section row end HTML code.
     */
    @Input
    public String getRowEndHtmlCode()
    {
        return fRowEndHtmlCode.get();
    }


    public void setRowEndHtmlCode(Object pCode)
    {
        fRowEndHtmlCode = asStringSupplier(pCode);
    }


    /**
     * Get the HTML code to start each section cell in the matrix with.
     *
     * @return  The section cell start HTML code.
     */
    @Input
    public String getCellStartHtmlCode()
    {
        return fCellStartHtmlCode.get();
    }


    public void setCellStartHtmlCode(Object pCode)
    {
        fCellStartHtmlCode = asStringSupplier(pCode);
    }


    /**
     * Get the HTML code to end each section cell in the matrix with.
     *
     * @return  The section cell end HTML code.
     */
    @Input
    public String getCellEndHtmlCode()
    {
        return fCellEndHtmlCode.get();
    }


    public void setCellEndHtmlCode(Object pCode)
    {
        fCellEndHtmlCode = asStringSupplier(pCode);
    }


    /**
     * Get the number of columns to present the sections in. Default is 2.
     *
     * @return  The number of columns.
     */
    @Input
    public int getNumColumns()
    {
        return fNumColumns;
    }


    public void setNumColumns(int pNumColumns)
    {
        fNumColumns = pNumColumns;
    }


    /**
     * Write the dashboard layout to a {@code pReportBuilder}.
     *
     * @param pReportBuilder            The report builder.
     * @param pSections                 The sections to write.
     * @param pChildProjectDashboards   A map from child project name to child project dashboard
     *                                  report path.
     */
    void write(
        ReportBuilder pReportBuilder,
        Collection<DashboardSection> pSections,
        Map<String, String> pChildProjectDashboards)
    {
        if (fHeaderHtmlFile != null)
            pReportBuilder.copy(fHeaderHtmlFile);
        else
            writeDefaultHeader(pReportBuilder);

        // The dashboard consists of the headline, all sections, and links to any child projects.
        writeHeadline(pReportBuilder);
        writeSectionsMatrix(pReportBuilder, pSections);
        writeChildProjectsLinks(pReportBuilder, pChildProjectDashboards);

        if (fFooterHtmlFile != null)
            pReportBuilder.copy(fFooterHtmlFile);
        else
            writeDefaultFooter(pReportBuilder);
    }


    /**
     * Write the default header to a report builder.
     *
     * @param pBuilder The report builder to write the header to.
     */
    private void writeDefaultHeader(ReportBuilder pBuilder)
    {
        pBuilder.write("<html><head>");
        pBuilder.write("<title>");
        pBuilder.write(fDefaultTitle);
        pBuilder.write("</title>");
        pBuilder.write("<style type=\"text/css\">");
        pBuilder.copy(HTML_RESOURCE_REPORT_CSS);
        pBuilder.write("</style></head><body>");
    }


    /**
     * Write the default footer to a report builder.
     *
     * @param pBuilder The report builder to write the footer to.
     */
    private void writeDefaultFooter(ReportBuilder pBuilder)
    {
        pBuilder.write("</body></html>");
    }


    /**
     * Write the report headline to a report builder.
     *
     * @param pBuilder The report builder to write the headline to.
     */
    private void writeHeadline(ReportBuilder pBuilder)
    {
        pBuilder.write(fHeadlineHtmlCode.get());
    }


    /**
     * Write the matrix with all sections to a report builder.
     *
     * @param pBuilder  The report builder to write the sections to.
     * @param pSections The sections to write.
     */
    private void writeSectionsMatrix(ReportBuilder pBuilder, Collection<DashboardSection> pSections)
    {
        // Get the HTML code configured to start and end the section rows and cells.
        String aRowStartHtmlCode = fRowStartHtmlCode.get();
        String aRowEndHtmlCode = fRowEndHtmlCode.get();
        String aCellStartHtmlCode = fCellStartHtmlCode.get();
        String aCellEndHtmlCode = fCellEndHtmlCode.get();

        // Start the sections matrix with the configured HTML code.
        pBuilder.write(fSectionsStartHtmlCode.get());

        // Write the sections in the specified number of columns.
        int aIndex = 0, aLastSection = pSections.size() - 1;
        for (DashboardSection aSection : pSections)
        {
            // Write start of row if the current section has column position 0.
            int aColumnPos = aIndex % fNumColumns;
            if (aColumnPos == 0)
                pBuilder.write(aRowStartHtmlCode);

            // Write the section.
            pBuilder.write(aCellStartHtmlCode);
            aSection.writeTo(pBuilder);
            pBuilder.write(aCellEndHtmlCode);

            // Write end of row if the current section has column position last in row or is
            // the last section no matter its position.
            if (aColumnPos == (fNumColumns-1) || aIndex == aLastSection)
                pBuilder.write(aRowEndHtmlCode);

            aIndex++;
        }

        // End the sections sequence with the configured HTML code.
        pBuilder.write(fSectionsEndHtmlCode.get());
    }


    /**
     * Write a section with links to the dashboard reports of any child projects.
     *
     * @param pBuilder                  The report builder to write the child project report links
     *                                  to.
     * @param pChildProjectDashboards   A map from child project name to child project dashboard
     *                                  report path.
     */
    private void writeChildProjectsLinks(ReportBuilder pBuilder, Map<String, String> pChildProjectDashboards)
    {
        String aXml = createChildProjectsXml(pChildProjectDashboards);
        if (aXml != null)
            pBuilder.transform(aXml, XSL_RESOURCE_CHILD_PROJECTS, null);
    }


    /**
     * Create an XML representation of the child projects that have a dashboard report. The XML
     * representation will be on the form
     *<pre>
     *  <child-projects>
     *    <child-project name="..." report="..."/>
     *    ...
     *  </child-projects>
     *</pre>
     *
     * where the attribute {@code name} contains the name of the child project and the attribute
     * {@code report} contains the path to the dashboard report of the child project.
     *
     * @param pChildProjectDashboards   A map from child project name to child project dashboard
     *                                  report path.
     *
     * @return  The child projects XML representation, or null if the map is empty.
     */
    private String createChildProjectsXml(Map<String, String> pChildProjectDashboards)
    {
        if (pChildProjectDashboards.isEmpty())
            return null;

        StringBuilder aBuffer = new StringBuilder();
        aBuffer.append("<child-projects>");
        for (Map.Entry<String, String> aEntry : pChildProjectDashboards.entrySet())
        {
            aBuffer
                .append("<child-project name=\"")
                .append(aEntry.getKey())
                .append("\" report=\"")
                .append(aEntry.getValue())
                .append("\"/>");
        }

        aBuffer.append("</child-projects>");
        return aBuffer.toString();
    }


    /**
     * Get the default headline HTML code.
     *
     * @return  The default headline HTML code.
     */
    private String defaultHeadlineHtmlCode()
    {
        return "<p class=\"headline\">" + fDefaultTitle + "</p>";
    }


    /**
     * Get the default section cell start HTML code.
     *
     * @return  The default section cell start HTML code.
     */
    private String defaultCellStartHtmlCode()
    {
        int aWidthPercent = 100 / fNumColumns;
        return "<td width=\"" + aWidthPercent + "%\" valign=\"top\">";
    }


    static private Supplier<String> asStringSupplier(Object pObject)
    {
        if (pObject instanceof Supplier<?>)
            return () -> ((Supplier<?>) pObject).get().toString();
        else if (pObject instanceof Closure<?>)
            return () -> ((Closure<?>) pObject).call().toString();
        else if (pObject != null)
            return pObject::toString;
        else
            return EMPTY_STRING_SUPPLIER;
    }
}
