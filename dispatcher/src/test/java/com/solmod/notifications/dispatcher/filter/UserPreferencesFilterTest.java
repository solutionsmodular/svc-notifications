package com.solmod.notifications.dispatcher.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPreferencesFilterTest {

    @Test
    @DisplayName("canDeliver - Assert SEND_NEVER when no preferences specified")
    void canDeliver_NoPreferences() {
    }

    @Test
    @DisplayName("canDeliver - Assert empty response and error log when recipient address cannot be determined")
    void canDeliver_NoRecipientAddressDetectable() {
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
}