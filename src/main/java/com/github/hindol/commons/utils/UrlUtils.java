package com.github.hindol.commons.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Collection of various URL related utility methods.
 */
public class UrlUtils {

    private static final String EMPTY_STRING = "";

    private static final Pattern PATTERN_URL_NON_LETTERS =
            Pattern.compile("(?:(?:%(?:\\p{XDigit}{2})+)+|[^\\p{L}\\p{Nd}]+)+");

    private static final Pattern PATTERN_URL_STRAY_PERCENT = Pattern.compile("%(?!\\p{XDigit}{2})");
    private static final Pattern PATTERN_URL_RECURSIVE_DECODE =
            Pattern.compile("%(25)+(?=\\p{XDigit}{2})");

    /**
     * Find words (any language) inside URL.
     * Since URLs are typically percent encoded, care has been taken to
     * ensure percent encoded ASCII characters are ignored while looking
     * for words. Encoded characters are never "alphabetic".
     *
     * @param url The URL.
     * @return An array of tokens.
     */
    public static String[] tokenize(String url) {
        return PATTERN_URL_NON_LETTERS.split(url);
    }

    public static Pattern getPatternUrlNonLetters() {
        return PATTERN_URL_NON_LETTERS;
    }

    public static String replaceAllNonLetters(String url, String replacement) {
        return PATTERN_URL_NON_LETTERS.matcher(url).replaceAll(replacement);
    }

    public static String getParameterValueOrEmpty(String url, String paramName) {
        Map<String, String> matchedParams = getParameters(url, paramName);
        return matchedParams.isEmpty() ? EMPTY_STRING : matchedParams.get(paramName);
    }

    /**
     * Extracts parameters from the URL. Note that there is no consensus on how to
     * handle duplicate parameters (see http://stackoverflow.com/a/1746566/1019491).
     * Here we are keeping the last occurrence's value.
     *
     * @param url The URL to parse.
     * @param firstParamName First varargs parameter. Ensures at least one argument.
     * @param otherParamNames Rest of the varargs parameter.
     * @return The parameter name and their values in a {@code Map<String, String>}.
     */
    public static Map<String, String> getParameters(String url, String firstParamName,
                                             String... otherParamNames) {
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");
            Map<String, String> matchedParams = new HashMap<>();

            params.stream()
                    .filter(param -> matchesAny(param.getName(), firstParamName, otherParamNames))
                    .filter(param -> !param.getValue().isEmpty())
                    .forEach(param -> matchedParams.put(param.getName(), param.getValue()));

            return matchedParams;
        } catch (URISyntaxException e) {
            // Unable to parse
            return Collections.emptyMap();
        }
    }

    /**
     * Replace URL encoded characters with their raw versions.
     *
     * @param encodedUrl The URL.
     * @return The decoded URL.
     * @throws UnsupportedEncodingException
     */
    public static String decodeUrl(String encodedUrl) throws UnsupportedEncodingException {
        encodedUrl = PATTERN_URL_STRAY_PERCENT.matcher(encodedUrl).replaceAll("%25");
        return URLDecoder.decode(encodedUrl, "UTF-8");
    }

    /**
     * Replace URL encoded characters with their raw versions.
     *
     * @param encodedUrl The URL.
     * @return The decoded URL.
     * @throws UnsupportedEncodingException
     */
    public static String decodeUrlRecursive(String encodedUrl) throws UnsupportedEncodingException {
        String url = PATTERN_URL_RECURSIVE_DECODE.matcher(encodedUrl).replaceAll("%");
        return decodeUrl(url);
    }

    private static boolean matchesAny(String target, String firstQuery, String... otherQueries) {
        if (target.equals(firstQuery)) {
            return true;
        }

        for (String query : otherQueries) {
            if (target.equals(query)) {
                return true;
            }
        }

        return false;
    }
}
