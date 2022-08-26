package com.solmod.notification.admin.data;

import com.solmod.notification.engine.domain.NotificationTrigger;
import com.solmod.notification.exception.DBRequestFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NotificationTriggerContextRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public NotificationTriggerContextRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Save context relative to a NotificationTrigger
     *
     * @param notificationTriggerId Long identifying the trigger for which this is related content
     * @param context Map of context properties
     */
    public void saveContext(@NotNull final Long notificationTriggerId,
                            @NotNull final Map<String, String> context) throws DBRequestFailureException {

        String sql = "INSERT INTO notification_trigger_context " +
                "(notification_trigger_id, context_key, context_value) " +
                "values(:notification_trigger_id, :context_key, :context_value)";
        try {
//            List<SqlParameterSource> values = new ArrayList<>();

            for (Map.Entry<String, String> contextEntry : context.entrySet()) {
                SqlParameterSource paramSource = new MapSqlParameterSource(Map.of(
                        "notification_trigger_id", notificationTriggerId,
                        "context_key", contextEntry.getKey(),
                        "context_value", contextEntry.getValue()));
                template.update(sql, paramSource);
//                values.add(paramSource);
            }

/*
TODO
            SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(values);
            template.batchUpdate(sql, batch);
*/

            log.info("NotificationTriggerContext saved per request");
        } catch (DataAccessException e) {
            log.error("DAE: Failed attempt to save component: {}", e.getMessage());
            throw new DBRequestFailureException("DB failure creating NotificationTriggerContext: " + e.getMessage());
        }  catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save component with missing fields");
            throw new DBRequestFailureException("DB failure creating NotificationTriggerContext: Content contained null data");
        }
    }

    /**
     * Get the context for a given {@link NotificationTrigger}
     *
     * @param notificationTriggerId {@code Long} Identifying the NotificationTrigger for which to retrieve context
     * @return Map context
     */
    public @NotNull Map<String, String> getNotificationTriggerContext(@NotNull final Long notificationTriggerId) {

        String sql = "select context_key, context_value " +
                "FROM notification_trigger_context where notification_trigger_id = :notificationTriggerId";

        List<Map<String, Object>> results = template.queryForList(sql, Map.of("notificationTriggerId", notificationTriggerId));

        Map<String, String> merged = new HashMap<>();
        for (Map<String, Object> result : results) {
            merged.put(result.get("context_key").toString(), result.get("context_value").toString());
        }

        return merged;
    }
}
