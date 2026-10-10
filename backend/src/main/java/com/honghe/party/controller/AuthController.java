package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.SysRole;
import com.honghe.party.entity.SysUser;
import com.honghe.party.entity.SysUserRole;
import com.honghe.party.mapper.SysRoleMapper;
import com.honghe.party.mapper.SysUserMapper;
import com.honghe.party.mapper.SysUserRoleMapper;
import com.honghe.party.auth.JwtService;
import com.honghe.party.auth.RequestUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    /**
     * 账号密码登录，只有校验 BCrypt 密码成功才签发令牌。
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginReq) {
        String username = loginReq.get("username");
        String password = loginReq.get("password");
        if (username == null || username.isBlank() || password == null || password.isBlank() || password.length() > 72) {
            return Result.error(400, "请输入账号和密码");
        }
        String account = username.trim();
        // 多凭证统一鉴权体系：支持手机号码、系统工号、用户账号任一凭证登录
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, account)
                .or().eq(SysUser::getPhone, account)
                .or().eq(SysUser::getWorkNo, account)
                .last("LIMIT 1"));
        if (user == null) {
            return Result.error(401, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error(403, "该党员账号已被禁用");
        }
        if (user.getPassword() == null || !user.getPassword().startsWith("$2") || !passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(401, "账号或密码错误，旧演示账号须先由管理员设置正式密码");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 获取用户关联的角色列表
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );
        List<SysRole> roles = new ArrayList<>();
        for (SysUserRole ur : userRoles) {
            SysRole r = roleMapper.selectById(ur.getRoleId());
            if (r != null && Integer.valueOf(1).equals(r.getStatus())) {
                roles.add(r);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtService.issue(user.getId()));
        data.put("userInfo", user);
        data.put("roles", roles);

        return Result.success("登录成功", data);
    }

    /**
     * 获取用户列表（支持按组织、姓名过滤）
     */
    @GetMapping("/users")
    public Result<List<Map<String, Object>>> getUserList(@RequestParam(required = false) Long orgId,
                                                         @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null) {
            wrapper.eq(SysUser::getOrgId, orgId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getRealName, keyword).or().like(SysUser::getUsername, keyword));
        }
        wrapper.orderByAsc(SysUser::getId);

        List<SysUser> userList = userMapper.selectList(wrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (SysUser u : userList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("realName", u.getRealName());
            map.put("workNo", u.getWorkNo());
            map.put("phone", u.getPhone());
            map.put("email", u.getEmail());
            map.put("wecomUserId", u.getWecomUserId());
            map.put("dingtalkUserId", u.getDingtalkUserId());
            map.put("orgId", u.getOrgId());
            map.put("orgName", u.getOrgName());
            map.put("status", u.getStatus());
            map.put("lastLoginTime", u.getLastLoginTime());
            map.put("createdAt", u.getCreatedAt());

            // 补充角色
            List<SysUserRole> urList = userRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, u.getId())
            );
            List<String> roleNames = new ArrayList<>();
            List<Long> roleIds = new ArrayList<>();
            for (SysUserRole ur : urList) {
                SysRole role = roleMapper.selectById(ur.getRoleId());
                if (role != null) {
                    roleNames.add(role.getRoleName());
                    roleIds.add(role.getId());
                }
            }
            map.put("roleNames", roleNames);
            map.put("roleIds", roleIds);
            resultList.add(map);
        }

        return Result.success("获取用户列表成功", resultList);
    }

    /**
     * 新增用户
     */
    @PostMapping("/users")
    public Result<SysUser> createUser(@RequestBody SysUser user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        if (user.getStatus() == null) user.setStatus(1);
        if (!validPassword(user.getPassword())) return Result.error(400, "请设置8至72字节且含字母、数字、特殊字符的密码");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return Result.success("新建党务账号成功", user);
    }

    /**
     * 修改用户（包含启停用、重置密码等）
     */
    @PutMapping("/users/{id}")
    public Result<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        if (user.getPassword() != null) {
            if (!validPassword(user.getPassword())) return Result.error(400, "请设置8至72字节且含字母、数字、特殊字符的密码");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setId(id);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success("更新用户资料成功", user);
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/users/{id}/roles")
    public Result<String> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        if (roleIds != null) {
            for (Long rId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(id);
                ur.setRoleId(rId);
                ur.setCreatedAt(LocalDateTime.now());
                userRoleMapper.insert(ur);
            }
        }
        return Result.success("角色权限指派成功");
    }

    /**
     * 获取所有角色列表及权限
     */
    @GetMapping("/roles")
    public Result<List<SysRole>> getRoleList() {
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSortOrder)
        );
        return Result.success("获取角色列表成功", roles);
    }

    /**
     * 新增或编辑角色
     */
    @PostMapping("/roles")
    public Result<SysRole> saveRole(@RequestBody SysRole role) {
        if (role.getId() == null) {
            role.setCreatedAt(LocalDateTime.now());
            role.setUpdatedAt(LocalDateTime.now());
            if (role.getStatus() == null) role.setStatus(1);
            roleMapper.insert(role);
        } else {
            role.setUpdatedAt(LocalDateTime.now());
            roleMapper.updateById(role);
        }
        return Result.success("保存角色权限成功", role);
    }

    @PutMapping("/password")
    public Result<String> changePassword(@RequestAttribute(RequestUser.ATTRIBUTE) RequestUser principal, @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        if (oldPassword == null || !passwordEncoder.matches(oldPassword, principal.user().getPassword())) return Result.error(400, "原密码错误");
        if (!validPassword(newPassword)) return Result.error(400, "新密码须为8至72字节且含字母、数字、特殊字符");
        SysUser updated = new SysUser();
        updated.setId(principal.user().getId());
        updated.setPassword(passwordEncoder.encode(newPassword));
        updated.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(updated);
        return Result.success("密码已更新");
    }

    private boolean validPassword(String password) {
        return password != null && password.length() >= 8 && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72
                && password.matches("(?s).*[a-zA-Z].*") && password.matches("(?s).*\\d.*") && password.matches("(?s).*[^a-zA-Z0-9].*");
    }
}
