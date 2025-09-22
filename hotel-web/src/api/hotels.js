import http from './http';

export const hotelsApi = {
  getHotelDetail(id) {
    return http.get(`/hotels/${id}`);
  },
  // add more hotel-related endpoints here
};

export const updateRoom = async (roomId, roomData) => {
  // `api/` 접두사를 제거하여 올바른 경로로 요청합니다.
  const response = await http.put(`/hotels/rooms/${roomId}`, roomData);
  return response.data;
};

export default hotelsApi;
