package cn.itrip.auth.controller;

import cn.itrip.auth.service.TokenService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.ErrorCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

@Controller
@RequestMapping("/api")
public class TokenController {
    @Resource
    private TokenService tokenService;
    // 给headers里加一个token信息 headers = "token"
    @RequestMapping(value = "/retoken",method = RequestMethod.POST,headers = "token")
    @ResponseBody
    public Dto reloadToken(HttpServletRequest request){
        // 通过request.getHeader("user-agent")在请求头里获取user-agent信息
        String userAgent = request.getHeader("user-agent");
        // 通过request.getHeader("token")在请求头里获取token信息
        String token = request.getHeader("token");
        try {
            //拿到token串证明置换成功，给前端返回一个数据vo对象
            String newToken = tokenService.reloadToken(userAgent, token);
            ItripTokenVO vo = new ItripTokenVO(token,
                    Calendar.getInstance().getTimeInMillis()+2*60*60*1000,
                    Calendar.getInstance().getTimeInMillis());
            // 返回Dto对象
            return DtoUtil.returnDataSuccess(vo);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
        }
    }
}
