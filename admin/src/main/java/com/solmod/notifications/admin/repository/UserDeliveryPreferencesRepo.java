package com.solmod.notifications.admin.repository;

import com.solmod.notifications.admin.repository.model.UserDeliveryPreferences;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserDeliveryPreferencesRepo extends CrudRepository<UserDeliveryPreferences, UUID> {

    List<UserDeliveryPreferences> findByUserId(@NotNull UUID userId);

    /**
     * User preferences will be unique for a given recipient addy and sender
     *
     * @param recipientAddress {@code String}
     * @param sender {@code String} naming the send protocol for which to retrieve recipient's preferences
     * @return {@link UserDeliveryPreferences}
     */
    UserDeliveryPreferences findByRecipientAddressAndSender(@NotNull String recipientAddress, @NotNull String sender);
}
