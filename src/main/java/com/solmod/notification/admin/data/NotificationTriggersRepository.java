package com.solmod.notification.admin.data;


import com.solmod.notification.domain.NotificationTrigger;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.NotificationTriggerNonexistentException;
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
     * NOTE: This method will update the given request with the resulting ID
     *
     * @param request {@link NotificationTrigger} representing the request.
     */
    public Long create(@NotNull final NotificationTrigger request) throws DBRequestFailureException {
        String sql = "INSERT INTO notification_triggers " +
                "(notification_event_id, uid, status) " +
                "values(:notification_event_id, :uid, :status)";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            SqlParameterSource paramSource = new MapSqlParameterSource(Map.of(
                    "notification_event_id", request.getNotificationEventId(),
                    "uid", request.getUid(),
                    "status", request.getStatus().code()));

            template.update(sql, paramSource, keyHolder);

            log.info("NotificationTrigger created per request");
            Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
            request.setId(id);
            return id;
        } catch (DataAccessException e) {
            log.error("DAE: Failed attempt to save component: {}\n{}", e.getMessage(), request);
            throw new DBRequestFailureException("DB failure creating NotificationTrigger: " + e.getMessage());
        }  catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save component with missing fields\n    {}", request);
            throw new DBRequestFailureException("DB failure creating NotificationTrigger: " + request);
        }
    }

    /**
     * Update NotificationTrigger details
     *
     * @param request {@link NotificationTrigger} representing the request to make updates to an existing
     *                NotificationTrigger
     */
    public Set<DataUtils.FieldUpdate> update(@NotNull final NotificationTrigger request)
            throws NotificationTriggerNonexistentException {
        log.debug("Updating NotificationTrigger {}", request.getId());
        NotificationTrigger origById = getNotificationTrigger(request.getId());
        if (origById == null) {
            log.warn("Attempt to update a NotificationTrigger which does not exist: {}. Pretty weird.", request);
            throw new NotificationTriggerNonexistentException(request, "NotificationTrigger was not found");
        }

        SQLUpdateStatementParams statementParams = new SQLUpdateStatementParams(request.getId());
        statementParams.addField("notification_event_id", origById.getNotificationEventId(), request.getNotificationEventId());
        statementParams.addField("uid", origById.getUid(), request.getUid());
        statementParams.addField("status", origById.getStatus(), request.getStatus());

        Set<DataUtils.FieldUpdate> fieldUpdates = statementParams.getUpdates();

        if (fieldUpdates.isEmpty()) {
            log.info("Update request for NotificationTrigger where no fields changed. Kinda weird.");
            return Collections.emptySet();
        }

        String sql = "UPDATE notification_triggers SET " +
                statementParams.getStatement() +
                " WHERE id = :id";

        template.update(sql, statementParams.getParams());

        log.info("Updated {} fields in NotificationTrigger {}", fieldUpdates.size(), request.getId());
        return fieldUpdates;
    }

    /**
     * Get a NotificationTrigger by ID
     *
     * @param id {@code Long} ID
     * @return {@link NotificationTrigger}, or null if a NotificationTrigger cannot be found with the given ID
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
     * @param criteria {@link NotificationTrigger} doubling as criteria for a query. Downside: Can't use null or empty string
     *                 as criteria value
     * @return List of matching {@link NotificationTrigger}s
     */
    public List<NotificationTrigger> getNotificationTriggers(@NotNull final NotificationTrigger criteria) {

        SQLSelectStatementParams params = new SQLSelectStatementParams();
        params.addField("notification_event_id", criteria.getNotificationEventId());
        params.addField("uid", criteria.getUid());
        params.addField("status", criteria.getStatus());

        String sql = "select id, notification_event_id, uid, status, created_date \n" +
                "FROM notification_triggers \n" +
                "WHERE " + params.getStatement();

        return template.query(sql, params.getParams(), new RowMapperResultSetExtractor<>(new NotificationTriggerRowMapper()));
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
