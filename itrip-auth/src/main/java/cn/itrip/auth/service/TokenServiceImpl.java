package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisUtil;
import cn.itrip.common.UserAgentUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class  TokenServiceImpl implements TokenService {
    //token:PC-usercode(MD5)-userid-creationdate-random(6位)
    @Override
    public String generrateToken(String userAgent, ItripUser user) throws Exception {
        StringBuilder str = new StringBuilder("token:");
        //判读哪种客户端
        if(!UserAgentUtil.CheckAgent(userAgent)){
            str.append("PC-");
        }else {
            str.append("MOBILE-");
        }
        //拼接usercode   usercod是加密过的加密
        str.append(MD5.getMd5(user.getUserCode(),32)+"-");
        //拼接userid
        str.append(user.getId()+"-");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        //拼接当前时间
        str.append(simpleDateFormat.format(new Date())+"-");
        //拼接加密过得6位随机数
        str.append(MD5.getMd5(userAgent,6));
        return str.toString();
    }
    @Resource
    private RedisUtil redisApi;
    @Override
    public void saveToken(String token, ItripUser user) throws Exception {
        String json = JSONObject.toJSONString(user);
        if(token.startsWith("token:PC-")){
            //pc端有过期时间  设定为（2小时）
            redisApi.setString(token,json,2*60*60);
        }else {
            //移动端没有过期时间
            redisApi.setString(token,json);
        }
    }

    @Override
    public boolean validateToken(String userAgent, String token) throws Exception {
        //判断Redis中的token是否存在  查询Redis中的数据
        if(!redisApi.hasKey(token)){
            return false;
        }
        //判断Redis中token字符串后6位加密的随机数和前端传来的是否一致
        String agentMD5 = token.split("-")[4];
        if(!MD5.getMd5(userAgent,6).equals(agentMD5)){
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteToken(String token) throws Exception {
        if(redisApi.del(token)){
            return true;
        }
        return false;
    }

    @Override
    public String reloadToken(String userAgent, String token) throws Exception {
        //判断Redis中是否存在token  不存在抛出一个异常
        if(!redisApi.hasKey(token)){
            throw new Exception("token无效");
        }
        // 转换时间格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        // 计算保护期，拆分token字符串，token中的第4部分是生成时间，通过一系列的转换把它转换成一个毫秒数
        // 生成token的时间
        long getTime = simpleDateFormat.parse(token.split("-")[3]).getTime();
        // 获取时间差  当前时间减去生成token的时间得到过去多少时间
        long passTime = Calendar.getInstance().getTimeInMillis() - getTime;
        // 设置保护期自己规定多长时间，并判断是否在保护期内，没有超过保护期抛出一个异常
        if(passTime < 30*60*1000){
            throw new Exception("token处于保护期，不允许置换，还剩"+(30*60*1000-passTime)/1000+"秒");
        }
        // 生成新的token
        String newToken = "";
        // 获取Redis中原来的user对象  通过key键获取value值
        // value的值是把user对象生成的json串  把value的值转换成user对象
        ItripUser user = JSONObject.parseObject(redisApi.getString(token),ItripUser.class);
        // 获取Redis的有效期 剩余的时间
        Long ttl = redisApi.getExpire(token);
        // 判断有效期剩余时间是否是有效的  有效就是合理  生成新的token
        if(ttl > 0 || ttl == -1){
            // 生成新的token
            newToken = generrateToken(userAgent,user);
            // 保存token到Redis中
            saveToken(newToken,user);
            // 给旧的token重新设置有效期，不能让它马上失效，给它一定的有效期
            // token代表原来的token，value值是json串把它转一下user对象，后边给一个时间
            redisApi.setString(token,JSONObject.toJSONString(user),5*60);
        }else {
            // 不合理 抛出已给异常
            throw new Exception("时间异常，不能置换");
        }
        return newToken;
    }


}
