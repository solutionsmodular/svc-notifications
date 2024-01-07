package com.solmod.notifications.dispatcher.config;

import com.solmod.notifications.dispatcher.filter.MessageDeliveryFilter;
import com.solmod.notifications.dispatcher.filter.MessageDeliveryRulesFilter;
import com.solmod.notifications.dispatcher.filter.ThemeCriteriaFilter;
import com.solmod.notifications.dispatcher.filter.UserPreferencesFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class MessageFilterConfig {

    @Bean
    Set<MessageDeliveryFilter> deliveryFilters() {
        HashSet<MessageDeliveryFilter> messageDeliveryFilters = new HashSet<>();
        messageDeliveryFilters.add(new ThemeCriteriaFilter());
        messageDeliveryFilters.add(new MessageDeliveryRulesFilter());
        messageDeliveryFilters.add(new UserPreferencesFilter());
        return messageDeliveryFilters;
    }
}
