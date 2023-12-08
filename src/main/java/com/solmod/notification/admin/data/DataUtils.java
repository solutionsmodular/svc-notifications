package com.solmod.notification.admin.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static com.solmod.commons.SolStringUtils.bytesToHex;

public class DataUtils {

    public static String generateUid() throws NoSuchAlgorithmException {
        MessageDigest salt = MessageDigest.getInstance("SHA-256");
        salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        return bytesToHex(salt.digest());
    }


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
