package edu.cdut.aiback.service;

import com.baidu.aip.face.AipFace;
import edu.cdut.aiback.config.BaiduFaceProperties;
import edu.cdut.aiback.dto.FaceRecognizeResponse;
import edu.cdut.aiback.dto.FaceRegisterResponse;
import edu.cdut.aiback.util.FaceImageUtil;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FaceService {
    private static final String IMAGE_TYPE_BASE64 = "BASE64";

    private final AipFace client;
    private final BaiduFaceProperties properties;

    public FaceService(AipFace client, BaiduFaceProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    public FaceRegisterResponse registerFace(MultipartFile image, String groupId, String userId) throws IOException {
        String base64Image = FaceImageUtil.toBase64(image);
        return registerFaceBase64(base64Image, groupId, userId);
    }

    public FaceRegisterResponse registerFaceBase64(String imageBase64, String groupId, String userId) {
        String base64Image = FaceImageUtil.normalizeBase64(imageBase64);
        JSONObject response = client.addUser(base64Image, IMAGE_TYPE_BASE64, groupId, userId, null);

        int errorCode = response.optInt("error_code", -1);
        String errorMessage = response.optString("error_msg", "");
        return new FaceRegisterResponse(errorCode == 0, errorCode, errorMessage, response.toString());
    }

    public FaceRecognizeResponse recognizeFace(MultipartFile image, String groupIdList) throws IOException {
        String base64Image = FaceImageUtil.toBase64(image);
        return recognizeFaceBase64(base64Image, groupIdList);
    }

    public FaceRecognizeResponse recognizeFaceBase64(String imageBase64, String groupIdList) {
        String base64Image = FaceImageUtil.normalizeBase64(imageBase64);
        JSONObject response = client.search(base64Image, IMAGE_TYPE_BASE64, groupIdList, null);
        int errorCode = response.optInt("error_code", -1);
        if (errorCode != 0) {
            String errorMessage = response.optString("error_msg", "未知错误");
            return new FaceRecognizeResponse(false, 0, "", "", errorCode + ":" + errorMessage);
        }
        JSONObject result = response.optJSONObject("result");
        if (result == null) {
            return new FaceRecognizeResponse(false, 0, "", "", "未识别到人脸");
        }
        org.json.JSONArray userList = result.optJSONArray("user_list");
        if (userList == null || userList.length() == 0) {
            return new FaceRecognizeResponse(false, 0, "", "", "人脸库无匹配");
        }
        JSONObject firstUser = userList.getJSONObject(0);
        double score = firstUser.getDouble("score");
        String userId = firstUser.optString("user_id", "");
        String groupId = firstUser.optString("group_id", "");
        return new FaceRecognizeResponse(isSamePerson(score), score, userId, groupId, response.toString());
    }

    public boolean isSamePerson(double score) {
        return score >= properties.getScoreThreshold();
    }
}
