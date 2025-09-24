-- 기존 내용을 모두 지우고 아래 코드로 교체하세요.

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

-- 사용자 데이터 (1번: 호텔 소유주, 2~4번: 일반 고객)
INSERT INTO users (id, name, email, password, phone, role, provider, created_on, address) VALUES
(1, '홍길동', 'test@com.my', '$2a$10$U/Ga3xnsdtt20xy2EgcBFe3zeYhMHBeTlfY8yidWBBBFwpZhYHE.C', '010-1234-5678', 'USER', 'LOCAL', NOW(), '서울'),
(2, '김여행', 'guest1@egoda.com', '$2a$10$f3bYV.N0k5sV1o8V.Zf8q.aG5h/bYJ/gYxN2T.9K.Z6.dYI.dZ/lC', '010-1111-1111', 'USER', 'LOCAL', NOW(), '부산'),
(3, '박휴가', 'guest2@egoda.com', '$2a$10$f3bYV.N0k5sV1o8V.Zf8q.aG5h/bYJ/gYxN2T.9K.Z6.dYI.dZ/lC', '010-2222-2222', 'USER', 'LOCAL', NOW(), '강릉'),
(4, '최체크인', 'guest3@egoda.com', '$2a$10$f3bYV.N0k5sV1o8V.Zf8q.aG5h/bYJ/gYxN2T.9K.Z6.dYI.dZ/lC', '010-3333-3333', 'USER', 'LOCAL', NOW(), '서울');

-- 호텔 데이터 (소유자: 1번 홍길동)
INSERT INTO hotel (id, user_id, name, address, star_rating, country, description, status) VALUES
(1, 1, '강릉 씨마크 호텔', '강원도 강릉시 해안로 123', 5, '대한민국', '동해바다가 보이는 프리미엄 호텔', 'APPROVED'),
(2, 1, '파라다이스 호텔 부산', '부산광역시 해운대구 해운대로 456', 5, '대한민국', '해운대 해변에 위치한 럭셔리 호텔', 'APPROVED');

-- 객실 데이터
INSERT INTO room (id, hotel_id, room_type, room_size, capacity_min, capacity_max, price, room_count, check_in_time, check_out_time) VALUES
(1, 1, '디럭스룸', '35㎡', 2, 3, 250000, 10, '15:00:00', '11:00:00'),
(2, 1, '스위트룸', '50㎡', 2, 4, 450000, 5, '15:00:00', '11:00:00'),
(3, 2, '스탠다드룸', '30㎡', 1, 2, 180000, 20, '15:00:00', '11:00:00');

-- 예약 및 결제 데이터
-- 2025-10-05일에 예약을 집중시켜 캘린더 UI 테스트
INSERT INTO reservation (id, user_id, room_id, num_rooms, num_adult, num_kid, start_date, end_date, status, created_at) VALUES
(101, 2, 1, 1, 2, 0, '2025-10-05 00:00:00', '2025-10-06 00:00:00', 'COMPLETED', NOW()),
(102, 3, 1, 1, 2, 1, '2025-10-05 00:00:00', '2025-10-07 00:00:00', 'COMPLETED', NOW()),
(103, 4, 2, 1, 2, 0, '2025-10-05 00:00:00', '2025-10-06 00:00:00', 'COMPLETED', NOW()),
(104, 2, 2, 1, 2, 0, '2025-10-05 00:00:00', '2025-10-08 00:00:00', 'CANCELLED', NOW()),
(105, 3, 3, 1, 1, 0, '2025-10-05 00:00:00', '2025-10-06 00:00:00', 'COMPLETED', NOW()),
(106, 4, 3, 1, 2, 1, '2025-10-10 00:00:00', '2025-10-12 00:00:00', 'COMPLETED', NOW()),
(107, 2, 1, 1, 2, 0, '2025-10-11 00:00:00', '2025-10-13 00:00:00', 'COMPLETED', NOW()),
(108, 3, 2, 1, 2, 0, '2025-10-15 00:00:00', '2025-10-17 00:00:00', 'CANCELLED', NOW()),
(109, 4, 3, 1, 1, 1, '2025-10-20 00:00:00', '2025-10-22 00:00:00', 'COMPLETED', NOW()),
(110, 2, 1, 1, 2, 0, '2025-11-01 00:00:00', '2025-11-03 00:00:00', 'COMPLETED', NOW());

INSERT INTO payment (id, order_id, amount, reservation_id, user_id, customer_name, email, phone, status) VALUES
(1, 'order_101', 250000, 101, 2, '김여행', 'guest1@egoda.com', '010-1111-1111', 'COMPLETED'),
(2, 'order_102', 500000, 102, 3, '박휴가', 'guest2@egoda.com', '010-2222-2222', 'COMPLETED'),
(3, 'order_103', 450000, 103, 4, '최체크인', 'guest3@egoda.com', '010-3333-3333', 'COMPLETED'),
(4, 'order_104', 1350000, 104, 2, '김여행', 'guest1@egoda.com', '010-1111-1111', 'CANCELLED'),
(5, 'order_105', 180000, 105, 3, '박휴가', 'guest2@egoda.com', '010-2222-2222', 'COMPLETED'),
(6, 'order_106', 360000, 106, 4, '최체크인', 'guest3@egoda.com', '010-3333-3333', 'COMPLETED'),
(7, 'order_107', 500000, 107, 2, '김여행', 'guest1@egoda.com', '010-1111-1111', 'COMPLETED'),
(8, 'order_108', 900000, 108, 3, '박휴가', 'guest2@egoda.com', '010-2222-2222', 'CANCELLED'),
(9, 'order_109', 360000, 109, 4, '최체크인', 'guest3@egoda.com', '010-3333-3333', 'COMPLETED'),
(10, 'order_110', 500000, 110, 2, '김여행', 'guest1@egoda.com', '010-1111-1111', 'COMPLETED');