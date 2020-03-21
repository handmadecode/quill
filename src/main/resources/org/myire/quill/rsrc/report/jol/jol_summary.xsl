<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a Jol XML report into part of an HTML page.
 *
 * 2020-03-15 /PF    Created.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- Parameter containing the path to the detailed report -->
    <xsl:param name="detailed-report-path"/>


    <!-- Main template for the document root -->
    <xsl:template match="/">
        <xsl:apply-templates select="jol-report"/>
    </xsl:template>


    <!-- Template for the jol-report element, which is the top-level element in the Jol report -->
    <xsl:template match="jol-report">

        <!-- The Jol summary section -->
        <div class="summarysection">

            <!-- Report header -->
            <div class="summaryheader">Object Layout</div>

            <!-- Intro text -->
            <div class="summarysectionitem">
                <span class="summaryintro">
                    Jol <xsl:value-of select="@version"/> report created
                    <xsl:value-of select="@date"/>&#160;<xsl:value-of select="@time"/>
                </span>
            </div>

            <!-- Summary table -->
            <div class="summarysectionitem">
                <xsl:call-template name="output-summary-table"/>
            </div>

            <!-- Output a link to the detailed html report if it is defined -->
            <xsl:if test="string-length($detailed-report-path) &gt; 0">
                <div class="summarysectionitem">
                    <a class="data" href="{$detailed-report-path}" target="_blank">Jol details</a>
                </div>
            </xsl:if>

        </div>

    </xsl:template>



    <!-- Output a table with a summary of the Jol analysis -->
    <xsl:template name="output-summary-table">
        <table class="neutralbg">
            <!-- Output the table row -->
            <tr>
                <td class="summaryvalue"><xsl:value-of select="count(packages/package/class)"/></td>
                <td class="summaryvalue"><xsl:value-of select="@total-internal-gap-size"/></td>
                <td class="summaryvalue"><xsl:value-of select="@total-external-gap-size"/></td>
            </tr>
            <tr>
                <td class="summarylabel">analyzed classes</td>
                <td class="summarylabel">total internal gap size</td>
                <td class="summarylabel">total external gap size</td>
            </tr>
        </table>
    </xsl:template>

</xsl:stylesheet>
