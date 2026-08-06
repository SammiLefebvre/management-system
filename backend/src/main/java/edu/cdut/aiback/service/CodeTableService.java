package edu.cdut.aiback.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cdut.aiback.entity.CodeTable;
import edu.cdut.aiback.entity.SlaConfig;
import edu.cdut.aiback.mapper.CodeTableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service
public class CodeTableService extends ServiceImpl<CodeTableMapper, CodeTable> {

    @Autowired
    private SlaConfigService slaConfigService;

    public List<CodeTable> listByType(String codeType) {
        return baseMapper.selectByType(codeType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(CodeTable entity) {
        boolean ok = super.save(entity);
        if (ok && "emergency_level".equals(entity.getCodeType())) {
            ensureSlaConfigExists(entity.getCodeValue());
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(CodeTable entity) {
        CodeTable old = getById(entity.getId());
        boolean ok = super.updateById(entity);
        if (ok && old != null && "emergency_level".equals(entity.getCodeType())) {
            // 如果码值发生变化，删除旧 SLA 配置，并确保新码值有配置
            if (!old.getCodeValue().equals(entity.getCodeValue())) {
                removeSlaConfig(old.getCodeValue());
                ensureSlaConfigExists(entity.getCodeValue());
            }
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        CodeTable entity = getById(id);
        boolean ok = super.removeById(id);
        if (ok && entity != null && "emergency_level".equals(entity.getCodeType())) {
            removeSlaConfig(entity.getCodeValue());
        }
        return ok;
    }

    private void ensureSlaConfigExists(String emergencyLevel) {
        SlaConfig existing = slaConfigService.getByEmergencyLevel(emergencyLevel);
        if (existing != null) {
            return;
        }
        SlaConfig config = new SlaConfig();
        config.setEmergencyLevel(emergencyLevel);
        config.setTargetResponseMinutes(60);
        config.setTargetRepairMinutes(240);
        slaConfigService.save(config);
    }

    private void removeSlaConfig(String emergencyLevel) {
        SlaConfig existing = slaConfigService.getByEmergencyLevel(emergencyLevel);
        if (existing != null) {
            slaConfigService.removeById(existing.getId());
        }
    }
}
