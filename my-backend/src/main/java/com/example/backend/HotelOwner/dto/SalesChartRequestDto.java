package com.example.backend.HotelOwner.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SalesChartRequestDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long hotelId; // "ALL" 대신 null 사용
    private String roomType; // "ALL" 대신 null 사용
}