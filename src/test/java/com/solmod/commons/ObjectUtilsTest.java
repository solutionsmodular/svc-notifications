package com.solmod.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ObjectUtilsTest {

    @Test
    void doSomethingRandom() {
        SomeClass thing = new SomeClass("Whatever", "Whatever else");
        String thingString = thing.toString();

        assertNotNull(thingString);
        assertNotEquals("", thingString);
        System.out.println("Solmod styled a thing: " + thingString);
    }

    public static class SomeClass {
        private String someProperty;
        private String someOtherProperty;

        public SomeClass(String someProperty, String someOtherProperty) {
            this.someProperty = someProperty;
            this.someOtherProperty = someOtherProperty;
        }

        public String getSomeProperty() {
            return someProperty;
        }

        public void setSomeProperty(String someProperty) {
            this.someProperty = someProperty;
        }

        public String getSomeOtherProperty() {
            return someOtherProperty;
        }

        public void setSomeOtherProperty(String someOtherProperty) {
            this.someOtherProperty = someOtherProperty;
        }

        @Override
        public String toString() {
            try {
                return ObjectUtils.toJson(this, new ObjectMapper());
            } catch (StringifyException e) {
                return Objects.toString(this);
            }
        }
    }


}