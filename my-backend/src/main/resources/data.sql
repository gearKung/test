-- src/main/resources/data.sql

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