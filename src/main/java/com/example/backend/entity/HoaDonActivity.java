package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity lưu lịch sử thay đổi của hóa đơn
 * Mapping với bảng hoa_don và users để theo dõi ai đã thực hiện thay đổi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hoa_don_activity", indexes = {
    @Index(name = "idx_hoa_don_activity_hoa_don_id", columnList = "hoa_don_id"),
    @Index(name = "idx_hoa_don_activity_performed_at", columnList = "performed_at"),
    @Index(name = "idx_hoa_don_activity_action", columnList = "action")
})
public class HoaDonActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key relationship với bảng hoa_don
     * nullable = true để cho phép lưu activity cho hóa đơn đã bị xóa
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id", nullable = true, foreignKey = @ForeignKey(name = "fk_hoa_don_activity_hoa_don"))
    private HoaDon hoaDon;

    /**
     * Lưu mã hóa đơn để dễ dàng query mà không cần join
     */
    @Column(name = "ma_hoa_don", length = 50, nullable = false)
    private String maHoaDon;

    /**
     * Loại hành động: CREATE, UPDATE, DELETE, STATUS_CHANGE, etc.
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Mô tả chi tiết về thay đổi
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Foreign key relationship với bảng users (người thực hiện)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true, foreignKey = @ForeignKey(name = "fk_hoa_don_activity_user"))
    private User user;

    /**
     * Lưu username để dễ dàng query mà không cần join
     */
    @Column(name = "performed_by", length = 100)
    private String performedBy;

    /**
     * Tên đầy đủ của người thực hiện (từ User.fullName hoặc NhanVien.hoTen)
     */
    @Column(name = "performed_by_name", length = 150)
    private String performedByName;

    /**
     * ID nhân viên nếu người thực hiện là nhân viên
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id", nullable = true, foreignKey = @ForeignKey(name = "fk_hoa_don_activity_nhan_vien"))
    private NhanVien nhanVien;

    /**
     * Thời gian thực hiện hành động
     */
    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    /**
     * Dữ liệu cũ (JSON format) - lưu trạng thái trước khi thay đổi
     */
    @Column(name = "old_data", columnDefinition = "TEXT")
    private String oldData;

    /**
     * Dữ liệu mới (JSON format) - lưu trạng thái sau khi thay đổi
     */
    @Column(name = "new_data", columnDefinition = "TEXT")
    private String newData;

    /**
     * IP address của người thực hiện (nếu cần)
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent của trình duyệt (nếu cần)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
}

