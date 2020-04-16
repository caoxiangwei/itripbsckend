package cn.itrip.biz.service.areadic;

import cn.itrip.beans.pojo.ItripAreaDic;
import cn.itrip.beans.vo.ItripAreaDicVO;
import cn.itrip.dao.areadic.ItripAreaDicMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
@Service
public class AreaDicServiceImpl implements AreaDicService {
    @Resource
    private ItripAreaDicMapper areaDicMapper;
    //查询商圈
    @Override
    public List<ItripAreaDic> getAreaDicList(Map map) throws Exception {
        return areaDicMapper.getItripAreaDicListByMap(map);
    }
    ////查询热门城市
    @Override
    public List<ItripAreaDic> getItripAreaDicListByMap(Map map) throws Exception {
        return areaDicMapper.getItripAreaDicListByMap(map);
    }
}
