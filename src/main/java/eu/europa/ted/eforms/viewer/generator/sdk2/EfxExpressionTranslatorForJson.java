package eu.europa.ted.eforms.viewer.generator.sdk2;

import org.antlr.v4.runtime.BaseErrorListener;

import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.efx.interfaces.ScriptGenerator;
import eu.europa.ted.efx.interfaces.SymbolResolver;
import eu.europa.ted.efx.sdk2.EfxExpressionTranslatorV2;

@SdkComponent(versions = {"2"}, componentType = SdkComponentType.EFX_EXPRESSION_TRANSLATOR, qualifier = "json")
public class EfxExpressionTranslatorForJson extends EfxExpressionTranslatorV2 {

    public EfxExpressionTranslatorForJson(final SymbolResolver symbolResolver,
            final ScriptGenerator scriptGenerator, final BaseErrorListener errorListener) {
        super(symbolResolver, scriptGenerator, errorListener);
    }
}