package com.solmod.notification.admin.data;


import com.solmod.notification.domain.NotificationTrigger;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.NotificationTriggerNonexistentException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class NotificationTriggersRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public NotificationTriggersRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Create a new NotificationTrigger, which is an instance of receipt of an event with a subject/verb for which there
     * is a NotificationEvent.
     *
     * @param request {@link NotificationTrigger} representing the request.
     */
    public void create(@NotNull final NotificationTrigger request) throws DBRequestFailureException {
        String sql = "INSERT INTO notification_triggers " +
                "(notification_event_id, uid, status) " +
                "values(:notification_event_id, :uid, :status)";
        try {
            template.update(sql, Map.of(
                    "notification_event_id", request.getNotificationEventId(),
                    "uid", request.getUid(),
                    "status", request.getStatus().code()
                    ));

            log.info("NotificationTrigger created per request");
        } catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save NotificationTrigger with missing fields: {}\n{}", e.getMessage(), request);
            throw new DBRequestFailureException("DB failure creating NotificationTrigger");
        }
    }

    /**
     * Update NotificationTrigger details
     *
     * @param request {@link NotificationTrigger} representing the request to make updates to an existing
     *                                           NotificationTrigger
     */
    public Set<DataUtils.FieldUpdate> update(@NotNull final NotificationTrigger request)
            throws NotificationTriggerNonexistentException {
        log.debug("Updating NotificationTrigger {}", request.getId());
        NotificationTrigger origById = getNotificationTrigger(request.getId());
        if (origById == null) {
            log.warn("Attempt to update a NotificationTrigger which does not exist: {}. Pretty weird.", request);
            throw new NotificationTriggerNonexistentException(request, "NotificationTrigger was not found");
        }

        SQLStatementParams statementParams = new SQLStatementParams(request);
        Set<DataUtils.FieldUpdate> fieldUpdates = statementParams.buildForUpdate(origById);

        if (fieldUpdates.isEmpty()) {
            log.info("Update request for NotificationTrigger where no fields changed. Kinda weird.");
            return Collections.emptySet();
        }

        String sql = "UPDATE notification_triggers SET " +
                statementParams.statement +
                " WHERE id = :id";

        template.update(sql, statementParams.params);

        log.info("Updated {} fields in NotificationTemplate {}", fieldUpdates.size(), request.getId());
        return fieldUpdates;
    }

    /**
     * Get a NotificationTemplate by ID
     *
     * @param id {@code Long} ID
     * @return {@link NotificationTrigger}, or null if a NotificationTemplate cannot be found with the given ID
     */
    public NotificationTrigger getNotificationTrigger(final Long id) {
        if (id == null) {
            log.warn("Request to get NotificationTrigger with null id. That's weird.");
            return null;
        }

        String sql = "select id, notification_event_id, uid, status, created_date " +
                "FROM notification_triggers where id = :id";

        List<NotificationTrigger> results = template.query(sql, Map.of("id", id), new NotificationTriggerRowMapper());

        switch (results.size()) {
            case 0:
                log.warn("Fetch NotificationTrigger by ID returned no results");
                return null;
            case 1:
                return results.get(0);
            default:
                log.error("DATA INTEGRITY ERROR: Multiple triggers found with the same ID");
                return null;
        }
    }

    /**
     * Retrieve {@link NotificationTrigger}s by criteria. ID will be ignored by this method and will return all which
     * qualify, an empty list otherwise.
     *
     * @param crit {@link NotificationTrigger} doubling as criteria for a query. Downside: Can't use null or empty string
     *             as criteria value
     * @return List of matching {@link NotificationTrigger}s
     */
    public List<NotificationTrigger> getNotificationTriggers(@NotNull final NotificationTrigger crit) {

        SQLStatementParams params = new SQLStatementParams(crit);
        params.buildForSelect();
        String sql = "select id, notification_event_id, uid, status, created_date \n" +
                "FROM notification_triggers \n" +
                "WHERE " + params.statement;

        return template.query(sql, params.params, new RowMapperResultSetExtractor<>(new NotificationTriggerRowMapper()));
    }


    /**
     * Encapsulate statement params building these in queries/updates
     */
    private static class SQLStatementParams {
        NotificationTrigger nTrigger;

        /**
         * As the statement in the context of a statement param, this is the statement representing the sub portion of
         * the statement listing the properties (WHERE clause for SELECT, SET clause for UPDATE)
         */
        private String statement;
        private final Map<String, Object> params = new HashMap<>();

        public SQLStatementParams(NotificationTrigger nTrigger) {
            this.nTrigger = nTrigger;
        }

        /**
         * When {@link NotificationTrigger} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * where clause for select statement and supporting params
         */
        void buildForSelect() {
            StringBuilder builder = new StringBuilder();
            appendProperty("notification_event_id", nTrigger.getNotificationEventId(), builder);
            appendProperty("uid", nTrigger.getUid(), builder);
            appendProperty("status", nTrigger.getStatus(), builder);

            statement = builder.toString();
        }

        /**
         * When {@link NotificationTrigger} is used as a criteria for a statement, this helper will build the where clause
         * and package the interpreted params needed for those. Using this build, this instance can be used to glean
         * this=that portion of an update statement and params
         * Note: Cheat here; that I'm taking this opportunity to build a map of old:new values for updated
         * templates, but we'll need that map for publishing to the bus
         * Note: Herein, the rule disallowing the updating of tenantId is endforced
         *
         * @param originalTemplate {@link NotificationTrigger} naming the original form of that being updated
         */
        Set<DataUtils.FieldUpdate> buildForUpdate(NotificationTrigger originalTemplate) {
            Set<DataUtils.FieldUpdate> updates = new HashSet<>();
            StringBuilder builder = new StringBuilder();

            params.put("id", originalTemplate.getId()); // always where clause for an update

            // Disallow updating tenantId
            updates.add(appendProperty("notification_event_id", originalTemplate.getNotificationEventId(),
                    nTrigger.getNotificationEventId(),
                    builder));
            updates.add(appendProperty("uid", originalTemplate.getUid(),
                    nTrigger.getUid(),
                    builder));
            updates.add(appendProperty("status", nTrigger.getStatus(),
                    nTrigger.getStatus(),
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

    public static class NotificationTriggerRowMapper implements RowMapper<NotificationTrigger> {
        @Override
        public NotificationTrigger mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationTrigger messageTemplate = new NotificationTrigger();
            messageTemplate.setId(rs.getLong("id"));
            messageTemplate.setNotificationEventId(rs.getLong("notification_event_id"));
            messageTemplate.setUid(rs.getString("uid"));
            messageTemplate.setStatus(Status.fromCode(rs.getString("status")));
            messageTemplate.setCreatedDate(new DateTime(rs.getTimestamp("created_date").getTime()));
            return messageTemplate;
        }
    }

}
