<#-- 
    variable_declaration.ftl
    This template generates an XSLT variable declaration.

    Parameters:
    - name: The name of the variable
    - type: The type of the variable
    - initialiser: The expression to initialise the variable
-->
<xsl:variable name="${name}" as="${type}" <#if initialiser?has_content>select="{$initialiser}"</#if> />
