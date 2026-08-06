import request from './request'

export interface ReportQuery {
  dataType: 'work_order' | 'device' | 'personnel'
  startDate?: string
  endDate?: string
  projectGroup?: string
  status?: string
}

export function exportExcel(query: ReportQuery): Promise<Blob> {
  return request.post('/reports/export-excel', query, {
    responseType: 'blob',
    timeout: 120000,
  }) as Promise<Blob>
}

export function exportPdf(query: ReportQuery): Promise<Blob> {
  return request.post('/reports/export-pdf', query, {
    responseType: 'blob',
    timeout: 120000,
  }) as Promise<Blob>
}
