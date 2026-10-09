package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.SysRole;
import com.honghe.party.entity.SysUser;
import com.honghe.party.entity.SysUserRole;
import com.honghe.party.mapper.SysRoleMapper;
import com.honghe.party.mapper.SysUserMapper;
import com.honghe.party.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @InjectMocks
    private AuthController authController;

    private SysUser testUser;
    private SysRole testRole;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setRealName("系统管理员");
        testUser.setWorkNo("SYS-001");
        testUser.setStatus(1);

        testRole = new SysRole();
        testRole.setId(10L);
        testRole.setRoleCode("SYS_ADMIN");
        testRole.setRoleName("系统管理员角色");
    }

    @Test
    @DisplayName("测试用户登录 - 成功流程")
    void testLoginSuccess() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(1L);
        ur.setRoleId(10L);
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(ur));
        when(roleMapper.selectById(10L)).thenReturn(testRole);

        Map<String, String> req = new HashMap<>();
        req.put("username", "admin");

        Result<Map<String, Object>> result = authController.login(req);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("token"));
        assertEquals(testUser, result.getData().get("userInfo"));
        List<SysRole> roles = (List<SysRole>) result.getData().get("roles");
        assertEquals(1, roles.size());
        assertEquals("SYS_ADMIN", roles.get(0).getRoleCode());

        verify(userMapper, times(1)).updateById(testUser);
    }

    @Test
    @DisplayName("测试用户登录 - 用户不存在")
    void testLoginUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Map<String, String> req = new HashMap<>();
        req.put("username", "unknown");

        Result<Map<String, Object>> result = authController.login(req);

        assertEquals(404, result.getCode());
        assertTrue(result.getMessage().contains("用户不存在"));
    }

    @Test
    @DisplayName("测试用户登录 - 账号已被禁用")
    void testLoginUserDisabled() {
        testUser.setStatus(0);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        Map<String, String> req = new HashMap<>();
        req.put("username", "admin");

        Result<Map<String, Object>> result = authController.login(req);

        assertEquals(403, result.getCode());
        assertTrue(result.getMessage().contains("禁用"));
    }

    @Test
    @DisplayName("测试获取用户列表及关联角色")
    void testGetUserList() {
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(testUser));

        SysUserRole ur = new SysUserRole();
        ur.setUserId(1L);
        ur.setRoleId(10L);
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(ur));
        when(roleMapper.selectById(10L)).thenReturn(testRole);

        Result<List<Map<String, Object>>> result = authController.getUserList(null, null);

        assertEquals(200, result.getCode());
        List<Map<String, Object>> list = result.getData();
        assertEquals(1, list.size());
        assertEquals("系统管理员", list.get(0).get("realName"));
        List<String> roleNames = (List<String>) list.get(0).get("roleNames");
        assertTrue(roleNames.contains("系统管理员角色"));
    }

    @Test
    @DisplayName("测试新建党务用户")
    void testCreateUser() {
        SysUser newUser = new SysUser();
        newUser.setUsername("zhangqiang");
        newUser.setRealName("张强");
        newUser.setWorkNo("HH-HS-012");

        Result<SysUser> result = authController.createUser(newUser);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData().getCreatedAt());
        assertEquals(1, result.getData().getStatus());
        assertEquals("123456", result.getData().getPassword());
        verify(userMapper, times(1)).insert(newUser);
    }

    @Test
    @DisplayName("测试为用户分配角色")
    void testAssignRoles() {
        Result<String> result = authController.assignRoles(1L, Arrays.asList(10L, 20L));

        assertEquals(200, result.getCode());
        verify(userRoleMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(userRoleMapper, times(2)).insert(any(SysUserRole.class));
    }
}
