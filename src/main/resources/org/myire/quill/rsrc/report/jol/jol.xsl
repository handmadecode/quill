<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a Jol XML report into part of an HTML page.
 *
 * 2020-03-12 /PF    Created.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>


    <!-- Main template for the document root -->
    <xsl:template match="/">
        <xsl:apply-templates select="jol-report"/>
    </xsl:template>


    <!-- Template for the scent-report element, which is the top-level element in the Jol report -->
    <xsl:template match="jol-report">

        <div class="mainsection">

            <!-- Report header -->
            <div class="mainheader">Object Layout Report</div>

            <!-- Analysis run timestamp table -->
            <xsl:call-template name="output-timestamp-table" />

            <!-- Statistics for all packages -->
            <xsl:call-template name="output-total-table" />

            <!-- Layout info -->
            <xsl:call-template name="output-layout-info-table" />

        </div>

    </xsl:template>


    <!-- Output a table with the analysis run timestamp-->
    <xsl:template name="output-timestamp-table">
        <table class="mainsectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
            <tr>
                <td class="data">
                    Jol analysis run on<xsl:text>&#32;</xsl:text>
                    <xsl:value-of select="@date"/>
                    <xsl:text>&#32;</xsl:text>
                    <xsl:value-of select="@time"/>
                    <br/>Jol version: <xsl:value-of select="@version"/>
                    <br/>Analysis type: <xsl:value-of select="@description"/>
                </td>
            </tr>
        </table>
    </xsl:template>


    <!-- Output a table with statistics for all analyzed classes -->
    <xsl:template name="output-total-table">
        <table class="mainsectionitem" width="30%" cellpadding="2" cellspacing="0" border="0">
            <colgroup>
                <col width="80%"/><col width="20%"/>
            </colgroup>
            <tr>
                <td class="label">Number of analyzed classes:</td>
                <td class="data" align="right"><xsl:value-of select="count(packages/package/class)"/></td>
            </tr>
            <xsl:if test="@total-internal-gap-size &gt; 0">
                <tr>
                    <td class="label">Total size of internal alignment gaps:</td>
                    <td class="emphasizeddata" align="right"><xsl:value-of select="@total-internal-gap-size"/></td>
                </tr>
            </xsl:if>
            <xsl:if test="@total-external-gap-size &gt; 0">
                <tr>
                    <td class="label">Total size of external alignment gaps:</td>
                    <td class="emphasizeddata" align="right"><xsl:value-of select="@total-external-gap-size"/></td>
                </tr>
            </xsl:if>
        </table>
    </xsl:template>


    <!-- Output a table with layout info for all analyzed classes, grouped per package -->
    <xsl:template name="output-layout-info-table">
        <xsl:for-each select="packages/package">
            <div class="level1section">
                <div class="level1header"><xsl:value-of select="@name"/></div>
                <xsl:for-each select="class">
                    <div class="level2section">
                        <div class="level2header"><xsl:value-of select="@name"/></div>
                        <!-- Class info table -->
                        <xsl:call-template name="output-class-info-table"/>
                        <xsl:if test="count(field) &gt; 0">
                            <!-- Fields table -->
                            <xsl:call-template name="output-fields-table"/>
                        </xsl:if>
                    </div>
                </xsl:for-each>
           </div>
        </xsl:for-each>
    </xsl:template>


    <!-- Output a table with overall info of the current class node -->
    <xsl:template name="output-class-info-table">
        <table class="level2sectionitem" width="25%" cellpadding="2" cellspacing="0" border="0">
            <colgroup>
                <col width="80%"/><col width="20%"/>
            </colgroup>
            <tr>
                <td class="label">Object header size:</td>
                <td class="data" align="right"><xsl:value-of select="@header-size"/></td>
            </tr>
            <tr>
                <td class="label">Instance size:</td>
                <td class="data" align="right"><xsl:value-of select="@instance-size"/></td>
            </tr>
            <xsl:if test="@internal-gaps &gt; 0">
                <tr>
                    <td class="label">Total internal gap size:</td>
                    <td class="emphasizeddata" align="right"><xsl:value-of select="@internal-gaps"/></td>
                </tr>
            </xsl:if>
            <xsl:if test="@external-gaps &gt; 0">
                <tr>
                    <td class="label">Total external gap size:</td>
                    <td class="emphasizeddata" align="right"><xsl:value-of select="@external-gaps"/></td>
                </tr>
            </xsl:if>
        </table>
    </xsl:template>


    <!-- Output a table with the fields of the current class node -->
    <xsl:template name="output-fields-table">
        <table class="level2sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
            <!-- Table header with column names -->
            <colgroup>
                <col width="5%"/><col width="5%"/><col width="5%"/><col width="15%"/><col width="70%"/>
            </colgroup>
            <tr class="altrow">
                <td class="colheader" align="right">Offset</td>
                <td class="colheader" align="right">Size</td>
                <td/>
                <td class="colheader" align="left">Field</td>
                <td class="colheader" align="left">Type</td>
                <td/>
            </tr>
            <!-- One table row per field -->
            <xsl:for-each select="field">
                <tr>
                    <!-- Use alternate row characteristics every other row -->
                    <xsl:if test="position() mod 2 = 0">
                        <xsl:attribute name="class">altrow</xsl:attribute>
                    </xsl:if>
                    <xsl:choose>
                        <xsl:when test="@type='gap'">
                            <td class="emphasizeddata" align="right"><xsl:value-of select="@offset"/></td>
                            <td class="emphasizeddata" align="right"><xsl:value-of select="@size"/></td>
                            <td/>
                            <td class="emphasizeddata"><xsl:value-of select="@name"/></td>
                            <td class="emphasizeddata"><xsl:value-of select="@type"/></td>
                        </xsl:when>
                        <xsl:otherwise>
                            <td class="data" align="right"><xsl:value-of select="@offset"/></td>
                            <td class="data" align="right"><xsl:value-of select="@size"/></td>
                            <td/>
                            <td class="data"><xsl:value-of select="@name"/></td>
                            <td class="data"><xsl:value-of select="@type"/></td>
                        </xsl:otherwise>
                    </xsl:choose>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>


    <!-- Output a table cell as emphasized data if the value if non-zero, otherwise as normal data -->
    <xsl:template name="output-non-zero-emphasized-value">
        <xsl:param name="value"/>
        <td align="right">
            <xsl:choose>
                <xsl:when test="$value &gt; 0">
                    <xsl:attribute name="class">emphasizeddata</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">data</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="$value"/>
        </td>
    </xsl:template>

</xsl:stylesheet>
