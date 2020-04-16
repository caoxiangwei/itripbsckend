package cn.itrip.biz.service.hotel;

import cn.itrip.beans.pojo.ItripAreaDic;
import cn.itrip.beans.pojo.ItripHotel;
import cn.itrip.beans.pojo.ItripLabelDic;
import cn.itrip.beans.vo.hotel.HotelVideoDescVO;
import cn.itrip.beans.vo.hotel.ItripSearchFacilitiesHotelVO;
import cn.itrip.beans.vo.hotel.ItripSearchPolicyHotelVO;
import cn.itrip.dao.hotel.ItripHotelMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class HotelServiceImpl implements HotelService {
    @Resource
    private ItripHotelMapper hotelMapper;

    //根据酒店id查询酒店设施
    @Override
    public ItripSearchFacilitiesHotelVO getHotelFacilities(Long id) throws Exception {
        return hotelMapper.getItripHotelFacilitiesById(id);
    }

    //根据酒店id查询酒店政策
    @Override
    public ItripSearchPolicyHotelVO getQueryHotelPolicy(Long id) throws Exception {
        return hotelMapper.queryHotelPolicy(id);
    }
    //根据酒店id查询酒店特色和介绍
    @Override
    public List<ItripLabelDic> queryHotelDetails(Long id) throws Exception {
        return hotelMapper.getHotelFeatureByHotelId(id);
    }
    //根据酒店id查询酒店特色、商圈、酒店名称
    @Override
    public HotelVideoDescVO getVideoDesc(Long id) throws Exception {
        //
        HotelVideoDescVO hotelVideoDescVO = new HotelVideoDescVO();
        List<ItripAreaDic> itripAreaDicList = new ArrayList<>();
        itripAreaDicList = hotelMapper.getHotelAreaByHotelId(id);
        List<String> tempList1 = new ArrayList<>();
        for (ItripAreaDic itripAreaDic:itripAreaDicList) {
            tempList1.add(itripAreaDic.getName());
        }
        hotelVideoDescVO.setTradingAreaNameList(tempList1);

        List<ItripLabelDic> itripLabelDicList = new ArrayList<>();
        itripLabelDicList = hotelMapper.getHotelFeatureByHotelId(id);
        List<String> tempList2 = new ArrayList<>();
        for (ItripLabelDic itripLabelDic:itripLabelDicList) {
            tempList2.add(itripLabelDic.getName());
        }
        hotelVideoDescVO.setHotelFeatureList(tempList2);

        hotelVideoDescVO.setHotelName(hotelMapper.getItripHotelById(id).getHotelName());
        return hotelVideoDescVO;
    }
    //获取酒店相关信息（酒店名称、酒店星级）
    @Override
    public ItripHotel getHotelDesc(Long id) throws Exception {
        return hotelMapper.getItripHotelById(id);
    }
}
