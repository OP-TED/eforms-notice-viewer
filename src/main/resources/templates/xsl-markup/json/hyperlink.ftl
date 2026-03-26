<!-- Capture label HTML and extract text inline -->
<xsl:variable name="label-html">
  ${label}
</xsl:variable>
<xsl:value-of select="normalize-space(string($label-html))"/>
