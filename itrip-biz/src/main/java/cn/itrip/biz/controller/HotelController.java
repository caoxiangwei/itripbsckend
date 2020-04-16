package cn.itrip.biz.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.*;
import cn.itrip.beans.vo.ItripAreaDicVO;
import cn.itrip.beans.vo.ItripLabelDicVO;
import cn.itrip.beans.vo.hotel.*;
import cn.itrip.biz.service.areadic.AreaDicService;
import cn.itrip.biz.service.hotel.HotelService;
import cn.itrip.biz.service.image.ImageService;
import cn.itrip.biz.service.labeldic.LabelService;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.ErrorCode;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/hotel")
public class HotelController {
    @Resource
    private AreaDicService areaDicService;
    @Resource
    private ImageService imageService;
    @Resource
    private HotelService hotelService;
    @Resource
    private LabelService labelService;

    //查询商圈
    @RequestMapping(value = "/querytradearea/{cityId}", method = RequestMethod.GET)
    @ResponseBody
    public Dto queryTradeArea(@PathVariable Integer cityId) {
        if (EmptyUtils.isEmpty(cityId)) {
            return DtoUtil.returnFail("cityId不能为空", ErrorCode.BIZ_CITYID_NOTNULL);
        }
        Map map = new HashMap();
        map.put("isTradingArea", 1);
        map.put("parent", cityId);
        try {
            List<ItripAreaDic> areaDicList = areaDicService.getAreaDicList(map);
            List<ItripAreaDicVO> voList = new ArrayList<>();
            for (ItripAreaDic dic : areaDicList) {
                ItripAreaDicVO vo = new ItripAreaDicVO();
                BeanUtils.copyProperties(dic, vo);
                voList.add(vo);
            }
            return DtoUtil.returnDataSuccess(voList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.BIZ_UNKNOWN);
        }
    }

    //根据targetId查询酒店图片(type=0)
    @RequestMapping(value = "/getimg/{targetId}", method = RequestMethod.GET)
    @ResponseBody
    public Dto getImgByTargetId(@PathVariable String targetId) {
        if (EmptyUtils.isEmpty(targetId)) {
            return DtoUtil.returnFail("酒店id不能为空", ErrorCode.BIZ_TARGETID_NOTNULL);
        }
        Map map = new HashMap();
        map.put("type", "0");
        map.put("targetId", targetId);
        try {
            List<ItripImage> imgList = imageService.getHotelImge(map);
            return DtoUtil.returnDataSuccess(imgList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), "100212 ");
        }
    }


    // 根据酒店id查询酒店特色、商圈、酒店名称
    @RequestMapping(value = "/getvideodesc/{hotelId}", method = RequestMethod.GET)
    @ResponseBody
    public Dto<Object> getVideoDescByHotelId(@PathVariable String hotelId) {
        if (EmptyUtils.isEmpty(hotelId)) {
            return DtoUtil.returnFail("酒店id不能为空","100215");
        }
        try {
            HotelVideoDescVO hotelVideoDescVO = hotelService.getVideoDesc(Long.parseLong(hotelId));
            return DtoUtil.returnDataSuccess(hotelVideoDescVO);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),"100214");
        }
    }

    //根据酒店id查询酒店设施
    @RequestMapping(value = "/queryhotelfacilities/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Dto queryHotelFacilities(@PathVariable Long id) {
        if (EmptyUtils.isEmpty(id)) {
            return DtoUtil.returnFail("酒店id不能为空", ErrorCode.BIZ_HOTELID_NOTNULL);
        }
        try {
            ItripSearchFacilitiesHotelVO vo = hotelService.getHotelFacilities(id);
            return DtoUtil.returnDataSuccess(vo.getFacilities());
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.BIZ_HOTEL_UNKNOWN);
        }
    }

    //查询酒店特色列表
    @RequestMapping(value = "/queryhotelfeature", method = RequestMethod.GET)
    @ResponseBody
    public Dto queryHotelFeature(Long parentId) {
        try {
            List<ItripLabelDicVO> hotelFeatures = labelService.getHotelFeatures(parentId);
            return DtoUtil.returnDataSuccess(hotelFeatures);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.BIZ_HOTELFEATURE_UNKNOWN);
        }
    }

    //查询热门城市
    @RequestMapping(value = "/queryhotcity/{type}", method = RequestMethod.GET)
    @ResponseBody
    public Dto<ItripAreaDicVO> queryHotCity(@PathVariable Integer type) {
        Map map = new HashMap();
        map.put("isHot", 1);
        map.put("isChina", type);
        try {
            List<ItripAreaDic> dicListByMap = areaDicService.getItripAreaDicListByMap(map);
            List<ItripAreaDicVO> dicVOList = new ArrayList<>();
            for(ItripAreaDic dic : dicListByMap){
                ItripAreaDicVO vo = new ItripAreaDicVO();
                BeanUtils.copyProperties(dic,vo);
                dicVOList.add(vo);
            }
            return DtoUtil.returnDataSuccess(dicVOList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("系统异常", "10202");
        }
    }

    //根据酒店id查询酒店特色和介绍
    @RequestMapping(value = "/queryhoteldetails/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Dto queryHotelDetails(@PathVariable Long id){
        if(EmptyUtils.isEmpty(id)){
            return DtoUtil.returnFail("酒店id不能为空","10210");
        }
        try {
            List<ItripLabelDic> itripLabelDics = hotelService.queryHotelDetails(id);
            List<ItripSearchDetailsHotelVO> voList = new ArrayList<>();
            for(ItripLabelDic dic : itripLabelDics){
                ItripSearchDetailsHotelVO vo = new ItripSearchDetailsHotelVO();
                BeanUtils.copyProperties(dic,vo);
                voList.add(vo);
            }
            return DtoUtil.returnDataSuccess(voList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),"10211");
        }
    }
    //根据酒店id查询酒店政策
    @RequestMapping(value = "/queryhotelpolicy/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Dto queryHotelPolicy(@PathVariable Long id){
        if(EmptyUtils.isEmpty(id)){
            return DtoUtil.returnFail("酒店id不能为空","10208");
        }
        try {
            ItripSearchPolicyHotelVO queryHotelPolicy = hotelService.getQueryHotelPolicy(id);
            return DtoUtil.returnDataSuccess(queryHotelPolicy);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),"10209");
        }
    }
}
