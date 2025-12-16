package com.example.backend.controller;

import com.example.backend.dto.chat.ChatMessageDTO;
import com.example.backend.dto.chat.ConversationDTO;
import com.example.backend.dto.chat.SendMessageRequest;
import com.example.backend.service.ChatConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"}, allowedHeaders = "*")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatConversationService chatConversationService;

    @GetMapping("/customer/conversation")
    public ResponseEntity<ConversationDTO> getOrCreateConversation(@RequestParam Long khachHangId) {
        log.debug("Fetching conversation for customer {}", khachHangId);
        return ResponseEntity.ok(chatConversationService.getOrCreateConversation(khachHangId));
    }

    @PostMapping("/customer/message")
    public ResponseEntity<ChatMessageDTO> sendCustomerMessage(@RequestBody SendMessageRequest request) {
        log.debug("Customer sending message: {}", request);
        return ResponseEntity.ok(chatConversationService.sendCustomerMessage(request));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ConversationDTO> getConversationById(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatConversationService.getConversationById(conversationId));
    }

    @GetMapping("/staff/conversations/waiting")
    public ResponseEntity<List<ConversationDTO>> getWaitingConversations() {
        return ResponseEntity.ok(chatConversationService.getWaitingConversations());
    }

    @GetMapping("/staff/conversations")
    public ResponseEntity<List<ConversationDTO>> getStaffConversations(@RequestParam(required = false) Long nhanVienId) {
        return ResponseEntity.ok(chatConversationService.getStaffConversations(nhanVienId));
    }

    @PostMapping("/staff/message")
    public ResponseEntity<ChatMessageDTO> sendStaffMessage(@RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(chatConversationService.sendStaffMessage(request));
    }

    @PostMapping("/staff/conversation/{conversationId}/assign")
    public ResponseEntity<ConversationDTO> assignConversation(@PathVariable Long conversationId,
                                                              @RequestParam Long nhanVienId) {
        return ResponseEntity.ok(chatConversationService.assignConversationToStaff(conversationId, nhanVienId));
    }

    @PostMapping("/conversation/{conversationId}/mark-read")
    public ResponseEntity<ConversationDTO> markMessagesAsRead(@PathVariable Long conversationId,
                                                              @RequestParam(required = false) Long nhanVienId) {
        return ResponseEntity.ok(chatConversationService.markMessagesAsRead(conversationId, nhanVienId));
    }

    @PostMapping("/conversation/{conversationId}/close")
    public ResponseEntity<ConversationDTO> closeConversation(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatConversationService.closeConversation(conversationId));
    }
}



