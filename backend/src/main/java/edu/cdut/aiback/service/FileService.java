package edu.cdut.aiback.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Service
public class FileService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 上传文件，返回访问 URL 相对路径
     */
    public String upload(MultipartFile file) throws IOException {
        // 按日期分子目录
        String dateDir = DateUtil.format(new Date(), "yyyyMMdd");
        String dir = uploadPath + File.separator + dateDir;
        if (!FileUtil.exist(dir)) {
            FileUtil.mkdir(dir);
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;
        String fullPath = dir + File.separator + newFileName;

        file.transferTo(new File(fullPath));

        return "/uploads/" + dateDir + "/" + newFileName;
    }
}
