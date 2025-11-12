package com.example.backend.service;

import com.example.backend.dto.NhanVienDTO;
import com.example.backend.entity.NhanVien;
import com.example.backend.entity.User;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 12;

    // Lấy tất cả nhân viên với phân trang
    public Page<NhanVienDTO> getAllNhanVien(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<NhanVien> nhanVienPage = nhanVienRepository.findAll(pageable);
        return nhanVienPage.map(this::convertToDTO);
    }

    // Lấy nhân viên theo ID
    public Optional<NhanVienDTO> getNhanVienById(Long id) {
        return nhanVienRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Tìm kiếm nhân viên với bộ lọc
    public Page<NhanVienDTO> searchNhanVien(String hoTen, String email, String soDienThoai, 
                                           String maNhanVien, Boolean trangThai, 
                                           int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<NhanVien> nhanVienPage = nhanVienRepository.findWithFilters(
            hoTen, email, soDienThoai, maNhanVien, trangThai, pageable);
        
        return nhanVienPage.map(this::convertToDTO);
    }

    // Tạo nhân viên mới
    public NhanVienDTO createNhanVien(NhanVienDTO nhanVienDTO) {
        // Kiểm tra mã nhân viên đã tồn tại
        if (nhanVienRepository.findByMaNhanVien(nhanVienDTO.getMaNhanVien()).isPresent()) {
            throw new RuntimeException("Mã nhân viên đã tồn tại");
        }
        
        // Kiểm tra email đã tồn tại
        if (nhanVienDTO.getEmail() == null || nhanVienDTO.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống");
        }
        
        if (nhanVienRepository.findByEmail(nhanVienDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
        
        // Kiểm tra email đã tồn tại trong User
        if (userRepository.existsByEmail(nhanVienDTO.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng cho tài khoản khác");
        }
        
        // Kiểm tra số điện thoại đã tồn tại
        if (nhanVienDTO.getSoDienThoai() != null && nhanVienRepository.findBySoDienThoai(nhanVienDTO.getSoDienThoai()).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        
        // Tạo User cho nhân viên
        String username = generateUsername(nhanVienDTO.getEmail(), nhanVienDTO.getMaNhanVien());
        String plainPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(plainPassword);
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setEmail(nhanVienDTO.getEmail());
        user.setFullName(nhanVienDTO.getHoTen());
        user.addRole(User.UserRole.STAFF);
        
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(username)) {
            // Nếu username đã tồn tại, thêm số vào cuối
            int counter = 1;
            String baseUsername = username;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter;
                counter++;
            }
            user.setUsername(username);
        }
        
        User savedUser = userRepository.save(user);
        
        // Tạo nhân viên và liên kết với User
        NhanVien nhanVien = convertToEntity(nhanVienDTO);
        nhanVien.setNgayVaoLam(LocalDate.now());
        nhanVien.setTrangThai(true);
        nhanVien.setUser(savedUser);
        
        NhanVien savedNhanVien = nhanVienRepository.save(nhanVien);
        
        // Gửi email thông tin tài khoản (async, không ảnh hưởng đến response)
        try {
            emailService.sendEmployeeAccountInfo(
                nhanVienDTO.getEmail(),
                nhanVienDTO.getHoTen(),
                username,
                plainPassword,
                nhanVienDTO.getMaNhanVien()
            );
            log.info("✅ Email thông tin tài khoản đã được gửi tới: {}", nhanVienDTO.getEmail());
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi email thông tin tài khoản tới {}: {}", nhanVienDTO.getEmail(), e.getMessage());
            // Không throw exception để không ảnh hưởng đến việc tạo nhân viên
        }
        
        return convertToDTO(savedNhanVien);
    }

    // Cập nhật nhân viên
    public Optional<NhanVienDTO> updateNhanVien(Long id, NhanVienDTO nhanVienDTO) {
        return nhanVienRepository.findById(id)
                .map(existingNhanVien -> {
                    // Kiểm tra mã nhân viên trùng lặp (trừ chính nó)
                    if (nhanVienDTO.getMaNhanVien() != null && 
                        !existingNhanVien.getMaNhanVien().equals(nhanVienDTO.getMaNhanVien())) {
                        Optional<NhanVien> existingByMa = nhanVienRepository.findByMaNhanVien(nhanVienDTO.getMaNhanVien());
                        if (existingByMa.isPresent() && !existingByMa.get().getId().equals(id)) {
                            throw new RuntimeException("Mã nhân viên đã tồn tại");
                        }
                    }
                    
                    // Kiểm tra email trùng lặp (trừ chính nó)
                    if (nhanVienDTO.getEmail() != null && 
                        !existingNhanVien.getEmail().equals(nhanVienDTO.getEmail())) {
                        Optional<NhanVien> existingByEmail = nhanVienRepository.findByEmail(nhanVienDTO.getEmail());
                        if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(id)) {
                            throw new RuntimeException("Email đã tồn tại");
                        }
                    }
                    
                    // Kiểm tra số điện thoại trùng lặp (trừ chính nó)
                    if (nhanVienDTO.getSoDienThoai() != null && 
                        !existingNhanVien.getSoDienThoai().equals(nhanVienDTO.getSoDienThoai())) {
                        Optional<NhanVien> existingByPhone = nhanVienRepository.findBySoDienThoai(nhanVienDTO.getSoDienThoai());
                        if (existingByPhone.isPresent() && !existingByPhone.get().getId().equals(id)) {
                            throw new RuntimeException("Số điện thoại đã tồn tại");
                        }
                    }
                    
                    updateEntityFromDTO(existingNhanVien, nhanVienDTO);
                    NhanVien updatedNhanVien = nhanVienRepository.save(existingNhanVien);
                    return convertToDTO(updatedNhanVien);
                });
    }

    // Xóa nhân viên (soft delete)
    public boolean deleteNhanVien(Long id) {
        return nhanVienRepository.findById(id)
                .map(nhanVien -> {
                    nhanVien.setTrangThai(false);
                    nhanVienRepository.save(nhanVien);
                    return true;
                })
                .orElse(false);
    }

    // Xóa vĩnh viễn nhân viên
    public boolean permanentlyDeleteNhanVien(Long id) {
        if (nhanVienRepository.existsById(id)) {
            nhanVienRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Kiểm tra email đã tồn tại
    public boolean existsByEmail(String email) {
        return nhanVienRepository.findByEmail(email).isPresent();
    }

    // Kiểm tra số điện thoại đã tồn tại
    public boolean existsBySoDienThoai(String soDienThoai) {
        return nhanVienRepository.findBySoDienThoai(soDienThoai).isPresent();
    }

    // Kiểm tra mã nhân viên đã tồn tại
    public boolean existsByMaNhanVien(String maNhanVien) {
        return nhanVienRepository.findByMaNhanVien(maNhanVien).isPresent();
    }

    // Lấy nhân viên theo email
    public Optional<NhanVienDTO> getNhanVienByEmail(String email) {
        return nhanVienRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    // Lấy nhân viên theo số điện thoại
    public Optional<NhanVienDTO> getNhanVienBySoDienThoai(String soDienThoai) {
        return nhanVienRepository.findBySoDienThoai(soDienThoai)
                .map(this::convertToDTO);
    }

    // Lấy nhân viên theo mã nhân viên
    public Optional<NhanVienDTO> getNhanVienByMaNhanVien(String maNhanVien) {
        return nhanVienRepository.findByMaNhanVien(maNhanVien)
                .map(this::convertToDTO);
    }

    // Lấy nhân viên theo trạng thái
    public List<NhanVienDTO> getNhanVienByTrangThai(Boolean trangThai) {
        List<NhanVien> nhanVienList = nhanVienRepository.findByTrangThai(trangThai);
        return nhanVienList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Thống kê nhân viên
    public long countNhanVienByTrangThai(Boolean trangThai) {
        return nhanVienRepository.countByTrangThai(trangThai);
    }

    // Lấy nhân viên theo khoảng thời gian vào làm
    public List<NhanVienDTO> getNhanVienByDateRange(LocalDate startDate, LocalDate endDate) {
        List<NhanVien> nhanVienList = nhanVienRepository.findByNgayVaoLamRange(startDate, endDate);
        return nhanVienList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tạo mã nhân viên tự động
    public String generateMaNhanVien() {
        String prefix = "NV";
        long count = nhanVienRepository.count();
        return prefix + String.format("%04d", count + 1);
    }

    // Tạo username từ email hoặc mã nhân viên
    private String generateUsername(String email, String maNhanVien) {
        if (email != null && !email.trim().isEmpty()) {
            // Lấy phần trước @ của email
            String username = email.split("@")[0];
            // Loại bỏ các ký tự đặc biệt, chỉ giữ chữ và số
            username = username.replaceAll("[^a-zA-Z0-9]", "");
            return username.toLowerCase();
        } else if (maNhanVien != null && !maNhanVien.trim().isEmpty()) {
            return maNhanVien.toLowerCase();
        } else {
            throw new RuntimeException("Không thể tạo username: email và mã nhân viên đều trống");
        }
    }

    // Tạo mật khẩu ngẫu nhiên
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        
        return password.toString();
    }

    // Convert Entity to DTO
    private NhanVienDTO convertToDTO(NhanVien nhanVien) {
        return NhanVienDTO.builder()
                .id(nhanVien.getId())
                .hoTen(nhanVien.getHoTen())
                .maNhanVien(nhanVien.getMaNhanVien())
                .email(nhanVien.getEmail())
                .soDienThoai(nhanVien.getSoDienThoai())
                .soCanCuocCongDan(nhanVien.getSoCanCuocCongDan())
                .diaChi(nhanVien.getDiaChi())
                .gioiTinh(nhanVien.getGioiTinh())
                .ngaySinh(nhanVien.getNgaySinh())
                .ngayVaoLam(nhanVien.getNgayVaoLam())
                .trangThai(nhanVien.getTrangThai())
                .userId(nhanVien.getUser() != null ? nhanVien.getUser().getId() : null)
                .username(nhanVien.getUser() != null ? nhanVien.getUser().getUsername() : null)
                .fullName(nhanVien.getUser() != null ? nhanVien.getUser().getFullName() : null)
                .build();
    }

    // Convert DTO to Entity
    private NhanVien convertToEntity(NhanVienDTO nhanVienDTO) {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setHoTen(nhanVienDTO.getHoTen());
        nhanVien.setMaNhanVien(nhanVienDTO.getMaNhanVien());
        nhanVien.setEmail(nhanVienDTO.getEmail());
        nhanVien.setSoDienThoai(nhanVienDTO.getSoDienThoai());
        nhanVien.setSoCanCuocCongDan(nhanVienDTO.getSoCanCuocCongDan());
        nhanVien.setDiaChi(nhanVienDTO.getDiaChi());
        nhanVien.setGioiTinh(nhanVienDTO.getGioiTinh());
        nhanVien.setNgaySinh(nhanVienDTO.getNgaySinh());
        nhanVien.setNgayVaoLam(nhanVienDTO.getNgayVaoLam());
        nhanVien.setTrangThai(nhanVienDTO.getTrangThai());
        return nhanVien;
    }

    // Update Entity from DTO
    private void updateEntityFromDTO(NhanVien nhanVien, NhanVienDTO nhanVienDTO) {
        if (nhanVienDTO.getHoTen() != null) {
            nhanVien.setHoTen(nhanVienDTO.getHoTen());
        }
        if (nhanVienDTO.getMaNhanVien() != null) {
            nhanVien.setMaNhanVien(nhanVienDTO.getMaNhanVien());
        }
        if (nhanVienDTO.getEmail() != null) {
            nhanVien.setEmail(nhanVienDTO.getEmail());
        }
        if (nhanVienDTO.getSoDienThoai() != null) {
            nhanVien.setSoDienThoai(nhanVienDTO.getSoDienThoai());
        }
        if (nhanVienDTO.getSoCanCuocCongDan() != null) {
            nhanVien.setSoCanCuocCongDan(nhanVienDTO.getSoCanCuocCongDan());
        }
        if (nhanVienDTO.getDiaChi() != null) {
            nhanVien.setDiaChi(nhanVienDTO.getDiaChi());
        }
        if (nhanVienDTO.getGioiTinh() != null) {
            nhanVien.setGioiTinh(nhanVienDTO.getGioiTinh());
        }
        if (nhanVienDTO.getNgaySinh() != null) {
            nhanVien.setNgaySinh(nhanVienDTO.getNgaySinh());
        }
        if (nhanVienDTO.getNgayVaoLam() != null) {
            nhanVien.setNgayVaoLam(nhanVienDTO.getNgayVaoLam());
        }
        if (nhanVienDTO.getTrangThai() != null) {
            nhanVien.setTrangThai(nhanVienDTO.getTrangThai());
        }
    }
}
