// my-backend/src/main/java/com/example/backend/service/AmenityService.java
package com.example.backend.service;

import com.example.backend.domain.Amenity;
import com.example.backend.dto.AmenityDto;
import com.example.backend.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AmenityService {

    private final AmenityRepository amenityRepository;

    public List<AmenityDto> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(AmenityDto::fromEntity)
                .collect(Collectors.toList());
    }
}