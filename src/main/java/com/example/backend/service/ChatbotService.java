package com.example.backend.service;

import com.example.backend.dto.BestSellingProductDTO;
import com.example.backend.dto.ChiTietSanPhamRequest;
import com.example.backend.dto.ChiTietSanPhamResponse;
import com.example.backend.dto.SanPhamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.example.backend.dto.chat.ChatbotReply;

/**
 * Service xử lý logic chatbot AI
 * Có thể phân tích câu hỏi và truy vấn sản phẩm từ database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final SanPhamService sanPhamService;
    private final ChiTietSanPhamService chiTietSanPhamService;
    private final StatisticsService statisticsService;

    /**
     * Phân tích câu hỏi và tạo phản hồi thông minh dựa trên dữ liệu sản phẩm (có kèm suggested products)
     */
    public ChatbotReply generateReplyWithProducts(String customerMessage) {
        if (customerMessage == null || customerMessage.trim().isEmpty()) {
            return ChatbotReply.builder()
                    .replyText("TDK Xin chào quý khách ạ ! mình có thắc mắc hay cần gợi ý sản phẩm gì có thể đưa ra em sẽ giúp mình giải đáp ạ?")
                    .suggestedProducts(null)
                    .build();
        }

        String normalizedMessage = normalizeText(customerMessage);
        log.debug("Processing customer message: {}", normalizedMessage);

        // Kiểm tra greeting
        if (isGreeting(normalizedMessage)) {
            return ChatbotReply.builder()
                    .replyText("TDK Xin chào quý khách ạ ! mình có thắc mắc hay cần gợi ý sản phẩm gì có thể đưa ra em sẽ giúp mình giải đáp ạ?")
                    .suggestedProducts(null)
                    .build();
        }

        // Kiểm tra câu hỏi về giá (có thể có products)
        if (isPriceQuestion(normalizedMessage)) {
            return handlePriceQuestionWithProducts(normalizedMessage);
        }

        // Kiểm tra câu hỏi về thuộc tính sản phẩm (có thể có products)
        if (isAttributeQuestion(normalizedMessage)) {
            return handleAttributeQuestionWithProducts(normalizedMessage);
        }

        // Kiểm tra câu hỏi về sản phẩm cụ thể
        if (isProductSearchQuestion(normalizedMessage)) {
            return handleProductSearchWithProducts(normalizedMessage);
        }

        // Kiểm tra câu hỏi về sản phẩm rẻ nhất/đắt nhất
        if (isCheapestOrMostExpensiveQuestion(normalizedMessage)) {
            return handleCheapestOrMostExpensiveWithProducts(normalizedMessage);
        }

        // Kiểm tra câu hỏi về tồn kho
        if (isStockQuestion(normalizedMessage)) {
            return ChatbotReply.builder()
                    .replyText(handleStockQuestion(normalizedMessage))
                    .suggestedProducts(null)
                    .build();
        }

        // Câu hỏi chung về sản phẩm
        if (isProductRelated(normalizedMessage)) {
            return handleGeneralProductQuestionWithProducts(normalizedMessage);
        }

        // Phản hồi mặc định
        return ChatbotReply.builder()
                .replyText("Cảm ơn Anh Chị đã liên hệ ạ! Em có thể giúp Anh Chị tìm kiếm sản phẩm, xem giá cả, hoặc tư vấn về các loại mũ bảo hiểm ạ. Anh Chị muốn biết gì cụ thể ạ?")
                .suggestedProducts(null)
                .build();
    }

    /**
     * Phân tích câu hỏi và tạo phản hồi thông minh dựa trên dữ liệu sản phẩm (backward compatible)
     */
    public String generateReply(String customerMessage) {
        if (customerMessage == null || customerMessage.trim().isEmpty()) {
            return "TDK Xin chào quý khách ạ ! mình có thắc mắc hay cần gợi ý sản phẩm gì có thể đưa ra em sẽ giúp mình giải đáp ạ?";
        }

        String normalizedMessage = normalizeText(customerMessage);
        log.debug("Processing customer message: {}", normalizedMessage);

        // Kiểm tra greeting
        if (isGreeting(normalizedMessage)) {
            return "TDK Xin chào quý khách ạ ! mình có thắc mắc hay cần gợi ý sản phẩm gì có thể đưa ra em sẽ giúp mình giải đáp ạ?";
        }

        // Kiểm tra câu hỏi về thuộc tính sản phẩm
        if (isAttributeQuestion(normalizedMessage)) {
            
            return handleAttributeQuestion(normalizedMessage);
        }

        // Kiểm tra câu hỏi về giá
        if (isPriceQuestion(normalizedMessage)) {
            return handlePriceQuestion(normalizedMessage);
        }

        // Kiểm tra câu hỏi về sản phẩm cụ thể
        if (isProductSearchQuestion(normalizedMessage)) {
            return handleProductSearch(normalizedMessage);
        }

        // Kiểm tra câu hỏi về sản phẩm rẻ nhất/đắt nhất
        if (isCheapestOrMostExpensiveQuestion(normalizedMessage)) {
            return handleCheapestOrMostExpensive(normalizedMessage);
        }

        // Kiểm tra câu hỏi về tồn kho
        if (isStockQuestion(normalizedMessage)) {
            return handleStockQuestion(normalizedMessage);
        }

        // Câu hỏi chung về sản phẩm
        if (isProductRelated(normalizedMessage)) {
            return handleGeneralProductQuestion();
        }

        // Phản hồi mặc định
        return "Cảm ơn Anh Chị đã liên hệ ạ! Em có thể giúp Anh Chị tìm kiếm sản phẩm, xem giá cả, hoặc tư vấn về các loại mũ bảo hiểm ạ. Anh Chị muốn biết gì cụ thể ạ?";
    }

    /**
     * Xử lý câu hỏi về giá
     * Tìm kiếm từ ChiTietSanPham dựa trên giá bán, sau đó lấy sản phẩm gợi ý
     */
    private String handlePriceQuestion(String message) {
        try {
            // Lấy tất cả chi tiết sản phẩm
            List<ChiTietSanPhamResponse> allChiTiet = chiTietSanPhamService.getAll();
            log.debug("Tổng số chi tiết sản phẩm: {}", allChiTiet.size());
            
            // Lọc chỉ lấy các chi tiết có trạng thái active
            List<ChiTietSanPhamResponse> activeChiTiet = allChiTiet.stream()
                    .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai())
                    .collect(Collectors.toList());
            log.debug("Số chi tiết sản phẩm active: {}", activeChiTiet.size());

            if (activeChiTiet.isEmpty()) {
                return "Hiện tại shop em chưa có sản phẩm này trong kho ạ, Anh Chị có thể lựa chọn những sản phẩm khác nhé ạ!";
            }

            // Parse giá từ String sang BigDecimal và tìm min/max
            List<BigDecimal> prices = new ArrayList<>();
            int parseFailedCount = 0;
            for (ChiTietSanPhamResponse ct : activeChiTiet) {
                String giaBanStr = ct.getGiaBan();
                if (giaBanStr == null || giaBanStr.trim().isEmpty()) {
                    parseFailedCount++;
                    continue;
                }
                
                BigDecimal price = parsePriceFromString(giaBanStr);
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    prices.add(price);
                } else {
                    parseFailedCount++;
                    log.debug("Không thể parse giá từ ChiTietSanPham ID {}: '{}'", ct.getId(), giaBanStr);
                }
            }
            log.debug("Số giá hợp lệ sau khi parse: {} / {} (thất bại: {})", prices.size(), activeChiTiet.size(), parseFailedCount);
            
            // Log một vài giá mẫu để debug
            if (!prices.isEmpty()) {
                log.debug("Mẫu giá (5 giá đầu): {}", prices.stream().limit(5).map(BigDecimal::toString).collect(Collectors.joining(", ")));
            }

            if (prices.isEmpty()) {
                log.warn("Không có giá hợp lệ nào được parse từ {} chi tiết sản phẩm", activeChiTiet.size());
                return "Hiện tại shop em chưa có sản phẩm này trong kho ạ, Anh Chị có thể lựa chọn những sản phẩm khác nhé ạ!";
            }

            BigDecimal minPrice = prices.stream()
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal maxPrice = prices.stream()
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            
            log.debug("Giá min: {}, max: {}", minPrice, maxPrice);

            // Kiểm tra xem có hỏi theo khoảng giá không
            PriceRange priceRange = extractPriceRange(message);

            if (priceRange == null && containsApproximateKeyword(message)) {
                BigDecimal approxPrice = extractPriceFromMessage(message);
                if (approxPrice != null) {
                    priceRange = createApproximateRange(approxPrice);
                }
            }

            if (priceRange != null && priceRange.isValid()) {
                final PriceRange rangeFilter = priceRange;
                log.debug("Tìm kiếm trong khoảng giá: {} - {}", rangeFilter.getMin(), rangeFilter.getMax());
                
                // Lọc chi tiết sản phẩm theo khoảng giá
                List<ChiTietSanPhamResponse> matchingChiTiet = new ArrayList<>();
                int filterFailedCount = 0;
                for (ChiTietSanPhamResponse ct : activeChiTiet) {
                    String giaBanStr = ct.getGiaBan();
                    if (giaBanStr == null || giaBanStr.trim().isEmpty()) {
                        filterFailedCount++;
                        continue;
                    }
                    
                    BigDecimal price = parsePriceFromString(giaBanStr);
                    if (price != null && rangeFilter.contains(price)) {
                        matchingChiTiet.add(ct);
                        log.debug("Chi tiết sản phẩm ID {} có giá {} nằm trong khoảng {} - {}", 
                                ct.getId(), price, rangeFilter.getMin(), rangeFilter.getMax());
                    } else {
                        if (price == null) {
                            filterFailedCount++;
                            log.debug("Không thể parse giá từ ChiTietSanPham ID {}: '{}'", ct.getId(), giaBanStr);
                        } else {
                            log.debug("Chi tiết sản phẩm ID {} có giá {} nằm ngoài khoảng {} - {}", 
                                    ct.getId(), price, rangeFilter.getMin(), rangeFilter.getMax());
                        }
                    }
                }
                
                log.debug("Lọc theo khoảng giá: tìm thấy {} / {} chi tiết (thất bại parse: {})", 
                        matchingChiTiet.size(), activeChiTiet.size(), filterFailedCount);
                
                // Sắp xếp theo giá
                matchingChiTiet.sort((ct1, ct2) -> {
                    BigDecimal p1 = parsePriceFromString(ct1.getGiaBan());
                    BigDecimal p2 = parsePriceFromString(ct2.getGiaBan());
                    if (p1 == null || p2 == null) return 0;
                    return p1.compareTo(p2);
                });
                
                matchingChiTiet = matchingChiTiet.stream().limit(10).collect(Collectors.toList());
                log.debug("Sau khi giới hạn: {} chi tiết sản phẩm phù hợp", matchingChiTiet.size());

                if (!matchingChiTiet.isEmpty()) {
                    // Lấy danh sách sanPhamId duy nhất
                    Set<Long> sanPhamIds = matchingChiTiet.stream()
                            .map(ChiTietSanPhamResponse::getSanPhamId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());

                    // Lấy thông tin sản phẩm gợi ý
                    List<SanPhamResponse> suggestedProducts = new ArrayList<>();
                    for (Long sanPhamId : sanPhamIds) {
                        try {
                            SanPhamResponse product = sanPhamService.getById(sanPhamId);
                            if (product != null && product.getTrangThai() != null && product.getTrangThai()) {
                                // Lấy ảnh từ ChiTietSanPham nếu SanPham không có
                                enrichProductImage(product);
                                suggestedProducts.add(product);
                            }
                        } catch (Exception e) {
                            log.warn("Không tìm thấy sản phẩm với ID: {}", sanPhamId);
                        }
                    }

                    if (!suggestedProducts.isEmpty()) {
                        StringBuilder reply = new StringBuilder();
                        reply.append(String.format("Các sản phẩm trong khoảng %s - %s VNĐ:\n\n",
                                formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax())));
                        
                        // Tạo map để lấy giá từ ChiTietSanPham
                        Map<Long, BigDecimal> chiTietPriceMap = new HashMap<>();
                        for (ChiTietSanPhamResponse ct : matchingChiTiet) {
                            BigDecimal price = parsePriceFromString(ct.getGiaBan());
                            if (price != null && ct.getSanPhamId() != null) {
                                // Lưu giá nhỏ nhất cho mỗi sản phẩm
                                chiTietPriceMap.merge(ct.getSanPhamId(), price, BigDecimal::min);
                            }
                        }
                        
                        for (SanPhamResponse product : suggestedProducts) {
                            reply.append(String.format("• %s", product.getTenSanPham()));
                            // Ưu tiên hiển thị giá từ ChiTietSanPham
                            BigDecimal chiTietPrice = chiTietPriceMap.get(product.getId());
                            if (chiTietPrice != null) {
                                reply.append(String.format(" - %s VNĐ", formatPrice(chiTietPrice)));
                            } else if (product.getGiaBan() != null) {
                                reply.append(String.format(" - %s VNĐ", formatPrice(product.getGiaBan())));
                            }
                            if (product.getSoLuongTon() != null && product.getSoLuongTon() > 0) {
                                reply.append(String.format(" (Còn %d sp)", product.getSoLuongTon()));
                            }
                            reply.append("\n");
                        }
                        if (matchingChiTiet.size() >= 10) {
                            reply.append("\nAnh Chị có muốn xem thêm sản phẩm trong khoảng giá này không ạ?");
                        }
                        return addAIfNeeded(reply.toString());
                    }
                }
                
                // Log thông tin debug khi không tìm thấy
                log.warn("Không tìm thấy sản phẩm trong khoảng {} - {} VNĐ. Giá trong DB dao động từ {} đến {} VNĐ", 
                        formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax()),
                        formatPrice(minPrice), formatPrice(maxPrice));
                log.debug("Tổng số chi tiết sản phẩm active: {}, số giá hợp lệ: {}", activeChiTiet.size(), prices.size());
                
                return String.format("Hiện tại shop em chưa có sản phẩm nào trong khoảng %s - %s VNĐ ạ. Giá hiện dao động từ %s đến %s VNĐ ạ. Anh Chị có thể tham khảo một số các sản phẩm hot bên em ạ.",
                        formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax()),
                        formatPrice(minPrice), formatPrice(maxPrice));
            }

            // Kiểm tra xem có hỏi về giá cụ thể không
            BigDecimal askedPrice = extractPriceFromMessage(message);
            if (askedPrice != null && askedPrice.compareTo(BigDecimal.ZERO) > 0) {
                log.debug("Tìm kiếm sản phẩm dưới giá: {}", askedPrice);
                
                // Lọc chi tiết sản phẩm có giá <= askedPrice
                List<ChiTietSanPhamResponse> matchingChiTiet = new ArrayList<>();
                int filterFailedCount = 0;
                for (ChiTietSanPhamResponse ct : activeChiTiet) {
                    String giaBanStr = ct.getGiaBan();
                    if (giaBanStr == null || giaBanStr.trim().isEmpty()) {
                        filterFailedCount++;
                        continue;
                    }
                    
                    BigDecimal price = parsePriceFromString(giaBanStr);
                    if (price != null && price.compareTo(askedPrice) <= 0) {
                        matchingChiTiet.add(ct);
                        log.debug("Chi tiết sản phẩm ID {} có giá {} <= {}", ct.getId(), price, askedPrice);
                    } else {
                        if (price == null) {
                            filterFailedCount++;
                            log.debug("Không thể parse giá từ ChiTietSanPham ID {}: '{}'", ct.getId(), giaBanStr);
                        } else {
                            log.debug("Chi tiết sản phẩm ID {} có giá {} > {}", ct.getId(), price, askedPrice);
                        }
                    }
                }
                
                log.debug("Lọc theo giá cụ thể: tìm thấy {} / {} chi tiết dưới giá {} (thất bại parse: {})", 
                        matchingChiTiet.size(), activeChiTiet.size(), askedPrice, filterFailedCount);
                
                matchingChiTiet = matchingChiTiet.stream().limit(10).collect(Collectors.toList());
                log.debug("Sau khi giới hạn: {} chi tiết sản phẩm dưới giá {}", matchingChiTiet.size(), askedPrice);

                if (!matchingChiTiet.isEmpty()) {
                    // Lấy danh sách sanPhamId duy nhất
                    Set<Long> sanPhamIds = matchingChiTiet.stream()
                            .map(ChiTietSanPhamResponse::getSanPhamId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());

                    // Lấy thông tin sản phẩm gợi ý
                    List<SanPhamResponse> suggestedProducts = new ArrayList<>();
                    for (Long sanPhamId : sanPhamIds) {
                        try {
                            SanPhamResponse product = sanPhamService.getById(sanPhamId);
                            if (product != null && product.getTrangThai() != null && product.getTrangThai()) {
                                // Lấy ảnh từ ChiTietSanPham nếu SanPham không có
                                enrichProductImage(product);
                                suggestedProducts.add(product);
                            }
                        } catch (Exception e) {
                            log.warn("Không tìm thấy sản phẩm với ID: {}", sanPhamId);
                        }
                    }

                    if (!suggestedProducts.isEmpty()) {
                        StringBuilder reply = new StringBuilder();
                        reply.append(String.format("Chúng em có các sản phẩm dưới %s VNĐ:\n\n", formatPrice(askedPrice)));
                        
                        // Tạo map để lấy giá từ ChiTietSanPham
                        Map<Long, BigDecimal> chiTietPriceMap = new HashMap<>();
                        for (ChiTietSanPhamResponse ct : matchingChiTiet) {
                            BigDecimal price = parsePriceFromString(ct.getGiaBan());
                            if (price != null && ct.getSanPhamId() != null) {
                                // Lưu giá nhỏ nhất cho mỗi sản phẩm
                                chiTietPriceMap.merge(ct.getSanPhamId(), price, BigDecimal::min);
                            }
                        }
                        
                        for (SanPhamResponse product : suggestedProducts) {
                            reply.append(String.format("• %s", product.getTenSanPham()));
                            // Ưu tiên hiển thị giá từ ChiTietSanPham
                            BigDecimal chiTietPrice = chiTietPriceMap.get(product.getId());
                            if (chiTietPrice != null) {
                                reply.append(String.format(" - %s VNĐ", formatPrice(chiTietPrice)));
                            } else if (product.getGiaBan() != null) {
                                reply.append(String.format(" - %s VNĐ", formatPrice(product.getGiaBan())));
                            }
                            reply.append("\n");
                        }
                        if (matchingChiTiet.size() >= 10) {
                            reply.append("\nAnh Chị có muốn xem thêm sản phẩm khác không ạ?");
                        }
                        return addAIfNeeded(reply.toString());
                    }
                }
                
                // Log thông tin debug khi không tìm thấy
                log.warn("Không tìm thấy sản phẩm dưới {} VNĐ. Giá trong DB dao động từ {} đến {} VNĐ", 
                        formatPrice(askedPrice), formatPrice(minPrice), formatPrice(maxPrice));
                log.debug("Tổng số chi tiết sản phẩm active: {}, số giá hợp lệ: {}", activeChiTiet.size(), prices.size());
                
                return String.format("Hiện tại chúng em không có sản phẩm nào dưới %s VNĐ ạ. Giá sản phẩm của chúng em dao động từ %s đến %s VNĐ ạ.", 
                        formatPrice(askedPrice), formatPrice(minPrice), formatPrice(maxPrice));
            }

            // Trả về thông tin giá chung
            return String.format("Giá sản phẩm của chúng em dao động từ %s đến %s VNĐ. Anh Chị muốn xem sản phẩm nào cụ thể không ạ ?", 
                    formatPrice(minPrice), formatPrice(maxPrice));

        } catch (Exception e) {
            log.error("Error handling price question", e);
            return "Ôiii không thật sự xin lỗi, em hiện gặp lỗi khi tìm kiếm thông tin giá ạ. Anh Chị vui lòng thử lại sau hoặc liên hệ nhân viên để được hỗ trợ ạ.";
        }
    }

    /**
     * Xử lý câu hỏi về giá và trả về ChatbotReply với suggestedProducts
     */
    private ChatbotReply handlePriceQuestionWithProducts(String message) {
        try {
            List<ChiTietSanPhamResponse> allChiTiet = chiTietSanPhamService.getAll();
            List<ChiTietSanPhamResponse> activeChiTiet = allChiTiet.stream()
                    .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai())
                    .collect(Collectors.toList());

            if (activeChiTiet.isEmpty()) {
                return ChatbotReply.builder()
                        .replyText("Hiện tại shop em chưa có sản phẩm này trong kho ạ, Anh Chị có thể lựa chọn những sản phẩm khác nhé ạ!")
                        .suggestedProducts(null)
                        .build();
            }

            List<BigDecimal> prices = new ArrayList<>();
            for (ChiTietSanPhamResponse ct : activeChiTiet) {
                BigDecimal price = parsePriceFromString(ct.getGiaBan());
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    prices.add(price);
                }
            }

            if (prices.isEmpty()) {
                return ChatbotReply.builder()
                        .replyText("Hiện tại shop em chưa có sản phẩm này trong kho ạ, Anh Chị có thể lựa chọn những sản phẩm khác nhé ạ!")
                        .suggestedProducts(null)
                        .build();
            }

            BigDecimal minPrice = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

            PriceRange priceRange = extractPriceRange(message);
            if (priceRange == null && containsApproximateKeyword(message)) {
                BigDecimal approxPrice = extractPriceFromMessage(message);
                if (approxPrice != null) {
                    priceRange = createApproximateRange(approxPrice);
                }
            }

            if (priceRange != null && priceRange.isValid()) {
                final PriceRange rangeFilter = priceRange;
                List<ChiTietSanPhamResponse> matchingChiTiet = activeChiTiet.stream()
                        .filter(ct -> {
                            BigDecimal price = parsePriceFromString(ct.getGiaBan());
                            return price != null && rangeFilter.contains(price);
                        })
                        .sorted((ct1, ct2) -> {
                            BigDecimal p1 = parsePriceFromString(ct1.getGiaBan());
                            BigDecimal p2 = parsePriceFromString(ct2.getGiaBan());
                            if (p1 == null || p2 == null) return 0;
                            return p1.compareTo(p2);
                        })
                        .limit(10)
                        .collect(Collectors.toList());

                if (!matchingChiTiet.isEmpty()) {
                    Set<Long> sanPhamIds = matchingChiTiet.stream()
                            .map(ChiTietSanPhamResponse::getSanPhamId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());

                    List<SanPhamResponse> suggestedProducts = new ArrayList<>();
                    Map<Long, BigDecimal> chiTietPriceMap = new HashMap<>();
                    
                    for (ChiTietSanPhamResponse ct : matchingChiTiet) {
                        BigDecimal price = parsePriceFromString(ct.getGiaBan());
                        if (price != null && ct.getSanPhamId() != null) {
                            chiTietPriceMap.merge(ct.getSanPhamId(), price, BigDecimal::min);
                        }
                    }

                    for (Long sanPhamId : sanPhamIds) {
                        try {
                            SanPhamResponse product = sanPhamService.getById(sanPhamId);
                            if (product != null && product.getTrangThai() != null && product.getTrangThai()) {
                                BigDecimal chiTietPrice = chiTietPriceMap.get(sanPhamId);
                                if (chiTietPrice != null) {
                                    product.setGiaBan(chiTietPrice);
                                }
                                // Lấy ảnh từ ChiTietSanPham nếu SanPham không có
                                enrichProductImage(product);
                                suggestedProducts.add(product);
                            }
                        } catch (Exception e) {
                            log.warn("Không tìm thấy sản phẩm với ID: {}", sanPhamId);
                        }
                    }

                    if (!suggestedProducts.isEmpty()) {
                        // Đảm bảo tất cả sản phẩm đều có ảnh (nếu có thể)
                        for (SanPhamResponse product : suggestedProducts) {
                            enrichProductImage(product);
                        }
                        
                        String replyText = String.format("Các sản phẩm trong khoảng %s - %s VNĐ:",
                                formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax()));
                        return ChatbotReply.builder()
                                .replyText(replyText)
                                .suggestedProducts(suggestedProducts)
                                .build();
                    }
                }

                return ChatbotReply.builder()
                        .replyText(String.format("Hiện tại shop em chưa có sản phẩm nào trong khoảng %s - %s VNĐ ạ. Giá hiện dao động từ %s đến %s VNĐ ạ.",
                                formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax()),
                                formatPrice(minPrice), formatPrice(maxPrice)))
                        .suggestedProducts(null)
                        .build();
            }

            BigDecimal askedPrice = extractPriceFromMessage(message);
            if (askedPrice != null && askedPrice.compareTo(BigDecimal.ZERO) > 0) {
                List<ChiTietSanPhamResponse> matchingChiTiet = activeChiTiet.stream()
                        .filter(ct -> {
                            BigDecimal price = parsePriceFromString(ct.getGiaBan());
                            return price != null && price.compareTo(askedPrice) <= 0;
                        })
                        .limit(10)
                        .collect(Collectors.toList());

                if (!matchingChiTiet.isEmpty()) {
                    Set<Long> sanPhamIds = matchingChiTiet.stream()
                            .map(ChiTietSanPhamResponse::getSanPhamId)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());

                    List<SanPhamResponse> suggestedProducts = new ArrayList<>();
                    Map<Long, BigDecimal> chiTietPriceMap = new HashMap<>();
                    
                    for (ChiTietSanPhamResponse ct : matchingChiTiet) {
                        BigDecimal price = parsePriceFromString(ct.getGiaBan());
                        if (price != null && ct.getSanPhamId() != null) {
                            chiTietPriceMap.merge(ct.getSanPhamId(), price, BigDecimal::min);
                        }
                    }

                    for (Long sanPhamId : sanPhamIds) {
                        try {
                            SanPhamResponse product = sanPhamService.getById(sanPhamId);
                            if (product != null && product.getTrangThai() != null && product.getTrangThai()) {
                                BigDecimal chiTietPrice = chiTietPriceMap.get(sanPhamId);
                                if (chiTietPrice != null) {
                                    product.setGiaBan(chiTietPrice);
                                }
                                // Lấy ảnh từ ChiTietSanPham nếu SanPham không có
                                enrichProductImage(product);
                                suggestedProducts.add(product);
                            }
                        } catch (Exception e) {
                            log.warn("Không tìm thấy sản phẩm với ID: {}", sanPhamId);
                        }
                    }

                    if (!suggestedProducts.isEmpty()) {
                        // Đảm bảo tất cả sản phẩm đều có ảnh (nếu có thể)
                        for (SanPhamResponse product : suggestedProducts) {
                            enrichProductImage(product);
                        }
                        
                        String replyText = String.format("Chúng em có các sản phẩm dưới %s VNĐ:",
                                formatPrice(askedPrice));
                        return ChatbotReply.builder()
                                .replyText(replyText)
                                .suggestedProducts(suggestedProducts)
                                .build();
                    }
                }

                return ChatbotReply.builder()
                        .replyText(String.format("Hiện tại chúng em không có sản phẩm nào dưới %s VNĐ ạ. Giá sản phẩm của chúng em dao động từ %s đến %s VNĐ ạ.",
                                formatPrice(askedPrice), formatPrice(minPrice), formatPrice(maxPrice)))
                        .suggestedProducts(null)
                        .build();
            }

            return ChatbotReply.builder()
                    .replyText(String.format("Giá sản phẩm của chúng em dao động từ %s đến %s VNĐ. Anh Chị muốn xem sản phẩm nào cụ thể không ạ ?",
                            formatPrice(minPrice), formatPrice(maxPrice)))
                    .suggestedProducts(null)
                    .build();

        } catch (Exception e) {
            log.error("Error handling price question with products", e);
            return ChatbotReply.builder()
                    .replyText("Ôiii không thật sự xin lỗi, em hiện gặp lỗi khi tìm kiếm thông tin giá ạ. Anh Chị vui lòng thử lại sau hoặc liên hệ nhân viên để được hỗ trợ ạ.")
                    .suggestedProducts(null)
                    .build();
        }
    }

    /**
     * Xử lý tìm kiếm sản phẩm với bộ lọc theo giá và tên
     */
    private String handleProductSearch(String message) {
        try {
            // Trích xuất keyword từ câu hỏi
            String keyword = extractProductKeyword(message);
            
            // Trích xuất giá từ câu hỏi nếu có
            BigDecimal priceFilter = extractPriceFromMessage(message);
            PriceRange priceRange = extractPriceRange(message);
            if (priceRange == null && containsApproximateKeyword(message) && priceFilter != null) {
                priceRange = createApproximateRange(priceFilter);
            }
            
            Pageable pageable = PageRequest.of(0, 20); // Tăng số lượng để lọc tốt hơn
            Page<SanPhamResponse> products = sanPhamService.search(keyword, true, pageable);
            List<SanPhamResponse> results = products.getContent();

            // Lọc theo giá nếu có
            if (priceRange != null && priceRange.isValid()) {
                final PriceRange rangeFilter = priceRange;
                results = results.stream()
                        .filter(p -> p.getGiaBan() != null && rangeFilter.contains(p.getGiaBan()))
                        .collect(Collectors.toList());
            } else if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
                results = results.stream()
                        .filter(p -> p.getGiaBan() != null && p.getGiaBan().compareTo(priceFilter) <= 0)
                        .collect(Collectors.toList());
            }

            if (results.isEmpty()) {
                String noResultMessage = "Ôiii không thật sự xin lỗi, em không tìm thấy sản phẩm mà Anh Chị yêu cầu ạ, Anh Chị có thể tham khảo một số mẫu sản phẩm hot bên em giá hợp lí mà bao đẹp bao thoải mái ạ";
                if (keyword != null && !keyword.trim().isEmpty() && !keyword.equals("")) {
                    noResultMessage += String.format(" với từ khóa '%s'", keyword);
                }
                if (priceRange != null && priceRange.isValid()) {
                    noResultMessage += String.format(" trong khoảng %s - %s VNĐ",
                            formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax()));
                } else if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
                    noResultMessage += String.format(" dưới %s VNĐ", formatPrice(priceFilter));
                }
                noResultMessage += ". Anh Chị có thể thử từ khóa khác hoặc điều chỉnh bộ lọc không để tìm kiếm sản phẩm ưng ý đó ạ?";
                return noResultMessage;
            }

            // Giới hạn số lượng kết quả hiển thị
            if (results.size() > 10) {
                results = results.stream().limit(10).collect(Collectors.toList());
            }

            StringBuilder reply = new StringBuilder();
            if (results.size() == 1) {
                SanPhamResponse product = results.get(0);
                reply.append(String.format("Em tìm thấy sản phẩm: %s\n", product.getTenSanPham()));
                reply.append(String.format("Giá: %s VNĐ\n", formatPrice(product.getGiaBan())));
                if (product.getSoLuongTon() != null && product.getSoLuongTon() > 0) {
                    reply.append(String.format("Còn hàng: %d sản phẩm\n", product.getSoLuongTon()));
                } else {
                    reply.append("Hiện tại hết hàng\n");
                }
                if (product.getMoTa() != null && !product.getMoTa().isEmpty()) {
                    reply.append(String.format("Mô tả: %s\n", product.getMoTa()));
                }
            } else {
                reply.append(String.format("Em tìm thấy %d sản phẩm", results.size()));
                if (priceRange != null && priceRange.isValid()) {
                    reply.append(String.format(" trong khoảng %s - %s VNĐ",
                            formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax())));
                } else if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
                    reply.append(String.format(" dưới %s VNĐ", formatPrice(priceFilter)));
                }
                reply.append(":\n\n");
                for (SanPhamResponse product : results) {
                    reply.append(String.format("• %s - %s VNĐ", product.getTenSanPham(), formatPrice(product.getGiaBan())));
                    if (product.getSoLuongTon() != null && product.getSoLuongTon() > 0) {
                        reply.append(String.format(" (Còn %d sản phẩm)", product.getSoLuongTon()));
                    } else {
                        reply.append(" (Hết hàng)");
                    }
                    reply.append("\n");
                }
            }

            return addAIfNeeded(reply.toString());

        } catch (Exception e) {
            log.error("Error handling product search", e);
            return "Ôiii không thật sự xin lỗi, em gặp lỗi khi tìm kiếm sản phẩm ạ. Anh Chị vui lòng thử lại sau ạ.";
        }
    }

    /**
     * Xử lý câu hỏi về sản phẩm rẻ nhất/đắt nhất
     */
    private String handleCheapestOrMostExpensive(String message) {
        try {
            Pageable pageable = PageRequest.of(0, 100);
            Page<SanPhamResponse> products = sanPhamService.search(null, true, pageable);
            List<SanPhamResponse> activeProducts = products.getContent();

            if (activeProducts.isEmpty()) {
                return "Hiện tại shop em chưa có sản phẩm này trong kho ạ, Anh Chị có thể lựa chọn những sản phẩm khác nhé ạ!";
            }

            boolean isCheapest = message.contains("rẻ") || message.contains("re") || 
                                 message.contains("thấp") || message.contains("thap") ||
                                 message.contains("cheap") || message.contains("lowest");

            List<SanPhamResponse> sortedProducts = activeProducts.stream()
                    .filter(p -> p.getGiaBan() != null)
                    .sorted((p1, p2) -> isCheapest ? 
                            p1.getGiaBan().compareTo(p2.getGiaBan()) : 
                            p2.getGiaBan().compareTo(p1.getGiaBan()))
                    .limit(5)
                    .collect(Collectors.toList());

            if (sortedProducts.isEmpty()) {
                return "Ôiii không thật sự xin lỗi, em không tìm thấy sản phẩm phù hợp ạ.";
            }

            StringBuilder reply = new StringBuilder();
            String type = isCheapest ? "rẻ nhất" : "đắt nhất";
            reply.append(String.format("Các sản phẩm %s:\n\n", type));
            for (SanPhamResponse product : sortedProducts) {
                reply.append(String.format("• %s - %s VNĐ\n", product.getTenSanPham(), formatPrice(product.getGiaBan())));
            }

            return addAIfNeeded(reply.toString());

        } catch (Exception e) {
            log.error("Error handling cheapest/most expensive question", e);
            return "Ôiii không thật sự xin lỗi, em gặp lỗi khi tìm kiếm ạ. Anh Chị vui lòng thử lại sau ạ.";
        }
    }

    /**
     * Xử lý câu hỏi về tồn kho
     */
    private String handleStockQuestion(String message) {
        try {
            String keyword = extractProductKeyword(message);
            Pageable pageable = PageRequest.of(0, 10);
            Page<SanPhamResponse> products = sanPhamService.search(keyword, true, pageable);
            List<SanPhamResponse> results = products.getContent();

            if (results.isEmpty()) {
                return "Ôiii không thật sự xin lỗi, em không tìm thấy sản phẩm nào ạ.";
            }

            StringBuilder reply = new StringBuilder();
            reply.append("Tình trạng tồn kho:\n\n");
            for (SanPhamResponse product : results) {
                reply.append(String.format("• %s: ", product.getTenSanPham()));
                if (product.getSoLuongTon() != null && product.getSoLuongTon() > 0) {
                    reply.append(String.format("Còn %d sản phẩm\n", product.getSoLuongTon()));
                } else {
                    reply.append("Hết hàng\n");
                }
            }

            return addAIfNeeded(reply.toString());

        } catch (Exception e) {
            log.error("Error handling stock question", e);
            return "Ôiii không thật sự xin lỗi, em gặp lỗi khi kiểm tra tồn kho ạ. Anh Chị vui lòng thử lại sau ạ.";
        }
    }

    /**
     * Xử lý câu hỏi chung về sản phẩm
     */
    private String handleGeneralProductQuestion() {
        try {
            Pageable pageable = PageRequest.of(0, 5);
            Page<SanPhamResponse> products = sanPhamService.search(null, true, pageable);
            List<SanPhamResponse> featuredProducts = products.getContent();

            if (featuredProducts.isEmpty()) {
                return "Chúng em chuyên bán các loại mũ bảo hiểm chất lượng cao ạ. Anh Chị muốn biết thông tin gì cụ thể ạ?";
            }

            StringBuilder reply = new StringBuilder();
            reply.append("Chúng em có các sản phẩm mũ bảo hiểm đa dạng:\n\n");
            for (SanPhamResponse product : featuredProducts) {
                reply.append(String.format("• %s - %s VNĐ\n", product.getTenSanPham(), formatPrice(product.getGiaBan())));
            }
            reply.append("\nAnh Chị muốn xem thêm sản phẩm nào cụ thể không ạ?");

            return addAIfNeeded(reply.toString());

        } catch (Exception e) {
            log.error("Error handling general product question", e);
            return "Chúng em chuyên bán các loại mũ bảo hiểm chất lượng cao ạ. Anh Chị muốn biết thông tin gì cụ thể ạ?";
        }
    }

    private String handleAttributeQuestion(String normalizedMessage) {
        // Tăng số lượng sản phẩm đọc từ DB để tìm kiếm tốt hơn
        List<SanPhamResponse> activeProducts = fetchActiveProducts(500);
        log.debug("Tìm kiếm thuộc tính từ {} sản phẩm", activeProducts.size());
        
        if (activeProducts.isEmpty()) {
            return "Hiện tại shop em chưa có sản phẩm này trong kho ạ, Anh Chị có thể lựa chọn những sản phẩm khác nhé ạ!";
        }

        if (containsColorKeywords(normalizedMessage)) {
            return handleColorAttributeQuery(normalizedMessage, activeProducts);
        }
        if (containsSizeKeywords(normalizedMessage)) {
            return handleSizeAttributeQuery(normalizedMessage, activeProducts);
        }
        if (containsMaterialKeywords(normalizedMessage)) {
            return handleSimpleAttributeQuery("chất liệu vỏ", normalizedMessage, activeProducts, SanPhamResponse::getChatLieuVoTen);
        }
        if (containsOriginKeywords(normalizedMessage)) {
            return handleSimpleAttributeQuery("xuất xứ", normalizedMessage, activeProducts, SanPhamResponse::getXuatXuTen);
        }
        if (containsWeightKeywords(normalizedMessage)) {
            return handleSimpleAttributeQuery("trọng lượng", normalizedMessage, activeProducts, SanPhamResponse::getTrongLuongTen);
        }
        if (containsHelmetTypeKeywords(normalizedMessage)) {
            return handleSimpleAttributeQuery("loại mũ bảo hiểm", normalizedMessage, activeProducts, SanPhamResponse::getLoaiMuBaoHiemTen);
        }
        if (containsManufacturerKeywords(normalizedMessage)) {
            return handleSimpleAttributeQuery("nhà sản xuất", normalizedMessage, activeProducts, SanPhamResponse::getNhaSanXuatTen);
        }
        if (containsStyleKeywords(normalizedMessage)) {
            return handleSimpleAttributeQuery("kiểu dáng", normalizedMessage, activeProducts, SanPhamResponse::getKieuDangMuTen);
        }
        if (containsSafetyKeywords(normalizedMessage)) {
            return handleSimpleAttributeQuery("công nghệ an toàn", normalizedMessage, activeProducts, SanPhamResponse::getCongNgheAnToanTen);
        }

        return "Em có thể hỗ trợ thông tin về màu sắc, kích thước, chất liệu vỏ, xuất xứ, trọng lượng, loại mũ bảo hiểm, nhà sản xuất, kiểu dáng và công nghệ an toàn ạ. Anh Chị muốn biết rõ hơn về phần nào ạ?";
    }

    private List<SanPhamResponse> fetchActiveProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<SanPhamResponse> products = sanPhamService.search(null, true, pageable).getContent();
        log.debug("Lấy được {} sản phẩm active từ DB", products.size());
        return products;
    }

    /**
     * Xử lý tìm kiếm màu sắc từ ChiTietSanPham
     */
    private String handleColorAttributeQuery(String normalizedMessage, List<SanPhamResponse> products) {
        log.debug("Tìm kiếm màu sắc từ message: {}", normalizedMessage);
        
        // Lấy tất cả chi tiết sản phẩm
        List<ChiTietSanPhamResponse> allChiTiet = chiTietSanPhamService.getAll();
        log.debug("Tổng số chi tiết sản phẩm: {}", allChiTiet.size());
        
        // Lọc chỉ lấy các chi tiết active và có màu sắc
        List<ChiTietSanPhamResponse> activeChiTiet = allChiTiet.stream()
                .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai())
                .filter(ct -> StringUtils.hasText(ct.getMauSacTen()))
                .collect(Collectors.toList());
        log.debug("Số chi tiết có màu sắc: {}", activeChiTiet.size());
        
        // Tạo map sản phẩm để tra cứu nhanh
        Map<Long, SanPhamResponse> productMap = products.stream()
                .filter(p -> p.getId() != null && p.getTrangThai() != null && p.getTrangThai())
                .collect(Collectors.toMap(SanPhamResponse::getId, Function.identity(), (a, b) -> a));
        
        // Trích xuất từ khóa màu từ message
        String colorKeyword = extractColorKeyword(normalizedMessage);
        log.debug("Từ khóa màu được trích xuất: '{}'", colorKeyword);
        
        // Tìm các chi tiết có màu phù hợp
        List<ChiTietSanPhamResponse> matchedDetails = new ArrayList<>();
        Set<String> matchedColors = new LinkedHashSet<>();
        
        for (ChiTietSanPhamResponse detail : activeChiTiet) {
            String colorName = detail.getMauSacTen();
            if (!StringUtils.hasText(colorName)) {
                continue;
            }
            
            // So sánh với nhiều cách khác nhau
            if (matchesColor(normalizedMessage, colorKeyword, colorName)) {
                matchedDetails.add(detail);
                matchedColors.add(colorName);
            }
        }
        
        log.debug("Tìm thấy {} chi tiết với {} màu khác nhau", matchedDetails.size(), matchedColors.size());
        
        if (!matchedDetails.isEmpty()) {
            // Lấy danh sách sanPhamId duy nhất
            Set<Long> sanPhamIds = matchedDetails.stream()
                    .map(ChiTietSanPhamResponse::getSanPhamId)
                    .filter(id -> id != null && productMap.containsKey(id))
                    .collect(Collectors.toSet());
            
            List<SanPhamResponse> suggestedProducts = new ArrayList<>();
            for (Long sanPhamId : sanPhamIds) {
                SanPhamResponse product = productMap.get(sanPhamId);
                if (product != null) {
                    // Lấy ảnh từ ChiTietSanPham nếu SanPham không có
                    enrichProductImage(product);
                    suggestedProducts.add(product);
                }
            }
            
            if (!suggestedProducts.isEmpty()) {
                StringBuilder reply = new StringBuilder();
                reply.append(String.format("Các sản phẩm màu %s:\n\n",
                        matchedColors.stream().limit(3).collect(Collectors.joining(", "))));
                
                // Tạo map để lấy màu từ chi tiết
                Map<Long, Set<String>> productColorsMap = new HashMap<>();
                for (ChiTietSanPhamResponse detail : matchedDetails) {
                    if (detail.getSanPhamId() != null) {
                        productColorsMap.computeIfAbsent(detail.getSanPhamId(), k -> new LinkedHashSet<>())
                                .add(detail.getMauSacTen());
                    }
                }
                
                suggestedProducts.stream().limit(10).forEach(product -> {
                    reply.append("• ").append(product.getTenSanPham() != null ? product.getTenSanPham() : "Sản phẩm");
                    Set<String> colors = productColorsMap.get(product.getId());
                    if (colors != null && !colors.isEmpty()) {
                        reply.append(String.format(" (Màu: %s)", String.join(", ", colors)));
                    }
                    if (product.getGiaBan() != null) {
                        reply.append(String.format(" - %s VNĐ", formatPrice(product.getGiaBan())));
                    }
                    reply.append("\n");
                });
                
                if (suggestedProducts.size() > 10) {
                    reply.append(String.format("\n... và %d sản phẩm khác ạ. Anh Chị muốn xem thêm không ạ?", suggestedProducts.size() - 10));
                }
                
                return addAIfNeeded(reply.toString());
            }
        }
        
        // Nếu không tìm thấy, liệt kê các màu có sẵn
        Set<String> availableColors = activeChiTiet.stream()
                .map(ChiTietSanPhamResponse::getMauSacTen)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        if (!availableColors.isEmpty()) {
            String summary = availableColors.stream().limit(15).collect(Collectors.joining(", "));
            if (availableColors.size() > 15) {
                summary += String.format(" ... (+%d màu khác)", availableColors.size() - 15);
            }
            return String.format("Hiện tại chúng em có các màu sắc: %s ạ. Anh Chị muốn em gửi chi tiết mẫu màu nào ạ?", summary);
        }
        
        return "Ôiii không thật sự xin lỗi, hiện dữ liệu màu sắc đang được cập nhật ạ. Anh Chị có thể hỏi nhân viên để được hỗ trợ nhanh hơn nhé ạ!";
    }
    
    /**
     * Trích xuất từ khóa màu từ message
     */
    private String extractColorKeyword(String normalizedMessage) {
        // Loại bỏ các từ không cần thiết
        String[] stopWords = {
            "màu", "mau", "color", "có", "co", "have", "tìm", "tim", "find",
            "sản phẩm", "san pham", "product", "mũ", "mu", "helmet",
            "cho", "tôi", "toi", "me", "bạn", "ban", "you", "với", "voi", "with"
        };
        
        String keyword = normalizedMessage.toLowerCase(Locale.ROOT);
        for (String stopWord : stopWords) {
            keyword = keyword.replaceAll("\\b" + stopWord + "\\b", " ").trim();
        }
        
        // Lấy các từ còn lại
        String[] words = keyword.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 1) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(word);
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Kiểm tra xem màu có khớp với message không (fuzzy matching)
     */
    private boolean matchesColor(String normalizedMessage, String colorKeyword, String colorName) {
        if (colorName == null || colorName.trim().isEmpty()) {
            return false;
        }
        
        String normalizedColor = normalizeText(colorName);
        String normalizedKeyword = normalizeText(colorKeyword);
        
        // So sánh chính xác
        if (normalizedMessage.contains(normalizedColor) || normalizedColor.contains(normalizedKeyword)) {
            return true;
        }
        
        // So sánh từng từ
        String[] colorWords = normalizedColor.split("\\s+");
        String[] keywordWords = normalizedKeyword.split("\\s+");
        
        for (String colorWord : colorWords) {
            for (String keywordWord : keywordWords) {
                if (colorWord.length() > 2 && keywordWord.length() > 2) {
                    if (colorWord.contains(keywordWord) || keywordWord.contains(colorWord)) {
                        return true;
                    }
                }
            }
        }
        
        // So sánh Levenshtein distance cho các từ ngắn
        if (normalizedColor.length() <= 10 && normalizedKeyword.length() <= 10) {
            int distance = levenshteinDistance(normalizedColor, normalizedKeyword);
            if (distance <= 2 && Math.max(normalizedColor.length(), normalizedKeyword.length()) > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Tính Levenshtein distance giữa 2 chuỗi
     */
    private int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return Integer.MAX_VALUE;
        }
        
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    private String handleSimpleAttributeQuery(String attributeLabel,
                                              String normalizedMessage,
                                              List<SanPhamResponse> products,
                                              Function<SanPhamResponse, String> valueExtractor) {
        log.debug("Tìm kiếm {} từ message: {}", attributeLabel, normalizedMessage);
        
        List<SanPhamResponse> matchedProducts = new ArrayList<>();
        Set<String> matchedValues = new LinkedHashSet<>();

        for (SanPhamResponse product : products) {
            String value = valueExtractor.apply(product);
            if (!StringUtils.hasText(value)) {
                continue;
            }
            String normalizedValue = normalizeText(value);
            if (normalizedValue.length() <= 1) {
                continue;
            }
            
            // Cải thiện logic so sánh
            if (matchesAttributeValue(normalizedMessage, normalizedValue)) {
                matchedProducts.add(product);
                matchedValues.add(value);
            }
        }
        
        log.debug("Tìm thấy {} sản phẩm với {} giá trị khác nhau", matchedProducts.size(), matchedValues.size());

        if (!matchedProducts.isEmpty()) {
            return buildAttributeProductResponse(attributeLabel, matchedValues, matchedProducts, valueExtractor);
        }

        Set<String> availableValues = products.stream()
                .map(valueExtractor)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!availableValues.isEmpty()) {
            String summary = availableValues.stream().limit(15).collect(Collectors.joining(", "));
            if (availableValues.size() > 15) {
                summary += String.format(" ... (+%d tuỳ chọn khác)", availableValues.size() - 15);
            }
            return String.format("Hiện tại chúng em có các %s: %s ạ. Anh Chị muốn em gửi chi tiết mẫu nào ạ?", attributeLabel, summary);
        }

        return String.format("Ôiii không thật sự xin lỗi, hiện dữ liệu %s đang được cập nhật ạ. Anh Chị có thể hỏi nhân viên để được hỗ trợ nhanh hơn nhé ạ!", attributeLabel);
    }
    
    /**
     * So sánh giá trị thuộc tính với message (cải thiện)
     */
    private boolean matchesAttributeValue(String normalizedMessage, String normalizedValue) {
        // So sánh chính xác
        if (normalizedMessage.contains(normalizedValue) || normalizedValue.contains(normalizedMessage)) {
            return true;
        }
        
        // So sánh từng từ
        String[] messageWords = normalizedMessage.split("\\s+");
        String[] valueWords = normalizedValue.split("\\s+");
        
        for (String msgWord : messageWords) {
            for (String valWord : valueWords) {
                if (msgWord.length() > 2 && valWord.length() > 2) {
                    if (msgWord.contains(valWord) || valWord.contains(msgWord)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private String buildAttributeProductResponse(String attributeLabel,
                                                 Set<String> matchedValues,
                                                 List<SanPhamResponse> products,
                                                 Function<SanPhamResponse, String> valueExtractor) {
        StringBuilder reply = new StringBuilder();
        if (!matchedValues.isEmpty()) {
            reply.append(String.format("Các sản phẩm %s %s:\n\n",
                    attributeLabel,
                    matchedValues.stream().limit(3).collect(Collectors.joining(", "))));
        } else {
            reply.append(String.format("Các sản phẩm nổi bật theo %s:\n\n", attributeLabel));
        }

        products.stream().limit(5).forEach(product -> {
            reply.append(formatProductLine(product, attributeLabel, valueExtractor.apply(product)));
        });

        if (products.size() > 5) {
            reply.append(String.format("\n... và %d sản phẩm khác ạ. Anh Chị muốn xem thêm không ạ?", products.size() - 5));
        }

        return addAIfNeeded(reply.toString());
    }

    private String formatProductLine(SanPhamResponse product, String attributeLabel, String attributeValue) {
        StringBuilder line = new StringBuilder("• ")
                .append(product.getTenSanPham() != null ? product.getTenSanPham() : "Sản phẩm");

        if (StringUtils.hasText(attributeValue)) {
            line.append(String.format(" (%s: %s)", attributeLabel, attributeValue));
        }

        if (product.getGiaBan() != null) {
            line.append(String.format(" - %s VNĐ", formatPrice(product.getGiaBan())));
        }

        line.append("\n");
        return line.toString();
    }

    private String handleSizeAttributeQuery(String normalizedMessage, List<SanPhamResponse> products) {
        Map<Long, SanPhamResponse> productMap = products.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(SanPhamResponse::getId, Function.identity(), (a, b) -> a));

        List<ChiTietSanPhamResponse> details = chiTietSanPhamService.getAll();
        List<ChiTietSanPhamResponse> relevantDetails = details.stream()
                .filter(detail -> detail.getSanPhamId() != null && productMap.containsKey(detail.getSanPhamId()))
                .collect(Collectors.toList());

        List<ChiTietSanPhamResponse> matchedDetails = new ArrayList<>();
        Set<String> matchedSizes = new LinkedHashSet<>();

        for (ChiTietSanPhamResponse detail : relevantDetails) {
            String sizeName = detail.getKichThuocTen();
            if (!StringUtils.hasText(sizeName)) {
                continue;
            }
            if (matchesSizeToken(normalizedMessage, sizeName)) {
                matchedDetails.add(detail);
                matchedSizes.add(sizeName);
            }
        }

        if (!matchedDetails.isEmpty()) {
            StringBuilder reply = new StringBuilder();
            reply.append(String.format("Các mẫu phù hợp kích thước %s:\n\n",
                    matchedSizes.stream().collect(Collectors.joining(", "))));
            matchedDetails.stream().limit(5).forEach(detail -> {
                SanPhamResponse product = productMap.get(detail.getSanPhamId());
                reply.append(formatSizeProductLine(product, detail));
            });
            if (matchedDetails.size() > 5) {
                reply.append(String.format("\n... và %d lựa chọn khác ạ. Anh Chị muốn xem thêm không ạ?", matchedDetails.size() - 5));
            }
            return addAIfNeeded(reply.toString());
        }

        Set<String> availableSizes = relevantDetails.stream()
                .map(ChiTietSanPhamResponse::getKichThuocTen)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!availableSizes.isEmpty()) {
            String summary = availableSizes.stream().limit(10).collect(Collectors.joining(", "));
            if (availableSizes.size() > 10) {
                summary += String.format(" ... (+%d size khác)", availableSizes.size() - 10);
            }
            return String.format("Chúng em hiện có các kích thước: %s ạ. Anh Chị muốn em gợi ý mẫu theo size nào ạ?", summary);
        }

        return "Em đang cập nhật dữ liệu kích thước chi tiết ạ. Anh Chị có thể để lại thông tin để nhân viên hỗ trợ nhanh hơn nhé ạ!";
    }

    private boolean matchesSizeToken(String normalizedMessage, String sizeLabel) {
        String normalizedSize = normalizeText(sizeLabel).trim();
        if (normalizedSize.isEmpty()) {
            return false;
        }
        if (normalizedSize.length() == 1) {
            return normalizedMessage.contains("size " + normalizedSize)
                    || normalizedMessage.contains("kich thuoc " + normalizedSize)
                    || normalizedMessage.contains("size: " + normalizedSize)
                    || normalizedMessage.matches(".*\\b" + normalizedSize + "\\b.*size.*");
        }
        return normalizedMessage.contains(normalizedSize);
    }

    private String formatSizeProductLine(SanPhamResponse product, ChiTietSanPhamResponse detail) {
        String productName = product != null && StringUtils.hasText(product.getTenSanPham())
                ? product.getTenSanPham()
                : (StringUtils.hasText(detail.getSanPhamTen()) ? detail.getSanPhamTen() : "Sản phẩm");
        String priceText;
        if (StringUtils.hasText(detail.getGiaBan())) {
            priceText = detail.getGiaBan().toLowerCase(Locale.ROOT).contains("vnđ")
                    ? detail.getGiaBan()
                    : detail.getGiaBan() + " VNĐ";
        } else if (product != null && product.getGiaBan() != null) {
            priceText = formatPrice(product.getGiaBan()) + " VNĐ";
        } else {
            priceText = "Giá đang cập nhật";
        }

        StringBuilder line = new StringBuilder("• ")
                .append(productName)
                .append(String.format(" (Kích thước: %s", detail.getKichThuocTen()));

        if (StringUtils.hasText(detail.getMauSacTen())) {
            line.append(String.format(", Màu: %s", detail.getMauSacTen()));
        }

        line.append(String.format(", Giá: %s)\n", priceText));
        return line.toString();
    }

    private boolean isAttributeQuestion(String message) {
        return containsColorKeywords(message)
                || containsSizeKeywords(message)
                || containsMaterialKeywords(message)
                || containsOriginKeywords(message)
                || containsWeightKeywords(message)
                || containsHelmetTypeKeywords(message)
                || containsManufacturerKeywords(message)
                || containsStyleKeywords(message)
                || containsSafetyKeywords(message);
    }

    private boolean containsColorKeywords(String message) {
        return containsAnyKeyword(message, "mau sac", "mau", "color");
    }

    private boolean containsSizeKeywords(String message) {
        return containsAnyKeyword(message, "kich thuoc", "size", "form size");
    }

    private boolean containsMaterialKeywords(String message) {
        return containsAnyKeyword(message, "chat lieu", "chat lieu vo", "vo mu", "shell", "material");
    }

    private boolean containsOriginKeywords(String message) {
        return containsAnyKeyword(message, "xuat xu", "nguon goc", "origin", "made in");
    }

    private boolean containsWeightKeywords(String message) {
        return containsAnyKeyword(message, "trong luong", "can nang", "weight");
    }

    private boolean containsHelmetTypeKeywords(String message) {
        return containsAnyKeyword(message, "loai mu", "dang mu", "helmet type", "fullface", "3/4", "34", "nua dau");
    }

    private boolean containsManufacturerKeywords(String message) {
        return containsAnyKeyword(message, "nha san xuat", "thuong hieu", "brand", "hang");
    }

    private boolean containsStyleKeywords(String message) {
        return containsAnyKeyword(message, "kieu dang", "phong cach", "style", "form");
    }

    private boolean containsSafetyKeywords(String message) {
        return containsAnyKeyword(message, "cong nghe an toan", "an toan", "safe tech", "mips", "ece", "dot");
    }

    private boolean containsAnyKeyword(String message, String... keywords) {
        if (message == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem có phải câu hỏi về giá không
     */
    private boolean isPriceQuestion(String message) {
        String[] priceKeywords = {
            "giá", "gia", "price", "cost", "chi phí", "chi phi",
            "bao nhiêu", "bao nhieu", "how much", "how many",
            "giá cả", "gia ca", "pricing"
        };
        return Arrays.stream(priceKeywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra xem có phải câu hỏi tìm kiếm sản phẩm không
     */
    private boolean isProductSearchQuestion(String message) {
        String[] searchKeywords = {
            "tìm", "tim", "find", "search", "có", "co", "have",
            "sản phẩm", "san pham", "product", "mũ", "mu", "helmet"
        };
        return Arrays.stream(searchKeywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra xem có phải câu hỏi về sản phẩm rẻ nhất/đắt nhất không
     */
    private boolean isCheapestOrMostExpensiveQuestion(String message) {
        String[] keywords = {
            "rẻ nhất", "re nhat", "cheapest", "lowest",
            "đắt nhất", "dat nhat", "most expensive", "highest",
            "giá thấp", "gia thap", "low price",
            "giá cao", "gia cao", "high price"
        };
        return Arrays.stream(keywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra xem có phải câu hỏi về tồn kho không
     */
    private boolean isStockQuestion(String message) {
        String[] stockKeywords = {
            "còn hàng", "con hang", "in stock", "available",
            "hết hàng", "het hang", "out of stock",
            "tồn kho", "ton kho", "stock", "số lượng", "so luong", "quantity"
        };
        return Arrays.stream(stockKeywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra xem có liên quan đến sản phẩm không
     */
    private boolean isProductRelated(String message) {
        String[] productKeywords = {
            "sản phẩm", "san pham", "product",
            "mũ", "mu", "helmet", "nón", "non",
            "mua", "buy", "purchase", "đặt hàng", "dat hang"
        };
        return Arrays.stream(productKeywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra greeting
     */
    private boolean isGreeting(String message) {
        String[] greetings = {
            "xin chào", "chào", "chao", "hello", "hi", "hey",
            "alo", "good morning", "good afternoon", "good evening"
        };
        return Arrays.stream(greetings).anyMatch(message::contains);
    }

    /**
     * Trích xuất keyword sản phẩm từ câu hỏi
     */
    private String extractProductKeyword(String message) {
        // Loại bỏ các từ không cần thiết
        String[] stopWords = {
            "tìm", "tim", "find", "search", "có", "co", "have", "của", "cua",
            "sản phẩm", "san pham", "product", "mũ", "mu", "helmet",
            "giá", "gia", "price", "bao nhiêu", "bao nhieu", "how much",
            "cho", "tôi", "toi", "me", "bạn", "ban", "you", "với", "voi", "with",
            "dưới", "duoi", "under", "trên", "tren", "above", "khoảng", "khoang", "about",
            "vnđ", "vnd", "đồng", "dong", "k", "nghìn", "nghin", "triệu", "trieu"
        };

        String keyword = message.toLowerCase(Locale.ROOT);
        for (String stopWord : stopWords) {
            keyword = keyword.replaceAll("\\b" + stopWord + "\\b", " ").trim();
        }

        // Lấy tất cả các từ có ý nghĩa (dài hơn 2 ký tự)
        String[] words = keyword.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            // Bỏ qua số và từ quá ngắn
            if (word.length() > 2 && !word.matches("\\d+")) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(word);
            }
        }

        String finalKeyword = result.toString().trim();
        // Nếu không tìm thấy keyword, trả về null để search tất cả
        return finalKeyword.isEmpty() ? null : finalKeyword;
    }

    /**
     * Trích xuất giá từ câu hỏi (hỗ trợ nhiều format: 1k=1000, 10k=10000, 1tr=1000000, v.v.)
     * Format: số + k = thêm 3 số 0, số + tr/triệu = thêm 6 số 0 (không có dấu phẩy)
     */
    private BigDecimal extractPriceFromMessage(String message) {
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        
        // Pattern để tìm số với các format khác nhau
        // Tìm: số + k/tr/triệu (không có dấu phẩy giữa số và đơn vị)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(\\d+)\\s*(k|tr|triệu|trieu|nghìn|nghin|vnđ|vnd|đồng|dong)?"
        );
        java.util.regex.Matcher matcher = pattern.matcher(lowerMessage);
        
        BigDecimal maxPrice = null;
        while (matcher.find()) {
            try {
                String numberStr = matcher.group(1); // Lấy số nguyên (không có dấu phẩy)
                String unit = matcher.group(2);
                
                BigDecimal price = new BigDecimal(numberStr);
                
                // Xử lý đơn vị: k = thêm 3 số 0, tr/triệu = thêm 6 số 0
                if (unit != null) {
                    unit = unit.toLowerCase();
                    if (unit.contains("k") || unit.contains("nghìn") || unit.contains("nghin")) {
                        // Thêm 3 số 0: 1k = 1000, 10k = 10000, 100k = 100000
                        price = price.multiply(new BigDecimal("1000"));
                    } else if (unit.contains("tr") || unit.contains("triệu") || unit.contains("trieu")) {
                        // Thêm 6 số 0: 1tr = 1000000, 10tr = 10000000
                        price = price.multiply(new BigDecimal("1000000"));
                    }
                }
                
                // Chấp nhận mọi giá >= 0 (bao gồm cả giá nhỏ như 1k = 1000)
                if (price.compareTo(BigDecimal.ZERO) >= 0) {
                    if (maxPrice == null || price.compareTo(maxPrice) > 0) {
                        maxPrice = price;
                    }
                }
            } catch (Exception e) {
                log.debug("extractPriceFromMessage: Lỗi khi parse giá từ '{}': {}", matcher.group(), e.getMessage());
            }
        }
        
        return maxPrice;
    }

    private PriceRange extractPriceRange(String message) {
        if (message == null) {
            return null;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        // Pattern: số + đơn vị (k/tr) - số + đơn vị (k/tr)
        java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile(
                "(\\d+)\\s*(k|tr|triệu|trieu|nghin|ngan|vnđ|vnd|dong)?\\s*(?:-|->|—|–|to|toi|den|tu|and|&|~)\\s*(\\d+)\\s*(k|tr|triệu|trieu|nghin|ngan|vnđ|vnd|dong)?",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = rangePattern.matcher(normalized);
        if (matcher.find()) {
            BigDecimal startPrice = parsePriceValue(matcher.group(1), matcher.group(2), null);
            BigDecimal endPrice = parsePriceValue(matcher.group(3), matcher.group(4), matcher.group(2));
            if (startPrice != null && endPrice != null) {
                return new PriceRange(startPrice, endPrice);
            }
        }
        return null;
    }
    
    private boolean containsApproximateKeyword(String message) {
        if (message == null) {
            return false;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        String[] approximateKeywords = {
                "khoang", "tam", "xap xi", "xapxi", "uoc", "gan", "range", "approx"
        };
        return java.util.Arrays.stream(approximateKeywords).anyMatch(lower::contains);
    }

    private PriceRange createApproximateRange(BigDecimal centerPrice) {
        if (centerPrice == null || centerPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal tolerance = centerPrice.multiply(new BigDecimal("0.2"));
        BigDecimal minTolerance = new BigDecimal("50000");
        if (tolerance.compareTo(minTolerance) < 0) {
            tolerance = minTolerance;
        }
        BigDecimal min = centerPrice.subtract(tolerance);
        if (min.compareTo(new BigDecimal("1000")) < 0) {
            min = new BigDecimal("1000");
        }
        BigDecimal max = centerPrice.add(tolerance);
        return new PriceRange(min, max);
    }

    private BigDecimal parsePriceValue(String rawNumber, String unit, String fallbackUnit) {
        if (rawNumber == null || rawNumber.isEmpty()) {
            return null;
        }
        // Loại bỏ dấu phẩy, chỉ giữ số nguyên
        String digits = rawNumber.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) {
            return null;
        }
        BigDecimal price = new BigDecimal(digits);
        String resolvedUnit = (unit != null && !unit.isBlank()) ? unit : fallbackUnit;
        if (resolvedUnit != null) {
            String normalizedUnit = resolvedUnit.toLowerCase(Locale.ROOT);
            // k = thêm 3 số 0, tr/triệu = thêm 6 số 0
            if (normalizedUnit.contains("k") || normalizedUnit.contains("nghin") || normalizedUnit.contains("ngan")) {
                price = price.multiply(new BigDecimal("1000"));
            } else if (normalizedUnit.contains("tr") || normalizedUnit.contains("trieu")) {
                price = price.multiply(new BigDecimal("1000000"));
            }
        }
        // Chấp nhận mọi giá >= 0 (không giới hạn tối thiểu 1000)
        return price.compareTo(BigDecimal.ZERO) >= 0 ? price : null;
    }

    private static class PriceRange {
        private final BigDecimal min;
        private final BigDecimal max;

        private PriceRange(BigDecimal first, BigDecimal second) {
            BigDecimal safeFirst = first != null ? first : BigDecimal.ZERO;
            BigDecimal safeSecond = second != null ? second : BigDecimal.ZERO;
            if (safeFirst.compareTo(safeSecond) <= 0) {
                this.min = safeFirst;
                this.max = safeSecond;
            } else {
                this.min = safeSecond;
                this.max = safeFirst;
            }
        }

        boolean contains(BigDecimal price) {
            if (!isValid() || price == null) {
                return false;
            }
            return price.compareTo(min) >= 0 && price.compareTo(max) <= 0;
        }

        boolean isValid() {
            return min != null && max != null && max.compareTo(min) >= 0 && max.compareTo(new BigDecimal("1000")) >= 0;
        }

        BigDecimal getMin() {
            return min;
        }

        BigDecimal getMax() {
            return max;
        }
    }
    /**
     * Format giá tiền
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0";
        }
        return String.format("%,d", price.longValue());
    }

    /**
     * Chuẩn hóa text (loại bỏ dấu, chuyển về lowercase)
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim();
    }

    /**
     * Thêm "ạ" vào cuối câu nếu chưa có
     */
    private String addAIfNeeded(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        String trimmed = text.trim();
        // Nếu đã kết thúc bằng "ạ", "ạ!", "ạ?", "ạ.", thì không thêm nữa
        if (trimmed.endsWith("ạ") || trimmed.endsWith("ạ!") || trimmed.endsWith("ạ?") || trimmed.endsWith("ạ.")) {
            return text;
        }
        // Nếu kết thúc bằng dấu câu, thêm "ạ" trước dấu câu
        if (trimmed.endsWith("!") || trimmed.endsWith("?") || trimmed.endsWith(".")) {
            return trimmed.substring(0, trimmed.length() - 1) + " ạ" + trimmed.charAt(trimmed.length() - 1);
        }
        // Nếu không có dấu câu, thêm " ạ" vào cuối
        return trimmed + " ạ";
    }

    /**
     * Parse giá từ String sang BigDecimal
     * Hỗ trợ nhiều format: "500000", "500,000", "500000 VNĐ", "500k", v.v.
     */
    private BigDecimal parsePriceFromString(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            log.debug("parsePriceFromString: priceStr is null or empty");
            return null;
        }
        
        try {
            String original = priceStr;
            log.debug("parsePriceFromString: Bắt đầu parse giá từ: '{}'", original);
            
            // Thử parse bằng ChiTietSanPhamRequest trước
            BigDecimal price = ChiTietSanPhamRequest.parseBigDecimalSafe(priceStr);
            if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                log.debug("parsePriceFromString: Parse thành công bằng parseBigDecimalSafe: {} từ '{}'", price, original);
                return price;
            }
            
            // Nếu không được, thử parse trực tiếp với nhiều cách khác nhau
            String cleaned = priceStr.trim();
            
            // Loại bỏ các ký tự không phải số, dấu phẩy, dấu chấm, dấu trừ
            cleaned = cleaned.replaceAll("[^0-9.,\\-]", "");
            
            if (cleaned.isEmpty()) {
                log.warn("parsePriceFromString: Sau khi clean, chuỗi rỗng từ '{}'", original);
                return null;
            }
            
            // Xử lý dấu trừ (số âm - không hợp lệ cho giá)
            if (cleaned.startsWith("-")) {
                cleaned = cleaned.substring(1);
            }
            
            // Xử lý trường hợp có cả dấu phẩy và dấu chấm
            if (cleaned.contains(",") && cleaned.contains(".")) {
                int commaIndex = cleaned.indexOf(',');
                int dotIndex = cleaned.indexOf('.');
                
                // Nếu dấu phẩy đứng sau dấu chấm, coi phẩy là thập phân
                if (commaIndex > dotIndex) {
                    // Ví dụ: 1.000.000,50 -> 1000000.50
                    cleaned = cleaned.replace(".", "").replace(",", ".");
                } else {
                    // Ví dụ: 1,000,000.50 -> 1000000.50
                    cleaned = cleaned.replace(",", "");
                }
            } else if (cleaned.contains(",")) {
                // Chỉ có dấu phẩy
                // Kiểm tra xem có phải là phân cách hàng nghìn không (ví dụ: 1,000,000)
                long commaCount = cleaned.chars().filter(ch -> ch == ',').count();
                if (commaCount > 1 || (commaCount == 1 && cleaned.length() - cleaned.indexOf(',') > 4)) {
                    // Có nhiều dấu phẩy hoặc dấu phẩy có nhiều số sau -> phân cách hàng nghìn
                    cleaned = cleaned.replace(",", "");
                } else {
                    // Có thể là dấu thập phân
                    cleaned = cleaned.replace(",", ".");
                }
            } else if (cleaned.contains(".")) {
                // Chỉ có dấu chấm
                // Kiểm tra xem có phải là phân cách hàng nghìn không
                long dotCount = cleaned.chars().filter(ch -> ch == '.').count();
                if (dotCount > 1) {
                    // Có nhiều dấu chấm -> phân cách hàng nghìn (ví dụ: 1.000.000)
                    cleaned = cleaned.replace(".", "");
                } else {
                    // Một dấu chấm - kiểm tra xem có phải thập phân không
                    int dotIndex = cleaned.indexOf('.');
                    if (cleaned.length() - dotIndex <= 3) {
                        // Có ít hơn 3 số sau dấu chấm -> có thể là thập phân
                        // Giữ nguyên
                    } else {
                        // Nhiều số sau dấu chấm -> phân cách hàng nghìn
                        cleaned = cleaned.replace(".", "");
                    }
                }
            }
            
            // Loại bỏ dấu chấm hoặc phẩy còn sót lại nếu không hợp lệ
            cleaned = cleaned.replaceAll("[^0-9.]", "");
            
            if (cleaned.isEmpty()) {
                log.warn("parsePriceFromString: Sau khi xử lý, chuỗi rỗng từ '{}'", original);
                return null;
            }
            
            BigDecimal result = new BigDecimal(cleaned);
            
            // Kiểm tra giá hợp lệ (>= 0)
            if (result.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("parsePriceFromString: Giá âm không hợp lệ: {} từ '{}'", result, original);
                return null;
            }
            
            log.debug("parsePriceFromString: Parse thành công: {} từ '{}' (cleaned: '{}')", result, original, cleaned);
            return result;
            
        } catch (NumberFormatException e) {
            log.warn("parsePriceFromString: NumberFormatException khi parse '{}': {}", priceStr, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("parsePriceFromString: Lỗi không mong đợi khi parse '{}': {}", priceStr, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Xử lý câu hỏi về thuộc tính sản phẩm và trả về ChatbotReply với suggestedProducts
     */
    private ChatbotReply handleAttributeQuestionWithProducts(String normalizedMessage) {
        List<SanPhamResponse> activeProducts = fetchActiveProducts(500);
        
        if (activeProducts.isEmpty()) {
            return ChatbotReply.builder()
                    .replyText("Hiện tại shop em chưa có sản phẩm này trong kho ạ, Anh Chị có thể lựa chọn những sản phẩm khác nhé ạ!")
                    .suggestedProducts(null)
                    .build();
        }

        // Xử lý màu sắc (có logic riêng)
        if (containsColorKeywords(normalizedMessage)) {
            return handleColorAttributeQueryWithProducts(normalizedMessage, activeProducts);
        }

        // Xử lý kích thước (có logic riêng)
        if (containsSizeKeywords(normalizedMessage)) {
            return handleSizeAttributeQueryWithProducts(normalizedMessage, activeProducts);
        }

        // Xử lý các thuộc tính khác
        List<SanPhamResponse> matchedProducts = new ArrayList<>();
        String attributeLabel = "";
        Function<SanPhamResponse, String> valueExtractor = null;

        if (containsMaterialKeywords(normalizedMessage)) {
            attributeLabel = "chất liệu vỏ";
            valueExtractor = SanPhamResponse::getChatLieuVoTen;
        } else if (containsOriginKeywords(normalizedMessage)) {
            attributeLabel = "xuất xứ";
            valueExtractor = SanPhamResponse::getXuatXuTen;
        } else if (containsWeightKeywords(normalizedMessage)) {
            attributeLabel = "trọng lượng";
            valueExtractor = SanPhamResponse::getTrongLuongTen;
        } else if (containsHelmetTypeKeywords(normalizedMessage)) {
            attributeLabel = "loại mũ bảo hiểm";
            valueExtractor = SanPhamResponse::getLoaiMuBaoHiemTen;
        } else if (containsManufacturerKeywords(normalizedMessage)) {
            attributeLabel = "nhà sản xuất";
            valueExtractor = SanPhamResponse::getNhaSanXuatTen;
        } else if (containsStyleKeywords(normalizedMessage)) {
            attributeLabel = "kiểu dáng";
            valueExtractor = SanPhamResponse::getKieuDangMuTen;
        } else if (containsSafetyKeywords(normalizedMessage)) {
            attributeLabel = "công nghệ an toàn";
            valueExtractor = SanPhamResponse::getCongNgheAnToanTen;
        }

        if (valueExtractor != null) {
            for (SanPhamResponse product : activeProducts) {
                String value = valueExtractor.apply(product);
                if (StringUtils.hasText(value)) {
                    String normalizedValue = normalizeText(value);
                    if (normalizedValue.length() > 1 && matchesAttributeValue(normalizedMessage, normalizedValue)) {
                        matchedProducts.add(product);
                    }
                }
            }
        }

            if (!matchedProducts.isEmpty()) {
            // Giới hạn số lượng sản phẩm hiển thị
            matchedProducts = matchedProducts.stream().limit(10).collect(Collectors.toList());
            
            // Đảm bảo tất cả sản phẩm đều có ảnh (nếu có thể)
            for (SanPhamResponse product : matchedProducts) {
                enrichProductImage(product);
            }
            
            String replyText = String.format("Các sản phẩm %s:", attributeLabel);
            return ChatbotReply.builder()
                    .replyText(replyText)
                    .suggestedProducts(matchedProducts)
                    .build();
        }

        // Nếu không tìm thấy, trả về text thông thường
        String replyText = handleAttributeQuestion(normalizedMessage);
        return ChatbotReply.builder()
                .replyText(replyText)
                .suggestedProducts(null)
                .build();
    }

    /**
     * Xử lý tìm kiếm màu sắc và trả về ChatbotReply với suggestedProducts
     */
    private ChatbotReply handleColorAttributeQueryWithProducts(String normalizedMessage, List<SanPhamResponse> products) {
        List<ChiTietSanPhamResponse> allChiTiet = chiTietSanPhamService.getAll();
        List<ChiTietSanPhamResponse> activeChiTiet = allChiTiet.stream()
                .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai())
                .filter(ct -> StringUtils.hasText(ct.getMauSacTen()))
                .collect(Collectors.toList());

        Map<Long, SanPhamResponse> productMap = products.stream()
                .filter(p -> p.getId() != null && p.getTrangThai() != null && p.getTrangThai())
                .collect(Collectors.toMap(SanPhamResponse::getId, Function.identity(), (a, b) -> a));

        String colorKeyword = extractColorKeyword(normalizedMessage);
        List<ChiTietSanPhamResponse> matchedDetails = new ArrayList<>();

        for (ChiTietSanPhamResponse detail : activeChiTiet) {
            String colorName = detail.getMauSacTen();
            if (StringUtils.hasText(colorName) && matchesColor(normalizedMessage, colorKeyword, colorName)) {
                matchedDetails.add(detail);
            }
        }

        if (!matchedDetails.isEmpty()) {
            Set<Long> sanPhamIds = matchedDetails.stream()
                    .map(ChiTietSanPhamResponse::getSanPhamId)
                    .filter(id -> id != null && productMap.containsKey(id))
                    .collect(Collectors.toSet());

            List<SanPhamResponse> suggestedProducts = new ArrayList<>();
            for (Long sanPhamId : sanPhamIds) {
                SanPhamResponse product = productMap.get(sanPhamId);
                if (product != null) {
                    // Lấy ảnh từ ChiTietSanPham nếu SanPham không có
                    enrichProductImage(product);
                    suggestedProducts.add(product);
                }
            }

            if (!suggestedProducts.isEmpty()) {
                suggestedProducts = suggestedProducts.stream().limit(10).collect(Collectors.toList());
                Set<String> matchedColors = matchedDetails.stream()
                        .map(ChiTietSanPhamResponse::getMauSacTen)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                
                // Đảm bảo tất cả sản phẩm đều có ảnh (nếu có thể)
                for (SanPhamResponse product : suggestedProducts) {
                    enrichProductImage(product);
                }
                
                String replyText = String.format("Các sản phẩm màu %s:",
                        matchedColors.stream().limit(3).collect(Collectors.joining(", ")));
                return ChatbotReply.builder()
                        .replyText(replyText)
                        .suggestedProducts(suggestedProducts)
                        .build();
            }
        }

        String replyText = handleColorAttributeQuery(normalizedMessage, products);
        return ChatbotReply.builder()
                .replyText(replyText)
                .suggestedProducts(null)
                .build();
    }

    /**
     * Xử lý tìm kiếm kích thước và trả về ChatbotReply với suggestedProducts
     */
    private ChatbotReply handleSizeAttributeQueryWithProducts(String normalizedMessage, List<SanPhamResponse> products) {
        Map<Long, SanPhamResponse> productMap = products.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(SanPhamResponse::getId, Function.identity(), (a, b) -> a));

        List<ChiTietSanPhamResponse> details = chiTietSanPhamService.getAll();
        List<ChiTietSanPhamResponse> relevantDetails = details.stream()
                .filter(detail -> detail.getSanPhamId() != null && productMap.containsKey(detail.getSanPhamId()))
                .collect(Collectors.toList());

        List<ChiTietSanPhamResponse> matchedDetails = new ArrayList<>();
        for (ChiTietSanPhamResponse detail : relevantDetails) {
            String sizeName = detail.getKichThuocTen();
            if (StringUtils.hasText(sizeName) && matchesSizeToken(normalizedMessage, sizeName)) {
                matchedDetails.add(detail);
            }
        }

        if (!matchedDetails.isEmpty()) {
            Set<Long> sanPhamIds = matchedDetails.stream()
                    .map(ChiTietSanPhamResponse::getSanPhamId)
                    .filter(id -> id != null && productMap.containsKey(id))
                    .collect(Collectors.toSet());

            List<SanPhamResponse> suggestedProducts = new ArrayList<>();
            for (Long sanPhamId : sanPhamIds) {
                SanPhamResponse product = productMap.get(sanPhamId);
                if (product != null) {
                    // Lấy ảnh từ ChiTietSanPham nếu SanPham không có
                    enrichProductImage(product);
                    suggestedProducts.add(product);
                }
            }

            if (!suggestedProducts.isEmpty()) {
                suggestedProducts = suggestedProducts.stream().limit(10).collect(Collectors.toList());
                Set<String> matchedSizes = matchedDetails.stream()
                        .map(ChiTietSanPhamResponse::getKichThuocTen)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                
                // Đảm bảo tất cả sản phẩm đều có ảnh (nếu có thể)
                for (SanPhamResponse product : suggestedProducts) {
                    enrichProductImage(product);
                }
                
                String replyText = String.format("Các mẫu phù hợp kích thước %s:",
                        matchedSizes.stream().collect(Collectors.joining(", ")));
                return ChatbotReply.builder()
                        .replyText(replyText)
                        .suggestedProducts(suggestedProducts)
                        .build();
            }
        }

        String replyText = handleSizeAttributeQuery(normalizedMessage, products);
        return ChatbotReply.builder()
                .replyText(replyText)
                .suggestedProducts(null)
                .build();
    }

    /**
     * Xử lý tìm kiếm sản phẩm và trả về ChatbotReply với suggestedProducts
     */
    private ChatbotReply handleProductSearchWithProducts(String normalizedMessage) {
        try {
            // Trích xuất keyword từ câu hỏi
            String keyword = extractProductKeyword(normalizedMessage);
            
            // Trích xuất giá từ câu hỏi nếu có
            BigDecimal priceFilter = extractPriceFromMessage(normalizedMessage);
            PriceRange priceRange = extractPriceRange(normalizedMessage);
            if (priceRange == null && containsApproximateKeyword(normalizedMessage) && priceFilter != null) {
                priceRange = createApproximateRange(priceFilter);
            }
            
            Pageable pageable = PageRequest.of(0, 20);
            Page<SanPhamResponse> products = sanPhamService.search(keyword, true, pageable);
            List<SanPhamResponse> results = products.getContent();

            // Lọc theo giá nếu có
            if (priceRange != null && priceRange.isValid()) {
                final PriceRange rangeFilter = priceRange;
                results = results.stream()
                        .filter(p -> p.getGiaBan() != null && rangeFilter.contains(p.getGiaBan()))
                        .collect(Collectors.toList());
            } else if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
                results = results.stream()
                        .filter(p -> p.getGiaBan() != null && p.getGiaBan().compareTo(priceFilter) <= 0)
                        .collect(Collectors.toList());
            }

            if (results.isEmpty()) {
                String noResultMessage = "Ôiii không thật sự xin lỗi, em không tìm thấy sản phẩm mà Anh Chị yêu cầu ạ, Anh Chị có thể tham khảo một số mẫu sản phẩm hot bên em giá hợp lí mà bao đẹp bao thoải mái ạ";
                if (keyword != null && !keyword.trim().isEmpty() && !keyword.equals("")) {
                    noResultMessage += String.format(" với từ khóa '%s'", keyword);
                }
                if (priceRange != null && priceRange.isValid()) {
                    noResultMessage += String.format(" trong khoảng %s - %s VNĐ",
                            formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax()));
                } else if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
                    noResultMessage += String.format(" dưới %s VNĐ", formatPrice(priceFilter));
                }
                noResultMessage += ". Anh Chị có thể thử từ khóa khác hoặc điều chỉnh bộ lọc không để tìm kiếm sản phẩm ưng ý đó ạ?";
                noResultMessage += " Anh chị có thể tham khảo thêm sản phẩm đang HOT HÒN HỌT bên em nhé giá rẻ và vô cùng hợp lí đó ạ";
                
                // Lấy sản phẩm bán chạy nhất để gợi ý
                List<SanPhamResponse> bestSellingProducts = getBestSellingProductsForSuggestion(5);
                
                return ChatbotReply.builder()
                        .replyText(noResultMessage)
                        .suggestedProducts(bestSellingProducts)
                        .build();
            }

            // Giới hạn số lượng kết quả hiển thị
            if (results.size() > 10) {
                results = results.stream().limit(10).collect(Collectors.toList());
            }

            // Đảm bảo tất cả sản phẩm đều có ảnh (nếu có thể)
            for (SanPhamResponse product : results) {
                enrichProductImage(product);
            }

            StringBuilder reply = new StringBuilder();
            if (results.size() == 1) {
                SanPhamResponse product = results.get(0);
                reply.append(String.format("Em tìm thấy sản phẩm: %s", product.getTenSanPham()));
            } else {
                reply.append(String.format("Em tìm thấy %d sản phẩm", results.size()));
                if (priceRange != null && priceRange.isValid()) {
                    reply.append(String.format(" trong khoảng %s - %s VNĐ",
                            formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax())));
                } else if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
                    reply.append(String.format(" dưới %s VNĐ", formatPrice(priceFilter)));
                }
            }

            return ChatbotReply.builder()
                    .replyText(addAIfNeeded(reply.toString()))
                    .suggestedProducts(results)
                    .build();

        } catch (Exception e) {
            log.error("Error handling product search with products", e);
            return ChatbotReply.builder()
                    .replyText("Ôiii không thật sự xin lỗi, em gặp lỗi khi tìm kiếm sản phẩm ạ. Anh Chị vui lòng thử lại sau ạ.")
                    .suggestedProducts(null)
                    .build();
        }
    }

    /**
     * Xử lý câu hỏi về sản phẩm rẻ nhất/đắt nhất và trả về ChatbotReply với suggestedProducts
     */
    private ChatbotReply handleCheapestOrMostExpensiveWithProducts(String normalizedMessage) {
        try {
            Pageable pageable = PageRequest.of(0, 100);
            Page<SanPhamResponse> products = sanPhamService.search(null, true, pageable);
            List<SanPhamResponse> activeProducts = products.getContent();

            if (activeProducts.isEmpty()) {
                return ChatbotReply.builder()
                        .replyText("Hiện tại shop em chưa có sản phẩm này trong kho ạ, Anh Chị có thể lựa chọn những sản phẩm khác nhé ạ!")
                        .suggestedProducts(null)
                        .build();
            }

            boolean isCheapest = normalizedMessage.contains("rẻ") || normalizedMessage.contains("re") || 
                                 normalizedMessage.contains("thấp") || normalizedMessage.contains("thap") ||
                                 normalizedMessage.contains("cheap") || normalizedMessage.contains("lowest");

            List<SanPhamResponse> sortedProducts = activeProducts.stream()
                    .filter(p -> p.getGiaBan() != null)
                    .sorted((p1, p2) -> isCheapest ? 
                            p1.getGiaBan().compareTo(p2.getGiaBan()) : 
                            p2.getGiaBan().compareTo(p1.getGiaBan()))
                    .limit(10)
                    .collect(Collectors.toList());

            // Đảm bảo tất cả sản phẩm đều có ảnh (nếu có thể)
            for (SanPhamResponse product : sortedProducts) {
                enrichProductImage(product);
            }

            StringBuilder reply = new StringBuilder();
            reply.append(String.format("Các sản phẩm %s nhất:\n\n", isCheapest ? "rẻ" : "đắt"));
            for (SanPhamResponse product : sortedProducts) {
                reply.append(String.format("• %s - %s VNĐ\n", product.getTenSanPham(), formatPrice(product.getGiaBan())));
            }

            return ChatbotReply.builder()
                    .replyText(addAIfNeeded(reply.toString()))
                    .suggestedProducts(sortedProducts)
                    .build();

        } catch (Exception e) {
            log.error("Error handling cheapest/most expensive with products", e);
            return ChatbotReply.builder()
                    .replyText("Ôiii không thật sự xin lỗi, em gặp lỗi khi tìm kiếm sản phẩm ạ. Anh Chị vui lòng thử lại sau ạ.")
                    .suggestedProducts(null)
                    .build();
        }
    }

    /**
     * Xử lý câu hỏi chung về sản phẩm và trả về ChatbotReply với suggestedProducts
     */
    private ChatbotReply handleGeneralProductQuestionWithProducts(String normalizedMessage) {
        try {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SanPhamResponse> products = sanPhamService.search(null, true, pageable);
            List<SanPhamResponse> featuredProducts = products.getContent();

            if (featuredProducts.isEmpty()) {
                return ChatbotReply.builder()
                        .replyText("Chúng em chuyên bán các loại mũ bảo hiểm chất lượng cao ạ. Anh Chị muốn biết thông tin gì cụ thể ạ?")
                        .suggestedProducts(null)
                        .build();
            }

            // Đảm bảo tất cả sản phẩm đều có ảnh (nếu có thể)
            for (SanPhamResponse product : featuredProducts) {
                enrichProductImage(product);
            }

            StringBuilder reply = new StringBuilder();
            reply.append("Chúng em có các sản phẩm mũ bảo hiểm đa dạng:");
            reply.append("\n\nAnh Chị muốn xem thêm sản phẩm nào cụ thể không ạ?");

            return ChatbotReply.builder()
                    .replyText(addAIfNeeded(reply.toString()))
                    .suggestedProducts(featuredProducts)
                    .build();

        } catch (Exception e) {
            log.error("Error handling general product question with products", e);
            return ChatbotReply.builder()
                    .replyText("Chúng em chuyên bán các loại mũ bảo hiểm chất lượng cao ạ. Anh Chị muốn biết thông tin gì cụ thể ạ?")
                    .suggestedProducts(null)
                    .build();
        }
    }

    /**
     * Lấy sản phẩm bán chạy nhất để gợi ý khi không tìm thấy sản phẩm
     */
    private List<SanPhamResponse> getBestSellingProductsForSuggestion(int limit) {
        try {
            List<BestSellingProductDTO> bestSelling = statisticsService.getBestSellingProducts(limit);
            List<SanPhamResponse> suggestedProducts = new ArrayList<>();
            
            for (BestSellingProductDTO bestProduct : bestSelling) {
                if (bestProduct.getSanPhamId() != null) {
                    try {
                        SanPhamResponse product = sanPhamService.getById(bestProduct.getSanPhamId());
                        if (product != null && product.getTrangThai() != null && product.getTrangThai()) {
                            // Set giá từ best selling nếu có
                            if (bestProduct.getDonGia() != null) {
                                product.setGiaBan(bestProduct.getDonGia());
                            }
                            // Lấy ảnh từ ChiTietSanPham nếu SanPham không có
                            enrichProductImage(product);
                            suggestedProducts.add(product);
                        }
                    } catch (Exception e) {
                        log.warn("Không thể lấy sản phẩm bán chạy với ID: {}", bestProduct.getSanPhamId(), e);
                    }
                }
            }
            
            log.debug("Lấy được {} sản phẩm bán chạy để gợi ý", suggestedProducts.size());
            return suggestedProducts;
        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm bán chạy để gợi ý", e);
            // Fallback: lấy một số sản phẩm active ngẫu nhiên
            try {
                Pageable pageable = PageRequest.of(0, limit);
                Page<SanPhamResponse> products = sanPhamService.search(null, true, pageable);
                List<SanPhamResponse> fallbackProducts = products.getContent();
                for (SanPhamResponse product : fallbackProducts) {
                    enrichProductImage(product);
                }
                return fallbackProducts;
            } catch (Exception ex) {
                log.error("Lỗi khi lấy sản phẩm fallback", ex);
                return new ArrayList<>();
            }
        }
    }

    /**
     * Lấy ảnh sản phẩm từ SanPham, nếu không có thì lấy từ ChiTietSanPham đầu tiên
     */
    private void enrichProductImage(SanPhamResponse product) {
        if (product == null || product.getId() == null) {
            log.warn("enrichProductImage: product hoặc product.id là null");
            return;
        }

        log.debug("enrichProductImage: Kiểm tra ảnh cho sản phẩm ID: {}, tên: {}", product.getId(), product.getTenSanPham());

        // Nếu đã có ảnh, không cần làm gì
        if (StringUtils.hasText(product.getAnhSanPham())) {
            log.debug("enrichProductImage: Sản phẩm ID {} đã có ảnh từ SanPham (độ dài: {})", 
                    product.getId(), product.getAnhSanPham().length());
            return;
        }

        log.debug("enrichProductImage: Sản phẩm ID {} không có ảnh từ SanPham, tìm trong ChiTietSanPham", product.getId());

        // Tìm ảnh từ ChiTietSanPham đầu tiên có ảnh
        try {
            List<ChiTietSanPhamResponse> chiTietList = chiTietSanPhamService.getBySanPhamId(product.getId());
            log.debug("enrichProductImage: Tìm thấy {} chi tiết sản phẩm cho sản phẩm ID: {}", 
                    chiTietList.size(), product.getId());
            
            for (ChiTietSanPhamResponse chiTiet : chiTietList) {
                if (StringUtils.hasText(chiTiet.getAnhSanPham())) {
                    product.setAnhSanPham(chiTiet.getAnhSanPham());
                    log.info("✅ enrichProductImage: Đã lấy ảnh từ ChiTietSanPham ID {} cho sản phẩm ID: {} (độ dài: {})", 
                            chiTiet.getId(), product.getId(), chiTiet.getAnhSanPham().length());
                    return;
                } else {
                    log.debug("enrichProductImage: ChiTietSanPham ID {} không có ảnh", chiTiet.getId());
                }
            }
            
            log.warn("⚠️ enrichProductImage: Không tìm thấy ảnh trong {} chi tiết sản phẩm cho sản phẩm ID: {}", 
                    chiTietList.size(), product.getId());
        } catch (Exception e) {
            log.error("❌ enrichProductImage: Lỗi khi lấy ảnh từ ChiTietSanPham cho sản phẩm ID: {}", product.getId(), e);
        }
    }
}



