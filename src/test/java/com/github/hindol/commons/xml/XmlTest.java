package com.github.hindol.commons.xml;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class XmlTest {

    private static final Xml.Escaper sXmlEscaper = Xml.getEscaper();
    private static final Xml.Unescaper sXmlUnescaper = Xml.getUnescaper();

    @Test
    public void testEscape() throws Exception {
        assertEquals(sXmlEscaper.escapeContent("\""), "&quot;");
        assertEquals(sXmlEscaper.escapeContent("AT&T"), "AT&amp;T");
        assertEquals(sXmlEscaper.escapeContent("Microsoft"), "Microsoft");
        assertEquals(sXmlEscaper.escapeContent("<Company />"), "&lt;Company /&gt;");
        assertEquals(sXmlEscaper.escapeContent("<Company>AT&T</Company>"), "&lt;Company&gt;AT&amp;T&lt;/Company&gt;");
        assertEquals(sXmlEscaper.escapeContent("<![CDATA[AT&T]]>"), "<![CDATA[AT&T]]>");
        assertEquals(sXmlEscaper.escapeContent("<![CDATA[AT&T]]>&"), "<![CDATA[AT&T]]>&amp;");
    }

    @Test
    public void testUnescape() throws Exception {
        assertEquals(sXmlUnescaper.unescapeContent("AT&amp;T"), "AT&T");
        assertEquals(sXmlUnescaper.unescapeContent("&lt;Company /&gt;"), "<Company />");
        assertEquals(sXmlUnescaper.unescapeContent("&lt;Company&gt;AT&amp;T&lt;/Company&gt;"), "<Company>AT&T</Company>");
        assertEquals(sXmlUnescaper.unescapeContent("<![CDATA[AT&T]]>"), "<![CDATA[AT&T]]>");
    }
}