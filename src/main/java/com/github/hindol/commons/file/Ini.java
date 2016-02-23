package com.github.hindol.commons.file;

import javax.annotation.concurrent.Immutable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Represents an INI file.
 */
@Immutable
public class Ini {

    private static final String COMMENT_CHARS = ";#";
    private static final String VALUE_SEPARATOR = ",";
    private static final String DEFAULT_SECTION_KEY = transformSectionKey("default");
    private static final boolean SECTION_KEY_CASE_SENSITIVE = false;

    private final Map<String, Section> mSections = new HashMap<>();

    public Ini(File file) throws IOException {
        load(file);
    }

    public Ini(String filename) throws IOException {
        load(new File(filename));
    }

    private void load(File file) throws IOException {
        FileReader reader = new FileReader(file);
        BufferedReader br = new BufferedReader(reader);

        String sectionKey = DEFAULT_SECTION_KEY;
        final Section defaultSection = new Section();
        mSections.put(sectionKey, defaultSection);

        String line;
        while ((line = br.readLine()) != null) {

            /* Ignore comment lines. */
            if (isCommentLine(line)) {
                continue;
            }

            if (isSectionLine(line)) {
                sectionKey = getSectionKey(line);

                if (!mSections.containsKey(sectionKey)) {
                    final Section section = new Section();
                    section.setDefaultSection(defaultSection);

                    mSections.put(sectionKey, section);
                }

                continue;
            }

            String[] pair = line.split("=", 2);
            if (pair.length == 2) {
                mSections.get(sectionKey).put(pair[0].trim(), pair[1].trim());
            }
        }
    }

    /**
     * Returns the set of section keys in *this* INI. Section 'default' is
     * always present, so the size of the set is >= 1.
     *
     * @return The set of section keys.
     */
    public Set<String> getSectionKeys() {
        return Collections.unmodifiableSet(mSections.keySet());
    }

    /**
     * Check if *this* INI has a section named {@code sectionKey}.
     *
     * @param sectionKey The section key.
     * @return True if section is found.
     */
    public boolean hasSection(String sectionKey) {
        return mSections.containsKey(transformSectionKey(sectionKey));
    }

    /**
     * Returns the section identified by {@code sectionKey}.
     *
     * @param sectionKey The section key.
     * @throws NoSuchElementException If section is not found.
     */
    public Section getSection(String sectionKey) {
        sectionKey = transformSectionKey(sectionKey);

        if (!mSections.containsKey(sectionKey)) {
            throw new NoSuchElementException("Section '" + sectionKey + "' not found.");
        }

        return mSections.get(sectionKey);
    }

    static String transformSectionKey(String sectionKey) {
        //noinspection ConstantConditions
        return SECTION_KEY_CASE_SENSITIVE ? sectionKey : sectionKey.toLowerCase();
    }

    /**
     * Check if the line is a comment line.
     *
     * @param line The line.
     * @return True if line is a comment line.
     */
    static boolean isCommentLine(String line) {
        if (line == null) {
            return false;
        }

        line = line.trim();

        /* Empty lines are also comment lines. */
        if (line.isEmpty()) {
            return true;
        }

        String firstChar = line.substring(0, 1);
        return COMMENT_CHARS.contains(firstChar);
    }

    /**
     * A quick check to identify section lines. Does not verify the validity
     * of the section name.
     *
     * @param line The line.
     * @return True if line is a section line.
     */
    static boolean isSectionLine(String line) {
        if (line == null) {
            return false;
        }

        line = line.trim();
        return line.startsWith("[") && line.endsWith("]");
    }

    /**
     * Attempt to extract the section name from the passed in line.
     *
     * @param line The line to parse.
     * @return The section key.
     * @throws IllegalArgumentException If line is not a valid section line.
     */
    static String getSectionKey(String line) {
        if (!isSectionLine(line)) {
            throw new IllegalArgumentException("Not a section.");
        }

        line = line.trim();
        String sectionKey = line.substring(1, line.length() - 1).trim();

        if (sectionKey.isEmpty()) {
            throw new IllegalArgumentException("Section key cannot be empty.");
        }

        return transformSectionKey(sectionKey);
    }

    /**
     * Represents a *section* of an INI file.
     */
    public static class Section {

        private static final boolean KEY_CASE_SENSITIVE = false;
        private final Map<String, String> mProperties;
        private Section mDefaultSection;

        private Section() {
            mProperties = new HashMap<>();
        }

        /**
         * Sets a fallback section for *this* section. When a key is not
         * found in *this* section, the key is looked up in the default
         * section.
         *
         * @param defaultSection The default section.
         */
        private void setDefaultSection(Section defaultSection) {
            mDefaultSection = defaultSection;
        }

        /**
         * Returns the set of keys for *this* section. If {@code includeDefault}
         * is true, also include keys from the default section.
         * <p>
         * This method is private to restrict mutability.
         *
         * @param includeDefault Include keys from the default section?
         * @return The set of keys.
         */
        public Set<String> getKeys(boolean includeDefault) {
            if (!includeDefault || mDefaultSection == null) {
                return Collections.unmodifiableSet( mProperties.keySet() );
            } else {
                Set<String> allKeys = new HashSet<>(mProperties.keySet());
                allKeys.addAll(mDefaultSection.getKeys());

                return allKeys;
            }
        }

        /**
         * Returns the set of keys for *this* section.
         * <p>
         * Also see {@link #getKeys(boolean)}
         *
         * @return The keys.
         */
        public Set<String> getKeys() {
            return getKeys(false);
        }

        /**
         * Check if a key exists in *this* section. If {@code includeDefault}
         * is true, also consider default section keys.
         *
         * @param key            The key.
         * @param includeDefault Include keys from the default section?
         * @return True if key is present.
         */
        public boolean containsKey(String key, boolean includeDefault) {
            key = transformKey(key);

            if (!includeDefault || mDefaultSection == null) {
                return mProperties.containsKey(key);
            } else {
                return mProperties.containsKey(key) || mDefaultSection.containsKey(key);
            }
        }

        /**
         * Check if a key exists in *this* section.
         * <p>
         * Also see {@link #containsKey(String, boolean)}
         *
         * @param key The key.
         * @return True if key if found.
         */
        public boolean containsKey(String key) {
            return containsKey(key, false);
        }

        /**
         * Put a key, value pair in *this* section.
         * <p>
         * This method is private to restrict mutability.
         *
         * @param key   The key.
         * @param value The value.
         * @return The newly inserted value.
         */
        private String put(String key, String value) {
            return mProperties.put(transformKey(key), value);
        }

        /**
         * Get the property value stored against {@code key}.
         *
         * @param key The property key.
         * @return The property value.
         * @throws NoSuchElementException If key is not present.
         */
        public String getString(String key) {
            key = transformKey(key);

            if (!containsKey(key)) {
                if (mDefaultSection == null || !mDefaultSection.containsKey(key)) {
                    throw new NoSuchElementException("Key '" + key + "' not found.");
                }

                return mDefaultSection.getString(key);
            }

            return mProperties.get(key);
        }

        /**
         * See {@link #getString(String)}
         */
        public String getString(String key, String defaultValue) {
            try {
                return getString(key);
            } catch (NoSuchElementException e) {
                return defaultValue;
            }
        }

        /**
         * Get the property value stored against {@code key} as a JSON string.
         *
         * @param key The key.
         * @return The value as a JSON string.
         * @throws NoSuchElementException   If key is not found.
         * @throws IllegalArgumentException If value is not valid JSON.
         */
        public String getJsonAsString(String key) {
            return extractJsonString(getString(key));
        }

        /**
         * See {@link #getJsonAsString(String)}
         *
         * @throws IllegalArgumentException If either value or defaultValue
         *                                  is not valid JSON.
         */
        public String getJsonAsString(String key, String defaultValue) {
            defaultValue = extractJsonString(defaultValue); // Throws

            try {
                return getJsonAsString(key);
            } catch (NoSuchElementException | IllegalArgumentException e) {
                return defaultValue;
            }
        }

        /**
         * Get the values against the specified {@code key} as a {@code List}.
         * Use {@code separator} to split the value.
         *
         * @param key       The key.
         * @param separator The separator used to split the key.
         * @return The values as a list.
         * @throws NoSuchElementException If key is not present.
         */
        public List<String> getStringList(String key, String separator) {
            String value = getString(key); // Throws

            if (value.isEmpty()) {
                return Collections.emptyList();
            }

            return Arrays.asList(value.split(separator));
        }

        /**
         * See {@link #getStringList(String, String)}
         */
        public List<String> getStringList(String key) {
            return getStringList(key, VALUE_SEPARATOR);
        }

        /**
         * Get the property value stored against {@code key} formatted as int.
         * <p>
         * Also see {@link #getString(String)}
         *
         * @throws NoSuchElementException If key is not present.
         * @throws NumberFormatException  If value cannot be converted to a int.
         */
        public int getInt(String key) {
            return Integer.parseInt(getString(key));
        }

        /**
         * See {@link #getInt(String)}
         */
        public int getInt(String key, int defaultValue) {
            try {
                return getInt(key);
            } catch (NoSuchElementException | NumberFormatException e) {
                return defaultValue;
            }
        }

        /**
         * See {@link #getInt(String)}
         */
        public Integer getInteger(String key, Integer defaultValue) {
            try {
                return Integer.valueOf(getString(key));
            } catch (NoSuchElementException | NumberFormatException e) {
                return defaultValue;
            }
        }

        /**
         * Get the property value stored against {@code key} formatted as float.
         * <p>
         * Also see {@link #getString(String)}
         *
         * @throws NoSuchElementException If key is not present.
         * @throws NumberFormatException  If value cannot be converted to a float.
         */
        public float getFloat(String key) {
            return Float.parseFloat(getString(key));
        }

        /**
         * See {@link #getFloat(String)}
         */
        public float getFloat(String key, float defaultValue) {
            try {
                return getFloat(key);
            } catch (NoSuchElementException | NumberFormatException e) {
                return defaultValue;
            }
        }

        /**
         * See {@link #getFloat(String)}
         */
        public Float getFloat(String key, Float defaultValue) {
            try {
                return Float.valueOf(getString(key));
            } catch (NoSuchElementException | NumberFormatException e) {
                return defaultValue;
            }
        }

        /**
         * Get the property value stored against {@code key} formatted as double.
         * <p>
         * Also see {@link #getString(String)}
         *
         * @throws NoSuchElementException If key is not present.
         * @throws NumberFormatException  If value cannot be converted to a double.
         */
        public double getDouble(String key) {
            return Double.parseDouble(getString(key));
        }

        /**
         * See {@link #getDouble(String)}
         */
        public double getDouble(String key, double defaultValue) {
            try {
                return getDouble(key);
            } catch (NoSuchElementException | NumberFormatException e) {
                return defaultValue;
            }
        }

        /**
         * See {@link #getDouble(String)}
         */
        public Double getDouble(String key, Double defaultValue) {
            try {
                return Double.valueOf(getString(key));
            } catch (NoSuchElementException | NumberFormatException e) {
                return defaultValue;
            }
        }

        /**
         * A *best effort* JSON extractor. Implementation can change from
         * time to time.
         *
         * @param value The extracted JSON as string.
         * @return The extracted JSON.
         */
        static String extractJsonString(String value) {
            int end = 0;
            if (value.startsWith("{")) {
                end = value.lastIndexOf("}") + 1;
            } else if (value.startsWith("[")) {
                end = value.lastIndexOf("]") + 1;
            }

            if (end > 0) {
                // Read till the closing parentheses or brace and ignore rest of
                // the characters.
                return value.substring(0, end);
            }

            throw new IllegalArgumentException("'" + value + "' is not valid JSON.");
        }

        static String transformKey(String key) {
            //noinspection ConstantConditions
            return KEY_CASE_SENSITIVE ? key : key.toLowerCase();
        }
    }
}
