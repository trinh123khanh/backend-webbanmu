package com.example.backend.service.impl;

import com.example.backend.dto.chat.ChatMessageDTO;
import com.example.backend.dto.chat.ConversationDTO;
import com.example.backend.dto.chat.SendMessageRequest;
import com.example.backend.service.ChatConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatConversationServiceImpl implements ChatConversationService {

    private final Map<Long, ConversationDTO> conversations = new ConcurrentHashMap<>();
    private final Map<Long, ConversationDTO> conversationsByCustomer = new ConcurrentHashMap<>();
    private final AtomicLong conversationIdSequence = new AtomicLong(1);
    private final AtomicLong messageIdSequence = new AtomicLong(1);
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public ConversationDTO getOrCreateConversation(Long khachHangId) {
        if (khachHangId == null) {
            throw new IllegalArgumentException("khachHangId không được để trống");
        }
        ConversationDTO existing = conversationsByCustomer.get(khachHangId);
        if (existing != null) {
            updateUnreadCount(existing, khachHangId);
            return existing;
        }
        ConversationDTO conversation = ConversationDTO.builder()
                .id(conversationIdSequence.getAndIncrement())
                .khachHangId(khachHangId)
                .khachHangTen("Khách hàng #" + khachHangId)
                .ngayTao(now())
                .ngayCapNhat(now())
                .trangThai("DANG_CHO")
                .dangChoPhanHoi(true)
                .tuDongTraLoi(true)
                .messages(new ArrayList<>())
                .soTinNhanChuaDoc(0)
                .build();
        conversations.put(conversation.getId(), conversation);
        conversationsByCustomer.put(khachHangId, conversation);
        log.info("Created new conversation for customer {}", khachHangId);
        return conversation;
    }

    @Override
    public ConversationDTO getConversationById(Long conversationId) {
        ConversationDTO conversation = conversations.get(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("Không tìm thấy cuộc trò chuyện");
        }
        updateUnreadCount(conversation, conversation.getKhachHangId());
        return conversation;
    }

    @Override
    public ChatMessageDTO sendCustomerMessage(SendMessageRequest request) {
        validateRequest(request, true);
        ConversationDTO conversation = resolveConversationForCustomer(request);
        ChatMessageDTO message = buildMessage(request, conversation, "KHACH_HANG");
        message.setKhachHangId(conversation.getKhachHangId());
        message.setKhachHangTen(conversation.getKhachHangTen());
        message.setDaDoc(true);
        conversation.getMessages().add(message);
        conversation.setNgayCapNhat(now());
        conversation.setDangChoPhanHoi(true);
        updateUnreadCount(conversation, conversation.getKhachHangId());

        // Tạo phản hồi tự động đơn giản
        addAutoReply(conversation);

        return message;
    }

    @Override
    public ChatMessageDTO sendStaffMessage(SendMessageRequest request) {
        validateRequest(request, false);
        ConversationDTO conversation = conversations.get(request.getConversationId());
        if (conversation == null) {
            throw new IllegalArgumentException("Không tìm thấy cuộc trò chuyện để gửi tin nhắn");
        }
        if (request.getNhanVienId() == null) {
            throw new IllegalArgumentException("nhanVienId không được để trống");
        }
        if (!StringUtils.hasText(conversation.getNhanVienTen())) {
            conversation.setNhanVienTen("Nhân viên #" + request.getNhanVienId());
        }
        conversation.setNhanVienId(request.getNhanVienId());
        conversation.setTrangThai("DANG_XU_LY");
        ChatMessageDTO message = buildMessage(request, conversation, "NHAN_VIEN");
        message.setNhanVienId(request.getNhanVienId());
        message.setNhanVienTen(conversation.getNhanVienTen());
        message.setDaDoc(true);
        conversation.getMessages().add(message);
        conversation.setDangChoPhanHoi(false);
        conversation.setNgayCapNhat(now());
        updateUnreadCount(conversation, conversation.getKhachHangId());
        return message;
    }

    @Override
    public List<ConversationDTO> getWaitingConversations() {
        return conversations.values().stream()
                .filter(conv -> Objects.equals(conv.getTrangThai(), "DANG_CHO") || Boolean.TRUE.equals(conv.getDangChoPhanHoi()))
                .sorted(Comparator.comparing(ConversationDTO::getNgayCapNhat).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationDTO> getStaffConversations(Long nhanVienId) {
        return conversations.values().stream()
                .filter(conv -> nhanVienId == null || Objects.equals(conv.getNhanVienId(), nhanVienId))
                .sorted(Comparator.comparing(ConversationDTO::getNgayCapNhat).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public ConversationDTO assignConversationToStaff(Long conversationId, Long nhanVienId) {
        if (nhanVienId == null) {
            throw new IllegalArgumentException("nhanVienId không được để trống");
        }
        ConversationDTO conversation = conversations.get(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("Không tìm thấy cuộc trò chuyện");
        }
        conversation.setNhanVienId(nhanVienId);
        conversation.setNhanVienTen(Optional.ofNullable(conversation.getNhanVienTen())
                .orElse("Nhân viên #" + nhanVienId));
        conversation.setTrangThai("DANG_XU_LY");
        conversation.setDangChoPhanHoi(false);
        conversation.setNgayCapNhat(now());
        return conversation;
    }

    @Override
    public ConversationDTO markMessagesAsRead(Long conversationId, Long nhanVienId) {
        ConversationDTO conversation = conversations.get(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("Không tìm thấy cuộc trò chuyện");
        }
        conversation.getMessages().forEach(msg -> msg.setDaDoc(true));
        conversation.setSoTinNhanChuaDoc(0);
        conversation.setNgayCapNhat(now());
        conversation.setDangChoPhanHoi(false);
        if (nhanVienId != null && conversation.getNhanVienId() == null) {
            conversation.setNhanVienId(nhanVienId);
            conversation.setNhanVienTen("Nhân viên #" + nhanVienId);
        }
        return conversation;
    }

    @Override
    public ConversationDTO closeConversation(Long conversationId) {
        ConversationDTO conversation = conversations.get(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("Không tìm thấy cuộc trò chuyện");
        }
        conversation.setTrangThai("DA_DONG");
        conversation.setDangChoPhanHoi(false);
        conversation.setNgayCapNhat(now());
        return conversation;
    }

    private void validateRequest(SendMessageRequest request, boolean forCustomer) {
        if (request == null) {
            throw new IllegalArgumentException("Request không được để trống");
        }
        if (!StringUtils.hasText(request.getNoiDung())) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống");
        }
        if (forCustomer && request.getKhachHangId() == null) {
            throw new IllegalArgumentException("khachHangId không được để trống");
        }
    }

    private ConversationDTO resolveConversationForCustomer(SendMessageRequest request) {
        ConversationDTO conversation = null;
        if (request.getConversationId() != null) {
            conversation = conversations.get(request.getConversationId());
        }
        if (conversation == null && request.getKhachHangId() != null) {
            conversation = conversationsByCustomer.get(request.getKhachHangId());
        }
        if (conversation == null) {
            conversation = getOrCreateConversation(request.getKhachHangId());
        }
        return conversation;
    }

    private ChatMessageDTO buildMessage(SendMessageRequest request, ConversationDTO conversation, String senderType) {
        return ChatMessageDTO.builder()
                .id(messageIdSequence.getAndIncrement())
                .conversationId(conversation.getId())
                .noiDung(request.getNoiDung().trim())
                .loaiNguoiGui(senderType)
                .thoiGianGui(now())
                .tuDongTraLoi("CHATBOT".equals(senderType))
                .daDoc(false)
                .build();
    }

    private void updateUnreadCount(ConversationDTO conversation, Long khachHangId) {
        if (conversation.getMessages() == null) {
            conversation.setSoTinNhanChuaDoc(0);
            return;
        }
        int count = (int) conversation.getMessages().stream()
                .filter(msg -> !Boolean.TRUE.equals(msg.getDaDoc()))
                .filter(msg -> !Objects.equals(msg.getKhachHangId(), khachHangId))
                .count();
        conversation.setSoTinNhanChuaDoc(count);
    }

    private void addAutoReply(ConversationDTO conversation) {
        String reply = "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi trong thời gian sớm nhất.";
        ChatMessageDTO autoMessage = ChatMessageDTO.builder()
                .id(messageIdSequence.getAndIncrement())
                .conversationId(conversation.getId())
                .noiDung(reply)
                .loaiNguoiGui("CHATBOT")
                .thoiGianGui(now())
                .tuDongTraLoi(true)
                .daDoc(false)
                .build();
        conversation.getMessages().add(autoMessage);
        conversation.setNgayCapNhat(now());
    }

    private String now() {
        return LocalDateTime.now().format(formatter);
    }
}

