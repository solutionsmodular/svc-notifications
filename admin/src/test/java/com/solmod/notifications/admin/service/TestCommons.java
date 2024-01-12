package com.solmod.notifications.admin.service;

import com.solmod.notifications.admin.repository.model.NotificationGroup;
import com.solmod.notifications.admin.repository.model.Theme;
import com.solmod.notifications.admin.repository.model.ThemeCriteria;
import com.solmod.notifications.admin.repository.model.TimelineMessageTemplate;

import java.util.Set;

public class TestCommons {

    public static NotificationGroup buildMockGroup() {
        NotificationGroup result = new NotificationGroup();
        result.setTenantId(1L);
        result.setSubject("somesubject");
        result.setVerb("someverb");
        Theme theme1 = new Theme();
        theme1.setResendInterval(15);
        ThemeCriteria mockCriteria1 = new ThemeCriteria();
        mockCriteria1.setKey("criteria1akey");
        mockCriteria1.setValue("criteria1aval");
        theme1.setCriteria(Set.of(mockCriteria1));

        TimelineMessageTemplate mockTemplate1 = new TimelineMessageTemplate();
        mockTemplate1.setMaxRetries(15);
        mockTemplate1.setMinWaitForRetry(60*10); // 10min
        mockTemplate1.setMaxSend(15);
        mockTemplate1.setSender("someSenderTemplate1");
        mockTemplate1.setResendInterval(2);
        mockTemplate1.setRecipientAddressContextKey("template1recipientaddy");
        mockTemplate1.setTimelineNodeType(TimelineMessageTemplate.TimelineNodeType.ALERT);
        mockTemplate1.setNodeTitleContentKey("nodetitlecontentkey");
        mockTemplate1.setMessageBodyContentKey("messagebodycontentkey");

        theme1.setMessageTemplates(Set.of(mockTemplate1));
        Set<Theme> themes = Set.of(theme1);
        result.setThemes(themes);
        return result;
    }

}
