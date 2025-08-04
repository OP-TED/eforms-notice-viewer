<#--
    FreemarkerTemplate.FRAGMENT_INVOCATION
    Used by MarkupGenerator.renderFragmentInvocation

    Invokes a previously defined fragment by its identifier.
    In EFX Template terminology, a fragment is a piece of markup that can be reused.
    Fragments are defined once and can be invoked as many times as needed.
    As a concept, a fragment is the EFX equivalent of an xsl:template in XSLT.
    This template is used to invoke a fragment by its name and pass parameters to it.
    It does so by using the xsl:call-template element.

    Parameters:
    - name:         Fragment identifier.
    - parameters:   List of parameters to pass to the fragment.
                    Each parameter is an object with parameter.name and parameter.value properties.

-->

<xsl:call-template name="${name}">
    <#list parameters as parameter>
        <xsl:with-param name="${parameter.name}" select = "${parameter.value}" />
    </#list>
</xsl:call-template>
