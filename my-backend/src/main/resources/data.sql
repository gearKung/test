-- 편의시설 데이터
INSERT INTO amenity (name, description, icon_url, fee_type, is_active, category) VALUES
('무료 Wi-Fi', '객실 및 공용 공간에서 무료 Wi-Fi 제공', 'wifi_icon.png', 'FREE', true, 'IN_ROOM'),
('수영장', '야외 또는 실내 수영장', 'pool_icon.png', 'FREE', true, 'LEISURE'),
('헬스장', '피트니스 센터', 'gym_icon.png', 'FREE', true, 'LEISURE'),
('주차장', '투숙객 전용 주차 공간', 'parking_icon.png', 'PAID', true, 'IN_HOTEL'),
('레스토랑', '호텔 내 레스토랑', 'restaurant_icon.png', 'PAID', true, 'FNB'),
('바', '칵테일 및 음료를 즐길 수 있는 바', 'bar_icon.png', 'PAID', true, 'FNB'),
('룸서비스', '24시간 룸서비스', 'roomservice_icon.png', 'PAID', true, 'IN_ROOM'),
('비즈니스 센터', '회의 및 업무 지원 시설', 'business_icon.png', 'PAID', true, 'BUSINESS'),
('세탁 서비스', '의류 세탁 및 다림질 서비스', 'laundry_icon.png', 'PAID', true, 'IN_HOTEL'),
('반려동물 동반', '반려동물 동반 가능 객실', 'pet_icon.png', 'PAID', true, 'OTHER');

-- 사용자 데이터
INSERT INTO users (id, name, email, password, phone, role, provider, created_on, address) VALUES
(1, '홍길동', 'test@com.my', '$2a$10$U/Ga3xnsdtt20xy2EgcBFe3zeYhMHBeTlfY8yidWBBBFwpZhYHE.C', '010-1234-5678', 'USER', 'LOCAL', NOW(), '서울'),
(2, '김여행', 'guest1@egoda.com', '$2a$10$f3bYV.N0k5sV1o8V.Zf8q.aG5h/bYJ/gYxN2T.9K.Z6.dYI.dZ/lC', '010-1111-1111', 'USER', 'LOCAL', NOW(), '부산'),
(3, '박휴가', 'guest2@egoda.com', '$2a$10$f3bYV.N0k5sV1o8V.Zf8q.aG5h/bYJ/gYxN2T.9K.Z6.dYI.dZ/lC', '010-2222-2222', 'USER', 'LOCAL', NOW(), '강릉'),
(4, '최체크인', 'guest3@egoda.com', '$2a$10$f3bYV.N0k5sV1o8V.Zf8q.aG5h/bYJ/gYxN2T.9K.Z6.dYI.dZ/lC', '010-3333-3333', 'USER', 'LOCAL', NOW(), '서울');

-- 호텔 및 객실 데이터
INSERT INTO hotel (id, user_id, name, address, star_rating, country, description, status) VALUES
(1, 1, '강릉 씨마크 호텔', '강원도 강릉시 해안로 123', 5, '대한민국', '동해바다가 보이는 프리미엄 호텔', 'APPROVED'),
(2, 1, '파라다이스 호텔 부산', '부산광역시 해운대구 해운대로 456', 5, '대한민국', '해운대 해변에 위치한 럭셔리 호텔', 'APPROVED');

INSERT INTO room (id, hotel_id, room_type, room_size, capacity_min, capacity_max, price, room_count, check_in_time, check_out_time) VALUES
(1, 1, '디럭스룸', '35㎡', 2, 3, 250000, 10, '15:00:00', '11:00:00'),
(2, 1, '스위트룸', '50㎡', 2, 4, 450000, 5, '15:00:00', '11:00:00'),
(3, 2, '스탠다드룸', '30㎡', 1, 2, 180000, 20, '15:00:00', '11:00:00');

-- 기존 데이터 삭제 (중복 방지)
-- DELETE FROM payment;
-- DELETE FROM reservation;

-- 8월 예약 데이터
INSERT INTO reservation (id, user_id, room_id, num_rooms, num_adult, num_kid, start_date, end_date, status, created_at) VALUES
(301, 2, 1, 1, 2, 1, '2025-08-01 00:00:00', '2025-08-03 00:00:00', 'COMPLETED', '2025-07-10 10:00:00'),
(302, 3, 2, 1, 2, 0, '2025-08-05 00:00:00', '2025-08-08 00:00:00', 'COMPLETED', '2025-07-15 11:00:00'),
(303, 4, 3, 2, 3, 2, '2025-08-15 00:00:00', '2025-08-17 00:00:00', 'COMPLETED', '2025-07-20 14:00:00'),
(304, 2, 3, 1, 1, 0, '2025-08-20 00:00:00', '2025-08-22 00:00:00', 'CANCELLED', '2025-08-01 09:00:00');

-- 9월 예약 데이터
INSERT INTO reservation (id, user_id, room_id, num_rooms, num_adult, num_kid, start_date, end_date, status, created_at) VALUES
(101, 2, 1, 1, 2, 0, '2025-09-05 00:00:00', '2025-09-07 00:00:00', 'COMPLETED', '2025-08-11 10:00:00'),
(102, 3, 1, 1, 2, 1, '2025-09-12 00:00:00', '2025-09-14 00:00:00', 'COMPLETED', '2025-08-12 11:00:00'),
(103, 4, 2, 1, 2, 0, '2025-09-15 00:00:00', '2025-09-16 00:00:00', 'COMPLETED', '2025-08-13 12:00:00'),
(104, 2, 2, 1, 2, 0, '2025-09-22 00:00:00', '2025-09-25 00:00:00', 'CANCELLED', '2025-08-14 13:00:00'),
(105, 3, 3, 1, 1, 0, '2025-09-28 00:00:00', '2025-09-30 00:00:00', 'COMPLETED', '2025-08-15 14:00:00');

-- 10월 예약 데이터 (특정일 집중 포함)
INSERT INTO reservation (id, user_id, room_id, num_rooms, num_adult, num_kid, start_date, end_date, status, created_at) VALUES
(106, 4, 3, 1, 2, 1, '2025-10-01 00:00:00', '2025-10-03 00:00:00', 'COMPLETED', '2025-09-06 15:00:00'),
(107, 2, 1, 1, 2, 0, '2025-10-04 00:00:00', '2025-10-05 00:00:00', 'COMPLETED', '2025-09-07 16:00:00'),
(108, 3, 2, 1, 2, 0, '2025-10-05 00:00:00', '2025-10-06 00:00:00', 'COMPLETED', '2025-09-08 17:00:00'),
(109, 4, 1, 1, 1, 1, '2025-10-05 00:00:00', '2025-10-08 00:00:00', 'COMPLETED', '2025-09-09 18:00:00'),
(110, 2, 3, 2, 4, 0, '2025-10-05 00:00:00', '2025-10-07 00:00:00', 'COMPLETED', '2025-09-10 19:00:00'),
(111, 3, 2, 1, 2, 2, '2025-10-25 00:00:00', '2025-10-28 00:00:00', 'COMPLETED', '2025-09-22 10:00:00');

-- '최근 예약' 테스트용 데이터 (현재 날짜 2025-09-26 기준)
INSERT INTO reservation (id, user_id, room_id, num_rooms, num_adult, num_kid, start_date, end_date, status, created_at) VALUES
(201, 2, 3, 1, 2, 0, '2025-12-01 00:00:00', '2025-12-03 00:00:00', 'COMPLETED', '2025-09-26 10:00:00'),
(202, 3, 1, 1, 1, 0, '2025-11-25 00:00:00', '2025-11-28 00:00:00', 'COMPLETED', '2025-09-25 15:30:00'),
(203, 4, 2, 2, 4, 0, '2025-12-10 00:00:00', '2025-12-11 00:00:00', 'COMPLETED', '2025-09-24 20:00:00');

-- 결제 데이터
INSERT INTO payment (reservation_id, payment_method, base_price, total_price, status, created_at) VALUES
-- 8월 결제
(301, 'CARD', 500000, 550000, 'COMPLETED', '2025-07-10 10:05:00'),
(302, 'TRANSFER', 1350000, 1485000, 'COMPLETED', '2025-07-15 11:05:00'),
(303, 'KAKAO_PAY', 720000, 792000, 'COMPLETED', '2025-07-20 14:05:00'),
-- 9월 결제
(101, 'CARD', 500000, 550000, 'COMPLETED', '2025-08-11 10:05:00'),
(102, 'TRANSFER', 500000, 550000, 'COMPLETED', '2025-08-12 11:05:00'),
(103, 'CARD', 450000, 495000, 'COMPLETED', '2025-08-13 12:05:00'),
(105, 'NAVER_PAY', 360000, 396000, 'COMPLETED', '2025-08-15 14:05:00'),
-- 10월 결제
(106, 'CARD', 360000, 396000, 'COMPLETED', '2025-09-06 15:05:00'),
(107, 'TRANSFER', 250000, 275000, 'COMPLETED', '2025-09-07 16:05:00'),
(108, 'CARD', 450000, 495000, 'COMPLETED', '2025-09-08 17:05:00'),
(109, 'KAKAO_PAY', 750000, 825000, 'COMPLETED', '2025-09-09 18:05:00'),
(110, 'NAVER_PAY', 720000, 792000, 'COMPLETED', '2025-09-10 19:05:00'),
(111, 'CARD', 1350000, 1485000, 'COMPLETED', '2025-09-22 10:05:00'),
-- 최근 예약 결제
(201, 'NAVER_PAY', 360000, 396000, 'COMPLETED', '2025-09-26 10:05:00'),
(202, 'CARD', 750000, 825000, 'COMPLETED', '2025-09-25 15:35:00'),
(203, 'TRANSFER', 900000, 990000, 'COMPLETED', '2025-09-24 20:05:00');