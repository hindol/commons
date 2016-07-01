package com.github.hindol.commons.util;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Template {

    private static final String BEGIN_MARKER = "{{";
    private static final String END_MARKER = "}}";

    private static final Engine ENGINE = new Engine(BEGIN_MARKER, END_MARKER);

    private final List<String> mParts;

    private Template(List<String> parts) {
        mParts = parts;
    }

    public static Template compile(String template) {
        return engine().compile(template);
    }

    public String format(String... parameters) {
        return format(newHashMap(parameters));
    }

    public String format(Map<String, ?> context) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < mParts.size(); ++i) {
            if (i % 2 == 0) {
                builder.append(mParts.get(i));
            } else {
                String key = mParts.get(i);
                Object value = context.get(key);
                checkArgument(value != null, "Context missing parameter " + key + "!");
                builder.append(value.toString());
            }
        }

        return builder.toString();
    }

    @SafeVarargs
    private static <T> Map<T, T> newHashMap(T... parameters) {
        Map<T, T> result = Maps.newHashMap();
        for (int i = 0; i < parameters.length; i += 2) {
            result.put(parameters[i], parameters[i + 1]);
        }
        return result;
    }

    public static Engine engine() {
        return ENGINE;
    }

    public static Engine createEngine(String beginMarker, String endMarker) {
        return new Engine(beginMarker, endMarker);
    }

    public static class Engine {

        private final String mBeginMarker;
        private final String mEndMarker;

        private Engine(String beginMarker, String endMarker) {
            mBeginMarker = beginMarker;
            mEndMarker = endMarker;
        }

        public String format(String template, Map<String, ?> context) {
            return compile(template).format(context);
        }

        public Template compile(String template) {
            checkNotNull(template);
            List<String> parts = new ArrayList<>();

            int start = 0;
            int begin = template.indexOf(mBeginMarker);
            while (begin >= 0) {
                int end = template.indexOf(mEndMarker, begin + 2);
                if (end > begin) {
                    parts.add(template.substring(start, begin));
                    parts.add(template.substring(begin + 2, end).trim());
                } else {
                    throw new IllegalArgumentException("Braces mismatch, template invalid!");
                }

                begin = template.indexOf(mBeginMarker, end + 2);
                start = end + 2;
            }

            parts.add(template.substring(start));

            return new Template(parts);
        }
    }
}
