package com.solmod.notification.admin.data;


import com.solmod.notification.domain.MessageConfig;
import com.solmod.notification.domain.MessageSender;
import com.solmod.notification.domain.Status;
import com.solmod.notification.domain.summary.MessageTemplateSummary;
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
    public Long create(@NotNull final MessageConfig request) throws DataCollisionException {
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

        template.update(sql, paramSource, keyHolder);

        log.info("MessageConfig created per request");
        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        request.setId(id);
        return id;
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

        String sql = "SELECT mc.id as message_config_id, mc.notification_event_id, name, mc.status, mc.modified_date, mc.created_date, \n" +
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

        String sql = "SELECT mc.id id, notification_event_id, name, mc.status mc_status, mc.created_date mc_created_date, \n" +
                "mc.modified_date mc_modified_date,\n" +
                "mt.id mt_id, mt.recipient_context_key, mt.message_sender, mt.content_key, mt.status mt_status\n" +
                "FROM message_configs mc\n" +
                "LEFT JOIN message_templates mt on mt.message_config_id = mc.id \n" +
                "WHERE " + params.getStatement() +
                " ORDER BY mc.id";

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

        /**
         * Note: Use the statement to determine eager or lazy
         *
         * @param rs ResultSet
         * @return List of {@link MessageConfig} where MessageConfigs contain {@link MessageTemplateSummary}s if the
         * SQL statement provides values:
         *
         * <ul>
         * <li>mt_id - Message Template ID</li>
         * <li>content_key</li>
         * <li>message_sender</li>
         * <li>recipient_context_key</li>
         * <li>mt_status</li>
         * </ul>
         * @throws SQLException If there's a problem with the code
         * @throws DataAccessException If there's a problem with the data or request
         */
        @Override
        public List<MessageConfig> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<MessageConfig> messageConfigs = new LinkedList<>();

            MessageConfig current = null;
            while (rs.next()) {
                Long currentId = rs.getLong("id");
                if (current == null || !Objects.equals(currentId, current.getId())) {
                    current = new MessageConfig();
                    current.setId(currentId);
                    current.setName(rs.getString("name"));
                    current.setNotificationEventId(rs.getLong("notification_event_id"));
                    current.setStatus(Status.fromCode(rs.getString("mc_status")));
                    current.loadByResultSet(rs);

                    messageConfigs.add(current);
                }

                Long msgTemplateId = rs.getLong("mt_id");
                if (msgTemplateId > 0) {
                    MessageTemplateSummary mt = MessageTemplateSummary.builder()
                            .id(msgTemplateId)
                            .messageConfigId(currentId)
                            .contentKey(rs.getString("content_key"))
                            .messageSender(MessageSender.valueOf(rs.getString("message_sender")))
                            .recipientContextKey(rs.getString("recipient_context_key"))
                            .status(Status.fromCode(rs.getString("mt_status")))
                            .build();
                    current.addMessageTemplate(mt);
                }
            }

            return messageConfigs;
        }
    }
}
