package com.example.backend.service;

import com.example.backend.dto.chat.ChatMessageDTO;
import com.example.backend.dto.chat.ConversationDTO;
import com.example.backend.dto.chat.SendMessageRequest;

import java.util.List;

public interface ChatConversationService {
    ConversationDTO getOrCreateConversation(Long khachHangId);

    ConversationDTO getConversationById(Long conversationId);

    ChatMessageDTO sendCustomerMessage(SendMessageRequest request);

    ChatMessageDTO sendStaffMessage(SendMessageRequest request);

    List<ConversationDTO> getWaitingConversations();

    List<ConversationDTO> getStaffConversations(Long nhanVienId);

    ConversationDTO assignConversationToStaff(Long conversationId, Long nhanVienId);

    ConversationDTO markMessagesAsRead(Long conversationId, Long nhanVienId);

    ConversationDTO closeConversation(Long conversationId);
}

