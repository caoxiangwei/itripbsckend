package cn.itrip.biz.service.hotelorder;

import cn.itrip.beans.pojo.ItripHotel;
import cn.itrip.beans.pojo.ItripHotelOrder;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.order.ItripAddHotelOrderVO;
import cn.itrip.beans.vo.order.ItripListHotelOrderVO;
import cn.itrip.beans.vo.order.ItripModifyHotelOrderVO;
import cn.itrip.beans.vo.order.ItripPersonalOrderRoomVO;
import cn.itrip.common.Page;

import java.math.BigDecimal;
import java.util.Map;

public interface OrderService {
    //3.2.2.根据订单ID查看个人订单详情
    ItripHotelOrder getItripHotelOrderById(Long id)throws Exception;
    //3.2.3. 根据订单ID查看个人订单详情-房型相关信息
    ItripPersonalOrderRoomVO getPersonalOrderRoomInfo(Long orderId) throws Exception;
    //3.2.4. 根据个人订单列表，并分页显示
    Page<ItripListHotelOrderVO> queryOrderPageByMap(Map map)throws Exception;
    //根据Id查询Hotel,订单模块3.2.6. 生成订单前,获取预订信息
    ItripHotel getItripHotelById(Long id)throws Exception;
    //3.2.8. 生成订单
    Map insertOrder(ItripHotelOrder order) throws Exception;
    // 生成订单
    Map<String, Object> addHotelOrder(ItripAddHotelOrderVO vo, String token) throws Exception;
    //3.2.9. 根据订单ID获取订单信息
    ItripHotelOrder queryOrderById(Long id) throws Exception;
    //3.2.10.修改订单的支付方式和状态
    boolean modifyStatusAndPaytype(ItripModifyHotelOrderVO vo, ItripUser user) throws Exception;
    // 修改订单的支付方式和状态的支付方式
    boolean getPayType(ItripModifyHotelOrderVO vo) throws Exception;
    // 支付金额
    BigDecimal getOrderPayAmount(int count,Long id) throws Exception;
    boolean flushOrderStatus(Integer type) throws Exception;
}
