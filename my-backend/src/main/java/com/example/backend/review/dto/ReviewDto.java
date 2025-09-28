package com.example.backend.review.dto;

import com.example.backend.review.domain.Review;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ReviewDto {
    private Long id;
    private Long replyId;
    private String authorName;
    private String hotelName;
    private int rating;
    private String content;
    private List<String> imageUrls;
    private String replyContent;
    private LocalDateTime createdAt;
    private boolean replied;

    public static ReviewDto fromEntity(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .replyId(review.getReply() != null ? review.getReply().getId() : null)
                .authorName(review.getUser().getName())
                .hotelName(review.getHotel().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrls(review.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .collect(Collectors.toList()))
                .replyContent(review.getReply() != null ? review.getReply().getContent() : null)
                .replied(review.getReply() != null)
                .createdAt(review.getCreatedAt())
                .build();
    }
}