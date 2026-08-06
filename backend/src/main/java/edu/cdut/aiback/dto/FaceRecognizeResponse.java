package edu.cdut.aiback.dto;

public record FaceRecognizeResponse(
        boolean samePerson,
        double score,
        String userId,
        String groupId,
        String rawResponse
) {
}
