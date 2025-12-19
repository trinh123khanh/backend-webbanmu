package com.example.backend.service;

import com.example.backend.dto.BestSellingProductDTO;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    
    // ThreadLocal để lưu suggestedProducts trong quá trình xử lý
    private static final ThreadLocal<List<SanPhamResponse>> ThreadLocalSuggestedProducts = new ThreadLocal<>();

    /**
     * Response object chứa cả reply và suggestedProducts
     */
    public static class ChatbotReply {
        private final String reply;
        private final List<SanPhamResponse> suggestedProducts;
        
        public ChatbotReply(String reply, List<SanPhamResponse> suggestedProducts) {
            this.reply = reply;
            this.suggestedProducts = suggestedProducts != null ? suggestedProducts : new ArrayList<>();
        }
        
        public String getReply() {
            return reply;
        }
        
        public List<SanPhamResponse> getSuggestedProducts() {
            return suggestedProducts;
        }
    }
    
    /**
     * Phân tích câu hỏi và tạo phản hồi thông minh dựa trên dữ liệu sản phẩm
     * Trả về cả reply và suggestedProducts
     */
    public ChatbotReply generateReplyWithProducts(String customerMessage) {
        String reply = generateReply(customerMessage);
        List<SanPhamResponse> suggestedProducts = extractSuggestedProducts(customerMessage, reply);
        return new ChatbotReply(reply, suggestedProducts);
    }
    
    /**
     * Phân tích câu hỏi và tạo phản hồi thông minh dựa trên dữ liệu sản phẩm
     */
    public String generateReply(String customerMessage) {
        if (customerMessage == null || customerMessage.trim().isEmpty()) {
            return "TDK xin chào quý khách ạ ! em có thể giúp gì cho mình ạ?";
        }

        String normalizedMessage = normalizeText(customerMessage);
        log.debug("Processing customer message: {}", normalizedMessage);

        // Kiểm tra greeting
        if (isGreeting(normalizedMessage)) {
            return "Xin chào quý khách ! em rất vui được hỗ trợ mình ạ. em có thể giúp bạn tìm kiếm sản phẩm, xem giá cả, hoặc tư vấn về các loại mũ bảo hiểm. Bạn cần gì?";
        }

        // ✅ ƯU TIÊN: Kiểm tra câu hỏi về giá TRƯỚC (vì có thể chứa từ khóa về attribute nhưng thực chất là hỏi về giá)
        if (isPriceQuestion(normalizedMessage)) {
            return handlePriceQuestion(normalizedMessage);
        }

        // ✅ ƯU TIÊN: Kiểm tra câu hỏi về sản phẩm cụ thể TRƯỚC (có thể chứa từ khóa attribute nhưng thực chất là tìm sản phẩm)
        if (isProductSearchQuestion(normalizedMessage)) {
            return handleProductSearch(normalizedMessage);
        }

        // ✅ Kiểm tra câu hỏi về sản phẩm rẻ nhất/đắt nhất TRƯỚC attribute
        if (isCheapestOrMostExpensiveQuestion(normalizedMessage)) {
            return handleCheapestOrMostExpensive(normalizedMessage);
        }

        // Kiểm tra câu hỏi về thuộc tính sản phẩm (sau khi đã loại trừ các trường hợp trên)
        if (isAttributeQuestion(normalizedMessage)) {
            return handleAttributeQuestion(normalizedMessage);
        }

        // Kiểm tra câu hỏi về tồn kho
        if (isStockQuestion(normalizedMessage)) {
            return handleStockQuestion(normalizedMessage);
        }

        // Câu hỏi chung về sản phẩm
        if (isProductRelated(normalizedMessage)) {
            return handleGeneralProductQuestion();
        }

        // Thử tìm kiếm với toàn bộ message như keyword (có thể là tên sản phẩm)
        String fullMessageKeyword = extractProductKeyword(customerMessage);
        if (fullMessageKeyword != null && !fullMessageKeyword.trim().isEmpty()) {
            try {
                Pageable pageable = PageRequest.of(0, 10);
                Page<SanPhamResponse> products = sanPhamService.search(fullMessageKeyword, true, pageable);
                List<SanPhamResponse> results = products.getContent();
                if (!results.isEmpty()) {
                    return handleProductSearch(customerMessage);
                }
            } catch (Exception e) {
                log.debug("Error trying to search with full message as keyword", e);
            }
        }
        
        // Phản hồi mặc định - Thông minh hơn
        return "Cảm ơn anh chị đã liên hệ! Em có thể giúp mình:\n" +
               "• Tìm kiếm sản phẩm theo tên, màu sắc, kích thước...\n" +
               "• Xem giá cả và so sánh giá\n" +
               "• Kiểm tra tồn kho\n" +
               "• Tư vấn về các loại mũ bảo hiểm\n\n" +
               "Bạn muốn biết gì cụ thể ạ?";
    }

    /**
     * Xử lý câu hỏi về giá
     */
    private String handlePriceQuestion(String message) {
        try {
            // Lấy tất cả sản phẩm active
            Pageable pageable = PageRequest.of(0, 100);
            Page<SanPhamResponse> products = sanPhamService.search(null, true, pageable);
            List<SanPhamResponse> activeProducts = products.getContent();

            if (activeProducts.isEmpty()) {
                return "Ôiiii không hiện tại bên TDK chưa có sản phẩm nào trong kho. Vui lòng quay lại sau!";
            }

            // Tìm giá min/max
            BigDecimal minPrice = activeProducts.stream()
                    .filter(p -> p.getGiaBan() != null)
                    .map(SanPhamResponse::getGiaBan)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal maxPrice = activeProducts.stream()
                    .filter(p -> p.getGiaBan() != null)
                    .map(SanPhamResponse::getGiaBan)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

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
                List<SanPhamResponse> inRangeProducts = activeProducts.stream()
                        .filter(p -> p.getGiaBan() != null && rangeFilter.contains(p.getGiaBan()))
                        .sorted((p1, p2) -> p1.getGiaBan().compareTo(p2.getGiaBan()))
                        .limit(6)
                        .collect(Collectors.toList());

                if (!inRangeProducts.isEmpty()) {
                    StringBuilder reply = new StringBuilder();
                    reply.append(String.format("Các sản phẩm trong khoảng %s - %s VNĐ:\n\n",
                            formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax())));
                    for (SanPhamResponse product : inRangeProducts) {
                        reply.append(String.format("• %s - %s VNĐ", product.getTenSanPham(), formatPrice(product.getGiaBan())));
                        if (product.getSoLuongTon() != null && product.getSoLuongTon() > 0) {
                            reply.append(String.format(" (Còn %d sp)", product.getSoLuongTon()));
                        }
                        reply.append("\n");
                    }
                    if (inRangeProducts.size() >= 6) {
                        reply.append("\nAnh chị có muốn xem thêm sản phẩm trong khoảng giá này không?");
                    }
                    // ✅ Lưu suggestedProducts vào thread-local
                    ThreadLocalSuggestedProducts.set(inRangeProducts);
                    return reply.toString();
                } else {
                    return String.format("Hiện chưa có sản phẩm nào trong khoảng %s - %s VNĐ. Giá hiện dao động từ %s đến %s VNĐ.",
                            formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax()),
                            formatPrice(minPrice), formatPrice(maxPrice));
                }
            }

            // Kiểm tra xem có hỏi về giá cụ thể không
            BigDecimal askedPrice = extractPriceFromMessage(message);
            if (askedPrice != null && askedPrice.compareTo(BigDecimal.ZERO) > 0) {
                List<SanPhamResponse> matchingProducts = activeProducts.stream()
                        .filter(p -> p.getGiaBan() != null && p.getGiaBan().compareTo(askedPrice) <= 0)
                        .limit(5)
                        .collect(Collectors.toList());

                if (!matchingProducts.isEmpty()) {
                    StringBuilder reply = new StringBuilder();
                    reply.append(String.format("Chúng em có các sản phẩm dưới %s VNĐ:\n\n", formatPrice(askedPrice)));
                    for (SanPhamResponse product : matchingProducts) {
                        reply.append(String.format("• %s - %s VNĐ\n", product.getTenSanPham(), formatPrice(product.getGiaBan())));
                    }
                    if (matchingProducts.size() < activeProducts.size()) {
                        reply.append("\nQuý khách có muốn xem thêm sản phẩm khác không?");
                    }
                    // ✅ Lưu suggestedProducts vào thread-local để extractSuggestedProducts có thể lấy
                    ThreadLocalSuggestedProducts.set(matchingProducts);
                    return reply.toString();
                } else {
                    return String.format("Hiện tại chúng em không có sản phẩm nào dưới %s VNĐ. Giá sản phẩm của chúng tôi dao động từ %s đến %s VNĐ.", 
                            formatPrice(askedPrice), formatPrice(minPrice), formatPrice(maxPrice));
                }
            }

            // Trả về thông tin giá chung
            return String.format("Giá sản phẩm của chúng em dao động từ %s đến %s VNĐ. Anh chị muốn xem sản phẩm nào cụ thể không ạ?", 
                    formatPrice(minPrice), formatPrice(maxPrice));

        } catch (Exception e) {
            log.error("Error handling price question", e);
            return "Xin lỗi, em gặp lỗi khi tìm kiếm thông tin giá. Vui lòng thử lại sau hoặc liên hệ nhân viên để được hỗ trợ.";
        }
    }

    /**
     * Xử lý tìm kiếm sản phẩm với bộ lọc theo giá và tên - Cải thiện logic tìm kiếm
     */
    private String handleProductSearch(String message) {
        try {
            // Trích xuất keyword từ câu hỏi
            String keyword = extractProductKeyword(message);
            
            // Nếu không tìm thấy keyword, thử tìm từ message gốc (không normalize quá nhiều)
            if (keyword == null || keyword.trim().isEmpty()) {
                // Thử tìm các từ có ý nghĩa từ message gốc
                String[] words = message.toLowerCase(Locale.ROOT).split("\\s+");
                StringBuilder fallbackKeyword = new StringBuilder();
                String[] commonStopWords = {"tìm", "tim", "có", "co", "cho", "tôi", "toi", "mình", "minh", 
                                            "bạn", "ban", "với", "voi", "giá", "gia", "bao", "nhiêu", "nhieu"};
                for (String word : words) {
                    word = word.trim();
                    boolean isStopWord = false;
                    for (String stop : commonStopWords) {
                        if (word.equals(stop)) {
                            isStopWord = true;
                            break;
                        }
                    }
                    if (!isStopWord && word.length() >= 2 && !word.matches("\\d+")) {
                        if (fallbackKeyword.length() > 0) {
                            fallbackKeyword.append(" ");
                        }
                        fallbackKeyword.append(word);
                    }
                }
                String fallback = fallbackKeyword.toString().trim();
                if (!fallback.isEmpty()) {
                    keyword = fallback;
                }
            }
            
            // Trích xuất giá từ câu hỏi nếu có
            BigDecimal priceFilter = extractPriceFromMessage(message);
            PriceRange priceRange = extractPriceRange(message);
            if (priceRange == null && containsApproximateKeyword(message) && priceFilter != null) {
                priceRange = createApproximateRange(priceFilter);
            }
            
            Pageable pageable = PageRequest.of(0, 50); // Tăng số lượng để tìm kiếm tốt hơn
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
                // ✅ Khi không tìm thấy sản phẩm, gợi ý sản phẩm bán chạy nhất
                String noResultMessage = "Xin lỗi, tôi không tìm thấy sản phẩm nào";
                if (keyword != null && !keyword.trim().isEmpty() && !keyword.equals("")) {
                    noResultMessage += String.format(" với từ khóa '%s'", keyword);
                }
                if (priceRange != null && priceRange.isValid()) {
                    noResultMessage += String.format(" trong khoảng %s - %s VNĐ",
                            formatPrice(priceRange.getMin()), formatPrice(priceRange.getMax()));
                } else if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
                    noResultMessage += String.format(" dưới %s VNĐ", formatPrice(priceFilter));
                }
                noResultMessage += ". Để em gợi ý một số sản phẩm bán chạy nhất cho mình nhé!";
                
                // Lưu suggestedProducts vào một biến để ChatConversationServiceImpl có thể truy cập
                // Tạm thời trả về message, ChatConversationServiceImpl sẽ xử lý suggestedProducts
                return noResultMessage;
            }

            // Giới hạn số lượng kết quả hiển thị
            if (results.size() > 10) {
                results = results.stream().limit(10).collect(Collectors.toList());
            }

            StringBuilder reply = new StringBuilder();
            if (results.size() == 1) {
                SanPhamResponse product = results.get(0);
                reply.append(String.format("Tôi tìm thấy sản phẩm: %s\n", product.getTenSanPham()));
                reply.append(String.format("Giá: %s VNĐ\n", formatPrice(product.getGiaBan())));
                if (product.getSoLuongTon() != null && product.getSoLuongTon() > 0) {
                    reply.append(String.format("Còn hàng: %d sản phẩm\n", product.getSoLuongTon()));
                } else {
                    reply.append("Hiện tại hết hàng\n");
                }
                if (product.getMoTa() != null && !product.getMoTa().isEmpty()) {
                    reply.append(String.format("Mô tả: %s\n", product.getMoTa()));
                }
                // ✅ Lưu suggestedProducts
                ThreadLocalSuggestedProducts.set(results);
            } else {
                reply.append(String.format("Tôi tìm thấy %d sản phẩm", results.size()));
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
                // ✅ Lưu suggestedProducts
                ThreadLocalSuggestedProducts.set(results);
            }

            return reply.toString();

        } catch (Exception e) {
            log.error("Error handling product search", e);
            return "Xin lỗi, tôi gặp lỗi khi tìm kiếm sản phẩm. Vui lòng thử lại sau.";
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
                return "Hiện tại chúng tôi chưa có sản phẩm nào trong kho.";
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
                return "Xin lỗi, tôi không tìm thấy sản phẩm phù hợp.";
            }

            StringBuilder reply = new StringBuilder();
            String type = isCheapest ? "rẻ nhất" : "đắt nhất";
            reply.append(String.format("Các sản phẩm %s:\n\n", type));
            for (SanPhamResponse product : sortedProducts) {
                reply.append(String.format("• %s - %s VNĐ\n", product.getTenSanPham(), formatPrice(product.getGiaBan())));
            }
            
            // ✅ Lưu suggestedProducts
            ThreadLocalSuggestedProducts.set(sortedProducts);

            return reply.toString();

        } catch (Exception e) {
            log.error("Error handling cheapest/most expensive question", e);
            return "Xin lỗi, tôi gặp lỗi khi tìm kiếm. Vui lòng thử lại sau.";
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
                return "Xin lỗi, tôi không tìm thấy sản phẩm nào.";
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

            return reply.toString();

        } catch (Exception e) {
            log.error("Error handling stock question", e);
            return "Xin lỗi, tôi gặp lỗi khi kiểm tra tồn kho. Vui lòng thử lại sau.";
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
                return "Chúng tôi chuyên bán các loại mũ bảo hiểm chất lượng cao. Bạn muốn biết thông tin gì cụ thể?";
            }

            StringBuilder reply = new StringBuilder();
            reply.append("Chúng tôi có các sản phẩm mũ bảo hiểm đa dạng:\n\n");
            for (SanPhamResponse product : featuredProducts) {
                reply.append(String.format("• %s - %s VNĐ\n", product.getTenSanPham(), formatPrice(product.getGiaBan())));
            }
            reply.append("\nBạn muốn xem thêm sản phẩm nào cụ thể không?");

            return reply.toString();

        } catch (Exception e) {
            log.error("Error handling general product question", e);
            return "Chúng tôi chuyên bán các loại mũ bảo hiểm chất lượng cao. Bạn muốn biết thông tin gì cụ thể?";
        }
    }

    private String handleAttributeQuestion(String normalizedMessage) {
        List<SanPhamResponse> activeProducts = fetchActiveProducts(200);
        if (activeProducts.isEmpty()) {
            return "Hiện tại chúng tôi chưa có sản phẩm nào trong kho. Bạn vui lòng quay lại sau nhé!";
        }

        if (containsColorKeywords(normalizedMessage)) {
            return handleSimpleAttributeQuery("màu sắc", normalizedMessage, activeProducts, SanPhamResponse::getMauSacTen);
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

        return "Mình có thể hỗ trợ thông tin về màu sắc, kích thước, chất liệu vỏ, xuất xứ, trọng lượng, loại mũ bảo hiểm, nhà sản xuất, kiểu dáng và công nghệ an toàn. Bạn muốn biết rõ hơn về phần nào?";
    }

    private List<SanPhamResponse> fetchActiveProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return sanPhamService.search(null, true, pageable).getContent();
    }

    private String handleSimpleAttributeQuery(String attributeLabel,
                                              String normalizedMessage,
                                              List<SanPhamResponse> products,
                                              Function<SanPhamResponse, String> valueExtractor) {
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
            if (normalizedMessage.contains(normalizedValue)) {
                matchedProducts.add(product);
                matchedValues.add(value);
            }
        }

        if (!matchedProducts.isEmpty()) {
            return buildAttributeProductResponse(attributeLabel, matchedValues, matchedProducts, valueExtractor);
        }

        Set<String> availableValues = products.stream()
                .map(valueExtractor)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!availableValues.isEmpty()) {
            String summary = availableValues.stream().limit(10).collect(Collectors.joining(", "));
            if (availableValues.size() > 10) {
                summary += String.format(" ... (+%d tuỳ chọn khác)", availableValues.size() - 10);
            }
            return String.format("Hiện tại chúng tôi có các %s: %s. Bạn muốn mình gửi chi tiết mẫu nào?", attributeLabel, summary);
        }

        return String.format("Xin lỗi, hiện dữ liệu %s đang được cập nhật. Bạn có thể hỏi nhân viên để được hỗ trợ nhanh hơn nhé!", attributeLabel);
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
            reply.append(String.format("\n... và %d sản phẩm khác. Bạn muốn xem thêm không?", products.size() - 5));
        }

        return reply.toString();
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
                reply.append(String.format("\n... và %d lựa chọn khác. Bạn muốn xem thêm không?", matchedDetails.size() - 5));
            }
            return reply.toString();
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
            return String.format("Chúng tôi hiện có các kích thước: %s. Bạn muốn mình gợi ý mẫu theo size nào?", summary);
        }

        return "Mình đang cập nhật dữ liệu kích thước chi tiết. Bạn có thể để lại thông tin để nhân viên hỗ trợ nhanh hơn nhé!";
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
        // ✅ Không coi là câu hỏi về attribute nếu có từ khóa về giá hoặc số tiền
        if (hasPriceOrMoneyKeywords(message)) {
            return false;
        }
        
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
    
    /**
     * Kiểm tra xem message có chứa từ khóa về giá hoặc tiền không
     * Nếu có, không nên coi là câu hỏi về attribute
     */
    private boolean hasPriceOrMoneyKeywords(String message) {
        if (message == null) {
            return false;
        }
        String[] priceMoneyKeywords = {
            "gia", "giá", "price", "cost", "vnđ", "vnd", "đồng", "dong",
            "k", "tr", "triệu", "trieu", "nghìn", "nghin", "nghin",
            "dưới", "duoi", "under", "trên", "tren", "above",
            "khoảng", "khoang", "tầm", "tam", "about", "range",
            "bao nhiêu", "bao nhieu", "how much", "how many"
        };
        for (String keyword : priceMoneyKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        // Kiểm tra xem có số kèm theo đơn vị tiền không (ví dụ: 100k, 500000, 1tr)
        if (message.matches(".*\\d+\\s*(k|tr|triệu|trieu|nghìn|nghin|vnđ|vnd|đồng|dong).*")) {
            return true;
        }
        return false;
    }

    private boolean containsColorKeywords(String message) {
        return containsAnyKeyword(message, 
            "mau sac", "mau", "color", "màu sắc", "màu",
            "màu gì", "mau gi", "có màu", "co mau", "màu nào", "mau nao"
        );
    }

    private boolean containsSizeKeywords(String message) {
        return containsAnyKeyword(message, 
            "kich thuoc", "size", "form size", "kích thước",
            "size nào", "size nao", "cỡ nào", "co nao", "cỡ", "co",
            "lớn", "lon", "nhỏ", "nho", "vừa", "vua"
        );
    }

    private boolean containsMaterialKeywords(String message) {
        // ✅ Cải thiện: Chỉ match khi có từ khóa đầy đủ, không match với "vo" đơn lẻ (có thể là "với", "dưới")
        // Kiểm tra các từ khóa đầy đủ trước
        String[] fullKeywords = {
            "chat lieu", "chat lieu vo", "vo mu", "shell", "material",
            "chất liệu", "chất liệu vỏ", "vỏ mũ",
            "nhựa", "nhua", "plastic", "composite", "carbon"
        };
        
        for (String keyword : fullKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        
        // Chỉ match "vỏ" hoặc "vo" nếu có từ "mũ" hoặc "chất liệu" gần đó
        if (message.contains(" vo ") || message.contains(" vỏ ")) {
            // Kiểm tra xem có từ "mũ" hoặc "chất liệu" trong vòng 10 ký tự
            int voIndex = Math.max(message.indexOf(" vo "), message.indexOf(" vỏ "));
            if (voIndex >= 0) {
                String context = message.substring(Math.max(0, voIndex - 10), 
                                                    Math.min(message.length(), voIndex + 15));
                if (context.contains("mu") || context.contains("mũ") || 
                    context.contains("chat lieu") || context.contains("chất liệu")) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private boolean containsOriginKeywords(String message) {
        return containsAnyKeyword(message, 
            "xuat xu", "nguon goc", "origin", "made in",
            "xuất xứ", "nguồn gốc", "sản xuất ở đâu", "san xuat o dau",
            "sản xuất tại", "san xuat tai", "sản xuất", "san xuat"
        );
    }

    private boolean containsWeightKeywords(String message) {
        return containsAnyKeyword(message, 
            "trong luong", "can nang", "weight",
            "trọng lượng", "cân nặng", "nặng", "nang", "nhẹ", "nhe",
            "bao nhiêu kg", "bao nhieu kg", "kg", "gram", "g"
        );
    }

    private boolean containsHelmetTypeKeywords(String message) {
        return containsAnyKeyword(message, 
            "loai mu", "dang mu", "helmet type", "fullface", "3/4", "34", "nua dau",
            "loại mũ", "dạng mũ", "kiểu mũ", "kieu mu",
            "full face", "nửa đầu", "nua dau", "mở mặt", "mo mat"
        );
    }

    private boolean containsManufacturerKeywords(String message) {
        return containsAnyKeyword(message, 
            "nha san xuat", "thuong hieu", "brand", "hang",
            "nhà sản xuất", "thương hiệu", "hãng", "hang",
            "của hãng nào", "cua hang nao", "thương hiệu nào", "thuong hieu nao"
        );
    }

    private boolean containsStyleKeywords(String message) {
        return containsAnyKeyword(message, 
            "kieu dang", "phong cach", "style", "form",
            "kiểu dáng", "phong cách", "dáng", "dang",
            "kiểu nào", "kieu nao", "dáng nào", "dang nao"
        );
    }

    private boolean containsSafetyKeywords(String message) {
        return containsAnyKeyword(message, 
            "cong nghe an toan", "an toan", "safe tech", "mips", "ece", "dot",
            "công nghệ an toàn", "an toàn", "tiêu chuẩn", "tieu chuan",
            "chứng nhận", "chung nhan", "certification", "standard"
        );
    }

    private boolean containsAnyKeyword(String message, String... keywords) {
        if (message == null) {
            return false;
        }
        String normalized = normalizeText(message);
        for (String keyword : keywords) {
            String normalizedKeyword = normalizeText(keyword);
            // Kiểm tra exact match
            if (normalized.contains(normalizedKeyword)) {
                return true;
            }
            // Kiểm tra fuzzy match (cho phép lỗi chính tả nhỏ)
            if (fuzzyMatch(normalized, normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Fuzzy matching đơn giản - kiểm tra xem keyword có xuất hiện trong message không
     * với độ lệch nhỏ (cho phép thiếu 1-2 ký tự)
     */
    private boolean fuzzyMatch(String text, String keyword) {
        if (text == null || keyword == null || keyword.length() < 3) {
            return false;
        }
        
        // Nếu keyword quá ngắn, chỉ dùng exact match
        if (keyword.length() <= 2) {
            return text.contains(keyword);
        }
        
        // Kiểm tra nếu text chứa keyword hoặc keyword chứa trong text
        if (text.contains(keyword)) {
            return true;
        }
        
        // Kiểm tra từng từ trong keyword có xuất hiện trong text không
        String[] keywordWords = keyword.split("\\s+");
        int matchCount = 0;
        for (String word : keywordWords) {
            if (word.length() >= 2 && text.contains(word)) {
                matchCount++;
            }
        }
        // Nếu >= 50% từ trong keyword match, coi như match
        return matchCount * 2 >= keywordWords.length;
    }

    /**
     * Kiểm tra xem có phải câu hỏi về giá không - Mở rộng với nhiều biến thể
     */
    private boolean isPriceQuestion(String message) {
        String[] priceKeywords = {
            "giá", "gia", "price", "cost", "chi phí", "chi phi",
            "bao nhiêu", "bao nhieu", "how much", "how many",
            "giá cả", "gia ca", "pricing", "giá tiền", "gia tien",
            "giá bán", "gia ban", "giá bao nhiêu", "gia bao nhieu",
            "giá thế nào", "gia the nao", "giá như thế nào", "gia nhu the nao",
            "giá bao nhiêu tiền", "gia bao nhieu tien", "giá rẻ", "gia re",
            "giá đắt", "gia dat", "giá cao", "gia cao", "giá thấp", "gia thap",
            "giá khoảng", "gia khoang", "giá tầm", "gia tam",
            "vnđ", "vnd", "đồng", "dong", "nghìn", "nghin", "triệu", "trieu",
            "k", "tr", "bao nhiêu tiền", "bao nhieu tien"
        };
        return Arrays.stream(priceKeywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra xem có phải câu hỏi tìm kiếm sản phẩm không - Mở rộng với nhiều biến thể
     */
    private boolean isProductSearchQuestion(String message) {
        String[] searchKeywords = {
            "tìm", "tim", "find", "search", "có", "co", "have",
            "sản phẩm", "san pham", "product", "mũ", "mu", "helmet",
            "tìm kiếm", "tim kiem", "tìm sản phẩm", "tim san pham",
            "có sản phẩm", "co san pham", "có mũ", "co mu",
            "bán", "ban", "sell", "cung cấp", "cung cap", "provide",
            "mua", "buy", "purchase", "đặt hàng", "dat hang", "order",
            "xem", "view", "show", "hiển thị", "hien thi", "display",
            "cho tôi xem", "cho toi xem", "cho mình xem", "cho minh xem",
            "giới thiệu", "gioi thieu", "introduce", "recommend",
            "gợi ý", "goi y", "suggest", "recommendation"
        };
        return Arrays.stream(searchKeywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra xem có phải câu hỏi về sản phẩm rẻ nhất/đắt nhất không - Mở rộng
     */
    private boolean isCheapestOrMostExpensiveQuestion(String message) {
        String[] keywords = {
            "rẻ nhất", "re nhat", "cheapest", "lowest",
            "đắt nhất", "dat nhat", "most expensive", "highest",
            "giá thấp", "gia thap", "low price",
            "giá cao", "gia cao", "high price",
            "rẻ", "re", "cheap", "giá rẻ nhất", "gia re nhat",
            "đắt", "dat", "expensive", "giá đắt nhất", "gia dat nhat",
            "giá thấp nhất", "gia thap nhat", "giá cao nhất", "gia cao nhat",
            "rẻ hơn", "re hon", "cheaper", "đắt hơn", "dat hon", "more expensive"
        };
        return Arrays.stream(keywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra xem có phải câu hỏi về tồn kho không - Mở rộng
     */
    private boolean isStockQuestion(String message) {
        String[] stockKeywords = {
            "còn hàng", "con hang", "in stock", "available",
            "hết hàng", "het hang", "out of stock",
            "tồn kho", "ton kho", "stock", "số lượng", "so luong", "quantity",
            "còn không", "con khong", "còn không ạ", "con khong a",
            "còn bao nhiêu", "con bao nhieu", "còn mấy", "con may",
            "số lượng còn", "so luong con", "còn lại", "con lai",
            "còn bao nhiêu cái", "con bao nhieu cai", "còn mấy cái", "con may cai",
            "có hàng không", "co hang khong", "có còn hàng", "co con hang"
        };
        return Arrays.stream(stockKeywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra xem có liên quan đến sản phẩm không - Mở rộng
     */
    private boolean isProductRelated(String message) {
        String[] productKeywords = {
            "sản phẩm", "san pham", "product",
            "mũ", "mu", "helmet", "nón", "non",
            "mua", "buy", "purchase", "đặt hàng", "dat hang",
            "mũ bảo hiểm", "mu bao hiem", "nón bảo hiểm", "non bao hiem",
            "hàng", "hang", "item", "goods", "sản phẩm nào", "san pham nao",
            "có gì", "co gi", "what", "có những gì", "co nhung gi",
            "danh sách", "danh sach", "list", "catalogue", "catalog"
        };
        return Arrays.stream(productKeywords).anyMatch(message::contains);
    }

    /**
     * Kiểm tra greeting - Mở rộng với nhiều biến thể hơn
     */
    private boolean isGreeting(String message) {
        String[] greetings = {
            "xin chào", "chào", "chao", "hello", "hi", "hey",
            "alo", "good morning", "good afternoon", "good evening",
            "chào bạn", "chao ban", "chào shop", "chao shop",
            "xin chao", "chào em", "chao em", "chào anh", "chao anh",
            "chào chị", "chao chi", "chào cô", "chao co",
            "hi bạn", "hi ban", "hello bạn", "hello ban",
            "chào mừng", "chao mung", "welcome"
        };
        // Kiểm tra nếu message chỉ chứa greeting (không có câu hỏi khác)
        boolean hasGreeting = Arrays.stream(greetings).anyMatch(message::contains);
        if (hasGreeting) {
            // Nếu message ngắn hoặc chỉ có greeting, coi như greeting
            if (message.length() < 30) {
                return true;
            }
            // Nếu có greeting ở đầu câu, cũng coi như greeting
            String trimmed = message.trim();
            for (String greeting : greetings) {
                if (trimmed.startsWith(greeting)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Trích xuất keyword sản phẩm từ câu hỏi - Cải thiện để giữ lại nhiều từ có ý nghĩa hơn
     */
    private String extractProductKeyword(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        // Loại bỏ các từ không cần thiết (stop words)
        String[] stopWords = {
            "tìm", "tim", "find", "search", "có", "co", "have", "của", "cua",
            "sản phẩm", "san pham", "product", "mũ", "mu", "helmet", "nón", "non",
            "giá", "gia", "price", "bao nhiêu", "bao nhieu", "how much",
            "cho", "tôi", "toi", "me", "bạn", "ban", "you", "với", "voi", "with",
            "dưới", "duoi", "under", "trên", "tren", "above", "khoảng", "khoang", "about",
            "vnđ", "vnd", "đồng", "dong", "k", "nghìn", "nghin", "triệu", "trieu",
            "mình", "minh", "em", "anh", "chị", "chi", "cô", "co", "chú", "chu",
            "xin", "vui lòng", "vui long", "please", "ạ", "a", "nhé", "nhe",
            "xem", "view", "show", "hiển thị", "hien thi", "display",
            "gợi ý", "goi y", "suggest", "recommend", "giới thiệu", "gioi thieu"
        };

        String normalized = normalizeText(message);
        String keyword = normalized;
        
        // Loại bỏ stop words
        for (String stopWord : stopWords) {
            keyword = keyword.replaceAll("\\b" + stopWord + "\\b", " ").trim();
        }
        
        // Loại bỏ các số và ký tự đặc biệt không cần thiết
        keyword = keyword.replaceAll("\\d+", " ").trim();
        keyword = keyword.replaceAll("[^\\p{L}\\s]", " ").trim();

        // Lấy tất cả các từ có ý nghĩa (dài hơn 1 ký tự, không phải số)
        String[] words = keyword.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            word = word.trim();
            // Giữ lại từ có độ dài >= 2 ký tự (giảm từ 3 xuống 2 để giữ nhiều từ hơn)
            if (word.length() >= 2 && !word.matches("\\d+")) {
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
     * Trích xuất giá từ câu hỏi (hỗ trợ nhiều format: 500k, 500000, 500.000, 1tr, 2triệu, v.v.)
     * ✅ CẢI THIỆN: Nhận diện "k" và "tr"/"triệu" tốt hơn
     * - "k" cuối số = thêm 3 số 0 (1k = 1000, 10k = 10000, 100k = 100000)
     * - "tr" hoặc "triệu" = thêm 6 số 0 (1tr = 1000000, 2tr = 2000000)
     */
    private BigDecimal extractPriceFromMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        
        // ✅ Pattern cải thiện: ưu tiên nhận diện số + "k"/"tr"/"triệu" ngay sau số (không cần khoảng trắng)
        // Ví dụ: 1k, 10k, 100k, 1tr, 2tr, 1triệu, 2triệu
        // Pattern 1: Số + "k"/"tr"/"triệu" ngay sau số (không có khoảng trắng) - ưu tiên
        java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile(
            "(\\d+(?:[.,]\\d+)*)\\s*(k|tr|triệu|trieu)\\b"
        );
        // Pattern 2: Số + đơn vị có khoảng trắng hoặc các đơn vị khác
        java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile(
            "(\\d+(?:[.,]\\d+)*)\\s+(nghìn|nghin|vnđ|vnd|đồng|dong)\\b"
        );
        
        BigDecimal maxPrice = null;
        
        // Thử pattern 1 trước (ưu tiên "k" và "tr" ngay sau số)
        java.util.regex.Matcher matcher1 = pattern1.matcher(lowerMessage);
        while (matcher1.find()) {
            try {
                String numberStr = matcher1.group(1).replaceAll("[.,]", "");
                String unit = matcher1.group(2);
                
                BigDecimal price = new BigDecimal(numberStr);
                
                // Xử lý đơn vị
                if (unit != null) {
                    String normalizedUnit = unit.toLowerCase(Locale.ROOT);
                    // "k" = thêm 3 số 0 (1000) - 1k = 1000, 10k = 10000, 100k = 100000
                    if (normalizedUnit.equals("k") || normalizedUnit.contains("nghìn") || normalizedUnit.contains("nghin")) {
                        price = price.multiply(new BigDecimal("1000"));
                    } 
                    // "tr" hoặc "triệu" = thêm 6 số 0 (1000000) - 1tr = 1000000, 2tr = 2000000
                    else if (normalizedUnit.equals("tr") || normalizedUnit.contains("triệu") || normalizedUnit.contains("trieu")) {
                        price = price.multiply(new BigDecimal("1000000"));
                    }
                }
                
                // Chỉ lấy giá hợp lệ (>= 1000 VNĐ)
                if (price.compareTo(new BigDecimal("1000")) >= 0) {
                    if (maxPrice == null || price.compareTo(maxPrice) > 0) {
                        maxPrice = price;
                    }
                }
            } catch (Exception e) {
                // Bỏ qua
            }
        }
        
        // Nếu chưa tìm thấy, thử pattern 2
        if (maxPrice == null) {
            java.util.regex.Matcher matcher2 = pattern2.matcher(lowerMessage);
            while (matcher2.find()) {
                try {
                    String numberStr = matcher2.group(1).replaceAll("[.,]", "");
                    String unit = matcher2.group(2);
                    
                    BigDecimal price = new BigDecimal(numberStr);
                    
                    // Xử lý đơn vị
                    if (unit != null) {
                        String normalizedUnit = unit.toLowerCase(Locale.ROOT);
                        if (normalizedUnit.contains("nghìn") || normalizedUnit.contains("nghin")) {
                            price = price.multiply(new BigDecimal("1000"));
                        }
                    }
                    
                    // Chỉ lấy giá hợp lệ (>= 1000 VNĐ)
                    if (price.compareTo(new BigDecimal("1000")) >= 0) {
                        if (maxPrice == null || price.compareTo(maxPrice) > 0) {
                            maxPrice = price;
                        }
                    }
                } catch (Exception e) {
                    // Bỏ qua
                }
            }
        }
        
        return maxPrice;
    }

    private PriceRange extractPriceRange(String message) {
        if (message == null) {
            return null;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        // ✅ Cải thiện pattern: nhận diện "k" và "tr"/"triệu" tốt hơn
        java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile(
                "(\\d+(?:[.,]\\d+)*)\\s*(k|tr|triệu|trieu|nghin|ngan|vnđ|vnd|dong)?\\s*(?:-|->|—|–|to|toi|den|tu|and|&|~)\\s*(\\d+(?:[.,]\\d+)*)\\s*(k|tr|triệu|trieu|nghin|ngan|vnđ|vnd|dong)?",
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
        String digits = rawNumber.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) {
            return null;
        }
        BigDecimal price = new BigDecimal(digits);
        String resolvedUnit = (unit != null && !unit.isBlank()) ? unit : fallbackUnit;
        if (resolvedUnit != null) {
            String normalizedUnit = resolvedUnit.toLowerCase(Locale.ROOT);
            // "k" = thêm 3 số 0 (1000)
            if (normalizedUnit.equals("k") || normalizedUnit.contains("nghin") || normalizedUnit.contains("ngan") || normalizedUnit.contains("nghìn")) {
                price = price.multiply(new BigDecimal("1000"));
            } 
            // "tr" hoặc "triệu" = thêm 6 số 0 (1000000)
            else if (normalizedUnit.equals("tr") || normalizedUnit.contains("trieu") || normalizedUnit.contains("triệu")) {
                price = price.multiply(new BigDecimal("1000000"));
            }
        }
        if (price.compareTo(new BigDecimal("1000")) < 0) {
            return null;
        }
        return price;
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
     * Trích xuất suggestedProducts từ reply và message
     * Lấy từ ThreadLocal hoặc từ các trường hợp đặc biệt
     */
    private List<SanPhamResponse> extractSuggestedProducts(String customerMessage, String reply) {
        // Lấy từ ThreadLocal trước (đã được set trong các handle methods)
        List<SanPhamResponse> products = ThreadLocalSuggestedProducts.get();
        if (products != null && !products.isEmpty()) {
            ThreadLocalSuggestedProducts.remove(); // Clear sau khi lấy
            return products;
        }
        
        // Nếu reply chứa "gợi ý" và "bán chạy", lấy sản phẩm bán chạy
        if (reply != null && reply.contains("gợi ý") && reply.contains("bán chạy")) {
            return getBestSellingProductsForSuggestion(5);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Lấy danh sách sản phẩm bán chạy nhất và convert sang SanPhamResponse
     * Dùng để gợi ý khi không tìm thấy sản phẩm
     */
    public List<SanPhamResponse> getBestSellingProductsForSuggestion(int limit) {
        try {
            List<BestSellingProductDTO> bestSelling = statisticsService.getBestSellingProducts(limit);
            List<SanPhamResponse> suggestedProducts = new ArrayList<>();
            
            for (BestSellingProductDTO bestProduct : bestSelling) {
                if (bestProduct.getSanPhamId() != null) {
                    try {
                        SanPhamResponse product = sanPhamService.getById(bestProduct.getSanPhamId());
                        if (product != null && Boolean.TRUE.equals(product.getTrangThai())) {
                            suggestedProducts.add(product);
                        }
                    } catch (Exception e) {
                        log.warn("Không thể lấy sản phẩm ID {}: {}", bestProduct.getSanPhamId(), e.getMessage());
                    }
                }
            }
            
            return suggestedProducts;
        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm bán chạy: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}