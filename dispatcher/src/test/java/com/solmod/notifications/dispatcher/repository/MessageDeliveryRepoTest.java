package com.solmod.notifications.dispatcher.repository;

import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.repository.domain.MessageMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles(value = "local")
@Transactional
//@ContextConfiguration(initializers = {NotificationGroupRepoMySQLTest.Initializer.class})
@RunWith(SpringRunner.class)
class MessageDeliveryRepoTest {

    @Autowired
    MessageDeliveryRepo repo;

    @Test
    @DisplayName("findAllDeliveries - All metadata returned for qualifying deliveries")
    void assertFindAllDeliveries_QualifyingDelivery_ExtraMetadataLoaded() {
        String matchKey = "metadata-key";
        String matchVal = "metadata-value";

        // Arrange
        MessageDelivery mock = new MessageDelivery();
        mock.setDateCreated(new Date());
        mock.setMessageTemplateId(58L);
        mock.setStatus(MessageDelivery.Status.D);
        mock.setRecipientAddress("someone@somewhere.com");
        mock.setMessageMetadata(Set.of(
                new MessageMetadata(mock, matchKey, matchVal),
                new MessageMetadata(mock, "another-key", "another-value")));
        MessageDelivery saved = repo.save(mock);

        // Act
        Collection<MessageDelivery> allDeliveries =
                repo.findAllDeliveries(saved.getMessageTemplateId(), "someone@somewhere.com", matchKey, matchVal);

        // Assert
        assertEquals(1, allDeliveries.size());
        MessageDelivery foundDelivery = allDeliveries.iterator().next();
        assertEquals(2, foundDelivery.getMessageMetadata().size());
        assertEquals(saved.getId(), foundDelivery.getId());
    }

    @Test
    @DisplayName("findAllDeliveries - Deliveries not returned when criteria not met")
    void assertFindAllDeliveries_WrongCriterionValueNotReturned() {
        String matchKey = "metadata-key";
        String matchVal = "metadata-value";

        // Arrange
        MessageDelivery mock = new MessageDelivery();
        mock.setDateCreated(new Date());
        mock.setMessageTemplateId(58L);
        mock.setStatus(MessageDelivery.Status.D);
        mock.setRecipientAddress("someone@somewhere.com");
        mock.setMessageMetadata(Set.of(new MessageMetadata(mock, matchKey, "different-value")));
        MessageDelivery saved = repo.save(mock);

        // Act
        Collection<MessageDelivery> allDeliveries =
                repo.findAllDeliveries(saved.getMessageTemplateId(), "someone@somewhere.com", matchKey, matchVal);

        // Assert
        assertEquals(0, allDeliveries.size());
    }

    @Test
    @DisplayName("findAllDeliveries - Failed deliveries not returned")
    void assertFindAllDeliveries_FailedNotReturned() {
        String matchKey = "metadata-key";
        String matchVal = "metadata-value";

        // Arrange
        MessageDelivery mock = new MessageDelivery();
        mock.setDateCreated(new Date());
        mock.setMessageTemplateId(58L);
        mock.setStatus(MessageDelivery.Status.F);
        mock.setRecipientAddress("someone@somewhere.com");
        mock.setMessageMetadata(Set.of(new MessageMetadata(mock, matchKey, matchVal)));
        MessageDelivery saved = repo.save(mock);

        // Act
        Collection<MessageDelivery> allDeliveries =
                repo.findAllDeliveries(saved.getMessageTemplateId(), "someone@somewhere.com", matchKey, matchVal);

        // Assert
        assertEquals(0, allDeliveries.size());
    }

}