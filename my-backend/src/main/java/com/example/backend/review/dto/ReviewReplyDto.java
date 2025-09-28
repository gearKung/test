package com.example.backend.review.dto;

import lombok.Data;

@Data
public class ReviewReplyDto {
    private String content;
    private Long ownerId;
}