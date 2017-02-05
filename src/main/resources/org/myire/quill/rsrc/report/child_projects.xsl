<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming child projects report reference into a part
 * of an HTML page.
 *
 * 2017-02-02 /PF    Created.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- Root element is 'child-projects' -->
    <xsl:template match="child-projects">
        <div class="summarysection">
          <!-- Section header -->
          <div class="summaryheader">Child projects</div>
          <!-- Links for each child project -->
          <xsl:apply-templates/>
        </div>
    </xsl:template>


    <!-- Element for one child project -->
    <xsl:template match="child-project">
        <div class="summarysectionitem">
            <a class="summarylabel" href="{@report}"><xsl:value-of select="@name"/></a>
        </div>
    </xsl:template>

</xsl:stylesheet>
