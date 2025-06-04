<#-- 
    function_declaration.ftl (FreemarkerTemplate.FUNCTION_DECLARATION)
    Used by MarkupGenerator.renderFunctionDeclaration
    This template generates an XSLT function declaration 

    Parameters:
    - type: The return type of the function.
    - name: The name of the function to be declared.
    - parameters: A list of parameters for the function, each being a pair of name and type.
    - expression: The body of the function, which can be a sequence of XSLT instructions or an expression.    
    - udfNamespace: The namespace for the user-defined function (UDF).
-->

<xsl:function name="${udfNamespace}:${name}" as="${type}">
    <#-- Parameters -->
    <#if parameters?has_content>
        <#list parameters as param>
        <#--  <xsl:param name="${param.name}" as="${param.type}" />  -->
        <xsl:param name="${param.name}" />
        </#list>
    </#if>

    <#-- Function body -->
    <xsl:sequence select="${expression! '()'}"/>
</xsl:function>
