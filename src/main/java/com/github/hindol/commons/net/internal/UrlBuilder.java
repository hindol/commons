package com.github.hindol.commons.net.internal;

import com.github.hindol.commons.net.URL;
import com.google.common.base.Strings;
import com.google.common.collect.Range;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class UrlBuilder implements URL.Builder {

    private static final Range<Integer> mValidPorts = Range.closed(0, 65535);

    private String mProtocol = "";
    private String mHost = "";
    private int mPort = URL.UNKNOWN_PORT;
    private String mPath = "/";
    private String mQuery = "";

    public URL.Builder setProtocol(String protocol) {
        checkState(!Strings.isNullOrEmpty(protocol), "Protocol cannot be null or empty.");
        mProtocol = protocol;
        return this;
    }

    public URL.Builder setHost(String host) {
        checkNotNull(host, "Host cannot be null. If setting explicitly, set it to \"\".");
        mHost = host;
        return this;
    }

    @Override
    public URL.Builder setPort(int port) {
        checkArgument(mValidPorts.contains(port), "Port number not within valid range.");
        mPort = port;
        return this;
    }

    @Override
    public URL.Builder setPath(String path) {
        checkNotNull(path, "Path cannot be null. If setting explicitly, set it to \"\".");
        checkArgument(path.isEmpty() || path.startsWith("/"), "Path must begin with a '/'.");
        mPath = path;
        return this;
    }

    @Override
    public URL.Builder setQuery(String query) {
        checkNotNull(query, "Query cannot be null. If setting explicitly, set it to \"\".");
        mQuery = query;
        return this;
    }

    @Override
    public String toUrl() {
        checkState(!Strings.isNullOrEmpty(mProtocol), "Protocol is not set.");

        StringBuilder builder = new StringBuilder();

        builder.append(mProtocol).append("://");

        if (!mHost.isEmpty()) {
            builder.append(mHost);
        }

        if (mPort != URL.UNKNOWN_PORT) {
            builder.append(":").append(mPort);
        }

        if (!mPath.isEmpty()) {
            builder.append(mPath);
        }

        if (!mQuery.isEmpty()) {
            builder.append("?").append(mQuery);
        }

        return builder.toString();
    }

    public static class QueryBuilder implements URL.QueryBuilder {

        private Map<String, String> mParameters = new LinkedHashMap<>();

        @Override
        public URL.QueryBuilder addParameter(String name, String value) {
            mParameters.put(URL.encode(name), URL.encode(value));
            return this;
        }

        @Override
        public URL.QueryBuilder addParameterUnencoded(String name, String value) {
            mParameters.put(URL.encode(name), value);
            return this;
        }

        @Override
        public String toQuery() {
            StringBuilder builder = new StringBuilder();

            String prefix = "";
            for (Map.Entry<String, String> p : mParameters.entrySet()) {
                builder.append(prefix).append(p.getKey()).append("=").append(p.getValue());
                prefix = "&";
            }

            return builder.toString();
        }
    }
}
