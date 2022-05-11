package com.solmod.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.style.DefaultToStringStyler;
import org.springframework.core.style.DefaultValueStyler;
import org.springframework.core.style.ToStringCreator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Arrays.stream;

public class ObjectUtils {

    public static final Logger log = LoggerFactory.getLogger(ObjectUtils.class);

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
     * @param toStringify
     * @return {@code String} Loggable string
     * @throws StringifyException In the event of any error
     */
    public static String stringify(Object toStringify) throws StringifyException {
        ToStringCreator toStringCreator = new ToStringCreator(toStringify, new ToStringSolmodStyler(toStringify));
        Method[] classMethods = toStringify.getClass().getMethods();

        final StringBuilder errorMessage = new StringBuilder();

        stream(classMethods)
                .filter(c -> (c.getName().startsWith("get") || c.getName().startsWith("is")) &&
                        !c.getName().equals("getClass"))
                .forEach(getMethod -> {
                    try {
                        toStringCreator.append(getMethod.getName(), getMethod.invoke(toStringify));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        errorMessage.append(e.getMessage()).append(" after parsing ").append(toStringCreator);
                    }
                });

        return errorMessage.length() == 0 ? toStringCreator.toString() : errorMessage.toString();
    }

    /**
     * Styler for logging objects
     */
    static class ToStringSolmodStyler extends DefaultToStringStyler {

        private Object toStyle;

        public ToStringSolmodStyler(Object toStyle) {
            super(new ValueStyler());
            this.toStyle = toStyle;
        }

        @Override
        public void styleStart(StringBuilder buffer, Object obj) {
            buffer.append(toStyle.getClass().getSimpleName());
        }

        @Override
        public void styleEnd(StringBuilder buffer, Object o) {

        }

        @Override
        public void styleField(StringBuilder buffer, String fieldName, Object value) {
            buffer.append("(")
                    .append(fieldName)
                    .append(" = ")
                    .append(getValueStyler().style(value))
                    .append(")");
        }
    }

    static class ValueStyler extends DefaultValueStyler {
        @Override
        public String style(Object value) {
            return super.style(value);
        }
    }

}
