package edu.cdut.aiback.dto;

public record FaceRegisterResponse(
        boolean success,
        int errorCode,
        String errorMessage,
        String rawResponse
) {
}
