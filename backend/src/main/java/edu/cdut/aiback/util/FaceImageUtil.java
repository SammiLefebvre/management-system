package edu.cdut.aiback.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

public final class FaceImageUtil {
    private FaceImageUtil() {
    }

    public static String toBase64(MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            throw new IllegalArgumentException("image cannot be empty");
        }
        return Base64.getEncoder().encodeToString(image.getBytes());
    }

    public static String normalizeBase64(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new IllegalArgumentException("image cannot be blank");
        }
        int commaIndex = imageBase64.indexOf(',');
        if (imageBase64.startsWith("data:image/") && commaIndex >= 0) {
            return imageBase64.substring(commaIndex + 1);
        }
        return imageBase64;
    }
}
