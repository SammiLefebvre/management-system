package edu.cdut.aiback.dto;

public record FaceRegisterBase64Request(
        String image,
        String groupId,
        String userId
) {
}
