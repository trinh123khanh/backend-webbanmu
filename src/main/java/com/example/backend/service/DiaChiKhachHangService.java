package com.example.backend.service;

import com.example.backend.dto.DiaChiKhachHangDTO;
import com.example.backend.entity.DiaChiKhachHang;
import com.example.backend.entity.KhachHang;
import com.example.backend.repository.DiaChiKhachHangRepository;
import com.example.backend.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaChiKhachHangService {
    
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final KhachHangRepository khachHangRepository;
    
    // Lấy tất cả địa chỉ của khách hàng
    public List<DiaChiKhachHangDTO> getDiaChiByKhachHangId(Long khachHangId) {
        List<DiaChiKhachHang> diaChiList = diaChiKhachHangRepository
            .findByKhachHangIdAndTrangThaiTrueOrderByMacDinhDescNgayTaoDesc(khachHangId);
        
        return diaChiList.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Lấy địa chỉ mặc định của khách hàng
    public Optional<DiaChiKhachHangDTO> getDiaChiMacDinhByKhachHangId(Long khachHangId) {
        return diaChiKhachHangRepository
            .findByKhachHangIdAndMacDinhTrueAndTrangThaiTrue(khachHangId)
            .map(this::convertToDTO);
    }
    
    // Thêm địa chỉ mới
    @Transactional
    public DiaChiKhachHangDTO createDiaChi(DiaChiKhachHangDTO diaChiDTO) {
        // Kiểm tra khách hàng có tồn tại không
        KhachHang khachHang = khachHangRepository.findById(diaChiDTO.getKhachHangId())
            .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        
        DiaChiKhachHang diaChi = convertToEntity(diaChiDTO);
        diaChi.setKhachHang(khachHang);
        
        // Nếu đây là địa chỉ mặc định, cập nhật tất cả địa chỉ khác thành không mặc định
        if (diaChi.getMacDinh()) {
            diaChiKhachHangRepository.updateAllAddressesToNonDefault(khachHang.getId());
        }
        
        DiaChiKhachHang savedDiaChi = diaChiKhachHangRepository.save(diaChi);
        return convertToDTO(savedDiaChi);
    }
    
    // Cập nhật địa chỉ
    @Transactional
    public DiaChiKhachHangDTO updateDiaChi(Long id, DiaChiKhachHangDTO diaChiDTO) {
        DiaChiKhachHang existingDiaChi = diaChiKhachHangRepository
            .findByIdAndKhachHangId(id, diaChiDTO.getKhachHangId())
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Cập nhật thông tin
        existingDiaChi.setTenNguoiNhan(diaChiDTO.getTenNguoiNhan());
        existingDiaChi.setSoDienThoai(diaChiDTO.getSoDienThoai());
        existingDiaChi.setDiaChiChiTiet(diaChiDTO.getDiaChiChiTiet());
        existingDiaChi.setTinhThanh(diaChiDTO.getTinhThanh());
        existingDiaChi.setQuanHuyen(diaChiDTO.getQuanHuyen());
        existingDiaChi.setPhuongXa(diaChiDTO.getPhuongXa());
        existingDiaChi.setTrangThai(diaChiDTO.getTrangThai());
        
        // Nếu đặt làm mặc định, cập nhật tất cả địa chỉ khác thành không mặc định
        if (diaChiDTO.getMacDinh() && !existingDiaChi.getMacDinh()) {
            diaChiKhachHangRepository.updateAllAddressesToNonDefault(diaChiDTO.getKhachHangId());
        }
        existingDiaChi.setMacDinh(diaChiDTO.getMacDinh());
        
        DiaChiKhachHang savedDiaChi = diaChiKhachHangRepository.save(existingDiaChi);
        return convertToDTO(savedDiaChi);
    }
    
    // Xóa địa chỉ (soft delete)
    @Transactional
    public void deleteDiaChi(Long id, Long khachHangId) {
        DiaChiKhachHang diaChi = diaChiKhachHangRepository
            .findByIdAndKhachHangId(id, khachHangId)
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Nếu đây là địa chỉ mặc định, cần đặt địa chỉ khác làm mặc định
        if (diaChi.getMacDinh()) {
            List<DiaChiKhachHang> otherAddresses = diaChiKhachHangRepository
                .findByKhachHangIdAndTrangThaiTrueOrderByMacDinhDescNgayTaoDesc(khachHangId)
                .stream()
                .filter(addr -> !addr.getId().equals(id))
                .collect(Collectors.toList());
            
            if (!otherAddresses.isEmpty()) {
                otherAddresses.get(0).setMacDinh(true);
                diaChiKhachHangRepository.save(otherAddresses.get(0));
            }
        }
        
        diaChi.setTrangThai(false);
        diaChiKhachHangRepository.save(diaChi);
    }
    
    // Đặt địa chỉ làm mặc định
    @Transactional
    public DiaChiKhachHangDTO setDiaChiMacDinh(Long id, Long khachHangId) {
        DiaChiKhachHang diaChi = diaChiKhachHangRepository
            .findByIdAndKhachHangId(id, khachHangId)
            .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Cập nhật tất cả địa chỉ khác thành không mặc định
        diaChiKhachHangRepository.updateAllAddressesToNonDefault(khachHangId);
        
        // Đặt địa chỉ này làm mặc định
        diaChi.setMacDinh(true);
        DiaChiKhachHang savedDiaChi = diaChiKhachHangRepository.save(diaChi);
        
        return convertToDTO(savedDiaChi);
    }
    
    // Lấy tất cả địa chỉ (cho hiển thị bảng)
    public List<DiaChiKhachHangDTO> getAllDiaChi() {
        List<DiaChiKhachHang> allDiaChi = diaChiKhachHangRepository
            .findByTrangThaiTrueOrderByKhachHangIdAscMacDinhDescNgayTaoDesc();
        
        return allDiaChi.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Chuyển đổi Entity sang DTO
    private DiaChiKhachHangDTO convertToDTO(DiaChiKhachHang diaChi) {
        return DiaChiKhachHangDTO.builder()
            .id(diaChi.getId())
            .tenNguoiNhan(diaChi.getTenNguoiNhan())
            .soDienThoai(diaChi.getSoDienThoai())
            .diaChiChiTiet(diaChi.getDiaChiChiTiet())
            .tinhThanh(diaChi.getTinhThanh())
            .quanHuyen(diaChi.getQuanHuyen())
            .phuongXa(diaChi.getPhuongXa())
            .macDinh(diaChi.getMacDinh())
            .trangThai(diaChi.getTrangThai())
            .ngayTao(diaChi.getNgayTao())
            .ngayCapNhat(diaChi.getNgayCapNhat())
            .khachHangId(diaChi.getKhachHang().getId())
            .build();
    }
    
    // Chuyển đổi DTO sang Entity
    private DiaChiKhachHang convertToEntity(DiaChiKhachHangDTO diaChiDTO) {
        DiaChiKhachHang diaChi = new DiaChiKhachHang();
        diaChi.setTenNguoiNhan(diaChiDTO.getTenNguoiNhan());
        diaChi.setSoDienThoai(diaChiDTO.getSoDienThoai());
        diaChi.setDiaChiChiTiet(diaChiDTO.getDiaChiChiTiet());
        diaChi.setTinhThanh(diaChiDTO.getTinhThanh());
        diaChi.setQuanHuyen(diaChiDTO.getQuanHuyen());
        diaChi.setPhuongXa(diaChiDTO.getPhuongXa());
        diaChi.setMacDinh(diaChiDTO.getMacDinh() != null ? diaChiDTO.getMacDinh() : false);
        diaChi.setTrangThai(diaChiDTO.getTrangThai() != null ? diaChiDTO.getTrangThai() : true);
        return diaChi;
    }
}
