package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.PartyOrg;
import com.honghe.party.mapper.PartyOrgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/org")
public class OrgController {

    @Autowired
    private PartyOrgMapper partyOrgMapper;

    @GetMapping("/tree")
    public Result<List<PartyOrg>> getOrgTree() {
        List<PartyOrg> list = partyOrgMapper.selectList(
            new LambdaQueryWrapper<PartyOrg>().orderByAsc(PartyOrg::getSortOrder)
        );
        return Result.success("获取党组织架构树成功", list);
    }
}
