package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.domain.MessageClass;
import com.solmod.notifications.admin.service.UserDeliveryPreferencesService;
import com.solmod.notifications.admin.web.model.UserDeliveryPreferencesDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.Serializable;
import java.util.*;

import static com.solmod.notifications.dispatcher.service.domain.DeliveryPermission.Verdict.SEND_NEVER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class UserPreferencesFilterTest {

    @InjectMocks
    UserPreferencesFilter filter;

    @Mock
    private MessageDeliveryRepo deliveryRepo;
    @Mock
    private UserDeliveryPreferencesService userDeliveryPreferencesService;

    @BeforeEach
    void setup() {
        openMocks(this);
    }

    @Test
    @DisplayName("apply - Assert SEND_NEVER when no preferences specified for template sender/class")
    void apply_NoMatchingPreferences() throws FilterException {

        String email = "some.email@somewhere.com";
        String sender = "email";

        // Arrange
        MessageTemplate template = new MessageTemplate();

        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.email");
        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));

        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);
        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        when(userDeliveryPreferencesService.getDeliveryPreferences(email, sender)).thenReturn(null);

        // Act
        DeliveryPermission result = filter.apply(template, msg.toTrigger());

        // Assert
        assertEquals(SEND_NEVER, result.getVerdict());
        assertTrue(result.getMessage().contains("has no specified preferences for email sender"));
        verify(userDeliveryPreferencesService, times(1)).getDeliveryPreferences(email, sender);
    }

    @Test
    @DisplayName("apply - Assert empty response and error log when recipient address cannot be determined")
    void apply_NoRecipientAddressDetectable() {
        String email = "some.email@somewhere.com";
        String sender = "email";

        MessageTemplate template = new MessageTemplate();

        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.wrongemailkey");
        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));

        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);
        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        // Act
        Exception exception = assertThrows(FilterException.class, () ->
                filter.apply(template, msg.toTrigger()));

        String exceptionMessage = exception.getMessage();
        assertTrue(exceptionMessage.contains("Could not determine recipient address"));
        assertTrue(exceptionMessage.contains("expected at root.user.wrongemailkey"));
    }

    @Test
    @DisplayName("apply - Assert SEND_NOW when preferences specify message class allowed")
    void apply_MessageClassAllowed() throws FilterException {
        String email = "some.email@somewhere.com";
        String sender = "email";

        MessageTemplate template = new MessageTemplate();
        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.email");

        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));
        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);

        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        UserDeliveryPreferencesDTO mockPrefs = new UserDeliveryPreferencesDTO();
        mockPrefs.setSender(sender);
        mockPrefs.setSupportedMessageClasses(MessageClass.TEAM.name() + "," + MessageClass.SELF.name());

        when(userDeliveryPreferencesService.getDeliveryPreferences(email, sender)).thenReturn(mockPrefs);

        DeliveryPermission result = filter.apply(template, msg.toTrigger());

        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, result);
    }

    @Test
    @DisplayName("apply - Assert SEND_NEVER when preferences do not specify message class allowed")
    void apply_MessageClassNotAllowed() throws FilterException {
        String email = "some.email@somewhere.com";
        String sender = "email";

        MessageTemplate template = new MessageTemplate();
        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.email");

        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));
        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);

        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        UserDeliveryPreferencesDTO mockPrefs = new UserDeliveryPreferencesDTO();
        mockPrefs.setSender(sender);
        mockPrefs.setSupportedMessageClasses(MessageClass.SELF.name());

        when(userDeliveryPreferencesService.getDeliveryPreferences(email, sender)).thenReturn(mockPrefs);

        DeliveryPermission result = filter.apply(template, msg.toTrigger());

        assertEquals(SEND_NEVER, result.getVerdict());
        assertTrue(result.getMessage().contains("preferences do not allow TEAM messages via email"));
    }

    @Test
    @DisplayName("apply - Assert SEND_NOW when delivering within the specified window")
    void apply_InWindow() throws FilterException {
        String email = "some.email@somewhere.com";
        String sender = "email";

        MessageTemplate template = new MessageTemplate();
        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.email");

        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));
        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);

        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        UserDeliveryPreferencesDTO mockPrefs = new UserDeliveryPreferencesDTO();
        mockPrefs.setSender(sender);
        mockPrefs.setSendWindowStart(8);
        mockPrefs.setSendWindowEnd(23);
        mockPrefs.setSupportedMessageClasses(MessageClass.TEAM.name() + "," + MessageClass.SELF.name());

        when(userDeliveryPreferencesService.getDeliveryPreferences(email, sender)).thenReturn(mockPrefs);

        DeliveryPermission result = filter.apply(template, msg.toTrigger());

        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, result);
    }

    @Test
    @DisplayName("apply - Assert SEND_LATER when delivering outside the specified window")
    void apply_OutsideWindow() throws FilterException {
        String email = "some.email@somewhere.com";
        String sender = "email";

        MessageTemplate template = new MessageTemplate();
        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.email");

        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));
        Map<String, TestObject> data = Map.of("root", v1);
        msg.setSubjectIdMetadataKey("root.user.email"); // since I know this will be in the metadata
        msg.setData(data);

        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        UserDeliveryPreferencesDTO mockPrefs = new UserDeliveryPreferencesDTO();
        mockPrefs.setSender(sender);
        mockPrefs.setSendWindowStart(3);
        mockPrefs.setSendWindowEnd(4);
        mockPrefs.setSupportedMessageClasses(MessageClass.TEAM.name() + "," + MessageClass.SELF.name());

        when(userDeliveryPreferencesService.getDeliveryPreferences(email, sender)).thenReturn(mockPrefs);
        MessageDelivery delivery = new MessageDelivery();
        Date now = new Date();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);
        nowCal.set(Calendar.HOUR_OF_DAY, 5); // Outside of the window 3-4

        delivery.setDateCreated(nowCal.getTime());
        when(deliveryRepo.findAllDeliveries(template.getMessageTemplateID(),
                email,
                "root.user.email", // use what we've already set up for email for simplicity
                email)).thenReturn(List.of(delivery));


        DeliveryPermission result = filter.apply(template, msg.toTrigger());

        assertEquals(DeliveryPermission.Verdict.SEND_LATER, result.getVerdict());
        assertTrue(result.getMessage().contains("within recipient blackout period"));
    }

    @Test
    @DisplayName("apply - Assert SEND_NOW when preferences specify resend interval and no deliveries found")
    void apply_ResendIntervalSpecified_NoPreviousDeliveries() throws FilterException {
        String email = "some.email@somewhere.com";
        String sender = "email";

        MessageTemplate template = new MessageTemplate();
        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.email");

        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));
        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);

        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        UserDeliveryPreferencesDTO mockPrefs = new UserDeliveryPreferencesDTO();
        mockPrefs.setSender(sender);
        mockPrefs.setResendInterval(5);
        mockPrefs.setSupportedMessageClasses(MessageClass.TEAM.name() + "," + MessageClass.SELF.name());

        when(userDeliveryPreferencesService.getDeliveryPreferences(email, sender)).thenReturn(mockPrefs);

        DeliveryPermission result = filter.apply(template, msg.toTrigger());

        assertEquals(DeliveryPermission.Verdict.SEND_NOW, result.getVerdict());
    }

    @Test
    @DisplayName("apply - Assert SEND_NOW when preferences specify resend interval which has been met")
    void apply_AfterResendInterval_PreviousDeliveries() throws FilterException {
        String email = "some.email@somewhere.com";
        String sender = "email";
        int resendInterval = 5;

        MessageTemplate template = new MessageTemplate();
        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.email");

        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));
        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);
        msg.setSubjectIdMetadataKey("root.user.email");

        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        UserDeliveryPreferencesDTO mockPrefs = new UserDeliveryPreferencesDTO();
        mockPrefs.setSender(sender);
        mockPrefs.setResendInterval(resendInterval);
        mockPrefs.setSupportedMessageClasses(MessageClass.TEAM.name() + "," + MessageClass.SELF.name());

        when(userDeliveryPreferencesService.getDeliveryPreferences(email, sender)).thenReturn(mockPrefs);
        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), -3 - resendInterval));
        when(deliveryRepo.findAllDeliveries(template.getMessageTemplateID(),
                email,
                "root.user.email", // use what we've already set up for email for simplicity
                email)).thenReturn(List.of(delivery));

        DeliveryPermission result = filter.apply(template, msg.toTrigger());

        assertEquals(DeliveryPermission.SEND_NOW_PERMISSION, result);
    }

    @Test
    @DisplayName("apply - Assert SEND_NEVER when preferences specify resend interval which has not been met")
    void apply_BeforeResendInterval_PreviousDeliveries() throws FilterException {
        String email = "some.email@somewhere.com";
        String sender = "email";
        int resendInterval = 5;

        MessageTemplate template = new MessageTemplate();
        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.email");

        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));
        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);
        msg.setSubjectIdMetadataKey("root.user.email");

        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        UserDeliveryPreferencesDTO mockPrefs = new UserDeliveryPreferencesDTO();
        mockPrefs.setSender(sender);
        mockPrefs.setResendInterval(resendInterval);
        mockPrefs.setSupportedMessageClasses(MessageClass.TEAM.name() + "," + MessageClass.SELF.name());

        when(userDeliveryPreferencesService.getDeliveryPreferences(email, sender)).thenReturn(mockPrefs);
        MessageDelivery delivery = new MessageDelivery();
        delivery.setDateCreated(DateUtils.addMinutes(new Date(), resendInterval - 2));
        when(deliveryRepo.findAllDeliveries(template.getMessageTemplateID(),
                email,
                "root.user.email", // use what we've already set up for email for simplicity
                email)).thenReturn(List.of(delivery));

        DeliveryPermission result = filter.apply(template, msg.toTrigger());

        assertEquals(SEND_NEVER, result.getVerdict());
        assertEquals("Recipient's interval settings for duplicate message has not elapsed", result.getMessage());
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class TestObject implements Serializable {
        private TestUser user = new TestUser();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class TestUser implements Serializable {
        private String email;
    }
}