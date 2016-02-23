package com.github.hindol.commons.file;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.testng.Assert.*;

public class IniTest {

    private static final String INI_FILE = "/test.ini";
    private Ini ini;
    private Ini.Section defaultSection;
    private Ini.Section section;

    @BeforeClass
    public void setUp() {
        try {
            File iniFile = new File(this.getClass().getResource(INI_FILE).toURI());
            ini = new Ini(iniFile);
            defaultSection = ini.getSection("default");
            section = ini.getSection("Section");
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSectionKeys() throws Exception {
        assertEquals(ini.getSectionKeys(),
                new HashSet<>(Arrays.<String>asList("default", "section")));
    }

    @Test
    public void testHasSection() throws Exception {
        assertTrue(ini.hasSection("Section"));
        assertTrue(ini.hasSection("section"));
    }

    @Test
    public void testContainsKey() throws Exception {
        assertTrue(section.containsKey("key", true));
        assertFalse(section.containsKey("key"));
    }

    @Test
    public void testGetKeys() throws Exception {
        assertTrue(section.getKeys().contains("section_key"));
        assertFalse(section.getKeys().contains("key"));

        assertTrue(section.getKeys(true).contains("key"));
    }

    @Test
    public void testGetString() throws Exception {
        assertEquals(defaultSection.getString("blank"), "");
        assertEquals(defaultSection.getString("blank_whitespace"), "");
        assertEquals(defaultSection.getString("key"), "value");
        assertEquals(defaultSection.getString("multiple_equals"), "key=value");

        assertEquals(defaultSection.getString("case_INSENSITIVE"), "case_sensitive");

        assertEquals(section.getString("section_key"), "section_value");
        assertEquals(section.getString("another_section_key"), "another_section_value");

        // Verify fallback to default section.
        assertEquals(section.getString("key"), "value");
    }

    @Test
    public void testGetStringList() throws Exception {
        assertEquals(defaultSection.getStringList("empty_array"), Collections.<String>emptyList());
        assertEquals(defaultSection.getStringList("array"), Arrays.asList("1", "2", "a", "b", "$"));
    }

    @Test
    public void testGetJsonAsString() throws Exception {
        String value = defaultSection.getJsonAsString("json");
        JSONObject json = new JSONObject(value);

        assertEquals(json.getString("key"), "value");
        assertEquals(json.getString("nested"), "{\"nestedKey\": \"nestedValue\"}");
        assertEquals(json.getString("semicolon"), ";");

        JSONObject nestedJson = new JSONObject(json.getString("nested"));
        assertEquals(nestedJson.getString("nestedKey"), "nestedValue");

        value = defaultSection.getJsonAsString("json_with_comment");
        assertTrue(value.startsWith("{"));
        assertTrue(value.endsWith("}"));

        value = defaultSection.getJsonAsString("json_array_with_comment");
        assertTrue(value.startsWith("["));
        assertTrue(value.endsWith("]"));

        value = defaultSection.getJsonAsString("key", "{}");
        assertEquals(value, "{}");

        value = defaultSection.getJsonAsString("key", "[]");
        assertEquals(value, "[]");
    }

    @Test
    public void testGetJsonAsStringThrows() throws Exception {
        boolean caught = false;
        try {
            defaultSection.getJsonAsString("key"); // Returns "value"
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);

        caught = false;
        try {
            defaultSection.getJsonAsString("key", "");
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    public void testGetInt() throws Exception {
        assertEquals(defaultSection.getInt("integer"), 365);

        boolean caught = false;
        try {
            defaultSection.getInt("key"); // Returns "value"
        } catch (NumberFormatException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    public void testGetFloat() throws Exception {
        assertEquals(defaultSection.getFloat("float"), -3.141f);

        boolean caught = false;
        try {
            defaultSection.getFloat("key"); // Returns "value"
        } catch (NumberFormatException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    public void testGetDouble() throws Exception {
        assertEquals(defaultSection.getDouble("double"), -3.14159);

        boolean caught = false;
        try {
            defaultSection.getDouble("key"); // Returns "value"
        } catch (NumberFormatException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    public void testIsCommentLine() throws Exception {
        assertTrue(Ini.isCommentLine("; Hello world!"));
        assertTrue(Ini.isCommentLine("# Hello world!"));
        assertTrue(Ini.isCommentLine(" ; Hello world!"));

        assertFalse(Ini.isCommentLine("key=;"));

        assertFalse(defaultSection.containsKey("comment"));
    }
}