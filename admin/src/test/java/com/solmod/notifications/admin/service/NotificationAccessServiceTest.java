package com.solmod.notifications.admin.service;

import com.solmod.notifications.admin.repository.NotificationGroupRepo;
import com.solmod.notifications.admin.repository.model.MessageTemplate;
import com.solmod.notifications.admin.repository.model.NotificationGroup;
import com.solmod.notifications.admin.repository.model.Theme;
import com.solmod.notifications.admin.repository.model.ThemeCriteria;
import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class NotificationAccessServiceTest {

    @InjectMocks
    NotificationAccessService service;
    @Mock
    NotificationGroupRepo repo;

    @BeforeEach
    void setup() {
        openMocks(this.getClass());
    }

    @Test
    void testFromEntity() {
        String mockVerb = "someverb";
        String mockSubject = "somesubject";
        long mockTenantId = 1L;

        NotificationGroup mockGroup = buildMockGroup();
        when(repo.findByTenantIdAndSubjectAndVerb(mockTenantId, mockSubject, mockVerb)).thenReturn(mockGroup);

        MessageTemplateGroupDTO result = service.getNotificationTemplateGroup(mockTenantId, mockSubject, mockVerb);

        System.out.println("hi");
    }

    private NotificationGroup buildMockGroup() {
        NotificationGroup result = new NotificationGroup();
        result.setTenantId(1L);
        result.setSubject("somesubject");
        result.setVerb("someverb");
        Theme theme1 = new Theme();
        theme1.setResendInterval(15);
        theme1.setResendIntervalPeriod(Calendar.MINUTE);
        ThemeCriteria mockCriteria1 = new ThemeCriteria();
        mockCriteria1.setKey("criteria1akey");
        mockCriteria1.setValue("criteria1aval");
        theme1.setCriteria(List.of(mockCriteria1));

        MessageTemplate mockTemplate1 = new MessageTemplate();
        mockTemplate1.setMaxSend(15);
        mockTemplate1.setSender("someSenderTemplate1");
        mockTemplate1.setResendInterval(2);
        mockTemplate1.setResendIntervalPeriod(Calendar.HOUR);
        mockTemplate1.setRecipientAddressContextKey("template1recipientaddy");
        mockTemplate1.setMessageBodyContentKey("messagebodycontentkey");

        theme1.setMessageTemplates(Set.of(mockTemplate1));
        Set<Theme> themes = Set.of(theme1);
        result.setThemes(themes);
        return result;
    }

}