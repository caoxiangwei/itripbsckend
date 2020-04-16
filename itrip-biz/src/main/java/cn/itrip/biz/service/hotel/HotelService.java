package cn.itrip.biz.service.hotel;

import cn.itrip.beans.pojo.ItripHotel;
import cn.itrip.beans.pojo.ItripLabelDic;
import cn.itrip.beans.vo.hotel.HotelVideoDescVO;
import cn.itrip.beans.vo.hotel.ItripSearchFacilitiesHotelVO;
import cn.itrip.beans.vo.hotel.ItripSearchPolicyHotelVO;

import java.util.List;

public interface HotelService {
    //根据酒店id查询酒店设施
    ItripSearchFacilitiesHotelVO getHotelFacilities(Long id) throws Exception;
    //根据酒店id查询酒店政策
    ItripSearchPolicyHotelVO getQueryHotelPolicy(Long id) throws Exception;
    //根据酒店id查询酒店特色和介绍
    List<ItripLabelDic> queryHotelDetails(Long id) throws Exception;
    //根据酒店id查询酒店特色、商圈、酒店名称
    HotelVideoDescVO getVideoDesc(Long id) throws Exception;
    //获取酒店相关信息（酒店名称、酒店星级）
    ItripHotel getHotelDesc(Long id) throws Exception;
}
