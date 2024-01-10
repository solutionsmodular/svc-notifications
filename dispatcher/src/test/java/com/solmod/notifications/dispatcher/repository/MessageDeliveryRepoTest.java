package com.solmod.notifications.dispatcher.repository;

import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.repository.domain.MessageMetadata;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles(value = "local")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
//@ContextConfiguration(initializers = {NotificationGroupRepoMySQLTest.Initializer.class})
@RunWith(SpringRunner.class)
class MessageDeliveryRepoTest {

    @Autowired
    MessageDeliveryRepo repo;

    @Test
    void assertSaveAndLoad() {
        MessageDelivery mock = new MessageDelivery();
        mock.setDateCreated(DateTime.now(DateTimeZone.UTC));
        mock.setMessageTemplateId(58L);
        MessageMetadata metadata = new MessageMetadata();
        metadata.setMessageDelivery(mock);
        metadata.setMetadataKey("metadata-key");
        metadata.setMetadataValue("metadata-value");
        mock.setMessageMetadata(Set.of(metadata));
        MessageDelivery saved = repo.save(mock);

        MessageDelivery found = repo.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(mock.getMessageTemplateId(), found.getMessageTemplateId());
        assertEquals(1, found.getMessageMetadata().size());

    }

}