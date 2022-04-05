package eu.europa.ted.eforms.viewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import eu.europa.ted.eforms.viewer.helpers.JavaTools;
import eu.europa.ted.efx.EfxTemplateTranslator;

public class EfxToXslTranslatorTests {
    
    final String SDK_VERSION = "latest";

    @Test
    public void testTranslateFile() throws IOException {
        Path template = JavaTools.getResourceAsPath("eforms-sdk/notice-types/view-templates/X02.efx");
        String translation = EfxTemplateTranslator.renderTemplateFile(template, SDK_VERSION, new DependencyFactory());

        String folderName = "target/output-xsl";
        File folder = new File(folderName); 
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folderName + "/X02.xsl");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
        writer.write(translation);
        writer.close();
    }
}
