package com.solmod.notification.engine.data;

import com.solmod.notification.admin.data.MessageTemplateSearchCriteria;
import com.solmod.notification.domain.ContentLookupType;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.MessageTemplateStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class NotificationEngineRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final NamedParameterJdbcTemplate template;

    public NotificationEngineRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Retrieve {@link MessageTemplate}s by criteria
     *
     * @param crit {@link MessageTemplate} doubling as criteria for a query. Downside: Can't use null or empty string
     *             as criteria value
     * @return List of matching {@link MessageTemplate}s
     */
    @Transactional
    public Collection<MessageTemplate> getMessageTemplates(final MessageTemplateSearchCriteria crit) {

        SQLStatementParams params = new SQLStatementParams(crit);
        params.buildForSelect();
        String sql = "select mt.id, mt.tenant_id, mt.event_subject, mt.event_verb, status, mt.recipient_context_key, " +
                "mt.content_lookup_type, mt.content_key, mt.created_date, mt.modified_date, \n" +
                "dc.context_key, dc.value \n" +
                "FROM message_templates mt \n" +
                "LEFT JOIN delivery_criteria dc on dc.message_template_id = mt.id " +
                "WHERE " + params.statement;

        List<MessageTemplate> matchingTemplates = template.query(sql, params.params, new RowMapperResultSetExtractor<>(new MessageTemplateRowMapper()));

        Collection<MessageTemplate> sortedTemplates = mergeMessageTemplateResults(matchingTemplates);

        return sortedTemplates == null ? Collections.emptyList() : sortedTemplates;
    }

    private Collection<MessageTemplate> mergeMessageTemplateResults(List<MessageTemplate> matchingTemplates) {
        if (matchingTemplates != null && !matchingTemplates.isEmpty()) {
            Map<Long, MessageTemplate> sortedTemplates = new HashMap<>();
            for (MessageTemplate curTemplate : matchingTemplates) {
                Map<String, Object> deliveryCriteria = sortedTemplates.putIfAbsent(curTemplate.getId(), curTemplate).getDeliveryCriteria();
                deliveryCriteria.putAll(curTemplate.getDeliveryCriteria());
            }

            // Match up criteria
            return sortedTemplates.values();
        }

        return null;
    }


    /**
     * Encapsulate statement params building these in queries/updates
     */
    private static class SQLStatementParams {
        MessageTemplateSearchCriteria msgTemplate;

        /**
         * As the statement in the context of a statement param, this is the statement representing the sub portion of
         * the statement listing the properties (WHERE clause for SELECT, SET clause for UPDATE)
         */
        private String statement;
        private Map<String, Object> params = new HashMap<>();

        public SQLStatementParams(MessageTemplateSearchCriteria msgTemplate) {
            this.msgTemplate = msgTemplate;
        }

        /**
         * When {@link MessageTemplate} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * where clause for select statement and supporting params
         */
        private void buildForSelect() {
            StringBuilder builder = new StringBuilder();
            appendProperty("tenant_id", msgTemplate.getTenantId(), builder);
            appendProperty("event_subject", msgTemplate.getEventSubject(), builder);
            appendProperty("event_verb", msgTemplate.getEventVerb(), builder);
            appendProperty("status", MessageTemplateStatus.ACTIVE.code(), builder);

            statement = builder.toString();
        }

        private void appendProperty(String fieldName, Object value, StringBuilder statement) {
            if (value != null) {
                delimCriteria(statement).append(fieldName).append("= :").append(fieldName);
                params.put(fieldName, value);
            }
        }

        private StringBuilder delimCriteria(StringBuilder builder) {
            if (builder.length() > 0)
                builder.append(" AND ");
            
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

    public static class MessageTemplateRowMapper implements RowMapper<MessageTemplate> {

        @Override
        public MessageTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            long templateId = rs.getLong("id");

            MessageTemplate messageTemplate = new MessageTemplate();

            messageTemplate.setId(templateId);
            messageTemplate.setTenantId(rs.getLong("tenant_id"));
            messageTemplate.setEventSubject(rs.getString("event_subject"));
            messageTemplate.setEventVerb(rs.getString("event_verb"));
            messageTemplate.setRecipientContextKey(rs.getString("recipient_context_key"));
            messageTemplate.setContentKey(rs.getString("content_key"));
            messageTemplate.setContentLookupType(ContentLookupType.valueOf(rs.getString("content_lookup_type")));

            return messageTemplate;
        }
    }
}
