package com.solmod.notification.admin.data;


import com.solmod.notification.domain.*;
import com.solmod.notification.engine.domain.MessageContentPurpose;
import com.solmod.notification.engine.domain.MessageConfig;
import com.solmod.notification.engine.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.MessageTemplateAlreadyExistsException;
import com.solmod.notification.exception.MessageTemplateNonexistentException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class MessageTemplatesRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public MessageTemplatesRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Create a new MessageTemplate
     *
     * @param request {@link MessageConfig} representing the request.
     */
    public void create(@NotNull final MessageConfig request) throws MessageTemplateAlreadyExistsException, DBRequestFailureException {
        MessageConfig existing = getMessageTemplate(request);
        if (existing != null) {
            throw new MessageTemplateAlreadyExistsException(existing,
                    "Cannot create this message template; it would collide with existing Message Template.");
        }

        String sql = "INSERT INTO message_templates " +
                "(notification_event_id, status, recipient_context_key, " +
                "message_content_purpose, content_key) " +
                "VALUES (:notification_event_id, :status, " +
                ":recipient_context_key, :message_content_purpose, :content_key)";
        try {
            template.update(sql, Map.of(
                    "notification_event_id", request.getNotificationEventId(),
                    "status", request.getStatus().code(),
                    "recipient_context_key", request.getRecipientContextKey(),
                    "message_content_purpose", request.getMessageContentPurpose().name(),
                    "content_key", request.getContentKey()
            ));

            log.info("Message template created per request");
        } catch (DataAccessException e) {
            log.error("DAE: Failed attempt to save component: {}\n{}", e.getMessage(), request);
            throw new DBRequestFailureException("DB failure creating MessageTemplate: " + e.getMessage());
        }  catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save component with missing fields\n    {}", request);
            throw new DBRequestFailureException("DB failure creating MessageTemplate: " + request);
        }
    }

    /**
     * Update MessageTemplate details
     *
     * @param request {@link MessageConfig} representing the request to make updates to an existing MessageTemplate
     */
    public Set<DataUtils.FieldUpdate> update(@NotNull final MessageConfig request) throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        log.debug("Updating MessageTemplate {}", request.getId());
        MessageConfig origById = getMessageTemplate(request.getId());
        if (origById == null) {
            log.warn("Attempt to update a MessageTemplate which does not exist: {}. Pretty weird.", request);
            throw new MessageTemplateNonexistentException(request, "MessageTemplate was not found");
        }

        // If the outcome of the request is an active status, we need to ensure there's !another active template to collide
        if (Optional.ofNullable(request.getStatus()).orElse(origById.getStatus()).equals(Status.ACTIVE)) {
            MessageConfig existing = getMessageTemplate(request);
            if (existing != null && existing.getStatus().equals(Status.ACTIVE)) {// getMessageTemplate should ensure active, but just in case...
                // There's been a change in one of the UniqueMessageTemplateId fields making it clash with an existing Template
                // Logging is not important, as it doesn't signify an error herein or with the client
                throw new MessageTemplateAlreadyExistsException(request, "Can not update Unique ID params for this MessageTemplate, one already exists");
            }
        }

        SQLUpdateStatementParams statementParams = new SQLUpdateStatementParams(request.getId());
        statementParams.addField("status", origById.getStatus(), request.getStatus());
        statementParams.addField("recipient_context_key", origById.getRecipientContextKey(), request.getRecipientContextKey());
        statementParams.addField("message_content_purpose", origById.getMessageContentPurpose(), request.getMessageContentPurpose());
        statementParams.addField("content_key", origById.getContentKey(), request.getContentKey());
        Set<DataUtils.FieldUpdate> fieldUpdates = statementParams.getUpdates();

        if (fieldUpdates.isEmpty()) {
            log.info("Update request for MessageTemplate where no fields changed. Kinda weird.");
            return Collections.emptySet();
        }

        String sql = "UPDATE message_templates SET " +
                statementParams.getStatement() +
                " WHERE id = :id";

        template.update(sql, statementParams.getParams());

        log.info("Updated {} fields in MessageTemplate {}", fieldUpdates.size(), request.getId());
        return fieldUpdates;
    }

    /**
     * Get a MessageTemplate by ID
     *
     * @param id {@code Long} ID
     * @return {@link MessageConfig}, or null if a MessageTemplate cannot be found with the given ID
     */
    public MessageConfig getMessageTemplate(final Long id) {
        if (id == null) {
            log.warn("Request to get message template with null id. That's weird.");
            return null;
        }

        String sql = "select id, notification_event_id, status, recipient_context_key, " +
                "message_content_purpose, content_key, modified_date, created_date " +
                "FROM message_templates where id = :id";

        List<MessageConfig> results = template.query(sql, Map.of("id", id), new MessageTemplateRowMapper());

        if (results.size() != 1) {
            log.warn("Fetch by ID returned no results");
            return null;
        }

        return results.get(0);
    }

    /**
     * Retrieve {@link MessageConfig}s by criteria. ID will be ignored by this method and will return all which
     * qualify, an empty list otherwise.
     * For search which assumes only one MessageTemplate should exist per the criteria provided, (e.g. duplicate), see
     * getMessageTemplate({@link MessageConfig})
     *
     * @param crit {@link MessageConfig} doubling as criteria for a query. Downside: Can't use null or empty string
     *             as criteria value
     * @return List of matching {@link MessageConfig}s
     */
    @Transactional
    public List<MessageConfig> getMessageTemplates(@NotNull final MessageConfig crit) {

        SQLSelectStatementParams params = new SQLSelectStatementParams();
        params.addField("status", crit.getStatus());
        params.addField("notification_event_id", crit.getNotificationEventId());
        params.addField("recipient_context_key", crit.getRecipientContextKey());
        params.addField("message_content_purpose", crit.getMessageContentPurpose());
        params.addField("content_key", crit.getContentKey());

        String sql = "select id, notification_event_id, status, recipient_context_key, " +
                "message_content_purpose, content_key, created_date, modified_date \n" +
                "FROM message_templates \n" +
                "WHERE " + params.getStatement();

        return template.query(sql, params.getParams(), new RowMapperResultSetExtractor<>(new MessageTemplateRowMapper()));
    }

    /**
     * Get a single MessageTemplate, throwing a DataIntegrity error if more than one is found.
     * This is meant to determine if there exists a conflicting MessageTemplate
     * NOTE: Only non-blank values will be used as criteria. Any value for which there is a blank, then, will result
     * in tha field not being filtered, which may result in multiple templates mistakenly
     *
     * @param id {@link MessageConfig} wherein values will be specified which should include all values which,
     *           together, represent a MessageTemplate which should only occur once
     * @return {@link MessageConfig}, or null in the event of not found or multiple found
     */
    public MessageConfig getMessageTemplate(@NotNull final MessageConfig id) {

        MessageConfig uniqueCriteria = new MessageConfig();
        uniqueCriteria.setStatus(Status.ACTIVE);
        uniqueCriteria.setDeliveryCriteria(id.getDeliveryCriteria());
        uniqueCriteria.setRecipientContextKey(id.getRecipientContextKey());
        uniqueCriteria.setNotificationEventId(id.getNotificationEventId());
        uniqueCriteria.setContentKey(id.getContentKey());

        List<MessageConfig> messageConfigs = getMessageTemplates(id);
        if (messageConfigs.size() > 1) {
            log.error("DATA INTEGRITY ERROR: more than one MessageTemplate found for {}", id);
            return null;
        }

        return messageConfigs.isEmpty() ? null : messageConfigs.get(0);
    }

    public static class MessageTemplateRowMapper implements RowMapper<MessageConfig> {
        @Override
        public MessageConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
            MessageConfig messageConfig = new MessageConfig();
            messageConfig.setId(rs.getLong("id"));
            messageConfig.setNotificationEventId(rs.getLong("notification_event_id"));
            messageConfig.setStatus(Status.fromCode(rs.getString("status")));
            messageConfig.setRecipientContextKey(rs.getString("recipient_context_key"));
            messageConfig.setContentKey(rs.getString("content_key"));
            messageConfig.setMessageContentPurpose(MessageContentPurpose.valueOf(rs.getString("message_content_purpose")));

            Timestamp dateTime = rs.getTimestamp("modified_date");
            if (dateTime != null)
                messageConfig.setModifiedDate(new DateTime(dateTime.getTime()));
            messageConfig.setCreatedDate(new DateTime(rs.getTimestamp("created_date").getTime()));
            return messageConfig;
        }
    }

}
