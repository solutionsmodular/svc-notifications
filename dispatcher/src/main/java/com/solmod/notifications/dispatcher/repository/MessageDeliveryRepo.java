package com.solmod.notifications.dispatcher.repository;

import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageDeliveryRepo extends CrudRepository<MessageDelivery, Long> {

    @Query("SELECT d from MessageDeliveries d " +
            "LEFT JOIN MessageMetadata m on m.messageDelivery = d " +
            "where m.metadataKey = :metadataKey and m.metadataValue = :metadataValue " +
            "and d.messageTemplateId = :templateId " +
            "and d.status <> 'F' " +
            "order by d.dateCompleted DESC, d.dateCreated DESC ")
    List<MessageDelivery> findAllDeliveries(Long templateId, String metadataKey, String metadataValue);
}
