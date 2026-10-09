package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.PartyAnnualQuota;
import com.honghe.party.mapper.PartyAnnualQuotaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quota")
public class QuotaController {

    @Autowired
    private PartyAnnualQuotaMapper quotaMapper;

    @GetMapping("/current")
    public Result<PartyAnnualQuota> getCurrentQuota(@RequestParam(defaultValue = "2025") Integer year) {
        PartyAnnualQuota quota = quotaMapper.selectOne(
            new LambdaQueryWrapper<PartyAnnualQuota>().eq(PartyAnnualQuota::getYearVal, year).last("LIMIT 1")
        );
        return Result.success("获取指标数据成功", quota);
    }
}
