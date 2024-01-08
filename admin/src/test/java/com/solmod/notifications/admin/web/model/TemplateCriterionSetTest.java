package com.solmod.notifications.admin.web.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TemplateCriterionSetTest {

    @Test
    void testEquals() {
        DeliveryCriterionSetDTO ruleset1 = new DeliveryCriterionSetDTO();
        Map<String, String> criteria1 = Map.of("keya", "valuea", "keyb", "valueb");
        ruleset1.setCriteria(criteria1);

        DeliveryCriterionSetDTO ruleset2 = new DeliveryCriterionSetDTO();
        Map<String, String> criteria2 = Map.of("keyb", "valueb", "keya", "valuea");
        ruleset2.setCriteria(criteria2);

        assertEquals(ruleset1, ruleset2);
    }

    @Test
    void testNotEquals() {
        DeliveryCriterionSetDTO ruleset1 = new DeliveryCriterionSetDTO();
        Map<String, String> criteria1 = Map.of("keya", "valuea", "keyb", "valueb", "keyc", "valuec");
        ruleset1.setCriteria(criteria1);

        DeliveryCriterionSetDTO ruleset2 = new DeliveryCriterionSetDTO();
        Map<String, String> criteria2 = Map.of("keya", "valuea", "keyb", "valueb");
        ruleset2.setCriteria(criteria2);

        assertNotEquals(ruleset1, ruleset2);
    }
}