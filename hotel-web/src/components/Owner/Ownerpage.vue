<template>
  <div class="owner-page">
    <aside class="sidebar">
      <div class="logo">🏨 Owner</div>
      <nav>
        <ul>
          <li :class="{ active: activeMenu === 'dashboard' }" @click="activeMenu = 'dashboard'">대시보드</li>
          <li :class="{ active: activeMenu === 'hotels' }" @click="activeMenu = 'hotels'">호텔/객실 관리</li>
          <li :class="{ active: activeMenu === 'reservations' }" @click="activeMenu = 'reservations'">예약 관리</li>
          <li :class="{ active: activeMenu === 'reviews' }" @click="activeMenu = 'reviews'">리뷰 관리</li>
        </ul>
      </nav>
    </aside>

    <main class="main-content">
      
      <section v-if="activeMenu === 'dashboard'">
        <div class="header-actions">
          <h2>대시보드</h2>
          <div class="user-actions">
            <span v-if="user" class="user-name">{{ user.name }}님</span>
            <button class="logout-btn" @click="logout">로그아웃</button>
          </div>
        </div>
        <div class="dashboard-grid">
          <div class="stat-card">
            <h4>오늘 매출</h4>
            <p>₩ 1,250,000</p>
            <span class="comparison positive">+5.2% vs 어제</span>
          </div>
          <div class="stat-card">
            <h4>이번 주 매출</h4>
            <p>₩ 8,760,000</p>
            <span class="comparison positive">+12.8% vs 지난주</span>
          </div>
          <div class="stat-card">
            <h4>이번 달 매출</h4>
            <p>₩ 34,800,000</p>
            <span class="comparison negative">-2.1% vs 지난달</span>
          </div>
        </div>
        <div class="placeholder-chart">
            <p>월별 매출 차트 (개발 예정)</p>
        </div>
      </section>

      <div v-if="activeMenu === 'hotels'">
        <section v-if="currentView === 'list'">
          <div class="header-actions">
            <h2>내 호텔 목록</h2>
            <div class="user-actions">
              <span v-if="user" class="user-name">{{ user.name }}님</span>
              <button class="add-btn" @click="openCreateForm">호텔 등록</button>
              <button class="logout-btn" @click="logout">로그아웃</button>
            </div>
          </div>
          <div class="hotel-grid">
            <div v-for="h in myHotels" :key="h.id" class="hotel-card" @click="showHotelDetails(h)">
              <img :src="h.imageUrls && h.imageUrls.length > 0 ? h.imageUrls[0] : 'https://via.placeholder.com/300'" alt="호텔 대표 이미지" class="hotel-card-image"/>
              <div class="hotel-card-info">
                <strong>{{ h.name }}</strong>
                <span>{{ h.address }}</span>
              </div>
            </div>
          </div>
        </section>

        <section v-if="currentView === 'details' && selectedHotel">
           <div class="header-actions">
             <button class="back-btn" @click="goToList">← 호텔 목록으로</button>
             <div class="user-actions">
              <span v-if="user" class="user-name">{{ user.name }}님</span>
              <button class="logout-btn" @click="logout">로그아웃</button>
            </div>
           </div>
           <div class="hotel-details-view">
              <img :src="selectedHotel.imageUrls && selectedHotel.imageUrls.length > 0 ? selectedHotel.imageUrls[0] : 'https://via.placeholder.com/400'" alt="호텔 대표 이미지" class="details-image"/>
              <div class="details-info">
                <h2>{{ selectedHotel.name }}</h2>
                <p><strong>주소:</strong> {{ selectedHotel.address }}, {{ selectedHotel.country }}</p>
                <p><strong>성급:</strong> {{ selectedHotel.starRating }}성</p>
                <p><strong>설명:</strong> {{ selectedHotel.description || '등록된 설명이 없습니다.' }}</p>
                <div class="details-actions">
                  <button class="btn-edit" @click="editHotel(selectedHotel)">수정</button>
                  <button class="btn-delete" @click="deleteHotel(selectedHotel.id)">삭제</button>
                  <button class="btn-rooms" @click="showRoomList(selectedHotel)">객실 보기</button>
                </div>
              </div>
           </div>
        </section>
        
        <section v-if="currentView === 'rooms' && selectedHotel">
          <div class="header-actions">
            <button class="back-btn" @click="showHotelDetails(selectedHotel)">← 호텔 정보로</button>
            <div class="user-actions">
               <span v-if="user" class="user-name">{{ user.name }}님</span>
               <button class="logout-btn" @click="logout">로그아웃</button>
            </div>
          </div>
          <h3>{{ selectedHotel.name }} - 객실 관리</h3>
           <div class="header-actions secondary">
            <p>등록된 객실 수: {{ rooms.length }}</p>
            <button class="add-btn" @click="openRoomCreateForm">객실 추가</button>
          </div>
          
          <ul class="room-list">
             <li v-for="room in rooms" :key="room.id" class="room-item">
                <img :src="room.imageUrls && room.imageUrls.length > 0 ? room.imageUrls[0] : 'https://via.placeholder.com/150'" alt="객실 대표 이미지" class="room-image" />
                <div class="room-info">
                  <strong>{{ room.roomType }}</strong>
                  <span>- 크기: {{ room.roomSize }}</span>
                  <span>- 인원: {{ room.capacityMin }}~{{ room.capacityMax }}명</span>
                  <span>- 가격: {{ room.price.toLocaleString() }}원</span>
                </div>
                <div class="actions">
                  <button @click="editRoom(room)">수정</button>
                  <button @click="deleteRoom(room.id)">삭제</button>
                </div>
             </li>
          </ul>
        </section>

        <section v-if="currentView === 'hotelForm'">
          <div class="form-wrapper">
          <div class="header-actions">
              <button class="back-btn" @click="cancelHotelForm">← 뒤로가기</button>
              <div class="user-actions">
               <span v-if="user" class="user-name">{{ user.name }}님</span>
               <button class="logout-btn" @click="logout">로그아웃</button>
              </div>
          </div>

          <div class="form-container">
            <h2>{{ editingHotel ? '호텔 수정' : '새 호텔 등록' }}</h2>
            <form @submit.prevent="handleHotelSubmit">
              <div class="form-group"><label>호텔명</label><input v-model="hotelForm.name" required /></div>
              <div class="form-group"><label>사업자번호 (선택)</label><input v-model.number="hotelForm.businessId" type="number" /></div>
              <div class="form-group"><label>주소</label><input v-model="hotelForm.address" required /></div>
              <div class="form-group"><label>국가</label><input v-model="hotelForm.country" required /></div>
              <div class="form-group"><label>성급 (1~5)</label><input v-model.number="hotelForm.starRating" type="number" min="1" max="5" required /></div>
              <div class="form-group"><label>호텔 설명</label><textarea v-model="hotelForm.description"></textarea></div>

              <div class="form-group">
                <label>이미지 (첫 번째 이미지가 대표 이미지)</label>
                <input type="file" @change="handleHotelFileChange" multiple accept="image/*" class="file-input">
                <div class="image-preview-grid">
                  <div v-for="(url, index) in hotelForm.imageUrls" :key="'url-' + index" class="image-preview-item">
                    <img :src="url" alt="기존 이미지"/>
                    <button type="button" class="btn-remove-img" @click="removeHotelImage('url', index)">X</button>
                  </div>
                  <div v-for="(file, index) in hotelImageFiles" :key="'file-' + index" class="image-preview-item">
                    <img :src="file.preview" alt="새 이미지"/>
                    <button type="button" class="btn-remove-img" @click="removeHotelImage('file', index)">X</button>
                  </div>
                </div>
              </div>

              <div class="form-group">
                <label>편의시설</label>
                <div class="amenities-grid">
                  <div v-for="amenity in allAmenities" :key="amenity.id" class="amenity-item">
                    <input 
                      type="checkbox" 
                      :id="'amenity-' + amenity.id" 
                      :value="amenity.id"
                      v-model="hotelForm.amenityIds" 
                    />
                    <label :for="'amenity-' + amenity.id">{{ amenity.name }}</label>
                  </div>
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-primary">저장</button>
                <button type="button" class="btn-secondary" @click="cancelHotelForm">취소</button>
              </div>
            </form>
          </div>
        </div>
        </section>
        
        <section v-if="currentView === 'roomForm'">
          <div class="form-wrapper">
          <div class="header-actions">
              <button class="back-btn" @click="showRoomList(selectedHotel)">← 객실 목록으로</button>
              <div class="user-actions">
                  <span v-if="user" class="user-name">{{ user.name }}님</span>
                  <button class="logout-btn" @click="logout">로그아웃</button>
              </div>
          </div>
          <div class="form-container">
            <h2>{{ editingRoom ? '객실 수정' : '새 객실 등록' }}</h2>
              <form @submit.prevent="handleRoomSubmit">
                <div class="form-group">
                  <label>객실 타입</label>
                  <select v-model="roomForm.roomType" required>
                      <option disabled value="">객실 타입을 선택하세요</option>
                      <option>스위트룸</option>
                      <option>디럭스룸</option>
                      <option>스탠다드룸</option>
                      <option>싱글룸</option>
                      <option>트윈룸</option>
                  </select>
                </div>
                <div class="form-group"><label>객실 크기</label><input v-model="roomForm.roomSize" required /></div>
                <div class="form-group"><label>최소/최대 인원</label><div class="inline-group"><input v-model.number="roomForm.capacityMin" type="number" required /><input v-model.number="roomForm.capacityMax" type="number" required /></div></div>
                <div class="form-group"><label>1박 가격</label><input v-model.number="roomForm.price" type="number" required /></div>
                <div class="form-group"><label>객실 수</label><input v-model.number="roomForm.roomCount" type="number" required /></div>
                <div class="form-group"><label>체크인/체크아웃 시간</label><div class="inline-group"><input v-model="roomForm.checkInTime" type="time" required /><input v-model="roomForm.checkOutTime" type="time" required /></div></div>
                
                <div class="form-group">
                  <label>이미지 (첫 번째 이미지가 대표 이미지)</label>
                  <input type="file" @change="handleRoomFileChange" multiple accept="image/*" class="file-input">
                  <div class="image-preview-grid">
                    <div v-for="(url, index) in roomForm.imageUrls" :key="'url-' + index" class="image-preview-item">
                      <img :src="url" alt="기존 이미지"/>
                      <button type="button" class="btn-remove-img" @click="removeRoomImage('url', index)">X</button>
                    </div>
                    <div v-for="(file, index) in roomImageFiles" :key="'file-' + index" class="image-preview-item">
                      <img :src="file.preview" alt="새 이미지"/>
                      <button type="button" class="btn-remove-img" @click="removeRoomImage('file', index)">X</button>
                    </div>
                  </div>
                </div>

                <div class="form-actions">
                  <button type="submit" class="btn-primary">저장</button>
                  <button type="button" class="btn-secondary" @click="showRoomList(selectedHotel)">취소</button>
                </div>
              </form>
          </div>
          </div>
        </section>
      </div>
      
      <section v-if="activeMenu === 'reservations'">
        <div class="header-actions">
          <h2>예약 현황 캘린더</h2>
           <div class="user-actions">
            <span v-if="user" class="user-name">{{ user.name }}님</span>
            <button class="logout-btn" @click="logout">로그아웃</button>
          </div>
        </div>
        <div class="calendar-container">
          <FullCalendar :options="calendarOptions" />
        </div>
      </section>

      <section v-if="activeMenu === 'reviews'">
        <div class="header-actions">
          <h2>리뷰 관리</h2>
           <div class="user-actions">
            <span v-if="user" class="user-name">{{ user.name }}님</span>
            <button class="logout-btn" @click="logout">로그아웃</button>
          </div>
        </div>
        <p>리뷰 관리 기능은 현재 개발 중입니다.</p>
      </section>

    </main>
  </div>
</template>

<script>
import axios from "axios";
import FullCalendar from '@fullcalendar/vue3';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';

export default {
  components: {
    FullCalendar
  },
  data() {
    return {
      activeMenu: "dashboard",
      user: null,
      myHotels: [],
      selectedHotel: null,
      rooms: [],
      editingHotel: null,
      editingRoom: null,
      
      hotelForm: {},
      roomForm: {},
      
      hotelImageFiles: [],
      roomImageFiles: [],

      allAmenities: [],

      currentView: 'list',

      calendarOptions: {
        plugins: [ dayGridPlugin, interactionPlugin ],
        initialView: 'dayGridMonth',
        headerToolbar: {
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,dayGridWeek'
        },
        locale: 'ko',
        events: [], 
      },
    };
  },
  methods: {
    // --- 공통 메소드 ---
    getAuthHeaders() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.$router.push("/login");
        return null;
      }
      return { 'Authorization': `Bearer ${token}` };
    },
    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        alert("로그아웃 되었습니다.");
        this.$router.push('/login');
    },
    goToList() {
      this.selectedHotel = null;
      this.currentView = 'list';
      this.fetchHotels();
    },

    // --- 데이터 조회 메소드 ---
    async fetchHotels() {
      // 1. 로그인된 사용자 정보 확인
      console.log("1. fetchHotels: 현재 사용자 정보", this.user);
      if (!this.user) {
        console.error("사용자 정보가 없어 호텔 목록을 조회할 수 없습니다.");
        return;
      }

      const headers = this.getAuthHeaders();
      if (!headers) {
        console.error("인증 헤더가 없어 API를 호출할 수 없습니다.");
        return;
      }

      // 2. API 호출 직전
      console.log("2. fetchHotels: /api/hotels/my-hotels API 호출 시작");

      try {
        const res = await axios.get(`/api/hotels/my-hotels`, { headers });
        // 3. API 응답 데이터 확인
        console.log("3. fetchHotels: API 응답 데이터", res.data);
        this.myHotels = res.data;
      } catch (err) {
        // 4. 에러 발생 시
        console.error("4. fetchHotels: 호텔 조회 실패:", err.response?.data || err.message);
      }
    },
    async fetchAmenities() {
      const headers = this.getAuthHeaders();
      if (!headers) return;
      try {
        const response = await axios.get('/api/hotels/amenities', { headers });
        this.allAmenities = response.data;
        console.log("전체 편의시설 목록:", this.allAmenities);
      } catch (err) {
        console.error("편의시설 목록 조회 실패:", err);

        // this.allAmenities = [
        //     { id: 1, name: '무료 Wi-Fi' }, { id: 2, name: '수영장' },
        //     { id: 3, name: '헬스장' }, { id: 4, name: '주차장' }
        // ];
      }
    },
    async fetchRooms(hotelId) {
      const headers = this.getAuthHeaders();
      if (!headers) return;
      console.log("1. [객실 조회] API 호출 시작:", `/api/hotels/${hotelId}/rooms`);
      try {
        const res = await axios.get(`/api/hotels/${hotelId}/rooms`, { headers });
        console.log("2. [객실 조회] API 응답 데이터:", res.data);
        this.rooms = res.data;
      } catch (err) {
        console.error("3. [객실 조회] API 호출 실패:", err.response?.data || err.message);
        alert("객실 정보 조회에 실패했습니다.");
      }
    },
    
    // --- 뷰 전환 메소드 ---
    showHotelDetails(hotel) {
      this.selectedHotel = hotel;
      this.currentView = 'details';
    },
    async showRoomList(hotel) {
      this.selectedHotel = hotel;
      this.currentView = 'loading';
      await this.fetchRooms(hotel.id);
      this.currentView = 'rooms';
    },
    cancelHotelForm() {
      if (this.editingHotel) {
        this.currentView = 'details';
      } else {
        this.currentView = 'list';
      }
      this.editingHotel = null;
    },

    // --- 호텔 관리 ---
    openCreateForm() {
      this.editingHotel = null;
      this.hotelForm = { starRating: 5, country: "대한민국", imageUrls: [], amenityIds: [] };
      this.hotelImageFiles = [];
      this.currentView = 'hotelForm';
    },
    editHotel(hotel) {
      this.editingHotel = hotel;
      this.hotelForm = { ...hotel };
      if (!this.hotelForm.amenityIds) this.hotelForm.amenityIds = [];
      if (!this.hotelForm.imageUrls) this.hotelForm.imageUrls = [];
      this.hotelImageFiles = [];
      this.currentView = 'hotelForm';
    },
    handleHotelFileChange(event) {
      const files = Array.from(event.target.files);
      files.forEach(file => { file.preview = URL.createObjectURL(file); });
      this.hotelImageFiles.push(...files);
    },
    removeHotelImage(type, index) {
      if (type === 'url') this.hotelForm.imageUrls.splice(index, 1);
      else this.hotelImageFiles.splice(index, 1);
    },
    handleHotelSubmit() {
      const formData = new FormData();
      const hotelData = { ...this.hotelForm };
      delete hotelData.files; 
      formData.append('hotel', new Blob([JSON.stringify(hotelData)], { type: 'application/json' }));
      this.hotelImageFiles.forEach(file => { formData.append('files', file); });

      if (this.editingHotel) this.updateHotel(formData);
      else this.createHotel(formData);
    },
    async createHotel(formData) {
      const headers = this.getAuthHeaders();
      if (!headers) return;
      try {
        await axios.post("/api/hotels", formData, { headers });
        alert("호텔이 성공적으로 등록되었습니다.");
        this.goToList();
      } catch (err) {
        console.error("호텔 등록 실패:", err.response?.data || err.message);
        alert("호텔 등록에 실패했습니다.");
      }
    },
    async updateHotel(formData) {
      const headers = this.getAuthHeaders();
      if (!headers) return;

      // 1. 수정 API 호출 직전 데이터 확인
      console.log("1. [수정] API 호출 시작:", `/api/hotels/${this.editingHotel.id}`);
      console.log("   [수정] 전송할 데이터 (FormData):", formData);
      // FormData의 내용을 확인하려면 아래와 같이 각 key를 직접 로깅해야 합니다.
      for (let [key, value] of formData.entries()) {
        console.log(`   [수정] FormData ${key}:`, value);
      }

      try {
        await axios.post(`/api/hotels/${this.editingHotel.id}`, formData, { headers });
        // 2. 수정 성공 시
        console.log("2. [수정] API 호출 성공");
        alert("호텔 정보가 성공적으로 수정되었습니다.");
        this.goToList();
      } catch (err) {
        // 3. 수정 실패 시
        console.error("3. [수정] API 호출 실패:", err.response?.data || err.message);
        alert("호텔 수정에 실패했습니다.");
      }
    },
    async deleteHotel(id) {
      if (!confirm("정말로 이 호텔을 삭제하시겠습니까? 연관된 모든 객실 정보도 함께 삭제됩니다.")) return;
      const headers = this.getAuthHeaders();
      if (!headers) return;

      // 1. 삭제 API 호출 직전 ID 확인
      console.log("1. [삭제] API 호출 시작:", `/api/hotels/${id}`);

      try {
        await axios.delete(`/api/hotels/${id}`, { headers });
        // 2. 삭제 성공 시
        console.log("2. [삭제] API 호출 성공");
        alert("호텔이 삭제되었습니다.");
        this.goToList();
      } catch (err) {
        // 3. 삭제 실패 시
        console.error("3. [삭제] API 호출 실패:", err.response?.data || err.message);
        alert("호텔 삭제에 실패했습니다.");
      }
    },

    // --- 객실 관리 ---
    openRoomCreateForm() {
      this.editingRoom = null;
      this.roomForm = { roomType: '스탠다드룸', checkInTime: '15:00', checkOutTime: '11:00', imageUrls: [] };
      this.roomImageFiles = [];
      this.currentView = 'roomForm';
    },
    editRoom(room) {
      this.editingRoom = room;
      this.roomForm = { ...room };
      if(!this.roomForm.imageUrls) this.roomForm.imageUrls = [];
      this.roomImageFiles = [];
      this.currentView = 'roomForm';
    },
    handleRoomFileChange(event) {
      const files = Array.from(event.target.files);
      files.forEach(file => { file.preview = URL.createObjectURL(file); });
      this.roomImageFiles.push(...files);
    },
    removeRoomImage(type, index) {
      if (type === 'url') this.roomForm.imageUrls.splice(index, 1);
      else this.roomImageFiles.splice(index, 1);
    },
    handleRoomSubmit() {
      const formData = new FormData();
      const roomData = { ...this.roomForm };
      delete roomData.files;
      formData.append('room', new Blob([JSON.stringify(roomData)], { type: 'application/json' }));
      this.roomImageFiles.forEach(file => { formData.append('files', file); });

      if (this.editingRoom) this.updateRoom(formData);
      else this.createRoom(formData);
    },
    async createRoom(formData) {
      const headers = this.getAuthHeaders();
      if (!headers) return;
      console.log("1. [객실 생성] API 호출 시작:", `/api/hotels/${this.selectedHotel.id}/rooms`);
      for (let [key, value] of formData.entries()) {
        console.log(`   [객실 생성] FormData ${key}:`, value);
      }
      try {
        await axios.post(`/api/hotels/${this.selectedHotel.id}/rooms`, formData, { headers });
        console.log("2. [객실 생성] API 호출 성공");
        alert("객실이 등록되었습니다.");
        this.showRoomList(this.selectedHotel);
      } catch(err) {
        console.error("3. [객실 생성] API 호출 실패:", err.response?.data || err.message);
        alert("객실 등록에 실패했습니다.");
      }
    },

    async updateRoom(formData) {
      const headers = this.getAuthHeaders();
      if (!headers) return;
      console.log("1. [객실 수정] API 호출 시작:", `/api/hotels/rooms/${this.editingRoom.id}`);
      for (let [key, value] of formData.entries()) {
        console.log(`   [객실 수정] FormData ${key}:`, value);
      }
      try {
        await axios.post(`/api/hotels/rooms/${this.editingRoom.id}`, formData, { headers });
        console.log("2. [객실 수정] API 호출 성공");
        alert("객실 정보가 수정되었습니다.");
        this.showRoomList(this.selectedHotel);
      } catch(err) {
        console.error("3. [객실 수정] API 호출 실패:", err.response?.data || err.message);
        alert("객실 수정에 실패했습니다.");
      }
    },
    async deleteRoom(roomId) {
      if (!confirm("객실을 삭제하시겠습니까?")) return;
      const headers = this.getAuthHeaders();
      if (!headers) return;
      console.log("1. [객실 삭제] API 호출 시작:", `/api/hotels/rooms/${roomId}`);
      try {
        await axios.delete(`/api/hotels/rooms/${roomId}`, { headers });
        console.log("2. [객실 삭제] API 호출 성공");
        alert("객실이 삭제되었습니다.");
        this.fetchRooms(this.selectedHotel.id);
      } catch(err) {
        console.error("3. [객실 삭제] API 호출 실패:", err.response?.data || err.message);
        alert("객실 삭제에 실패했습니다.");
      }
    },
    
    checkLoginStatus() {
      const userInfo = localStorage.getItem('user');
      if (userInfo) {
        this.user = JSON.parse(userInfo);
        this.fetchHotels();
      } else {
        this.$router.push("/login");
      }
    }
  },
  mounted() {
    this.checkLoginStatus();
    this.fetchAmenities();
  },
};
</script>

<style scoped>
/* 전체 레이아웃 */
.owner-page{display:flex;height:100vh;width:100vw;margin:0;background:#f3f4f6}
.sidebar{width:220px;background:#111827;color:#fff;padding:20px 10px;box-sizing:border-box;position:fixed;top:0;left:0;bottom:0;overflow-y:auto;z-index:10}
.sidebar .logo{font-weight:700;font-size:20px;margin-bottom:25px;text-align:center}
.sidebar ul{list-style:none;padding:0;margin:0}
.sidebar li{padding:12px 15px;cursor:pointer;border-radius:6px;margin:4px 0;transition:background-color .2s}
.sidebar li.active,.sidebar li:hover{background:#374151}
.main-content{margin-left:220px;width:calc(100% - 220px);height:100vh;padding:0;box-sizing:border-box;overflow-y:auto}
.main-content>section{padding:30px}
.main-content h2{margin:0;font-size:24px;color:#111827}
.main-content h3{margin-top:20px;font-size:20px;color:#111827}
.header-actions{display:flex;justify-content:space-between;align-items:center;margin-bottom:25px; margin-top: 25px;}
.header-actions.secondary{margin-top:20px;margin-bottom:20px;padding-bottom:10px;border-bottom:1px solid #e5e7eb}
.user-actions{display:flex;align-items:center;gap:15px}
.user-name{font-weight:600;color:#374151}
.add-btn{padding:10px 16px;background:#3b82f6;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:700}
.add-btn:hover{background:#2563eb}
.logout-btn{padding:10px 16px;background:#6b7280;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:700}
.logout-btn:hover{background:#4b5563}
.back-btn{margin:0;padding:10px 16px;background:#6b7280;color:#fff;border:none;border-radius:6px;cursor:pointer}
.back-btn:hover{background:#4b5563}
.hotel-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:25px}
.hotel-card{aspect-ratio:1/1;background:#fff;border-radius:12px;box-shadow:0 4px 12px #00000014;cursor:pointer;overflow:hidden;display:flex;flex-direction:column;transition:transform .2s,box-shadow .2s}
.hotel-card:hover{transform:translateY(-5px);box-shadow:0 8px 20px #0000001f}
.hotel-card-image{width:100%;height:70%;object-fit:cover}
.hotel-card-info{padding:15px;flex-grow:1;display:flex;flex-direction:column}
.hotel-card-info strong{font-size:18px;margin-bottom:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.hotel-card-info span{font-size:14px;color:#6b7280;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.hotel-details-view{background:#fff;border-radius:12px;padding:30px;display:flex;gap:30px;border:1px solid #e5e7eb}
.details-image{width:400px;height:400px;object-fit:cover;border-radius:10px;flex-shrink:0}
.details-info{display:flex;flex-direction:column}
.details-info h2{margin-top:0}
.details-info p{font-size:16px;color:#374151;line-height:1.6}
.details-actions{margin-top:auto;padding-top:20px;display:flex;gap:15px}
.details-actions button{padding:12px 24px;font-size:16px;font-weight:700;border-radius:8px;border:none;cursor:pointer;transition:background-color .2s}
.btn-edit{background-color:#3b82f6;color:#fff}
.btn-edit:hover{background-color:#2563eb}
.btn-delete{background-color:#ef4444;color:#fff}
.btn-delete:hover{background-color:#dc2626}
.btn-rooms{background-color:#10b981;color:#fff}
.btn-rooms:hover{background-color:#059669}
.room-list{list-style:none;padding:0;margin:0}
.room-item{background:#fff;padding:15px;margin-bottom:10px;border-radius:8px;border:1px solid #e5e7eb;display:flex;align-items:center;gap:15px}
.room-image{width:120px;height:90px;border-radius:6px;object-fit:cover}
.room-info{flex:1;display:flex;flex-direction:column}
.room-info strong{font-size:16px}
.room-info span{font-size:14px;color:#6b7280}
.actions button{margin-left:5px;padding:6px 10px;border:none;border-radius:6px;cursor:pointer;font-size:14px}
.actions button:first-child{background:#3b82f6;color:#fff}
.actions button:last-child{background:#ef4444;color:#fff}
.form-container {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  /* max-width: 800px; <-- 이 줄 삭제 */
  /* margin: 0 auto;   <-- 이 줄 삭제 */
}
.form-wrapper {
  max-width: 800px; /* 폼의 최대 너비 설정 */
  margin: 0 auto;   /* 페이지 중앙에 위치하도록 설정 */
} 
.form-group{margin-bottom:20px}
.form-group label{display:block;font-size:14px;font-weight:600;margin-bottom:8px;color:#374151}
.form-group input,.form-group select,.form-group textarea{width:100%;padding:12px;border:1px solid #d1d5db;border-radius:6px;font-size:14px;box-sizing:border-box}
.form-group textarea{resize:vertical;min-height:120px}
.inline-group{display:flex;gap:10px}
.form-actions{margin-top:30px;display:flex;justify-content:flex-end;gap:12px}
.form-actions button{padding:12px 20px;border-radius:6px;border:none;cursor:pointer;font-size:14px;font-weight:700}
.btn-primary{background:#3b82f6;color:#fff}
.btn-primary:hover{background:#2563eb}
.btn-secondary{background:#e5e7eb;color:#374151}
.btn-secondary:hover{background:#d1d5db}
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}
.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.stat-card h4 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #6b7280;
}
.stat-card p {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}
.stat-card .comparison {
  font-size: 14px;
}
.comparison.positive { color: #10b981; }
.comparison.negative { color: #ef4444; }
.placeholder-chart {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #9ca3af;
    font-size: 18px;
    border: 2px dashed #e5e7eb;
}
.calendar-container {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.file-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background-color: white;
}
.image-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  margin-top: 15px;
}
.image-preview-item {
  position: relative;
  aspect-ratio: 4 / 3;
}
.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}
.btn-remove-img {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
.amenities-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  background-color: #f9fafb;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
}
.amenity-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.amenity-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}
.amenity-item label {
  font-size: 14px;
  color: #374151;
  margin-bottom: 0;
  cursor: pointer;
}
</style>