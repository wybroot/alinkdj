package com.honghe.party.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.*;
import com.honghe.party.mapper.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;

/** Protect notification operations and their account/role administration boundary. */
@Component
public class NoticeAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final SysUserMapper users;
    private final SysUserRoleMapper userRoles;
    private final SysRoleMapper roles;
    private final ObjectMapper mapper;

    public NoticeAuthFilter(JwtService jwt, SysUserMapper users, SysUserRoleMapper userRoles, SysRoleMapper roles, ObjectMapper mapper) {
        this.jwt = jwt; this.users = users; this.userRoles = userRoles; this.roles = roles; this.mapper = mapper;
    }

    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // 生产安全白名单：仅放行登录入口与 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return "/auth/login".equals(path);
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        Long userId;
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) throw new IllegalArgumentException();
            userId = jwt.userId(authorization.substring(7));
        } catch (Exception e) {
            reject(response, 401, "请使用账号密码登录，或登录已过期");
            return;
        }
        SysUser user = users.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            reject(response, 401, "账号已停用或不存在"); return;
        }
        var codes = new HashSet<String>();
        for (SysUserRole ur : userRoles.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))) {
            SysRole role = roles.selectById(ur.getRoleId());
            if (role != null && Integer.valueOf(1).equals(role.getStatus())) codes.add(role.getRoleCode());
        }
        RequestUser principal = new RequestUser(user, codes);
        String path = request.getServletPath();
        boolean requiresAdmin = path.startsWith("/notice/channels") || (path.startsWith("/auth/") && !path.equals("/auth/password"));
        boolean requiresSender = path.equals("/notice/send") || path.equals("/notice/trigger-warnings")
                || path.equals("/notice/statistics") || path.equals("/notice/recipients");
        if ((requiresAdmin && !principal.admin()) || (requiresSender && !principal.managesNotices())) {
            reject(response, 403, "当前账号无此操作权限"); return;
        }
        request.setAttribute(RequestUser.ATTRIBUTE, principal);
        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), Result.error(status, message));
    }
}
