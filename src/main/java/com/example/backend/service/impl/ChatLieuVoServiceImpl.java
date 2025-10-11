package com.example.backend.service.impl;

import com.example.backend.dto.ChatLieuVoRequest;
import com.example.backend.dto.ChatLieuVoResponse;
import com.example.backend.entity.ChatLieuVo;
import com.example.backend.repository.ChatLieuVoRepository;
import com.example.backend.service.ChatLieuVoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ChatLieuVoServiceImpl implements ChatLieuVoService {

    private final ChatLieuVoRepository repository;

    public ChatLieuVoServiceImpl(ChatLieuVoRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChatLieuVoResponse create(ChatLieuVoRequest request) {
        if (!StringUtils.hasText(request.getTenChatLieu())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên chất liệu là bắt buộc");
        }
        if (repository.existsByTenChatLieu(request.getTenChatLieu())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên chất liệu đã tồn tại");
        }
        ChatLieuVo e = new ChatLieuVo();
        e.setTenChatLieu(request.getTenChatLieu());
        e.setMoTa(request.getMoTa());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public ChatLieuVoResponse update(Long id, ChatLieuVoRequest request) {
        ChatLieuVo e = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chất liệu"));
        if (!StringUtils.hasText(request.getTenChatLieu())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên chất liệu là bắt buộc");
        }
        if (repository.existsByTenChatLieu(request.getTenChatLieu()) && !request.getTenChatLieu().equals(e.getTenChatLieu())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên chất liệu đã tồn tại");
        }
        e.setTenChatLieu(request.getTenChatLieu());
        e.setMoTa(request.getMoTa());
        e.setTrangThai(Boolean.TRUE.equals(request.getTrangThai()));
        return map(repository.save(e));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatLieuVoResponse getById(Long id) {
        return repository.findById(id).map(this::map).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chất liệu"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatLieuVoResponse> search(String keyword, Boolean trangThai, Pageable pageable) {
        return repository.search(keyword, trangThai, pageable).map(this::map);
    }

    @Override
    public void fixSequence() {
        repository.fixSequence();
    }

    private ChatLieuVoResponse map(ChatLieuVo e) {
        ChatLieuVoResponse r = new ChatLieuVoResponse();
        r.setId(e.getId());
        r.setTenChatLieu(e.getTenChatLieu());
        r.setMoTa(e.getMoTa());
        r.setTrangThai(e.getTrangThai());
        return r;
    }
}


