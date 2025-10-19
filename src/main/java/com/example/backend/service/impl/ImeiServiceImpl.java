package com.example.backend.service.impl;

import com.example.backend.dto.ImeiRequest;
import com.example.backend.dto.ImeiResponse;
import com.example.backend.entity.Imei;
import com.example.backend.entity.SanPham;
import com.example.backend.repository.ImeiRepository;
import com.example.backend.repository.SanPhamRepository;
import com.example.backend.service.ImeiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class ImeiServiceImpl implements ImeiService {
    
    @Autowired
    private ImeiRepository imeiRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    private static final Pattern IMEI_PATTERN = Pattern.compile("^[0-9]{15}$");
    
    @Override
    public ImeiResponse create(ImeiRequest request) {
        // Validate IMEI format
        if (!isValidImei(request.getSoImei())) {
            throw new IllegalArgumentException("IMEI phải có đúng 15 chữ số");
        }
        
        // Check if IMEI already exists
        if (imeiRepository.existsBySoImei(request.getSoImei())) {
            throw new IllegalArgumentException("IMEI đã tồn tại trong hệ thống");
        }
        
        // Find product
        SanPham sanPham = sanPhamRepository.findById(request.getSanPhamId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        
        // Create IMEI entity
        Imei imei = new Imei();
        imei.setSoImei(request.getSoImei());
        imei.setSanPham(sanPham);
        imei.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        
        Imei savedImei = imeiRepository.save(imei);
        return convertToResponse(savedImei);
    }
    
    @Override
    public List<ImeiResponse> createMultiple(List<ImeiRequest> requests) {
        List<ImeiResponse> responses = new ArrayList<>();
        
        for (ImeiRequest request : requests) {
            try {
                ImeiResponse response = create(request);
                responses.add(response);
            } catch (Exception e) {
                // Log error but continue with other IMEIs
                System.err.println("Lỗi khi tạo IMEI " + request.getSoImei() + ": " + e.getMessage());
            }
        }
        
        return responses;
    }
    
    @Override
    @Transactional(readOnly = true)
    public ImeiResponse getById(Long id) {
        Imei imei = imeiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy IMEI"));
        return convertToResponse(imei);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ImeiResponse> getBySanPhamId(Long sanPhamId) {
        List<Imei> imeis = imeiRepository.findBySanPhamId(sanPhamId);
        return imeis.stream()
                .map(this::convertToResponse)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ImeiResponse> getBySanPhamId(Long sanPhamId, Pageable pageable) {
        Page<Imei> imeiPage = imeiRepository.findBySanPhamId(sanPhamId, pageable);
        return imeiPage.map(this::convertToResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ImeiResponse> getAvailableBySanPhamId(Long sanPhamId) {
        List<Imei> imeis = imeiRepository.findBySanPhamIdAndTrangThaiTrue(sanPhamId);
        return imeis.stream()
                .map(this::convertToResponse)
                .toList();
    }
    
    @Override
    public ImeiResponse updateStatus(Long id, Boolean trangThai) {
        Imei imei = imeiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy IMEI"));
        
        imei.setTrangThai(trangThai);
        Imei updatedImei = imeiRepository.save(imei);
        return convertToResponse(updatedImei);
    }
    
    @Override
    public void delete(Long id) {
        if (!imeiRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy IMEI");
        }
        imeiRepository.deleteById(id);
    }
    
    @Override
    public void deleteBySanPhamId(Long sanPhamId) {
        imeiRepository.deleteBySanPhamId(sanPhamId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsBySoImei(String soImei) {
        return imeiRepository.existsBySoImei(soImei);
    }
    
    @Override
    public boolean isValidImei(String soImei) {
        return soImei != null && IMEI_PATTERN.matcher(soImei).matches();
    }
    
    @Override
    public List<ImeiResponse> importImeiList(Long sanPhamId, List<String> imeiList) {
        List<ImeiRequest> requests = new ArrayList<>();
        
        for (String imei : imeiList) {
            if (isValidImei(imei)) {
                ImeiRequest request = new ImeiRequest();
                request.setSoImei(imei);
                request.setSanPhamId(sanPhamId);
                request.setTrangThai(true);
                requests.add(request);
            }
        }
        
        return createMultiple(requests);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ImeiResponse> searchBySoImei(String soImei, Pageable pageable) {
        List<Imei> imeis = imeiRepository.findBySoImeiContaining(soImei);
        // Convert to Page manually since we don't have Page support in repository
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), imeis.size());
        List<Imei> pageContent = imeis.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent.stream().map(this::convertToResponse).toList(),
            pageable,
            imeis.size()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getImeiStatsBySanPham(Long sanPhamId) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalImei = imeiRepository.findBySanPhamId(sanPhamId).size();
        long availableImei = imeiRepository.countBySanPhamIdAndTrangThaiTrue(sanPhamId);
        long soldImei = totalImei - availableImei;
        
        stats.put("totalImei", totalImei);
        stats.put("availableImei", availableImei);
        stats.put("soldImei", soldImei);
        stats.put("sanPhamId", sanPhamId);
        
        return stats;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getImeiOverviewStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalImei = imeiRepository.count();
        long availableImei = imeiRepository.countByTrangThaiTrue();
        long soldImei = totalImei - availableImei;
        
        stats.put("totalImei", totalImei);
        stats.put("availableImei", availableImei);
        stats.put("soldImei", soldImei);
        
        return stats;
    }
    
    @Override
    public byte[] exportImeiBySanPham(Long sanPhamId) {
        List<Imei> imeis = imeiRepository.findBySanPhamId(sanPhamId);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            
            // Write CSV header
            writer.write("IMEI,Trạng thái,Ngày tạo,Ngày cập nhật\n");
            
            // Write data
            for (Imei imei : imeis) {
                writer.write(String.format("%s,%s,%s,%s\n",
                    imei.getSoImei(),
                    imei.getTrangThai() ? "Còn hàng" : "Đã bán",
                    imei.getNgayTao(),
                    imei.getNgayCapNhat()
                ));
            }
            
            writer.flush();
            return baos.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi export CSV", e);
        }
    }
    
    @Override
    public List<ImeiResponse> updateBulkStatus(List<Long> imeiIds, Boolean trangThai) {
        List<ImeiResponse> responses = new ArrayList<>();
        
        for (Long id : imeiIds) {
            try {
                Imei imei = imeiRepository.findById(id).orElse(null);
                if (imei != null) {
                    imei.setTrangThai(trangThai);
                    Imei updatedImei = imeiRepository.save(imei);
                    responses.add(convertToResponse(updatedImei));
                }
            } catch (Exception e) {
                // Log error but continue with other IMEIs
                System.err.println("Lỗi khi cập nhật IMEI " + id + ": " + e.getMessage());
            }
        }
        
        return responses;
    }
    
    private ImeiResponse convertToResponse(Imei imei) {
        ImeiResponse response = new ImeiResponse();
        response.setId(imei.getId());
        response.setSoImei(imei.getSoImei());
        response.setSanPhamId(imei.getSanPham().getId());
        response.setSanPhamTen(imei.getSanPham().getTenSanPham());
        response.setSanPhamMa(imei.getSanPham().getMaSanPham());
        response.setTrangThai(imei.getTrangThai());
        response.setNgayTao(imei.getNgayTao());
        response.setNgayCapNhat(imei.getNgayCapNhat());
        return response;
    }
}
