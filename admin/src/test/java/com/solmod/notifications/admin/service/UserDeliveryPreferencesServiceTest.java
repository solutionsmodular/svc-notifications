package com.solmod.notifications.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solmod.notifications.admin.repository.UserDeliveryPreferencesRepo;
import com.solmod.notifications.admin.repository.model.UserDeliveryPreferences;
import com.solmod.notifications.admin.web.model.UserDeliveryPreferencesDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDeliveryPreferencesServiceTest extends TestCommons {

    UserDeliveryPreferencesService service;
    private UserDeliveryPreferencesRepo repo;

    @BeforeEach
    void setup() {
        repo = mock(UserDeliveryPreferencesRepo.class);
        service = new UserDeliveryPreferencesService(repo, new ObjectMapper());
    }

    @Test
    void getUserDeliveryPreferences() {
        // Arrange
        UUID userId = UUID.randomUUID();

        List<UserDeliveryPreferences> dpfs = List.of(
                buildUserDeliveryPreferences("someone@somewhere.com", "email", 10, userId),
                buildUserDeliveryPreferences("+8015551212", "sms", 10, userId)
                );

        when(repo.findByUserId(userId)).thenReturn(dpfs);

        // Act
        List<UserDeliveryPreferencesDTO> result = service.getUserDeliveryPreferences(userId);

        for (UserDeliveryPreferencesDTO curPref : result) {
            assertNotNull(curPref.getSupportedMessageClasses());
            assertNotNull(curPref.getSender());
            assertEquals(userId, curPref.getUserId());
            assertNotNull(curPref.getRecipientAddress());
            assertEquals(10, curPref.getResendInterval());
            assertNotNull(curPref.getResendInterval());
            assertNotNull(curPref.getSendWindowStart());
            assertNotNull(curPref.getSendWindowEnd());
            assertNotNull(curPref.getTimezone());
        }
    }
}