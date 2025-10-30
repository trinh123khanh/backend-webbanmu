package com.example.backend.service;

import com.example.backend.dto.KhachHangDTO;
import com.example.backend.dto.DiaChiKhachHangDTO;
import com.example.backend.entity.KhachHang;
import com.example.backend.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private DiaChiKhachHangService diaChiKhachHangService;

    // Lấy tất cả khách hàng với phân trang
    public Page<KhachHangDTO> getAllKhachHang(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<KhachHang> khachHangPage = khachHangRepository.findAll(pageable);
        
        return khachHangPage.map(this::convertToDTOWithAddress);
    }

    // Tìm kiếm khách hàng với bộ lọc
    public Page<KhachHangDTO> searchKhachHang(String keyword, Boolean trangThai, 
                                             int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<KhachHang> khachHangPage = khachHangRepository.findAll(pageable);
        
        return khachHangPage.map(this::convertToDTOWithAddress);
    }

    // Lấy khách hàng theo ID
    public Optional<KhachHangDTO> getKhachHangById(Long id) {
        return khachHangRepository.findById(id)
                .map(this::convertToDTO);
    }


    // Tìm kiếm khách hàng với bộ lọc
    public Page<KhachHangDTO> searchKhachHang(String maKhachHang, String tenKhachHang, String email, String soDienThoai, 
                                            Boolean trangThai, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<KhachHang> khachHangPage = khachHangRepository.findWithFilters(
            maKhachHang, tenKhachHang, email, soDienThoai, trangThai, pageable);
        
        return khachHangPage.map(this::convertToDTO);
    }

    // Lấy khách hàng theo mã
    public Optional<KhachHangDTO> getKhachHangByMa(String maKhachHang) {
        return khachHangRepository.findByMaKhachHang(maKhachHang)
                .map(this::convertToDTO);
    }

    // Lấy khách hàng theo email
    public Optional<KhachHangDTO> getKhachHangByEmail(String email) {
        return khachHangRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    // Lấy khách hàng theo số điện thoại
    public Optional<KhachHangDTO> getKhachHangBySoDienThoai(String soDienThoai) {
        return khachHangRepository.findBySoDienThoai(soDienThoai)
                .map(this::convertToDTO);

    }

    // Tạo khách hàng mới
    public KhachHangDTO createKhachHang(KhachHangDTO khachHangDTO) {
        // Kiểm tra email đã tồn tại
        if (khachHangRepository.findByEmail(khachHangDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại: " + khachHangDTO.getEmail());
        }

        // Kiểm tra số điện thoại đã tồn tại
        if (khachHangRepository.findBySoDienThoai(khachHangDTO.getSoDienThoai()).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại: " + khachHangDTO.getSoDienThoai());
        }

        // Tạo mã khách hàng nếu chưa có
        if (khachHangDTO.getMaKhachHang() == null || khachHangDTO.getMaKhachHang().trim().isEmpty()) {
            khachHangDTO.setMaKhachHang(generateMaKhachHang());
        } else {
            // Kiểm tra mã khách hàng đã tồn tại
            if (khachHangRepository.findByMaKhachHang(khachHangDTO.getMaKhachHang()).isPresent()) {
                throw new RuntimeException("Mã khách hàng đã tồn tại: " + khachHangDTO.getMaKhachHang());
            }
        }

        KhachHang khachHang = convertToEntity(khachHangDTO);
        khachHang.setNgayTao(LocalDate.now());

        khachHang.setDiemTichLuy(0);
        khachHang.setTrangThai(true);
        khachHang.setSoLanMua(0);
        khachHang.setLanMuaGanNhat(null);
        

        khachHang.setTrangThai(true); // Mặc định active


        KhachHang savedKhachHang = khachHangRepository.save(khachHang);
        return convertToDTO(savedKhachHang);
    }

    // Cập nhật khách hàng
    public KhachHangDTO updateKhachHang(Long id, KhachHangDTO khachHangDTO) {
        KhachHang existingKhachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));

        // Kiểm tra email đã tồn tại (trừ khách hàng hiện tại)
        if (khachHangRepository.existsByEmailAndIdNot(khachHangDTO.getEmail(), id)) {
            throw new RuntimeException("Email đã tồn tại: " + khachHangDTO.getEmail());
        }

        // Kiểm tra số điện thoại đã tồn tại (trừ khách hàng hiện tại)
        if (khachHangRepository.existsBySoDienThoaiAndIdNot(khachHangDTO.getSoDienThoai(), id)) {
            throw new RuntimeException("Số điện thoại đã tồn tại: " + khachHangDTO.getSoDienThoai());
        }

        // Kiểm tra mã khách hàng đã tồn tại (trừ khách hàng hiện tại)
        if (khachHangRepository.existsByMaKhachHangAndIdNot(khachHangDTO.getMaKhachHang(), id)) {
            throw new RuntimeException("Mã khách hàng đã tồn tại: " + khachHangDTO.getMaKhachHang());
        }

        // Cập nhật thông tin
        existingKhachHang.setMaKhachHang(khachHangDTO.getMaKhachHang());
        existingKhachHang.setTenKhachHang(khachHangDTO.getTenKhachHang());
        existingKhachHang.setEmail(khachHangDTO.getEmail());
        existingKhachHang.setSoDienThoai(khachHangDTO.getSoDienThoai());
        existingKhachHang.setDiaChi(khachHangDTO.getDiaChi());
        existingKhachHang.setNgaySinh(khachHangDTO.getNgaySinh());
        existingKhachHang.setGioiTinh(khachHangDTO.getGioiTinh());
        existingKhachHang.setTrangThai(khachHangDTO.getTrangThai());

        KhachHang updatedKhachHang = khachHangRepository.save(existingKhachHang);
        return convertToDTO(updatedKhachHang);
    }

    // Xóa vĩnh viễn khách hàng (hard delete)
    public void deleteKhachHang(Long id) {
        if (!khachHangRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khách hàng với ID: " + id);
        }
        khachHangRepository.deleteById(id);
    }
    
    // Xóa mềm khách hàng (cập nhật trạng thái thành không hoạt động)
    public void softDeleteKhachHang(Long id) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));
        
        khachHang.setTrangThai(false);
        khachHangRepository.save(khachHang);
    }

    // Xóa cứng khách hàng
    public void deleteKhachHangPermanently(Long id) {
        if (!khachHangRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khách hàng với ID: " + id);
        }
        khachHangRepository.deleteById(id);
    }

    // Kiểm tra email đã tồn tại
    public boolean checkEmailExists(String email) {
        return khachHangRepository.findByEmail(email).isPresent();
    }

    // Kiểm tra số điện thoại đã tồn tại
    public boolean checkSoDienThoaiExists(String soDienThoai) {
        return khachHangRepository.findBySoDienThoai(soDienThoai).isPresent();
    }

    // Kiểm tra mã khách hàng đã tồn tại
    public boolean existsByMaKhachHang(String maKhachHang) {
        return khachHangRepository.existsByMaKhachHang(maKhachHang);
    }

    // Lấy khách hàng theo mã khách hàng
    public Optional<KhachHangDTO> getKhachHangByMaKhachHang(String maKhachHang) {
        return khachHangRepository.findByMaKhachHang(maKhachHang)
                .map(this::convertToDTO);
    }

    public boolean checkMaKhachHangExists(String maKhachHang) {
        return khachHangRepository.findByMaKhachHang(maKhachHang).isPresent();
    }

    // Lấy thống kê
    public long getTotalKhachHang() {
        return khachHangRepository.count();
    }

    public long getActiveKhachHang() {
        return khachHangRepository.countByTrangThai(true);
    }

    public long getInactiveKhachHang() {
        return khachHangRepository.countByTrangThai(false);
    }


    // Tạo mã khách hàng tự động
    private String generateMaKhachHang() {
        String prefix = "KH";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + timestamp.substring(timestamp.length() - 6);
    }

    // Convert Entity to DTO
    private KhachHangDTO convertToDTO(KhachHang khachHang) {
        return KhachHangDTO.builder()
                .id(khachHang.getId())
                .maKhachHang(khachHang.getMaKhachHang())
                .tenKhachHang(khachHang.getTenKhachHang())
                .email(khachHang.getEmail())
                .soDienThoai(khachHang.getSoDienThoai())
                .diaChi(khachHang.getDiaChi())
                .ngaySinh(khachHang.getNgaySinh())
                .gioiTinh(khachHang.getGioiTinh())
                .ngayTao(khachHang.getNgayTao())
                .trangThai(khachHang.getTrangThai())
                .soLanMua(khachHang.getSoLanMua())
                .diemTichLuy(khachHang.getDiemTichLuy())
                .lanMuaGanNhat(khachHang.getLanMuaGanNhat())
                .userId(khachHang.getUser() != null ? khachHang.getUser().getId() : null)
                .username(khachHang.getUser() != null ? khachHang.getUser().getUsername() : null)
                .build();
    }

    // Convert Entity to DTO with default address
    private KhachHangDTO convertToDTOWithAddress(KhachHang khachHang) {
        KhachHangDTO dto = convertToDTO(khachHang);
        
        // Load địa chỉ từ service riêng
        try {
            List<DiaChiKhachHangDTO> addresses = diaChiKhachHangService.getDiaChiByKhachHangId(khachHang.getId());
            if (addresses != null && !addresses.isEmpty()) {
                // Tìm địa chỉ mặc định
                Optional<DiaChiKhachHangDTO> defaultAddress = addresses.stream()
                    .filter(addr -> addr.getMacDinh() != null && addr.getMacDinh())
                    .findFirst();
                
                if (defaultAddress.isPresent()) {
                    DiaChiKhachHangDTO addr = defaultAddress.get();
                    dto.setDiaChiMacDinh(addr.getDiaChiChiTiet());
                    dto.setTinhThanhMacDinh(addr.getTinhThanh());
                    dto.setQuanHuyenMacDinh(addr.getQuanHuyen());
                    dto.setPhuongXaMacDinh(addr.getPhuongXa());
                    dto.setCoDiaChiMacDinh(true);
                } else {
                    // Nếu không có địa chỉ mặc định, lấy địa chỉ đầu tiên
                    DiaChiKhachHangDTO firstAddr = addresses.get(0);
                    dto.setDiaChiMacDinh(firstAddr.getDiaChiChiTiet());
                    dto.setTinhThanhMacDinh(firstAddr.getTinhThanh());
                    dto.setQuanHuyenMacDinh(firstAddr.getQuanHuyen());
                    dto.setPhuongXaMacDinh(firstAddr.getPhuongXa());
                    dto.setCoDiaChiMacDinh(true);
                }
            } else {
                dto.setCoDiaChiMacDinh(false);
            }
        } catch (Exception e) {
            dto.setCoDiaChiMacDinh(false);
        }
        
        return dto;
    }

    // Convert DTO to Entity
    private KhachHang convertToEntity(KhachHangDTO khachHangDTO) {
        KhachHang khachHang = new KhachHang();

        khachHang.setId(khachHangDTO.getId());

        khachHang.setMaKhachHang(khachHangDTO.getMaKhachHang());
        khachHang.setTenKhachHang(khachHangDTO.getTenKhachHang());
        khachHang.setEmail(khachHangDTO.getEmail());
        khachHang.setSoDienThoai(khachHangDTO.getSoDienThoai());
        khachHang.setDiaChi(khachHangDTO.getDiaChi());
        khachHang.setNgaySinh(khachHangDTO.getNgaySinh());
        khachHang.setGioiTinh(khachHangDTO.getGioiTinh());
        khachHang.setNgayTao(khachHangDTO.getNgayTao());
        khachHang.setTrangThai(khachHangDTO.getTrangThai());

        khachHang.setSoLanMua(khachHangDTO.getSoLanMua());
        return khachHang;
    }

    // Update Entity from DTO
    private void updateEntityFromDTO(KhachHang khachHang, KhachHangDTO khachHangDTO) {
        if (khachHangDTO.getMaKhachHang() != null) {
            khachHang.setMaKhachHang(khachHangDTO.getMaKhachHang());
        }
        if (khachHangDTO.getTenKhachHang() != null) {
            khachHang.setTenKhachHang(khachHangDTO.getTenKhachHang());
        }
        if (khachHangDTO.getEmail() != null) {
            khachHang.setEmail(khachHangDTO.getEmail());
        }
        if (khachHangDTO.getSoDienThoai() != null) {
            khachHang.setSoDienThoai(khachHangDTO.getSoDienThoai());
        }
        if (khachHangDTO.getNgaySinh() != null) {
            khachHang.setNgaySinh(khachHangDTO.getNgaySinh());
        }
        if (khachHangDTO.getGioiTinh() != null) {
            khachHang.setGioiTinh(khachHangDTO.getGioiTinh());
        }
        if (khachHangDTO.getTrangThai() != null) {
            khachHang.setTrangThai(khachHangDTO.getTrangThai());
        }
        if (khachHangDTO.getSoLanMua() != null) {
            khachHang.setSoLanMua(khachHangDTO.getSoLanMua());
        }
    }
    
    // Lấy danh sách khách hàng cho form phiếu giảm giá
    public List<KhachHangDTO> getAllCustomersForVoucher() {
        List<KhachHang> khachHangList = khachHangRepository.findAll();
        return khachHangList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Đếm tổng số khách hàng
    public long getTotalCustomerCount() {
        return khachHangRepository.count();
    }
}
