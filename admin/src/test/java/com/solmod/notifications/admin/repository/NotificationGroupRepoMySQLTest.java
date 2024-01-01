package com.solmod.notifications.admin.repository;

import com.solmod.notifications.admin.repository.model.*;
import org.jetbrains.annotations.NotNull;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@Testcontainers
@ActiveProfiles(value = "local")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
//@ContextConfiguration(initializers = {NotificationGroupRepoMySQLTest.Initializer.class})
@RunWith(SpringRunner.class)
class NotificationGroupRepoMySQLTest {

    @Autowired
    NotificationGroupRepo repo;

    private static UUID resultGroupId;

/*
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
*/

    @Test
    void buildSaveAndGetAll() {
        for (int i = 0; i < 5; i++) {
            NotificationGroup entity = buildBasicNotificationGroup(i);
            Theme testTheme = buildTheme(entity, i);
            entity.setMessageThemes(List.of(testTheme));
            NotificationGroup savedGroup = repo.save(entity);
            resultGroupId = savedGroup.getId();
            assertNotNull(savedGroup.getId());
            assertEquals(entity.getSubject(), savedGroup.getSubject());
            assertNotNull(savedGroup.getMessageThemes());
            assertNotEquals(0, savedGroup.getMessageThemes().size());
        }

        NotificationGroup foundGroup = repo.findById(resultGroupId).orElse(null);
        assertNotNull(foundGroup);
        Collection<Theme> themes = foundGroup.getMessageThemes();
        assertNotNull(themes);

        assertEquals(1, themes.size());
        Theme resultTheme = themes.iterator().next();

        assertEquals(1, resultTheme.getCriteria().size());
        assertEquals(1, resultTheme.getDeliveryRules().size());

        assertTrue(resultTheme.getMessageTemplates().iterator().hasNext());
        MessageTemplate resultTemplate = resultTheme.getMessageTemplates().iterator().next();
        assertNotNull(resultTemplate.getMessageBodyContentKey());
    }

    /**
     * This just shows @Transaction is at entire class level and test data is not persisted outside test
     */
    @Test
    void assertNothing() {
        Iterable<NotificationGroup> all = repo.findAll();
        assertFalse(all.iterator().hasNext());
    }

    @NotNull
    private NotificationGroup buildBasicNotificationGroup(int var) {
        NotificationGroup entity = new NotificationGroup();
        entity.setDescription("This is a test: " + var);
        entity.setSubject(var + "somesubject");
        entity.setVerb(var + "someverb");
        return entity;
    }

    @NotNull
    private Theme buildTheme(NotificationGroup entity, int var) {
        Theme testTheme = new Theme();

        testTheme.setDescription(var + " some description");
        testTheme.setNotificationGroup(entity);
        ThemeCriteria testCriteria = new ThemeCriteria();
        testCriteria.setTheme(testTheme);
        testCriteria.setKey(var + "some-key");
        testCriteria.setValue(var + "some-value");
        testTheme.setCriteria(List.of(testCriteria));
        ThemeDeliveryRules testRules = new ThemeDeliveryRules();
        testRules.setTheme(testTheme);
        testRules.setIntervalPeriod(Calendar.HOUR);
        testRules.setResendInterval(2);
        testTheme.setDeliveryRules(List.of(testRules));
        MessageTemplate testTemplate = new MessageTemplate();
        testTemplate.setTheme(testTheme);
        testTemplate.setMaxRetries(100 + var);
        testTemplate.setMessageBodyContentKey(var + "TheBody");
        testTemplate.setRecipientAddressContextKey(var + "_where_to_find_it");
        testTheme.setMessageTemplates(List.of(testTemplate));

        return testTheme;
    }


/*
    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + mysqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mysqlContainer.getUsername(),
                    "spring.datasource.password=" + mysqlContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
*/
}