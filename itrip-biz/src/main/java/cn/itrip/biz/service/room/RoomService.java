package cn.itrip.biz.service.room;

import cn.itrip.beans.pojo.ItripHotelRoom;
import cn.itrip.beans.vo.comment.ItripHotelDescVO;
import cn.itrip.beans.vo.hotelroom.ItripHotelRoomVO;
import cn.itrip.beans.vo.hotelroom.SearchHotelRoomVO;

import java.util.List;
import java.util.Map;

public interface RoomService {
    //查询酒店房间列表
    List<ItripHotelRoomVO> queryHotelRoomByHotel(Map map) throws Exception;
    //根据订单ID查看个人订单详情
    ItripHotelRoom getItripHotelRoomById(Long id)throws Exception;
}
