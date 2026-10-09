package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.PartyMember;
import com.honghe.party.entity.PartyOrg;
import com.honghe.party.mapper.PartyMemberMapper;
import com.honghe.party.mapper.PartyOrgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/screen")
public class ScreenController {

    @Autowired
    private PartyMemberMapper memberMapper;

    @Autowired
    private PartyOrgMapper orgMapper;

    /**
     * 智慧党建大屏综合指标聚合接口
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getScreenOverview() {
        Map<String, Object> data = new HashMap<>();

        // 1. 基础数据统计
        List<PartyMember> allMembers = memberMapper.selectList(null);
        List<PartyOrg> allOrgs = orgMapper.selectList(null);

        long totalFormal = allMembers.stream().filter(m -> m.getPartyStatus() != null && m.getPartyStatus() == 1).count();
        long totalProbationary = allMembers.stream().filter(m -> m.getPartyStatus() != null && m.getPartyStatus() == 2).count();
        long totalTarget = allMembers.stream().filter(m -> m.getPartyStatus() != null && m.getPartyStatus() == 3).count();
        long totalActivist = allMembers.stream().filter(m -> m.getPartyStatus() != null && m.getPartyStatus() == 4).count();
        long totalApplicant = allMembers.stream().filter(m -> m.getPartyStatus() != null && m.getPartyStatus() == 5).count();

        data.put("totalMembers", allMembers.size());
        data.put("formalCount", totalFormal);
        data.put("probationaryCount", totalProbationary);
        data.put("developingCount", totalTarget + totalActivist + totalApplicant);
        data.put("orgCount", allOrgs.size());

        // 2. 国企特色结构统计
        long frontlineCount = allMembers.stream().filter(m -> Boolean.TRUE.equals(m.getIsFrontline())).count();
        long technicalCount = allMembers.stream().filter(m -> Boolean.TRUE.equals(m.getIsTechnicalTalent())).count();
        long dualCount = allMembers.stream().filter(m -> Boolean.TRUE.equals(m.getIsDualCultivate())).count();

        data.put("frontlineCount", frontlineCount);
        data.put("frontlineRatio", allMembers.isEmpty() ? 0 : Math.round((double) frontlineCount / allMembers.size() * 100));
        data.put("technicalCount", technicalCount);
        data.put("technicalRatio", allMembers.isEmpty() ? 0 : Math.round((double) technicalCount / allMembers.size() * 100));
        data.put("dualCultivateCount", dualCount);

        // 3. 25步发展流转漏斗
        Map<String, Long> funnel = new LinkedHashMap<>();
        funnel.put("申请入党人库", totalApplicant);
        funnel.put("确定积极分子", totalActivist);
        funnel.put("列为发展对象", totalTarget);
        funnel.put("预备党员接收", totalProbationary);
        funnel.put("正式党员转正", totalFormal);
        data.put("funnelStats", funnel);

        // 4. 三大子公司支部进度
        List<Map<String, Object>> branchProgress = new ArrayList<>();
        branchProgress.add(Map.of("name", "红数信息技术支部", "total", 4, "done", 3, "rate", 75, "tag", "政务云运维与安全先锋"));
        branchProgress.add(Map.of("name", "云南幂次科技支部", "total", 5, "done", 5, "rate", 100, "tag", "算力算法青年攻关先锋"));
        branchProgress.add(Map.of("name", "红河链达科技支部", "total", 4, "done", 3, "rate", 75, "tag", "数据要素流通创新先锋"));
        branchProgress.add(Map.of("name", "集团直属机关小组", "total", 2, "done", 1, "rate", 50, "tag", "企划党群综合保障"));
        data.put("branchProgress", branchProgress);

        return Result.success("获取大屏数据成功", data);
    }
}
