package edu.cdut.aiback.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FaceImageUtilTest {

    @Test
    void shouldStripDataUrlPrefix() {
        String base64 = FaceImageUtil.normalizeBase64("data:image/jpeg;base64,aGVsbG8=");

        assertEquals("aGVsbG8=", base64);
    }

    @Test
    void shouldKeepPlainBase64() {
        String base64 = FaceImageUtil.normalizeBase64("aGVsbG8=");

        assertEquals("aGVsbG8=", base64);
    }

    @Test
    void shouldRejectBlankBase64() {
        assertThrows(IllegalArgumentException.class, () -> FaceImageUtil.normalizeBase64(" "));
    }
}
