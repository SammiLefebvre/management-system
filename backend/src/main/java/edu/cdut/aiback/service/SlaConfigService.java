package edu.cdut.aiback.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cdut.aiback.entity.SlaConfig;
import edu.cdut.aiback.mapper.SlaConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SlaConfigService extends ServiceImpl<SlaConfigMapper, SlaConfig> {

    /**
     * 获取全部 SLA 配置（仅有 3 条：一级/二级/三级）
     */
    public List<SlaConfig> listAll() {
        return list();
    }

    /**
     * 按紧急程度获取 SLA 配置
     */
    public SlaConfig getByEmergencyLevel(String level) {
        return lambdaQuery().eq(SlaConfig::getEmergencyLevel, level).one();
    }
}
