package cn.itrip.biz.service.hoteltempstore;

import cn.itrip.beans.vo.store.StoreVO;

import java.util.List;
import java.util.Map;

public interface TempStoreService {
    //修改订房日期验证是否有房
    boolean validateRoomStore(Map map) throws Exception;
    //3.2.6. 生成订单前,获取预订信息
    List<StoreVO> queryRoomStroe(Map map)throws Exception;
}
