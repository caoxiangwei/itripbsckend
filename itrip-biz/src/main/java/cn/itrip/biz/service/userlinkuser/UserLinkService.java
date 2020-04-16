package cn.itrip.biz.service.userlinkuser;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.pojo.ItripUserLinkUser;
import cn.itrip.beans.vo.userinfo.ItripSearchUserLinkUserVO;

import java.util.List;
import java.util.Map;

public interface UserLinkService {
    //新增常用联系人接口
    Integer addUserLinkUser(ItripUserLinkUser linkUser) throws Exception;
    //删除常用联系人接口
    Integer delUserLinkUser(Long[] ids) throws Exception;
    //删除常用联系人接口中，查询所有未支付的订单所关联的所有常用联系人
    List<Long> getItripOrderLinkUserIdsByOrder() throws Exception;
    //查询常用联系人接口
    List<ItripUserLinkUser> queryUserLinkUser(Map map) throws Exception;
    List<ItripUserLinkUser> queryUserLinkUsers(ItripSearchUserLinkUserVO vo, ItripUser user) throws Exception;
    //修改常用联系人接口
    Integer modifyUserLinkUser(ItripUserLinkUser linkUser) throws Exception;
}
