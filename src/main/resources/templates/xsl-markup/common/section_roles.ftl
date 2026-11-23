<#--
  What the summary and the navigation sections do with the headings they display.

  A template line does not know in which section it is displayed: the same callable template can be
  invoked from any section. A heading only gives its id, made of its outline address and of the
  element of the notice it is displayed for. Each section is rendered into a variable, and then
  passed through the mode of its role:
  - the main section keeps the ids, which the navigation entries link to;
  - in mode "navigation", what a heading itself displays becomes a link to its id, and the heading
    keeps no id of its own;
  - in mode "summary", a heading keeps no id, because nothing links to it.
  Everything else is copied as it is. The ids are made when the sections are rendered against the
  notice, so the modes only read them: generate-id() is never called on the rendered sections.

  Every section of a template line is written as a section element, and the lines nested in a line
  are its child sections.
-->

  <xsl:mode name="navigation" on-no-match="shallow-copy"/>
  <xsl:mode name="summary" on-no-match="shallow-copy"/>

  <#--
    In the navigation, what a heading itself displays, its number and its content, becomes a link to
    the heading with the same id in the main section. The lines nested in it, and the lines of a
    template that it invokes, follow as they are: the headings among them are links of their own.
    A heading that displays nothing itself gets no link.
  -->
  <xsl:template match="section[@id]" mode="navigation">
    <xsl:variable name="content" select="node() except section"/>
    <xsl:copy>
      <xsl:copy-of select="@* except @id"/>
      <xsl:if test="normalize-space(string-join($content))">
        <a href="{concat('#', @id)}"><xsl:apply-templates select="$content" mode="navigation"/></a>
      </xsl:if>
      <xsl:apply-templates select="section" mode="navigation"/>
    </xsl:copy>
  </xsl:template>

  <#-- In the summary, headings are not link targets. -->
  <xsl:template match="section/@id" mode="summary"/>
