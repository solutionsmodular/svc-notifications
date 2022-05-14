package com.solmod.svcnotificationadmin.data;

public class DataUtils {

    public static class FieldUpdate {
        private String fieldName;
        private Object originalVal;
        private Object newVal;

        public FieldUpdate(String fieldName, Object originalVal, Object newVal) {
            this.fieldName = fieldName;
            this.originalVal = originalVal;
            this.newVal = newVal;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public Object getOriginalVal() {
            return originalVal;
        }

        public void setOriginalVal(Object originalVal) {
            this.originalVal = originalVal;
        }

        public Object getNewVal() {
            return newVal;
        }

        public void setNewVal(Object newVal) {
            this.newVal = newVal;
        }
    }
}
