package com.int371.eventhub; // สำคัญ: ต้องมีบรรทัดนี้

import java.lang.reflect.Field;

import com.int371.eventhub.util.EncryptionUtil;

public class TestGenQr {
    public static void main(String[] args) {

        String qrRawData = "UID147 EID2 23-11-1482T15:44";

        EncryptionUtil encryptionUtil = new EncryptionUtil();
        
        try {
            Field secretKeyField = EncryptionUtil.class.getDeclaredField("SECRET_KEY");
            secretKeyField.setAccessible(true);
            secretKeyField.set(encryptionUtil, "FOREVENTHUBKEYEC"); // Key จาก application.properties
        } catch (Exception e) {
            System.err.println("Error setting secret key: " + e.getMessage());
            return;
        }

        try {
            String encryptedString = encryptionUtil.encrypt(qrRawData);
            System.out.println("--- Copy ค่าด้านล่างนี้ไปใส่ใน Postman (ตรง qrContent) ---");
            System.out.println(encryptedString);
            System.out.println("---------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}