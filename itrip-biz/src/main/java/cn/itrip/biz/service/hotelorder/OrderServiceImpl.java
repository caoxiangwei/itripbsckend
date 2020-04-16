package cn.itrip.biz.service.hotelorder;

import cn.itrip.beans.pojo.*;
import cn.itrip.beans.vo.order.*;
import cn.itrip.common.*;
import cn.itrip.dao.hotel.ItripHotelMapper;
import cn.itrip.dao.hotelorder.ItripHotelOrderMapper;
import cn.itrip.dao.hotelroom.ItripHotelRoomMapper;
import cn.itrip.dao.hoteltempstore.ItripHotelTempStoreMapper;
import cn.itrip.dao.orderlinkuser.ItripOrderLinkUserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.net.www.protocol.http.HttpURLConnection;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ROUND_DOWN;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Resource
    private ItripHotelOrderMapper orderMapper;

    @Resource
    private ItripHotelTempStoreMapper storeMapper;

    @Resource
    private ItripHotelMapper itripHotelMapper;
    @Resource
    private ItripHotelRoomMapper roomMapper;
    @Resource
    private ItripOrderLinkUserMapper linkUserMapper;

    //3.2.2.根据订单ID查看个人订单详情
    @Override
    public ItripHotelOrder getItripHotelOrderById(Long id) throws Exception {
        return orderMapper.getItripHotelOrderById(id);
    }

    //3.2.3. 根据订单ID查看个人订单详情-房型相关信息
    @Override
    public ItripPersonalOrderRoomVO getPersonalOrderRoomInfo(Long orderId) throws Exception {
        return orderMapper.getItripHotelOrderRoomInfoById(orderId);
    }

    //3.2.4. 根据个人订单列表，并分页显示
    @Override
    public Page<ItripListHotelOrderVO> queryOrderPageByMap(Map map) throws Exception {
        Integer beginPos = ((Integer) map.get("pageNo")-1)*(Integer)map.get("pageSize");
        map.put("beginPos",beginPos);
        Integer total = orderMapper.getItripHotelOrderCountByMap(map);
        List list = orderMapper.getOrderListByMap(map);
        Integer pageNo = (Integer) map.get("pageNo");
        Integer pageSize = (Integer) map.get("pageSize");
        Page page = new Page(pageNo,pageSize,total);
        page.setRows(list);
        return page;
    }

    //根据Id查询Hotel,订单模块3.2.6. 生成订单前,获取预订信息
    @Override
    public ItripHotel getItripHotelById(Long id) throws Exception {
        return itripHotelMapper.getItripHotelById(id);
    }

    //3.2.8. 生成订单
    @Override
    public Map insertOrder(ItripHotelOrder order) throws Exception {
        Map map = new HashMap();
        map.put("id",order.getId());
        map.put("orderNo",order.getOrderNo());
        orderMapper.insertItripHotelOrder(order);
        return map;
    }

    //根据订单ID获取订单信息
    @Override
    public ItripHotelOrder queryOrderById(Long id) throws Exception {
        return orderMapper.getItripHotelOrderById(id);
    }

    //修改订单的支付方式和状态
    @Override
    public boolean modifyStatusAndPaytype(ItripModifyHotelOrderVO vo, ItripUser user) throws Exception {
        ItripHotelOrder order = new ItripHotelOrder();
        BeanUtils.copyProperties(vo,order);
        //修改userId（一般是和原来一样的）
        order.setUserId(user.getId());
        //更新入住人信息
        ItripOrderLinkUser linkUser = new ItripOrderLinkUser();
        linkUser.setOrderId(vo.getId());
        for(ItripOrderLinkUserVo linkUserVo : vo.getItripOrderLinkUserList()){
            Map map = new HashMap();
            map.put("orderId",vo.getId());
            map.put("linkUserId",linkUserVo.getLinkUserId());
            List<ItripOrderLinkUserVo> listByMap = linkUserMapper.getItripOrderLinkUserListByMap(map);
            linkUser.setLinkUserId(linkUserVo.getLinkUserId());
            linkUser.setLinkUserName(linkUserVo.getLinkUserName());
            if(EmptyUtils.isEmpty(listByMap)){
                linkUser.setCreatedBy(user.getId());
                linkUser.setCreationDate(new Date());
                Integer integer = linkUserMapper.insertItripOrderLinkUser(linkUser);
                if(integer < 1){
                    throw new Exception("新增入住人信息失败");
                }
            }else {
                linkUser.setModifiedBy(user.getId());
                linkUser.setModifyDate(new Date());
                linkUserMapper.updateItripOrderLinkUser(linkUser);
            }
        }
        //修改时间
        order.setModifiedBy(user.getId());
        //修改人
        order.setModifyDate(new Date());
        orderMapper.updateItripHotelOrder(order);
        return true;
    }
    // 修改订单的支付方式和状态的支付方式
    @Override
    public boolean getPayType(ItripModifyHotelOrderVO vo) throws Exception {
        ItripHotelRoom room = roomMapper.getItripHotelRoomById(vo.getRoomId());
        if(room.getPayType() == 3){return true;}
        if(room.getPayType() == 2 && vo.getPayType() ==3){return true;}
        if(room.getPayType() == 1 && (vo.getPayType() ==1 && vo.getPayType() ==3)){return true;}
        return false;
    }
    //生成订单的支付金额
    @Override
    public BigDecimal getOrderPayAmount(int count, Long roomId) throws Exception {
        BigDecimal roomPrice = roomMapper.getItripHotelRoomById(roomId).getRoomPrice();
        BigDecimal payAmount  = BigDecimalUtil.OperationASMD(count, roomPrice, BigDecimalUtil.BigDecimalOprations.multiply, 2, ROUND_DOWN);
        return payAmount ;
    }

    @Override
    public Map<String, Object> addHotelOrder(ItripAddHotelOrderVO vo, String token) throws Exception {
        ItripHotelOrder order = new ItripHotelOrder();
        BeanUtils.copyProperties(vo, order);
        //拆分token
        String[] tokens = token.split("-");
        //userId
        String userIdStr = tokens[2];
        Long userId = Long.parseLong(userIdStr);
        order.setUserId(userId);
        //生成订单号：机器码 +日期+（MD5）（商品IDs+毫秒数+1000000的随机数）
        StringBuilder md5String = new StringBuilder();
        md5String.append(order.getHotelId());
        md5String.append(order.getRoomId());
        md5String.append(System.currentTimeMillis());
        md5String.append(Math.random() * 1000000);
        String md5 = MD5.getMd5(md5String.toString(), 6);
        //生成订单编号
        StringBuilder orderNo = new StringBuilder();
        orderNo.append("D1000001");
        orderNo.append(DateUtil.format(new Date(), "yyyyMMddHHmmss"));
        orderNo.append(md5);
        order.setOrderNo(orderNo.toString());
        //预订天数
        List<Date> dateList = DateUtil.getBetweenDates(order.getCheckInDate(), order.getCheckOutDate());
        order.setBookingDays(dateList.size());
        //订单状态
        order.setOrderStatus(0);
        //订单金额
        BigDecimal roomPrice = roomMapper.getItripHotelRoomById(vo.getRoomId()).getRoomPrice();
        BigDecimal price = roomPrice.multiply(BigDecimal.valueOf(vo.getCount()*dateList.size()));
        order.setPayAmount(price);
        //入住人姓名
        StringBuilder linkUserName = new StringBuilder();
        List<ItripUserLinkUser> userList = vo.getLinkUser();
        for (int i = 0; i < userList.size(); i++){
            if (i == userList.size()-1){
                linkUserName.append(userList.get(i).getLinkUserName());
            }else {
                linkUserName.append(userList.get(i).getLinkUserName() + ",");
            }
        }
        order.setLinkUserName(linkUserName.toString());
        //订单生成时间
        order.setCreationDate(new Date());
        //订单创建者
        order.setCreatedBy(userId);
        //预订方式
        String bookType = tokens[0];
        if (bookType.endsWith("PC")){
            order.setBookType(0);
        }else if (bookType.endsWith("MOBILE")){
            order.setBookType(1);
        }else {
            order.setBookType(2);
        }
        //订单表插入数据
        orderMapper.insertItripHotelOrder(order);
        //订单联系人表插入数据
        ItripOrderLinkUser orderLinkUser = new ItripOrderLinkUser();
        orderLinkUser.setOrderId(order.getId());
        for (ItripUserLinkUser userLinkUser : userList){
            orderLinkUser.setLinkUserId(userLinkUser.getId());
            orderLinkUser.setLinkUserName(userLinkUser.getLinkUserName());
            orderLinkUser.setCreationDate(new Date());
            orderLinkUser.setCreatedBy(userId);
            linkUserMapper.insertItripOrderLinkUser(orderLinkUser);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", order.getId());
        map.put("orderNo", orderNo);
        return map;
    }

    @Override
    public boolean flushOrderStatus(Integer type) throws Exception {
        Integer i = 0;
        if(type == 1){
            i = orderMapper.flushCancelOrderStatus();
        }else if(type == 2){
            i = orderMapper.flushSuccessOrderStatus();
        }
        return i == 1;
    }
}
