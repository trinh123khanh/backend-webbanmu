package com.example.backend.service.impl;

import com.example.backend.dto.chat.ChatMessageDTO;
import com.example.backend.dto.chat.ConversationDTO;
import com.example.backend.dto.chat.SendMessageRequest;
import com.example.backend.service.ChatConversationService;
import com.example.backend.service.ChatbotService;
import com.example.backend.service.KhachHangService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Slf4j
public class ChatConversationServiceImpl implements ChatConversationService {

    private final ChatbotService chatbotService;
    private final KhachHangService khachHangService;
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
            // Cập nhật thông tin khách hàng nếu cần
            updateCustomerInfo(existing, khachHangId);
            updateUnreadCount(existing, khachHangId);
            return existing;
        }
        
        // Lấy thông tin khách hàng từ database
        String khachHangTen = "Khách hàng #" + khachHangId;
        String khachHangEmail = null;
        Optional<com.example.backend.dto.KhachHangDTO> khachHangOpt = khachHangService.getKhachHangById(khachHangId);
        if (khachHangOpt.isPresent()) {
            com.example.backend.dto.KhachHangDTO khachHang = khachHangOpt.get();
            if (khachHang.getTenKhachHang() != null && !khachHang.getTenKhachHang().trim().isEmpty()) {
                khachHangTen = khachHang.getTenKhachHang();
            }
            khachHangEmail = khachHang.getEmail();
        }
        
        ConversationDTO conversation = ConversationDTO.builder()
                .id(conversationIdSequence.getAndIncrement())
                .khachHangId(khachHangId)
                .khachHangTen(khachHangTen)
                .khachHangEmail(khachHangEmail)
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
        
        // Tự động gửi tin nhắn chào khi tạo conversation mới
        addWelcomeMessage(conversation);
        
        log.info("Created new conversation for customer {} ({})", khachHangId, khachHangTen);
        return conversation;
    }

    @Override
    public ConversationDTO getConversationById(Long conversationId) {
        ConversationDTO conversation = conversations.get(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("Không tìm thấy cuộc trò chuyện");
        }
        // Cập nhật thông tin khách hàng nếu cần
        if (conversation.getKhachHangId() != null) {
            updateCustomerInfo(conversation, conversation.getKhachHangId());
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

        // Tạo phản hồi tự động: nếu hỏi về sản phẩm thì yêu cầu đợi nhân viên
        addAutoReply(conversation, message.getNoiDung());

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
                .peek(conv -> {
                    // Cập nhật thông tin khách hàng cho mỗi conversation
                    if (conv.getKhachHangId() != null) {
                        updateCustomerInfo(conv, conv.getKhachHangId());
                    }
                })
                .sorted(Comparator.comparing(ConversationDTO::getNgayCapNhat).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationDTO> getStaffConversations(Long nhanVienId) {
        return conversations.values().stream()
                .filter(conv -> nhanVienId == null || Objects.equals(conv.getNhanVienId(), nhanVienId))
                .peek(conv -> {
                    // Cập nhật thông tin khách hàng cho mỗi conversation
                    if (conv.getKhachHangId() != null) {
                        updateCustomerInfo(conv, conv.getKhachHangId());
                    }
                })
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

    /**
     * Thêm tin nhắn chào khi tạo conversation mới
     */
    private void addWelcomeMessage(ConversationDTO conversation) {
        String welcomeMessage = "Xin chào! Tôi là trợ lý AI của cửa hàng. Tôi có thể giúp gì cho bạn?";
        ChatMessageDTO welcomeMsg = ChatMessageDTO.builder()
                .id(messageIdSequence.getAndIncrement())
                .conversationId(conversation.getId())
                .noiDung(welcomeMessage)
                .loaiNguoiGui("CHATBOT")
                .thoiGianGui(now())
                .tuDongTraLoi(true)
                .daDoc(false)
                .build();
        conversation.getMessages().add(welcomeMsg);
        conversation.setNgayCapNhat(now());
    }

    /**
     * Thêm phản hồi tự động
     * Chatbot tự động trả lời dựa trên nội dung tin nhắn của khách hàng
     * Sử dụng ChatbotService để phân tích và truy vấn sản phẩm từ database
     */
    private void addAutoReply(ConversationDTO conversation, String customerMessage) {
        try {
            // ✅ Sử dụng generateReplyWithProducts để lấy cả reply và suggestedProducts
            ChatbotService.ChatbotReply chatbotReply = chatbotService.generateReplyWithProducts(customerMessage);
            String reply = chatbotReply.getReply();
            List<com.example.backend.dto.SanPhamResponse> suggestedProducts = chatbotReply.getSuggestedProducts();
            
            if (suggestedProducts != null && !suggestedProducts.isEmpty()) {
                log.info("Đã lấy {} sản phẩm để gợi ý", suggestedProducts.size());
            }
            
            ChatMessageDTO.ChatMessageDTOBuilder messageBuilder = ChatMessageDTO.builder()
                    .id(messageIdSequence.getAndIncrement())
                    .conversationId(conversation.getId())
                    .noiDung(reply)
                    .loaiNguoiGui("CHATBOT")
                    .thoiGianGui(now())
                    .tuDongTraLoi(true)
                    .daDoc(false);
            
            // Set suggestedProducts nếu có
            if (suggestedProducts != null && !suggestedProducts.isEmpty()) {
                messageBuilder.suggestedProducts(suggestedProducts);
            }
            
            ChatMessageDTO autoMessage = messageBuilder.build();
            conversation.getMessages().add(autoMessage);
            conversation.setNgayCapNhat(now());
        } catch (Exception e) {
            log.error("Lỗi khi tạo phản hồi tự động cho tin nhắn: {}", customerMessage, e);
            // Gửi tin nhắn lỗi thân thiện cho khách hàng
            String errorReply = "Xin lỗi, tôi gặp lỗi khi xử lý câu hỏi của bạn. Vui lòng thử lại sau hoặc liên hệ nhân viên để được hỗ trợ.";
            ChatMessageDTO errorMessage = ChatMessageDTO.builder()
                    .id(messageIdSequence.getAndIncrement())
                    .conversationId(conversation.getId())
                    .noiDung(errorReply)
                    .loaiNguoiGui("CHATBOT")
                    .thoiGianGui(now())
                    .tuDongTraLoi(true)
                    .daDoc(false)
                    .build();
            conversation.getMessages().add(errorMessage);
            conversation.setNgayCapNhat(now());
        }
    }


    /**
     * Cập nhật thông tin khách hàng từ database
     */
    private void updateCustomerInfo(ConversationDTO conversation, Long khachHangId) {
        try {
            Optional<com.example.backend.dto.KhachHangDTO> khachHangOpt = khachHangService.getKhachHangById(khachHangId);
            if (khachHangOpt.isPresent()) {
                com.example.backend.dto.KhachHangDTO khachHang = khachHangOpt.get();
                
                // Cập nhật tên khách hàng nếu có
                if (khachHang.getTenKhachHang() != null && !khachHang.getTenKhachHang().trim().isEmpty()) {
                    conversation.setKhachHangTen(khachHang.getTenKhachHang());
                }
                
                // Cập nhật email khách hàng nếu có
                if (khachHang.getEmail() != null && !khachHang.getEmail().trim().isEmpty()) {
                    conversation.setKhachHangEmail(khachHang.getEmail());
                }
            }
        } catch (Exception e) {
            log.warn("Không thể cập nhật thông tin khách hàng {}: {}", khachHangId, e.getMessage());
        }
    }

    private String now() {
        return LocalDateTime.now().format(formatter);
    }
}
//abcdef

