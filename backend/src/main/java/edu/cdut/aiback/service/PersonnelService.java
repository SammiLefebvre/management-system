package edu.cdut.aiback.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cdut.aiback.common.BizException;
import edu.cdut.aiback.dto.PersonnelQueryDTO;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.mapper.PersonnelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonnelService extends ServiceImpl<PersonnelMapper, Personnel> {

    public Page<Personnel> page(PersonnelQueryDTO query) {
        LambdaQueryWrapper<Personnel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getName()), Personnel::getName, query.getName())
                .eq(StrUtil.isNotBlank(query.getRole()), Personnel::getRole, query.getRole())
                .eq(query.getStatus() != null, Personnel::getStatus, query.getStatus())
                .orderByDesc(Personnel::getCreatedAt);
        return page(new Page<>(query.getPage(), query.getSize()), wrapper);
    }

    public Personnel getByAccount(String account) {
        LambdaQueryWrapper<Personnel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Personnel::getAccount, account);
        Personnel p = getOne(wrapper);
        if (p == null) {
            throw new BizException("用户不存在: " + account);
        }
        return p;
    }

    public Personnel getByWxOpenId(String wxOpenId) {
        LambdaQueryWrapper<Personnel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Personnel::getWxOpenId, wxOpenId);
        return getOne(wrapper);
    }

    /**
     * 查询外场人员列表（供班组选择成员）
     */
    public List<Personnel> listExternal(String projectGroup) {
        LambdaQueryWrapper<Personnel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Personnel::getRole, "外场")
                .eq(Personnel::getStatus, 1)
                .eq(StrUtil.isNotBlank(projectGroup), Personnel::getProjectGroup, projectGroup)
                .orderByAsc(Personnel::getName);
        return list(wrapper);
    }
}
