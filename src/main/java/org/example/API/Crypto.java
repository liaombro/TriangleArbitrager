package org.example.API;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Crypto {

    public static final String API_KEY = getApiKey();

    public static final String SECRET_KEY = getSecretKey();



    public static final Charset charset = StandardCharsets.UTF_8;
    public String getHMAC(String key, String inputData) {
        try {

            SecretKey keyBytes = new SecretKeySpec(key.getBytes(charset), "HmacSHA256");
            Mac m = Mac.getInstance("HmacSHA256");

            m.init(keyBytes);
            m.update(inputData.getBytes(charset));

            byte[] rawHmac = m.doFinal();
            byte[] hexBytes = new Hex().encode(rawHmac);

            return new String(hexBytes, charset);
        } catch (Exception ignored) {
        }
        return null;
    }
    public String getSignature(String request) {
        try {
            String hmac = getHMAC(SECRET_KEY, request);
            byte[] decodedHex = Hex.decodeHex(hmac);
            byte[] token = Base64.encodeBase64(decodedHex);
            return new String(token, charset);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getApiKey() {
        Scanner myReader = null;
        String apiKey = null;

        try {
            File settings = new File("src/main/resources/token.txt");
            myReader = new Scanner(settings);
            apiKey = myReader.nextLine();
        } catch (Exception ignored) {

        } finally {
            myReader.close();
        }
        return apiKey;
    }

    private static String getSecretKey() {
        Scanner myReader = null;
        String secretKey = null;

        try {
            File settings = new File("src/main/resources/token.txt");
            myReader = new Scanner(settings);
            myReader.nextLine();
            secretKey = myReader.nextLine();
        } catch (Exception ignored) {

        } finally {
            myReader.close();
        }
        System.out.println(secretKey);
        return secretKey;

    }
}
