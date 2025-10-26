package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DotGiamGiaRequest {
    @NotBlank(message = "Mã đợt giảm giá không được để trống")
    private String maDotGiamGia;



        //jkmgvhbjknhvkljhvnklhj
    
    private String loaiDotGiamGia;
    
    private String giaTriDotGiam;
    
    private Long soTien;
    
    private String moTa;
    
    @NotNull(message = "Ngày bắt đầu không được để trống ")
    private LocalDateTime ngayBatDau;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime ngayKetThuc;
    
    @NotNull(message = "Số lượng sử dụng không được để trống")
    private Integer soLuongSuDung;
    
    @NotBlank(message = "Tên đợt giảm giá không được để trống")
    private String tenDotGiamGia;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Boolean trangThai;
}
