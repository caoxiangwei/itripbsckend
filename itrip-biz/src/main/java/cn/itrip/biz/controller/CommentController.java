package cn.itrip.biz.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripComment;
import cn.itrip.beans.pojo.ItripHotel;
import cn.itrip.beans.pojo.ItripImage;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.ItripImageVO;
import cn.itrip.beans.vo.ItripLabelDicVO;
import cn.itrip.beans.vo.comment.*;
import cn.itrip.biz.service.comment.CommentService;
import cn.itrip.biz.service.hotel.HotelService;
import cn.itrip.biz.service.image.ImageService;
import cn.itrip.biz.service.labeldic.LabelService;
import cn.itrip.common.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

@Controller
@RequestMapping("/api/comment")
public class CommentController {
    @Resource
    private CommentService commentService;
    @Resource
    private ImageService imageService;
    @Resource
    private LabelService labelService;
    @Resource
    private HotelService hotelService;
    @Resource
    private ValidationToken validationToken;
    @Resource
    private SystemConfig systemConfig;

    @ApiOperation(value = "查询出游类型列表", httpMethod = "GET",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class,notes = "查询出游类型列表"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100019 : 获取旅游类型列表错误 </p>")
    @RequestMapping(value = "/gettraveltype",method = RequestMethod.GET)
    @ResponseBody
    public Dto getTravelType(Long parentId){
        try {
            List<ItripLabelDicVO> travelType = labelService.getTravelType(parentId);
            return DtoUtil.returnDataSuccess(travelType);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(" 获取旅游类型列表错误", ErrorCode.BIZ_TRAVELTYPE_TYPEFAIL);
        }
    }

    @ApiOperation(value = "根据targetId查询评论照片(type=2)", httpMethod = "GET",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class,notes = "总体评分、位置评分、设施评分、服务评分、卫生评分"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100012 : 获取评论图片失败 </p>"+
            "<p>100013 : 评论id不能为空</p>")
    @RequestMapping(value = "/getimg/{targetId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto getImg(@PathVariable String targetId){
        if(EmptyUtils.isEmpty(targetId)){
            return DtoUtil.returnFail("评论id不能为空","100013");
        }
        Map map = new HashMap();
        map.put("type","2");
        map.put("targetId",targetId);
        try {
            List<ItripImageVO> imageVOList = imageService.getCommentedImages(map);
            return DtoUtil.returnDataSuccess(imageVOList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取评论图片失败","100012");
        }
    }
    @ApiOperation(value = "新增评论接口", httpMethod = "POST",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class,notes = "新增评论信息"+
            "<p style=‘color:red’>注意：若有评论图片，需要传图片路径</p>"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100003 : 新增评论失败 </p>"+
            "<p>100004 : 不能提交空，请填写评论信息</p>"+
            "<p>100005 : 新增评论，订单ID不能为空</p>"+
            "<p>100000 : token失效，请重登录 </p>")
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public Dto commentAdd(@RequestBody ItripAddCommentVO vo,HttpServletRequest request){
        String agent = request.getHeader("user-agent");
        String token = request.getHeader("token");
        try {
            if(!validationToken.validateToken(agent, token)){
                return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
            }
            if(EmptyUtils.isEmpty(vo)){
                return DtoUtil.returnFail("不能提交空，请填写评论信息", "100004");
            }
            if(EmptyUtils.isEmpty(vo.getOrderId())){
                return DtoUtil.returnFail("新增评论，订单ID不能为空","100005 ");
            }
            ItripComment comment = new ItripComment();
            BeanUtils.copyProperties(vo,comment);
            ItripUser currentUser = validationToken.getCurrentUser(token);
            comment.setCreatedBy(currentUser.getId());
            comment.setCreationDate(new Date());
            comment.setUserId(currentUser.getId());

            List<ItripImage> imageList = new ArrayList<>();
            if(vo.getIsHavingImg() == 1){
                ItripImage[] itripImages = vo.getItripImages();
                int i = 1;
                for (ItripImage image : itripImages){
                    image.setCreatedBy(currentUser.getId());
                    image.setPosition(i);
                    image.setCreationDate(comment.getCreationDate());
                    image.setType("2");
                    imageList.add(image);
                    i++;
                }
            }
            commentService.addComment(comment,imageList);
            return DtoUtil.returnSuccess("新增评论成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("新增评论失败","100003");
        }
    }

    @ApiOperation(value = "图片删除接口", httpMethod = "POST",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class, notes = "删除传递图片名称"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100010 : 文件不存在，删除失败 </p>"+
            "<p>100000 : token失效，请重登录 </p>")
    @RequestMapping(value = "/delpic",method = RequestMethod.POST)
    @ResponseBody
    public Dto delPic(String imgName, HttpServletRequest request){
        String userAgent = request.getHeader("user-agent");
        String token = request.getHeader("token");
        try {
            if (!validationToken.validateToken(userAgent, token)) {
                return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
            }
            String path = "/data/comment/upload/" + imgName;
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            return DtoUtil.returnSuccess("删除图片成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("删除图片失败", "100010");
        }
    }


    @ApiOperation(value = "图片上传接口", httpMethod = "POST", response = Dto.class,
            notes = "上传评论图片，最多支持4张图片同时上传，格式为：jpg、jpeg、png，" +
                    "大小不超过5M<p style=‘color:red’>注意：input file 中的name不可重复" +
                    " e:g : file1 、 file2 、 fileN")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Dto<List<String>> upload(HttpServletRequest request){
        List<String> result = new ArrayList<>();
        List<String> hasError = new ArrayList<>();
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        if (multipartResolver.isMultipart(request)){
            //处理文件上传
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            String userAgent = request.getHeader("user-agent");
            String token = request.getHeader("token");
            try {
                if (!validationToken.validateToken(userAgent, token)){
                    return DtoUtil.returnFail("token失效，请重新登录", ErrorCode.BIZ_TOKENFAILUER);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return DtoUtil.returnFail(e.getMessage(), ErrorCode.BIZ_UNKNOWN);
            }
            int fileCount;
            try {
                fileCount = multipartHttpServletRequest.getMultiFileMap().size();
            }catch (Exception e){
                return DtoUtil.returnFail("文件大小超限","100009");
            }
            if (fileCount <= 4){
                //读文件
                //获取文件名，文件类型
                //验证文件类型
                //保存文件：不用文件原名，用户id-系统时间毫秒数-随机数.后缀
                //存到服务器指定目录
                Iterator<String> names = multipartHttpServletRequest.getFileNames();
                ItripUser user = validationToken.getCurrentUser(token);
                while (names.hasNext()){
                    String originalFilename = null;
                    try {
                        MultipartFile file = multipartHttpServletRequest.getFile(names.next());
                        if (file != null){
                            originalFilename = file.getOriginalFilename();
                            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                            if (!originalFilename.equals("") && (suffix.equals(".jpg") || suffix.equals(".jpeg") || suffix.equals(".png")) ){
                                //文件名
                                String fileName = user.getId() + "-" + System.currentTimeMillis() + "-"
                                        + (int)(Math.random()*100000) + suffix;
                                //完整的保存路径
                                //File.separator  目录分隔符  /
                                //File.pathSeparator  路径分隔符  ;
                                String path = "/data/itrip/uploadimg" + fileName;
                                File localFile = new File(path);
                                file.transferTo(localFile);
                                result.add("http://img.itrip.cn/" + fileName);
                                /*String fullPath = systemConfig.getFileUploadPathString() + fileName;
                                file.transferTo(new File(fullPath));
                                result.add(systemConfig.getVisitImgUrlString() + fileName);*/
                            }else {
                                hasError.add(originalFilename + "不是规定的文件类型");
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        hasError.add(originalFilename + "上传失败");
                    }
                }
                if (hasError.isEmpty()){
                    return DtoUtil.returnDataSuccess(result);
                }else {
                    return DtoUtil.returnSuccess(hasError.toString(), result);
                }
            }else {
                return DtoUtil.returnFail("上传的文件数不正确，必须是大于1小于等于4", "100007");
            }
        }else {
            return DtoUtil.returnFail("请求的内容不是上传文件的类型","100008");
        }
    }

    @ApiOperation(value = "根据评论类型查询评论列表，并分页显示", httpMethod = "POST",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class,notes = "根据评论类型查询评论列表，并分页显示"+
            "<p>参数数据e.g：</p>" +
            "<p>全部评论：{\"hotelId\":10,\"isHavingImg\":-1,\"isOk\":-1,\"pageSize\":5,\"pageNo\":1}</p>" +
            "<p>有图片：{\"hotelId\":10,\"isHavingImg\":1,\"isOk\":-1,\"pageSize\":5,\"pageNo\":1}</p>" +
            "<p>值得推荐：{\"hotelId\":10,\"isHavingImg\":-1,\"isOk\":1,\"pageSize\":5,\"pageNo\":1}</p>" +
            "<p>有待改善：{\"hotelId\":10,\"isHavingImg\":-1,\"isOk\":0,\"pageSize\":5,\"pageNo\":1}</p>" +
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100020 : 获取评论列表错误 </p>")
    @RequestMapping(value = "/getcommentlist",method = RequestMethod.POST)
    @ResponseBody
    public Dto getCommentList(@RequestBody ItripSearchCommentVO vo){
        Map map = new HashMap();
        map.put("hotelId",vo.getHotelId());
        if(vo.getIsHavingImg() == -1){
            vo.setIsHavingImg(null);
        }
        if(vo.getIsOk() == -1){
            vo.setIsOk(null);
        }
        map.put("isHavingImg",vo.getIsHavingImg());
        map.put("isOk",vo.getIsOk());
        Integer pageNo = vo.getPageNo() == null ? 1 : vo.getPageNo();
        map.put("pageNo",pageNo);
        Integer pageSize = vo.getPageSize() == null ? 5 : vo.getPageSize();
        map.put("pageSize",pageSize);
        try {
            Page<ItripListCommentVO> listPage = commentService.getCommentListPage(map);
            return DtoUtil.returnDataSuccess(listPage);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取评论列表错误","100020");
        }
    }

    @ApiOperation(value = "获取酒店相关信息（酒店名称、酒店星级）", httpMethod = "GET",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class,notes = "新增评论信息页面内获取酒店相关信息（酒店名称、酒店星级）"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100021 : 获取酒店相关信息错误 </p>")
    @RequestMapping(value = "/gethoteldesc/{hotelId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto getHotelDesc(@PathVariable String hotelId){
        try {
            ItripHotel hotel = hotelService.getHotelDesc(Long.parseLong(hotelId));
            ItripHotelDescVO descVO = new ItripHotelDescVO();
            BeanUtils.copyProperties(hotel,descVO);
            return DtoUtil.returnDataSuccess(descVO);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取酒店相关信息错误","100021");
        }
    }

    @ApiOperation(value = "据酒店id查询酒店平均分", httpMethod = "GET",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class,notes = "总体评分、位置评分、设施评分、服务评分、卫生评分"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100001 : 获取评分失败 </p>"+
            "<p>100002 : hotelId不能为空</p>")
    @RequestMapping(value = "/gethotelscore/{hotelId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto gethotelscore(@PathVariable String hotelId){
        if(EmptyUtils.isEmpty(hotelId)){
            return DtoUtil.returnFail("hotelId不能为空","100002");
        }
        try {
            ItripScoreCommentVO hotelScore = commentService.getHotelScore(Long.parseLong(hotelId));
            return DtoUtil.returnSuccess("获取评分成功",hotelScore);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取评分失败","100001");
        }
    }

    @ApiOperation(value = "根据酒店id查询各类评论数量", httpMethod = "GET",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class,notes = "根据酒店id查询评论数量（全部评论、值得推荐、有待改善、有图片）"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100014 : 获取酒店总评论数失败 </p>"+
            "<p>100015 : 获取酒店有图片评论数失败</p>"+
            "<p>100016 : 获取酒店有待改善评论数失败</p>"+
            "<p>100017 : 获取酒店值得推荐评论数失败</p>"+
            "<p>100018 : 参数hotelId为空</p>")
    @RequestMapping(value = "/getcount/{hotelId}",method = RequestMethod.GET)
    @ResponseBody
    public Dto getCount(@PathVariable String hotelId) {
        if (EmptyUtils.isEmpty(hotelId)) {
            return DtoUtil.returnFail("参数hotelId为空", "100018");
        }
        Map map = new HashMap();
        Map resultMap = new HashMap();
        map.put("hotelId", hotelId);
        try {
            Integer count = commentService.getCount(map);
            resultMap.put("allcomment", count);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取酒店总评论数失败", "100014");
        }
        try {
            map.put("isHavingImg", 1);
            Integer imgCount = commentService.getCount(map);
            resultMap.put("havingimg",imgCount);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取酒店有图片评论数失败", "100015");
        }
        try {
            map.remove("isHavingImg");
            map.remove("isOk",0);
            Integer improveCount = commentService.getCount(map);
            resultMap.put("improve", improveCount);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(" 获取酒店有待改善评论数失败", "100016");
        }
        try {
            map.put("isOk",1);
            Integer isOkCount = commentService.getCount(map);
            resultMap.put("isok",isOkCount);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(" 获取酒店值得推荐评论数失败", "100017");
        }
        return DtoUtil.returnSuccess("获取评论数成功", resultMap);
    }
}
