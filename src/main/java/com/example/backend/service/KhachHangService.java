package com.example.backend.service;

import com.example.backend.dto.KhachHangDTO;
import com.example.backend.dto.DiaChiKhachHangDTO;
import com.example.backend.entity.KhachHang;
import com.example.backend.entity.User;
import com.example.backend.repository.KhachHangRepository;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    // Lấy khách hàng theo ID (chi tiết đầy đủ bao gồm địa chỉ)
    public Optional<KhachHangDTO> getKhachHangById(Long id) {
        return khachHangRepository.findById(id)
                .map(this::convertToDTOWithAddress);
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
    // Lấy khách hàng theo User ID (bao gồm địa chỉ mặc định)
    public Optional<KhachHangDTO> getKhachHangByUserId(Long userId) {
        return khachHangRepository.findByUserId(userId)
                .map(this::convertToDTOWithAddress);
    }
    
    /**
     * Tạo KhachHang từ User (dùng khi user chưa có record trong bảng khach_hang)
     */
    public KhachHangDTO createKhachHangFromUser(User user) {
        log.info("🔄 Tạo KhachHang từ User: {} (ID: {})", user.getUsername(), user.getId());
        
        // Kiểm tra xem đã có KhachHang chưa (theo user_id)
        Optional<KhachHang> existingKhachHangByUserId = khachHangRepository.findByUserId(user.getId());
        if (existingKhachHangByUserId.isPresent()) {
            log.info("✅ KhachHang đã tồn tại cho user: {} (ID: {})", user.getUsername(), existingKhachHangByUserId.get().getId());
            return convertToDTO(existingKhachHangByUserId.get());
        }
        
        // Kiểm tra xem có KhachHang nào có email trùng với user nhưng chưa có user_id không
        // (có thể là orphan record từ lần đăng ký trước)
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            Optional<KhachHang> existingByEmail = khachHangRepository.findByEmail(user.getEmail());
            if (existingByEmail.isPresent()) {
                KhachHang existingKh = existingByEmail.get();
                // Nếu KhachHang này chưa có user_id, thì update user_id cho nó
                if (existingKh.getUser() == null) {
                    log.info("🔄 Tìm thấy KhachHang orphan với email {}, đang cập nhật user_id...", user.getEmail());
                    existingKh.setUser(user);
                    // Cập nhật thông tin từ user nếu cần
                    if (existingKh.getTenKhachHang() == null || existingKh.getTenKhachHang().trim().isEmpty()) {
                        existingKh.setTenKhachHang(user.getFullName() != null && !user.getFullName().isBlank() 
                                ? user.getFullName() : user.getUsername());
                    }
                    KhachHang savedKhachHang = khachHangRepository.save(existingKh);
                    log.info("✅ Đã cập nhật KhachHang orphan thành công: {} (ID: {})", 
                            savedKhachHang.getTenKhachHang(), savedKhachHang.getId());
                    return convertToDTO(savedKhachHang);
                } else if (!existingKh.getUser().getId().equals(user.getId())) {
                    // Email đã được sử dụng bởi KhachHang khác với user khác
                    log.warn("⚠️ Email {} đã được sử dụng bởi KhachHang khác (ID: {}, User ID: {}), sẽ tạo KhachHang mới không có email", 
                            user.getEmail(), existingKh.getId(), existingKh.getUser().getId());
                    // Tiếp tục tạo KhachHang mới nhưng không set email
                }
            }
        }
        
        // Tạo KhachHang mới
        KhachHang khachHang = new KhachHang();
        khachHang.setTenKhachHang(user.getFullName() != null && !user.getFullName().isBlank() 
                ? user.getFullName() : user.getUsername());
        
        // Set email nếu chưa bị sử dụng bởi KhachHang khác
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            Optional<KhachHang> existingByEmail = khachHangRepository.findByEmail(user.getEmail());
            if (existingByEmail.isEmpty()) {
                // Email chưa tồn tại, set email
                khachHang.setEmail(user.getEmail());
            } else {
                KhachHang existingKh = existingByEmail.get();
                // Nếu email đã được sử dụng bởi KhachHang khác (khác user), không set email
                if (existingKh.getUser() != null && !existingKh.getUser().getId().equals(user.getId())) {
                    log.warn("⚠️ Email {} đã được sử dụng bởi KhachHang khác, không set email cho KhachHang mới", user.getEmail());
                    khachHang.setEmail(null); // Không set email để tránh unique constraint violation
                } else {
                    // Email chưa được sử dụng hoặc là của user này (đã xử lý ở trên), set email
                    khachHang.setEmail(user.getEmail());
                }
            }
        }
        
        khachHang.setSoDienThoai(null);
        khachHang.setTrangThai(true);
        khachHang.setNgayTao(LocalDate.now());
        khachHang.setUser(user); // Liên kết với user
        
        // Tạo mã khách hàng unique
        String mkh;
        int attempts = 0;
        do {
            mkh = "KH" + System.currentTimeMillis() + (attempts > 0 ? "_" + attempts : "");
            attempts++;
            if (attempts > 10) {
                mkh = "KH" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
                break;
            }
        } while (khachHangRepository.existsByMaKhachHang(mkh));
        
        khachHang.setMaKhachHang(mkh);
        
        // Set các giá trị mặc định
        khachHang.setSoLanMua(0);
        khachHang.setDiemTichLuy(0);
        // lanMuaGanNhat có thể null
        
        // Save KhachHang
        try {
            KhachHang savedKhachHang = khachHangRepository.saveAndFlush(khachHang);
            log.info("✅ Đã tạo KhachHang thành công: {} (ID: {}, maKhachHang: {})", 
                    savedKhachHang.getTenKhachHang(), savedKhachHang.getId(), savedKhachHang.getMaKhachHang());
            return convertToDTO(savedKhachHang);
        } catch (Exception ex) {
            log.error("❌ Lỗi khi save KhachHang: {}", ex.getMessage(), ex);
            log.error("   - KhachHang details: tenKhachHang={}, email={}, maKhachHang={}, user_id={}", 
                    khachHang.getTenKhachHang(), khachHang.getEmail(), khachHang.getMaKhachHang(), 
                    khachHang.getUser() != null ? khachHang.getUser().getId() : null);
            throw new RuntimeException("Không thể tạo thông tin khách hàng: " + ex.getMessage(), ex);
        }
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

        // Kiểm tra email đã tồn tại (trừ khách hàng hiện tại) - chỉ kiểm tra nếu có giá trị
        if (khachHangDTO.getEmail() != null && !khachHangDTO.getEmail().trim().isEmpty()) {
            if (khachHangRepository.existsByEmailAndIdNot(khachHangDTO.getEmail(), id)) {
                throw new RuntimeException("Email đã tồn tại: " + khachHangDTO.getEmail());
            }
        }

        // Kiểm tra số điện thoại đã tồn tại (trừ khách hàng hiện tại) - chỉ kiểm tra nếu có giá trị
        if (khachHangDTO.getSoDienThoai() != null && !khachHangDTO.getSoDienThoai().trim().isEmpty()) {
            if (khachHangRepository.existsBySoDienThoaiAndIdNot(khachHangDTO.getSoDienThoai(), id)) {
                throw new RuntimeException("Số điện thoại đã tồn tại: " + khachHangDTO.getSoDienThoai());
            }
        }

        // Không kiểm tra mã khách hàng vì frontend không gửi (giữ nguyên mã hiện tại)

        // Cập nhật thông tin - chỉ cập nhật các trường được gửi từ frontend
        // Không cập nhật maKhachHang (giữ nguyên mã khách hàng hiện tại)
        if (khachHangDTO.getTenKhachHang() != null) {
            existingKhachHang.setTenKhachHang(khachHangDTO.getTenKhachHang());
        }
        if (khachHangDTO.getEmail() != null) {
            existingKhachHang.setEmail(khachHangDTO.getEmail());
        }
        if (khachHangDTO.getSoDienThoai() != null) {
            existingKhachHang.setSoDienThoai(khachHangDTO.getSoDienThoai());
        }
        if (khachHangDTO.getDiaChi() != null) {
            existingKhachHang.setDiaChi(khachHangDTO.getDiaChi());
        }
        if (khachHangDTO.getNgaySinh() != null) {
            existingKhachHang.setNgaySinh(khachHangDTO.getNgaySinh());
        }
        if (khachHangDTO.getGioiTinh() != null) {
            existingKhachHang.setGioiTinh(khachHangDTO.getGioiTinh());
        }
        // Không cập nhật trangThai từ frontend (chỉ admin/staff mới được cập nhật)
        // if (khachHangDTO.getTrangThai() != null) {
        //     existingKhachHang.setTrangThai(khachHangDTO.getTrangThai());
        // }

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
