package com.solmod.notification.admin.data;

import com.solmod.notification.engine.domain.NotificationDelivery;
import com.solmod.notification.engine.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
public class NotificationDeliveriesRepositoryTest {

    @Spy
    @InjectMocks
    NotificationDeliveriesRepository repo;

    @Mock
    NamedParameterJdbcTemplate template;

    @Captor
    ArgumentCaptor<String> stringArgCaptor;

    @Test
    void assertBasics() {
        assertNotNull(template);
    }

    @Test
    @DisplayName("Assert that we cannot create a NotificationDelivery and an error is logged if fields are missing")
    void create_MissingFields(CapturedOutput captured) {
        NotificationDelivery request = new NotificationDelivery();
        // request.setMessageTemplateId(345L);
        request.setRecipient("some-recipient");
        request.setStatus(Status.PENDING_PERMISSION);

        // Call
        assertThrows(DBRequestFailureException.class, () -> repo.create(request));

        // Assert
        assertTrue(captured.getOut().contains("NPE"));
        assertTrue(captured.getOut().contains("Failed attempt to save component"));
    }

    @Test
    @DisplayName("Assert no update is performed if there are no changes")
    void update_noChange() {
        NotificationDelivery origFormOfRequest = new NotificationDelivery();
        origFormOfRequest.setId(155L);
        origFormOfRequest.setMessageTemplateId(255L);
        origFormOfRequest.setDeliveryProcessKey("some-uid");
        origFormOfRequest.setRecipient("some-recipient");
        origFormOfRequest.setMessageBodyUri("some-message-body-uri");
        origFormOfRequest.setStatus(Status.INACTIVE);

        NotificationDelivery request = new NotificationDelivery();
        request.setId(155L);
        request.setMessageTemplateId(255L);
        request.setDeliveryProcessKey("some-uid");
        request.setRecipient("some-recipient");
        request.setMessageBodyUri("some-message-body-uri");
        request.setStatus(Status.INACTIVE);

        doReturn(origFormOfRequest).when(repo).getNotificationDelivery(request.getId());

        Set<DataUtils.FieldUpdate> result = repo.update(request);

        assertTrue(result.isEmpty());
        verify(template, never()).update(anyString(), anyMap());
    }

    @Test
    void getNotificationDelivery_byId_exists() {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(NotificationDeliveriesRepository.NotificationDeliveryRowMapper.class)))
                .thenReturn(List.of(new NotificationDelivery()));

        // Test call
        NotificationDelivery result = repo.getNotificationDelivery(testId);

        assertNotNull(result);
        verify(template, times(1)).query(anyString(), anyMap(), any(NotificationDeliveriesRepository.NotificationDeliveryRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a warning log when we get a req by ID which doesn't exist")
    void getNotificationDelivery_byId_notExists(CapturedOutput output) {

        long testId = 15L;
        when(template.query(anyString(), eq(Map.of("id", testId)), any(NotificationDeliveriesRepository.NotificationDeliveryRowMapper.class)))
                .thenReturn(emptyList());

        // Test call
        NotificationDelivery result = repo.getNotificationDelivery(15L);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("no results"));
        verify(template, times(1)).query(anyString(), eq(Map.of("id", testId)), any(NotificationDeliveriesRepository.NotificationDeliveryRowMapper.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Assert we get a null and a log if getMessage(id) is supplied a null id")
    void getNotificationDelivery_byId_nullId(CapturedOutput output) {
        // Test call
        NotificationDelivery result = repo.getNotificationDelivery(null);

        assertNull(result);
        assertTrue(output.getOut().contains("WARN"));
        assertTrue(output.getOut().contains("null id"));
        verify(template, never()).query(anyString(), anyMap(), any(NotificationDeliveriesRepository.NotificationDeliveryRowMapper.class));
    }

    @Test
    @DisplayName("Assert all criteria is considered in SQL statement when supplied. By criteria ignores ID")
    void getNotificationDeliveries_byAllCrit() {
        NotificationDelivery crit = new NotificationDelivery();
        crit.setId(155L);
        crit.setMessageTemplateId(255L);
        crit.setDeliveryProcessKey("some-uid");
        crit.setRecipient("some-recipient");
        crit.setMessageBodyUri("some-message-body-uri");
        crit.setStatus(Status.INACTIVE);

        // Test call
        List<NotificationDelivery> notificationDeliveries = repo.getNotificationDeliveries(crit);

        verify(template, times(1)).query(stringArgCaptor.capture(), anyMap(),
                ArgumentMatchers.<RowMapperResultSetExtractor<NotificationDeliveriesRepository.NotificationDeliveryRowMapper>>any());

        assertFalse(stringArgCaptor.getValue().contains(":id"));
        assertTrue(stringArgCaptor.getValue().contains(":message_template_id"));
        assertTrue(stringArgCaptor.getValue().contains(":delivery_process_key"));
        assertTrue(stringArgCaptor.getValue().contains(":recipient"));
        assertTrue(stringArgCaptor.getValue().contains(":message_body_uri"));
        assertTrue(stringArgCaptor.getValue().contains(":status"));
    }

    @Test
    @DisplayName("Assert a SQL statement contains any non-null criteria supplied in the criteria. By criteria ignores ID")
    void getNotificationDeliveries_byCrit() {
        NotificationDelivery crit = new NotificationDelivery();
        crit.setId(155L);
        crit.setMessageTemplateId(255L);
        crit.setDeliveryProcessKey("some-uid");
//        crit.setRecipient("some-recipient");
//        crit.setMessageBodyUri("some-message-body-uri");
//        crit.setStatus(Status.INACTIVE);

        // Test call
        List<NotificationDelivery> notificationDeliveries = repo.getNotificationDeliveries(crit);

        verify(template, times(1)).query(stringArgCaptor.capture(), anyMap(),
                ArgumentMatchers.<RowMapperResultSetExtractor<NotificationDeliveriesRepository.NotificationDeliveryRowMapper>>any());

        assertFalse(stringArgCaptor.getValue().contains(":id"));
        assertTrue(stringArgCaptor.getValue().contains(":message_template_id"));
        assertTrue(stringArgCaptor.getValue().contains(":delivery_process_key"));
        assertFalse(stringArgCaptor.getValue().contains(":recipient"));
        assertFalse(stringArgCaptor.getValue().contains(":message_body_uri"));
        assertFalse(stringArgCaptor.getValue().contains(":status"));
    }
}
