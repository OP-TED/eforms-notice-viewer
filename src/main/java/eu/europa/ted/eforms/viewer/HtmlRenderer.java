package eu.europa.ted.eforms.viewer;


import java.io.StringWriter;
import eu.europa.ted.efx.NoticeRenderer;

public class HtmlRenderer extends StringWriter implements NoticeRenderer {

    public HtmlRenderer() {
        super();
    }

    int indent = 0;

    private void writeLine(String text) {
        this.write(String.format("%s%s\n", "\t".repeat(indent), text));
    }

    private void openTag(String tag, String attributes) {
        if (attributes == null || attributes.isEmpty()) {
            this.writeLine(String.format("<%s>", tag));
        } else {
            this.writeLine(String.format("<%s %s>", tag, attributes));
        }
        indent++;
    }

    private void openTag(String tag) {
        this.openTag(tag, "");
    }

    private void closeTag(String tag) {
        indent--;
        this.writeLine(String.format("</%s>", tag));
    }

    @Override
    public void beginFile() {
        this.openTag("html");
    }

    @Override
    public void endFile() {
        this.closeTag("html");
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void beginBlock(int level, String content) {
        this.openTag("section", String.format("class='level-%s'", level));
        this.writeLine(String.format("<span>%s</span>", content));
    }

    @Override
    public void endBlock() {
        this.closeTag("section");
    }
}
