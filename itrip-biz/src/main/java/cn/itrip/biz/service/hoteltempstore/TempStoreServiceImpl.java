package cn.itrip.biz.service.hoteltempstore;

import cn.itrip.beans.pojo.ItripHotelTempStore;
import cn.itrip.beans.vo.store.StoreVO;
import cn.itrip.common.DateUtil;
import cn.itrip.dao.hoteltempstore.ItripHotelTempStoreMapper;
import cn.itrip.dao.productstore.ItripProductStoreMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@Transactional
public class TempStoreServiceImpl implements TempStoreService {
    @Resource
    private ItripHotelTempStoreMapper tempStoreMapper;
    @Resource
    private ItripProductStoreMapper productStoreMapper;

    //修改订房日期验证是否有房
    @Override
    public boolean validateRoomStore(Map map) throws Exception {
        List<Date> dateList = DateUtil.getBetweenDates((Date) map.get("startTime"), (Date) map.get("endTime"));
        for(Date date : dateList){
            map.put("time",date);
            ItripHotelTempStore hotelTempStore = tempStoreMapper.queryByTime(map);
            if(hotelTempStore == null){
                ItripHotelTempStore tempStore = new ItripHotelTempStore();
                tempStore.setHotelId((Long) map.get("hotelId"));
                tempStore.setRoomId((Long) map.get("roomId"));
                tempStore.setRecordDate(date);
                tempStore.setCreationDate(new Date());
                //去原始库存表查询库存量
                Map<String, Object> map1 = new HashMap<String, Object>();
                map1.put("productType", 1);
                map1.put("productId", map.get("roomId"));
                tempStore.setStore(productStoreMapper.getStore(map1));
                //tempStore.setStore(20);
                tempStoreMapper.insertItripHotelTempStore(tempStore);
            }
        }
        List<StoreVO> storeVOList = tempStoreMapper.queryRoomStore(map);
        for (StoreVO vo : storeVOList) {
            if (vo.getStore() < (Integer) map.get("count")) {
                return false;
            }
        }
        return true;
    }

    //3.2.6. 生成订单前,获取预订信息
    @Override
    public List<StoreVO> queryRoomStroe(Map map) throws Exception {
        return tempStoreMapper.queryRoomStore(map);
    }
}
