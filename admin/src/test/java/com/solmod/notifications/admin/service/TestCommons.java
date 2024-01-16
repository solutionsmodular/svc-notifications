package com.solmod.notifications.admin.service;

import com.solmod.notifications.admin.repository.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class TestCommons {

    @NotNull
    public UserDeliveryPreferences buildUserDeliveryPreferences(String recipientAddress, String sender, int resendInterval, UUID userId) {
        UserDeliveryPreferences mockPrefs = new UserDeliveryPreferences();
        mockPrefs.setMessageClass(MessageTemplate.MessageClass.TEAM.name());
        mockPrefs.setRecipientAddress(recipientAddress);
        mockPrefs.setSender(sender);
        mockPrefs.setResendInterval(resendInterval);
        mockPrefs.setSendWindowStart(8);
        mockPrefs.setSendWindowEnd(18);
        mockPrefs.setTimezone("PST");
        mockPrefs.setUserId(userId);
        return mockPrefs;
    }

    public static NotificationGroup buildMockGroup() {
        NotificationGroup result = new NotificationGroup();
        result.setTenantId(1L);
        result.setSubject("somesubject");
        result.setVerb("someverb");
        Theme theme = new Theme();
        theme.setResendInterval(15);
        ThemeCriteria mockCriteria = new ThemeCriteria();
        mockCriteria.setKey("criteria1akey");
        mockCriteria.setValue("criteria1aval");
        theme.setCriteria(Set.of(mockCriteria));

        TimelineMessageTemplate mockTemplate = new TimelineMessageTemplate();
        mockTemplate.setMaxRetries(15);
        mockTemplate.setMinWaitForRetry(60*10); // 10min
        mockTemplate.setMaxSend(15);
        mockTemplate.setSender("someSenderTemplate1");
        mockTemplate.setResendInterval(2);
        mockTemplate.setRecipientAddressContextKey("template1recipientaddy");
        mockTemplate.setTimelineNodeType(TimelineMessageTemplate.TimelineNodeType.ALERT);
        mockTemplate.setNodeTitleContentKey("nodetitlecontentkey");
        mockTemplate.setMessageBodyContentKey("messagebodycontentkey");
        mockTemplate.setMessageClass(MessageTemplate.MessageClass.GEN);

        theme.setMessageTemplates(Set.of(mockTemplate));
        Set<Theme> themes = Set.of(theme);
        result.setThemes(themes);
        return result;
    }

}
