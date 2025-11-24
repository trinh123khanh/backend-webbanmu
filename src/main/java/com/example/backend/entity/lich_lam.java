package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "lich_lam")  // Đổi tên bảng từ work_schedule thành lich_lam
public class lich_lam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Liên kết với bảng User

    @Column(nullable = false)
    private String dayOfWeek; // Thứ trong tuần (Thứ 2, Thứ 3,...)

    @Column(nullable = false)
    private String shift; // Ca làm việc (Ca 1, Ca 2,...)

    @Column(nullable = false)
    private String position; // Vị trí làm việc (Nhân viên thu ngân,...)

    @Column(nullable = false)
    private String date; // Ngày làm việc (ví dụ: 17/11/2025)

    // Constructor, getter, setter (lombok @Data sẽ tự động sinh ra)
}
