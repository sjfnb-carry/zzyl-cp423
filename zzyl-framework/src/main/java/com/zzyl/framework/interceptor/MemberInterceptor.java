package com.zzyl.framework.interceptor;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.zzyl.common.constant.HttpStatus;
import com.zzyl.common.utils.UserThreadLocal;
import com.zzyl.framework.web.service.TokenService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MemberInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenService tokenService;

    /**
     * 在请求处理之前进行拦截处理，用于验证用户身份令牌
     *
     * @param request  HTTP请求对象，用于获取请求头信息
     * @param response HTTP响应对象，用于设置响应状态
     * @param handler  处理器对象
     * @return boolean 返回true表示验证通过，继续执行后续处理；返回false表示验证失败，中断请求处理
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取认证令牌
        String token = request.getHeader("authorization");
        if (StrUtil.isEmpty(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 解析令牌获取声明信息
        Claims claims = null;
        try {
            claims = tokenService.parseToken(token);
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 从声明中提取用户ID并存储到线程本地变量中
        Long userId = MapUtil.get(claims, "userId", Long.class);
        if (userId == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED);
            return false;
        }
        UserThreadLocal.set(userId);

        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.remove();
    }
}
