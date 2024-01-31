package com.solmod.notifications.admin.repository;

import com.solmod.notifications.admin.repository.model.UserDeliveryPreferences;
import com.solmod.notifications.admin.service.TestCommons;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
//@Testcontainers
//@ContextConfiguration(initializers = {NotificationGroupRepoMySQLTest.Initializer.class})
@ActiveProfiles(value = "local")
@Transactional
@RunWith(SpringRunner.class)
class UserDeliveryPreferencesRepoTest extends TestCommons {

    @Autowired
    UserDeliveryPreferencesRepo repo;

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

        UUID userId = UUID.randomUUID();

        UserDeliveryPreferences mockPrefs = buildUserDeliveryPreferences("foo@somewhere.com", "email", 15, userId);
        UserDeliveryPreferences mockPrefs2 = buildUserDeliveryPreferences("+18017911897", "sms", 120, userId);

        repo.save(mockPrefs);
        repo.save(mockPrefs2);

        List<UserDeliveryPreferences> byUserId = repo.findByUserId(userId);

        assertEquals(2, byUserId.size());
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