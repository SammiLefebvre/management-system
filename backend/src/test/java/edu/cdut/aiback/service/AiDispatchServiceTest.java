package edu.cdut.aiback.service;

import edu.cdut.aiback.client.HuggingFaceClient;
import edu.cdut.aiback.entity.Device;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.entity.WorkOrder;
import edu.cdut.aiback.mapper.PersonnelMapper;
import edu.cdut.aiback.vo.AiDispatchAdviceVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiDispatchServiceTest {

    @Test
    void advise_shouldReturnRecommendation() {
        HuggingFaceClient huggingFaceClient = mock(HuggingFaceClient.class);
        WorkOrderService workOrderService = mock(WorkOrderService.class);
        DeviceService deviceService = mock(DeviceService.class);
        PersonnelMapper personnelMapper = mock(PersonnelMapper.class);

        AiDispatchService service = new AiDispatchService(
                huggingFaceClient, workOrderService, deviceService, personnelMapper);

        WorkOrder wo = new WorkOrder();
        wo.setId(1L);
        wo.setWorkOrderCode("20260101/维护-0001");
        wo.setDeviceId(10L);
        wo.setEmergencyLevel("一级");
        wo.setFaultType("黑屏");
        when(workOrderService.getById(1L)).thenReturn(wo);

        Device device = new Device();
        device.setId(10L);
        device.setLatitude(new BigDecimal("23.1291"));
        device.setLongitude(new BigDecimal("113.2644"));
        when(deviceService.getById(10L)).thenReturn(device);

        Personnel p = new Personnel();
        p.setId(2L);
        p.setName("张三");
        p.setLatitude(new BigDecimal("23.1300"));
        p.setLongitude(new BigDecimal("113.2700"));
        p.setPendingCount(1);
        p.setAvgResponse(15.0);
        p.setCompletedWeek(3);
        when(personnelMapper.selectCandidates(any(), any())).thenReturn(List.of(p));

        when(huggingFaceClient.generate(any())).thenReturn(
                "{\"personnelId\":2,\"name\":\"张三\",\"reason\":\"距离最近\"}");

        AiDispatchAdviceVO advice = service.advise(1L);

        assertNotNull(advice);
        assertEquals(2L, advice.getPersonnelId());
        assertEquals("张三", advice.getName());
    }
}
