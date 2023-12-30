package com.solmod.notifications.admin.repository;

import com.solmod.notifications.admin.repository.model.NotificationGroup;
import com.solmod.notifications.admin.repository.model.Theme;
import com.solmod.notifications.admin.repository.model.ThemeCriteria;
import com.solmod.notifications.admin.repository.model.ThemeDeliveryRules;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles(value = "local")
class NotificationGroupRepoMySQLTest {

    @Autowired
    NotificationGroupRepo repo;

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.2.0")
            .withDatabaseName("admin_db")
            .withUsername("testuser")
            .withPassword("testpass");

    @TestConfiguration
    static class TestConfig {
        @Bean
        public String dataSourceUrl() {
            return mysqlContainer.getJdbcUrl();
        }
    }

    @Test
    void bigSaveBigGet() {
        NotificationGroup entity = buildBasicNotificationGroup();
        Theme testTheme = buildTheme();
        entity.setMessageThemes(List.of(testTheme));

        NotificationGroup savedGroup = repo.save(entity);
        assertNotNull(savedGroup.getId());
        assertEquals("somesubject", savedGroup.getSubject());
        assertNotNull(savedGroup.getMessageThemes());
        assertNotEquals(0, savedGroup.getMessageThemes().size());

        System.out.println(savedGroup.getId());

        NotificationGroup foundGroup = repo.findById(savedGroup.getId()).orElse(null);
        assertNotNull(foundGroup);
        Collection<Theme> themes = foundGroup.getMessageThemes();
        assertNotNull(themes);

        assertEquals(1, themes.size());
        Theme resultTheme = themes.iterator().next();

        assertEquals(1, resultTheme.getCriteria().size());
        assertEquals(1, resultTheme.getDeliveryRules().size());
    }

    @NotNull
    private NotificationGroup buildBasicNotificationGroup() {
        NotificationGroup entity = new NotificationGroup();
        entity.setDescription("This is a test");
        entity.setSubject("somesubject");
        entity.setVerb("someverb");
        return entity;
    }

    @NotNull
    private Theme buildTheme() {
        Theme testTheme = new Theme();
        ThemeCriteria testCriteria = new ThemeCriteria();
        testCriteria.setKey("some-key");
        testCriteria.setValue("some-value");
        testTheme.setCriteria(List.of(testCriteria));
        ThemeDeliveryRules testRules = new ThemeDeliveryRules();
        testRules.setIntervalPeriod(Calendar.HOUR);
        testRules.setResendInterval(2);
        testTheme.setDeliveryRules(List.of(testRules));

        return testTheme;
    }


    @Test
    void assertNothing() {
        Iterable<NotificationGroup> all = repo.findAll();
        assertFalse(all.iterator().hasNext());
    }
}