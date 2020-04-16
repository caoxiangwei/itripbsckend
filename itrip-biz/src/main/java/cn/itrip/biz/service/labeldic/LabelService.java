package cn.itrip.biz.service.labeldic;

import cn.itrip.beans.vo.ItripLabelDicVO;

import java.util.List;

public interface LabelService {
    //查询酒店特色列表
    List<ItripLabelDicVO> getHotelFeatures(Long parentId) throws Exception;
    //查询酒店房间床型列表
    List<ItripLabelDicVO> queryHotelRoomBed(Long parentId) throws Exception;
    //查询出游类型列表
    List<ItripLabelDicVO> getTravelType(Long parentId) throws Exception;
}
