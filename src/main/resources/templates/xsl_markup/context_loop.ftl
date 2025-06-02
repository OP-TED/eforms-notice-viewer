<#--
    FreemarkerTemplate.CONTEXT_LOOP
    Used by MarkupGenerator.renderContextLoop

    Evaluate an XPath (context) and repeat a given content for each item in the context sequence.
    
    Parameters:
    - context:   The context XPath to loop over.
    - content:   The content to be repeated at each loop iteration.
    - variables: A list of local variables to define within the loop. 
                 Each variable is an object with variable.name, variable.type, and variable.value properties.
-->

<xsl:for-each select="${context}">
    <#list variables as variable>
    <xsl:variable name="${variable.name}" as="${variable.type}" select = "${variable.value}" />
    </#list>
    ${content}
</xsl:for-each>