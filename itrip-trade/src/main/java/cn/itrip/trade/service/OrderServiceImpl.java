package cn.itrip.trade.service;

import cn.itrip.beans.pojo.ItripHotelOrder;
import cn.itrip.common.EmptyUtils;
import cn.itrip.dao.hotelorder.ItripHotelOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Resource
    private ItripHotelOrderMapper orderMapper;
    @Override
    public ItripHotelOrder getOrderNo(String orderNo) throws Exception {
        Map map = new HashMap();
        map.put("orderNo",orderNo);
        List<ItripHotelOrder> orderList = orderMapper.getItripHotelOrderListByMap(map);
        if(orderList.size() == 1 || orderList.size() > 1){
            return orderList.get(0);
        }
        return null;
    }

    @Override
    public void paySuccess(String orderNo, int payType, String tradeNo) throws Exception {
        ItripHotelOrder hotelOrder = getOrderNo(orderNo);
        hotelOrder.setOrderType(2);
        hotelOrder.setPayType(payType);
        hotelOrder.setTradeNo(tradeNo);
        orderMapper.updateItripHotelOrder(hotelOrder);
    }

    @Override
    public void payFailed(String orderNo, int payType, String tradeNo) throws Exception {
        ItripHotelOrder order = getOrderNo(orderNo);
        order.setOrderType(1);
        order.setPayType(payType);
        order.setTradeNo(tradeNo);
        orderMapper.updateItripHotelOrder(order);
    }

    @Override
    public boolean processed(String orderNo) throws Exception {
        ItripHotelOrder hotelOrder = getOrderNo(orderNo);
        return hotelOrder.getOrderType().equals(2) && EmptyUtils.isNotEmpty(hotelOrder.getTradeNo());
    }
}
