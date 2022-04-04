package eu.europa.ted.eforms.viewer;

import java.io.StringWriter;

public class IndentedStringWriter extends StringWriter {

    int indent = 0;

    public IndentedStringWriter(int indent) {
        this.indent = indent;
    }

    void writeLine(String text) {
        this.write(String.format("%s%s\n", "\t".repeat(indent), text));
    }

    void writeBlock(String text) {
        this.write(String.format("%s\n", text.replaceAll("(?m)^", "\t".repeat(indent))));
    }

    void openTag(String tag, String attributes) {
        if (attributes == null || attributes.isEmpty()) {
            this.writeLine(String.format("<%s>", tag));
        } else {
            this.writeLine(String.format("<%s %s>", tag, attributes));
        }
        indent++;
    }

    public void openTag(String tag) {
        this.openTag(tag, "");
    }

    public void closeTag(String tag) {
        indent--;
        this.writeLine(String.format("</%s>", tag));
    }
}
