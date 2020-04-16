package cn.itrip.biz.service.comment;

import cn.itrip.beans.pojo.ItripComment;
import cn.itrip.beans.pojo.ItripImage;
import cn.itrip.beans.vo.comment.ItripListCommentVO;
import cn.itrip.beans.vo.comment.ItripScoreCommentVO;
import cn.itrip.common.Page;

import java.util.List;
import java.util.Map;

public interface CommentService {
    //新增评论接口
    int addComment(ItripComment comment,List<ItripImage> itripImages) throws Exception;
    //据酒店id查询酒店平均分
    ItripScoreCommentVO getHotelScore(Long hotelId) throws Exception;
    // 根据酒店id查询各类评论数量
    Integer getCount(Map map) throws Exception;
    //根据评论类型查询评论列表，并分页显示
    Page<ItripListCommentVO> getCommentListPage(Map map) throws Exception;

}
