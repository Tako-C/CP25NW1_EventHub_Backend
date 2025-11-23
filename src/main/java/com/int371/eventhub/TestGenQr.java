package com.int371.eventhub; // สำคัญ: ต้องมีบรรทัดนี้

import java.lang.reflect.Field;

import com.int371.eventhub.util.EncryptionUtil;

public class TestGenQr {
    public static void main(String[] args) {

        String jsonPayload = "{\"userId\": 147, \"eventId\": 2, \"registrationDate\": \"1482-11-23:15:14:12\", \"status\": \"registration\"}";

        EncryptionUtil encryptionUtil = new EncryptionUtil();
        
        try {
            Field secretKeyField = EncryptionUtil.class.getDeclaredField("SECRET_KEY");
            
            secretKeyField.setAccessible(true);

            secretKeyField.set(encryptionUtil, "FOREVENTHUBKEYEC");
            
        } catch (Exception e) {
            System.err.println("Error setting secret key: " + e.getMessage());
            return;
        }

        try {
            String encryptedString = encryptionUtil.encrypt(jsonPayload);
            System.out.println("--- Copy ค่าด้านล่างนี้ไปใส่ใน Postman (ตรง qrContent) ---");
            System.out.println(encryptedString);
            System.out.println("---------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}