package cn.itrip.biz.service.room;

import cn.itrip.beans.pojo.ItripHotelRoom;
import cn.itrip.beans.vo.comment.ItripHotelDescVO;
import cn.itrip.beans.vo.hotelroom.ItripHotelRoomVO;
import cn.itrip.beans.vo.hotelroom.SearchHotelRoomVO;
import cn.itrip.dao.comment.ItripCommentMapper;
import cn.itrip.dao.hotel.ItripHotelMapper;
import cn.itrip.dao.hotelroom.ItripHotelRoomMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class RoomServiceImpl implements RoomService {
    @Resource
    private ItripHotelRoomMapper roomMapper;
    @Override
    public List<ItripHotelRoomVO> queryHotelRoomByHotel(Map map) throws Exception {
        return roomMapper.getItripHotelRoomListByMap(map);
    }

    //根据订单ID查看个人订单详情
    @Override
    public ItripHotelRoom getItripHotelRoomById(Long id) throws Exception {
        return roomMapper.getItripHotelRoomById(id);
    }
}
