package com.github.hindol.commons.net;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UrlTest {

    @Test
    public void testTimesEncoded() throws Exception {
        assertEquals(URL.timesEncoded("www.vizury.com"), 0);
        assertEquals(URL.timesEncoded("www.vizury.com?utm_source=vizury"), 0);
        assertEquals(URL.timesEncoded("developers.google.com/interactive-media-ads/"), 0);
        assertEquals(URL.timesEncoded("namshi://n/target/?utm_source=testng"), 0);
        assertEquals(URL.timesEncoded(
            "https://www.google.co.in/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=complex+url"
        ), 0);

        assertEquals(URL.timesEncoded("www.vizury.com%3Futm_source%3Dvizury"), 1);
        assertEquals(URL.timesEncoded("developers.google.com%2Finteractive-media-ads%2F"), 1);
        assertEquals(URL.timesEncoded("namshi%3A%2F%2Fn%2Ftarget%2F%3Futm_source%3Dtestng"), 1);

        assertEquals(
            URL.timesEncoded("namshi%253A%252F%252Fn%252Ftarget%252F%253Futm_source%253Dtestng"),
            2
        );
    }

    @Test
    public void testDecode() throws Exception {
        assertEquals(
            URL.decode("namshi%3A%2F%2Fn%2Ftarget%2F%3Futm_source%3Dtestng"),
            "namshi://n/target/?utm_source=testng"
        );
        assertEquals(
            URL.decode("namshi%3A%2F%2Fn%2Ftarget%2F%3Futm_source%3Dtestng", 0),
            "namshi%3A%2F%2Fn%2Ftarget%2F%3Futm_source%3Dtestng"
        );
        assertEquals(
            URL.decode("namshi%253A%252F%252Fn%252Ftarget%252F%253Futm_source%253Dtestng", 2),
            "namshi://n/target/?utm_source=testng"
        );
    }

    @Test
    public void testEncode() throws Exception {
        assertEquals(
            URL.encode("namshi://n/target/?utm_source=testng"),
            "namshi%3A%2F%2Fn%2Ftarget%2F%3Futm_source%3Dtestng"
        );
        assertEquals(
            URL.encode("namshi%3A%2F%2Fn%2Ftarget%2F%3Futm_source%3Dtestng", 0),
            "namshi%3A%2F%2Fn%2Ftarget%2F%3Futm_source%3Dtestng"
        );
        assertEquals(
            URL.encode("namshi://n/target/?utm_source=testng", 2),
            "namshi%253A%252F%252Fn%252Ftarget%252F%253Futm_source%253Dtestng"
        );
    }

    @Test
    public void testEnsureProtocol() throws Exception {
        assertEquals(URL.ensureProtocol("www.vizury.com", "https"), "https://www.vizury.com");
        assertEquals(URL.ensureProtocol("http://www.vizury.com", "https"), "http://www.vizury.com");
    }

    @Test
    public void testIsDeepLink() throws Exception {
        assertTrue(URL.isDeepLink("mmyt://htl/listing/?checkin=05192016&checkout=05212016&city=GOI"));
        assertFalse(URL.isDeepLink(
            "http://hotelz.makemytrip.com/makemytrip/site/hotels/detail?" +
                "city=ATQ&country=INcheckin=06212016checkout=06232016")
        );
    }

    @Test
    public void testSafeDecode() throws Exception {
        assertEquals(
            URL.safeDecode("http://www.google.com?q=Guy+Ritchie"),
            "http://www.google.com?q=Guy+Ritchie"
        );
    }

    @Test
    public void testParse() throws Exception {
        URL.Parser parser = URL.parse("mmyt://htl/listing/?checkin=05192016&checkout=05212016&city=GOI");

        assertEquals(parser.protocol(), "mmyt");
        assertEquals(parser.host(), "htl");
        assertEquals(parser.path(), "/listing/");
        assertEquals(parser.query(), "checkin=05192016&checkout=05212016&city=GOI");

        URL.QueryParser query = parser.queryParser();

        assertEquals(query.parameter("checkin"), "05192016");
        assertEquals(query.parameter("checkout"), "05212016");
        assertEquals(query.parameter("city"), "GOI");

        parser = URL.parse("www.vizury.com:80/video/");

        assertEquals(parser.host(), "www.vizury.com");
        assertEquals(parser.port(), 80);

        parser = URL.parse("http://www.vizury.com:80/video/");

        assertEquals(parser.protocol(), "http");
        assertEquals(parser.host(), "www.vizury.com");
        assertEquals(parser.port(), 80);

        assertEquals(URL.parse("www.vizury.com").protocolOrDefault("https"), "https");

        assertEquals(URL.parse("www.google.com?q=Guy+Ritchie").queryParser().parameter("q"), "Guy Ritchie");
        assertEquals(
            URL.parse("www.google.com").queryParser().parameterOrDefault("utm_source", "vizury"),
            "vizury"
        );
        assertEquals(
            URL.parse("www.google.com?utm_source=").queryParser().parameterOrDefault("utm_source", "vizury"),
            ""
        );
    }

    @Test
    public void testBuilder() throws Exception {
        assertEquals(
            URL.parse(
                "mmyt://htl/listing/?checkin=05192016&checkout=05212016&city=GOI"
            ).newBuilder().toUrl(),
            "mmyt://htl/listing/?checkin=05192016&checkout=05212016&city=GOI"
        );
        assertEquals(
            URL.builder().setProtocol("https").toUrl(),
            "https:///"
        );
        assertEquals(
            URL.builder().setProtocol("https").setHost("www.vizury.com").toUrl(),
            "https://www.vizury.com/"
        );
    }
}
