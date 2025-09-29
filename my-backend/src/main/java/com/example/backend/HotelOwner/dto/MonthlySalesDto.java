package com.example.backend.HotelOwner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // JPQL new DTO 생성을 위해 기본 생성자 추가
public class MonthlySalesDto {
    private String yearMonth;
    private Long totalSales;
}