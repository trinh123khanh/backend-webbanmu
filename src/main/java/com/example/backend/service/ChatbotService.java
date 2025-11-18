package com.example.backend.service;

import com.example.backend.dto.SanPhamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    /**
     * Phân tích câu hỏi và tạo phản hồi thông minh dựa trên dữ liệu sản phẩm
     */
    public String generateReply(String customerMessage) {
        if (customerMessage == null || customerMessage.trim().isEmpty()) {
            return "Xin chào! Tôi có thể giúp gì cho bạn?";
        }

        String normalizedMessage = normalizeText(customerMessage);
        log.debug("Processing customer message: {}", normalizedMessage);

        // Kiểm tra greeting
        if (isGreeting(normalizedMessage)) {
            return "Xin chào bạn! Rất vui được hỗ trợ. Tôi có thể giúp bạn tìm kiếm sản phẩm, xem giá cả, hoặc tư vấn về các loại mũ bảo hiểm. Bạn cần gì?";
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
        return "Cảm ơn bạn đã liên hệ! Tôi có thể giúp bạn tìm kiếm sản phẩm, xem giá cả, hoặc tư vấn về các loại mũ bảo hiểm. Bạn muốn biết gì cụ thể?";
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
                return "Hiện tại chúng tôi chưa có sản phẩm nào trong kho. Vui lòng quay lại sau!";
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

            // Kiểm tra xem có hỏi về giá cụ thể không
            BigDecimal askedPrice = extractPriceFromMessage(message);
            if (askedPrice != null && askedPrice.compareTo(BigDecimal.ZERO) > 0) {
                List<SanPhamResponse> matchingProducts = activeProducts.stream()
                        .filter(p -> p.getGiaBan() != null && p.getGiaBan().compareTo(askedPrice) <= 0)
                        .limit(5)
                        .collect(Collectors.toList());

                if (!matchingProducts.isEmpty()) {
                    StringBuilder reply = new StringBuilder();
                    reply.append(String.format("Chúng tôi có các sản phẩm dưới %s VNĐ:\n\n", formatPrice(askedPrice)));
                    for (SanPhamResponse product : matchingProducts) {
                        reply.append(String.format("• %s - %s VNĐ\n", product.getTenSanPham(), formatPrice(product.getGiaBan())));
                    }
                    if (matchingProducts.size() < activeProducts.size()) {
                        reply.append("\nBạn có muốn xem thêm sản phẩm khác không?");
                    }
                    return reply.toString();
                } else {
                    return String.format("Hiện tại chúng tôi không có sản phẩm nào dưới %s VNĐ. Giá sản phẩm của chúng tôi dao động từ %s đến %s VNĐ.", 
                            formatPrice(askedPrice), formatPrice(minPrice), formatPrice(maxPrice));
                }
            }

            // Trả về thông tin giá chung
            return String.format("Giá sản phẩm của chúng tôi dao động từ %s đến %s VNĐ. Bạn muốn xem sản phẩm nào cụ thể không?", 
                    formatPrice(minPrice), formatPrice(maxPrice));

        } catch (Exception e) {
            log.error("Error handling price question", e);
            return "Xin lỗi, tôi gặp lỗi khi tìm kiếm thông tin giá. Vui lòng thử lại sau hoặc liên hệ nhân viên để được hỗ trợ.";
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
            
            Pageable pageable = PageRequest.of(0, 20); // Tăng số lượng để lọc tốt hơn
            Page<SanPhamResponse> products = sanPhamService.search(keyword, true, pageable);
            List<SanPhamResponse> results = products.getContent();

            // Lọc theo giá nếu có
            if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
                results = results.stream()
                        .filter(p -> p.getGiaBan() != null && p.getGiaBan().compareTo(priceFilter) <= 0)
                        .collect(Collectors.toList());
            }

            if (results.isEmpty()) {
                String noResultMessage = "Xin lỗi, tôi không tìm thấy sản phẩm nào";
                if (keyword != null && !keyword.trim().isEmpty() && !keyword.equals("")) {
                    noResultMessage += String.format(" với từ khóa '%s'", keyword);
                }
                if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
                    noResultMessage += String.format(" dưới %s VNĐ", formatPrice(priceFilter));
                }
                noResultMessage += ". Bạn có thể thử từ khóa khác hoặc điều chỉnh bộ lọc không?";
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
            } else {
                reply.append(String.format("Tôi tìm thấy %d sản phẩm", results.size()));
                if (priceFilter != null && priceFilter.compareTo(BigDecimal.ZERO) > 0) {
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
     * Trích xuất giá từ câu hỏi (hỗ trợ nhiều format: 500k, 500000, 500.000, v.v.)
     */
    private BigDecimal extractPriceFromMessage(String message) {
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        
        // Pattern để tìm số với các format khác nhau
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(\\d+(?:[.,]\\d+)*)\\s*(k|nghìn|nghin|triệu|trieu|vnđ|vnd|đồng|dong)?"
        );
        java.util.regex.Matcher matcher = pattern.matcher(lowerMessage);
        
        BigDecimal maxPrice = null;
        while (matcher.find()) {
            try {
                String numberStr = matcher.group(1).replaceAll("[.,]", "");
                String unit = matcher.group(2);
                
                BigDecimal price = new BigDecimal(numberStr);
                
                // Xử lý đơn vị
                if (unit != null) {
                    if (unit.contains("k") || unit.contains("nghìn") || unit.contains("nghin")) {
                        price = price.multiply(new BigDecimal("1000"));
                    } else if (unit.contains("triệu") || unit.contains("trieu")) {
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
        
        return maxPrice;
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
}

