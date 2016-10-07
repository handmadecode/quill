<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a JDepend XML report into part of an HTML
 * page.
 *
 * 2015-03-06 /PF    Created from javancss.xsl.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- Parameters where the XML file's modification timestamp are passed by the
         caller -->
    <xsl:param name="xml-modified-date"/>
    <xsl:param name="xml-modified-time"/>


    <!-- Main template for the document root -->
    <xsl:template match="/">
        <xsl:apply-templates select="JDepend"/>
    </xsl:template>


    <!-- Template for the JDepend element, which is the top-level element in
         the JDepend report -->
    <xsl:template match="JDepend">

        <div class="mainsection">

            <!-- Report header -->
            <div class="mainheader">JDepend Report</div>

            <!-- Overall statistics -->
            <xsl:call-template name="output-timestamp-table" />
            <xsl:call-template name="output-total-table" />

            <!-- Statistics per package -->
            <xsl:call-template name="output-packages-table" />

            <!-- Efferent and afferent couplings per package -->
            <xsl:call-template name="output-efferent-table" />
            <xsl:call-template name="output-afferent-table" />

            <!-- Cycles per package -->
            <xsl:call-template name="output-cycles-table" />
        </div>

    </xsl:template>


    <!-- Output a table with the analysis run timestamp-->
    <xsl:template name="output-timestamp-table">
        <table class="mainsectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
            <tr>
                <td class="data">
                    Analysis run on<xsl:text>&#32;</xsl:text>
                    <xsl:value-of select="$xml-modified-date"/>
                    <xsl:text>&#32;</xsl:text>
                    <xsl:value-of select="$xml-modified-time"/>
                </td>
            </tr>
        </table>
    </xsl:template>


    <!-- Output a table with some overall information -->
    <xsl:template name="output-total-table">
        <table class="mainsectionitem" width="20%" cellpadding="2" cellspacing="0" border="0">
          <colgroup>
            <col width="80%"/><col width="20%"/>
          </colgroup>
            <tr>
                <td class="label">Number of analyzed packages:</td>
                <td class="data" align="right"><xsl:value-of select="count(Packages/Package/Stats)"/></td>
            </tr>
            <tr>
                <td class="label">Max classes per package:</td>
                <td class="data" align="right"><xsl:call-template name="max-classes"/></td>
            </tr>
            <tr>
                <td class="label">Max afferent couplings:</td>
                <td class="data" align="right"><xsl:call-template name="max-afferent"/></td>
            </tr>
            <tr>
                <td class="label">Max efferent couplings:</td>
                <td class="data" align="right"><xsl:call-template name="max-efferent"/></td>
            </tr>
            <tr>
                <td class="label">Max distance from main sequence:</td>
                <td class="data" align="right"><xsl:call-template name="max-distance"/></td>
            </tr>
            <tr>
              <td class="label">Cycles found:</td>
              <xsl:variable name="num-cycles" select="count(Cycles/Package)"/>
              <td class="data" align="right">
                <xsl:choose>
                  <xsl:when test="$num-cycles &gt; 0">
                    <span class="warning"><xsl:value-of select="$num-cycles"/></span>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$num-cycles"/>
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
        </table>
    </xsl:template>


    <!-- Output a table with statistics for each analyzed package -->
    <xsl:template name="output-packages-table">
        <div class="level1section">
            <div class="level1header">Package statistics</div>
            <table class="level1sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
                <colgroup>
                    <col width="10%"/>
                    <col width="10%"/><col width="10%"/><col width="10%"/>
                    <col width="10%"/><col width="10%"/><col width="10%"/>
                    <col width="10%"/><col width="10%"/><col width="10%"/>
                </colgroup>
                <tr>
                    <td class="colheader">Name</td>
                    <td class="colheader" align="right">Total classes</td>
                    <td class="colheader" align="right">Abstract classes</td>
                    <td class="colheader" align="right">Concrete classes</td>
                    <td class="colheader" align="right">Afferent couplings</td>
                    <td class="colheader" align="right">Efferent couplings</td>
                    <td class="colheader" align="right">Abstractness</td>
                    <td class="colheader" align="right">Instability</td>
                    <td class="colheader" align="right">Main sequence distance</td>
                    <td class="colheader" align="right">Volatility</td>
                </tr>

                <xsl:for-each select="Packages/Package">
                  <xsl:sort data-type="number" order="descending" select="Stats/TotalClasses"/>
                  <xsl:if test="Stats">
                    <tr>
                        <!-- Use alternate row characteristics every other row -->
                        <xsl:if test="position() mod 2 = 0">
                            <xsl:attribute name="class">altrow</xsl:attribute>
                        </xsl:if>
                        <td class="data"><xsl:value-of select="@name"/></td>
                        <td class="data" align="right"><xsl:value-of select="Stats/TotalClasses"/></td>
                        <td class="data" align="right"><xsl:value-of select="Stats/AbstractClasses"/></td>
                        <td class="data" align="right"><xsl:value-of select="Stats/ConcreteClasses"/></td>
                        <td class="data" align="right"><xsl:value-of select="Stats/Ca"/></td>
                        <td class="data" align="right"><xsl:value-of select="Stats/Ce"/></td>
                        <td class="data" align="right"><xsl:value-of select="Stats/A"/></td>
                        <td class="data" align="right"><xsl:value-of select="Stats/I"/></td>
                        <td class="data" align="right"><xsl:value-of select="Stats/D"/></td>
                        <td class="data" align="right"><xsl:value-of select="Stats/V"/></td>
                    </tr>
                  </xsl:if>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>


    <!-- Output a table with efferent couplings (depends upon) for each analyzed package -->
    <xsl:template name="output-efferent-table">
        <xsl:if test="Packages/Package/DependsUpon/Package">
          <div class="level1section">
            <div class="level1header">Efferent (outgoing) couplings</div>
            <table class="level1sectionitem" width="80%" cellpadding="2" cellspacing="0" border="0">
                <colgroup>
                    <col width="30%"/><col width="70%"/>
                </colgroup>
                <xsl:for-each select="Packages/Package/DependsUpon">
                    <xsl:sort data-type="number" order="descending" select="count(Package)"/>
                    <tr>
                      <!-- Use alternate row characteristics every other row -->
                      <xsl:if test="position() mod 2 = 0">
                        <xsl:attribute name="class">altrow</xsl:attribute>
                      </xsl:if>
                        <xsl:call-template name="output-package-list-table-cells">
                            <xsl:with-param name="label" select="../@name"/>
                            <xsl:with-param name="separator">, </xsl:with-param>
                        </xsl:call-template>
                    </tr>
                </xsl:for-each>
            </table>
          </div>
        </xsl:if>
    </xsl:template>


    <!-- Output a table with afferent couplings (used by) of each analyzed package -->
    <xsl:template name="output-afferent-table">
        <xsl:if test="Packages/Package/UsedBy/Package">
            <div class="level1section">
                <div class="level1header">Afferent (incoming) couplings</div>
                <table class="level1sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
                    <colgroup>
                        <col width="10%"/><col width="90%"/>
                    </colgroup>
                    <xsl:for-each select="Packages/Package/UsedBy">
                        <xsl:sort data-type="number" order="descending" select="count(Package)"/>
                        <tr>
                            <!-- Use alternate row characteristics every other row -->
                            <xsl:if test="position() mod 2 = 0">
                                <xsl:attribute name="class">altrow</xsl:attribute>
                            </xsl:if>
                            <xsl:call-template name="output-package-list-table-cells">
                                <xsl:with-param name="label" select="../@name"/>
                                <xsl:with-param name="separator">, </xsl:with-param>
                            </xsl:call-template>
                        </tr>
                    </xsl:for-each>
                </table>
            </div>
        </xsl:if>
    </xsl:template>


    <!-- Output a table with cyclic dependencies per package -->
    <xsl:template name="output-cycles-table">
        <xsl:if test="Cycles/Package">
            <div class="level1section">
                <div class="level1header">Cycles</div>
                <table class="level1sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
                    <colgroup>
                        <col width="10%"/><col width="90%"/>
                    </colgroup>
                    <xsl:for-each select="Cycles/Package">
                        <xsl:sort data-type="number" order="descending" select="count(Package)"/>
                        <tr>
                            <!-- Use alternate row characteristics every other row -->
                            <xsl:if test="position() mod 2 = 0">
                                <xsl:attribute name="class">altrow</xsl:attribute>
                            </xsl:if>
                            <xsl:call-template name="output-package-list-table-cells">
                                <xsl:with-param name="label" select="@name"/>
                                <xsl:with-param name="separator"> -> </xsl:with-param>
                            </xsl:call-template>
                        </tr>
                    </xsl:for-each>
                </table>
            </div>
        </xsl:if>
    </xsl:template>


    <!-- Output a table cell containing the name attribute of the parent element and on cell
         containing the textual value of each Package element -->
    <xsl:template name="output-package-list-table-cells">
        <xsl:param name="label"/>
        <xsl:param name="separator"/>
        <td class="data" width="30%"><xsl:value-of select="$label"/></td>
        <td class="data">
            <xsl:for-each select="Package">
                <xsl:if test="position() &gt; 1">
                    <xsl:value-of select="$separator"/>
                </xsl:if>
                <xsl:value-of select="." />
            </xsl:for-each>
        </td>
    </xsl:template>


    <xsl:template name="max-classes">
        <xsl:for-each select="Packages/Package/Stats">
            <xsl:sort data-type="number" order="descending" select="TotalClasses"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="TotalClasses"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="max-afferent">
        <xsl:for-each select="Packages/Package/Stats">
            <xsl:sort data-type="number" order="descending" select="Ca"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="Ca"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="max-efferent">
        <xsl:for-each select="Packages/Package/Stats">
            <xsl:sort data-type="number" order="descending" select="Ce"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="Ce"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="max-distance">
        <xsl:for-each select="Packages/Package/Stats">
            <xsl:sort data-type="number" order="descending" select="D"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="D"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
