package com.solmod.notification.admin.data;

import com.solmod.notification.engine.domain.Status;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Encapsulate statement params building these in queries/updates
 */
class SQLSelectStatementParams {
    /**
     * As the statement in the context of a statement param, this is the statement representing the sub portion of
     * the statement listing the properties (WHERE clause for SELECT, SET clause for UPDATE)
     */
    private final StringBuilder statement = new StringBuilder();
    private final Map<String, Object> params = new HashMap<>();

    public void addField(String fieldName, Object requestValue) {
        // Lame workaround, https://solutionsmodular.atlassian.net/browse/NE-39
        if (requestValue instanceof Status) {
            appendProperty(fieldName, ((Status)requestValue).code(), statement);
        } else {
            appendProperty(fieldName, requestValue, statement);
        }
    }

    private void appendProperty(String fieldName, Object value, StringBuilder statement) {
        if (value != null && !StringUtils.isBlank(value.toString())) {
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
        return statement.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
