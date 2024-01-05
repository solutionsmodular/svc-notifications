package com.solmod.notifications.admin.web.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRulesetTest {

    @Test
    void testEquals() {
        TemplateRuleset ruleset1 = new TemplateRuleset();
        List<TemplateRule> rules1 = List.of(new TemplateRule("akey", "avalue"), new TemplateRule("bkey", "bvalue"));
        ruleset1.setRules(rules1);

        TemplateRuleset ruleset2 = new TemplateRuleset();
        List<TemplateRule> rules2 = List.of(new TemplateRule("akey", "avalue"), new TemplateRule("bkey", "bvalue"));
        ruleset2.setRules(rules2);

        assertTrue(ruleset1.equals(ruleset2));
    }

    @Test
    void testNotEquals() {
        TemplateRuleset ruleset1 = new TemplateRuleset();
        List<TemplateRule> rules1 = List.of(new TemplateRule("akey", "avalue"), new TemplateRule("bkey", "bvalue"), new TemplateRule("ckey", "cvalue"));
        ruleset1.setRules(rules1);

        TemplateRuleset ruleset2 = new TemplateRuleset();
        List<TemplateRule> rules2 = List.of(new TemplateRule("akey", "avalue"), new TemplateRule("bkey", "bvalue"));
        ruleset2.setRules(rules2);

        assertFalse(ruleset1.equals(ruleset2));
    }
}