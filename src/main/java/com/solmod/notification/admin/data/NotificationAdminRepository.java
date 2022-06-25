package com.solmod.notification.admin.data;


import com.solmod.notification.domain.ContentLookupType;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.MessageTemplateAlreadyExistsException;
import com.solmod.notification.exception.MessageTemplateNonexistentException;
import org.apache.commons.lang3.StringUtils;
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
public class NotificationAdminRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public NotificationAdminRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Create a new User and grant it the default authorities for the tenant for which it is being provisioned
     * TODO: Prevent duplicates
     *
     * @param request {@link MessageTemplate} representing the request.
     */
    public void create(@NotNull final MessageTemplate request) throws MessageTemplateAlreadyExistsException {
        MessageTemplate existing = getMessageTemplate(request);
        if (existing != null) {
            throw new MessageTemplateAlreadyExistsException(existing,
                    "Cannot create this message template; it would collide with existing Message Template.");
        }

        String sql = "INSERT INTO message_templates " +
                "(tenant_id, event_subject, event_verb, status, recipient_context_key, " +
                "content_lookup_type, content_key) " +
                "VALUES (:tenant_id, :event_subject, :event_verb, :status, " +
                ":recipient_context_key, :content_lookup_type, :content_key)";
        try {
            template.update(sql, Map.of(
                    "tenant_id", request.getTenantId(),
                    "event_subject", request.getEventSubject(),
                    "event_verb", request.getEventVerb(),
                    "status", request.getStatus().code(),
                    "recipient_context_key", request.getRecipientContextKey(),
                    "content_lookup_type", request.getContentLookupType().name(),
                    "content_key", request.getContentKey()
            ));

            log.info("Message template created per request");
        } catch (DataAccessException e) {
            log.warn("DAE: Failed attempt to save message template: {}\n{}", e.getMessage(), request);
        } catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save message template with missing fields: {}\n{}", e.getMessage(), request);
        }
    }

    /**
     * Update MessageTemplate details
     *
     * @param request {@link MessageTemplate} representing the request to make updates to an existing MessageTemplate
     */
    public Set<DataUtils.FieldUpdate> update(@NotNull final MessageTemplate request) throws MessageTemplateNonexistentException, MessageTemplateAlreadyExistsException {
        log.debug("Updating MessageTemplate {}", request.getId());
        MessageTemplate origById = getMessageTemplate(request.getId());
        if (origById == null) {
            log.warn("Attempt to update a MessageTemplate which does not exist: {}. Pretty weird.", request);
            throw new MessageTemplateNonexistentException(request, "MessageTemplate was not found");
        }

        // If the request template is active, we need to ensure there's !another active template to collide
        if (request.getMessageTemplateStatus().equals(Status.ACTIVE)) {
            MessageTemplate existing = getMessageTemplate(request);
            if (existing != null) {
                // There's been a change in one of the UniqueMessageTemplateId fields making it clash with an existing Template
                // Logging is not important, as it doesn't signify an error herein or with the client
                throw new MessageTemplateAlreadyExistsException(request, "Can not update Unique ID params for this MessageTemplate, one already exists");
            }
        }

        SQLStatementParams statementParams = new SQLStatementParams(request);
        Set<DataUtils.FieldUpdate> fieldUpdates = statementParams.buildForUpdate(origById);

        if (fieldUpdates.isEmpty()) {
            log.info("Update request for MessageTemplate where no fields changed. Kinda weird.");
            return Collections.emptySet();
        }

        String sql = "UPDATE message_templates SET " +
                statementParams.statement +
                " WHERE id = :id";

        template.update(sql, statementParams.params);

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

        String sql = "select id, tenant_id, event_subject, event_verb, status, recipient_context_key, " +
                "content_lookup_type, content_key, modified_date, created_date " +
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

        SQLStatementParams params = new SQLStatementParams(crit);
        params.buildForSelect();
        String sql = "select id, tenant_id, event_subject, event_verb, status, recipient_context_key, " +
                "content_lookup_type, content_key, created_date, modified_date \n" +
                "FROM message_templates \n" +
                "WHERE " + params.statement;

        return template.query(sql, params.params, new RowMapperResultSetExtractor<>(new MessageTemplateRowMapper()));
    }

    public MessageTemplate getMessageTemplate(@NotNull final MessageTemplate id) {

        MessageTemplate uniqueCriteria = new MessageTemplate();
        uniqueCriteria.setMessageTemplateStatus(Status.ACTIVE);
        uniqueCriteria.setEventSubject(id.getEventSubject());
        uniqueCriteria.setEventVerb(id.getEventVerb());
        uniqueCriteria.setDeliveryCriteria(id.getDeliveryCriteria());
        uniqueCriteria.setRecipientContextKey(id.getRecipientContextKey());
        uniqueCriteria.setTenantId(id.getTenantId());
        uniqueCriteria.setContentKey(id.getContentKey()); // TODO: at some point, this needs to consider sender strategy

        List<MessageTemplate> messageTemplates = getMessageTemplates(id);
        if (messageTemplates.size() > 1) {
            log.error("DATA INTEGRITY ERROR: more than one MessageTemplate found for {}", id);
            return null;
        }

        return messageTemplates.isEmpty() ? null : messageTemplates.get(0);
    }

    /**
     * Encapsulate statement params building these in queries/updates
     */
    private static class SQLStatementParams {
        MessageTemplate msgTemplate;

        /**
         * As the statement in the context of a statement param, this is the statement representing the sub portion of
         * the statement listing the properties (WHERE clause for SELECT, SET clause for UPDATE)
         */
        private String statement;
        private final Map<String, Object> params = new HashMap<>();

        public SQLStatementParams(MessageTemplate msgTemplate) {
            this.msgTemplate = msgTemplate;
        }

        /**
         * When {@link MessageTemplate} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * where clause for select statement and supporting params
         */
        void buildForSelect() {
            StringBuilder builder = new StringBuilder();
            appendProperty("tenant_id", msgTemplate.getTenantId(), builder);
            appendProperty("event_subject", msgTemplate.getEventSubject(), builder);
            appendProperty("event_verb", msgTemplate.getEventVerb(), builder);
            appendProperty("status", msgTemplate.getStatus().code(), builder);
            appendProperty("recipient_context_key", msgTemplate.getRecipientContextKey(), builder);
            appendProperty("content_lookup_type", msgTemplate.getContentLookupType(), builder);
            appendProperty("content_key", msgTemplate.getContentKey(), builder);

            statement = builder.toString();
        }

        /**
         * When {@link MessageTemplate} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * this=that portion of an update statement and params
         * Note: Cheat here; that I'm taking this opportunity to build a map of old:new values for updated
         * templates, but we'll need that map for publishing to the bus
         * Note: Herein, the rule disallowing the updating of tenantId is endforced
         *
         * @param originalTemplate {@link MessageTemplate} naming the original form of that being updated
         */
        Set<DataUtils.FieldUpdate> buildForUpdate(MessageTemplate originalTemplate) {
            Set<DataUtils.FieldUpdate> updates = new HashSet<>();
            StringBuilder builder = new StringBuilder();

            params.put("id", originalTemplate.getId()); // always where clause for an update

            // Disallow updating tenantId

            updates.add(appendProperty("event_subject", originalTemplate.getEventSubject(),
                    msgTemplate.getEventSubject(),
                    builder));
            updates.add(appendProperty("event_verb", originalTemplate.getEventVerb(),
                    msgTemplate.getEventVerb(),
                    builder));
            updates.add(appendProperty("status", originalTemplate.getStatus().code(),
                    msgTemplate.getStatus().code(),
                    builder));
            updates.add(appendProperty("recipient_context_key", originalTemplate.getRecipientContextKey(),
                    msgTemplate.getRecipientContextKey(),
                    builder));
            updates.add(appendProperty("content_lookup_type", originalTemplate.getContentLookupType(),
                    msgTemplate.getContentLookupType(),
                    builder));
            updates.add(appendProperty("content_key", originalTemplate.getContentKey(),
                    msgTemplate.getContentKey(),
                    builder));

            statement = builder.toString();

            updates.remove(null);
            return updates;
        }

        /**
         * This busy body helper takes old and new values and a field name (matching the DB column name), as well as
         * the statement presumed to be being built. It will return null if there is no difference in values, but will
         * construct a {@link DataUtils.FieldUpdate} and return it
         * Note: The rule that any value has to be replaced with a non-empty value is enforced here. If there is a
         * requirement to blank out a value, that needs to be architected
         *
         * @return DataUtils.FieldUpdate if there is a diff in value. null otherwise
         */
        private DataUtils.FieldUpdate appendProperty(String fieldName, Object originalValue, Object newValue, StringBuilder statement) {
            if (newValue != null && !StringUtils.isBlank(newValue.toString()) &&
                    !Objects.equals(newValue, originalValue)) {
                delimCriteria(statement, ", ").append(fieldName).append("= :").append(fieldName);
                params.put(fieldName, newValue);
                return new DataUtils.FieldUpdate(fieldName, originalValue, newValue);
            }

            return null;
        }

        private void appendProperty(String fieldName, Object value, StringBuilder statement) {
            if (value != null) {
                delimCriteria(statement, " AND ").append(fieldName).append("= :").append(fieldName);
                params.put(fieldName, value);
            }
        }

        private StringBuilder delimCriteria(StringBuilder builder, String delim) {
            if (builder.length() > 0)
                builder.append(delim);

            return builder;
        }
    }

    public  static class MessageTemplateRowMapper implements RowMapper<MessageTemplate> {
        @Override
        public MessageTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            MessageTemplate messageTemplate = new MessageTemplate();
            messageTemplate.setId(rs.getLong("id"));
            messageTemplate.setEventSubject(rs.getString("event_subject"));
            messageTemplate.setEventVerb(rs.getString("event_verb"));
            messageTemplate.setTenantId(rs.getLong("tenant_id"));
            messageTemplate.setMessageTemplateStatus(Status.fromCode(rs.getString("status")));
            messageTemplate.setRecipientContextKey(rs.getString("recipient_context_key"));
            messageTemplate.setContentKey(rs.getString("content_key"));
            messageTemplate.setContentLookupType(ContentLookupType.valueOf(rs.getString("content_lookup_type")));

            Timestamp dateTime = rs.getTimestamp("modified_date");
            if (dateTime != null)
                messageTemplate.setModifiedDate(new DateTime(dateTime.getTime()));
            messageTemplate.setCreatedDate(new DateTime(rs.getTimestamp("created_date").getTime()));
            return messageTemplate;
        }
    }

}
