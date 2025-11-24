package com.example.backend.service;

import com.example.backend.dto.LichLamRequest;
import com.example.backend.dto.LichLamResponse;
import com.example.backend.dto.LichLamWeekResponse;
import com.example.backend.entity.lich_lam;
import com.example.backend.entity.NhanVien;
import com.example.backend.entity.User;
import com.example.backend.repository.LichLamRepository;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LichLamService {

    private final LichLamRepository lichLamRepository;
    private final NhanVienRepository nhanVienRepository;
    private final UserRepository userRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Lấy lịch làm theo tuần và năm
     */
    public LichLamWeekResponse getLichLamByWeek(Integer week, Integer year) {
        // Tính ngày bắt đầu và kết thúc tuần
        LocalDate weekStart = calculateWeekStart(week, year);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        // Lấy tất cả lịch làm và filter theo tuần
        List<lich_lam> allLichLam = lichLamRepository.findAll();
        List<lich_lam> lichLamList = allLichLam.stream()
                .filter(ll -> {
                    LocalDate llDate = parseDate(ll.getDate());
                    return llDate != null && !llDate.isBefore(weekStart) && !llDate.isAfter(weekEnd);
                })
                .collect(Collectors.toList());
        
        // Lấy tất cả nhân viên đang hoạt động
        List<NhanVien> nhanVienList = nhanVienRepository.findByTrangThai(true);
        
        // Tạo map để nhóm lịch làm theo user
        Map<Long, List<lich_lam>> lichLamByUser = lichLamList.stream()
                .collect(Collectors.groupingBy(l -> l.getUser().getId()));
        
        // Tạo danh sách nhân viên với ca làm
        List<LichLamWeekResponse.NhanVienCaLam> nhanVienCaLamList = new ArrayList<>();
        
        for (NhanVien nhanVien : nhanVienList) {
            if (nhanVien.getUser() == null) continue;
            
            Long userId = nhanVien.getUser().getId();
            List<lich_lam> userLichLam = lichLamByUser.getOrDefault(userId, new ArrayList<>());
            
            // Tạo map ca làm theo thứ
            Map<String, Integer> caLamMap = new HashMap<>();
            String position = null;
            
            for (lich_lam ll : userLichLam) {
                // Kiểm tra xem ngày có trong tuần không
                LocalDate llDate = parseDate(ll.getDate());
                if (llDate != null && !llDate.isBefore(weekStart) && !llDate.isAfter(weekEnd)) {
                    caLamMap.put(ll.getDayOfWeek(), parseShiftToInt(ll.getShift()));
                    if (position == null) {
                        position = ll.getPosition();
                    }
                }
            }
            
            // Tạo object ca làm trong tuần
            LichLamWeekResponse.NhanVienCaLam.CaLamWeek caLamWeek = LichLamWeekResponse.NhanVienCaLam.CaLamWeek.builder()
                    .thu2(caLamMap.get("Thứ 2"))
                    .thu3(caLamMap.get("Thứ 3"))
                    .thu4(caLamMap.get("Thứ 4"))
                    .thu5(caLamMap.get("Thứ 5"))
                    .thu6(caLamMap.get("Thứ 6"))
                    .thu7(caLamMap.get("Thứ 7"))
                    .chuNhat(caLamMap.get("Chủ Nhật"))
                    .build();
            
            LichLamWeekResponse.NhanVienCaLam nhanVienCaLam = LichLamWeekResponse.NhanVienCaLam.builder()
                    .userId(userId)
                    .maNhanVien(nhanVien.getMaNhanVien())
                    .tenNhanVien(nhanVien.getHoTen())
                    .position(position != null ? position : "Nhân Viên Thu Ngân")
                    .caLam(caLamWeek)
                    .build();
            
            nhanVienCaLamList.add(nhanVienCaLam);
        }
        
        return LichLamWeekResponse.builder()
                .week(week)
                .year(year)
                .weekStartDate(weekStart.format(DATE_FORMATTER))
                .weekEndDate(weekEnd.format(DATE_FORMATTER))
                .nhanVienList(nhanVienCaLamList)
                .build();
    }

    /**
     * Lưu lịch làm cho một nhân viên trong tuần
     */
    public void saveLichLam(LichLamRequest request) {
        // Validate request
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId không được để trống");
        }
        if (request.getWeek() == null || request.getYear() == null) {
            throw new IllegalArgumentException("week và year không được để trống");
        }
        if (request.getPosition() == null || request.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("position không được để trống");
        }
        
        log.info("Bắt đầu lưu lịch làm cho userId: {}, week: {}, year: {}", 
            request.getUserId(), request.getWeek(), request.getYear());
        
        // Tìm user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + request.getUserId()));
        
        // Tính ngày bắt đầu và kết thúc tuần
        LocalDate weekStart = calculateWeekStart(request.getWeek(), request.getYear());
        LocalDate weekEnd = weekStart.plusDays(6);
        
        log.info("Tuần bắt đầu: {}, tuần kết thúc: {}", weekStart, weekEnd);
        
        // Lấy tất cả lịch làm của user và xóa những cái trong tuần này
        List<lich_lam> userLichLam = lichLamRepository.findByUser(user);
        List<lich_lam> toDelete = userLichLam.stream()
                .filter(ll -> {
                    LocalDate llDate = parseDate(ll.getDate());
                    return llDate != null && !llDate.isBefore(weekStart) && !llDate.isAfter(weekEnd);
                })
                .collect(Collectors.toList());
        
        log.info("Số lịch làm cũ cần xóa: {}", toDelete.size());
        
        // Xóa lịch làm cũ
        if (!toDelete.isEmpty()) {
            lichLamRepository.deleteAll(toDelete);
        }
        
        // Tạo lịch làm mới
        int savedCount = 0;
        if (request.getCaLamList() != null) {
            for (LichLamRequest.CaLamItem item : request.getCaLamList()) {
                if (item.getShift() == null) continue; // Bỏ qua nếu không có ca
                
                lich_lam lichLam = new lich_lam();
                lichLam.setUser(user);
                lichLam.setDayOfWeek(item.getDayOfWeek());
                lichLam.setShift("Ca " + item.getShift());
                lichLam.setPosition(request.getPosition());
                lichLam.setDate(item.getDate());
                
                lichLamRepository.save(lichLam);
                savedCount++;
            }
        }
        
        log.info("Đã lưu {} ca làm cho user {} tuần {} năm {}", 
            savedCount, request.getUserId(), request.getWeek(), request.getYear());
    }

    /**
     * Lấy lịch sử ca làm
     */
    public List<LichLamResponse> getLichSuCaLam(Long userId) {
        List<lich_lam> lichLamList;
        
        if (userId != null) {
            lichLamList = lichLamRepository.findHistoryByUserId(userId);
        } else {
            lichLamList = lichLamRepository.findAllHistory();
        }
        
        return lichLamList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tính ngày bắt đầu tuần (Thứ 2)
     */
    private LocalDate calculateWeekStart(Integer week, Integer year) {
        // Lấy ngày 1 tháng 1 của năm
        LocalDate jan1 = LocalDate.of(year, 1, 1);
        
        // Tính ngày thứ 2 đầu tiên của năm
        int dayOfWeek = jan1.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        LocalDate firstMonday;
        
        if (dayOfWeek == 1) {
            firstMonday = jan1;
        } else if (dayOfWeek == 7) {
            firstMonday = jan1.plusDays(2); // Nếu 1/1 là Chủ Nhật, thứ 2 đầu tiên là 3/1
        } else {
            firstMonday = jan1.plusDays(8 - dayOfWeek); // Tính đến thứ 2 đầu tiên
        }
        
        // Tính ngày bắt đầu của tuần hiện tại
        return firstMonday.plusWeeks(week - 1);
    }

    /**
     * Parse shift string thành integer (Ca 1 -> 1, Ca 2 -> 2, ...)
     */
    private Integer parseShiftToInt(String shift) {
        if (shift == null || shift.isEmpty()) return null;
        try {
            // Lấy số từ "Ca 1", "Ca 2", ...
            String number = shift.replaceAll("[^0-9]", "");
            return number.isEmpty() ? null : Integer.parseInt(number);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse date string thành LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Không thể parse date: {}", dateStr);
            return null;
        }
    }

    /**
     * Convert entity to response DTO
     */
    private LichLamResponse convertToResponse(lich_lam lichLam) {
        // Tìm nhân viên theo user
        NhanVien nhanVien = nhanVienRepository.findByUserId(lichLam.getUser().getId())
                .orElse(null);
        
        // Tính tuần và năm từ ngày
        LocalDate date = parseDate(lichLam.getDate());
        Integer week = null;
        Integer year = null;
        if (date != null) {
            year = date.getYear();
            // Tính tuần (đơn giản hóa)
            week = calculateWeekFromDate(date);
        }
        
        return LichLamResponse.builder()
                .id(lichLam.getId())
                .userId(lichLam.getUser().getId())
                .userName(lichLam.getUser().getFullName() != null ? 
                         lichLam.getUser().getFullName() : 
                         lichLam.getUser().getUsername())
                .maNhanVien(nhanVien != null ? nhanVien.getMaNhanVien() : null)
                .dayOfWeek(lichLam.getDayOfWeek())
                .shift(lichLam.getShift())
                .position(lichLam.getPosition())
                .date(lichLam.getDate())
                .week(week)
                .year(year)
                .build();
    }

    /**
     * Tính tuần từ ngày
     */
    private Integer calculateWeekFromDate(LocalDate date) {
        int year = date.getYear();
        LocalDate jan1 = LocalDate.of(year, 1, 1);
        
        int dayOfWeek = jan1.getDayOfWeek().getValue();
        LocalDate firstMonday;
        
        if (dayOfWeek == 1) {
            firstMonday = jan1;
        } else if (dayOfWeek == 7) {
            firstMonday = jan1.plusDays(2);
        } else {
            firstMonday = jan1.plusDays(8 - dayOfWeek);
        }
        
        if (date.isBefore(firstMonday)) {
            // Thuộc tuần cuối năm trước
            return 52;
        }
        
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(firstMonday, date);
        int week = (int) (daysBetween / 7) + 1;
        
        return week > 52 ? 52 : week;
    }
    
}

