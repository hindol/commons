package com.github.hindol.commons.util;

import org.testng.annotations.Test;

public class TemplateTest {

    private final Template mTemplateOne =
            Template.engine().compile("I tried to {{ verb }} your {{ noun }}.");
    private final Template mTemplateTwo =
            Template.engine().compile("{{ noun-one }} are falling on your {{ noun-two }}.");
    private final Template mTemplateThree =
            Template.engine().compile("To {{verb}} or not to {{verb}}.");

    @Test
    public void testFormat() throws Exception {
        System.out.println(mTemplateOne.format("verb", "bake", "noun", "cake"));
        System.out.println(mTemplateOne.format("verb", "roast", "noun", "toast"));

        System.out.println(mTemplateTwo.format("noun-one", "Raindrops", "noun-two", "head"));

        System.out.println(mTemplateThree.format("verb", "be"));
    }
}
