package com.solmod.commons;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ObjectUtilsTest {

    @Test
    void testToString_withValues() {
        SomeClass thing = new SomeClass("Whatever", 15);
        String thingString = thing.toString();

        assertNotNull(thingString);
        assertNotEquals("", thingString);
        assertTrue(thingString.contains("Whatever"));
        assertTrue(thingString.toLowerCase().contains("someproperty"));
    }

    @Test
    void doSomethingNoValues() {
        SomeClass thing = new SomeClass();
        String thingString = thing.toString();

        assertNotNull(thingString);
        assertEquals("", thingString);
    }

    public static class SomeClass {
        private String someProperty;
        private Integer someOtherProperty;

        public SomeClass() {
        }

        public SomeClass(String someProperty, int someOtherProperty) {
            this.someProperty = someProperty;
            this.someOtherProperty = someOtherProperty;
        }

        public String getSomeProperty() {
            return someProperty;
        }

        public void setSomeProperty(String someProperty) {
            this.someProperty = someProperty;
        }

        public Integer getSomeOtherProperty() {
            return someOtherProperty;
        }

        public void setSomeOtherProperty(int someOtherProperty) {
            this.someOtherProperty = someOtherProperty;
        }

        @Override
        public String toString() {
            try {
                return ObjectUtils.stringify(this);
            } catch (StringifyException e) {
                return Objects.toString(this);
            }
        }
    }


}