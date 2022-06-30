package com.solmod.notification.admin.data;


import com.solmod.notification.domain.NotificationContext;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.NotificationContextAlreadyExistsException;
import com.solmod.notification.exception.NotificationContextNonexistentException;
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
public class NotificationContextRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public NotificationContextRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Create a new Notification Context.
     *
     * @param request {@link NotificationContext} representing the request.
     * @throws NotificationContextAlreadyExistsException In the event an existing Notification Context would be duplicated
     */
    public void create(@NotNull final NotificationContext request) throws NotificationContextAlreadyExistsException {
        NotificationContext existing = getNotificationContext(request);
        if (existing != null) {
            throw new NotificationContextAlreadyExistsException(existing,
                    "Cannot create this Notification Context; it would collide with existing Notification Context.");
        }

        String sql = "INSERT INTO notification_contexts " +
                "(tenant_id, event_subject, event_verb, status) " +
                "VALUES (:tenant_id, :event_subject, :event_verb, :status)";
        try {
            template.update(sql, Map.of(
                    "tenant_id", request.getTenantId(),
                    "event_subject", request.getEventSubject(),
                    "event_verb", request.getEventVerb(),
                    "status", request.getStatus().code()
                    ));

            log.info("Notification Context created per request");
        } catch (DataAccessException e) {
            log.warn("DAE: Failed attempt to save notification context: {}\n{}", e.getMessage(), request);
        } catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save notification context with missing fields: {}\n{}", e.getMessage(), request);
        }
    }

    /**
     * Update NotificationContext details
     *
     * @param request {@link NotificationContext} representing the request to make updates to an existing NotificationContext
     */
    public Set<DataUtils.FieldUpdate> update(@NotNull final NotificationContext request)
            throws NotificationContextNonexistentException, NotificationContextAlreadyExistsException {
        log.debug("Updating NotificationContext {}", request.getId());
        NotificationContext origById = getNotificationContext(request.getId());
        if (origById == null) {
            log.warn("Attempt to update a NotificationContext which does not exist: {}. Pretty weird.", request);
            throw new NotificationContextNonexistentException(request, "NotificationContext was not found");
        }

        // If the outcome of the request is an active status, we need to ensure there's !another active template to collide
        if (Optional.ofNullable(request.getStatus()).orElse(origById.getStatus()).equals(Status.ACTIVE)) {
            NotificationContext existing = getNotificationContext(request);
            if (existing != null && existing.getStatus().equals(Status.ACTIVE)) { // getNotificationStatus should ensure active, but just in case...
                // There's been a change in one of the UniqueNotificationContextId fields making it clash with an existing Template
                // Logging is not important, as it doesn't signify an error herein or with the client
                throw new NotificationContextAlreadyExistsException(request, "Can not update Unique ID params for this NotificationContext, one already exists");
            }
        }

        SQLStatementParams statementParams = new SQLStatementParams(request);
        Set<DataUtils.FieldUpdate> fieldUpdates = statementParams.buildForUpdate(origById);

        if (fieldUpdates.isEmpty()) {
            log.info("Update request for NotificationContext where no fields changed. Kinda weird.");
            return Collections.emptySet();
        }

        String sql = "UPDATE notification_contexts SET " +
                statementParams.statement +
                " WHERE id = :id";

        template.update(sql, statementParams.params);

        log.info("Updated {} fields in NotificationContext {}", fieldUpdates.size(), request.getId());
        return fieldUpdates;
    }

    /**
     * Get a NotificationContext by ID
     *
     * @param id {@code Long} ID
     * @return {@link NotificationContext}, or null if a NotificationContext cannot be found with the given ID
     */
    public NotificationContext getNotificationContext(final Long id) {
        if (id == null) {
            log.warn("Request to get message template with null id. That's weird.");
            return null;
        }

        String sql = "select id, tenant_id, event_subject, event_verb, status, modified_date, created_date " +
                "FROM notification_contexts where id = :id";

        List<NotificationContext> results = template.query(sql, Map.of("id", id), new NotificationContextRowMapper());

        if (results.size() != 1) {
            log.warn("Fetch by ID returned no results");
            return null;
        }

        return results.get(0);
    }

    /**
     * Retrieve {@link NotificationContext}s by criteria. ID will be ignored by this method and will return all which
     * qualify, an empty list otherwise.
     * For search which assumes only one NotificationContext should exist per the criteria provided, (e.g. duplicate), see
     * getNotificationContext({@link NotificationContext})
     *
     * @param crit {@link NotificationContext} doubling as criteria for a query. Downside: Can't use null or empty string
     *             as criteria value
     * @return List of matching {@link NotificationContext}s
     */
    @Transactional
    public List<NotificationContext> getNotificationContexts(@NotNull final NotificationContext crit) {

        SQLStatementParams params = new SQLStatementParams(crit);
        params.buildForSelect();
        String sql = "select id, tenant_id, event_subject, event_verb, status, created_date, modified_date \n" +
                "FROM notification_contexts \n" +
                "WHERE " + params.statement;

        return template.query(sql, params.params, new RowMapperResultSetExtractor<>(new NotificationContextRowMapper()));
    }

    /**
     * Get a single NotificationContext, throwing a DataIntegrity error if more than one is found.
     * This is meant to determine if there exists a conflicting NotificationContext
     *
     * @param id {@link NotificationContext} wherein values will be specified which should include all values which,
     *                                      together, represent a NotificationContext which should only occur once
     * @return {@link NotificationContext}, or null in the event of not found or multiple found
     */
    public NotificationContext getNotificationContext(@NotNull final NotificationContext id) {

        NotificationContext uniqueCriteria = new NotificationContext();
        uniqueCriteria.setStatus(Status.ACTIVE); // Unique only counts for active
        uniqueCriteria.setEventSubject(id.getEventSubject());
        uniqueCriteria.setEventVerb(id.getEventVerb());
        uniqueCriteria.setTenantId(id.getTenantId());

        List<NotificationContext> messageTemplates = getNotificationContexts(id);
        if (messageTemplates.size() > 1) {
            log.error("DATA INTEGRITY ERROR: more than one NotificationContext found for {}", id);
            return null;
        }

        return messageTemplates.isEmpty() ? null : messageTemplates.get(0);
    }

    /**
     * Encapsulate statement params building these in queries/updates
     */
    private static class SQLStatementParams {
        NotificationContext notContext;

        /**
         * As the statement in the context of a statement param, this is the statement representing the sub portion of
         * the statement listing the properties (WHERE clause for SELECT, SET clause for UPDATE)
         */
        private String statement;
        private final Map<String, Object> params = new HashMap<>();

        public SQLStatementParams(NotificationContext notContext) {
            this.notContext = notContext;
        }

        /**
         * When {@link NotificationContext} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * where clause for select statement and supporting params
         */
        void buildForSelect() {
            StringBuilder builder = new StringBuilder();
            appendProperty("tenant_id", notContext.getTenantId(), builder);
            appendProperty("event_subject", notContext.getEventSubject(), builder);
            appendProperty("event_verb", notContext.getEventVerb(), builder);
            appendProperty("status", notContext.getStatus(), builder);

            statement = builder.toString();
        }

        /**
         * When {@link NotificationContext} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * this=that portion of an update statement and params
         * Note: Cheat here; that I'm taking this opportunity to build a map of old:new values for updated
         * templates, but we'll need that map for publishing to the bus
         * Note: Herein, the rule disallowing the updating of tenantId is endforced
         *
         * @param originalTemplate {@link NotificationContext} naming the original form of that being updated
         */
        Set<DataUtils.FieldUpdate> buildForUpdate(NotificationContext originalTemplate) {
            Set<DataUtils.FieldUpdate> updates = new HashSet<>();
            StringBuilder builder = new StringBuilder();

            params.put("id", originalTemplate.getId()); // always where clause for an update

            // Disallow updating tenantId

            updates.add(appendProperty("event_subject", originalTemplate.getEventSubject(),
                    notContext.getEventSubject(),
                    builder));
            updates.add(appendProperty("event_verb", originalTemplate.getEventVerb(),
                    notContext.getEventVerb(),
                    builder));
            updates.add(appendProperty("status", notContext.getStatus(),
                    notContext.getStatus(),
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

    public static class NotificationContextRowMapper implements RowMapper<NotificationContext> {
        @Override
        public NotificationContext mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationContext messageTemplate = new NotificationContext();
            messageTemplate.setId(rs.getLong("id"));
            messageTemplate.setEventSubject(rs.getString("event_subject"));
            messageTemplate.setEventVerb(rs.getString("event_verb"));
            messageTemplate.setTenantId(rs.getLong("tenant_id"));
            messageTemplate.setStatus(Status.fromCode(rs.getString("status")));

            Timestamp dateTime = rs.getTimestamp("modified_date");
            if (dateTime != null)
                messageTemplate.setModifiedDate(new DateTime(dateTime.getTime()));
            messageTemplate.setCreatedDate(new DateTime(rs.getTimestamp("created_date").getTime()));
            return messageTemplate;
        }
    }

}
