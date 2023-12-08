package com.solmod.notification.admin.data;

import com.solmod.notification.domain.Status;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Encapsulate statement params building these in queries/updates
 */
class SQLUpdateStatementParams {

    /**
     * As the statement in the context of a statement param, this is the statement representing the sub portion of
     * the statement listing the properties (WHERE clause for SELECT, SET clause for UPDATE)
     */
    private final Set<DataUtils.FieldUpdate> updates = new HashSet<>();
    private final StringBuilder statement = new StringBuilder();
    private final Map<String, Object> params = new HashMap<>();

    public SQLUpdateStatementParams(Long id) {
        params.put("id", id); // always where clause for an update
    }

    /**
     * This helper will build the where clause and package the interpreted params needed for those.
     * Using this build, this instance can be used to glean this=that portion of an update statement and params
     * Note: Cheat here; that I'm taking this opportunity to build a map of old:new values for updated
     * templates, but we'll need that map for publishing to the bus
     *
     * @param fieldName   String naming the DB field corresponding
     * @param originalVal Original value being updated
     * @param newVal      New value being set
     */
    public void addField(String fieldName, Object originalVal, Object newVal) {
        DataUtils.FieldUpdate update = appendProperty(fieldName, originalVal, newVal, statement);
        if (update != null)
            updates.add(update);
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

        // Lame workaround, https://solutionsmodular.atlassian.net/browse/NE-39
        if (newValue != null && !StringUtils.isBlank(newValue.toString()) &&
                !Objects.equals(newValue, originalValue)) {
            delimCriteria(statement).append(fieldName).append("= :").append(fieldName);
            if (newValue instanceof Status) {
                params.put(fieldName, ((Status)newValue).code());
            } else {
                params.put(fieldName, newValue);
            }

            return new DataUtils.FieldUpdate(fieldName, originalValue, newValue);
        }

        return null;
    }

    private StringBuilder delimCriteria(StringBuilder builder) {
        if (builder.length() > 0)
            builder.append(", ");

        return builder;
    }

    public String getStatement() {
        return statement.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Set<DataUtils.FieldUpdate> getUpdates() {
        return updates;
    }
}
