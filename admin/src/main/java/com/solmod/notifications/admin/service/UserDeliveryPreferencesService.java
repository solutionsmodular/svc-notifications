package com.solmod.notifications.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solmod.notifications.admin.repository.UserDeliveryPreferencesRepo;
import com.solmod.notifications.admin.repository.model.UserDeliveryPreferences;
import com.solmod.notifications.admin.web.model.UserDeliveryPreferencesDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserDeliveryPreferencesService {

    UserDeliveryPreferencesRepo repo;
    ObjectMapper mapper;

    @Autowired
    public UserDeliveryPreferencesService(UserDeliveryPreferencesRepo repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public List<UserDeliveryPreferencesDTO> getUserDeliveryPreferences(@NotNull UUID userId) {
        List<UserDeliveryPreferences> preferences = repo.findByUserId(userId);

        return preferences.stream().map(t -> mapper.convertValue(t, UserDeliveryPreferencesDTO.class)).collect(Collectors.toList());
    }

    @Transactional
    public UserDeliveryPreferencesDTO getDeliveryPreferences(@NotNull String recipient, @NotNull String sender) {
        UserDeliveryPreferences preferences = repo.findByRecipientAddressAndSender(recipient, sender);

        return mapper.convertValue(preferences, UserDeliveryPreferencesDTO.class);
    }
}

