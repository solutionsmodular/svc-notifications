package com.solmod.notification.admin.data;


import com.solmod.notification.domain.NotificationEvent;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.NotificationEventAlreadyExistsException;
import com.solmod.notification.exception.NotificationEventNonexistentException;
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
public class NotificationEventsRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public NotificationEventsRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Create a new NotificationEvent, which is the top line categorization of the actual messages MessageTemplates.
     * that get delivered.
     *
     * @param request {@link NotificationEvent} representing the request.
     * @throws NotificationEventAlreadyExistsException In the event an existing NotificationEvent would be duplicated
     */
    public void create(@NotNull final NotificationEvent request) throws NotificationEventAlreadyExistsException {
        NotificationEvent existing = getNotificationEvent(request);
        if (existing != null) {
            throw new NotificationEventAlreadyExistsException(existing,
                    "Cannot create this NotificationEvent; it would collide with existing NotificationEvent.");
        }

        String sql = "INSERT INTO notification_events " +
                "(tenant_id, event_subject, event_verb, status) " +
                "VALUES (:tenant_id, :event_subject, :event_verb, :status)";
        try {
            template.update(sql, Map.of(
                    "tenant_id", request.getTenantId(),
                    "event_subject", request.getEventSubject(),
                    "event_verb", request.getEventVerb(),
                    "status", request.getStatus().code()
                    ));

            log.info("NotificationEvent created per request");
        } catch (DataAccessException e) {
            log.warn("DAE: Failed attempt to save NotificationEvent: {}\n{}", e.getMessage(), request);
        } catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save NotificationEvent with missing fields: {}\n{}", e.getMessage(), request);
        }
    }

    /**
     * Update NotificationEvent details
     *
     * @param request {@link NotificationEvent} representing the request to make updates to an existing NotificationEvent
     */
    public Set<DataUtils.FieldUpdate> update(@NotNull final NotificationEvent request)
            throws NotificationEventNonexistentException, NotificationEventAlreadyExistsException {
        log.debug("Updating NotificationEvent {}", request.getId());
        NotificationEvent origById = getNotificationEvent(request.getId());
        if (origById == null) {
            log.warn("Attempt to update a NotificationEvent which does not exist: {}. Pretty weird.", request);
            throw new NotificationEventNonexistentException(request, "NotificationEvent was not found");
        }

        // If the outcome of the request is an active status, we need to ensure there's !another active template to collide
        if (Optional.ofNullable(request.getStatus()).orElse(origById.getStatus()).equals(Status.ACTIVE)) {
            NotificationEvent existing = getNotificationEvent(request);
            if (existing != null && existing.getStatus().equals(Status.ACTIVE)) { // getNotificationStatus should ensure active, but just in case...
                // There's been a change in one of the UniqueNotificationEventId fields making it clash with an existing Template
                // Logging is not important, as it doesn't signify an error herein or with the client
                throw new NotificationEventAlreadyExistsException(request, "Can not update Unique ID params for this NotificationEvent, one already exists");
            }
        }

        SQLStatementParams statementParams = new SQLStatementParams(request);
        Set<DataUtils.FieldUpdate> fieldUpdates = statementParams.buildForUpdate(origById);

        if (fieldUpdates.isEmpty()) {
            log.info("Update request for NotificationEvent where no fields changed. Kinda weird.");
            return Collections.emptySet();
        }

        String sql = "UPDATE notification_events SET " +
                statementParams.statement +
                " WHERE id = :id";

        template.update(sql, statementParams.params);

        log.info("Updated {} fields in NotificationEvent {}", fieldUpdates.size(), request.getId());
        return fieldUpdates;
    }

    /**
     * Get a NotificationEvent by ID
     *
     * @param id {@code Long} ID
     * @return {@link NotificationEvent}, or null if a NotificationEvent cannot be found with the given ID
     */
    public NotificationEvent getNotificationEvent(final Long id) {
        if (id == null) {
            log.warn("Request to get message template with null id. That's weird.");
            return null;
        }

        String sql = "select id, tenant_id, event_subject, event_verb, status, modified_date, created_date " +
                "FROM notification_events where id = :id";

        List<NotificationEvent> results = template.query(sql, Map.of("id", id), new NotificationEventRowMapper());

        if (results.size() != 1) {
            log.warn("Fetch by ID returned no results");
            return null;
        }

        return results.get(0);
    }

    /**
     * Retrieve {@link NotificationEvent}s by criteria. ID will be ignored by this method and will return all which
     * qualify, an empty list otherwise.
     * For search which assumes only one NotificationEvent should exist per the criteria provided, (e.g. duplicate), see
     * getNotificationEvent({@link NotificationEvent})
     *
     * @param crit {@link NotificationEvent} doubling as criteria for a query. Downside: Can't use null or empty string
     *             as criteria value
     * @return List of matching {@link NotificationEvent}s
     */
    @Transactional
    public List<NotificationEvent> getNotificationEvents(@NotNull final NotificationEvent crit) {

        SQLStatementParams params = new SQLStatementParams(crit);
        params.buildForSelect();
        String sql = "select id, tenant_id, event_subject, event_verb, status, created_date, modified_date \n" +
                "FROM notification_events \n" +
                "WHERE " + params.statement;

        return template.query(sql, params.params, new RowMapperResultSetExtractor<>(new NotificationEventRowMapper()));
    }

    /**
     * Get a single NotificationEvent, throwing a DataIntegrity error if more than one is found.
     * This is meant to determine if there exists a conflicting NotificationEvent
     *
     * @param id {@link NotificationEvent} wherein values will be specified which should include all values which,
     *                                      together, represent a NotificationEvent which should only occur once
     * @return {@link NotificationEvent}, or null in the event of not found or multiple found
     */
    public NotificationEvent getNotificationEvent(@NotNull final NotificationEvent id) {

        NotificationEvent uniqueCriteria = new NotificationEvent();
        uniqueCriteria.setStatus(Status.ACTIVE); // Unique only counts for active
        uniqueCriteria.setEventSubject(id.getEventSubject());
        uniqueCriteria.setEventVerb(id.getEventVerb());
        uniqueCriteria.setTenantId(id.getTenantId());

        List<NotificationEvent> messageTemplates = getNotificationEvents(id);
        if (messageTemplates.size() > 1) {
            log.error("DATA INTEGRITY ERROR: more than one NotificationEvent found for {}", id);
            return null;
        }

        return messageTemplates.isEmpty() ? null : messageTemplates.get(0);
    }

    /**
     * Encapsulate statement params building these in queries/updates
     */
    private static class SQLStatementParams {
        NotificationEvent nEvent;

        /**
         * As the statement in the context of a statement param, this is the statement representing the sub portion of
         * the statement listing the properties (WHERE clause for SELECT, SET clause for UPDATE)
         */
        private String statement;
        private final Map<String, Object> params = new HashMap<>();

        public SQLStatementParams(NotificationEvent nEvent) {
            this.nEvent = nEvent;
        }

        /**
         * When {@link NotificationEvent} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * where clause for select statement and supporting params
         */
        void buildForSelect() {
            StringBuilder builder = new StringBuilder();
            appendProperty("tenant_id", nEvent.getTenantId(), builder);
            appendProperty("event_subject", nEvent.getEventSubject(), builder);
            appendProperty("event_verb", nEvent.getEventVerb(), builder);
            appendProperty("status", nEvent.getStatus(), builder);

            statement = builder.toString();
        }

        /**
         * When {@link NotificationEvent} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * this=that portion of an update statement and params
         * Note: Cheat here; that I'm taking this opportunity to build a map of old:new values for updated
         * templates, but we'll need that map for publishing to the bus
         * Note: Herein, the rule disallowing the updating of tenantId is endforced
         *
         * @param originalTemplate {@link NotificationEvent} naming the original form of that being updated
         */
        Set<DataUtils.FieldUpdate> buildForUpdate(NotificationEvent originalTemplate) {
            Set<DataUtils.FieldUpdate> updates = new HashSet<>();
            StringBuilder builder = new StringBuilder();

            params.put("id", originalTemplate.getId()); // always where clause for an update

            // Disallow updating tenantId

            updates.add(appendProperty("event_subject", originalTemplate.getEventSubject(),
                    nEvent.getEventSubject(),
                    builder));
            updates.add(appendProperty("event_verb", originalTemplate.getEventVerb(),
                    nEvent.getEventVerb(),
                    builder));
            updates.add(appendProperty("status", nEvent.getStatus(),
                    nEvent.getStatus(),
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

    public static class NotificationEventRowMapper implements RowMapper<NotificationEvent> {
        @Override
        public NotificationEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationEvent messageTemplate = new NotificationEvent();
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