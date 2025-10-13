package com.example.backend.service;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.KhachHang;
import com.example.backend.entity.NhanVien;
import com.example.backend.entity.DiaChiKhachHang;
import com.example.backend.repository.HoaDonRepository;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.DiaChiKhachHangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;

    public HoaDonService(HoaDonRepository hoaDonRepository, 
                        KhachHangRepository khachHangRepository,
                        NhanVienRepository nhanVienRepository,
                        DiaChiKhachHangRepository diaChiKhachHangRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
    }

    // Expose repositories for controller access
    public KhachHangRepository getKhachHangRepository() {
        return khachHangRepository;
    }

    public NhanVienRepository getNhanVienRepository() {
        return nhanVienRepository;
    }

    public List<HoaDonDTO> getAllHoaDon() {
        return hoaDonRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public HoaDonDTO getHoaDonById(Long id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));
        return convertToDTO(hoaDon);
    }
    
    public HoaDonDTO getHoaDonByMa(String maHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon));
        return convertToDTO(hoaDon);
    }
    
    public List<HoaDonDTO> getHoaDonByTrangThai(HoaDon.TrangThaiHoaDon trangThai) {
        return hoaDonRepository.findByTrangThai(trangThai)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<HoaDonDTO> getHoaDonByKhachHang(Long khachHangId) {
        return hoaDonRepository.findByKhachHangId(khachHangId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<HoaDonDTO> getHoaDonByNhanVien(Long nhanVienId) {
        return hoaDonRepository.findByNhanVienId(nhanVienId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<HoaDonDTO> getHoaDonByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return hoaDonRepository.findByNgayTaoBetween(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public HoaDonDTO createHoaDon(HoaDonDTO hoaDonDTO) {
        HoaDon hoaDon = convertToEntity(hoaDonDTO);
        hoaDon.setNgayTao(LocalDateTime.now());
        
        // Generate mã hóa đơn nếu chưa có
        if (hoaDon.getMaHoaDon() == null || hoaDon.getMaHoaDon().isEmpty()) {
            hoaDon.setMaHoaDon(generateMaHoaDon());
        }
        
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        return convertToDTO(savedHoaDon);
    }

    @Transactional
    public HoaDonDTO updateHoaDon(Long id, HoaDonDTO hoaDonDTO) {
        HoaDon existingHoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));
        
        // Update fields
        existingHoaDon.setMaHoaDon(hoaDonDTO.getMaHoaDon());
        existingHoaDon.setNgayThanhToan(hoaDonDTO.getNgayThanhToan());
        existingHoaDon.setTongTien(hoaDonDTO.getTongTien());
        existingHoaDon.setTienGiamGia(hoaDonDTO.getTienGiamGia());
        existingHoaDon.setThanhTien(hoaDonDTO.getThanhTien());
        existingHoaDon.setGhiChu(hoaDonDTO.getGhiChu());
        existingHoaDon.setTrangThai(hoaDonDTO.getTrangThai());
        
        // Update relationships - xử lý khách hàng
        if (hoaDonDTO.getKhachHangId() != null) {
            // Nếu có ID khách hàng, tìm khách hàng theo ID
            KhachHang khachHang = khachHangRepository.findById(hoaDonDTO.getKhachHangId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + hoaDonDTO.getKhachHangId()));
            existingHoaDon.setKhachHang(khachHang);
        } else if (hoaDonDTO.getTenKhachHang() != null && !hoaDonDTO.getTenKhachHang().isEmpty()) {
            // Nếu không có ID nhưng có tên khách hàng, tạo hoặc tìm khách hàng mới
            KhachHang khachHang = createOrFindKhachHang(hoaDonDTO);
            existingHoaDon.setKhachHang(khachHang);
        }
        
        // Update relationships - xử lý nhân viên
        if (hoaDonDTO.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(hoaDonDTO.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + hoaDonDTO.getNhanVienId()));
            existingHoaDon.setNhanVien(nhanVien);
        } else if (hoaDonDTO.getTenNhanVien() != null && !hoaDonDTO.getTenNhanVien().isEmpty()) {
            // Nếu không có ID nhưng có tên nhân viên, tạo hoặc tìm nhân viên mới
            NhanVien nhanVien = createOrFindNhanVien(hoaDonDTO);
            existingHoaDon.setNhanVien(nhanVien);
        }
        
        HoaDon updatedHoaDon = hoaDonRepository.save(existingHoaDon);
        return convertToDTO(updatedHoaDon);
    }

    @Transactional
    public void deleteHoaDon(Long id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));
        hoaDonRepository.delete(hoaDon);
    }

    @Transactional
    public HoaDonDTO updateTrangThaiHoaDon(Long id, HoaDon.TrangThaiHoaDon trangThai) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));
        
        hoaDon.setTrangThai(trangThai);
        
        // Nếu trạng thái là DA_GIAO_HANG thì cập nhật ngày thanh toán
        if (trangThai == HoaDon.TrangThaiHoaDon.DA_GIAO_HANG && hoaDon.getNgayThanhToan() == null) {
            hoaDon.setNgayThanhToan(LocalDateTime.now());
        }
        
        HoaDon updatedHoaDon = hoaDonRepository.save(hoaDon);
        return convertToDTO(updatedHoaDon);
    }

    private String generateMaHoaDon() {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        String time = String.format("%02d%02d%02d", now.getHour(), now.getMinute(), now.getSecond());
        return "HD" + year + month + day + time;
    }

    private HoaDon convertToEntity(HoaDonDTO dto) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(dto.getId());
        hoaDon.setMaHoaDon(dto.getMaHoaDon());
        hoaDon.setNgayTao(dto.getNgayTao());
        hoaDon.setNgayThanhToan(dto.getNgayThanhToan());
        hoaDon.setTongTien(dto.getTongTien());
        hoaDon.setTienGiamGia(dto.getTienGiamGia());
        hoaDon.setThanhTien(dto.getThanhTien());
        hoaDon.setGhiChu(dto.getGhiChu());
        hoaDon.setTrangThai(dto.getTrangThai());
        
        // Xử lý khách hàng
        if (dto.getKhachHangId() != null) {
            // Nếu có ID khách hàng, tìm khách hàng theo ID
            KhachHang khachHang = khachHangRepository.findById(dto.getKhachHangId()).orElse(null);
            hoaDon.setKhachHang(khachHang);
        } else if (dto.getTenKhachHang() != null && !dto.getTenKhachHang().isEmpty()) {
            // Nếu không có ID nhưng có tên khách hàng, tạo khách hàng mới
            KhachHang khachHang = createOrFindKhachHang(dto);
            hoaDon.setKhachHang(khachHang);
        }
        
        // Xử lý nhân viên
        if (dto.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(dto.getNhanVienId()).orElse(null);
            hoaDon.setNhanVien(nhanVien);
        } else if (dto.getTenNhanVien() != null && !dto.getTenNhanVien().isEmpty()) {
            // Nếu không có ID nhưng có tên nhân viên, tạo nhân viên mới
            NhanVien nhanVien = createOrFindNhanVien(dto);
            hoaDon.setNhanVien(nhanVien);
        }
        
        return hoaDon;
    }

    private HoaDonDTO convertToDTO(HoaDon hoaDon) {
        return HoaDonDTO.builder()
                .id(hoaDon.getId())
                .maHoaDon(hoaDon.getMaHoaDon())
                .khachHangId(hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null)
                .tenKhachHang(hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getTenKhachHang() : null)
                .emailKhachHang(hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getEmail() : null)
                .soDienThoaiKhachHang(hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getSoDienThoai() : null)
                .diaChiKhachHang(getDiaChiKhachHang(hoaDon.getKhachHang()))
                .nhanVienId(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getId() : null)
                .tenNhanVien(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getHoTen() : null)
                .ngayTao(hoaDon.getNgayTao())
                .ngayThanhToan(hoaDon.getNgayThanhToan())
                .tongTien(hoaDon.getTongTien())
                .tienGiamGia(hoaDon.getTienGiamGia())
                .thanhTien(hoaDon.getThanhTien())
                .ghiChu(hoaDon.getGhiChu())
                .trangThai(hoaDon.getTrangThai())
                .soLuongSanPham(hoaDon.getDanhSachChiTiet() != null ? hoaDon.getDanhSachChiTiet().size() : 0)
                .build();
    }

    private String buildFullAddress(DiaChiKhachHang diaChi) {
        if (diaChi == null) return null;
        
        StringBuilder address = new StringBuilder();
        if (diaChi.getDiaChi() != null && !diaChi.getDiaChi().trim().isEmpty()) {
            address.append(diaChi.getDiaChi().trim());
        }
        if (diaChi.getPhuongXa() != null && !diaChi.getPhuongXa().trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(diaChi.getPhuongXa().trim());
        }
        if (diaChi.getQuanHuyen() != null && !diaChi.getQuanHuyen().trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(diaChi.getQuanHuyen().trim());
        }
        if (diaChi.getTinhThanh() != null && !diaChi.getTinhThanh().trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(diaChi.getTinhThanh().trim());
        }
        
        return address.length() > 0 ? address.toString() : null;
    }

    private String getDiaChiKhachHang(KhachHang khachHang) {
        if (khachHang == null || khachHang.getId() == null) {
            return null;
        }
        
        // Tìm địa chỉ mặc định của khách hàng
        List<DiaChiKhachHang> diaChiMacDinhList = diaChiKhachHangRepository.findDiaChiMacDinhByKhachHangId(khachHang.getId());
        if (!diaChiMacDinhList.isEmpty()) {
            return buildFullAddress(diaChiMacDinhList.get(0));
        }
        
        // Nếu không có địa chỉ mặc định, lấy địa chỉ đầu tiên
        List<DiaChiKhachHang> danhSachDiaChi = diaChiKhachHangRepository.findDiaChiActiveByKhachHangId(khachHang.getId());
        if (!danhSachDiaChi.isEmpty()) {
            return buildFullAddress(danhSachDiaChi.get(0));
        }
        
        return null;
    }

    private KhachHang createOrFindKhachHang(HoaDonDTO dto) {
        // Tìm khách hàng theo tên và số điện thoại để tránh duplicate
        if (dto.getSoDienThoaiKhachHang() != null && !dto.getSoDienThoaiKhachHang().isEmpty()) {
            Optional<KhachHang> existingByPhone = khachHangRepository.findBySoDienThoai(dto.getSoDienThoaiKhachHang());
            if (existingByPhone.isPresent()) {
                // Kiểm tra xem tên có khác không
                KhachHang existingKhachHang = existingByPhone.get();
                if (existingKhachHang.getTenKhachHang().equals(dto.getTenKhachHang())) {
                    // Cùng tên và số điện thoại -> trả về khách hàng cũ
                    return existingKhachHang;
                } else {
                    // Khác tên nhưng cùng số điện thoại -> tạo khách hàng mới với số điện thoại khác
                    System.out.println("Tìm thấy khách hàng cũ với số điện thoại " + dto.getSoDienThoaiKhachHang() + 
                                     " nhưng tên khác (" + existingKhachHang.getTenKhachHang() + " vs " + dto.getTenKhachHang() + 
                                     "). Tạo khách hàng mới.");
                }
            }
        }
        
        // Tạo khách hàng mới
        KhachHang khachHang = new KhachHang();
        khachHang.setTenKhachHang(dto.getTenKhachHang());
        khachHang.setEmail(dto.getEmailKhachHang());
        
        // Nếu số điện thoại đã tồn tại với tên khác, tạo số điện thoại mới
        if (dto.getSoDienThoaiKhachHang() != null && !dto.getSoDienThoaiKhachHang().isEmpty()) {
            Optional<KhachHang> existingByPhone = khachHangRepository.findBySoDienThoai(dto.getSoDienThoaiKhachHang());
            if (existingByPhone.isPresent()) {
                // Tạo số điện thoại mới bằng cách thay đổi số cuối
                String originalPhone = dto.getSoDienThoaiKhachHang();
                String newPhone;
                if (originalPhone.length() >= 10) {
                    // Thay đổi 2 số cuối
                    newPhone = originalPhone.substring(0, originalPhone.length() - 2) + 
                              String.format("%02d", (System.currentTimeMillis() % 100));
                } else {
                    // Nếu số điện thoại ngắn, thêm số vào cuối
                    newPhone = originalPhone + String.format("%02d", (System.currentTimeMillis() % 100));
                }
                khachHang.setSoDienThoai(newPhone);
                System.out.println("Tạo số điện thoại mới: " + newPhone);
            } else {
                khachHang.setSoDienThoai(dto.getSoDienThoaiKhachHang());
            }
        } else {
            khachHang.setSoDienThoai("0000000000"); // Default phone
        }
        
        khachHang.setNgaySinh(java.time.LocalDate.of(1990, 1, 1)); // Default date
        khachHang.setGioiTinh(true); // Default gender
        khachHang.setDiemTichLuy(0);
        khachHang.setNgayTao(java.time.LocalDate.now());
        khachHang.setTrangThai(true);
        
        KhachHang savedKhachHang = khachHangRepository.save(khachHang);
        System.out.println("Đã tạo khách hàng mới với ID: " + savedKhachHang.getId() + 
                         ", Tên: " + savedKhachHang.getTenKhachHang() + 
                         ", SĐT: " + savedKhachHang.getSoDienThoai());
        
        return savedKhachHang;
    }

    private NhanVien createOrFindNhanVien(HoaDonDTO dto) {
        // Tìm nhân viên theo tên
        List<NhanVien> existingNhanVien = nhanVienRepository.findByHoTen(dto.getTenNhanVien());
        if (!existingNhanVien.isEmpty()) {
            return existingNhanVien.get(0);
        }
        
        // Tạo nhân viên mới
        NhanVien nhanVien = new NhanVien();
        nhanVien.setHoTen(dto.getTenNhanVien());
        nhanVien.setEmail("nv_" + System.currentTimeMillis() + "@company.com");
        nhanVien.setSoDienThoai("0123456789"); // Default phone
        nhanVien.setDiaChi("Hà Nội"); // Default address
        nhanVien.setGioiTinh(true); // Default gender
        nhanVien.setNgaySinh(java.time.LocalDate.of(1990, 1, 1)); // Default date
        nhanVien.setNgayVaoLam(java.time.LocalDate.now());
        nhanVien.setTrangThai(true);
        
        return nhanVienRepository.save(nhanVien);
    }
}
