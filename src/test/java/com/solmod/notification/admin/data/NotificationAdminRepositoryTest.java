package com.solmod.notification.admin.data;

import com.solmod.notification.domain.ContentLookupType;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.MessageTemplateStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {
    "classpath:/scripts/notification-admin-tests.sql"
})
class NotificationAdminRepositoryTest {

    @Autowired
    NotificationAdminRepository adminRepository;

    @Test
    void create() {
        MessageTemplate request = new MessageTemplate();
        request.setTenantId(1L);
        request.setEventSubject("Something");
        request.setEventVerb("Occurred");
        request.setBodyContentKey("some.body.key");
        request.setBodyContentLookupType(ContentLookupType.STATIC);
        request.setSummaryContentKey("some.summary.key");
        request.setSummaryContentLookupType(ContentLookupType.STATIC);
        request.setRecipientContextKey("some.recipient.context.key");
        request.setMessageTemplateStatus(MessageTemplateStatus.ACTIVE);
        adminRepository.create(request);
    }

    @Test
    void update() {
    }

    @Test
    void getMessageTemplate() {
    }

    @Test
    void getMessageTemplates() {
    }

    @Test
    void testGetMessageTemplates() {
    }
}