package cn.itrip.biz.service.userlinkuser;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.pojo.ItripUserLinkUser;
import cn.itrip.beans.vo.userinfo.ItripSearchUserLinkUserVO;
import cn.itrip.dao.orderlinkuser.ItripOrderLinkUserMapper;
import cn.itrip.dao.userlinkuser.ItripUserLinkUserMapper;
import com.sun.scenario.effect.impl.prism.PrImage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class UserLinkServiceImpl implements UserLinkService {
    @Resource
    private ItripUserLinkUserMapper linkUserMapper;
    @Resource
    private ItripOrderLinkUserMapper orderLinkUserMapper;

    //新增常用联系人接口
    @Override
    public Integer addUserLinkUser(ItripUserLinkUser linkUser) throws Exception {
        return linkUserMapper.insertItripUserLinkUser(linkUser);
    }

    //删除常用联系人接口
    @Override
    public Integer delUserLinkUser(Long[] ids) throws Exception {
        return linkUserMapper.deleteItripUserLinkUserByIds(ids);
    }
    //删除常用联系人接口中，查询所有未支付的订单所关联的所有常用联系人
    @Override
    public List<Long> getItripOrderLinkUserIdsByOrder() throws Exception {
        return orderLinkUserMapper.getItripOrderLinkUserIdsByOrder();
    }

    //查询常用联系人接口
    @Override
    public List<ItripUserLinkUser> queryUserLinkUser(Map map) throws Exception {
        return linkUserMapper.getItripUserLinkUserListByMap(map);
    }
    @Override
    public List<ItripUserLinkUser> queryUserLinkUsers(ItripSearchUserLinkUserVO vo, ItripUser user) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getId());
        map.put("linkUserName", vo.getLinkUserName());
        return linkUserMapper.getItripUserLinkUserListByMap(map);
    }
    //修改常用联系人接口
    @Override
    public Integer modifyUserLinkUser(ItripUserLinkUser linkUser) throws Exception {
        return linkUserMapper.updateItripUserLinkUser(linkUser);
    }
}
