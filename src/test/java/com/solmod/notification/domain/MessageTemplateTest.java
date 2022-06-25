package com.solmod.notification.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTemplateTest {
    
    @Test
    void testEquals() {
        MessageTemplate template1 = new MessageTemplate();
        template1.getDeliveryCriteria().put("situation", "situated");
        MessageTemplate template2 = new MessageTemplate();
        template2.getDeliveryCriteria().put("situation", "situated");

        assertEquals(template1, template2);
    }

}