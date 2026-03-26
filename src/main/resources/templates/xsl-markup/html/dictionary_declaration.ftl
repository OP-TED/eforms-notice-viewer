<#-- 
    FreeMarkerTemplate.DICTIONARY_DECLARATION 
    Used by MarkupGenerator.renderDictionaryDeclaration
    
    This template generates an xsl:key declaration.

    Parameters:
    - name: The name of the dictionary.
    - match: The nodes to which the key will be applied.
    - key: The expression used to calculate the key for each node.
-->
<xsl:key name="${name}" match="${match}" use="${key}" />
