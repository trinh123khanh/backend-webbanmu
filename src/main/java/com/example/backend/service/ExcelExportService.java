package com.example.backend.service;

// Tạm thời comment Apache POI imports để tránh lỗi compile
// import org.apache.poi.ss.usermodel.*;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    public String exportPhieuGiamGiaToExcel(List<Map<String, Object>> data) throws IOException {
        // Tạm thời return empty string thay vì tạo Excel
        // TODO: Uncomment khi Apache POI đã được cấu hình đúng
        /*
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Phiếu Giảm Giá");

        // Tạo header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        // Tạo data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        // Tạo header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "STT", "Mã Phiếu", "Tên Phiếu", "Loại Phiếu", "Giá Trị Giảm",
            "Giá Trị Tối Thiểu", "Số Tiền Tối Đa", "Hóa Đơn Tối Thiểu",
            "Số Lượng", "Trạng Thái", "Ngày Bắt Đầu", "Ngày Kết Thúc",
            "Ngày Tạo", "Ngày Cập Nhật"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Tạo data rows
        int rowNum = 1;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            
            int colNum = 0;
            for (String header : headers) {
                Cell cell = row.createCell(colNum++);
                Object value = rowData.get(header);
                
                if (value != null) {
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                } else {
                    cell.setCellValue("");
                }
                cell.setCellStyle(dataStyle);
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Convert to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        // Convert to base64
        byte[] excelBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(excelBytes);
        */
        
        // Tạm thời return empty string
        System.out.println("📊 Excel Export Service - Simulated export for " + data.size() + " records");
        return Base64.getEncoder().encodeToString("Excel export simulated".getBytes());
    }
}
