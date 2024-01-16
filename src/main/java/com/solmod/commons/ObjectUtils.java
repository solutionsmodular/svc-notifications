package com.solmod.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static java.util.Arrays.stream;

public class ObjectUtils {

    /**
     * Utility to take control over the manner by which the application serializes to JSON
     * to allow for us to account for security level/requirements.
     * At some iteration, we'll add levels of access so that field values are masked as needed
     * for information security. Now, it just converts to JSON.
     * If there are any Exceptions during this process, this method will handle logging the error
     *
     * @param toStringify {@code Object} to serialize
     * @return {@code String} with masking appropriate to the security levels required
     * @throws StringifyException In the event of an error of any sort
     */
    public static String toJson(Object toStringify, ObjectMapper objectMapper) throws StringifyException {
        try {
            return objectMapper.writeValueAsString(toStringify);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // blunt reaction to what's most likely a coding error
            throw new StringifyException("Exception attempting to serialize: " + e.getMessage());
        }
    }

    /**
     * Eventually, this method will ensure that objects are properly stringified to mask for security, etc
     *
     * @param toStringify Object to create a string of, for logging purposes
     * @return {@code String} Loggable string
     * @throws StringifyException In the event of any error
     */
    public static String stringify(Object toStringify) throws StringifyException {
        Method[] classMethods = toStringify.getClass().getMethods();

        final StringBuilder errorMessage = new StringBuilder();

        StringBuffer stringified = new StringBuffer();
        stream(classMethods)
                .filter(c -> (c.getName().startsWith("get") || c.getName().startsWith("is")) &&
                        !c.getName().equals("getClass"))
                .forEach(getMethod -> {
                    try {
                        Object value = getMethod.invoke(toStringify);
                        if (value != null && !StringUtils.isBlank(value.toString())) {
                            stringified.append(String.format("(%s=%s)", getMethod.getName(), value));
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        errorMessage.append(e.getMessage()).append(" after parsing ").append(stringified);
                    }
                });

        return errorMessage.length() == 0 ? stringified.toString() : errorMessage.toString();
    }

    /**
     * Create a flat, Properties-like, construct representing the data in the provided context. This facilitates
     * the use of context keys such as {@code parent.child.property}
     *
     * @param context {@code Object} of any sort, to flatten
     * @return Map of String key Object value context
     * @throws StringifyException In the event there's something funky with the specified context
     */
    public static Map<String, Object> flatten(Object context) throws StringifyException {
        String json = toJson(context, new JsonMapper());
        Map<String, Object> flattened = JsonFlattener.flattenAsMap(json);

        return flattened;
    }
}
