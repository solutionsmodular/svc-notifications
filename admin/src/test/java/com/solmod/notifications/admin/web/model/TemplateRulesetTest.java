package com.solmod.notifications.admin.web.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRulesetTest {

    @Test
    void testEquals() {
        DeliveryCriterionSetDTO ruleset1 = new DeliveryCriterionSetDTO();
        Set<DeliveryCriteriaDTO> rules1 = Set.of(new DeliveryCriteriaDTO("akey", "avalue"), new DeliveryCriteriaDTO("bkey", "bvalue"));
        ruleset1.setCriteria(rules1);

        DeliveryCriterionSetDTO ruleset2 = new DeliveryCriterionSetDTO();
        Set<DeliveryCriteriaDTO> rules2 = Set.of(new DeliveryCriteriaDTO("akey", "avalue"), new DeliveryCriteriaDTO("bkey", "bvalue"));
        ruleset2.setCriteria(rules2);

        assertTrue(ruleset1.equals(ruleset2));
    }

    @Test
    void testNotEquals() {
        DeliveryCriterionSetDTO ruleset1 = new DeliveryCriterionSetDTO();
        Set<DeliveryCriteriaDTO> rules1 = Set.of(new DeliveryCriteriaDTO("akey", "avalue"), new DeliveryCriteriaDTO("bkey", "bvalue"), new DeliveryCriteriaDTO("ckey", "cvalue"));
        ruleset1.setCriteria(rules1);

        DeliveryCriterionSetDTO ruleset2 = new DeliveryCriterionSetDTO();
        Set<DeliveryCriteriaDTO> rules2 = Set.of(new DeliveryCriteriaDTO("akey", "avalue"), new DeliveryCriteriaDTO("bkey", "bvalue"));
        ruleset2.setCriteria(rules2);

        assertFalse(ruleset1.equals(ruleset2));
    }
}