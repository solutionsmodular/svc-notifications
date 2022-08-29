package com.solmod.notification.admin.data;


import com.solmod.notification.domain.MessageConfig;
import com.solmod.notification.domain.MessageSender;
import com.solmod.notification.domain.MessageTemplate;
import com.solmod.notification.domain.Status;
import com.solmod.notification.exception.DBRequestFailureException;
import com.solmod.notification.exception.DataCollisionException;
import com.solmod.notification.exception.ExpectedNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
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
import java.util.*;

@Repository
public class MessageConfigsRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public MessageConfigsRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Create a new MessageConfig
     *
     * @param request {@link MessageConfig} representing the request.
     */
    public Long create(@NotNull final MessageConfig request) throws DataCollisionException, DBRequestFailureException {
        MessageConfig existing = getMessageConfig(request);
        if (existing != null) {
            throw new DataCollisionException("MessageConfig", existing.getId());
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO message_configs " +
                "(notification_event_id, name, status) " +
                "VALUES (:notification_event_id, :name, :status)";

        SqlParameterSource paramSource = new MapSqlParameterSource(Map.of(
                "notification_event_id", request.getNotificationEventId(),
                "status", request.getStatus().code(),
                "name", request.getName()));

        try {
            template.update(sql, paramSource, keyHolder);

            log.info("MessageConfig created per request");
            Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
            request.setId(id);
            return id;
        } catch (DataAccessException e) {
            log.error("DAE: Failed attempt to save component: {}\n{}", e.getMessage(), request);
            throw new DBRequestFailureException("DB failure creating MessageConfig: " + e.getMessage());
        }  catch (NullPointerException e) {
            log.warn("NPE: Failed attempt to save component with missing fields\n    {}", request);
            throw new DBRequestFailureException("DB failure creating MessageConfig: " + request);
        }
    }

    /**
     * Update MessageConfig details
     *
     * @param request {@link MessageConfig} representing the request to make updates to an existing MessageConfig
     */
    public Set<DataUtils.FieldUpdate> update(@NotNull final MessageConfig request)
            throws ExpectedNotFoundException, DataCollisionException {

        log.debug("Updating MessageConfig {}", request.getId());
        MessageConfig origById = getMessageConfig(request.getId());
        if (origById == null) {
            log.warn("Attempt to update a MessageConfig which does not exist: {}. Pretty weird.", request);
            throw new ExpectedNotFoundException("MessageConfig", request.getId());
        }

        // If the outcome of the request is an active status, we need to ensure there's !another active config to collide
        if (Optional.ofNullable(request.getStatus()).orElse(origById.getStatus()).equals(Status.ACTIVE)) {
            MessageConfig existing = getMessageConfig(request);
            if (existing != null && !Objects.equals(existing.getId(), request.getId()) && existing.getStatus().equals(Status.ACTIVE)) {// getMessageConfig should ensure active, but just in case...
                // There's been a change in one of the uniqueness rules fields making it clash with an existing Config
                // Logging is not important, as it doesn't signify an error herein or with the client
                throw new DataCollisionException("MessageConfig", existing.getId());
            }
        }

        SQLUpdateStatementParams statementParams = new SQLUpdateStatementParams(request.getId());
        statementParams.addField("status", origById.getStatus(), request.getStatus());
        statementParams.addField("name", origById.getName(), request.getName());
        Set<DataUtils.FieldUpdate> fieldUpdates = statementParams.getUpdates();

        if (fieldUpdates.isEmpty()) {
            log.info("Update request for MessageConfig where no fields changed. Kinda weird.");
            return Collections.emptySet();
        }

        String sql = "UPDATE message_configs SET " +
                statementParams.getStatement() +
                " WHERE id = :id";

        template.update(sql, statementParams.getParams());

        log.info("Updated {} fields in MessageConfig {}", fieldUpdates.size(), request.getId());
        return fieldUpdates;
    }

    /**
     * Get a MessageConfig by ID
     *
     * @param id {@code Long} ID
     * @return {@link MessageConfig}, or null if a MessageConfig cannot be found with the given ID
     */
    public MessageConfig getMessageConfig(final Long id) {
        if (id == null) {
            log.warn("Request to get message config with null id. That's weird.");
            return null;
        }

        String sql = "SELECT mc.id message_config_id, mc.notification_event_id, name, mc.status, mc.modified_date, mc.created_date, \n" +
                "mt.id message_template_id, mt.recipient_context_key, mt.message_sender, mt.content_key \n" +
                "FROM message_configs mc \n" +
                "LEFT JOIN message_templates mt on mt.message_config_id = mc.id " +
                "where mt.id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource(Map.of("id", id));
        List<MessageConfig> results = template.query(sql, params, new MessageConfigResultSetExtractor());

        if (results == null || results.isEmpty()) {
            log.warn("Fetch by ID returned no results");
            return null;
        }

        return results.get(0);
    }

    /**
     * Retrieve {@link MessageConfig}s by criteria. ID will be ignored by this method and will return all which
     * qualify per other attributes, an empty list otherwise.
     * For search which assumes only one MessageConfig should exist per the criteria provided, (e.g. duplicate), see
     * getMessageConfig({@link MessageConfig})
     *
     * @param crit {@link MessageConfig} doubling as criteria for a query. Downside: Can't use null or empty string
     *             as criteria value
     * @return List of matching {@link MessageConfig}s
     */
    @Transactional
    public List<MessageConfig> getMessageConfigs(@NotNull final MessageConfig crit) { // TODO: This should take indiv properties as criteria

        SQLSelectStatementParams params = new SQLSelectStatementParams();
        params.addField("status", crit.getStatus());
        params.addField("name", crit.getName());
        params.addField("notification_event_id", crit.getNotificationEventId());

        String sql = "select id, notification_event_id, name, status, created_date, modified_date \n" +
                "FROM message_configs \n" +
                "WHERE " + params.getStatement();

        return template.query(sql, params.getParams(), new MessageConfigResultSetExtractor());
    }

    /**
     * Get a single MessageConfig, throwing a DataIntegrity error if more than one is found.
     * This is meant to determine if there exists a conflicting MessageConfig
     * NOTE: Only non-blank values will be used as criteria. Any value for which there is a blank, then, will result
     * in tha field not being filtered, which may result in multiple configs mistakenly
     *
     * @param id {@link MessageConfig} wherein values will be specified which should include all values which,
     *           together, represent a MessageConfig which should only occur once
     * @return {@link MessageConfig}, or null in the event of not found or multiple found
     */
    public MessageConfig getMessageConfig(@NotNull final MessageConfig id) {

        MessageConfig uniqueCriteria = new MessageConfig();
        uniqueCriteria.setStatus(Status.ACTIVE);
        uniqueCriteria.setName(id.getName()); // TODO: what is a unique config? Has to do with criteria.......
        uniqueCriteria.setDeliveryCriteria(id.getDeliveryCriteria());
        uniqueCriteria.setNotificationEventId(id.getNotificationEventId());

        List<MessageConfig> messageConfigs = getMessageConfigs(uniqueCriteria);
        if (messageConfigs.size() > 1) {
            log.error("DATA INTEGRITY ERROR: more than one MessageConfig found for {}", id);
            return null;
        }

        return messageConfigs.isEmpty() ? null : messageConfigs.get(0);
    }

    public static class MessageConfigResultSetExtractor implements ResultSetExtractor<List<MessageConfig>> {

        @Override
        public List<MessageConfig> extractData(ResultSet rs) throws SQLException, DataAccessException {
            HashMap<Long, MessageConfig> builderMap = new HashMap<>();

            while (rs.next()) {
                long currentId = rs.getLong("message_config_id");
                MessageConfig current = builderMap.get(currentId);
                if (current == null) {
                    current = new MessageConfig();
                    current.setId(currentId);
                    current.setName(rs.getString("name"));
                    current.setNotificationEventId(rs.getLong("notification_event_id"));
                    current.setStatus(Status.valueOf(rs.getString("status")));
                    current.loadByResultSet(rs);
                    builderMap.put(currentId, current);
                }


                long templateId = rs.getLong("notification_template_id");
                if (templateId == 0L) {
                    MessageTemplate messageTemplate = new MessageTemplate();
                    messageTemplate.setId(rs.getLong("message_template_id"));
                    messageTemplate.setContentKey("content_key");
                    messageTemplate.setRecipientContextKey("recipient_context_key");
                    messageTemplate.setMessageConfigId(currentId);
                    messageTemplate.setMessageSender(MessageSender.valueOf(rs.getString("message_sender")));
                    messageTemplate.setStatus(Status.fromCode(rs.getString("status")));
                    current.addMessageTemplate(messageTemplate);
                }
            }

            return new ArrayList<>(builderMap.values());
        }
    }
}
