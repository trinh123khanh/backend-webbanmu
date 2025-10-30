package com.example.backend.service;

import com.example.backend.dto.HoaDonDTO;
import com.example.backend.dto.SanPhamTrongHoaDon;
import java.math.BigDecimal;
import com.example.backend.entity.HoaDon;
import com.example.backend.entity.HoaDonChiTiet;
import com.example.backend.entity.ChiTietSanPham;
import com.example.backend.entity.SanPham;
import com.example.backend.entity.ThongTinDonHang;
import com.example.backend.entity.KhachHang;
import com.example.backend.entity.NhanVien;
import com.example.backend.entity.DiaChiKhachHang;
import com.example.backend.entity.PhuongThucThanhToan;
import com.example.backend.entity.HinhThucThanhToan;
import com.example.backend.repository.HoaDonRepository;
import com.example.backend.repository.HoaDonChiTietRepository;
import com.example.backend.repository.ChiTietSanPhamRepository;
import com.example.backend.repository.SanPhamRepository;
import com.example.backend.repository.ThongTinDonHangRepository;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.DiaChiKhachHangRepository;
import com.example.backend.repository.PhuongThucThanhToanRepository;
import com.example.backend.repository.HinhThucThanhToanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final SanPhamRepository sanPhamRepository;
    private final ThongTinDonHangRepository thongTinDonHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final HinhThucThanhToanRepository hinhThucThanhToanRepository;

    public HoaDonService(HoaDonRepository hoaDonRepository,
                        HoaDonChiTietRepository hoaDonChiTietRepository,
                        ChiTietSanPhamRepository chiTietSanPhamRepository,
                        SanPhamRepository sanPhamRepository,
                        ThongTinDonHangRepository thongTinDonHangRepository,
                        KhachHangRepository khachHangRepository,
                        NhanVienRepository nhanVienRepository,
                        DiaChiKhachHangRepository diaChiKhachHangRepository,
                        PhuongThucThanhToanRepository phuongThucThanhToanRepository,
                        HinhThucThanhToanRepository hinhThucThanhToanRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.thongTinDonHangRepository = thongTinDonHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.hinhThucThanhToanRepository = hinhThucThanhToanRepository;
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
        
        HoaDonDTO hoaDonDTO = convertToDTO(hoaDon);
        
        // Load chi tiết sản phẩm
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDon(hoaDon);
        List<SanPhamTrongHoaDon> danhSachSanPham = chiTietList.stream()
                .map(chiTiet -> {
                    SanPhamTrongHoaDon sanPhamDTO = new SanPhamTrongHoaDon();
                    sanPhamDTO.setId(chiTiet.getId());
                    sanPhamDTO.setSoLuong(chiTiet.getSoLuong());
                    sanPhamDTO.setDonGia(chiTiet.getDonGia());
                    sanPhamDTO.setThanhTien(chiTiet.getThanhTien());
                    
                    if (chiTiet.getChiTietSanPham() != null) {
                        ChiTietSanPham chiTietSanPham = chiTiet.getChiTietSanPham();
                        sanPhamDTO.setSanPhamId(chiTietSanPham.getId());
                        sanPhamDTO.setSoLuongTon(chiTietSanPham.getSoLuongTon());
                        
                        if (chiTietSanPham.getSanPham() != null) {
                            SanPham sanPham = chiTietSanPham.getSanPham();
                            sanPhamDTO.setTenSanPham(sanPham.getTenSanPham());
                            sanPhamDTO.setMaSanPham(sanPham.getMaSanPham());
                            sanPhamDTO.setDanhMuc(sanPham.getLoaiMuBaoHiem() != null ? sanPham.getLoaiMuBaoHiem().getTenLoai() : "Chưa phân loại");
                            sanPhamDTO.setThuongHieu(sanPham.getNhaSanXuat() != null ? sanPham.getNhaSanXuat().getTenNhaSanXuat() : "Chưa có");
                        }
                    }
                    
                    return sanPhamDTO;
                })
                .collect(Collectors.toList());
        
        hoaDonDTO.setDanhSachSanPham(danhSachSanPham);
        
        return hoaDonDTO;
    }
    
    public HoaDonDTO getHoaDonDetail(Long id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));
        
        HoaDonDTO hoaDonDTO = convertToDTO(hoaDon);
        
        // Load chi tiết sản phẩm
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDon(hoaDon);
        List<SanPhamTrongHoaDon> danhSachSanPham = chiTietList.stream()
                .map(chiTiet -> {
                    SanPhamTrongHoaDon sanPhamDTO = new SanPhamTrongHoaDon();
                    sanPhamDTO.setId(chiTiet.getId());
                    sanPhamDTO.setSoLuong(chiTiet.getSoLuong());
                    sanPhamDTO.setDonGia(chiTiet.getDonGia());
                    sanPhamDTO.setThanhTien(chiTiet.getThanhTien());
                    
                    if (chiTiet.getChiTietSanPham() != null) {
                        ChiTietSanPham chiTietSanPham = chiTiet.getChiTietSanPham();
                        sanPhamDTO.setSanPhamId(chiTietSanPham.getId());
                        sanPhamDTO.setSoLuongTon(chiTietSanPham.getSoLuongTon());
                        
                        if (chiTietSanPham.getSanPham() != null) {
                            SanPham sanPham = chiTietSanPham.getSanPham();
                            sanPhamDTO.setTenSanPham(sanPham.getTenSanPham());
                            sanPhamDTO.setMaSanPham(sanPham.getMaSanPham());
                            sanPhamDTO.setDanhMuc(sanPham.getLoaiMuBaoHiem() != null ? sanPham.getLoaiMuBaoHiem().getTenLoai() : "Chưa phân loại");
                            sanPhamDTO.setThuongHieu(sanPham.getNhaSanXuat() != null ? sanPham.getNhaSanXuat().getTenNhaSanXuat() : "Chưa có");
                        }
                    }
                    
                    return sanPhamDTO;
                })
                .collect(Collectors.toList());
        
        hoaDonDTO.setDanhSachSanPham(danhSachSanPham);
        // soLuongSanPham đã được set trong convertToDTO từ database
        
        return hoaDonDTO;
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
        
        // Xử lý khách hàng
        if (hoaDonDTO.getKhachHangId() != null) {
            KhachHang khachHang = khachHangRepository.findById(hoaDonDTO.getKhachHangId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + hoaDonDTO.getKhachHangId()));
            hoaDon.setKhachHang(khachHang);
        } else if (hoaDonDTO.getTenKhachHang() != null && !hoaDonDTO.getTenKhachHang().isEmpty()) {
            KhachHang khachHang = createOrFindKhachHang(hoaDonDTO);
            hoaDon.setKhachHang(khachHang);
            
            // Tạo địa chỉ mặc định cho khách hàng mới nếu có thông tin địa chỉ
            if (hoaDonDTO.getDiaChiChiTiet() != null && !hoaDonDTO.getDiaChiChiTiet().isEmpty()) {
                System.out.println("🏠 Creating default address for new customer: " + khachHang.getTenKhachHang());
                
                // Kiểm tra xem khách hàng đã có địa chỉ mặc định chưa
                List<DiaChiKhachHang> existingAddresses = diaChiKhachHangRepository.findDiaChiMacDinhByKhachHangId(khachHang.getId());
                
                if (existingAddresses.isEmpty()) {
                    // Tạo địa chỉ mặc định mới
                    DiaChiKhachHang diaChi = new DiaChiKhachHang();
                    diaChi.setKhachHang(khachHang);
                    diaChi.setDiaChi(hoaDonDTO.getDiaChiChiTiet());
                    diaChi.setTinhThanh(hoaDonDTO.getTinhThanh());
                    diaChi.setQuanHuyen(hoaDonDTO.getQuanHuyen());
                    diaChi.setPhuongXa(hoaDonDTO.getPhuongXa());
                    diaChi.setMacDinh(true);
                    diaChi.setTrangThai(true);
                    diaChi.setTenNguoiNhan(khachHang.getTenKhachHang());
                    diaChi.setSoDienThoai(khachHang.getSoDienThoai());
                    
                    diaChiKhachHangRepository.save(diaChi);
                    System.out.println("✅ Created default address for new customer: " + khachHang.getTenKhachHang());
                } else {
                    System.out.println("ℹ️ Customer already has default address, skipping creation");
                }
            }
        }
        
        // Xử lý nhân viên
        if (hoaDonDTO.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(hoaDonDTO.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + hoaDonDTO.getNhanVienId()));
            hoaDon.setNhanVien(nhanVien);
        } else if (hoaDonDTO.getTenNhanVien() != null && !hoaDonDTO.getTenNhanVien().isEmpty()) {
            NhanVien nhanVien = createOrFindNhanVien(hoaDonDTO);
            hoaDon.setNhanVien(nhanVien);
        }
        
        // Lưu hóa đơn trước để có ID
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        
        // Xử lý chi tiết sản phẩm nếu có
        if (hoaDonDTO.getDanhSachSanPham() != null && !hoaDonDTO.getDanhSachSanPham().isEmpty()) {
            for (SanPhamTrongHoaDon sanPhamDTO : hoaDonDTO.getDanhSachSanPham()) {
                HoaDonChiTiet chiTiet = new HoaDonChiTiet();
                chiTiet.setHoaDon(savedHoaDon);
                chiTiet.setSoLuong(sanPhamDTO.getSoLuong());
                chiTiet.setDonGia(sanPhamDTO.getDonGia());
                chiTiet.setThanhTien(sanPhamDTO.getThanhTien());
                
                // Tìm chi tiết sản phẩm theo ID hoặc tên
                if (sanPhamDTO.getSanPhamId() != null) {
                    // Tìm ChiTietSanPham theo sanPhamId
                    List<ChiTietSanPham> chiTietSanPhamList = chiTietSanPhamRepository.findBySanPhamId(sanPhamDTO.getSanPhamId());
                    
                    ChiTietSanPham chiTietSanPham;
                    if (chiTietSanPhamList.isEmpty()) {
                        // Nếu không tìm thấy ChiTietSanPham, tạo mới từ SanPham
                        SanPham sanPham = sanPhamRepository.findById(sanPhamDTO.getSanPhamId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + sanPhamDTO.getSanPhamId()));
                        
                        chiTietSanPham = new ChiTietSanPham();
                        chiTietSanPham.setId(null); // Đảm bảo ID là null
                        chiTietSanPham.setSanPham(sanPham);
                        chiTietSanPham.setSoLuongTon(sanPham.getSoLuongTon());
                        chiTietSanPham.setGiaBan(sanPham.getGiaBan());
                        chiTietSanPham.setTrangThai(true);
                        
                        try {
                            chiTietSanPham = chiTietSanPhamRepository.save(chiTietSanPham);
                        } catch (Exception e) {
                            // Nếu có lỗi duplicate, thử tìm lại
                            chiTietSanPhamList = chiTietSanPhamRepository.findBySanPhamId(sanPhamDTO.getSanPhamId());
                            if (!chiTietSanPhamList.isEmpty()) {
                                chiTietSanPham = chiTietSanPhamList.get(0);
                            } else {
                                throw new RuntimeException("Không thể tạo chi tiết sản phẩm: " + e.getMessage());
                            }
                        }
                    } else {
                        chiTietSanPham = chiTietSanPhamList.get(0);
                    }
                    
                    chiTiet.setChiTietSanPham(chiTietSanPham);
                } else if (sanPhamDTO.getTenSanPham() != null && !sanPhamDTO.getTenSanPham().isEmpty()) {
                    // Tìm chi tiết sản phẩm theo tên
                    ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findByTenSanPham(sanPhamDTO.getTenSanPham())
                            .orElse(null);
                    if (chiTietSanPham != null) {
                        chiTiet.setChiTietSanPham(chiTietSanPham);
                    }
                }
                
                hoaDonChiTietRepository.save(chiTiet);
            }
            
            // Cập nhật số lượng sản phẩm trong database
            savedHoaDon.setSoLuongSanPham(hoaDonDTO.getDanhSachSanPham().size());
            savedHoaDon = hoaDonRepository.save(savedHoaDon);
        } else {
            // Nếu không có sản phẩm, set soLuongSanPham = 0
            savedHoaDon.setSoLuongSanPham(0);
            savedHoaDon = hoaDonRepository.save(savedHoaDon);
        }
        
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
        existingHoaDon.setGiamGiaPhanTram(hoaDonDTO.getGiamGiaPhanTram());
        existingHoaDon.setThanhTien(hoaDonDTO.getThanhTien());
        existingHoaDon.setGhiChu(hoaDonDTO.getGhiChu());
        existingHoaDon.setTrangThai(hoaDonDTO.getTrangThai());
        
        // Cập nhật phương thức thanh toán nếu có
        if (hoaDonDTO.getPhuongThucThanhToan() != null && !hoaDonDTO.getPhuongThucThanhToan().isEmpty()) {
            // Xóa phương thức thanh toán cũ
            existingHoaDon.getPhuongThucThanhToan().clear();
            
            // Tìm HinhThucThanhToan hiện có
            Optional<HinhThucThanhToan> existingHinhThuc = hinhThucThanhToanRepository.findByTenHinhThuc(hoaDonDTO.getPhuongThucThanhToan());
            
            HinhThucThanhToan hinhThuc;
            if (existingHinhThuc.isPresent()) {
                // Sử dụng HinhThucThanhToan đã tồn tại
                hinhThuc = existingHinhThuc.get();
            } else {
                // Tạo mới HinhThucThanhToan (JPA sẽ tự động tạo ID)
                hinhThuc = new HinhThucThanhToan();
                hinhThuc.setTenHinhThuc(hoaDonDTO.getPhuongThucThanhToan());
                hinhThuc.setTrangThai(true);
                hinhThuc.setMoTa("Phương thức thanh toán: " + hoaDonDTO.getPhuongThucThanhToan());
                // Lưu để có ID
                hinhThuc = hinhThucThanhToanRepository.save(hinhThuc);
            }
            
            // Tạo phương thức thanh toán mới
            PhuongThucThanhToan phuongThuc = new PhuongThucThanhToan();
            phuongThuc.setHoaDon(existingHoaDon);
            phuongThuc.setHinhThucThanhToan(hinhThuc);
            phuongThuc.setSoTienThanhToan(existingHoaDon.getThanhTien());
            phuongThuc.setTrangThai(PhuongThucThanhToan.TrangThaiThanhToan.CHO_THANH_TOAN);
            
            existingHoaDon.getPhuongThucThanhToan().add(phuongThuc);
        }
        
        // Update relationships - xử lý khách hàng
        if (hoaDonDTO.getKhachHangId() != null) {
            // Nếu có ID khách hàng, tìm khách hàng theo ID và cập nhật thông tin
            KhachHang khachHang = khachHangRepository.findById(hoaDonDTO.getKhachHangId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + hoaDonDTO.getKhachHangId()));
            
            // Cập nhật thông tin khách hàng từ form nếu có
            if (hoaDonDTO.getTenKhachHang() != null && !hoaDonDTO.getTenKhachHang().isEmpty()) {
                khachHang.setTenKhachHang(hoaDonDTO.getTenKhachHang());
            }
            if (hoaDonDTO.getEmailKhachHang() != null && !hoaDonDTO.getEmailKhachHang().isEmpty()) {
                khachHang.setEmail(hoaDonDTO.getEmailKhachHang());
            }
            if (hoaDonDTO.getSoDienThoaiKhachHang() != null && !hoaDonDTO.getSoDienThoaiKhachHang().isEmpty()) {
                khachHang.setSoDienThoai(hoaDonDTO.getSoDienThoaiKhachHang());
            }
            
            // Cập nhật địa chỉ khách hàng từ form nếu có
            if (hoaDonDTO.getTinhThanh() != null || hoaDonDTO.getQuanHuyen() != null || 
                hoaDonDTO.getPhuongXa() != null || hoaDonDTO.getDiaChiChiTiet() != null) {
                
                // Tìm địa chỉ mặc định của khách hàng
                List<DiaChiKhachHang> diaChiMacDinhList = diaChiKhachHangRepository.findDiaChiMacDinhByKhachHangId(khachHang.getId());
                DiaChiKhachHang diaChiMacDinh;
                
                if (!diaChiMacDinhList.isEmpty()) {
                    // Cập nhật địa chỉ mặc định hiện có
                    diaChiMacDinh = diaChiMacDinhList.get(0);
                } else {
                    // Tạo địa chỉ mặc định mới
                    diaChiMacDinh = new DiaChiKhachHang();
                    diaChiMacDinh.setKhachHang(khachHang);
                    diaChiMacDinh.setTenNguoiNhan(khachHang.getTenKhachHang());
                    diaChiMacDinh.setSoDienThoai(khachHang.getSoDienThoai());
                    diaChiMacDinh.setMacDinh(true);
                    diaChiMacDinh.setTrangThai(true);
                }
                
                // Cập nhật thông tin địa chỉ từ form
                if (hoaDonDTO.getDiaChiChiTiet() != null && !hoaDonDTO.getDiaChiChiTiet().isEmpty()) {
                    diaChiMacDinh.setDiaChi(hoaDonDTO.getDiaChiChiTiet());
                }
                if (hoaDonDTO.getTinhThanh() != null && !hoaDonDTO.getTinhThanh().isEmpty()) {
                    diaChiMacDinh.setTinhThanh(hoaDonDTO.getTinhThanh());
                }
                if (hoaDonDTO.getQuanHuyen() != null && !hoaDonDTO.getQuanHuyen().isEmpty()) {
                    diaChiMacDinh.setQuanHuyen(hoaDonDTO.getQuanHuyen());
                }
                if (hoaDonDTO.getPhuongXa() != null && !hoaDonDTO.getPhuongXa().isEmpty()) {
                    diaChiMacDinh.setPhuongXa(hoaDonDTO.getPhuongXa());
                }
                
                // Lưu địa chỉ đã cập nhật
                diaChiKhachHangRepository.save(diaChiMacDinh);
                System.out.println("✅ Updated customer address for ID: " + khachHang.getId());
            }
            
            // Lưu thông tin khách hàng đã cập nhật
            khachHangRepository.save(khachHang);
            System.out.println("✅ Updated customer info for ID: " + khachHang.getId());
            
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
        
        // Update danh sách sản phẩm nếu có
        if (hoaDonDTO.getDanhSachSanPham() != null && !hoaDonDTO.getDanhSachSanPham().isEmpty()) {
            System.out.println("🔄 Updating invoice products: " + hoaDonDTO.getDanhSachSanPham().size() + " products");
            
            // Xóa tất cả chi tiết hóa đơn cũ
            List<HoaDonChiTiet> existingChiTiet = hoaDonChiTietRepository.findByHoaDon(existingHoaDon);
            hoaDonChiTietRepository.deleteAll(existingChiTiet);
            System.out.println("🗑️ Deleted " + existingChiTiet.size() + " old invoice details");
            
            // Tạo chi tiết hóa đơn mới
            List<HoaDonChiTiet> newChiTietList = new ArrayList<>();
            for (SanPhamTrongHoaDon sanPhamDTO : hoaDonDTO.getDanhSachSanPham()) {
                HoaDonChiTiet chiTiet = new HoaDonChiTiet();
                chiTiet.setHoaDon(existingHoaDon);
                chiTiet.setSoLuong(sanPhamDTO.getSoLuong());
                chiTiet.setDonGia(sanPhamDTO.getDonGia());
                
                // Tính toán thanhTien: donGia * soLuong (không có giảm giá ở cấp sản phẩm)
                BigDecimal tongTienSanPham = sanPhamDTO.getDonGia().multiply(BigDecimal.valueOf(sanPhamDTO.getSoLuong()));
                chiTiet.setThanhTien(tongTienSanPham);
                chiTiet.setGiamGia(BigDecimal.ZERO); // Không có giảm giá ở cấp sản phẩm
                
                System.out.println("💰 Product: " + sanPhamDTO.getTenSanPham() + 
                                 ", DonGia: " + sanPhamDTO.getDonGia() + 
                                 ", SoLuong: " + sanPhamDTO.getSoLuong() + 
                                 ", ThanhTien: " + tongTienSanPham);
                
                // Tìm ChiTietSanPham theo sanPhamId
                if (sanPhamDTO.getSanPhamId() != null) {
                    List<ChiTietSanPham> chiTietSanPhamList = chiTietSanPhamRepository.findBySanPhamId(sanPhamDTO.getSanPhamId());
                    if (!chiTietSanPhamList.isEmpty()) {
                        chiTiet.setChiTietSanPham(chiTietSanPhamList.get(0));
                    } else {
                        // Tạo ChiTietSanPham mới nếu không tìm thấy
                        SanPham sanPham = sanPhamRepository.findById(sanPhamDTO.getSanPhamId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + sanPhamDTO.getSanPhamId()));
                        
                        ChiTietSanPham newChiTietSanPham = new ChiTietSanPham();
                        newChiTietSanPham.setSanPham(sanPham);
                        newChiTietSanPham.setGiaBan(sanPhamDTO.getDonGia());
                        newChiTietSanPham.setSoLuongTon(sanPhamDTO.getSoLuongTon() != null ? sanPhamDTO.getSoLuongTon() : 0);
                        newChiTietSanPham.setTrangThai(true);
                        
                        ChiTietSanPham savedChiTietSanPham = chiTietSanPhamRepository.save(newChiTietSanPham);
                        chiTiet.setChiTietSanPham(savedChiTietSanPham);
                        System.out.println("✅ Created new ChiTietSanPham for product: " + sanPham.getTenSanPham());
                    }
                }
                
                newChiTietList.add(chiTiet);
            }
            
            // Lưu tất cả chi tiết mới
            hoaDonChiTietRepository.saveAll(newChiTietList);
            System.out.println("✅ Saved " + newChiTietList.size() + " new invoice details");
            
            // Cập nhật số lượng sản phẩm
            existingHoaDon.setSoLuongSanPham(newChiTietList.size());
        }
        
        HoaDon updatedHoaDon = hoaDonRepository.save(existingHoaDon);
        return convertToDTO(updatedHoaDon);
    }

    @Transactional
    public void deleteHoaDon(Long id) {
        try {
            // Tìm hóa đơn
            HoaDon hoaDon = hoaDonRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn với ID: " + id));
            
            System.out.println("🗑️ Deleting invoice: " + hoaDon.getMaHoaDon() + " (ID: " + id + ")");
            
            // Xóa các bản ghi liên quan trước
            // 1. Xóa thông tin đơn hàng (thong_tin_don_hang) trước
            try {
                ThongTinDonHang thongTinDonHang = thongTinDonHangRepository.findByHoaDon(hoaDon);
                if (thongTinDonHang != null) {
                    System.out.println("🗑️ Deleting thong_tin_don_hang for invoice: " + hoaDon.getMaHoaDon());
                    thongTinDonHangRepository.delete(thongTinDonHang);
                } else {
                    System.out.println("ℹ️ No thong_tin_don_hang found for invoice: " + hoaDon.getMaHoaDon());
                }
            } catch (Exception e) {
                System.out.println("ℹ️ No thong_tin_don_hang records to delete: " + e.getMessage());
            }
            
            // 2. Xóa chi tiết hóa đơn (hoa_don_chi_tiet)
            if (hoaDon.getDanhSachChiTiet() != null && !hoaDon.getDanhSachChiTiet().isEmpty()) {
                System.out.println("🗑️ Deleting " + hoaDon.getDanhSachChiTiet().size() + " invoice details");
                hoaDonChiTietRepository.deleteAll(hoaDon.getDanhSachChiTiet());
            }
            
            // 3. Cuối cùng xóa hóa đơn
            hoaDonRepository.delete(hoaDon);
            System.out.println("✅ Successfully deleted invoice: " + hoaDon.getMaHoaDon());
            
        } catch (EntityNotFoundException e) {
            System.err.println("❌ Invoice not found ID " + id + ": " + e.getMessage());
            throw e; // Re-throw EntityNotFoundException để controller xử lý
        } catch (Exception e) {
            System.err.println("❌ Error deleting invoice ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Không thể xóa hóa đơn: " + e.getMessage(), e);
        }
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
                .tinhThanh(getTinhThanh(hoaDon.getKhachHang()))
                .quanHuyen(getQuanHuyen(hoaDon.getKhachHang()))
                .phuongXa(getPhuongXa(hoaDon.getKhachHang()))
                .diaChiChiTiet(getDiaChiChiTiet(hoaDon.getKhachHang()))
                .nhanVienId(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getId() : null)
                .tenNhanVien(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getHoTen() : null)
                .ngayTao(hoaDon.getNgayTao())
                .ngayThanhToan(hoaDon.getNgayThanhToan())
                .tongTien(hoaDon.getTongTien())
                .tienGiamGia(hoaDon.getTienGiamGia())
                .giamGiaPhanTram(hoaDon.getGiamGiaPhanTram())
                .thanhTien(hoaDon.getThanhTien())
                .ghiChu(hoaDon.getGhiChu())
                .phuongThucThanhToan(getPhuongThucThanhToan(hoaDon))
                .trangThai(hoaDon.getTrangThai())
                .soLuongSanPham(hoaDon.getSoLuongSanPham())
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

    private String getTinhThanh(KhachHang khachHang) {
        if (khachHang == null || khachHang.getId() == null) {
            return null;
        }
        
        List<DiaChiKhachHang> diaChiMacDinhList = diaChiKhachHangRepository.findDiaChiMacDinhByKhachHangId(khachHang.getId());
        if (!diaChiMacDinhList.isEmpty()) {
            return diaChiMacDinhList.get(0).getTinhThanh();
        }
        
        List<DiaChiKhachHang> danhSachDiaChi = diaChiKhachHangRepository.findDiaChiActiveByKhachHangId(khachHang.getId());
        if (!danhSachDiaChi.isEmpty()) {
            return danhSachDiaChi.get(0).getTinhThanh();
        }
        
        return null;
    }

    private String getQuanHuyen(KhachHang khachHang) {
        if (khachHang == null || khachHang.getId() == null) {
            return null;
        }
        
        List<DiaChiKhachHang> diaChiMacDinhList = diaChiKhachHangRepository.findDiaChiMacDinhByKhachHangId(khachHang.getId());
        if (!diaChiMacDinhList.isEmpty()) {
            return diaChiMacDinhList.get(0).getQuanHuyen();
        }
        
        List<DiaChiKhachHang> danhSachDiaChi = diaChiKhachHangRepository.findDiaChiActiveByKhachHangId(khachHang.getId());
        if (!danhSachDiaChi.isEmpty()) {
            return danhSachDiaChi.get(0).getQuanHuyen();
        }
        
        return null;
    }

    private String getPhuongXa(KhachHang khachHang) {
        if (khachHang == null || khachHang.getId() == null) {
            return null;
        }
        
        List<DiaChiKhachHang> diaChiMacDinhList = diaChiKhachHangRepository.findDiaChiMacDinhByKhachHangId(khachHang.getId());
        if (!diaChiMacDinhList.isEmpty()) {
            return diaChiMacDinhList.get(0).getPhuongXa();
        }
        
        List<DiaChiKhachHang> danhSachDiaChi = diaChiKhachHangRepository.findDiaChiActiveByKhachHangId(khachHang.getId());
        if (!danhSachDiaChi.isEmpty()) {
            return danhSachDiaChi.get(0).getPhuongXa();
        }
        
        return null;
    }

    private String getDiaChiChiTiet(KhachHang khachHang) {
        if (khachHang == null || khachHang.getId() == null) {
            return null;
        }
        
        List<DiaChiKhachHang> diaChiMacDinhList = diaChiKhachHangRepository.findDiaChiMacDinhByKhachHangId(khachHang.getId());
        if (!diaChiMacDinhList.isEmpty()) {
            return diaChiMacDinhList.get(0).getDiaChi();
        }
        
        List<DiaChiKhachHang> danhSachDiaChi = diaChiKhachHangRepository.findDiaChiActiveByKhachHangId(khachHang.getId());
        if (!danhSachDiaChi.isEmpty()) {
            return danhSachDiaChi.get(0).getDiaChi();
        }
        
        return null;
    }

    private String getPhuongThucThanhToan(HoaDon hoaDon) {
        if (hoaDon.getPhuongThucThanhToan() != null && !hoaDon.getPhuongThucThanhToan().isEmpty()) {
            // Lấy phương thức thanh toán đầu tiên
            return hoaDon.getPhuongThucThanhToan().get(0).getHinhThucThanhToan().getTenHinhThuc();
        }
        return null;
    }

    private KhachHang createOrFindKhachHang(HoaDonDTO dto) {
        System.out.println("🔍 Creating or finding customer for: " + dto.getTenKhachHang());
        
        // Tìm khách hàng theo email trước
        if (dto.getEmailKhachHang() != null && !dto.getEmailKhachHang().isEmpty()) {
            Optional<KhachHang> existingByEmail = khachHangRepository.findByEmail(dto.getEmailKhachHang());
            if (existingByEmail.isPresent()) {
                System.out.println("✅ Found existing customer by email: " + existingByEmail.get().getTenKhachHang());
                return existingByEmail.get();
            }
        }
        
        // Tìm khách hàng theo số điện thoại
        if (dto.getSoDienThoaiKhachHang() != null && !dto.getSoDienThoaiKhachHang().isEmpty()) {
            Optional<KhachHang> existingByPhone = khachHangRepository.findBySoDienThoai(dto.getSoDienThoaiKhachHang());
            if (existingByPhone.isPresent()) {
                System.out.println("✅ Found existing customer by phone: " + existingByPhone.get().getTenKhachHang());
                return existingByPhone.get();
            }
        }
        
        // Tạo khách hàng mới
        System.out.println("🆕 Creating new customer...");
        KhachHang khachHang = new KhachHang();
        khachHang.setTenKhachHang(dto.getTenKhachHang());
        khachHang.setEmail(dto.getEmailKhachHang());
        khachHang.setSoDienThoai(dto.getSoDienThoaiKhachHang() != null ? dto.getSoDienThoaiKhachHang() : "0000000000");
        khachHang.setNgaySinh(java.time.LocalDate.of(1990, 1, 1));
        khachHang.setGioiTinh(true);
        khachHang.setDiemTichLuy(0);
        khachHang.setNgayTao(java.time.LocalDate.now());
        khachHang.setTrangThai(true);
        
        // Không set ID - để JPA tự động generate
        KhachHang savedKhachHang = khachHangRepository.save(khachHang);
        System.out.println("✅ Created new customer with ID: " + savedKhachHang.getId() + 
                         ", Name: " + savedKhachHang.getTenKhachHang() + 
                         ", Email: " + savedKhachHang.getEmail() +
                         ", Phone: " + savedKhachHang.getSoDienThoai());
        
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
    
    /**
     * Lấy danh sách hóa đơn có phân trang với các bộ lọc
     */
    public Page<HoaDonDTO> getAllHoaDonPaginated(Pageable pageable, String maHoaDon, String keyword, 
                                               String trangThai, String trangThaiThanhToan, 
                                               String phuongThucThanhToan) {
        
        // Tạo Specification để lọc dữ liệu
        Specification<HoaDon> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            
            // Lọc theo mã hóa đơn
            if (maHoaDon != null && !maHoaDon.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("maHoaDon")), 
                    "%" + maHoaDon.toLowerCase() + "%"
                ));
            }
            
            // Lọc theo keyword (tên khách hàng, email, số điện thoại)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Predicate keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("tenKhachHang")), 
                        "%" + keyword.toLowerCase() + "%"
                    ),
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("emailKhachHang")), 
                        "%" + keyword.toLowerCase() + "%"
                    ),
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("soDienThoaiKhachHang")), 
                        "%" + keyword.toLowerCase() + "%"
                    )
                );
                predicates.add(keywordPredicate);
            }
            
            // Lọc theo trạng thái
            if (trangThai != null && !trangThai.trim().isEmpty() && !trangThai.equals("all")) {
                try {
                    HoaDon.TrangThaiHoaDon status = HoaDon.TrangThaiHoaDon.valueOf(trangThai);
                    predicates.add(criteriaBuilder.equal(root.get("trangThai"), status));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status values
                }
            }
            
            // Lọc theo trạng thái thanh toán
            if (trangThaiThanhToan != null && !trangThaiThanhToan.trim().isEmpty() && !trangThaiThanhToan.equals("all")) {
                if ("paid".equals(trangThaiThanhToan)) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("ngayThanhToan")));
                } else if ("pending".equals(trangThaiThanhToan)) {
                    predicates.add(criteriaBuilder.isNull(root.get("ngayThanhToan")));
                }
            }
            
            // Lọc theo phương thức thanh toán
            if (phuongThucThanhToan != null && !phuongThucThanhToan.trim().isEmpty() && !phuongThucThanhToan.equals("all")) {
                predicates.add(criteriaBuilder.equal(root.get("viTriBanHang"), phuongThucThanhToan));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        // Thực hiện truy vấn với phân trang
        Page<HoaDon> hoaDonPage = hoaDonRepository.findAll(spec, pageable);
        
        // Chuyển đổi sang DTO
        return hoaDonPage.map(this::convertToDTO);
    }
}
