package cn.itrip.biz.service.areadic;

import cn.itrip.beans.pojo.ItripAreaDic;

import java.util.List;
import java.util.Map;

public interface AreaDicService {
    //查询商圈
    List<ItripAreaDic> getAreaDicList(Map map) throws Exception;
    ////查询热门城市
    List<ItripAreaDic>getItripAreaDicListByMap(Map map)throws Exception;
}
