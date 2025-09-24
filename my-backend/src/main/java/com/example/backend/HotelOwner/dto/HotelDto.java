package com.example.backend.HotelOwner.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.domain.HotelImage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HotelDto {
    private Long id;
    private Long businessId;
    private String name;
    private String address;
    private Integer starRating;
    private String description;
    private String country;
    private String status;

    // ✨ [추가] 다중 이미지 URL을 위한 필드
    private List<String> imageUrls;

    // ✨ [추가] 편의시설 ID 목록을 위한 필드
    private List<Long> amenityIds;

    public static HotelDto fromEntity(Hotel hotel) {
        HotelDto dto = new HotelDto();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setAddress(hotel.getAddress());
        dto.setStarRating(hotel.getStarRating());
        dto.setDescription(hotel.getDescription());
        dto.setCountry(hotel.getCountry());
        dto.setStatus(hotel.getStatus().name());
        dto.setBusinessId(hotel.getBusinessId());

        if (hotel.getImages() != null) {
            dto.imageUrls = hotel.getImages().stream()
                    .map(HotelImage::getUrl)
                    .collect(Collectors.toList());
        }

        if (hotel.getHotelAmenities() != null) {
            dto.amenityIds = hotel.getHotelAmenities().stream()
                    .map(hotelAmenity -> hotelAmenity.getAmenity().getId())
                    .collect(Collectors.toList());
        }

        return dto;
    }
}