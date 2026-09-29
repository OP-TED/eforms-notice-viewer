<#--
  The plural-label-suffix function takes a quantity (a number) as a parameter, and returns the suffix
  that identifies the form of the label to use (singular, or one of the plural forms).
  The rules are language dependent: they follow the Unicode CLDR plural rules for the language
  passed in the $LANGUAGE parameter of the XSL transformation.
  See https://www.unicode.org/cldr/charts/latest/supplemental/language_plural_rules.html

  The suffix is empty for the singular (CLDR "one"), ".plural" for the general plural (CLDR "other"),
  and ".zero", ".two", ".few" or ".many" for the other CLDR categories.
  An empty quantity returns the empty suffix, so the label is displayed as if no quantity was given.

  The quantity must be converted to xs:decimal by the caller (a numeric field value is an xs:double).
  Trailing zeros are therefore lost ("1.0" is received as 1), so the rules for decimals are only as
  accurate as their input.

  CLDR operands used below:
    n  absolute value of the quantity
    i  integer digits of n
    v  number of visible fraction digits of n
    f  visible fraction digits of n, as an integer
-->
<xsl:function name="efx:plural-label-suffix" as="xs:string">
  <xsl:param name="quantity" as="xs:decimal?"/>
  <xsl:variable name="language" select="lower-case($LANGUAGE)"/>
  <xsl:variable name="n" select="abs($quantity)"/>
  <xsl:variable name="i" select="xs:integer(floor($n))"/>
  <xsl:variable name="fraction" select="substring-after(string($n), '.')"/>
  <xsl:variable name="v" select="string-length($fraction)"/>
  <xsl:variable name="f" select="if ($v = 0) then 0 else xs:integer($fraction)"/>

  <xsl:variable name="category" as="xs:string">
    <xsl:choose>
      <xsl:when test="empty($quantity)">
        <xsl:sequence select="'one'"/>
      </xsl:when>
      <!-- French -->
      <xsl:when test="$language = 'fr'">
        <xsl:sequence select="
          if ($i = (0, 1)) then 'one'
          else if ($v = 0 and $i != 0 and $i mod 1000000 = 0) then 'many'
          else 'other'"/>
      </xsl:when>
      <!-- Italian, Portuguese (European Portuguese, CLDR pt_PT) -->
      <xsl:when test="$language = ('it', 'pt')">
        <xsl:sequence select="
          if ($i = 1 and $v = 0) then 'one'
          else if ($v = 0 and $i != 0 and $i mod 1000000 = 0) then 'many'
          else 'other'"/>
      </xsl:when>
      <!-- Spanish -->
      <xsl:when test="$language = 'es'">
        <xsl:sequence select="
          if ($n = 1) then 'one'
          else if ($v = 0 and $i != 0 and $i mod 1000000 = 0) then 'many'
          else 'other'"/>
      </xsl:when>
      <!-- Bulgarian, Greek, Hungarian -->
      <xsl:when test="$language = ('bg', 'el', 'hu')">
        <xsl:sequence select="if ($n = 1) then 'one' else 'other'"/>
      </xsl:when>
      <!-- Danish -->
      <xsl:when test="$language = 'da'">
        <xsl:sequence select="if ($n = 1 or ($f != 0 and $i = (0, 1))) then 'one' else 'other'"/>
      </xsl:when>
      <!-- Latvian -->
      <xsl:when test="$language = 'lv'">
        <xsl:sequence select="
          if ($n mod 10 = 0 or $n mod 100 = (11 to 19) or ($v = 2 and $f mod 100 = (11 to 19))) then 'zero'
          else if (($n mod 10 = 1 and $n mod 100 != 11) or ($v = 2 and $f mod 10 = 1 and $f mod 100 != 11) or ($v != 2 and $f mod 10 = 1)) then 'one'
          else 'other'"/>
      </xsl:when>
      <!-- Lithuanian -->
      <xsl:when test="$language = 'lt'">
        <xsl:sequence select="
          if ($f != 0) then 'many'
          else if ($n mod 10 = 1 and not($n mod 100 = (11 to 19))) then 'one'
          else if ($n mod 10 = (2 to 9) and not($n mod 100 = (11 to 19))) then 'few'
          else 'other'"/>
      </xsl:when>
      <!-- Polish -->
      <xsl:when test="$language = 'pl'">
        <xsl:sequence select="
          if ($v != 0) then 'other'
          else if ($i = 1) then 'one'
          else if ($i mod 10 = (2 to 4) and not($i mod 100 = (12 to 14))) then 'few'
          else 'many'"/>
      </xsl:when>
      <!-- Czech, Slovak -->
      <xsl:when test="$language = ('cs', 'sk')">
        <xsl:sequence select="
          if ($v != 0) then 'many'
          else if ($i = 1) then 'one'
          else if ($i = (2 to 4)) then 'few'
          else 'other'"/>
      </xsl:when>
      <!-- Croatian -->
      <xsl:when test="$language = 'hr'">
        <xsl:sequence select="
          if (($v = 0 and $i mod 10 = 1 and $i mod 100 != 11) or ($f mod 10 = 1 and $f mod 100 != 11)) then 'one'
          else if (($v = 0 and $i mod 10 = (2 to 4) and not($i mod 100 = (12 to 14)))
                or ($f mod 10 = (2 to 4) and not($f mod 100 = (12 to 14)))) then 'few'
          else 'other'"/>
      </xsl:when>
      <!-- Romanian -->
      <xsl:when test="$language = 'ro'">
        <xsl:sequence select="
          if ($i = 1 and $v = 0) then 'one'
          else if ($v != 0 or $n = 0 or $n mod 100 = (1 to 19)) then 'few'
          else 'other'"/>
      </xsl:when>
      <!-- Slovenian -->
      <xsl:when test="$language = 'sl'">
        <xsl:sequence select="
          if ($v != 0) then 'few'
          else if ($i mod 100 = 1) then 'one'
          else if ($i mod 100 = 2) then 'two'
          else if ($i mod 100 = (3, 4)) then 'few'
          else 'other'"/>
      </xsl:when>
      <!-- Maltese -->
      <xsl:when test="$language = 'mt'">
        <xsl:sequence select="
          if ($n = 1) then 'one'
          else if ($n = 2) then 'two'
          else if ($n = 0 or $n mod 100 = (3 to 10)) then 'few'
          else if ($n mod 100 = (11 to 19)) then 'many'
          else 'other'"/>
      </xsl:when>
      <!-- Irish -->
      <xsl:when test="$language = 'ga'">
        <xsl:sequence select="
          if ($n = 1) then 'one'
          else if ($n = 2) then 'two'
          else if ($n = (3 to 6)) then 'few'
          else if ($n = (7 to 10)) then 'many'
          else 'other'"/>
      </xsl:when>
      <!-- English, German, Dutch, Swedish, Estonian, Finnish, and any other language -->
      <xsl:otherwise>
        <xsl:sequence select="if ($i = 1 and $v = 0) then 'one' else 'other'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:sequence select="
    if ($category = 'one') then ''
    else if ($category = 'other') then '.plural'
    else concat('.', $category)"/>
</xsl:function>
