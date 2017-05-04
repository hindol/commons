package com.github.hindol.commons.xml.internal;

import com.github.hindol.commons.xml.Xml;

import java.util.Arrays;

public class XmlEscaper implements Xml.Escaper, Xml.Unescaper {

    private static final String ESCAPE_CHARACTERS = "\"'<>&";
    private static final String[] SUBSTITUTES = new String[]{
        "&quot;", "&apos;", "&lt;", "&gt;", "&amp;"
    };

    private XmlEscaper() {}

    public static XmlEscaper create() {
        return new XmlEscaper();
    }

    private String forward(char input) {
        int index = ESCAPE_CHARACTERS.indexOf(input);
        return index > -1 ? SUBSTITUTES[index] : "";
    }

    private char backward(String input) {
        int length = input.length();
        if (length >= 4 && length <= 6) {
            int index = Arrays.asList(SUBSTITUTES).indexOf(input);
            if (index > -1) {
                return ESCAPE_CHARACTERS.charAt(index);
            }
        }
        return '\0';
    }

    private int indexOfCdataEnd(String input, int beginIndex) {
        int endIndex = -1;
        if (beginIndex + 9 < input.length() && input.indexOf("<![CDATA[", beginIndex) == beginIndex) {

            endIndex = input.indexOf("]]>");
            if (endIndex > -1) {
                endIndex += 3;
            }
        }
        return endIndex;
    }

    @Override
    public String escapeAttribute(String input) {
        return escape(input, false);
    }

    @Override
    public String unescapeAttribute(String input) {
        return unescape(input, false);
    }

    @Override
    public String escapeContent(String input) {
        return escape(input, true);
    }

    @Override
    public String unescapeContent(String input) {
        return unescape(input, true);
    }

    private String escape(String input, boolean skipCdata) {
        StringBuilder builder = null;

        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);

            if (skipCdata && c == '<') {
                int cdataEndIndex = indexOfCdataEnd(input, i);
                if (cdataEndIndex > -1) {
                    if (builder != null) {
                        builder.append(input.substring(i, cdataEndIndex));
                    }
                    i = cdataEndIndex - 1;
                    continue;
                }
            }

            String substitute = forward(c);
            if ("".equals(substitute)) {
                if (builder != null) {
                    builder.append(c);
                }
            } else {
                if (builder == null) {
                    builder = new StringBuilder(input.substring(0, i));
                }
                builder.append(substitute);
            }
        }

        return builder != null ? builder.toString() : input;
    }

    private String unescape(String input, boolean skipCdata) {
        StringBuilder builder = null;

        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (skipCdata && c == '<') {
                int cdataEndIndex = indexOfCdataEnd(input, i);
                if (cdataEndIndex > -1) {
                    if (builder != null) {
                        builder.append(input.substring(i, cdataEndIndex));
                    }
                    i = cdataEndIndex - 1;
                }
            } else if (c == '&') {
                final int end = input.indexOf(';', i) + 1;
                char substitute = backward(input.substring(i, end));
                if (substitute != '\0') {
                    if (builder == null) {
                        builder = new StringBuilder(input.substring(0, i));
                    }
                    builder.append(substitute);
                    i = end - 1;
                } else {
                    throw new IllegalArgumentException("Unescaped '&' in input string.");
                }
            } else {
                if (builder != null) {
                    builder.append(c);
                }
            }
        }

        return builder != null ? builder.toString() : input;
    }
}
