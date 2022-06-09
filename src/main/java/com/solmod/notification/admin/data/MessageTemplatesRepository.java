package com.solmod.notification.admin.data;


import com.solmod.notification.admin.domain.ContentLookupType;
import com.solmod.notification.admin.domain.MessageTemplate;
import com.solmod.notification.admin.domain.MessageTemplateStatus;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class MessageTemplatesRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final NamedParameterJdbcTemplate template;

    public MessageTemplatesRepository(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Create a new User and grant it the default authorities for the tenant for which it is being provisioned
     * TODO: Prevent duplicates
     *
     * @param request {@link MessageTemplate} representing the request.
     */
    @Transactional
    public MessageTemplate create(final MessageTemplate request) {
        List<MessageTemplate> existingTemplates = getMessageTemplates(UniqueMessageTemplateId.from(request));
        if (!existingTemplates.isEmpty()) {
            return existingTemplates.get(0);
        }

        String sql = "INSERT INTO message_templates " +
                "(tenant_id, event_subject, event_verb, status, recipient_context_key, " +
                "summary_content_lookup_type, summary_content_key, body_content_lookup_type, " +
                "body_content_key) " +
                "VALUES (:tenant_id, :event_subject, :event_verb, :status, " +
                ":recipient_context_key, :summary_content_lookup_type, :summary_content_key, " +
                ":body_content_lookup_type, :body_content_key)";
        template.update(sql, Map.of(
                "tenant_id", request.getTenantId(),
                "event_subject", request.getEventSubject(),
                "event_verb", request.getEventVerb(),
                "status", request.getStatus().code(),
                "recipient_context_key", request.getRecipientContextKey(),
                "summary_content_lookup_type", request.getSummaryContentLookupType().name(),
                "summary_content_key", request.getSummaryContentKey(),
                "body_content_lookup_type", request.getBodyContentLookupType().name(),
                "body_content_key", request.getBodyContentKey()
        ));

        existingTemplates = getMessageTemplates(UniqueMessageTemplateId.from(request));
        if (!existingTemplates.isEmpty()) {
            return existingTemplates.get(0);
        }

        return null;
    }

    /**
     * Update MessageTemplate details
     *
     * @param request {@link MessageTemplate} representing the request to make updates to an existing MessageTemplate
     */
    @Transactional
    public Set<DataUtils.FieldUpdate> update(final MessageTemplate request) {
        log.debug("Updating MessageTemplate {}", request.getId());
        MessageTemplate orig = getMessageTemplate(request.getId());

        SQLStatementParams statementParams = new SQLStatementParams(request);
        Set<DataUtils.FieldUpdate> fieldUpdates = statementParams.buildForUpdate(orig);

        if (statementParams.params.isEmpty()) {
            log.debug("No fields changed");
            return Collections.emptySet();
        }

        String sql = "UPDATE message_templates SET " +
                statementParams.statement +
                " WHERE id = :id";

        template.update(sql, statementParams.params);

        log.info("Updated {} fields in MessageTemplate {}", fieldUpdates.size(), request.getId());
        return fieldUpdates;
    }

    @Transactional
    public MessageTemplate getMessageTemplate(Long id) {
        if (id == null)
            return null;

        String sql = "select id, tenant_id, event_subject, event_verb, status, recipient_context_key, " +
                "summary_content_lookup_type, summary_content_key, body_content_lookup_type, body_content_key, \n" +
                "created_date, modified_date " +
                "FROM message_templates where id = :id";

        System.out.println(sql);
        List<MessageTemplate> results = template.query(sql, Map.of("id", id), new MessageTemplateRowMapper());

        if (results.size() != 1) {
            log.warn("Fetch by ID returned no results");
            return null;
        }

        return results.get(0);
    }

    /**
     * Retrieve {@link MessageTemplate}s by criteria
     *
     * @param crit {@link MessageTemplate} doubling as criteria for a query. Downside: Can't use null or empty string
     *             as criteria value
     * @return List of matching {@link MessageTemplate}s
     */
    @Transactional
    public List<MessageTemplate> getMessageTemplates(final MessageTemplate crit) {

        if (crit.getId() != null) {
            MessageTemplate messageTemplate = getMessageTemplate(crit.getId());
            if (messageTemplate != null)
                return List.of(messageTemplate);
            return Collections.emptyList();
        }

        SQLStatementParams params = new SQLStatementParams(crit);
        params.buildForSelect();
        String sql = "select id, tenant_id, event_subject, event_verb, status, recipient_context_key, " +
                "summary_content_lookup_type, summary_content_key, body_content_lookup_type, body_content_key, " +
                "created_date, modified_date \n" +
                "FROM message_templates \n" +
                "WHERE " + params.statement;

        return template.query(sql, params.params, new RowMapperResultSetExtractor<>(new MessageTemplateRowMapper()));
    }


    @Transactional
    public List<MessageTemplate> getMessageTemplates(UniqueMessageTemplateId id) {
        MessageTemplate criteria = new MessageTemplate();
        criteria.setTenantId(id.tenantId);
        criteria.setEventSubject(id.eventSubject);
        criteria.setEventVerb(id.eventVerb);

        return getMessageTemplates(criteria);
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
        private Map<String, Object> params = new HashMap<>();

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
            appendProperty("summary_content_lookup_type", msgTemplate.getSummaryContentLookupType(), builder);
            appendProperty("summary_content_key", msgTemplate.getSummaryContentKey(), builder);
            appendProperty("body_content_lookup_type", msgTemplate.getBodyContentLookupType(), builder);
            appendProperty("body_content_key", msgTemplate.getBodyContentKey(), builder);

            statement = builder.toString();
        }

        /**
         * When {@link MessageTemplate} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * this=that portion of an update statement and params
         * Note: Bit of a cheat here; that I'm taking this opportunity to build a map of old:new values for updated
         * templates, but we'll need that map for publishing to the bus
         *
         * @param originalTemplate {@link MessageTemplate} naming the original form of that being updated
         */
        Set<DataUtils.FieldUpdate> buildForUpdate(MessageTemplate originalTemplate) {
            Set<DataUtils.FieldUpdate> updates = new HashSet<>();
            StringBuilder builder = new StringBuilder();
            String delim = ", ";

            params.put("id", originalTemplate.getId()); // always where clause for an update
            updates.add(appendProperty("tenant_id", originalTemplate.getTenantId(), 
                    msgTemplate.getTenantId(),
                    builder));
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
            updates.add(appendProperty("summary_content_lookup_type", originalTemplate.getSummaryContentLookupType().name(), 
                    msgTemplate.getSummaryContentLookupType().name(),
                    builder));
            updates.add(appendProperty("summary_content_key", originalTemplate.getSummaryContentKey(), 
                    msgTemplate.getSummaryContentKey(),
                    builder));
            updates.add(appendProperty("body_content_lookup_type", originalTemplate.getBodyContentLookupType().name(), 
                    msgTemplate.getBodyContentLookupType().name(),
                    builder));
            updates.add(appendProperty("body_content_key", originalTemplate.getBodyContentKey(), 
                    msgTemplate.getBodyContentKey(),
                    builder));

            statement = builder.toString();

            updates.remove(null);
            return updates;
        }

        /**
         * This busy body helper takes old and new values and a field name (matching the DB column name), as well as
         * the statement presumed to be being built. It will return null if there is no difference in values, but will
         * construct a {@link DataUtils.FieldUpdate} and return it
         * 
         * @return DataUtils.FieldUpdate if there is a diff in value. null otherwise
         */
        private DataUtils.FieldUpdate appendProperty(String fieldName, Object originalValue, Object newValue, StringBuilder statement) {
            if (!Objects.equals(newValue, originalValue)) {
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

        public String getStatement() {
            return statement;
        }

        public void setStatement(String statement) {
            this.statement = statement;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }
    }

    private static class MessageTemplateRowMapper implements RowMapper<MessageTemplate> {
        @Override
        public MessageTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            MessageTemplate messageTemplate = new MessageTemplate();
            messageTemplate.setId(rs.getLong("id"));
            messageTemplate.setEventSubject(rs.getString("event_subject"));
            messageTemplate.setEventVerb(rs.getString("event_verb"));
            messageTemplate.setTenantId(rs.getLong("tenant_id"));
            messageTemplate.setMessageTemplateStatus(MessageTemplateStatus.fromCode(rs.getString("status")));
            messageTemplate.setRecipientContextKey(rs.getString("recipient_context_key"));
            messageTemplate.setBodyContentKey(rs.getString("body_content_key"));
            messageTemplate.setBodyContentLookupType(ContentLookupType.valueOf(rs.getString("body_content_lookup_type")));
            messageTemplate.setSummaryContentKey(rs.getString("summary_content_key"));
            messageTemplate.setSummaryContentLookupType(ContentLookupType.valueOf(rs.getString("summary_content_lookup_type")));

            Timestamp dateTime = rs.getTimestamp("modified_date");
            if (dateTime != null)
                messageTemplate.setModifiedDate(new DateTime(dateTime.getTime()));
            messageTemplate.setCreatedDate(new DateTime(rs.getTimestamp("created_date").getTime()));
            return messageTemplate;
        }
    }

    /**
     * Besides id, this class encapsulates a minimum description of a MessageTemplate which cannot occur more than once
     */
    public static class UniqueMessageTemplateId {
        Long tenantId;
        String eventSubject;
        String eventVerb;

        public UniqueMessageTemplateId(Long tenantId, String eventSubject, String eventVerb) {
            this.tenantId = tenantId;
            this.eventSubject = eventSubject;
            this.eventVerb = eventVerb;
        }

        public static UniqueMessageTemplateId from(MessageTemplate template) {
            return new UniqueMessageTemplateId(template.getTenantId(), template.getEventSubject(), template.getEventVerb());
        }
    }
}
