package com.github.hindol.commons.xml.internal;

import com.github.hindol.commons.xml.Xml;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class XmlWriterTest {

    private static final String XML =
        "<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"3.0\">" +
            "<Ad id=\"xyz\">" +
                "<Wrapper>" +
                    "<AdSystem>Vizury</AdSystem>" +
                    "<VASTAdTagURI><![CDATA[https://www.vizury.com/]]></VASTAdTagURI>" +
                    "<Error><![CDATA[https://www.vizury.com/]]></Error>" +
                    "<Impression><![CDATA[https://www.vizury.com/]]></Impression>" +
                    "<Creatives><Creative AdID=\"xyz\" /></Creatives>" +
                "</Wrapper>" +
            "</Ad>" +
        "</VAST>";

    @Test
    public void testCreate() throws Exception {
        assertEquals(
            Xml.createWriter()
                .beginElement("VAST")
                    .attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                    .attribute("xsi:noNamespaceSchemaLocation", "vast.xsd")
                    .attribute("version", "3.0")
                    .beginElement("Ad")
                        .attribute("id", "xyz")
                        .beginElement("Wrapper")
                            .element("AdSystem", "Vizury")
                            .element("VASTAdTagURI", Xml.wrapInCdata("https://www.vizury.com/"))
                            .element("Error", Xml.wrapInCdata("https://www.vizury.com/"))
                            .element("Impression", Xml.wrapInCdata("https://www.vizury.com/"))
                            .beginElement("Creatives")
                                .beginElement("Creative")
                                    .attribute("AdID", "xyz")
                                .endElement()
                            .endElement()
                        .endElement()
                    .endElement()
                .endElement().toXml(),
            XML
        );
    }
}