package com.example.backend.controller;

import com.example.backend.entity.DiaChiKhachHang;
import com.example.backend.repository.DiaChiKhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private DiaChiKhachHangRepository diaChiKhachHangRepository;

    @PostMapping("/insert-sample-addresses")
    public ResponseEntity<String> insertSampleAddresses() {
        try {
            // Kiểm tra xem đã có dữ liệu chưa
            List<DiaChiKhachHang> existingAddresses = diaChiKhachHangRepository.findAll();
            if (!existingAddresses.isEmpty()) {
                return ResponseEntity.ok("Dữ liệu địa chỉ đã tồn tại. Số lượng: " + existingAddresses.size());
            }

            // Tạo dữ liệu mẫu
            DiaChiKhachHang address1 = new DiaChiKhachHang();
            address1.setId(1L);
            address1.setTenNguoiNhan("Sample_70");
            address1.setSoDienThoai("0934638785");
            address1.setDiaChi("123 Đường Nguyễn Huệ");
            address1.setTinhThanh("TP.HCM");
            address1.setQuanHuyen("Quận 1");
            address1.setPhuongXa("Phường Bến Nghé");
            address1.setMacDinh(true);
            address1.setTrangThai(true);
            // Note: khach_hang_id sẽ được set thông qua entity relationship

            DiaChiKhachHang address2 = new DiaChiKhachHang();
            address2.setId(2L);
            address2.setTenNguoiNhan("Sample_67");
            address2.setSoDienThoai("0951277399");
            address2.setDiaChi("789 Đường Trần Hưng Đạo");
            address2.setTinhThanh("Hà Nội");
            address2.setQuanHuyen("Quận Hoàn Kiếm");
            address2.setPhuongXa("Phường Hàng Bạc");
            address2.setMacDinh(true);
            address2.setTrangThai(true);

            // Lưu vào database
            diaChiKhachHangRepository.save(address1);
            diaChiKhachHangRepository.save(address2);

            return ResponseEntity.ok("Đã tạo thành công 2 địa chỉ mẫu");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo dữ liệu mẫu: " + e.getMessage());
        }
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<DiaChiKhachHang>> getAllAddresses() {
        List<DiaChiKhachHang> addresses = diaChiKhachHangRepository.findAll();
        return ResponseEntity.ok(addresses);
    }
}
