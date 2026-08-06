package edu.cdut.aiback.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cdut.aiback.common.BizException;
import edu.cdut.aiback.dto.DeviceQueryDTO;
import edu.cdut.aiback.entity.Device;
import edu.cdut.aiback.mapper.DeviceMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService extends ServiceImpl<DeviceMapper, Device> {

    public Page<Device> page(DeviceQueryDTO query) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getDeviceCode()), Device::getDeviceCode, query.getDeviceCode())
                .like(StrUtil.isNotBlank(query.getDeviceName()), Device::getDeviceName, query.getDeviceName())
                .like(StrUtil.isNotBlank(query.getArea()), Device::getArea, query.getArea())
                .eq(StrUtil.isNotBlank(query.getOperationType()), Device::getOperationType, query.getOperationType())
                .orderByDesc(Device::getCreatedAt);
        return page(new Page<>(query.getPage(), query.getSize()), wrapper);
    }

    public List<Device> listByOperationType(String operationType) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(operationType), Device::getOperationType, operationType)
                .orderByDesc(Device::getCreatedAt);
        return list(wrapper);
    }

    public Device getByCode(String deviceCode) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getDeviceCode, deviceCode);
        Device device = getOne(wrapper);
        if (device == null) {
            throw new BizException("设备不存在: " + deviceCode);
        }
        return device;
    }
}
