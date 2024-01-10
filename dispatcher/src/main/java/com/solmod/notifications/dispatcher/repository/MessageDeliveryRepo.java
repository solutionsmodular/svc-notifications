package com.solmod.notifications.dispatcher.repository;

import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageDeliveryRepo extends CrudRepository<MessageDelivery, Long> {
}
