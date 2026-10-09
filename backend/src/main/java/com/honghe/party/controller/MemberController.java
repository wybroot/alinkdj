package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.compliance.ComplianceCheckResult;
import com.honghe.party.compliance.PartyComplianceEngine;
import com.honghe.party.entity.PartyMember;
import com.honghe.party.entity.PartyStepRecord;
import com.honghe.party.mapper.PartyMemberMapper;
import com.honghe.party.mapper.PartyStepRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private PartyMemberMapper memberMapper;

    @Autowired
    private PartyStepRecordMapper stepRecordMapper;

    @Autowired
    private PartyComplianceEngine complianceEngine;

    @Autowired
    private com.honghe.party.service.RosterImportService rosterImportService;

    /**
     * 【所有党员花名册】下载标准 Excel 导入模板
     */
    @GetMapping("/roster/template")
    public org.springframework.http.ResponseEntity<byte[]> downloadRosterTemplate() throws java.io.IOException {
        byte[] bytes = rosterImportService.generateTemplateExcel();
        String encodedName = java.net.URLEncoder.encode("红河数据产业集团_党员花名册导入模板.xlsx", java.nio.charset.StandardCharsets.UTF_8);
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(bytes);
    }

    /**
     * 【所有党员花名册】批量上传导入 Excel
     */
    @PostMapping("/roster/import")
    public Result<com.honghe.party.dto.RosterImportResultDTO> importRoster(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        com.honghe.party.dto.RosterImportResultDTO result = rosterImportService.importRosterFromExcel(file);
        return Result.success("花名册解析完成", result);
    }

    /**
     * 【所有党员花名册】单条新增录入党员
     */
    @PostMapping("/roster/add")
    public Result<PartyMember> addMember(@RequestBody PartyMember member) {
        try {
            PartyMember saved = rosterImportService.addSingleMember(member);
            return Result.success("成功录入党员信息！", saved);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 【党员信息修改/更新接口】支持更新档案、党籍状态、组织关系转接时间及支部等
     */
    @PutMapping("/{id}")
    public Result<PartyMember> updateMember(@PathVariable Long id, @RequestBody PartyMember updateForm) {
        PartyMember exist = memberMapper.selectById(id);
        if (exist == null) {
            return Result.error(404, "未找到该党员档案");
        }
        if (updateForm.getRealName() != null) exist.setRealName(updateForm.getRealName());
        if (updateForm.getIdCard() != null) exist.setIdCard(updateForm.getIdCard());
        if (updateForm.getGender() != null) exist.setGender(updateForm.getGender());
        if (updateForm.getBirthDate() != null) exist.setBirthDate(updateForm.getBirthDate());
        if (updateForm.getEducation() != null) exist.setEducation(updateForm.getEducation());
        if (updateForm.getOrgId() != null) exist.setOrgId(updateForm.getOrgId());
        if (updateForm.getDeptName() != null) exist.setDeptName(updateForm.getDeptName());
        if (updateForm.getJobTitle() != null) exist.setJobTitle(updateForm.getJobTitle());
        if (updateForm.getPartyStatus() != null) exist.setPartyStatus(updateForm.getPartyStatus());
        if (updateForm.getPartyPost() != null) exist.setPartyPost(updateForm.getPartyPost());
        if (updateForm.getPartyStandingYears() != null) exist.setPartyStandingYears(updateForm.getPartyStandingYears());
        if (updateForm.getJoinPartyDate() != null) exist.setJoinPartyDate(updateForm.getJoinPartyDate());
        if (updateForm.getOfficialPartyDate() != null) exist.setOfficialPartyDate(updateForm.getOfficialPartyDate());
        if (updateForm.getDuesStatus() != null) exist.setDuesStatus(updateForm.getDuesStatus());
        if (updateForm.getNationalPartyCode() != null) exist.setNationalPartyCode(updateForm.getNationalPartyCode());
        if (updateForm.getAnnualStudyHours() != null) exist.setAnnualStudyHours(updateForm.getAnnualStudyHours());
        if (updateForm.getIsFrontline() != null) exist.setIsFrontline(updateForm.getIsFrontline());
        if (updateForm.getIsTechnicalTalent() != null) exist.setIsTechnicalTalent(updateForm.getIsTechnicalTalent());
        if (updateForm.getIsDualCultivate() != null) exist.setIsDualCultivate(updateForm.getIsDualCultivate());
        
        // 组织关系转接时间及支部
        if (updateForm.getOriginBranch() != null) exist.setOriginBranch(updateForm.getOriginBranch());
        if (updateForm.getTransferInDate() != null) exist.setTransferInDate(updateForm.getTransferInDate());
        if (updateForm.getTransferOutDate() != null) exist.setTransferOutDate(updateForm.getTransferOutDate());
        if (updateForm.getTransferOutBranch() != null) exist.setTransferOutBranch(updateForm.getTransferOutBranch());

        // 根据入党时间自动重新核算党龄
        if (exist.getJoinPartyDate() != null) {
            int autoYears = java.time.Period.between(exist.getJoinPartyDate(), java.time.LocalDate.now()).getYears();
            exist.setPartyStandingYears(Math.max(0, autoYears));
        }

        exist.setUpdatedAt(LocalDateTime.now());
        memberMapper.updateById(exist);
        return Result.success("党员档案信息更新成功！", exist);
    }

    /**
     * 【所有党员花名册】查询接口（全量在册党员，支持政治面貌、党内职务、党龄、党费等多维检索）
     */
    @GetMapping("/roster")
    public Result<List<PartyMember>> getRoster(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer partyStatus,
            @RequestParam(required = false) Integer duesStatus,
            @RequestParam(required = false) Boolean isFrontline,
            @RequestParam(required = false) Boolean isTechnicalTalent,
            @RequestParam(required = false) Boolean isDualCultivate,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<PartyMember> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null) {
            wrapper.eq(PartyMember::getOrgId, orgId);
        }
        if (partyStatus != null) {
            wrapper.eq(PartyMember::getPartyStatus, partyStatus);
        }
        if (duesStatus != null) {
            wrapper.eq(PartyMember::getDuesStatus, duesStatus);
        }
        if (isFrontline != null) {
            wrapper.eq(PartyMember::getIsFrontline, isFrontline);
        }
        if (isTechnicalTalent != null) {
            wrapper.eq(PartyMember::getIsTechnicalTalent, isTechnicalTalent);
        }
        if (isDualCultivate != null) {
            wrapper.eq(PartyMember::getIsDualCultivate, isDualCultivate);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(q -> q.like(PartyMember::getRealName, keyword)
                              .or().like(PartyMember::getWorkNo, keyword)
                              .or().like(PartyMember::getDeptName, keyword)
                              .or().like(PartyMember::getPartyPost, keyword));
        }
        wrapper.orderByAsc(PartyMember::getPartyStatus)
               .orderByDesc(PartyMember::getPartyStandingYears)
               .orderByAsc(PartyMember::getId);
        return Result.success("获取所有党员花名册成功", memberMapper.selectList(wrapper));
    }

    /**
     * 查询发展成员列表（支持按党组织、阶段、工号姓名模糊筛选）
     */
    @GetMapping("/list")
    public Result<List<PartyMember>> listMembers(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer stageId,
            @RequestParam(required = false) String keyword) {
        
        LambdaQueryWrapper<PartyMember> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null) {
            wrapper.eq(PartyMember::getOrgId, orgId);
        }
        if (stageId != null) {
            wrapper.eq(PartyMember::getCurrentStage, stageId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(q -> q.like(PartyMember::getRealName, keyword)
                              .or().like(PartyMember::getWorkNo, keyword)
                              .or().like(PartyMember::getJobTitle, keyword));
        }
        wrapper.orderByAsc(PartyMember::getId);
        return Result.success(memberMapper.selectList(wrapper));
    }

    /**
     * 获取成员详情档案与流转历史
     */
    @GetMapping("/{id}")
    public Result<PartyMember> getMemberDetail(@PathVariable Long id) {
        PartyMember member = memberMapper.selectById(id);
        if (member == null) {
            return Result.error(404, "未找到该成员档案");
        }
        return Result.success(member);
    }

    /**
     * 对特定成员拟推进的目标步骤执行【党务合规审计体检】
     */
    @GetMapping("/{id}/audit")
    public Result<ComplianceCheckResult> auditAdvance(@PathVariable Long id, @RequestParam int targetStep) {
        PartyMember member = memberMapper.selectById(id);
        if (member == null) {
            return Result.error(404, "未找到该成员档案");
        }
        ComplianceCheckResult auditResult = complianceEngine.auditStepAdvance(member, targetStep);
        return Result.success(auditResult);
    }

    /**
     * 审核推进至下一步骤 (集成合规引擎阻断拦截)
     */
    @PostMapping("/{id}/advance-step")
    public Result<?> advanceStep(
            @PathVariable Long id,
            @RequestParam int nextStep,
            @RequestParam(required = false) String auditOpinion,
            @RequestParam(required = false) String handlerName) {

        PartyMember member = memberMapper.selectById(id);
        if (member == null) {
            return Result.error(404, "未找到该成员档案");
        }

        // 1. 严格触发合规防错引擎校验
        ComplianceCheckResult auditResult = complianceEngine.auditStepAdvance(member, nextStep);
        if (!auditResult.isPassed()) {
            return Result.error(400, "【合规防错引擎阻断】" + auditResult.getDescription());
        }

        // 2. 更新成员步骤状态
        member.setCurrentStep(nextStep);
        if (nextStep >= 4 && nextStep <= 8) {
            member.setCurrentStage(2);
        } else if (nextStep >= 9 && nextStep <= 14) {
            member.setCurrentStage(3);
        } else if (nextStep >= 15 && nextStep <= 21) {
            member.setCurrentStage(4);
        } else if (nextStep >= 22) {
            member.setCurrentStage(5);
        }
        member.setUpdatedAt(LocalDateTime.now());
        memberMapper.updateById(member);

        // 3. 记录全流程办理审计日志
        PartyStepRecord record = new PartyStepRecord();
        record.setMemberId(member.getId());
        record.setStepCode(nextStep);
        record.setStepName("推进至第 " + nextStep + " 步");
        record.setHandlerName(handlerName != null ? handlerName : "党务系统经办人");
        record.setFinishTime(LocalDateTime.now());
        record.setAuditStatus(2); // 审核通过
        record.setAuditOpinion(auditOpinion);
        stepRecordMapper.insert(record);

        return Result.success("成功办结并推进至第 " + nextStep + " 步！", member);
    }
}
