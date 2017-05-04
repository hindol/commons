package com.github.hindol.commons.xml.internal;

import com.github.hindol.commons.xml.Xml;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.Stack;

import static com.github.hindol.commons.xml.Xml.*;

public class XmlWriter implements Xml.Writer {

    private Element mRoot;
    private Stack<Element> mStack = new Stack<>();

    private XmlWriter() { }

    public static Xml.Writer create() {
        return new XmlWriter();
    }

    @Override
    public Xml.Writer beginElement(String name) {
        Element child = XmlElement.create(name);

        if (mStack.isEmpty()) {
            mRoot = child;
        } else {
            mStack.peek().withChild(child);
        }

        mStack.push(child);
        return this;
    }

    @Override
    public Xml.Writer endElement() {
        Preconditions.checkState(!mStack.isEmpty(), "endElement() called on empty stack.");
        mStack.pop();
        return this;
    }

    @Override
    public Xml.Writer attribute(String name, String value) {
        Preconditions.checkState(
            !mStack.isEmpty(),
            "No element to add attribute to. Call beginElement(String) first."
        );
        mStack.peek().withAttribute(name, value);
        return this;
    }

    @Override
    public Xml.Writer element(String name, String value) {
        Preconditions.checkState(
            !mStack.isEmpty(),
            "No element to add child element to. Call beginElement(String) first."
        );
        mStack.peek().withChild(name, value);
        return this;
    }

    @Override
    public String toXml() {
        Preconditions.checkState(mRoot != null, "Root element cannot be null.");
        Preconditions.checkState(
            mStack.isEmpty(),
            "endElement() should be called exactly as many times as beginElement(String)."
        );
        return mRoot.toXml();
    }

    @Override
    public void writeTo(Appendable appendable) throws IOException {
        mRoot.writeTo(appendable);
    }
}
