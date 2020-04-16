package cn.itrip.biz.service.image;

import cn.itrip.beans.pojo.ItripImage;
import cn.itrip.beans.vo.ItripImageVO;
import cn.itrip.dao.image.ItripImageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
@Service
public class ImageServiceImpl implements ImageService {
    @Resource
    private ItripImageMapper imageMapper;

    //根据targetId查询酒店图片(type=0)
    @Override
    public List<ItripImage> getHotelImge(Map map) throws Exception {
        return imageMapper.getItripImageListByMap(map);
    }

    @Override
    public List<ItripImageVO> getHotelRoomImge(Map map) throws Exception {
        return imageMapper.getItripImageListByMap(map);
    }

    @Override
    public List<ItripImageVO> getCommentedImages(Map map) throws Exception {
        return imageMapper.getItripImageListByMap(map);
    }

}
