package com.example.backend.service;

import com.example.backend.dto.KhachHangRequest;
import com.example.backend.dto.KhachHangResponse;
import com.example.backend.dto.DiaChiKhachHangRequest;
import com.example.backend.dto.DiaChiKhachHangResponse;

import java.util.List;

public interface KhachHangService {
    
    // Khách hàng methods
    List<KhachHangResponse> getAllKhachHang();
    
    KhachHangResponse getKhachHangById(Long id);
    
    KhachHangResponse createKhachHang(KhachHangRequest request);
    
    KhachHangResponse updateKhachHang(Long id, KhachHangRequest request);
    
    void deleteKhachHang(Long id);
    
    List<KhachHangResponse> searchKhachHang(String keyword);
    
    List<KhachHangResponse> getKhachHangByTrangThai(Boolean trangThai);
    
    KhachHangResponse getKhachHangByEmail(String email);
    
    KhachHangResponse getKhachHangByUserId(Long userId);
    
    List<KhachHangResponse> getTopKhachHangByDiemTichLuy();
    
    // Địa chỉ khách hàng methods
    List<DiaChiKhachHangResponse> getDiaChiByKhachHangId(Long khachHangId);
    
    DiaChiKhachHangResponse createDiaChi(DiaChiKhachHangRequest request);
    
    DiaChiKhachHangResponse updateDiaChi(Long id, DiaChiKhachHangRequest request);
    
    void deleteDiaChi(Long id);
    
    DiaChiKhachHangResponse getDiaChiById(Long id);
    
    List<DiaChiKhachHangResponse> getDiaChiMacDinhByKhachHangId(Long khachHangId);
    
    void setDiaChiMacDinh(Long id, Long khachHangId);
}
