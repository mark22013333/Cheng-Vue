package com.cheng.common.utils;

import org.jasypt.commons.CommonUtils;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * @author cheng
 * @since 2022/12/16 10:20 AM
 */
public class JasyptUtils {

    public static final String KEY = System.getProperty("jasypt.encryptor.password");

    /**
     * Jasypt encryption
     *
     * @param salt  salt
     * @param value raw value
     * @return value to encrypt
     */
    public static String encryptVal(String salt, String value) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(crypto(salt));
        return encryptor.encrypt(value);
    }

    /**
     * Jasypt decryption
     *
     * @param salt       salt
     * @param encryptVal encrypted value
     * @return value to decrypt
     */
    public static String decryptVal(String salt, String encryptVal) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(crypto(salt));
        return encryptor.decrypt(encryptVal);
    }

    public static SimpleStringPBEConfig crypto(String salt) {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(salt); // secret key encryption
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType(CommonUtils.STRING_OUTPUT_TYPE_BASE64);
        return config;
    }

    public static void main(String[] args) {
        String key = System.getProperty("jasypt.encryptor.password");
        String encVal = encryptVal(key, "test");
        String decVal = decryptVal(key, "s7eiBxm219FzkR526Shrd+Adne1JNzCT");

        System.out.printf("encVal = ENC(%s)%n", encVal);
        System.out.printf("decVal = %s", decVal);
    }
}
