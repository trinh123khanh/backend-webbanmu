package com.example.backend.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ghn")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class GHNController {

    private final RestTemplate restTemplate = new RestTemplate();
    
    // GHN API endpoints
    private static final String GHN_API_BASE_URL = "https://online-gateway.ghn.vn/shiip/public-api/v2";
    private static final String GHN_AVAILABLE_SERVICES_URL = GHN_API_BASE_URL + "/shipping-order/available-services";
    private static final String GHN_FEE_URL = GHN_API_BASE_URL + "/shipping-order/fee";
    
    // GHN Token và Shop ID (cần cấu hình trong application.properties)
    // @Value("${ghn.token}")
    private String ghnToken = ""; // Cần cấu hình token thực tế
    
    // @Value("${ghn.shop_id}")
    private int ghnShopId = 0; // Cần cấu hình shop_id thực tế
    
    // Địa chỉ gửi mặc định (cần cấu hình)
    private static final int DEFAULT_FROM_DISTRICT_ID = 1442; // Quận Ba Đình, Hà Nội
    private static final String DEFAULT_FROM_WARD_CODE = "10001"; // Phường Điện Biên

    /**
     * Test endpoint để kiểm tra controller có hoạt động
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("GHN Controller is working!");
    }

    /**
     * Lấy danh sách dịch vụ khả dụng
     * Endpoint: POST /api/ghn/available-services
     */
    @PostMapping("/available-services")
    public ResponseEntity<?> getAvailableServices(@RequestBody Map<String, Object> request) {
        System.out.println("🚚 GHN Available Services Request: " + request);
        
        try {
            Integer fromDistrictId = (Integer) request.getOrDefault("from_district_id", DEFAULT_FROM_DISTRICT_ID);
            Integer toDistrictId = (Integer) request.get("to_district_id");
            
            if (toDistrictId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("to_district_id is required"));
            }
            
            // Chuẩn bị request body cho GHN API
            Map<String, Object> ghnRequest = new HashMap<>();
            ghnRequest.put("shop_id", ghnShopId);
            ghnRequest.put("from_district", fromDistrictId);
            ghnRequest.put("to_district", toDistrictId);
            
            System.out.println("🚚 GHN API Payload: " + ghnRequest);
            
            // Headers cho GHN API
            HttpHeaders headers = createGHNHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ghnRequest, headers);
            
            // Gọi GHN API
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GHN_AVAILABLE_SERVICES_URL,
                HttpMethod.POST,
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            System.out.println("✅ GHN API Response: " + response.getBody());
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            System.err.println("❌ GHN API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(createErrorResponse("Lỗi khi lấy danh sách dịch vụ: " + e.getMessage()));
        }
    }

    /**
     * Tính phí vận chuyển GHN
     * Proxy request từ frontend đến GHN API
     * Endpoint: POST /api/ghn/calculate-fee
     */
    @PostMapping("/calculate-fee")
    public ResponseEntity<?> calculateShippingFee(@RequestBody(required = false) Map<String, Object> request) {
        if (request == null) {
            request = new HashMap<>();
        }
        
        System.out.println("🚚 GHN Calculate Fee Request: " + request);
        
        try {
            // Lấy các tham số từ request
            Integer fromDistrictId = getIntegerValue(request, "from_district_id", DEFAULT_FROM_DISTRICT_ID);
            String fromWardCode = (String) request.getOrDefault("from_ward_code", DEFAULT_FROM_WARD_CODE);
            Integer toDistrictId = getIntegerValue(request, "to_district_id", null);
            String toWardCode = (String) request.get("to_ward_code");
            Integer weight = getIntegerValue(request, "weight", 1000);
            Integer length = getIntegerValue(request, "length", 20);
            Integer width = getIntegerValue(request, "width", 20);
            Integer height = getIntegerValue(request, "height", 20);
            Integer insuranceValue = getIntegerValue(request, "insurance_value", 0);
            Integer serviceTypeId = getIntegerValue(request, "service_type_id", null);
            String coupon = (String) request.get("coupon");
            
            if (toDistrictId == null || toDistrictId == 0) {
                // Nếu không có district_id, sử dụng phí mặc định
                return ResponseEntity.ok(createDefaultResponse(request));
            }
            
            // Nếu chưa có service_type_id, lấy dịch vụ đầu tiên từ available services
            if (serviceTypeId == null) {
                serviceTypeId = getDefaultServiceTypeId(fromDistrictId, toDistrictId);
            }
            
            // Chuẩn bị request body cho GHN API
            Map<String, Object> ghnRequest = new HashMap<>();
            ghnRequest.put("service_type_id", serviceTypeId);
            ghnRequest.put("from_district_id", fromDistrictId);
            ghnRequest.put("from_ward_code", fromWardCode);
            ghnRequest.put("to_district_id", toDistrictId);
            if (toWardCode != null && !toWardCode.isEmpty()) {
                ghnRequest.put("to_ward_code", toWardCode);
            }
            ghnRequest.put("weight", weight);
            ghnRequest.put("length", length);
            ghnRequest.put("width", width);
            ghnRequest.put("height", height);
            ghnRequest.put("insurance_value", insuranceValue);
            if (coupon != null && !coupon.isEmpty()) {
                ghnRequest.put("coupon", coupon);
            }
            
            System.out.println("🚚 GHN API Payload: " + ghnRequest);
            
            // Headers cho GHN API
            HttpHeaders headers = createGHNHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ghnRequest, headers);
            
            // Gọi GHN API
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GHN_FEE_URL,
                HttpMethod.POST,
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            System.out.println("✅ GHN API Response: " + response.getBody());
            
            // Xử lý response từ GHN
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                // Nếu GHN trả về code = 200 và có data
                Integer code = (Integer) responseBody.get("code");
                if (code != null && code == 200 && responseBody.containsKey("data")) {
                    return ResponseEntity.ok(responseBody);
                }
            }
            
            // Nếu response không đúng format, trả về phí mặc định
            return ResponseEntity.ok(createDefaultResponse(request));
            
        } catch (Exception e) {
            System.err.println("❌ GHN API Error: " + e.getMessage());
            e.printStackTrace();
            
            // Trả về phí mặc định khi có lỗi
            return ResponseEntity.ok(createDefaultResponse(request));
        }
    }
    
    /**
     * Tạo headers cho GHN API
     */
    private HttpHeaders createGHNHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (ghnToken != null && !ghnToken.isEmpty()) {
            headers.set("Token", ghnToken);
        }
        headers.set("ShopId", String.valueOf(ghnShopId));
        return headers;
    }
    
    /**
     * Lấy service_type_id mặc định từ available services
     */
    private Integer getDefaultServiceTypeId(int fromDistrictId, int toDistrictId) {
        try {
            Map<String, Object> servicesRequest = new HashMap<>();
            servicesRequest.put("shop_id", ghnShopId);
            servicesRequest.put("from_district", fromDistrictId);
            servicesRequest.put("to_district", toDistrictId);
            
            HttpHeaders headers = createGHNHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(servicesRequest, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GHN_AVAILABLE_SERVICES_URL,
                HttpMethod.POST,
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Integer code = (Integer) responseBody.get("code");
                if (code != null && code == 200 && responseBody.containsKey("data")) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String, Object>> services = (java.util.List<Map<String, Object>>) responseBody.get("data");
                    if (services != null && !services.isEmpty()) {
                        Map<String, Object> firstService = services.get(0);
                        return (Integer) firstService.get("service_type_id");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error getting default service type: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Tạo response mặc định với phí tính toán
     */
    private Map<String, Object> createDefaultResponse(Map<String, Object> request) {
        String province = (String) request.getOrDefault("province", "");
        int defaultFee = calculateDefaultFee(province);
        
        System.out.println("💰 Calculating default fee for province: " + province);
        System.out.println("💰 Calculated fee: " + defaultFee);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "Tính phí mặc định (API GHN không khả dụng)");
        
        Map<String, Object> feeData = new HashMap<>();
        feeData.put("total", defaultFee);
        feeData.put("service_fee", defaultFee);
        feeData.put("insurance_fee", 0);
        feeData.put("pick_station_fee", 0);
        feeData.put("coupon_value", 0);
        feeData.put("r2s_fee", 0);
        response.put("data", feeData);
        
        System.out.println("💰 Response: " + response);
        
        return response;
    }
    
    /**
     * Tính phí mặc định dựa trên khoảng cách và vùng miền (fallback khi API GHN không khả dụng)
     */
    private int calculateDefaultFee(String province) {
        if (province == null || province.isEmpty()) {
            return 30000; // Mặc định
        }
        
        // Cùng tỉnh/thành phố với Hà Nội
        if (province.contains("Hà Nội")) {
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
                return 35000; // 35,000 VND - miền Bắc
            }
        }
        
        // Các tỉnh/thành phố miền Nam (gần TP.HCM) - KIỂM TRA TRƯỚC
        String provinceLower = province.toLowerCase();
        if (provinceLower.contains("hồ chí minh") || 
            provinceLower.contains("tp.hcm") || 
            provinceLower.contains("tp hcm") ||
            provinceLower.contains("ho chi minh")) {
            return 60000; // 60,000 VND - miền Nam
        }
        
        String[] mienNamGan = {
            "Bình Dương", "Đồng Nai", "Bà Rịa - Vũng Tàu", "Bà Rịa-Vũng Tàu",
            "Tây Ninh", "Bình Phước", "Long An", "Tiền Giang", "Bến Tre",
            "Trà Vinh", "Vĩnh Long", "Đồng Tháp", "An Giang", "Kiên Giang",
            "Cần Thơ", "Hậu Giang", "Sóc Trăng", "Bạc Liêu", "Cà Mau"
        };
        
        for (String tinh : mienNamGan) {
            if (province.contains(tinh)) {
                return 60000; // 60,000 VND - miền Nam
            }
        }
        
        // Các tỉnh/thành phố Tây Nguyên
        String[] tayNguyen = {
            "Kon Tum", "Gia Lai", "Đắk Lắk", "Đắk Nông", "Lâm Đồng"
        };
        
        for (String tinh : tayNguyen) {
            if (province.contains(tinh)) {
                return 55000; // 55,000 VND - Tây Nguyên
            }
        }
        
        // Các tỉnh/thành phố miền Trung
        String[] mienTrung = {
            "Thanh Hóa", "Nghệ An", "Hà Tĩnh", "Quảng Bình", "Quảng Trị",
            "Thừa Thiên Huế", "Đà Nẵng", "Quảng Nam", "Quảng Ngãi", "Bình Định",
            "Phú Yên", "Khánh Hòa", "Ninh Thuận", "Bình Thuận"
        };
        
        for (String tinh : mienTrung) {
            if (province.contains(tinh)) {
                return 50000; // 50,000 VND - miền Trung
            }
        }
        
        // Các tỉnh/thành phố miền Nam xa hơn
        return 70000; // 70,000 VND - các tỉnh xa nhất
    }
    
    /**
     * Tạo response lỗi
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", message);
        return response;
    }
    
    /**
     * Helper method để lấy giá trị Integer từ Map
     */
    private Integer getIntegerValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}

