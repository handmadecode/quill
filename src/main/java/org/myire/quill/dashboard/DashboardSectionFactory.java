/*
 * Copyright 2015, 2019-2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.Pmd;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.tasks.JacocoReport;

import org.myire.quill.common.ProjectAware;
import org.myire.quill.cpd.CpdTask;
import org.myire.quill.jol.JolTask;
import org.myire.quill.report.FormatChoiceReport;
import org.myire.quill.report.ReportSet;
import org.myire.quill.report.ReportingEntity;
import org.myire.quill.scent.ScentTask;


/**
 * Factory for creating {@code DashboardSection} instances from {@code Task} instances.
 */
class DashboardSectionFactory extends ProjectAware
{
    // XSL resources with default style sheets for the standard dashboard sections.
    static private final String XSL_RESOURCE_CHECKSTYLE = "/org/myire/quill/rsrc/report/checkstyle/checkstyle_summary.xsl";
    static private final String XSL_RESOURCE_COBERTURA = "/org/myire/quill/rsrc/report/cobertura/cobertura_summary.xsl";
    static private final String XSL_RESOURCE_CPD = "/org/myire/quill/rsrc/report/cpd/cpd_summary.xsl";
    static private final String XSL_RESOURCE_JACOCO = "/org/myire/quill/rsrc/report/jacoco/jacoco_summary.xsl";
    static private final String XSL_RESOURCE_JDEPEND = "/org/myire/quill/rsrc/report/jdepend/jdepend_summary.xsl";
    static private final String XSL_RESOURCE_JOL = "/org/myire/quill/rsrc/report/jol/jol_summary.xsl";
    static private final String XSL_RESOURCE_JUNIT = "/org/myire/quill/rsrc/report/junit/junit_summary.xsl";
    static private final String XSL_RESOURCE_PMD = "/org/myire/quill/rsrc/report/pmd/pmd_summary.xsl";
    static private final String XSL_RESOURCE_SCENT = "/org/myire/quill/rsrc/report/scent/scent_summary.xsl";
    static private final String XSL_RESOURCE_SPOTBUGS= "/org/myire/quill/rsrc/report/spotbugs/spotbugs_summary.xsl";

    // Reports added to the convention of Gradle built-in tasks
    static private final String ENHANCED_CHECK_TASK_REPORT_NAME = "quillHtmlReport";
    static private final String JUNIT_SUMMARY_REPORT_NAME = "junitSummaryReport";

    // The SpotBugs classes can't be referenced explicitly since the SpotBugs plugin may not be
    // available.
    static private final Class<? extends Task> cSpotBugsTaskClass =
        getTaskClass("com.github.spotbugs.SpotBugsTask");

    // The JDepend classes can't be referenced explicitly since the JDepend plugin may not be
    // available (it was removed in Gradle 6.0).
    static private final Class<? extends Task> cJDependTaskClass =
        getTaskClass("org.gradle.api.plugins.quality.JDepend");

    // The Cobertura classes can't be referenced explicitly since the're still in the Groovy source
    // tree.
    static private final Class<? extends Task> cCoberturaReportsTaskClass =
        getTaskClass("org.myire.quill.cobertura.CoberturaReportsTask");


    /**
     * Create a new {@code DashboardSectionFactory}.
     *
     * @param pProject  The project to create dashboard sections for.
     */
    DashboardSectionFactory(Project pProject)
    {
        super(pProject);
    }


    /**
     * Create a dashboard section for a task if its type is known to the factory.
     *
     * @param pTask The task to create ta dashboard section for.
     *
     * @return  A new {@code DashboardSection}, or null if the factory doesn't support the task's
     *          type.
     */
    DashboardSection create(Task pTask)
    {
        if (pTask instanceof Checkstyle)
            return createCheckstyleSection((Checkstyle) pTask);
        else if (pTask instanceof CpdTask)
            return createCpdSection((CpdTask) pTask);
        else if (pTask instanceof JacocoReport)
            return createJacocoSection((JacocoReport) pTask);
        else if (pTask instanceof Test)
            return createJUnitSection((Test) pTask);
        else if (pTask instanceof Pmd)
            return createPmdSection((Pmd) pTask);
        else if (pTask instanceof ScentTask)
            return createScentSection((ScentTask) pTask);
        else if (pTask instanceof JolTask)
            return createJolSection((JolTask) pTask);
        else if (cSpotBugsTaskClass != null && cSpotBugsTaskClass.isAssignableFrom(pTask.getClass()))
            return createSpotBugsSection(pTask);
        else if (cJDependTaskClass != null && cJDependTaskClass.isAssignableFrom(pTask.getClass()))
            return createJDependSection(pTask);
        else if (cCoberturaReportsTaskClass != null && cCoberturaReportsTaskClass.isAssignableFrom(pTask.getClass()))
            return createCoberturaSection(pTask);
        else
            return null;
    }


    Collection<DashboardSection> createAvailableDefaultSections()
    {
        Collection<DashboardSection> aSections = new ArrayList<>();

        addSectionsForTaskType(aSections, Test.class, this::createJUnitSection);
        addSectionsForTaskType(aSections, JacocoReport.class, this::createJacocoSection);
        if (cCoberturaReportsTaskClass != null)
            addSectionsForTaskType(aSections, cCoberturaReportsTaskClass, this::createCoberturaSection);
        if (cSpotBugsTaskClass != null)
            addSectionsForTaskType(aSections, cSpotBugsTaskClass, this::createSpotBugsMainSection);
        addSectionsForTaskType(aSections, Checkstyle.class, this::createCheckstyleMainSection);
        addSectionsForTaskType(aSections, Pmd.class, this::createPmdMainSection);
        addSectionsForTaskType(aSections, CpdTask.class, this::createCpdSection);
        addSectionsForTaskType(aSections, ScentTask.class, this::createScentSection);
        addSectionsForTaskType(aSections, JolTask.class, this::createJolSection);
        if (cJDependTaskClass != null)
            addSectionsForTaskType(aSections, cJDependTaskClass, this::createJDependMainSection);

        return aSections;
    }


    private <T extends Task> void addSectionsForTaskType(
        Collection<DashboardSection> pSections,
        Class<T> pTaskType,
        Function<T, DashboardSection> pCreator)
    {
        getProject()
            .getTasks()
            .withType(pTaskType)
            .stream()
            .map(pCreator)
            .filter(Objects::nonNull)
            .forEach(pSections::add);
    }


    /**
     * Create a dashboard section for the XML report of the &quot;checkstyleMain&quot; task.
     *
     * @param pTask The Checkstyle task.
     *
     * @return  A new {@code DashboardSection}, or null if the task isn't the
     *          &quot;checkstyleMain&quot; task.
     */
    private DashboardSection createCheckstyleMainSection(Checkstyle pTask)
    {
        if ("checkstyleMain".equals(pTask.getName()))
            return createCheckstyleSection(pTask);
        else
            return null;
    }


    /**
     * Create a dashboard section for the XML report of a Checkstyle task.
     *
     * @param pTask The Checkstyle task.
     *
     * @return  A new {@code DashboardSection}.
     */
    private DashboardSection createCheckstyleSection(Checkstyle pTask)
    {
        Report aDetailedReport = null;

        Convention aConvention = pTask.getConvention();
        if (aConvention != null)
            aDetailedReport = (Report) aConvention.findByName(ENHANCED_CHECK_TASK_REPORT_NAME);

        if (aDetailedReport == null)
            aDetailedReport = pTask.getReports().getHtml();

        return new DashboardSection(
            pTask.getProject(),
            pTask.getName(),
            pTask.getReports().getXml(),
            aDetailedReport,
            XSL_RESOURCE_CHECKSTYLE);
    }


    /**
     * Create a dashboard section for the XML report of a CPD task.
     *
     * @param pTask The CPD task.
     *
     * @return  A new {@code DashboardSection}, or null if the task's primary report is not on the
     *          XML format.
     */
    private DashboardSection createCpdSection(CpdTask pTask)
    {
        FormatChoiceReport aPrimaryReport = pTask.getReports().getPrimary();
        if ("xml".equals(aPrimaryReport.getFormat()))
        {
            return new DashboardSection(
                pTask.getProject(),
                pTask.getName(),
                aPrimaryReport,
                pTask.getReports().getHtml(),
                XSL_RESOURCE_CPD);
        }
        else
        {
            pTask.getLogger().debug(
                "Task '{}' creates a '{}' report, cannot create a dashboard section for it",
                pTask.getName(),
                aPrimaryReport.getFormat());
            return null;
        }
    }


    /**
     * Create a dashboard section for the XML report of a Jacoco report task.
     *
     * @param pTask The Jacoco report task.
     *
     * @return  A new {@code DashboardSection}.
     */
    private DashboardSection createJacocoSection(JacocoReport pTask)
    {
        return new DashboardSection(
            pTask.getProject(),
            pTask.getName(),
            pTask.getReports().getXml(),
            pTask.getReports().getHtml(),
            XSL_RESOURCE_JACOCO);
    }


    /**
     * Create a dashboard section for the XML report of the &quot;jdependMain&quot; task.
     *
     * @param pTask The JDepend task.
     *
     * @return  A new {@code DashboardSection}, or null if the task isn't the
     *          &quot;jdependMain&quot; task.
     */
    private DashboardSection createJDependMainSection(Task pTask)
    {
        if ("jdependMain".equals(pTask.getName()))
            return createJDependSection(pTask);
        else
            return null;
    }


    /**
     * Create a dashboard section for the XML report of a JDepend task.
     *
     * @param pTask The JDepend task.
     *
     * @return  A new {@code DashboardSection}.
     */
    private DashboardSection createJDependSection(Task pTask)
    {
        // Can't refer to the JDepend plugin types since they plugin may not be available (it was
        // removed in Gradle 6.0).
        return createReportingTaskSection(pTask, XSL_RESOURCE_JDEPEND);
    }


    /**
     * Create a dashboard section for the XML report of a Jol task.
     *
     * @param pTask The Jol task.
     *
     * @return  A new {@code DashboardSection}.
     */
    private DashboardSection createJolSection(JolTask pTask)
    {
        return new DashboardSection(
            pTask.getProject(),
            pTask.getName(),
            pTask.getReports().getXml(),
            pTask.getReports().getHtml(),
            XSL_RESOURCE_JOL);
    }


    /**
     * Create a dashboard section for the JUnit summary report of a test task.
     *
     * @param pTask The test task.
     *
     * @return  A new {@code DashboardSection}, or null if the task has no JUnit summary report in
     *          its convention.
     */
    private DashboardSection createJUnitSection(Test pTask)
    {
        Report aSummaryReport = null;

        Convention aConvention = pTask.getConvention();
        if (aConvention != null)
            aSummaryReport = (Report) aConvention.findByName(JUNIT_SUMMARY_REPORT_NAME);

        if (aSummaryReport != null)
        {
            return new DashboardSection(
                pTask.getProject(),
                pTask.getName(),
                aSummaryReport,
                pTask.getReports().getHtml(),
                XSL_RESOURCE_JUNIT);
        }
        else
        {
            getProjectLogger().debug(
                "Task '{}' has no '{}' report, cannot create dashboard section for it",
                pTask.getName(),
                JUNIT_SUMMARY_REPORT_NAME);
            return null;
        }
    }


    /**
     * Create a dashboard section for the XML report of the &quot;pmdMain&quot; task.
     *
     * @param pTask The PMD task.
     *
     * @return  A new {@code DashboardSection}, or null if the task isn't the &quot;pmdMain&quot;
     *          task.
     */
    private DashboardSection createPmdMainSection(Pmd pTask)
    {
        if ("pmdMain".equals(pTask.getName()))
            return createPmdSection(pTask);
        else
            return null;
    }


    /**
     * Create a dashboard section for the XML report of a PMD task.
     *
     * @param pTask The PMD task.
     *
     * @return  A new {@code DashboardSection}.
     */
    private DashboardSection createPmdSection(Pmd pTask)
    {
        Report aDetailedReport = null;

        Convention aConvention = pTask.getConvention();
        if (aConvention != null)
            aDetailedReport = (Report) aConvention.findByName(ENHANCED_CHECK_TASK_REPORT_NAME);

        if (aDetailedReport == null)
            aDetailedReport = pTask.getReports().getHtml();

        return new DashboardSection(
            pTask.getProject(),
            pTask.getName(),
            pTask.getReports().getXml(),
            aDetailedReport,
            XSL_RESOURCE_PMD);
    }


    /**
     * Create a dashboard section for the XML report of a Scent task.
     *
     * @param pTask The Scent task.
     *
     * @return  A new {@code DashboardSection}.
     */
    private DashboardSection createScentSection(ScentTask pTask)
    {
        return new DashboardSection(
            pTask.getProject(),
            pTask.getName(),
            pTask.getReports().getXml(),
            pTask.getReports().getHtml(),
            XSL_RESOURCE_SCENT);
    }


    /**
     * Create a dashboard section for the XML report of the &quot;spotbugsMain&quot; task.
     *
     * @param pTask The SpotBugs task.
     *
     * @return  A new {@code DashboardSection}, or null if the task isn't the
     *          &quot;spotbugsMain&quot; task.
     */
    private DashboardSection createSpotBugsMainSection(Task pTask)
    {
        if ("spotbugsMain".equals(pTask.getName()))
            return createSpotBugsSection(pTask);
        else
            return null;
    }


    /**
     * Create a dashboard section for the XML report of a SpotBugs task.
     *
     * @param pTask The SpotBugs task.
     *
     * @return  A new {@code DashboardSection}.
     */
    private DashboardSection createSpotBugsSection(Task pTask)
    {
        // Can't refer to the SpotBugs plugin types since they plugin may not be available.
        return createReportingTaskSection(pTask, XSL_RESOURCE_SPOTBUGS);
    }


    /**
     * Create a dashboard section for the XML report of a Cobertura reports task.
     *
     * @param pTask The Cobertura reports task.
     *
     * @return  A new {@code DashboardSection}.
     */
    private DashboardSection createCoberturaSection(Task pTask)
    {
        // Can't access the types in the cobertura package since they're in the Groovy source tree.
        if (pTask instanceof ReportingEntity<?>)
        {
            ReportSet aReports = ((ReportingEntity<?>) pTask).getReports();
            Report aXmlReport = aReports.getReportByName("coberturaXml");
            if (aXmlReport != null)
            {
                return new DashboardSection(
                    pTask.getProject(),
                    pTask.getName(),
                    aXmlReport,
                    aReports.getReportByName("coberturaHtml"),
                    XSL_RESOURCE_COBERTURA);
            }
        }

        return null;
    }


    /**
     * Create a dashboard section for the XML and HTML reports, if available, of a {@code Reporting}
     * task.
     *
     * @param pTask         The task.
     * @param pXslResource  The dashboard section's default XSL resource.
     *
     * @return  A new {@code DashboardSection}, or null if {@code pTask} does not implement the
     *          {@code Reporting} interface or does not have an XML report.
     */
    static private DashboardSection createReportingTaskSection(Task pTask, String pXslResource)
    {
        ReportContainer<?> aReports = null;
        if (pTask instanceof Reporting<?>)
            aReports = ((Reporting<?>) pTask).getReports();

        if (aReports == null)
            return null;

        Report aXmlReport =  aReports.getByName("xml");
        if (aXmlReport == null)
            return null;

        Report aDetailedReport = null;

        Convention aConvention = pTask.getConvention();
        if (aConvention != null)
            aDetailedReport = (Report) aConvention.findByName(ENHANCED_CHECK_TASK_REPORT_NAME);

        if (aDetailedReport == null)
            aDetailedReport = aReports.getByName("html");

        return new DashboardSection(
            pTask.getProject(),
            pTask.getName(),
            aXmlReport,
            aDetailedReport,
            pXslResource);
    }


    /**
     * Get a task class by its fully qualified name.
     *
     * @param pClassName    The name of the class.
     *
     * @return  The class, or null if not found.
     */
    @SuppressWarnings("unchecked")
    static private Class<? extends Task> getTaskClass(String pClassName)
    {
        try
        {
            Class<?> aClass = Class.forName(pClassName);
            if (Task.class.isAssignableFrom(aClass))
                return (Class<? extends Task>) aClass;
            else
                // Shouldn't happen unless the real class has been replaced on the classpath...
                return null;
        }
        catch (ClassNotFoundException ignore)
        {
            return null;
        }
    }
}
