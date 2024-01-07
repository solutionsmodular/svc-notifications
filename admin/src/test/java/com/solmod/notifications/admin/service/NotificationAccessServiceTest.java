package com.solmod.notifications.admin.service;

import com.solmod.notifications.admin.repository.NotificationGroupRepo;
import com.solmod.notifications.admin.repository.model.MessageTemplate;
import com.solmod.notifications.admin.repository.model.NotificationGroup;
import com.solmod.notifications.admin.repository.model.Theme;
import com.solmod.notifications.admin.repository.model.ThemeCriteria;
import com.solmod.notifications.admin.web.model.DeliveryCriterionSetDTO;
import com.solmod.notifications.admin.web.model.MessageTemplateDTO;
import com.solmod.notifications.admin.web.model.MessageTemplateGroupDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @ParameterizedTest()
    @CsvSource({"true,false", "false,true", "true,true", "false,false"})
    void testFromEntity(String templateMaxSendLargest, String templateResendIntervalLargest) {
        String mockVerb = "someverb";
        String mockSubject = "somesubject";
        long mockTenantId = 1L;

        NotificationGroup mockGroup = buildMockGroup();
        Theme mockTheme = mockGroup.getThemes().stream().iterator().next();
        Collection<MessageTemplate> mockTemplates = mockTheme.getMessageTemplates();
        MessageTemplate mockTemplate = mockTemplates.iterator().next();

        // All permutations of maxSend and maxResendInterval being highest in template setting as compared to theme setting
        boolean templateMaxSendHigher = Boolean.parseBoolean(templateMaxSendLargest);
        boolean templateResendIntervalHigher = Boolean.parseBoolean(templateResendIntervalLargest);

        mockTheme.setMaxSend(templateMaxSendHigher ? 5 : 15);
        mockTheme.setResendInterval(templateResendIntervalHigher ? 5 : 15);
        mockTemplate.setMaxSend(templateMaxSendHigher ? 15 : 5);
        mockTemplate.setResendInterval(templateResendIntervalHigher ? 15 : 5);

        when(repo.findByTenantIdAndSubjectAndVerb(mockTenantId, mockSubject, mockVerb)).thenReturn(mockGroup);

        MessageTemplateGroupDTO result = service.getNotificationTemplateGroup(mockTenantId, mockSubject, mockVerb);

        Map<DeliveryCriterionSetDTO, Set<MessageTemplateDTO>> commonCriteriaDTO = result.getMessageTemplates();

        DeliveryCriterionSetDTO commonCriteria = commonCriteriaDTO.keySet().iterator().next();
        assertEquals(commonCriteria, result.getMessageTemplates().keySet().iterator().next());

        MessageTemplateDTO resultTemplateDTO = commonCriteriaDTO.values().iterator().next().iterator().next();
        assertEquals(mockTemplate.getMaxRetries(), resultTemplateDTO.getMaxRetries());
        assertEquals(mockTemplate.getMessageBodyContentKey(), resultTemplateDTO.getMessageBodyContentKey());
        assertEquals(mockTemplate.getMinWaitForRetry(), resultTemplateDTO.getMinWaitForRetry());
        assertEquals(mockTemplate.getRecipientAddressContextKey(), resultTemplateDTO.getRecipientAddressContextKey());
        assertEquals(mockTemplate.getSender(), resultTemplateDTO.getSender());
        // Should be highest (longest interval) of values
        assertEquals(templateResendIntervalHigher ? mockTemplate.getResendInterval() : mockTheme.getResendInterval(),
                resultTemplateDTO.getResendInterval());
        // Should be lowest of values
        assertEquals(templateMaxSendHigher ? mockTheme.getMaxSend() : mockTemplate.getMaxSend(),
                resultTemplateDTO.getMaxSend());
    }

    private NotificationGroup buildMockGroup() {
        NotificationGroup result = new NotificationGroup();
        result.setTenantId(1L);
        result.setSubject("somesubject");
        result.setVerb("someverb");
        Theme theme1 = new Theme();
        theme1.setResendInterval(15);
        ThemeCriteria mockCriteria1 = new ThemeCriteria();
        mockCriteria1.setKey("criteria1akey");
        mockCriteria1.setValue("criteria1aval");
        theme1.setCriteria(List.of(mockCriteria1));

        MessageTemplate mockTemplate1 = new MessageTemplate();
        mockTemplate1.setMaxSend(15);
        mockTemplate1.setSender("someSenderTemplate1");
        mockTemplate1.setResendInterval(2);
        mockTemplate1.setRecipientAddressContextKey("template1recipientaddy");
        mockTemplate1.setMessageBodyContentKey("messagebodycontentkey");

        theme1.setMessageTemplates(Set.of(mockTemplate1));
        Set<Theme> themes = Set.of(theme1);
        result.setThemes(themes);
        return result;
    }

}