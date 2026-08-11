# Feature Mapping: Public Datasets → Java System Schema

This document explains how the public datasets used for model training map to the real entities in the Java/Spring Boot work-order system.

## UCI AI4I 2020 → `Device` / Predictive Maintenance

The AI4I dataset is a synthetic industrial process dataset. In our system, each row represents a **device health snapshot** that can be attached to a `Device` record.

| AI4I Feature | Java Entity / Field | Meaning in our system |
|---|---|---|
| `Type` | `Device.deviceType` or code-table `work_order_type` | Product / device variant (H/M/L) |
| `Air temperature [K]` | Telemetry table or `Device` tag | Environmental / operating temperature |
| `Process temperature [K]` | Telemetry table or `Device` tag | Core process temperature |
| `Rotational speed [rpm]` | Telemetry table or `Device` tag | Motor / fan speed |
| `Torque [Nm]` | Telemetry table or `Device` tag | Mechanical load |
| `Tool wear [min]` | Derived from maintenance log | Accumulated runtime / wear since last service |
| `Machine failure` | Predicted field | Binary risk score (0 = healthy, 1 = likely to fail) |
| `TWF/HDF/PWF/OSF/RNF` | Predicted failure reasons | Which failure mode is likely |

### Integration idea

When a device telemetry snapshot is collected, the Java backend sends these features to the Python prediction service and receives a `failureProbability`. If the probability exceeds a threshold, the system can **auto-generate a preventive work order** with emergency level `一级` and assign it to the nearest available engineer.

---

## Public Work-Order CSV → `WorkOrder` / SLA

The public CSV comes from a sewer-maintenance sample. It maps to the `WorkOrder` lifecycle in our system.

| CSV Column | Java Entity / Field | Meaning in our system |
|---|---|---|
| `WORKORDER_ACTIVITY_CODE` | `WorkOrder.workOrderType` or `faultType` code-table | Job type / fault category |
| `WORKORDER_ADDED` | `WorkOrder.createdAt` / `publishTime` | When the work order was created / published |
| `WORKORDER_STARTED` | `WorkOrder.claimTime` / `checkinTime` | When work actually began |
| `WORKORDER_COMPLETED` | `WorkOrder.completeTime` / `confirmTime` | When work finished |
| `WORKORDER_ACTIVITY_DESCRIPTION` | `WorkOrder.faultDescription` | Free-text description of the work |

### Derived features used for training

| Derived Feature | Source | Meaning |
|---|---|---|
| `activity_code` | `WORKORDER_ACTIVITY_CODE` | Categorical code for the job type |
| `hour_added` | `WORKORDER_ADDED` | Hour of day the ticket was created |
| `dow_added` | `WORKORDER_ADDED` | Day of week (0=Monday) |
| `month_added` | `WORKORDER_ADDED` | Month of year |
| `was_completed` | `WORKORDER_COMPLETED` | Whether the ticket was finished |
| `duration_minutes` | `COMPLETED - STARTED` | How long the job took |
| `sla_breached` | Duration + completion status | Whether the job missed its SLA window |

### Integration idea

When a new work order is created in the Java system, the backend can send the activity code and creation time to the Python service. The service returns:

- `predictedDurationMinutes` — expected repair time
- `slaBreachProbability` — likelihood of missing the SLA

The Vue dashboard can color-code high-risk work orders and trigger early escalation.

---

## Recommended future features

When the system accumulates real data, these additional fields should be added to the training set:

| Real System Field | Model Use |
|---|---|
| `Device.latitude` / `longitude` | Dispatch distance, route optimization |
| `Personnel.skill` / `role` | Engineer matching |
| `Personnel.currentWorkload` | Workload balancing |
| `SlaConfig.responseTime` / `resolveTime` | More precise SLA targets |
| `WorkOrder.emergencyLevel` | Priority-aware prediction |
| Weather / traffic API | External factors affecting response time |
| Historical completion photos | Image-based quality scoring (future CV model) |
