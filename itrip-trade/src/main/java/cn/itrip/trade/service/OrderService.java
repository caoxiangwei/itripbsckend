package cn.itrip.trade.service;

import cn.itrip.beans.pojo.ItripHotelOrder;

public interface OrderService {
    ItripHotelOrder getOrderNo(String orderNo) throws Exception;
    void paySuccess(String orderNo,int payType,String tradeNo) throws Exception;
    void payFailed(String orderNo,int payType,String tradeNo) throws Exception;
    boolean processed(String orderNo) throws Exception;
}
