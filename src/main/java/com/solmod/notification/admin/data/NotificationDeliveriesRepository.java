package com.solmod.notification.admin.data;


import com.solmod.notification.engine.domain.NotificationDelivery;
import com.solmod.notification.engine.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class NotificationDeliveriesRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public NotificationDeliveriesRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Create a new NotificationDelivery, which is an instance of a notification info package for delivery to a
     * determined receipt.
     *
     * @param request {@link NotificationDelivery} representing the request.
     */
    public void create(@NotNull final NotificationDelivery request) throws DBRequestFailureException {
        boolean hasDeliveryProcessKey = StringUtils.isNotBlank(request.getDeliveryProcessKey());
        boolean hasMessageBodyUri = StringUtils.isNotBlank(request.getMessageBodyUri());

        String sql = String.format("INSERT INTO notification_deliveries " +
                "(message_template_id, recipient, status%s%s) " +
                "values(:message_template_id, :recipient, :status%s%s)",
                hasDeliveryProcessKey ? ", delivery_process_key" : "",
                hasMessageBodyUri ? ", message_body_uri" : "",
                hasDeliveryProcessKey ? ", :delivery_process_key" : "",
                hasMessageBodyUri ? ", :message_body_uri" : ""
                );

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("message_template_id", request.getMessageTemplateId().toString()); // cause a NPE
            params.put("recipient", request.getRecipient().toString()); // cause a NPE
            params.put("status", request.getStatus().code()); // cause a NPE
            if (hasMessageBodyUri) {
                params.put("message_body_uri", request.getMessageBodyUri());
            }
            if (hasDeliveryProcessKey) {
                params.put("delivery_process_key", request.getDeliveryProcessKey());
            }

            SqlParameterSource paramSource = new MapSqlParameterSource(params);

            template.update(sql, paramSource);

            log.info("NotificationDelivery created per request");
        } catch (DataAccessException e) {
            log.error("DAE: Failed attempt to save component: {}\n{}", e.getMessage(), request);
            throw new DBRequestFailureException("DB failure creating NotificationDelivery: " + e.getMessage());
        }  catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save component with missing fields\n    {}", request);
            throw new DBRequestFailureException("DB failure creating NotificationDelivery: " + request);
        }
    }

    /**
     * Update NotificationDelivery details
     *
     * @param request {@link NotificationDelivery} representing the request to make updates to an existing
     *                NotificationDelivery
     */
    public Set<DataUtils.FieldUpdate> update(@NotNull final NotificationDelivery request) {
        log.debug("Updating NotificationDelivery {}", request.getId());
        NotificationDelivery origById = getNotificationDelivery(request.getId());

        SQLUpdateStatementParams statementParams = new SQLUpdateStatementParams(request.getId());
        statementParams.addField("status", origById.getStatus(), request.getStatus());

        Set<DataUtils.FieldUpdate> fieldUpdates = statementParams.getUpdates();

        if (fieldUpdates.isEmpty()) {
            log.info("Update request for NotificationDelivery where no fields changed. Kinda weird.");
            return Collections.emptySet();
        }

        String sql = "UPDATE notification_deliveries SET " +
                statementParams.getStatement() +
                " WHERE id = :id";

        template.update(sql, statementParams.getParams());

        log.info("Updated {} fields in NotificationDelivery {}", fieldUpdates.size(), request.getId());
        return fieldUpdates;
    }

    /**
     * Get a NotificationDelivery by ID
     *
     * @param id {@code Long} ID
     * @return {@link NotificationDelivery}, or null if a NotificationDelivery cannot be found with the given ID
     */
    public NotificationDelivery getNotificationDelivery(final Long id) {
        if (id == null) {
            log.warn("Request to get NotificationDelivery with null id. That's weird.");
            return null;
        }

        String sql = "select id, message_template_id, message_body_uri, delivery_process_key, recipient, status, created_date, modified_date " +
                "FROM notification_deliveries where id = :id";

        List<NotificationDelivery> results = template.query(sql, Map.of("id", id), new NotificationDeliveryRowMapper());

        switch (results.size()) {
            case 0:
                log.warn("Fetch NotificationDelivery by ID returned no results");
                return null;
            case 1:
                return results.get(0);
            default:
                log.error("DATA INTEGRITY ERROR: Multiple triggers found with the same ID");
                return null;
        }
    }

    /**
     * Retrieve {@link NotificationDelivery}s by criteria. ID will be ignored by this method and will return all which
     * qualify, an empty list otherwise.
     *
     * @param criteria {@link NotificationDelivery} doubling as criteria for a query. Downside: Can't use null or empty string
     *                 as criteria value
     * @return List of matching {@link NotificationDelivery}s
     */
    public List<NotificationDelivery> getNotificationDeliveries(@NotNull final NotificationDelivery criteria) {

        SQLSelectStatementParams params = new SQLSelectStatementParams();
        params.addField("message_template_id", criteria.getMessageTemplateId());
        params.addField("delivery_process_key", criteria.getDeliveryProcessKey());
        params.addField("recipient", criteria.getRecipient());
        params.addField("message_body_uri", criteria.getMessageBodyUri());
        params.addField("status", criteria.getStatus());

        String sql = "select id, message_template_id, message_body_uri, delivery_process_key, recipient, status, created_date, modified_date \n" +
                "FROM notification_deliveries \n" +
                "WHERE " + params.getStatement();

        return template.query(sql, params.getParams(), new RowMapperResultSetExtractor<>(new NotificationDeliveryRowMapper()));
    }

    public static class NotificationDeliveryRowMapper implements RowMapper<NotificationDelivery> {
        @Override
        public NotificationDelivery mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationDelivery messageTemplate = new NotificationDelivery();
            messageTemplate.setId(rs.getLong("id"));
            messageTemplate.setMessageTemplateId(rs.getLong("message_template_id"));
            messageTemplate.setRecipient(rs.getString("recipient"));
            messageTemplate.setMessageBodyUri(rs.getString("message_body_uri"));
            messageTemplate.setDeliveryProcessKey(rs.getString("delivery_process_key"));
            messageTemplate.setStatus(Status.fromCode(rs.getString("status")));
            messageTemplate.setCreatedDate(new DateTime(rs.getTimestamp("created_date").getTime()));

            if (rs.getTimestamp("modified_date") != null)
                messageTemplate.setModifiedDate(new DateTime(rs.getTimestamp("modified_date").getTime()));

            return messageTemplate;
        }
    }

}
