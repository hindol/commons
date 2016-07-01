package com.github.hindol.commons.net.internal;

import com.github.hindol.commons.net.URL;
import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class UrlParser implements URL.Parser {

    private String mProtocol = "";
    private String mHost = "";
    private int mPort = URL.UNKNOWN_PORT;
    private String mPath = "";
    private String mQuery = "";

    private QueryParser mQueryParser;

    private enum State {
        PROTOCOL,
        HOST,
        PORT,
        PATH,
        QUERY
    }

    public UrlParser(final String url) {
        checkArgument(!Strings.isNullOrEmpty(url), "URL cannot be null or empty.");
        State parserState = State.PROTOCOL;

        final int length = url.length();
        int start = 0;
        for (int i = 0; i < length; ++i) {
            char c = url.charAt(i);
            switch (c) {
                case ':':
                    switch (parserState) {
                        case PROTOCOL:
                            if (i + 2 < length && url.charAt(i + 1) == '/' && url.charAt(i + 2) == '/') {
                                mProtocol = url.substring(start, i);
                                start = (i += 2) + 1;
                                parserState = State.HOST;
                                break;
                            }
                            // Don't break, follow through.
                        case HOST:
                            mHost = url.substring(start, i);
                            start = i + 1;
                            parserState = State.PORT;
                            break;
                    }
                    break;
                case '/':
                    if (parserState == State.PROTOCOL || parserState == State.HOST) {
                        mHost = url.substring(start, i);
                        start = i;
                    } else if (parserState == State.PORT) {
                        try {
                            mPort = Integer.parseInt(url.substring(start, i));
                        } catch (NumberFormatException nfe) {
                            // Ignore, as this parser is intended to be lenient.
                        }
                        start = i;
                    }
                    parserState = State.PATH;
                    break;
                case '?':
                    switch (parserState) {
                        case HOST:
                            mHost = url.substring(start, i);
                        case PATH:
                            mPath = url.substring(start, i);
                            break;
                    }
                    start = i + 1;
                    parserState = State.QUERY;
                    mQuery = url.substring(start);
                    break;
            }
        }

        switch (parserState) {
            case PROTOCOL:
                mHost = url;
                break;
            case HOST:
                mHost = url.substring(start);
                break;
            case PATH:
                mPath = url.substring(start);
                break;
            case QUERY:
                mQuery = url.substring(start);
                break;
        }
    }

    @Override
    public String protocol() {
        return mProtocol;
    }

    @Override
    public String protocolOrDefault(String protocolIfMissing) {
        checkArgument(
                !Strings.isNullOrEmpty(protocolIfMissing),
                "Protocol cannot be null or empty."
        );
        return Strings.isNullOrEmpty(mProtocol) ? protocolIfMissing : mProtocol;
    }

    @Override
    public String host() {
        return mHost;
    }

    @Override
    public int port() {
        return mPort;
    }

    @Override
    public String path() {
        return mPath;
    }

    @Override
    public String query() {
        return mQuery;
    }

    @Override
    public URL.QueryParser queryParser() {
        if (mQueryParser == null) {
            mQueryParser = QueryParser.parse(mQuery);
        }
        return mQueryParser;
    }

    @Override
    public URL.Builder newBuilder() {
        URL.Builder builder = URL.builder()
                .setProtocol(mProtocol)
                .setHost(mHost)
                .setPath(mPath)
                .setQuery(mQuery);

        if (mPort != URL.UNKNOWN_PORT) {
            builder.setPort(mPort);
        }

        return builder;
    }

    private static class QueryParser implements URL.QueryParser {

        private Map<String, String> mParameters = new HashMap<>();

        private QueryParser(String query) {

            if (!Strings.isNullOrEmpty(query)) {
                for (String parameter : query.split("&")) {
                    String[] pair = parameter.split("=", 2);
                    if (pair.length == 2) {
                        try {
                            mParameters.put(URL.decode(pair[0]), URL.decode(pair[1]));
                        } catch (IllegalArgumentException iae) {
                            // Don't decode.
                            mParameters.put(pair[0], pair[1]);
                        }
                    }
                }
            }
        }

        public static QueryParser parse(String query) {
            return new QueryParser(query);
        }

        @Override
        public boolean hasParameter(String name) {
            return !Strings.isNullOrEmpty(parameter(name));
        }

        public String parameter(String name) {
            return mParameters.get(name);
        }

        public String parameterOrDefault(String name, String valueIfMissing) {
            String value = mParameters.get(name);
            return value == null ? valueIfMissing : value;
        }
    }
}
