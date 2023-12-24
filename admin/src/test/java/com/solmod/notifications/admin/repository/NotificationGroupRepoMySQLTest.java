package com.solmod.notifications.admin.repository;

import com.solmod.notifications.admin.repository.model.EventCriteria;
import com.solmod.notifications.admin.repository.model.MessageTheme;
import com.solmod.notifications.admin.repository.model.NotificationGroup;
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

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
//@ActiveProfiles(value = "local")
    @Transactional
class NotificationGroupRepoMySQLTest {

    @Autowired
    NotificationGroupRepo repo;

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.2.0")
            .withDatabaseName("testdb")
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
    void testBasic() {
        NotificationGroup entity = new NotificationGroup();
        entity.setDescription("This is a test");
        entity.setSubject("somesubject");
        entity.setVerb("someverb");
        MessageTheme testTheme = new MessageTheme();
        EventCriteria testCriteria = new EventCriteria();
        testCriteria.setKey("some-key");
        testCriteria.setValue("some-value");
        testTheme.setCriteria(List.of(testCriteria));
        entity.setMessageThemes(List.of(testTheme));

        NotificationGroup saved = repo.save(entity);
        assertNotNull(saved.getId());
        assertEquals("somesubject", saved.getSubject());
        assertNotNull(saved.getMessageThemes());
        assertNotEquals(0, saved.getMessageThemes().size());
        System.out.println(saved.getId());

        Iterable<NotificationGroup> all = repo.findAll();
        Collection<MessageTheme> messageThemes = all.iterator().next().getMessageThemes();
        assertNotNull(messageThemes);

    }

    @Test
    void assertNothing() {
        Iterable<NotificationGroup> all = repo.findAll();
        assertFalse(all.iterator().hasNext());
    }
}