package cn.itrip.biz.service.image;

import cn.itrip.beans.pojo.ItripComment;
import cn.itrip.beans.pojo.ItripImage;
import cn.itrip.beans.vo.ItripImageVO;

import java.util.List;
import java.util.Map;

public interface ImageService {
    //根据targetId查询酒店图片(type=0)
    List<ItripImage> getHotelImge(Map map) throws Exception;
    // 根据targetId查询酒店房型图片(type=1)
    List<ItripImageVO> getHotelRoomImge(Map map) throws Exception;
    //根据targetId查询评论照片(type=2)
    List<ItripImageVO> getCommentedImages(Map map) throws Exception;
}
