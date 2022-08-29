package com.solmod.notification.admin.data;


import com.solmod.notification.domain.MessageSender;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.DataCollisionException;
import com.solmod.notification.exception.ExpectedNotFoundException;
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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
     * Create a new {@link MessageTemplate}
     *
     * @param request {@link MessageTemplate} representing the request.
     * @return Long indicating the ID of the newly added MessageTemplate
     */
    public Long create(@NotNull final MessageTemplate request)
            throws DataCollisionException, DBRequestFailureException {

        MessageTemplate existing = getMessageTemplate(request);
        if (existing != null) {
            throw new DataCollisionException("MessageTemplate", existing.getId());
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        String sql = "INSERT INTO message_templates " +
                "(message_config_id, status, recipient_context_key, message_sender, content_key) " +
                "VALUES (:message_config_id, :status, " +
                ":recipient_context_key, :message_sender, :content_key)";
        SqlParameterSource paramSource = new MapSqlParameterSource(Map.of(
                "message_config_id", request.getMessageConfigId(),
                "status", request.getStatus().code(),
                "message_sender", request.getMessageSender().name(),
                "recipient_context_key", request.getRecipientContextKey(),
                "content_key", request.getContentKey()));

        try {
            template.update(sql, paramSource, keyHolder);

            log.info("MessageTemplate created per request");
            Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
            request.setId(id);
            return id;
        } catch (DataAccessException e) {
            log.error("DAE: Failed attempt to save component: {}\n{}", e.getMessage(), request);
            throw new DBRequestFailureException("DB failure creating MessageTemplate: " + e.getMessage());
        } catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save component with missing fields\n    {}", request);
            throw new DBRequestFailureException("DB failure creating MessageTemplate: " + request);
        }
    }

    /**
     * Update MessageTemplate details
     *
     * @param request {@link MessageTemplate} representing the request to make updates to an existing MessageTemplate
     */
    public Set<DataUtils.FieldUpdate> update(@NotNull final MessageTemplate request)
            throws DataCollisionException, ExpectedNotFoundException {
        log.debug("Updating MessageTemplate {}", request.getId());
        MessageTemplate origById = getMessageTemplate(request.getId());
        if (origById == null) {
            log.warn("Attempt to update a MessageTemplate which does not exist: {}. Pretty weird.", request);
            throw new ExpectedNotFoundException("MessageTemplate", request.getId());
        }

        // If the outcome of the request is an active status, we need to ensure there's !another active template to collide
        if (Optional.ofNullable(request.getStatus()).orElse(origById.getStatus()).equals(Status.ACTIVE)) {
            MessageTemplate existing = getMessageTemplate(request);
            if (existing != null && existing.getId() != request.getId() && existing.getStatus().equals(Status.ACTIVE)) {// getMessageTemplate should ensure active, but just in case...
                // There's been a change in one of the UniqueMessageTemplateId fields making it clash with an existing Template
                // Logging is not important, as it doesn't signify an error herein or with the client
                throw new DataCollisionException("MessageTemplate", request.getId());
            }
        }

        SQLUpdateStatementParams statementParams = new SQLUpdateStatementParams(request.getId());
        statementParams.addField("status", origById.getStatus(), request.getStatus());
        statementParams.addField("recipient_context_key", origById.getRecipientContextKey(), request.getRecipientContextKey());
        statementParams.addField("message_sender", origById.getMessageSender(), request.getMessageSender());
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
     * @return {@link MessageTemplate}, or null if a MessageTemplate cannot be found with the given ID
     */
    public MessageTemplate getMessageTemplate(final Long id) {
        if (id == null) {
            log.warn("Request to get message template with null id. That's weird.");
            return null;
        }

        String sql = "select id, message_config_id, status, recipient_context_key, " +
                "message_sender, content_key, modified_date, created_date " +
                "FROM message_templates where id = :id";

        List<MessageTemplate> results = template.query(sql, Map.of("id", id), new MessageTemplateRowMapper());

        if (results.size() != 1) {
            log.warn("Fetch by ID returned no results");
            return null;
        }

        return results.get(0);
    }

    /**
     * Retrieve {@link MessageTemplate}s by criteria. ID will be ignored by this method and will return all which
     * qualify, an empty list otherwise.
     * For search which assumes only one MessageTemplate should exist per the criteria provided, (e.g. duplicate), see
     * getMessageTemplate({@link MessageTemplate})
     *
     * @param crit {@link MessageTemplate} doubling as criteria for a query. Downside: Can't use null or empty string
     *             as criteria value
     * @return List of matching {@link MessageTemplate}s
     */
    @Transactional
    public List<MessageTemplate> getMessageTemplates(@NotNull final MessageTemplate crit) {

        SQLSelectStatementParams params = new SQLSelectStatementParams();
        params.addField("status", crit.getStatus());
        params.addField("message_config_id", crit.getMessageConfigId());
        params.addField("recipient_context_key", crit.getRecipientContextKey());
        params.addField("message_sender", crit.getMessageSender());
        params.addField("content_key", crit.getContentKey());

        String sql = "select id, message_config_id, status, recipient_context_key, " +
                "message_sender, content_key, created_date, modified_date \n" +
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
     * @param id {@link MessageTemplate} wherein values will be specified which should include all values which,
     *           together, represent a MessageTemplate which should only occur once
     * @return {@link MessageTemplate}, or null in the event of not found or multiple found
     */
    public MessageTemplate getMessageTemplate(@NotNull final MessageTemplate id) {

        MessageTemplate uniqueCriteria = new MessageTemplate();
        uniqueCriteria.setStatus(Status.ACTIVE);
        uniqueCriteria.setMessageConfigId(id.getMessageConfigId());
        uniqueCriteria.setRecipientContextKey(id.getRecipientContextKey());
        uniqueCriteria.setContentKey(id.getContentKey());

        List<MessageTemplate> messageTemplates = getMessageTemplates(id);
        if (messageTemplates.size() > 1) {
            log.error("DATA INTEGRITY ERROR: more than one MessageTemplate found for {}", id);
            return null;
        }

        return messageTemplates.isEmpty() ? null : messageTemplates.get(0);
    }

    public static class MessageTemplateRowMapper implements RowMapper<MessageTemplate> {
        @Override
        public MessageTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            MessageTemplate messageTemplate = new MessageTemplate();
            messageTemplate.setId(rs.getLong("id"));
            messageTemplate.setMessageConfigId(rs.getLong("message_config_id"));
            messageTemplate.setStatus(Status.fromCode(rs.getString("status")));
            messageTemplate.setRecipientContextKey(rs.getString("recipient_context_key"));
            messageTemplate.setContentKey(rs.getString("content_key"));
            messageTemplate.setMessageSender(MessageSender.valueOf(rs.getString("message_sender")));

            Timestamp dateTime = rs.getTimestamp("modified_date");
            if (dateTime != null)
                messageTemplate.setModifiedDate(new DateTime(dateTime.getTime()));
            messageTemplate.setCreatedDate(new DateTime(rs.getTimestamp("created_date").getTime()));
            return messageTemplate;
        }
    }

}
