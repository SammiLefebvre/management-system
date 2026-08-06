package edu.cdut.aiback.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * 图片水印工具 —— 在完工照片上叠加地点 + 时间文字
 */
public class ImageWatermarkUtil {

    /**
     * 给图片添加水印文字（地点 + 时间）
     *
     * @param sourcePath 原始图片路径
     * @param location   地点文字
     * @param outputDir  输出目录
     * @return 加水印后的文件路径（相对 URL）
     */
    public static String addWatermark(String sourcePath, String location, String outputDir) {
        try {
            BufferedImage image = ImgUtil.read(FileUtil.file(sourcePath));

            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 水印文字: 地点 + 时间
            String timeStr = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            String watermark = location + "  " + timeStr;

            // 字体大小按图片宽度 5% 计算
            int fontSize = Math.max(18, image.getWidth() / 25);
            Font font = new Font("微软雅黑", Font.PLAIN, fontSize);
            g.setFont(font);

            // 半透明白色
            g.setColor(new Color(255, 255, 255, 200));

            // 计算文字宽度，居中放在底部
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(watermark);
            int textHeight = fm.getHeight();
            int x = (image.getWidth() - textWidth) / 2;
            int y = image.getHeight() - textHeight;

            // 先画阴影
            g.setColor(new Color(0, 0, 0, 100));
            g.drawString(watermark, x + 1, y + 1);

            // 再画白色文字
            g.setColor(new Color(255, 255, 255, 220));
            g.drawString(watermark, x, y);

            g.dispose();

            // 输出
            String dateDir = DateUtil.format(new Date(), "yyyyMMdd");
            String dir = outputDir + File.separator + dateDir;
            if (!FileUtil.exist(dir)) {
                FileUtil.mkdir(dir);
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
            String outputPath = dir + File.separator + fileName;
            ImgUtil.write(image, FileUtil.file(outputPath));

            return "/uploads/" + dateDir + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("图片水印添加失败", e);
        }
    }
}
