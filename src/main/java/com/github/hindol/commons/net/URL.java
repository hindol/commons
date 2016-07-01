package com.github.hindol.commons.net;

import com.github.hindol.commons.net.internal.UrlBuilder;
import com.github.hindol.commons.net.internal.UrlParser;
import com.google.common.base.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * URL related helper methods.
 */
public final class URL {

    public static final int UNKNOWN_PORT = -1;

    public static String decode(String value) {
        return decode(value, 1);
    }

    public static String decode(String value, int times) {
        try {
            while (--times >= 0) {
                String decoded = URLDecoder.decode(value, "UTF-8");
                if (value.equals(decoded)) {
                    break;
                }
                value = decoded;
            }
            return value;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Should not happen. UTF-8 is always supported.");
        }
    }

    /**
     * 1) Encodes stray characters first before decoding. This prevents decoder exception on
     * non-encoded strings.
     * 2) Encodes '+' so that decoding does not change it to ' '. This ensures the resulting
     * URL is still valid provided the original URL was valid.
     *
     * @param value The value to decode.
     * @return The decoded value.
     */
    public static String safeDecode(String value) {
        try {
            value = value.replaceAll("%(?![0-9a-fA-F]{2})", "%25").replaceAll("\\+", "%2B");
            return URLDecoder.decode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Should not happen. UTF-8 is always supported.");
        }
    }

    public static int timesEncoded(String value) {

        if (Strings.isNullOrEmpty(value)) {
            return 0;
        }

        int times = 0;
        try {
            String current = value;
            while (true) {
                if (current.contains("://") || current.contains("?")) {
                    break;
                }

                String next = decode(current);
                if (current.equals(next)) {
                    break;
                }

                ++times;
                current = next;
            }
        } catch (IllegalArgumentException iae) {
            // Stray '%' found, stop the loop.
        }

        return times;
    }

    public static String encode(String value) {
        return encode(value, 1);
    }

    public static String encode(String value, int times) {
        try {
            while (--times >= 0) {
                value = URLEncoder.encode(value, "UTF-8");
            }
            return value;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Should not happen. UTF-8 is always supported.");
        }
    }

    public static String ensureProtocol(String url, String protocolIfMissing) {
        return parse(url).protocol().isEmpty() ? protocolIfMissing + "://" + url : url;
    }

    public static boolean isDeepLink(String url) {
        final String protocol = parse(url).protocolOrDefault("http");
        return !("http".equals(protocol) || "https".equals(protocol));
    }

    public static String toHttps(String url) {

        String protocol = parse(url).protocol();

        switch (protocol) {
            case "":
                return "https://" + url;
            case "http":
                return url.replaceFirst("http://", "https://");
            default:
                return url;
        }
    }

    public static Parser parse(String url) {
        return new UrlParser(url);
    }

    public static Builder builder() {
        return new UrlBuilder();
    }

    public static QueryBuilder queryBuilder() {
        return new UrlBuilder.QueryBuilder();
    }

    public interface Parser {

        String protocol();
        String protocolOrDefault(String protocolIfMissing);
        String host();
        int port();
        String path();
        String query();

        QueryParser queryParser();
        Builder newBuilder();
    }

    public interface QueryParser {

        boolean hasParameter(String name);
        String parameter(String name);
        String parameterOrDefault(String name, String valueIfMissing);
    }

    public interface Builder {

        Builder setProtocol(String protocol);
        Builder setHost(String host);
        Builder setPort(int port);
        Builder setPath(String path);
        Builder setQuery(String query);

        String toUrl();
    }

    public interface QueryBuilder {

        QueryBuilder addParameter(String name, String value);
        QueryBuilder addParameterUnencoded(String name, String value);

        String toQuery();
    }
}
