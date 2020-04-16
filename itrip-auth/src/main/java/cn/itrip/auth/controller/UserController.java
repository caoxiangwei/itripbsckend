package cn.itrip.auth.controller;

import cn.itrip.auth.service.TokenService;
import cn.itrip.auth.service.UserService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.beans.vo.userinfo.ItripUserVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.ErrorCode;
import cn.itrip.common.MD5;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.regex.Pattern;
@Api(value = "用户controller",tags = {"故操作接口"})
@Controller
@RequestMapping("api")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private TokenService tokenService;

    @RequestMapping(value = "/dologin",method = RequestMethod.POST)
    @ResponseBody
    public Dto dologin(String name, String password, HttpServletRequest request){
        try { //登录失败
            ItripUser user = userService.login(name, MD5.getMd5(password,32));
            if(user == null){
                return DtoUtil.returnFail("用户名或密码错误",ErrorCode.AUTH_UNKNOWN);
            }
            //登录成功  生成token
            String userAgent = request.getHeader("user-agent");
            String token = tokenService.generrateToken(userAgent, user);
            //保存token到redis
            tokenService.saveToken(token,user);
            //返回一个vo对象
            ItripTokenVO vo = new ItripTokenVO(token,
                    //过期时间
                    Calendar.getInstance().getTimeInMillis()+2*60*60*1000,
                    //生成时间
                    Calendar.getInstance().getTimeInMillis());
            return DtoUtil.returnSuccess("登录成功",vo);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
        }
    }
    //在请求头headers里必须有一个token  headers = "token"中的token是我们自己生成的token字符串
    @RequestMapping(value = "/logout",method = RequestMethod.GET,headers = "token")
    @ResponseBody
    public Dto logout(HttpServletRequest request){
        //获取浏览器中的user-agent数据验证是不是同一个浏览器发送的请求
        //浏览器信息不一样 agent中的信息也不一样
        String userAgent = request.getHeader("user-agent");
        //在前端请求头里传入token  保存一个token信息
        String token = request.getHeader("token");
        try {
            //判断token和userAgent是否有效  有效就删除token  无效就返回已给前端页面或错误信息
            if(tokenService.validateToken(userAgent,token)){
                //删除token
                tokenService.deleteToken(token);
                return DtoUtil.returnSuccess("退出成功");
            }else {
                return DtoUtil.returnFail("token无效", ErrorCode.AUTH_TOKEN_INVALID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnSuccess(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
        }
    }

    @ApiOperation(value = "手机号注册",httpMethod = "post",response = Dto.class,notes = "手机号注册")
    @RequestMapping(value="/registerbyphone",method = RequestMethod.POST)
    @ResponseBody
    public Dto registerByPhone(@ApiParam(name = "userVo",value = "用户实体",
            required = true) @RequestBody ItripUserVO vo){
        if(!validatePhone(vo.getUserCode())){
            return DtoUtil.returnFail("请输入正确的手机号",ErrorCode.AUTH_ILLEGAL_USERCODE);
        }
        ItripUser user = new ItripUser();
        user.setUserCode(vo.getUserCode());
        user.setUserName(vo.getUserName());
        try {
            if(userService.findByUserCode(user.getUserCode()) != null){
                return DtoUtil.returnFail("用户已存在",ErrorCode.AUTH_USER_ALREADY_EXISTS);
            }
            user.setUserPassword(MD5.getMd5(vo.getUserPassword(),32));
            userService.createUserByPhone(user);
            return DtoUtil.returnSuccess("注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
        }
    }

    @ApiOperation(value = "验证手机号",httpMethod = "put",response = Dto.class,notes = "验证手机号")
    @RequestMapping(value="/validatephone",method = RequestMethod.PUT)
    @ResponseBody
    public Dto validatePhone(@ApiParam(name = "user",value = "手机号",required = true) String user,
                             @ApiParam(name = "user",value = "验证码",required = true) String code){
        try {
            if(userService.validatePhone(user,code)){
                return DtoUtil.returnSuccess("验证成功");
            }else{
                return DtoUtil.returnFail("验证失败",ErrorCode.AUTH_AUTHENTICATION_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
        }
    }
    // 检查用户存不存在的验证
    @RequestMapping(value = "/ckusr",method = RequestMethod.GET)
    @ResponseBody
    public Dto checkUser(@RequestParam String name){
        try {
            if(null == userService.findByUserCode(name)){
                return DtoUtil.returnSuccess("用户名可用");
            }else {
                return DtoUtil.returnFail("用户已存在，注册失效",ErrorCode.AUTH_USER_ALREADY_EXISTS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
        }
    }
    @RequestMapping(value = "/doregister",method = RequestMethod.POST)
    @ResponseBody
    public Dto registerByMail(@RequestBody ItripUserVO userVO){
        if(!validEmail(userVO.getUserCode())){
            return DtoUtil.returnFail("邮箱地址不正确",ErrorCode.AUTH_ILLEGAL_USERCODE);
        }
        ItripUser user = new ItripUser();
        user.setUserCode(userVO.getUserCode());
        user.setUserName(userVO.getUserName());
        try {
            if(userService.findByUserCode(userVO.getUserCode()) != null){
                return DtoUtil.returnFail("邮箱已被注册",ErrorCode.AUTH_USER_ALREADY_EXISTS);
            }
            user.setUserPassword(MD5.getMd5(userVO.getUserPassword(),32));
            userService.createUserByMail(user);
            return DtoUtil.returnSuccess("注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
        }

    }
    @RequestMapping(value = "/activate",method = RequestMethod.PUT)
    @ResponseBody
    public Dto validateMail(String user,String code){
        try {
            if(userService.validateMail(user,code)){
                return DtoUtil.returnSuccess("激活成功");
            }else {
                return DtoUtil.returnFail("激活失败",ErrorCode.AUTH_ACTIVATE_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
        }
    }
    private boolean validatePhone(String phoneNum){
        String reg = "^1[356789]\\d{9}$";
        return Pattern.compile(reg).matcher(phoneNum).find();
    }
    private boolean validEmail(String email){
        String regex="^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$"  ;
        return Pattern.compile(regex).matcher(email).find();
    }
}
