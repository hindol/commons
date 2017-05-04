package com.github.hindol.commons.xml.internal;

import com.github.hindol.commons.xml.Xml;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class XmlElement implements Xml.Element {

    private static final Xml.Escaper sXmlEscaper = Xml.getEscaper();

    private final String mName;
    private String mContent;

    private final List<Xml.Element> mChildren = new LinkedList<>();
    private final Map<String, String> mAttributes = new LinkedHashMap<>();

    private XmlElement(String name) {
        mName = name;
    }

    public static XmlElement create(String name) {
        Preconditions.checkNotNull(name, "Element name cannot be null.");
        return new XmlElement(name);
    }

    @Override
    public Xml.Element withAttribute(String name, String value) {
        Preconditions.checkNotNull(name, "Attribute name cannot be null.");
        Preconditions.checkNotNull(value, "Attribute value cannot be null.");
        mAttributes.put(name, sXmlEscaper.escapeAttribute(value));
        return this;
    }

    @Override
    public Xml.Element withContent(String content) {
        Preconditions.checkState(
            mChildren.isEmpty(),
            "XML element cannot have child element as well as text content."
        );

        if (!Strings.isNullOrEmpty(content)) {
            mContent = sXmlEscaper.escapeContent(content);
        }
        return this;
    }

    @Override
    public Xml.Element withChild(String name, String childContent) {
        Preconditions.checkState(
            mContent == null,
            "XML element cannot have child element as well as text content."
        );

        mChildren.add(create(name).withContent(childContent));
        return this;
    }

    @Override
    public Xml.Element withChild(Xml.Element child) {
        Preconditions.checkState(
            mContent == null,
            "XML element cannot have child element as well as text content."
        );

        mChildren.add(child);
        return this;
    }

    @Override
    public Xml.Element withChildren(Xml.Element first, Xml.Element second, Xml.Element... rest) {

        withChild(first);
        withChild(second);

        for (Xml.Element child : rest) {
            withChild(child);
        }

        return this;
    }

    @Override
    public String toXml() {

        Preconditions.checkState(
            (mContent == null) || (mChildren.isEmpty()),
            "XML element cannot have child element as well as text content."
        );

        StringBuilder builder = new StringBuilder();
        try {
            writeTo(builder);
        } catch (IOException e) {
            // StringBuilder never throws IOException.
        }

        return builder.toString();
    }

    @Override
    public void writeTo(Appendable appendable) throws IOException {
        appendable.append("<").append(mName);

        for (Map.Entry<String, String> entry : mAttributes.entrySet()) {
            appendable.append(" ").append(entry.getKey());
            appendable.append("=\"").append(entry.getValue()).append("\"");
        }

        if (!Strings.isNullOrEmpty(mContent)) {
            appendable.append(">").append(mContent).append("</").append(mName).append(">");
        } else if (!mChildren.isEmpty()) {
            appendable.append(">");

            for (Xml.Element child : mChildren) {
                child.writeTo(appendable);
            }

            appendable.append("</").append(mName).append(">");
        } else {
            appendable.append(" />");
        }
    }
}
