package cn.itrip.biz.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.vo.ItripImageVO;
import cn.itrip.beans.vo.ItripLabelDicVO;
import cn.itrip.beans.vo.hotelroom.ItripHotelRoomVO;
import cn.itrip.beans.vo.hotelroom.SearchHotelRoomVO;
import cn.itrip.biz.service.image.ImageService;
import cn.itrip.biz.service.labeldic.LabelService;
import cn.itrip.biz.service.room.RoomService;
import cn.itrip.common.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/api/hotelroom")
public class HotelRoomController {
    @Resource
    private ImageService imageService;
    @Resource
    private RoomService roomService;
    @Resource
    private LabelService labelService;

    @ApiOperation(value = "根据targetId查询酒店房型图片(type=1)", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "根据酒店房型ID查询酒店房型图片" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100301 : 获取酒店房型图片失败 </p>" +
            "<p>100302 : 酒店房型id不能为空</p>")
    @RequestMapping(value = "/getimg/{targetId}", method = RequestMethod.GET)
    @ResponseBody
    public Dto getImg(@PathVariable String targetId) {
        if (EmptyUtils.isEmpty(targetId)) {
            return DtoUtil.returnFail("酒店房型id不能为空", "100302 ");
        }
        Map map = new HashMap();
        map.put("type", "1");
        map.put("targetId", targetId);
        try {
            List<ItripImageVO> imageList = imageService.getHotelRoomImge(map);
            return DtoUtil.returnDataSuccess(imageList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取酒店房型图片失败", "100301");
        }
    }

    @ApiOperation(value = "查询酒店房间床型列表", httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "查询酒店床型列表" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100305 : 获取酒店房间床型失败</p>")
    @RequestMapping(value = "/queryhotelroombed", method = RequestMethod.GET)
    @ResponseBody
    public Dto queryHotelRoomBed(Long parentId) {
        try {
            List<ItripLabelDicVO> queryHotelRoomBed = labelService.queryHotelRoomBed(parentId);
            return DtoUtil.returnDataSuccess(queryHotelRoomBed);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取酒店房间床型失败", ErrorCode.BIZ_ROOMBED_LOAD_FAIL);
        }
    }

    @ApiOperation(value = "查询酒店房间列表", httpMethod = "POST",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class, notes = "查询酒店房间列表" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>" +
            "<p>100303 : 酒店id不能为空,酒店入住及退房时间不能为空,入住时间不能大于退房时间</p>" +
            "<p>100304 : 系统异常</p>")
    @RequestMapping(value = "/queryhotelroombyhotel",method = RequestMethod.GET)
    @ResponseBody
    public Dto queryHotelRoomByHotel(@RequestBody SearchHotelRoomVO vo){
        if(EmptyUtils.isEmpty(vo.getHotelId())){
            return DtoUtil.returnFail("酒店id不能为空",ErrorCode.BIZ_ROOMBYHOTEL_NOTNULL);
        }
        if(EmptyUtils.isEmpty(vo.getStartDate())|| EmptyUtils.isEmpty(vo.getEndDate())){
            return DtoUtil.returnFail("酒店入住及退房时间不能为空",ErrorCode.BIZ_ROOMBYHOTEL_NOTNULL);
        }
        if(vo.getStartDate().getTime() > vo.getEndDate().getTime()){
            return DtoUtil.returnFail("入住时间不能大于退房时间",ErrorCode.BIZ_ROOMBYHOTEL_NOTNULL);
        }
        Map map = new HashMap();
        List<Date> dates = DateUtil.getBetweenDates(vo.getStartDate(), vo.getEndDate());
        map.put("timesList", dates);
        map.put("hotelId",vo.getHotelId());
        map.put("isBook",vo.getIsBook());
        map.put("isHavingBreakfast",vo.getIsHavingBreakfast());
        map.put("isTimelyResponse",vo.getIsTimelyResponse());
        map.put("roomBedTypeId", vo.getRoomBedTypeId());
        map.put("isCancel", vo.getIsCancel());
        map.put("payType", vo.getPayType());
        try {
            List<ItripHotelRoomVO> roomVOList = roomService.queryHotelRoomByHotel(map);
            List<List<ItripHotelRoomVO>> roomList = new ArrayList<>();
            for(ItripHotelRoomVO roomVO : roomVOList){
                List<ItripHotelRoomVO> tempList = new ArrayList<ItripHotelRoomVO>();
                tempList.add(roomVO);
                roomList.add(tempList);
            }
            return DtoUtil.returnDataSuccess(roomList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常","100304");
        }
    }
}