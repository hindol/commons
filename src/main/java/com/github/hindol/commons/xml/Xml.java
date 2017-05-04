package com.github.hindol.commons.xml;

import com.github.hindol.commons.xml.internal.XmlEscaper;
import com.github.hindol.commons.xml.internal.XmlWriter;

import java.io.IOException;

public abstract class Xml {

    private static final XmlEscaper sXmlEscaper = XmlEscaper.create();

    public static Writer createWriter() {
        return XmlWriter.create();
    }

    public static Escaper getEscaper() {
        return sXmlEscaper;
    }

    public static Unescaper getUnescaper() {
        return sXmlEscaper;
    }

    public static String wrapInCdata(String content) {
        return "<![CDATA[" + content + "]]>";
    }

    public interface Element {

        Element withAttribute(String name, String value);

        Element withContent(String content);

        Element withChild(String name, String childContent);

        Element withChild(Element child);

        Element withChildren(Element first, Element second, Element... rest);

        String toXml();

        void writeTo(Appendable appendable) throws IOException;
    }

    public interface Writer {

        Writer beginElement(String name);

        Writer endElement();

        Writer attribute(String name, String value);

        Writer element(String name, String value);

        String toXml();

        void writeTo(Appendable appendable) throws IOException;
    }

    public interface Escaper {

        String escapeAttribute(String input);

        String escapeContent(String input);
    }

    public interface Unescaper {

        String unescapeAttribute(String input);

        String unescapeContent(String input);
    }
}
