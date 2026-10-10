package com.honghe.party.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.common.Result;
import com.honghe.party.entity.PartyMember;
import com.honghe.party.entity.PartyOrg;
import com.honghe.party.entity.PartyPositionAdjustment;
import com.honghe.party.entity.PartyRelationTransfer;
import com.honghe.party.mapper.PartyMemberMapper;
import com.honghe.party.mapper.PartyOrgMapper;
import com.honghe.party.mapper.PartyPositionAdjustmentMapper;
import com.honghe.party.mapper.PartyRelationTransferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
public class TransferController {

    @Autowired
    private PartyRelationTransferMapper transferMapper;

    @Autowired
    private PartyPositionAdjustmentMapper adjustmentMapper;

    @Autowired
    private PartyMemberMapper memberMapper;

    @Autowired
    private PartyOrgMapper orgMapper;

    // ==========================================
    // 1. 党员组织关系转接 (转入 / 转出) 接口
    // ==========================================

    /**
     * 查询组织关系转接记录列表
     */
    @GetMapping("/transfer/list")
    public Result<List<PartyRelationTransfer>> listTransfers(
            @RequestParam(required = false) Integer transferType,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<PartyRelationTransfer> wrapper = new LambdaQueryWrapper<>();
        if (transferType != null) {
            wrapper.eq(PartyRelationTransfer::getTransferType, transferType);
        }
        if (orgId != null) {
            wrapper.and(q -> q.eq(PartyRelationTransfer::getFromOrgId, orgId)
                              .or().eq(PartyRelationTransfer::getToOrgId, orgId));
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(q -> q.like(PartyRelationTransfer::getMemberName, keyword)
                              .or().like(PartyRelationTransfer::getWorkNo, keyword)
                              .or().like(PartyRelationTransfer::getLetterNo, keyword)
                              .or().like(PartyRelationTransfer::getFromOrgName, keyword)
                              .or().like(PartyRelationTransfer::getToOrgName, keyword));
        }
        wrapper.orderByDesc(PartyRelationTransfer::getTransferDate)
               .orderByDesc(PartyRelationTransfer::getId);

        return Result.success(transferMapper.selectList(wrapper));
    }

    /**
     * 办理组织关系转入：详细记录转入信息，并自动同步/恢复党员档案至花名册
     */
    @PostMapping("/transfer/in")
    @Transactional(rollbackFor = Exception.class)
    public Result<PartyRelationTransfer> transferIn(@RequestBody PartyRelationTransfer form) {
        if (form.getMemberName() == null || form.getMemberName().isBlank()) {
            return Result.error(400, "党员姓名不能为空！");
        }
        if (form.getToOrgName() == null || form.getToOrgName().isBlank()) {
            return Result.error(400, "拟转入目标党组织不能为空！");
        }
        if (form.getTransferDate() == null) {
            form.setTransferDate(LocalDate.now());
        }
        form.setTransferType(1); // 1: 转入
        if (form.getApprovalStatus() == null) {
            form.setApprovalStatus(2); // 办结生效
        }

        // 解析转入目标党组织ID
        if (form.getToOrgId() == null) {
            PartyOrg targetOrg = orgMapper.selectOne(
                new LambdaQueryWrapper<PartyOrg>().like(PartyOrg::getOrgName, form.getToOrgName()).last("LIMIT 1")
            );
            if (targetOrg != null) {
                form.setToOrgId(targetOrg.getId());
            } else {
                form.setToOrgId(1L); // 默认集团党总支
            }
        }

        // 同步花名册逻辑：先根据身份证号或工号查验是否已存在
        PartyMember existMember = null;
        if (form.getIdCard() != null && !form.getIdCard().isBlank()) {
            existMember = memberMapper.selectOne(
                new LambdaQueryWrapper<PartyMember>().eq(PartyMember::getIdCard, form.getIdCard().trim()).last("LIMIT 1")
            );
        }
        if (existMember == null && form.getWorkNo() != null && !form.getWorkNo().isBlank()) {
            existMember = memberMapper.selectOne(
                new LambdaQueryWrapper<PartyMember>().eq(PartyMember::getWorkNo, form.getWorkNo().trim()).last("LIMIT 1")
            );
        }

        if (existMember != null) {
            // 已有档案：恢复为在册正常(status = 1)，并更新转入支部及时间
            existMember.setStatus(1);
            existMember.setOrgId(form.getToOrgId());
            existMember.setOriginBranch(form.getFromOrgName());
            existMember.setTransferInDate(form.getTransferDate());
            if (form.getPartyStatus() != null) existMember.setPartyStatus(form.getPartyStatus());
            if (form.getPartyPost() != null) existMember.setPartyPost(form.getPartyPost());
            existMember.setTransferOutDate(null);
            existMember.setTransferOutBranch(null);
            existMember.setUpdatedAt(LocalDateTime.now());
            memberMapper.updateById(existMember);
            form.setMemberId(existMember.getId());
        } else {
            // 全新转入党员：新建花名册在册档案
            PartyMember newMember = new PartyMember();
            newMember.setRealName(form.getMemberName());
            newMember.setIdCard(form.getIdCard() != null && !form.getIdCard().isBlank() ? form.getIdCard() : ("TRANS" + System.currentTimeMillis()));
            newMember.setWorkNo(form.getWorkNo() != null && !form.getWorkNo().isBlank() ? form.getWorkNo() : ("NO-" + System.currentTimeMillis() % 100000));
            newMember.setGender(form.getGender() != null ? form.getGender() : 1);
            newMember.setOrgId(form.getToOrgId());
            newMember.setDeptName(form.getToOrgName());
            newMember.setJobTitle("党员职工");
            newMember.setPartyStatus(form.getPartyStatus() != null ? form.getPartyStatus() : 1);
            newMember.setPartyPost(form.getPartyPost() != null ? form.getPartyPost() : "普通党员");
            newMember.setOriginBranch(form.getFromOrgName());
            newMember.setTransferInDate(form.getTransferDate());
            newMember.setDuesStatus(1);
            newMember.setStatus(1); // 正常在册
            newMember.setCurrentStage(newMember.getPartyStatus() == 1 ? 5 : 4);
            newMember.setCurrentStep(newMember.getPartyStatus() == 1 ? 25 : 20);
            newMember.setCreatedAt(LocalDateTime.now());
            newMember.setUpdatedAt(LocalDateTime.now());
            memberMapper.insert(newMember);
            form.setMemberId(newMember.getId());
        }

        form.setCreatedAt(LocalDateTime.now());
        form.setUpdatedAt(LocalDateTime.now());
        transferMapper.insert(form);

        return Result.success("组织关系转入办理成功！党员档案已同步纳入花名册。", form);
    }

    /**
     * 办理组织关系转出：记录转出信息，并自动从党员花名册中除名（标记为转出状态）
     */
    @PostMapping("/transfer/out")
    @Transactional(rollbackFor = Exception.class)
    public Result<PartyRelationTransfer> transferOut(@RequestBody PartyRelationTransfer form) {
        if (form.getMemberId() == null && (form.getWorkNo() == null || form.getWorkNo().isBlank())) {
            return Result.error(400, "必须指定转出的党员ID或员工工号！");
        }
        if (form.getToOrgName() == null || form.getToOrgName().isBlank()) {
            return Result.error(400, "转出目标党组织不能为空！");
        }
        if (form.getTransferDate() == null) {
            form.setTransferDate(LocalDate.now());
        }
        form.setTransferType(2); // 2: 转出
        if (form.getApprovalStatus() == null) {
            form.setApprovalStatus(2); // 办结生效
        }

        PartyMember member = null;
        if (form.getMemberId() != null) {
            member = memberMapper.selectById(form.getMemberId());
        } else {
            member = memberMapper.selectOne(
                new LambdaQueryWrapper<PartyMember>().eq(PartyMember::getWorkNo, form.getWorkNo()).last("LIMIT 1")
            );
        }

        if (member == null) {
            return Result.error(404, "未找到拟转出的党员档案！");
        }

        form.setMemberId(member.getId());
        form.setMemberName(member.getRealName());
        form.setIdCard(member.getIdCard());
        form.setWorkNo(member.getWorkNo());
        form.setGender(member.getGender());
        form.setPartyStatus(member.getPartyStatus());
        form.setPartyPost(member.getPartyPost());
        form.setFromOrgId(member.getOrgId());
        if (form.getFromOrgName() == null || form.getFromOrgName().isBlank()) {
            PartyOrg fromOrg = orgMapper.selectById(member.getOrgId());
            form.setFromOrgName(fromOrg != null ? fromOrg.getOrgName() : "原所在党支部");
        }

        // 从花名册除名：标记状态为4（调离转出），记录转出时间与目标支部
        member.setStatus(4); // 4: 调离转出 (除名)
        member.setTransferOutDate(form.getTransferDate());
        member.setTransferOutBranch(form.getToOrgName());
        member.setUpdatedAt(LocalDateTime.now());
        memberMapper.updateById(member);

        form.setCreatedAt(LocalDateTime.now());
        form.setUpdatedAt(LocalDateTime.now());
        transferMapper.insert(form);

        return Result.success("组织关系转出办理成功！该党员已从在册花名册中除名。", form);
    }

    /**
     * 删除转接记录
     */
    @DeleteMapping("/transfer/{id}")
    public Result<Void> deleteTransfer(@PathVariable Long id) {
        transferMapper.deleteById(id);
        return Result.success("转接记录删除成功", null);
    }

    // ==========================================
    // 2. 党员党内职务调整备案接口
    // ==========================================

    /**
     * 查询党员职务调整备案记录列表
     */
    @GetMapping("/position-adjustment/list")
    public Result<List<PartyPositionAdjustment>> listAdjustments(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<PartyPositionAdjustment> wrapper = new LambdaQueryWrapper<>();
        if (memberId != null) {
            wrapper.eq(PartyPositionAdjustment::getMemberId, memberId);
        }
        if (orgId != null) {
            wrapper.eq(PartyPositionAdjustment::getOrgId, orgId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(q -> q.like(PartyPositionAdjustment::getMemberName, keyword)
                              .or().like(PartyPositionAdjustment::getDocumentNo, keyword)
                              .or().like(PartyPositionAdjustment::getNewPost, keyword)
                              .or().like(PartyPositionAdjustment::getOrgName, keyword));
        }
        wrapper.orderByDesc(PartyPositionAdjustment::getEffectiveDate)
               .orderByDesc(PartyPositionAdjustment::getId);

        return Result.success(adjustmentMapper.selectList(wrapper));
    }

    /**
     * 登记党员党内职务调整备案：记录批文号与任免信息，并联动更新花名册中的党内职务
     */
    @PostMapping("/position-adjustment/save")
    @Transactional(rollbackFor = Exception.class)
    public Result<PartyPositionAdjustment> saveAdjustment(@RequestBody PartyPositionAdjustment form) {
        if (form.getMemberId() == null) {
            return Result.error(400, "必须指定调整职务的党员！");
        }
        if (form.getNewPost() == null || form.getNewPost().isBlank()) {
            return Result.error(400, "调整后党内职务不能为空！");
        }
        if (form.getDocumentNo() == null || form.getDocumentNo().isBlank()) {
            return Result.error(400, "批准文号不能为空！");
        }
        if (form.getEffectiveDate() == null) {
            form.setEffectiveDate(LocalDate.now());
        }

        PartyMember member = memberMapper.selectById(form.getMemberId());
        if (member == null) {
            return Result.error(404, "未找到该党员档案！");
        }

        form.setMemberName(member.getRealName());
        form.setWorkNo(member.getWorkNo());
        form.setOldPost(member.getPartyPost() != null ? member.getPartyPost() : "普通党员");
        if (form.getOrgId() == null) {
            form.setOrgId(member.getOrgId());
        }
        if (form.getOrgName() == null || form.getOrgName().isBlank()) {
            PartyOrg org = orgMapper.selectById(member.getOrgId());
            form.setOrgName(org != null ? org.getOrgName() : "所属党支部");
        }
        if (form.getApprovalUnit() == null || form.getApprovalUnit().isBlank()) {
            form.setApprovalUnit("中共红河数据产业集团有限公司总支部委员会");
        }

        // 联动更新花名册中的党内职务
        member.setPartyPost(form.getNewPost());
        member.setUpdatedAt(LocalDateTime.now());
        memberMapper.updateById(member);

        form.setCreatedAt(LocalDateTime.now());
        form.setUpdatedAt(LocalDateTime.now());
        adjustmentMapper.insert(form);

        return Result.success("党员党内职务调整备案成功！花名册党内职务已自动联动更新。", form);
    }

    /**
     * 删除职务调整备案记录
     */
    @DeleteMapping("/position-adjustment/{id}")
    public Result<Void> deleteAdjustment(@PathVariable Long id) {
        adjustmentMapper.deleteById(id);
        return Result.success("调整备案记录删除成功", null);
    }
}
