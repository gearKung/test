<template>
  <div class="search-form-wrapper">
    <div class="search-form-card">
      <h2 class="search-form-title">어디로 갈까요 ?</h2>

      <div class="search-form-grid">
        <!-- 목적지 -->
        <div class="search-form-item destination">
          <label class="search-form-label">목적지</label>
          <div class="input-field">
            <input
              type="text"
              v-model="destination"
              class="input-text"
              placeholder="목적지 또는 숙소명"
            />
          </div>
        </div>

        <!-- 체크인 + 체크아웃 (범위 선택) -->
        <div class="search-form-item date-range">
          <label class="search-form-label">체크인 / 체크아웃</label>
          <div class="input-field date-with-nights">
            <flat-pickr
              v-model="dateRange"
              :config="dateRangeConfig"
              placeholder="체크인 ~ 체크아웃 날짜 선택"
              class="input-text"
            />
            <span v-if="nights > 0" class="nights-inside">{{ nights }}박</span>
          </div>
        </div>

        <!-- 여행자 수 -->
        <div class="search-form-item">
          <label class="search-form-label">여행자 수</label>
          <div class="input-field traveler-dropdown">
            <button type="button" class="traveler-button" @click="toggleTravelerMenu">
              {{ travelerLabel }}
            </button>


            <div v-if="showTravelerMenu" class="traveler-menu">
              <div class="traveler-item">
                <span>성인</span>
                <div class="counter">
                  <button @click="decrease('adults')" :disabled="adults <= 1">-</button>
                  <span>{{ adults }}</span>
                  <button @click="increase('adults')">+</button>
                </div>
              </div>
              <div class="traveler-item">
                <span>어린이</span>
                <div class="counter">
                  <button @click="decrease('children')" :disabled="children <= 0">-</button>
                  <span>{{ children }}</span>
                  <button @click="increase('children')">+</button>
                </div>
              </div>
              <!-- 확인 버튼 -->
              <div class="traveler-actions">
                <button class="confirm-btn" @click="closeTravelerMenu">확인</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 검색 버튼 (grid 안에 포함) -->
        <div class="search-form-item search-button-container">
          <label class="search-form-label">&nbsp;</label>
          <button class="search-button" @click="goSearch">검색</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style src="@/assets/css/homepage/searchForm.css"></style>

<script>
import FlatPickr from "vue-flatpickr-component";
import { Korean } from "flatpickr/dist/l10n/ko.js"; // 한국어 locale

export default {
  name: "SearchForm",
  components: { FlatPickr },
  data() {
    return {
      destination: "",
      dateRange: [], // [체크인, 체크아웃]
      adults: 1,
      children: 0,
      showTravelerMenu: false,
      dateRangeConfig: {
      mode: "range",
      altInput: true,
      dateFormat: "Y-m-d",
      altFormat: "Y/m/d",  // 기본 포맷
      minDate: "today",
      locale: Korean,
      onClose: (selectedDates, dateStr, instance) => {
          if (selectedDates.length === 2) {
            const checkIn = selectedDates[0];
            const checkOut = selectedDates[1];
            const diff = (checkOut - checkIn) / (1000 * 60 * 60 * 24);

            if (diff > 0) {
              const checkInStr = instance.formatDate(checkIn, "Y/m/d");
              const checkOutStr = instance.formatDate(checkOut, "Y/m/d");
              instance.altInput.value = `${checkInStr} ~ ${checkOutStr} (${diff}박)`;
            }
          }
        }
      }
    };
  },
  computed: {
    travelerLabel() {
      let parts = [];
      if (this.adults > 0) parts.push(`성인 ${this.adults}명`);
      if (this.children > 0) parts.push(`어린이 ${this.children}명`);
      return parts.length > 0 ? parts.join(", ") : "여행자 선택";
    },
    nights() {
      if (this.dateRange.length === 2) {
        const checkIn = new Date(this.dateRange[0]);
        const checkOut = new Date(this.dateRange[1]);
        if (!isNaN(checkIn) && !isNaN(checkOut)) {
          const diff = (checkOut - checkIn) / (1000 * 60 * 60 * 24);
          return diff > 0 ? diff : 0;
        }
      }
      return 0;
    }
  },
  methods: {
    toggleTravelerMenu() {
      this.showTravelerMenu = !this.showTravelerMenu;
    },
    closeTravelerMenu() {
      this.showTravelerMenu = false;
    },
    increase(type) {
      this[type]++;
    },
    decrease(type) {
      if (this[type] > 0) this[type]--;
    },
    goSearch() {
      if (!this.destination) {
        alert("목적지를 입력해주세요.");
        return;
      }

      this.$router.push({
        path: "/search",
        query: {
          destination: this.destination,
          checkIn: this.checkIn || "",   // 선택 안 하면 빈 값
          checkOut: this.checkOut || "",
          adults: this.adults,
          children: this.children
        }
      });
    }
  }
};
</script>