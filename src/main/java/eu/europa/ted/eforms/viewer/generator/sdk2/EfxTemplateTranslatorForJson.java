package eu.europa.ted.eforms.viewer.generator.sdk2;

import org.antlr.v4.runtime.BaseErrorListener;

import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.interfaces.ScriptGenerator;
import eu.europa.ted.efx.interfaces.SymbolResolver;
import eu.europa.ted.efx.sdk2.EfxTemplateTranslatorV2;

@SdkComponent(versions = { "2" }, componentType = SdkComponentType.EFX_TEMPLATE_TRANSLATOR, qualifier = "json")
public class EfxTemplateTranslatorForJson extends EfxTemplateTranslatorV2 {

    public EfxTemplateTranslatorForJson(final MarkupGenerator markupGenerator,
            final SymbolResolver symbolResolver, final ScriptGenerator scriptGenerator,
            final BaseErrorListener errorListener) {
        super(markupGenerator, symbolResolver, scriptGenerator, errorListener);
    }
}
