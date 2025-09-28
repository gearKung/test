package com.example.backend.review.service;

import com.example.backend.HotelOwner.domain.User;
import com.example.backend.review.domain.Review;
import com.example.backend.review.domain.ReviewReply;
import com.example.backend.review.dto.ReviewReplyDto;
import com.example.backend.review.repository.ReviewReplyRepository;
import com.example.backend.review.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final UserRepository userRepository;

    public void addReplyToReview(Long reviewId, ReviewReplyDto replyDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        User owner = userRepository.findById(replyDto.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ReviewReply reply = ReviewReply.builder()
                .review(review)
                .owner(owner)
                .content(replyDto.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        review.setReply(reply);
        reviewRepository.save(review);
    }

    public void updateReply(Long replyId, ReviewReplyDto replyDto) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("답변을 찾을 수 없습니다."));
        reply.setContent(replyDto.getContent());
        reviewReplyRepository.save(reply);
    }

    public void reportReview(Long reviewId, Long ownerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        // 해당 리뷰가 로그인한 사장님의 호텔에 달린 것이 맞는지 권한 확인
        if (!review.getHotel().getOwner().getId().equals(ownerId)) {
            throw new SecurityException("해당 리뷰를 신고할 권한이 없습니다.");
        }

        // 이미 신고되었거나 숨김 처리된 리뷰는 중복 신고 불가
        if (review.getStatus() != Review.ReviewStatus.VISIBLE) {
            throw new IllegalStateException("이미 신고되었거나 관리자에 의해 처리된 리뷰입니다.");
        }

        // 상태를 '신고됨'으로 변경
        review.setStatus(Review.ReviewStatus.REPORTED);
        reviewRepository.save(review);
    }
}