package com.example.backend.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ghtk")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class GHTKController {

    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Test endpoint để kiểm tra controller có hoạt động
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("GHTK Controller is working!");
    }
    
    // GHTK API endpoint
    private static final String GHTK_API_URL = "https://services.giaohangtietkiem.vn/services/shipment/fee";
    
    // GHTK Token (cần cấu hình trong application.properties)
    // @Value("${ghtk.token}")
    private String ghtkToken = ""; // Cần cấu hình token thực tế

    /**
     * Tính phí vận chuyển GHTK
     * Proxy request từ frontend đến GHTK API
     * Endpoint: POST /api/ghtk/calculate-fee
     */
    @PostMapping("/calculate-fee")
    public ResponseEntity<?> calculateShippingFee(@RequestBody(required = false) Map<String, Object> request) {
        // Nếu request null, trả về phí mặc định
        if (request == null) {
            request = new HashMap<>();
        }
        
        System.out.println("🚚 GHTK API Request received: " + request);
        
        try {
            // Chuẩn bị request body cho GHTK API
            Map<String, Object> ghtkRequest = new HashMap<>();
            
            // Địa chỉ gửi (mặc định - cần cấu hình)
            ghtkRequest.put("pick_province", request.getOrDefault("pick_province", "Hà Nội"));
            ghtkRequest.put("pick_district", request.getOrDefault("pick_district", "Quận Ba Đình"));
            ghtkRequest.put("pick_ward", request.getOrDefault("pick_ward", "Phường Điện Biên"));
            
            // Địa chỉ nhận
            ghtkRequest.put("province", request.get("province"));
            ghtkRequest.put("district", request.get("district"));
            if (request.containsKey("ward") && request.get("ward") != null) {
                ghtkRequest.put("ward", request.get("ward"));
            }
            if (request.containsKey("address") && request.get("address") != null) {
                ghtkRequest.put("address", request.get("address"));
            }
            
            // Trọng lượng (gram)
            ghtkRequest.put("weight", request.getOrDefault("weight", 1000));
            
            // Giá trị đơn hàng (VND)
            ghtkRequest.put("value", request.getOrDefault("value", 0));
            
            // Phương thức vận chuyển
            ghtkRequest.put("transport", request.getOrDefault("transport", "road"));
            
            // Tùy chọn giao hàng
            ghtkRequest.put("deliver_option", request.getOrDefault("deliver_option", "none"));

            System.out.println("🚚 GHTK API Payload: " + ghtkRequest);

            // Headers cho GHTK API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (ghtkToken != null && !ghtkToken.isEmpty()) {
                headers.set("Token", ghtkToken);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ghtkRequest, headers);

            // Gọi GHTK API
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GHTK_API_URL,
                HttpMethod.POST,
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            System.out.println("✅ GHTK API Response: " + response.getBody());
            
            // Xử lý response từ GHTK
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                // Nếu GHTK trả về success = true và có fee
                if (Boolean.TRUE.equals(responseBody.get("success")) && responseBody.containsKey("fee")) {
                    return ResponseEntity.ok(responseBody);
                }
                // Nếu có delivery object
                if (responseBody.containsKey("delivery")) {
                    return ResponseEntity.ok(responseBody);
                }
            }

            // Nếu response không đúng format, trả về phí mặc định
            return ResponseEntity.ok(createDefaultResponse(request));
            
        } catch (Exception e) {
            System.err.println("❌ GHTK API Error: " + e.getMessage());
            e.printStackTrace();
            
            // Trả về phí mặc định khi có lỗi
            return ResponseEntity.ok(createDefaultResponse(request));
        }
    }
    
    /**
     * Tạo response mặc định với phí tính toán
     */
    private Map<String, Object> createDefaultResponse(Map<String, Object> request) {
        String province = (String) request.getOrDefault("province", "");
        int defaultFee = calculateDefaultFee(request);
        
        System.out.println("💰 Calculating default fee for province: " + province);
        System.out.println("💰 Calculated fee: " + defaultFee);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tính phí mặc định (API GHTK không khả dụng)");
        
        Map<String, Object> feeData = new HashMap<>();
        feeData.put("fee", defaultFee);
        feeData.put("name", "Phí vận chuyển tiêu chuẩn");
        response.put("fee", feeData);
        
        System.out.println("💰 Response: " + response);
        
        return response;
    }

    /**
     * Tính phí mặc định dựa trên khoảng cách và vùng miền (fallback khi API GHTK không khả dụng)
     */
    private int calculateDefaultFee(Map<String, Object> request) {
        String province = (String) request.getOrDefault("province", "");
        int weight = ((Number) request.getOrDefault("weight", 1000)).intValue();
        
        // Phí cơ bản theo vùng miền (địa chỉ gửi: Hà Nội)
        int baseFee = getBaseFeeByRegion(province);
        
        // Điều chỉnh theo trọng lượng (mỗi 500g thêm 5,000 VND)
        double weightKg = weight / 1000.0;
        if (weightKg > 1) {
            baseFee += (int) Math.ceil((weightKg - 1) / 0.5) * 5000;
        }
        
        return baseFee;
    }
    
    /**
     * Tính phí cơ bản dựa trên vùng miền
     * Địa chỉ gửi mặc định: Hà Nội
     */
    private int getBaseFeeByRegion(String province) {
        System.out.println("📍 [Backend] Getting base fee for province: " + province);
        
        if (province == null || province.isEmpty()) {
            System.out.println("📍 [Backend] No province, returning default 30,000");
            return 30000; // Mặc định
        }
        
        // Cùng tỉnh/thành phố với Hà Nội
        if (province.contains("Hà Nội")) {
            System.out.println("📍 [Backend] Hà Nội detected, returning 25,000");
            return 25000; // 25,000 VND - nội thành
        }
        
        // Các tỉnh/thành phố miền Bắc (gần Hà Nội)
        String[] mienBac = {
            "Hải Phòng", "Hưng Yên", "Hải Dương", "Bắc Ninh", "Vĩnh Phúc", 
            "Thái Nguyên", "Bắc Giang", "Quảng Ninh", "Hà Nam", "Nam Định",
            "Thái Bình", "Ninh Bình", "Phú Thọ", "Tuyên Quang", "Yên Bái",
            "Lào Cai", "Lạng Sơn", "Cao Bằng", "Bắc Kạn", "Hòa Bình",
            "Sơn La", "Điện Biên", "Lai Châu"
        };
        
        for (String tinh : mienBac) {
            if (province.contains(tinh)) {
                System.out.println("📍 [Backend] Miền Bắc detected (" + tinh + "), returning 35,000");
                return 35000; // 35,000 VND - miền Bắc
            }
        }
        
        // Các tỉnh/thành phố miền Nam (gần TP.HCM) - KIỂM TRA TRƯỚC để tránh conflict với "Bình Thuận"
        // Kiểm tra TP.HCM trước (có nhiều cách viết)
        String provinceLower = province.toLowerCase();
        if (provinceLower.contains("hồ chí minh") || 
            provinceLower.contains("tp.hcm") || 
            provinceLower.contains("tp hcm") ||
            provinceLower.contains("ho chi minh")) {
            System.out.println("📍 [Backend] TP.HCM detected, returning 60,000");
            return 60000; // 60,000 VND - miền Nam
        }
        
        // Danh sách các tỉnh miền Nam - kiểm tra trước miền Trung để tránh conflict
        String[] mienNamGan = {
            "Bình Dương", "Đồng Nai", "Bà Rịa - Vũng Tàu", "Bà Rịa-Vũng Tàu",
            "Tây Ninh", "Bình Phước", "Long An", "Tiền Giang", "Bến Tre",
            "Trà Vinh", "Vĩnh Long", "Đồng Tháp", "An Giang", "Kiên Giang",
            "Cần Thơ", "Hậu Giang", "Sóc Trăng", "Bạc Liêu", "Cà Mau"
        };
        
        for (String tinh : mienNamGan) {
            if (province.contains(tinh)) {
                System.out.println("📍 [Backend] Miền Nam detected (" + tinh + "), returning 60,000");
                return 60000; // 60,000 VND - miền Nam
            }
        }
        
        // Các tỉnh/thành phố Tây Nguyên
        String[] tayNguyen = {
            "Kon Tum", "Gia Lai", "Đắk Lắk", "Đắk Nông", "Lâm Đồng"
        };
        
        for (String tinh : tayNguyen) {
            if (province.contains(tinh)) {
                System.out.println("📍 [Backend] Tây Nguyên detected (" + tinh + "), returning 55,000");
                return 55000; // 55,000 VND - Tây Nguyên
            }
        }
        
        // Các tỉnh/thành phố miền Trung - KIỂM TRA SAU miền Nam
        String[] mienTrung = {
            "Thanh Hóa", "Nghệ An", "Hà Tĩnh", "Quảng Bình", "Quảng Trị",
            "Thừa Thiên Huế", "Đà Nẵng", "Quảng Nam", "Quảng Ngãi", "Bình Định",
            "Phú Yên", "Khánh Hòa", "Ninh Thuận", "Bình Thuận"
        };
        
        for (String tinh : mienTrung) {
            if (province.contains(tinh)) {
                System.out.println("📍 [Backend] Miền Trung detected (" + tinh + "), returning 50,000");
                return 50000; // 50,000 VND - miền Trung
            }
        }
        
        // Các tỉnh/thành phố miền Nam xa hơn
        System.out.println("📍 [Backend] Unknown province, returning 70,000 (xa nhất)");
        return 70000; // 70,000 VND - các tỉnh xa nhất
    }
}

