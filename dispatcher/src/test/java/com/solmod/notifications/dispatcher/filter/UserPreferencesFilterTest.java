package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.service.UserDeliveryPreferencesService;
import com.solmod.notifications.admin.web.model.UserDeliveryPreferencesDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

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
    @DisplayName("canDeliver - Assert SEND_NEVER when no preferences specified for template sender/class")
    void canDeliver_NoMatchingPreferences() throws FilterException {

        String email = "some.email@somewhere.com";
        String sender = "email";

        // Arrange
        TriggeredMessageTemplateGroup tGroup = new TriggeredMessageTemplateGroup();
        MessageTemplate template = new MessageTemplate();

        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(com.solmod.notifications.admin.repository.model.MessageTemplate.MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.email");
        tGroup.setQualifiedTemplates(Set.of(template));
        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));

        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);
        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        when(userDeliveryPreferencesService.getDeliveryPreferences(email, sender)).thenReturn(null);

        // Act
        FilterResponse apply = filter.apply(tGroup, msg);

        // Assert
        verify(userDeliveryPreferencesService, times(1)).getDeliveryPreferences(email, sender);
        assertEquals(SEND_NEVER, apply.getPermissions().get(template.getMessageTemplateID()).getVerdict());
    }

    @Test
    @DisplayName("canDeliver - Assert empty response and error log when recipient address cannot be determined")
    void canDeliver_NoRecipientAddressDetectable() {
        String email = "some.email@somewhere.com";
        String sender = "email";

        TriggeredMessageTemplateGroup tGroup = new TriggeredMessageTemplateGroup();
        MessageTemplate template = new MessageTemplate();

        template.setMessageTemplateID(5500L);
        template.setSender(sender); // This and class relate to preferences
        template.setMessageClass(com.solmod.notifications.admin.repository.model.MessageTemplate.MessageClass.TEAM.name());
        template.setRecipientAddressContextKey("root.user.wrongemailkey");
        tGroup.setQualifiedTemplates(Set.of(template));
        SolMessage msg = new SolMessage();
        TestObject v1 = new TestObject(new TestUser(email));

        Map<String, TestObject> data = Map.of("root", v1);
        msg.setData(data);
        Map<String, Object> metadata = msg.getMetadata();
        assertNotNull(metadata.get("root.user.email"));

        // Act
        Exception exception = assertThrows(FilterException.class, () -> {
            filter.apply(tGroup, msg);
        });


    }

    @Test
    @DisplayName("canDeliver - Assert SEND_NOW when preferences specify message class allowed")
    void canDeliver_MessageClassAllowed() {
    }

    @Test
    @DisplayName("canDeliver - Assert SEND_NEVER when preferences do not specify message class allowed")
    void canDeliver_MessageClassNotAllowed() {
    }

    @Test
    @DisplayName("canDeliver - Assert SEND_NOW when delivering within the specified window")
    void canDeliver_InWindow() {
    }

    @Test
    @DisplayName("canDeliver - Assert SEND_LATER when delivering outside the specified window")
    void canDeliver_OutsideWindow() {
    }

    @Test
    @DisplayName("canDeliver - Assert SEND_NOW when preferences specify resend interval and no deliveries found")
    void canDeliver_ResendIntervalSpecified_NoPreviousDeliveries() {
    }

    @Test
    @DisplayName("canDeliver - Assert SEND_NOW when preferences specify resend interval which has been met")
    void canDeliver_AfterResendInterval_PreviousDeliveries() {
    }

    @Test
    @DisplayName("canDeliver - Assert SEND_NEVER when preferences specify resend interval which has not been met")
    void canDeliver_BeforeResendInterval_PreviousDeliveries() {
    }


    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestObject implements Serializable {
        TestUser user = new TestUser();

        public TestUser getUser() {
            return user;
        }

        public void setUser(TestUser user) {
            this.user = user;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestUser implements Serializable {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}