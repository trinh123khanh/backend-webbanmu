package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "chat_lieu_vo")
public class ChatLieuVo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String tenChatLieu;
    private String moTa;
    
    @Column(columnDefinition = "boolean default true")
    private Boolean trangThai;
}
