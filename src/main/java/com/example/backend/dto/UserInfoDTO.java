package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private List<RoleInfo> roles;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private String roleName;      // Tên role: ADMIN, STAFF, CUSTOMER
        private String roleDisplayName; // Tên hiển thị: Quản trị viên, Nhân viên, Khách hàng
    }
}

