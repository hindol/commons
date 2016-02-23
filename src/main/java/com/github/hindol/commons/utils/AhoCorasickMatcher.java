package com.github.hindol.commons.utils;

import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.SearchResult;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * An immutable wrapper for the AhoCorasick class. The mutation of the class
 * state is limited within the constructors only. Once constructed, the class
 * cannot change state.
 *
 * Instances of this class can be accessed from multiple threads.
 */
public class AhoCorasickMatcher {

    private static final Pattern DEFAULT_DELIMITERS = Pattern.compile("\\W");

    private final AhoCorasick mTree;

    // Configuration
    private final boolean mMatchWholeWords;
    private final boolean mCaseInsensitive;
    private final Pattern mDelimiters;

    private AhoCorasickMatcher(Set<String> needles, boolean matchWholeWords,
                               Pattern delimiters, boolean caseInsensitive) {
        mTree = new AhoCorasick();
        mMatchWholeWords = matchWholeWords;
        mDelimiters = delimiters;
        mCaseInsensitive = caseInsensitive;

        for (String needle : needles) {
            addNeedle(transform(needle), needle);
        }

        mTree.prepare();
    }

    private AhoCorasickMatcher(Map<String, ?> needleOutputMap, boolean matchWholeWords,
                               Pattern delimiters, boolean caseInsensitive) {
        mTree = new AhoCorasick();
        mMatchWholeWords = matchWholeWords;
        mDelimiters = delimiters;
        mCaseInsensitive = caseInsensitive;

        for (Map.Entry<String, ?> entry : needleOutputMap.entrySet()) {
            addNeedle(transform(entry.getKey()), entry.getValue());
        }

        mTree.prepare();
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    private static <T> Iterator<T> cast(Iterator<?> rawIterator) {
        return (Iterator<T>) rawIterator;
    }

    @SuppressWarnings("unchecked")
    private static <T> Set<T> cast(Set<?> rawSet) {
        return (Set<T>) rawSet;
    }

    private String transform(String input) {
        if (mMatchWholeWords) {
            input = " " + mDelimiters.matcher(input).replaceAll(" ") + " ";
        }

        if (mCaseInsensitive) {
            input = input.toLowerCase();
        }

        return input; // Transformed input
    }

    /**
     * Find all occurrences of <em>all</em> needles in the provided haystack.
     *
     * @param haystack The haystack.
     * @return An iterable object where each next() moves on to the next needle.
     */
    public <T> Set<T> searchIn(String haystack) {
        Set<T> outputs = new HashSet<>();

        Iterator<SearchResult> iter = cast(
                mTree.search(transform(haystack).getBytes())
        );

        while (iter.hasNext()) {
            SearchResult result = iter.next();
            outputs.addAll(
                    cast(result.getOutputs())
            );
        }

        return outputs;
    }

    /**
     * Add a string that needs to be searched in our haystack.
     * See {@link #searchIn(String)}
     *
     * @param needle The needle.
     * @param output The output to produce when this needle is found.
     */
    private void addNeedle(String needle, Object output) {
        mTree.add(needle.getBytes(), output);
    }

    public static class Builder {

        private boolean mMatchWholeWords = false;
        private boolean mCaseInsensitive = false;
        private Pattern mDelimiters = DEFAULT_DELIMITERS;

        public Builder matchWholeWords() {
            mMatchWholeWords = true;
            return this;
        }

        public Builder withDelimiters(String delimiters) {
            return withDelimiters(Pattern.compile(delimiters));
        }

        public Builder withDelimiters(Pattern delimiters) {
            mDelimiters = delimiters;
            return this;
        }

        public Builder caseInsensitive() {
            mCaseInsensitive = true;
            return this;
        }

        public AhoCorasickMatcher build(Set<String> needles) {
            return new AhoCorasickMatcher(
                    needles, mMatchWholeWords, mDelimiters, mCaseInsensitive
            );
        }

        public AhoCorasickMatcher build(Map<String, ?> needleOutputMap) {
            return new AhoCorasickMatcher(
                    needleOutputMap, mMatchWholeWords, mDelimiters, mCaseInsensitive
            );
        }
    }
}
