package cn.itrip.biz.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.*;
import cn.itrip.beans.vo.order.*;
import cn.itrip.beans.vo.store.StoreVO;
import cn.itrip.biz.service.hotelorder.OrderService;
import cn.itrip.biz.service.hoteltempstore.TempStoreService;
import cn.itrip.biz.service.room.RoomService;
import cn.itrip.common.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/hotelorder")
public class HotelOrderController {
    @Resource
    private ValidationToken validationToken;
    @Resource
    private OrderService orderService;
    @Resource
    private RoomService roomService;
    @Resource
    private TempStoreService tempStoreService;
    @Resource
    private SystemConfig systemConfig;

    //修改订房日期验证是否有房 testOK
    @RequestMapping(value = "/validateroomstore",method = RequestMethod.POST)
    @ResponseBody
    public Dto validateRoomStore(@RequestBody ValidateRoomStoreVO vo,HttpServletRequest request){
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(currentUser == null){
            return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
        }
        if(EmptyUtils.isEmpty(vo.getHotelId())){
            return DtoUtil.returnFail("hotelId不能为空", "100515");
        }
        if(EmptyUtils.isEmpty(vo.getRoomId())){
            return DtoUtil.returnFail("roomId不能为空", "100516");
        }
        Map map = new HashMap();
        map.put("hotelId",vo.getHotelId());
        map.put("roomId",vo.getRoomId());
        map.put("startTime",vo.getCheckInDate());
        map.put("endTime",vo.getCheckOutDate());
        map.put("count",vo.getCount());
        try {
            boolean flag = tempStoreService.validateRoomStore(map);
            map.clear();
            map.put("flag",flag);
            return DtoUtil.returnDataSuccess(map);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100517");
        }
    }

    //根据订单ID查看个人订单详情 testOK
    @RequestMapping(value = "/getpersonalorderinfo/{orderId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto getPersonalOrderInfo(@PathVariable String orderId,HttpServletRequest request){
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(currentUser == null){
            return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
        }
        if (EmptyUtils.isEmpty(orderId)){
            return DtoUtil.returnFail("请传递参数：orderId","100525");
        }
        try {
            ItripHotelOrder itripHotelOrder = orderService.getItripHotelOrderById(Long.parseLong(orderId));
            if(EmptyUtils.isEmpty(itripHotelOrder)) {
                return DtoUtil.returnFail("没有相关订单信息", "100526");
            }
                ItripPersonalHotelOrderVO vo = new ItripPersonalHotelOrderVO();
                BeanUtils.copyProperties(itripHotelOrder,vo);
                //订单状态（0：待支付 1:已取消 2:支付成功 3:已消费 4:已点评）
                //{"1":"订单提交","2":"订单支付","3":"支付成功","4":"入住","5":"订单点评","6":"订单完成"}
                //订单状态(1:已取消)的流程：
                //{"1":"订单提交","2":"订单支付","3":"订单取消"}
                Object processOk = JSON.parse("{\"1\":\"订单提交\",\"2\":\"订单支付\",\"3\":\"支付成功\",\"4\":\"入住\",\"5\":\"订单点评\",\"6\":\"订单完成\"}");
                Object cancel = JSON.parse("{\"1\":\"订单提交\",\"2\":\"订单支付\",\"3\":\"订单取消\"}");
                if(itripHotelOrder.getOrderStatus() == 0){
                    vo.setOrderProcess(processOk);
                    vo.setProcessNode("2"); //订单支付
                }else if(itripHotelOrder.getOrderStatus() == 1){
                    vo.setOrderProcess(cancel);
                    vo.setProcessNode("3");
                }else if(itripHotelOrder.getOrderStatus() == 2){
                    vo.setOrderProcess(processOk);
                    vo.setProcessNode("4"); //支付成功（入住）
                }else if(itripHotelOrder.getOrderStatus() == 3){
                    vo.setOrderProcess(processOk);
                    vo.setProcessNode("5"); //订单点评
                }else if(itripHotelOrder.getOrderStatus() == 4){
                    vo.setOrderProcess(processOk);
                    vo.setProcessNode("6"); //订单完成
                }else {
                    vo.setOrderProcess(null);
                    vo.setProcessNode(null);
                }
                vo.setRoomPayType(itripHotelOrder.getPayType());
                return DtoUtil.returnSuccess("获取个人订单信息成功",vo);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取个人订单信息错误","100527");
        }
    }

    /*//根据订单ID查看个人订单详情 把订单状态放入properties文件中
    @RequestMapping(value = "/getpersonalorderinfo/{orderId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto getPersonalOrderInfo(@PathVariable String orderId,HttpServletRequest request){
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(currentUser == null){
            return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
        }
        if (EmptyUtils.isEmpty(orderId)){
            return DtoUtil.returnFail("请传递参数：orderId","100525");
        }
        try {
            ItripHotelOrder itripHotelOrder = orderService.getItripHotelOrderById(Long.parseLong(orderId));
            if(EmptyUtils.isNotEmpty(itripHotelOrder)){
                ItripPersonalHotelOrderVO vo = new ItripPersonalHotelOrderVO();
                BeanUtils.copyProperties(itripHotelOrder,vo);
                //订单状态（0：待支付 1:已取消 2:支付成功 3:已消费 4:已点评）
                //{"1":"订单提交","2":"订单支付","3":"支付成功","4":"入住","5":"订单点评","6":"订单完成"}
                //sysConfig.orderProcessOK = {"1":"订单提交","2":"订单支付","3":"支付成功","4":"入住","5":"订单点评","6":"订单完成"}
                //订单状态(1:已取消)的流程：
                //{"1":"订单提交","2":"订单支付","3":"订单取消"}
                //sysConfig.orderProcessCancel = {"1":"订单提交","2":"订单支付","3":"订单取消"}
                Object processOk = JSON.parse("{\"1\":\"订单提交\",\"2\":\"订单支付\",\"3\":\"支付成功\",\"4\":\"入住\",\"5\":\"订单点评\",\"6\":\"订单完成\"}");
                Object cancel = JSON.parse("{\"1\":\"订单提交\",\"2\":\"订单支付\",\"3\":\"订单取消\"}");

                if(itripHotelOrder.getOrderStatus() == 0){
                    vo.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
                    vo.setProcessNode("2"); //订单支付
                }else if(itripHotelOrder.getOrderStatus() == 1){
                    vo.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessCancel()));
                    vo.setProcessNode("3");
                }else if(itripHotelOrder.getOrderStatus() == 2){
                    vo.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
                    vo.setProcessNode("4");
                }else if(itripHotelOrder.getOrderStatus() == 3){
                    vo.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
                    vo.setProcessNode("5");
                }else if(itripHotelOrder.getOrderStatus() == 4){
                    vo.setOrderProcess(JSONArray.parse(systemConfig.getOrderProcessOK()));
                    vo.setProcessNode("6");
                }else {
                    vo.setOrderProcess(null);
                    vo.setProcessNode(null);
                }
                vo.setRoomPayType(itripHotelOrder.getPayType());
                return DtoUtil.returnSuccess("获取个人订单信息成功",vo);
            }else {
                return DtoUtil.returnFail("没有相关订单信息", "100526");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取个人订单信息错误","100527");
        }
    }*/

    //根据订单ID查看个人订单详情-房型相关信息 testOK
    @RequestMapping(value = "/getpersonalorderroominfo/{orderId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto getPersonalOrderRoomInfo(@PathVariable String orderId,HttpServletRequest request){
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(currentUser == null){
            return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
        }
        if(EmptyUtils.isEmpty(orderId)){
            return DtoUtil.returnFail("请传递参数：orderId","100529");
        }
        try {
            ItripPersonalOrderRoomVO roomVO = orderService.getPersonalOrderRoomInfo(Long.parseLong(orderId));
            if(EmptyUtils.isEmpty(roomVO)){
                return DtoUtil.returnFail("没有相关订单房型信息", "100530");
            }else {
                return DtoUtil.returnSuccess("获取个人订单房型信息成功", roomVO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取个人订单房型信息错误","100531");
        }
    }

    //根据个人订单列表，并分页显示  testOK
    @RequestMapping(value = "/getpersonalorderlist",method = RequestMethod.POST)
    @ResponseBody
    public Dto getPersonalOrderList(@RequestBody ItripSearchOrderVO vo,HttpServletRequest request){
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(currentUser == null){
            return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
        }
        if(EmptyUtils.isEmpty(vo.getOrderStatus())){
            return DtoUtil.returnFail("请传递参数：orderStatus", "100502");
        }
        if(EmptyUtils.isEmpty(vo.getOrderType())){
            return DtoUtil.returnFail("请传递参数：orderType", "100501");
        }
        Map map = new HashMap();
        map.put("userId",currentUser.getId());
        map.put("orderNo",vo.getOrderNo());
        map.put("linkUserName",vo.getLinkUserName());
        map.put("startDate",vo.getStartDate());
        map.put("endDate",vo.getEndDate());
        map.put("orderStatus",vo.getOrderStatus() == -1 ? null : vo.getOrderStatus());
        map.put("orderType",vo.getOrderType()== -1 ? null : vo.getOrderType());
        Integer pageNo = vo.getPageNo() == null ? 1 : vo.getPageNo();
        map.put("pageNo",pageNo);
        Integer pageSize = vo.getPageSize() == null ? 5 : vo.getPageSize();
        map.put("pageSize",pageSize);
        try {
            Page<ItripListHotelOrderVO> page = orderService.queryOrderPageByMap(map);
            return DtoUtil.returnDataSuccess(page);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取个人订单列表错误", "100503");
        }
    }

    //生成订单前,获取预订信息
    @RequestMapping(value = "/getpreorderinfo",method = RequestMethod.POST)
    @ResponseBody
    public Dto getPreOrderInfo(@RequestBody ValidateRoomStoreVO vo,HttpServletRequest request){
        String userAgent = request.getHeader("user-agent");
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        try {
            if(!validationToken.validateToken(userAgent,tokenString)){
                return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
            }
            if(EmptyUtils.isEmpty(vo.getHotelId())){
                return DtoUtil.returnFail("hotelId不能为空","100510");
            }
            if(EmptyUtils.isEmpty(vo.getRoomId())){
                return DtoUtil.returnFail("roomId不能为空","100511");
            }
            ItripHotel itripHotel = orderService.getItripHotelById(vo.getHotelId());
            ItripHotelRoom hotelRoom = roomService.getItripHotelRoomById(vo.getRoomId());
            Map map = new HashMap();
            map.put("startTime",vo.getCheckInDate());
            map.put("endTime",vo.getCheckOutDate());
            map.put("hotelId",vo.getHotelId());
            map.put("roomId",vo.getRoomId());
            RoomStoreVO roomStoreVO = new RoomStoreVO();
            PreAddOrderVO preAddOrderVO = new PreAddOrderVO();
            preAddOrderVO.setHotelId(vo.getHotelId());
            preAddOrderVO.setRoomId(vo.getRoomId());
            preAddOrderVO.setCheckInDate(vo.getCheckInDate());
            preAddOrderVO.setCheckOutDate(vo.getCheckOutDate());
            preAddOrderVO.setHotelName(itripHotel.getHotelName());
            preAddOrderVO.setPrice(hotelRoom.getRoomPrice());
            preAddOrderVO.setCount(vo.getCount());
            List<StoreVO> storeVOList = tempStoreService.queryRoomStroe(map);
            if(EmptyUtils.isNotEmpty(storeVOList)){
                roomStoreVO.setStore(storeVOList.get(0).getStore());
            }else {
                return DtoUtil.returnFail("暂时无房", "100512");
            }
            return DtoUtil.returnSuccess("获取成功", roomStoreVO);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100513");
        }
    }

    //支付成功后查询订单信息 testOK
    @RequestMapping(value = "/querysuccessorderinfo/{id}",method = RequestMethod.POST)
    @ResponseBody
    public Dto querySuccessOrderInfo(@PathVariable Long id,HttpServletRequest request){
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(currentUser == null){
            return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
        }
        if(EmptyUtils.isEmpty(id)){
            return DtoUtil.returnFail("id不能为空","100519");
        }
        try {
            ItripHotelOrder order = orderService.getItripHotelOrderById(id);
            if (EmptyUtils.isEmpty(order)) {
                return DtoUtil.returnFail("没有查询到相应订单", "100519");
            }
            ItripHotelRoom room = roomService.getItripHotelRoomById(id);
            Map map = new HashMap();
            map.put("id",order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("payType", order.getPayType());
            map.put("payAmount", order.getPayAmount());
            map.put("hotelName", order.getHotelName());
            map.put("roomTitle", room.getRoomTitle());
            return DtoUtil.returnDataSuccess(map);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取数据失败","100520");
        }
    }

    /*//3.2.8
    @RequestMapping(value = "/addhotelorder", method = RequestMethod.POST)
    @ResponseBody
    public Dto<Map<String, Object>> addHotelOrder(HttpServletRequest request,@RequestBody ItripAddHotelOrderVO vo){
        String token = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(token);
        if (EmptyUtils.isEmpty(vo)){
            return DtoUtil.returnFail("不能提交空，请填写订单信息", "100506");
        }
        try {
            if(currentUser == null){
                return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
            }
            Map map = new HashMap();
            map.put("hotelId",vo.getHotelId());
            map.put("roomId",vo.getRoomId());
            map.put("startTime",vo.getCheckInDate());
            map.put("endTime",vo.getCheckOutDate());
            map.put("count",vo.getCount());
            if (!tempStoreService.validateRoomStore(map)){
                return DtoUtil.returnFail("库存不足", "100507");
            }
            Map<String, Object> map1 = orderService.addHotelOrder(vo, token);
            return DtoUtil.returnSuccess("成功生成订单" ,map1);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("生成订单失败", "100505");
        }
    }*/
    //生成订单
    @RequestMapping(value = "/addhotelorder",method = RequestMethod.POST)
    @ResponseBody
    public Dto addHotelOrder(@RequestBody ItripAddHotelOrderVO vo,HttpServletRequest request){
        String token = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(token);
        if(currentUser == null){
            return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
        }
        if(EmptyUtils.isEmpty(vo)){
            return DtoUtil.returnFail("不能提交空，请填写订单信息", "100506");
        }
        Map map = new HashMap();
        map.put("hotelId",vo.getHotelId());
        map.put("roomId",vo.getRoomId());
        map.put("startTime",vo.getCheckInDate());
        map.put("endTime",vo.getCheckOutDate());
        map.put("count",vo.getCount());
        List<ItripUserLinkUser> linkUserList = vo.getLinkUser();
        try {
            boolean flag = tempStoreService.validateRoomStore(map);
            if(!flag) {
                return DtoUtil.returnFail("库存不足", "100507");
            }
            //计算订单的预定天数
            Integer days = DateUtil.getBetweenDates(vo.getCheckInDate(),vo.getCheckOutDate()).size() - 1;
            if(days <= 0){
                return DtoUtil.returnFail("退房日期必须大于入住日期", "100505");
            }
            ItripHotelOrder hotelOrder = new ItripHotelOrder();
            BeanUtils.copyProperties(vo,hotelOrder);
            hotelOrder.setUserId(currentUser.getId());
            hotelOrder.setCreatedBy(currentUser.getId());
            hotelOrder.setCreationDate(new Date());
            // 入住人名称，多个名称之间用逗号隔开拼接字符串
            StringBuffer linkUserName = new StringBuffer();
            int size = linkUserList.size();
            for(int i = 0;i < size; i++){
                if(i != size - 1){
                    linkUserName.append(linkUserList.get(i).getLinkUserName() + ",");
                }else {
                    linkUserName.append(linkUserList.get(i).getLinkUserName());
                }
            }
            hotelOrder.setLinkUserName(linkUserName.toString());
            hotelOrder.setBookingDays(days);
            //判断是移动端还是PC端
            if(token.startsWith("token:PC")){
                hotelOrder.setBookType(0);
            }else if(token.startsWith("token:MOBILE")){
                hotelOrder.setBookType(1);
            }else {
                hotelOrder.setBookType(2);
            }
            //支付之前生成的订单的初始状态为未支付
            hotelOrder.setOrderStatus(0);
            try {
                //生成订单号：机器码 +日期+（MD5）（商品IDs+毫秒数+1000000的随机数）
                StringBuilder md5String = new StringBuilder();
                md5String.append(hotelOrder.getHotelId());
                md5String.append(hotelOrder.getRoomId());
                md5String.append(System.currentTimeMillis());
                md5String.append(Math.random() * 1000000);
                String md5 = MD5.getMd5(md5String.toString(), 6);
                //生成订单编号
                StringBuilder orderNo = new StringBuilder();
                orderNo.append("D1000001");
                orderNo.append(DateUtil.format(new Date(), "yyyyMMddHHmmss"));
                orderNo.append(md5);
                hotelOrder.setOrderNo(orderNo.toString());
                //计算订单的总金额
                BigDecimal orderPayAmount = orderService.getOrderPayAmount(days * hotelOrder.getCount(), hotelOrder.getRoomId());
                hotelOrder.setPayAmount(orderPayAmount);
                Map order = orderService.insertOrder(hotelOrder);
                return DtoUtil.returnSuccess("生成订单成功",order);
            } catch (ParseException e) {
                e.printStackTrace();
                return DtoUtil.returnFail("生成订单失败", "100505");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "100508");
        }
    }

    //根据订单ID获取订单信息 testOK
    @RequestMapping(value = "/queryOrderById/{orderId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto queryOrderById(@PathVariable Long orderId){
        try {
            ItripHotelOrder order = orderService.queryOrderById(orderId);
            if(EmptyUtils.isEmpty(order)){
                return DtoUtil.returnFail("没有查询到相应订单","100533");
            }else {
                return DtoUtil.returnSuccess("获取订单成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常","100534");
        }
    }

    //修改订单的支付方式和状态 testOK
    @RequestMapping(value = "/updateorderstatusandpaytype",method = RequestMethod.POST)
    @ResponseBody
    public Dto updateOrderStatusAndPaytype(@RequestBody ItripModifyHotelOrderVO vo, HttpServletRequest request){
        String userAgent = request.getHeader("user-agent");
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(EmptyUtils.isEmpty(vo)){
            return DtoUtil.returnFail("不能提交空，请填写订单信息","100523");
        }
        try {
            if(!validationToken.validateToken(userAgent,tokenString)){
                return DtoUtil.returnFail("token失效，请重新登录", ErrorCode.BIZ_TOKENFAILUER);
            }
            if(!orderService.getPayType(vo)){
                if(vo.getPayType() == 3){
                    return DtoUtil.returnFail("对不起，此房间不支持线下支付","100521");
                }else {
                    return DtoUtil.returnFail("对不起，此房间不支持线上支付","100521");
                }
            }
            orderService.modifyStatusAndPaytype(vo,currentUser);
            return DtoUtil.returnSuccess("修改订单成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("修改订单失败", "100522");
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void flushCancleOrder(){
        try {
            boolean flag =  orderService.flushOrderStatus(1);
            System.out.println(flag ? "刷新订单成功" : "刷单失败");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Scheduled(cron = "0 0/30 * * * ?")
    public void flushOrder(){
        try {
            boolean flag =  orderService.flushOrderStatus(2);
            System.out.println(flag ? "刷新订单成功" : "刷单失败");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
