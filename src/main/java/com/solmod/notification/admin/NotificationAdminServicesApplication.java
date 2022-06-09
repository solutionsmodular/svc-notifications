package com.solmod.notification.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.solmod.notification.admin.domain.ContentLookupType;
import com.solmod.notification.admin.domain.MessageTemplateStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NotificationAdminServicesApplication {

    Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        SpringApplication.run(NotificationAdminServicesApplication.class, args);
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValueAsString(ContentLookupType.class);
            mapper.writeValueAsString(MessageTemplateStatus.class);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.registerModule(new JodaModule());

            return mapper;
        } catch (JsonProcessingException e) {
            logger.error("ERROR Setting up object mapper, behavior will not be as expected {}", e.getMessage());
            return null;
        }
    }

}
