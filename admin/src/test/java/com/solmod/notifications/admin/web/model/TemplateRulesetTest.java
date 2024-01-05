package com.solmod.notifications.admin.web.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRulesetTest {

    @Test
    void testEquals() {
        DeliveryCriteriaSetDTO ruleset1 = new DeliveryCriteriaSetDTO();
        List<DeliveryCriteriaDTO> rules1 = List.of(new DeliveryCriteriaDTO("akey", "avalue"), new DeliveryCriteriaDTO("bkey", "bvalue"));
        ruleset1.setRules(rules1);

        DeliveryCriteriaSetDTO ruleset2 = new DeliveryCriteriaSetDTO();
        List<DeliveryCriteriaDTO> rules2 = List.of(new DeliveryCriteriaDTO("akey", "avalue"), new DeliveryCriteriaDTO("bkey", "bvalue"));
        ruleset2.setRules(rules2);

        assertTrue(ruleset1.equals(ruleset2));
    }

    @Test
    void testNotEquals() {
        DeliveryCriteriaSetDTO ruleset1 = new DeliveryCriteriaSetDTO();
        List<DeliveryCriteriaDTO> rules1 = List.of(new DeliveryCriteriaDTO("akey", "avalue"), new DeliveryCriteriaDTO("bkey", "bvalue"), new DeliveryCriteriaDTO("ckey", "cvalue"));
        ruleset1.setRules(rules1);

        DeliveryCriteriaSetDTO ruleset2 = new DeliveryCriteriaSetDTO();
        List<DeliveryCriteriaDTO> rules2 = List.of(new DeliveryCriteriaDTO("akey", "avalue"), new DeliveryCriteriaDTO("bkey", "bvalue"));
        ruleset2.setRules(rules2);

        assertFalse(ruleset1.equals(ruleset2));
    }
}