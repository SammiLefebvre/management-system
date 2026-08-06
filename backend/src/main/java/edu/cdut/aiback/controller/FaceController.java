package edu.cdut.aiback.controller;

import edu.cdut.aiback.dto.FaceRecognizeResponse;
import edu.cdut.aiback.dto.FaceRegisterBase64Request;
import edu.cdut.aiback.dto.FaceRegisterResponse;
import edu.cdut.aiback.service.FaceService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/api/face")
public class FaceController {
    private final FaceService faceService;

    public FaceController(FaceService faceService) {
        this.faceService = faceService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FaceRegisterResponse register(
            @RequestParam MultipartFile image,
            @RequestParam String groupId,
            @RequestParam String userId
    ) throws IOException {
        return faceService.registerFace(image, groupId, userId);
    }

    @PostMapping(value = "/register/base64", consumes = MediaType.APPLICATION_JSON_VALUE)
    public FaceRegisterResponse registerBase64(@RequestBody FaceRegisterBase64Request request) {
        return faceService.registerFaceBase64(request.image(), request.groupId(), request.userId());
    }

    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FaceRecognizeResponse recognize(
            @RequestParam MultipartFile image,
            @RequestParam String groupIdList
    ) throws IOException {
        return faceService.recognizeFace(image, groupIdList);
    }
}
