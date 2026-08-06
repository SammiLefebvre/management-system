package edu.cdut.aiback.controller;

import edu.cdut.aiback.dto.FaceRegisterBase64Request;
import edu.cdut.aiback.dto.FaceRegisterResponse;
import edu.cdut.aiback.service.FaceService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class LegacyFaceController {
    private final FaceService faceService;

    public LegacyFaceController(FaceService faceService) {
        this.faceService = faceService;
    }

    @PostMapping("/faceregister")
    public FaceRegisterResponse faceRegister(@RequestBody FaceRegisterBase64Request request) {
        return faceService.registerFaceBase64(request.image(), request.groupId(), request.userId());
    }
}
