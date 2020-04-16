package cn.itrip.biz.service.comment;

import cn.itrip.beans.pojo.ItripComment;
import cn.itrip.beans.pojo.ItripImage;
import cn.itrip.beans.vo.comment.ItripListCommentVO;
import cn.itrip.beans.vo.comment.ItripScoreCommentVO;
import cn.itrip.common.Page;
import cn.itrip.dao.comment.ItripCommentMapper;
import cn.itrip.dao.hotelorder.ItripHotelOrderMapper;
import cn.itrip.dao.image.ItripImageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {
    @Resource
    private ItripCommentMapper commentMapper;
    @Resource
    private ItripImageMapper imageMapper;
    @Resource
    private ItripHotelOrderMapper orderMapper;

    //新增评论接口
    @Override
    @Transactional
    public int addComment(ItripComment comment,List<ItripImage> itripImages) throws Exception {
        //计算综合评分，综合评分=(设施+卫生+位置+服务)/4
        int sum = comment.getFacilitiesScore()+comment.getHygieneScore()+comment.getPositionScore()+comment.getServiceScore();
        int score = (int)Math.round(sum*1.0 / 4);
        comment.setScore(score);
        if(commentMapper.insertItripComment(comment) > 0){
            if(itripImages != null && itripImages.size() > 0){
                for (ItripImage image : itripImages ){
                    image.setTargetId(comment.getId());
                    imageMapper.insertItripImage(image);
                }
            }
        }
        orderMapper.updateHotelOrderStatus(comment.getOrderId(),comment.getCreatedBy());
        return 1;
    }

    ////据酒店id查询酒店平均分
    @Override
    public ItripScoreCommentVO getHotelScore(Long hotelId) throws Exception {
        return commentMapper.getCommentAvgScore(hotelId);
    }
    //// 根据酒店id查询各类评论数量
    @Override
    public Integer getCount(Map map) throws Exception {
        return commentMapper.getItripCommentCountByMap(map);
    }
    //根据评论类型查询评论列表，并分页显示
    @Override
    public Page<ItripListCommentVO> getCommentListPage(Map map) throws Exception {
        Integer beginPos = ((Integer) map.get("pageNo")-1)*(Integer)map.get("pageSize");
        map.put("beginPos",beginPos);
        List list = commentMapper.getItripCommentListByMap(map);
        Integer total = commentMapper.getItripCommentCountByMap(map);
        //int cuurentPage = EmptyUtils.isEmpty(pageNo) ? Constants.DEFAULT_PAGE_NO : pageNo;
        //int rows = EmptyUtils.isEmpty(pageSize) ? Constants.DEFAULT_PAGE_SIZE : pageSize;
        Integer pageNo = (Integer) map.get("pageNo");
        Integer pageSize = (Integer) map.get("pageSize");
        Page page = new Page(pageNo,pageSize,total);
        //map.put("beginPos",page.getBeginPos());
        //map.put("pageSize",page.getPageSize());
        page.setRows(list);
        return page;
    }
}
