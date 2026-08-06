package edu.cdut.aiback.service;

import com.baidu.aip.face.AipFace;
import edu.cdut.aiback.config.BaiduFaceProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FaceServiceTest {

    @Test
    void score80ShouldBeSamePerson() {
        FaceService faceService = new FaceService(new AipFace("app", "key", "secret"), new BaiduFaceProperties());

        assertTrue(faceService.isSamePerson(80));
    }

    @Test
    void scoreBelow80ShouldNotBeSamePerson() {
        FaceService faceService = new FaceService(new AipFace("app", "key", "secret"), new BaiduFaceProperties());

        assertFalse(faceService.isSamePerson(79.99));
    }
}
