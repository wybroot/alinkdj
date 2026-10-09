package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.PartyHonorPunishment;
import com.honghe.party.mapper.PartyHonorPunishmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/honor-punishment")
public class HonorPunishmentController {

    @Autowired
    private PartyHonorPunishmentMapper honorPunishmentMapper;

    /**
     * 查询奖惩/荣誉台账列表 (支持按分类:1个人/2组织，类型:1荣誉/2处分，党组织，级别及关键字检索)
     */
    @GetMapping("/list")
    public Result<List<PartyHonorPunishment>> list(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer recordType,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<PartyHonorPunishment> wrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            wrapper.eq(PartyHonorPunishment::getCategory, category);
        }
        if (recordType != null) {
            wrapper.eq(PartyHonorPunishment::getRecordType, recordType);
        }
        if (orgId != null) {
            wrapper.eq(PartyHonorPunishment::getOrgId, orgId);
        }
        if (level != null && !level.isBlank()) {
            wrapper.eq(PartyHonorPunishment::getLevel, level);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(q -> q.like(PartyHonorPunishment::getTitle, keyword)
                              .or().like(PartyHonorPunishment::getTargetName, keyword)
                              .or().like(PartyHonorPunishment::getDocNo, keyword));
        }
        wrapper.orderByDesc(PartyHonorPunishment::getRecordDate)
               .orderByDesc(PartyHonorPunishment::getId);
        return Result.success("获取奖惩/荣誉台账成功", honorPunishmentMapper.selectList(wrapper));
    }

    /**
     * 新增奖惩/荣誉记录
     */
    @PostMapping("/add")
    public Result<PartyHonorPunishment> add(@RequestBody PartyHonorPunishment entity) {
        if (entity.getCategory() == null || entity.getRecordType() == null || entity.getTitle() == null) {
            return Result.error(400, "奖惩分类、类型与名称为必填项！");
        }
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        honorPunishmentMapper.insert(entity);
        return Result.success("奖惩/荣誉台账记录成功录入！", entity);
    }

    /**
     * 修改/编辑奖惩荣誉记录
     */
    @PutMapping("/{id}")
    public Result<PartyHonorPunishment> update(
            @PathVariable Long id,
            @RequestBody PartyHonorPunishment form) {
        PartyHonorPunishment exist = honorPunishmentMapper.selectById(id);
        if (exist == null) {
            return Result.error(404, "未找到该记录");
        }
        if (form.getCategory() != null) exist.setCategory(form.getCategory());
        if (form.getRecordType() != null) exist.setRecordType(form.getRecordType());
        if (form.getTargetName() != null) exist.setTargetName(form.getTargetName());
        if (form.getOrgId() != null) exist.setOrgId(form.getOrgId());
        if (form.getOrgName() != null) exist.setOrgName(form.getOrgName());
        if (form.getTitle() != null) exist.setTitle(form.getTitle());
        if (form.getLevel() != null) exist.setLevel(form.getLevel());
        if (form.getGrantOrg() != null) exist.setGrantOrg(form.getGrantOrg());
        if (form.getDocNo() != null) exist.setDocNo(form.getDocNo());
        if (form.getRecordDate() != null) exist.setRecordDate(form.getRecordDate());
        if (form.getReasonContent() != null) exist.setReasonContent(form.getReasonContent());
        if (form.getAttachmentPath() != null) exist.setAttachmentPath(form.getAttachmentPath());
        exist.setUpdatedAt(LocalDateTime.now());

        honorPunishmentMapper.updateById(exist);
        return Result.success("修改更新成功！", exist);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        honorPunishmentMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}
