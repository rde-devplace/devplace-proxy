package com.kubepattern.kubeproxy.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SubdomainEncodingUtil {
    private static final String AES = "AES";
    // 실제 환경에서는 안전하게 키를 관리해야 합니다. 여기서는 예시를 위한 키입니다.
    private static final byte[] key = new byte[]{-95, 123, -75, 88, 75, -33, 121, 65, 114, 5, -88, -127, -32, -45, -45, 35};

    // 데이터를 인코딩하는 메서드
    public static String encode(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKey secretKey = new SecretKeySpec(key, AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        String base64Encoded = Base64.getEncoder().encodeToString(encryptedBytes);

        // Base64 문자열을 서브도메인에서 사용 가능하게 변환합니다.
        return base64Encoded.replace('+', '-').replace('/', '_').replaceAll("=", "");
    }

    // 인코딩된 데이터를 디코딩하는 메서드
    public static String decode(String encodedData) throws Exception {
        // 서브도메인에서 사용 가능한 형식을 원래의 Base64 형식으로 변환합니다.
        String base64Data = encodedData.replace('-', '+').replace('_', '/');
        int paddingLength = (4 - base64Data.length() % 4) % 4;
        base64Data = base64Data + "=".repeat(paddingLength);

        Cipher cipher = Cipher.getInstance(AES);
        SecretKey secretKey = new SecretKeySpec(key, AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(base64Data));
        return new String(decryptedBytes);
    }
}
