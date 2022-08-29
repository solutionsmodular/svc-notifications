package com.solmod.notification.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageConfigTest {
    
    @Test
    void testEquals() {
        MessageConfig template1 = new MessageConfig();
        template1.getDeliveryCriteria().put("situation", "situated");
        MessageConfig template2 = new MessageConfig();
        template2.getDeliveryCriteria().put("situation", "situated");

        assertEquals(template1, template2);
    }

}