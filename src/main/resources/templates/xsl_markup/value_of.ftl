<#--
    FreemarkerTemplate.VALUE_OF
    Used by MarkupGenerator.renderVariableExpression

    Evaluates the given expression and displays its value.

    Available variables:
    - expression: The variable expression to render
-->
<span class="value"><xsl:value-of separator=", " select="${expression}"/></span>