package cn.itrip.biz.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.pojo.ItripUserLinkUser;
import cn.itrip.beans.vo.userinfo.ItripAddUserLinkUserVO;
import cn.itrip.beans.vo.userinfo.ItripModifyUserLinkUserVO;
import cn.itrip.beans.vo.userinfo.ItripSearchUserLinkUserVO;
import cn.itrip.biz.service.userlinkuser.UserLinkService;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.ErrorCode;
import cn.itrip.common.ValidationToken;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/api/userinfo")
public class UserInfoController {
    @Resource
    private ValidationToken validationToken;
    @Resource
    private UserLinkService userLinkService;

    @ApiOperation(value = "新增常用联系人接口", httpMethod = "POST", response = Dto.class,notes = "新增常用联系人信息"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ " +"并返回错误码，" +"如下：100411 : " +
            "新增常用联系人失败," +"100412 : 不能提交空，请填写常用联系人信息100000 : token失效，请重登录 </p>")
    @RequestMapping(value = "/adduserlinkuser", method = RequestMethod.POST)
    @ResponseBody
    public Dto addUserLinkUser(@RequestBody ItripAddUserLinkUserVO vo, HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");
        String token = request.getHeader("token");
        if (EmptyUtils.isEmpty(vo)) {
            return DtoUtil.returnFail("不能提交空，请填写常用联系人信息", "100412");
        }
        try {
            if(!validationToken.validateToken(userAgent,token)){
                return DtoUtil.returnFail("token失效，请重登录",ErrorCode.BIZ_TOKENFAILUER);
            }
            ItripUserLinkUser linkUser = new ItripUserLinkUser();
            BeanUtils.copyProperties(vo,linkUser);
            ItripUser currentUser = validationToken.getCurrentUser(token);
            linkUser.setUserId(currentUser.getId());
            linkUser.setCreatedBy(currentUser.getId());
            linkUser.setCreationDate(currentUser.getCreationDate());
            userLinkService.addUserLinkUser(linkUser);
            return DtoUtil.returnSuccess("新增常用联系人成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("新增常用联系人失败", "100411");
        }
    }

    @ApiOperation(value = "删除常用联系人接口", httpMethod = "GET",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class,notes = "删除常用联系人信息"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100431 : 所选的常用联系人中有与某条待支付的订单关联的项，无法删除 </p>"+
            "<p>100432 : 删除常用联系人失败 </p>"+
            "<p>100433 : 请选择要删除的常用联系人</p>"+
            "<p>100000 : token失效，请重登录 </p>")
    @RequestMapping(value = "/deluserlinkuser",method = RequestMethod.GET)
    @ResponseBody
    public Dto delUserLinkUser(Long[] ids,HttpServletRequest request){
        String tokenString = request.getHeader("token");
        ItripUser currentUser = validationToken.getCurrentUser(tokenString);
        if(EmptyUtils.isEmpty(ids)){
            return DtoUtil.returnFail("请选择要删除的常用联系人","100433");
        }
        if(currentUser == null){
            return DtoUtil.returnFail("token失效，请重新登录",ErrorCode.BIZ_TOKENFAILUER);
        }
        try {
            List<Long> idsByOrder = userLinkService.getItripOrderLinkUserIdsByOrder();
            if(idsByOrder.size() > 0){
                return DtoUtil.returnFail("所选的常用联系人中有与某条待支付的订单关联的项，无法删除","100431");
            }else {
                userLinkService.delUserLinkUser(ids);
                return DtoUtil.returnSuccess("删除常用联系人成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("删除常用联系人失败","100432");
        }
    }

    @ApiOperation(value = "查询常用联系人接口", httpMethod = "POST", response = Dto.class,
            notes = "可根据联系人姓名进行模糊查询。若不根据联系人姓名进行查询，不输入参数即可 |" +
                    " 若根据联系人姓名进行查询，须进行相应的入参，比如：{\"linkUserName\":\"张三\"}")
    @RequestMapping(value = "/queryuserlinkuser",method = RequestMethod.POST)
    @ResponseBody
    public Dto queryUserLinkUser(@RequestBody ItripSearchUserLinkUserVO vo,HttpServletRequest request){
        String userAgent = request.getHeader("user-agent");
        String token = request.getHeader("token");
        try {
            if(!validationToken.validateToken(userAgent,token)){
                return DtoUtil.returnFail("token失效，请重新登录",ErrorCode.BIZ_TOKENFAILUER);
            }
            ItripUser currentUser = validationToken.getCurrentUser(token);
            //String linkUserName = (vo == null) ? null : vo.getLinkUserName();
            Map map = new HashMap();
            map.put("userId",currentUser.getId());
            map.put("linkUserName",vo.getLinkUserName());
            List<ItripUserLinkUser> userList = userLinkService.queryUserLinkUser(map);
            return DtoUtil.returnDataSuccess(userList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取常用联系人信息失败","100401");
        }
    }
    /*@ApiOperation(value = "查询常用联系人接口", httpMethod = "POST", response = Dto.class,
            notes = "可根据联系人姓名进行模糊查询。若不根据联系人姓名进行查询，不输入参数即可 |" +
                    " 若根据联系人姓名进行查询，须进行相应的入参，比如：{\"linkUserName\":\"张三\"}")
    @RequestMapping(value = "/queryuserlinkuser", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Dto<List<ItripUserLinkUser>> queryUserLinkUsers(HttpServletRequest request,
                   @ApiParam(name = "itripSearchUserLinkUserVO", value = "itripSearchUserLinkUserVO", required = false)
                   @RequestBody ItripSearchUserLinkUserVO itripSearchUserLinkUserVO){
        String userAgent = request.getHeader("user-agent");
        String token = request.getHeader("token");
        try {
            if (!validationToken.validateToken(userAgent, token)){
                return DtoUtil.returnFail("token失效，请重新登录", ErrorCode.BIZ_TOKENFAILUER);
            }
            ItripUser user = validationToken.getCurrentUser(token);
            List<ItripUserLinkUser> linkUsers = userLinkService.queryUserLinkUsers(itripSearchUserLinkUserVO, user);
            if (EmptyUtils.isEmpty(linkUsers)){
                return DtoUtil.returnFail("获取常用联系人信息失败","100401");
            }else {
                return DtoUtil.returnDataSuccess(linkUsers);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.BIZ_UNKNOWN);
        }
    }*/

    @ApiOperation(value = "修改常用联系人接口", httpMethod = "POST",
            protocols = "HTTP",produces = "application/json",
            response = Dto.class,notes = "修改常用联系人信息"+
            "<p>成功：success = ‘true’ | 失败：success = ‘false’ 并返回错误码，如下：</p>" +
            "<p>错误码：</p>"+
            "<p>100421 : 修改常用联系人失败 </p>"+
            "<p>100422 : 不能提交空，请填写常用联系人信息</p>"+
            "<p>100000 : token失效，请重登录 </p>")
    @RequestMapping(value = "/modifyuserlinkuser",method = RequestMethod.POST)
    @ResponseBody
    public Dto modifyUserLinkUser(@RequestBody ItripModifyUserLinkUserVO vo, HttpServletRequest request){
        String userAgent = request.getHeader("user-agent");
        String token = request.getHeader("token");
        if(EmptyUtils.isEmpty(vo)){
            return DtoUtil.returnFail("不能提交空，请填写常用联系人信息","100422");
        }
        try {
            if(!validationToken.validateToken(userAgent,token)){
                return DtoUtil.returnFail("token失效，请重新登录", ErrorCode.BIZ_TOKENFAILUER);
            }
            ItripUserLinkUser user = new ItripUserLinkUser();
            ItripUser currentUser = validationToken.getCurrentUser(token);
            BeanUtils.copyProperties(vo,user);
            //user.setId(vo.getId());
            //user.setLinkUserName(vo.getLinkUserName());
            //user.setLinkIdCardType(vo.getLinkIdCardType());
            //user.setLinkIdCard(vo.getLinkIdCard());
            //user.setLinkPhone(vo.getLinkPhone());
            user.setModifiedBy(currentUser.getId());
            user.setModifyDate(currentUser.getModifyDate());
            user.setUserId(currentUser.getId());
            userLinkService.modifyUserLinkUser(user);
            return DtoUtil.returnSuccess("修改常用联系人成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("修改常用联系人失败","100421");
        }
    }
}
