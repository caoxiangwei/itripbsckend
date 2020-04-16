package cn.itrip.biz.service.labeldic;

import cn.itrip.beans.vo.ItripLabelDicVO;
import cn.itrip.dao.labeldic.ItripLabelDicMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
public class LabelServiceImpl implements LabelService {
    @Resource
    private ItripLabelDicMapper dicMapper;

    //查询酒店房间床型列表
    @Override
    public List<ItripLabelDicVO> queryHotelRoomBed(Long parentId) throws Exception {
        return dicMapper.getItripLabelDicByParentId(parentId);
    }
    //查询出游类型列表
    @Override
    public List<ItripLabelDicVO> getTravelType(Long parentId) throws Exception {
        return dicMapper.getItripLabelDicByParentId(parentId);
    }
    //查询酒店特色列表
    @Override
    public List<ItripLabelDicVO> getHotelFeatures(Long parentId) throws Exception {
        return dicMapper.getItripLabelDicByParentId(parentId);
    }
}
