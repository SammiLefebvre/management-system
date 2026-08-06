package edu.cdut.aiback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.cdut.aiback.entity.Device;
import edu.cdut.aiback.vo.DeviceMapVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    @Select("SELECT d.*, " +
            "(SELECT status FROM work_order WHERE device_id = d.id ORDER BY created_at DESC LIMIT 1) AS latestWorkOrderStatus " +
            "FROM device d WHERE d.project_group = #{projectGroup} AND d.latitude IS NOT NULL AND d.longitude IS NOT NULL")
    List<DeviceMapVO> selectDevicesWithLocation(@Param("projectGroup") String projectGroup);
}
