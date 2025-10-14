package com.example.backend.service.impl;

import com.example.backend.dto.KhachHangRequest;
import com.example.backend.dto.KhachHangResponse;
import com.example.backend.dto.DiaChiKhachHangRequest;
import com.example.backend.dto.DiaChiKhachHangResponse;
import com.example.backend.entity.KhachHang;
import com.example.backend.entity.DiaChiKhachHang;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.DiaChiKhachHangRepository;
import com.example.backend.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class KhachHangServiceImpl implements KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private DiaChiKhachHangRepository diaChiKhachHangRepository;
    

    @Override
    public List<KhachHangResponse> getAllKhachHang() {
        return khachHangRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KhachHangResponse getKhachHangById(Long id) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        return convertToResponse(khachHang);
    }

    @Override
    public KhachHangResponse createKhachHang(KhachHangRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
        
        // Không cần kiểm tra user_id nữa vì chỉ lưu userId
        
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang(generateMaKhachHang());
        khachHang.setTenKhachHang(request.getTenKhachHang());
        khachHang.setEmail(request.getEmail());
        khachHang.setSoDienThoai(request.getSoDienThoai());
        khachHang.setNgaySinh(request.getNgaySinh());
        khachHang.setGioiTinh(request.getGioiTinh());
        khachHang.setDiemTichLuy(request.getDiemTichLuy() != null ? request.getDiemTichLuy() : 0);
        khachHang.setTongSoLanMua(request.getTongSoLanMua() != null ? request.getTongSoLanMua() : 0);
        khachHang.setLanMuaGanNhat(request.getLanMuaGanNhat());
        khachHang.setNgayTao(request.getNgayTao() != null ? request.getNgayTao() : LocalDate.now());
        khachHang.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        khachHang.setUserId(request.getUserId());
        
        KhachHang savedKhachHang = khachHangRepository.save(khachHang);
        return convertToResponse(savedKhachHang);
    }

    @Override
    public KhachHangResponse updateKhachHang(Long id, KhachHangRequest request) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        
        // Kiểm tra email trùng lặp (trừ khách hàng hiện tại)
        if (!khachHang.getEmail().equals(request.getEmail()) && 
            khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
        
        // Cập nhật thông tin
        khachHang.setTenKhachHang(request.getTenKhachHang());
        khachHang.setEmail(request.getEmail());
        khachHang.setSoDienThoai(request.getSoDienThoai());
        khachHang.setNgaySinh(request.getNgaySinh());
        khachHang.setGioiTinh(request.getGioiTinh());
        if (request.getDiemTichLuy() != null) {
            khachHang.setDiemTichLuy(request.getDiemTichLuy());
        }
        if (request.getTongSoLanMua() != null) {
            khachHang.setTongSoLanMua(request.getTongSoLanMua());
        }
        khachHang.setLanMuaGanNhat(request.getLanMuaGanNhat());
        if (request.getTrangThai() != null) {
            khachHang.setTrangThai(request.getTrangThai());
        }
        
        // Cập nhật userId nếu có
        if (request.getUserId() != null) {
            khachHang.setUserId(request.getUserId());
        }
        
        KhachHang updatedKhachHang = khachHangRepository.save(khachHang);
        return convertToResponse(updatedKhachHang);
    }

    @Override
    public void deleteKhachHang(Long id) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        khachHangRepository.delete(khachHang);
    }

    @Override
    public List<KhachHangResponse> searchKhachHang(String keyword) {
        List<KhachHang> results = khachHangRepository.findByTenKhachHangContaining(keyword);
        results.addAll(khachHangRepository.findByEmailContaining(keyword));
        results.addAll(khachHangRepository.findBySoDienThoaiContaining(keyword));
        
        return results.stream()
                .distinct()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<KhachHangResponse> getKhachHangByTrangThai(Boolean trangThai) {
        return khachHangRepository.findByTrangThai(trangThai).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KhachHangResponse getKhachHangByEmail(String email) {
        KhachHang khachHang = khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        return convertToResponse(khachHang);
    }

    @Override
    public KhachHangResponse getKhachHangByUserId(Long userId) {
        KhachHang khachHang = khachHangRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        return convertToResponse(khachHang);
    }

    @Override
    public List<KhachHangResponse> getTopKhachHangByDiemTichLuy() {
        return khachHangRepository.findAllOrderByDiemTichLuyDesc().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Địa chỉ khách hàng methods
    @Override
    public List<DiaChiKhachHangResponse> getDiaChiByKhachHangId(Long khachHangId) {
        return diaChiKhachHangRepository.findByKhachHangId(khachHangId).stream()
                .map(this::convertDiaChiToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DiaChiKhachHangResponse createDiaChi(DiaChiKhachHangRequest request) {
        KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        
        // Nếu địa chỉ mặc định, bỏ mặc định của các địa chỉ khác
        if (request.getMacDinh() != null && request.getMacDinh()) {
            List<DiaChiKhachHang> existingDiaChi = diaChiKhachHangRepository.findByKhachHangId(request.getKhachHangId());
            for (DiaChiKhachHang dc : existingDiaChi) {
                dc.setMacDinh(false);
                diaChiKhachHangRepository.save(dc);
            }
        }
        
        DiaChiKhachHang diaChi = new DiaChiKhachHang();
        diaChi.setKhachHang(khachHang);
        diaChi.setTenNguoiNhan(request.getTenNguoiNhan());
        diaChi.setSoDienThoai(request.getSoDienThoai());
        diaChi.setDiaChi(request.getDiaChi());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setQuanHuyen(request.getQuanHuyen());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setMacDinh(request.getMacDinh() != null ? request.getMacDinh() : false);
        diaChi.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        
        DiaChiKhachHang savedDiaChi = diaChiKhachHangRepository.save(diaChi);
        return convertDiaChiToResponse(savedDiaChi);
    }

    @Override
    public DiaChiKhachHangResponse updateDiaChi(Long id, DiaChiKhachHangRequest request) {
        DiaChiKhachHang diaChi = diaChiKhachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Nếu địa chỉ mặc định, bỏ mặc định của các địa chỉ khác
        if (request.getMacDinh() != null && request.getMacDinh()) {
            List<DiaChiKhachHang> existingDiaChi = diaChiKhachHangRepository.findByKhachHangId(diaChi.getKhachHang().getId());
            for (DiaChiKhachHang dc : existingDiaChi) {
                if (!dc.getId().equals(id)) {
                    dc.setMacDinh(false);
                    diaChiKhachHangRepository.save(dc);
                }
            }
        }
        
        diaChi.setTenNguoiNhan(request.getTenNguoiNhan());
        diaChi.setSoDienThoai(request.getSoDienThoai());
        diaChi.setDiaChi(request.getDiaChi());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setQuanHuyen(request.getQuanHuyen());
        diaChi.setPhuongXa(request.getPhuongXa());
        if (request.getMacDinh() != null) {
            diaChi.setMacDinh(request.getMacDinh());
        }
        if (request.getTrangThai() != null) {
            diaChi.setTrangThai(request.getTrangThai());
        }
        
        DiaChiKhachHang updatedDiaChi = diaChiKhachHangRepository.save(diaChi);
        return convertDiaChiToResponse(updatedDiaChi);
    }

    @Override
    public void deleteDiaChi(Long id) {
        DiaChiKhachHang diaChi = diaChiKhachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        diaChiKhachHangRepository.delete(diaChi);
    }

    @Override
    public DiaChiKhachHangResponse getDiaChiById(Long id) {
        DiaChiKhachHang diaChi = diaChiKhachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        return convertDiaChiToResponse(diaChi);
    }

    @Override
    public List<DiaChiKhachHangResponse> getDiaChiMacDinhByKhachHangId(Long khachHangId) {
        return diaChiKhachHangRepository.findByKhachHangIdAndMacDinhTrue(khachHangId).stream()
                .map(this::convertDiaChiToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void setDiaChiMacDinh(Long id, Long khachHangId) {
        DiaChiKhachHang diaChi = diaChiKhachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Bỏ mặc định của các địa chỉ khác
        List<DiaChiKhachHang> existingDiaChi = diaChiKhachHangRepository.findByKhachHangId(khachHangId);
        for (DiaChiKhachHang dc : existingDiaChi) {
            dc.setMacDinh(false);
            diaChiKhachHangRepository.save(dc);
        }
        
        // Đặt địa chỉ hiện tại làm mặc định
        diaChi.setMacDinh(true);
        diaChiKhachHangRepository.save(diaChi);
    }

    // Helper methods
    private KhachHangResponse convertToResponse(KhachHang khachHang) {
        KhachHangResponse response = new KhachHangResponse();
        response.setId(khachHang.getId());
        response.setMaKhachHang(khachHang.getMaKhachHang());
        response.setTenKhachHang(khachHang.getTenKhachHang());
        response.setEmail(khachHang.getEmail());
        response.setSoDienThoai(khachHang.getSoDienThoai());
        response.setNgaySinh(khachHang.getNgaySinh());
        response.setGioiTinh(khachHang.getGioiTinh());
        response.setDiemTichLuy(khachHang.getDiemTichLuy());
        response.setTongSoLanMua(khachHang.getTongSoLanMua());
        response.setLanMuaGanNhat(khachHang.getLanMuaGanNhat());
        response.setNgayTao(khachHang.getNgayTao());
        response.setTrangThai(khachHang.getTrangThai());
        response.setUserId(khachHang.getUserId());
        
        // Load địa chỉ khách hàng
        List<DiaChiKhachHang> diaChiList = diaChiKhachHangRepository.findByKhachHangId(khachHang.getId());
        List<DiaChiKhachHangResponse> diaChiResponseList = diaChiList.stream()
                .map(this::convertDiaChiToResponse)
                .collect(Collectors.toList());
        response.setDiaChiList(diaChiResponseList);
        
        return response;
    }
    
    private DiaChiKhachHangResponse convertDiaChiToResponse(DiaChiKhachHang diaChi) {
        DiaChiKhachHangResponse response = new DiaChiKhachHangResponse();
        response.setId(diaChi.getId());
        response.setTenNguoiNhan(diaChi.getTenNguoiNhan());
        response.setSoDienThoai(diaChi.getSoDienThoai());
        response.setDiaChi(diaChi.getDiaChi());
        response.setTinhThanh(diaChi.getTinhThanh());
        response.setQuanHuyen(diaChi.getQuanHuyen());
        response.setPhuongXa(diaChi.getPhuongXa());
        response.setMacDinh(diaChi.getMacDinh());
        response.setTrangThai(diaChi.getTrangThai());
        return response;
    }
    
    private String generateMaKhachHang() {
        // Tạo mã khách hàng tự động
        Long count = khachHangRepository.count();
        return "KH" + String.format("%06d", count + 1);
    }
}
