package com.honghe.party.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.honghe.party.dto.RosterImportResultDTO;
import com.honghe.party.entity.PartyMember;
import com.honghe.party.entity.PartyOrg;
import com.honghe.party.mapper.PartyMemberMapper;
import com.honghe.party.mapper.PartyOrgMapper;
import com.honghe.party.service.RosterImportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RosterImportServiceImpl implements RosterImportService {

    @Autowired
    private PartyMemberMapper memberMapper;

    @Autowired
    private PartyOrgMapper orgMapper;

    private static final String[] HEADERS = {
        "姓名*", "员工工号*", "身份证号*", "性别", "出生日期",
        "最高学历", "所属党组织名称*", "企业行政部门", "岗位职务", "政治面貌*",
        "党内职务", "党龄(年)", "接收预备党员日期", "正式转正日期", "全国党员编码",
        "生产经营一线(是/否)", "数字技术骨干(是/否)", "双培养对象(是/否)"
    };

    @Override
    public byte[] generateTemplateExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("党员花名册导入模板");

            // 样式设置
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 标题行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 示例数据行
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("李卫民");
            sampleRow.createCell(1).setCellValue("HH-HS-001");
            sampleRow.createCell(2).setCellValue("532501198009214444");
            sampleRow.createCell(3).setCellValue("男");
            sampleRow.createCell(4).setCellValue("1980-09-21");
            sampleRow.createCell(5).setCellValue("大学本科");
            sampleRow.createCell(6).setCellValue("中共红河红数信息技术服务有限公司支部委员会");
            sampleRow.createCell(7).setCellValue("管理层");
            sampleRow.createCell(8).setCellValue("总经理");
            sampleRow.createCell(9).setCellValue("正式党员");
            sampleRow.createCell(10).setCellValue("党支部书记");
            sampleRow.createCell(11).setCellValue(18);
            sampleRow.createCell(12).setCellValue("2007-04-18");
            sampleRow.createCell(13).setCellValue("2008-04-18");
            sampleRow.createCell(14).setCellValue("532501001980092104");
            sampleRow.createCell(15).setCellValue("是");
            sampleRow.createCell(16).setCellValue("是");
            sampleRow.createCell(17).setCellValue("是");

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RosterImportResultDTO importRosterFromExcel(MultipartFile file) throws IOException {
        RosterImportResultDTO result = new RosterImportResultDTO();
        if (file.isEmpty()) {
            result.getErrorMessages().add("上传的 Excel 文件为空");
            return result;
        }

        // 预加载组织名称字典
        List<PartyOrg> orgList = orgMapper.selectList(null);
        Map<String, Long> orgNameMap = orgList.stream()
                .collect(Collectors.toMap(PartyOrg::getOrgName, PartyOrg::getId, (k1, k2) -> k1));

        DataFormatter formatter = new DataFormatter();

        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < 1) {
                result.getErrorMessages().add("Excel 中未包含数据行（至少需要有一条成员记录）");
                return result;
            }

            for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                result.setTotalRows(result.getTotalRows() + 1);

                String name = formatter.formatCellValue(row.getCell(0)).trim();
                String workNo = formatter.formatCellValue(row.getCell(1)).trim();
                String idCard = formatter.formatCellValue(row.getCell(2)).trim();
                String genderStr = formatter.formatCellValue(row.getCell(3)).trim();
                String birthStr = formatter.formatCellValue(row.getCell(4)).trim();
                String education = formatter.formatCellValue(row.getCell(5)).trim();
                String orgName = formatter.formatCellValue(row.getCell(6)).trim();
                String deptName = formatter.formatCellValue(row.getCell(7)).trim();
                String jobTitle = formatter.formatCellValue(row.getCell(8)).trim();
                String partyStatusStr = formatter.formatCellValue(row.getCell(9)).trim();
                String partyPost = formatter.formatCellValue(row.getCell(10)).trim();
                String standingStr = formatter.formatCellValue(row.getCell(11)).trim();
                String joinDateStr = formatter.formatCellValue(row.getCell(12)).trim();
                String officialDateStr = formatter.formatCellValue(row.getCell(13)).trim();
                String nationalCode = formatter.formatCellValue(row.getCell(14)).trim();
                String frontlineStr = formatter.formatCellValue(row.getCell(15)).trim();
                String techStr = formatter.formatCellValue(row.getCell(16)).trim();
                String dualStr = formatter.formatCellValue(row.getCell(17)).trim();

                // 校验必填项
                if (StrUtil.isBlank(name) || StrUtil.isBlank(workNo) || StrUtil.isBlank(idCard) || StrUtil.isBlank(orgName)) {
                    result.setFailCount(result.getFailCount() + 1);
                    result.getErrorMessages().add("第 " + (rowNum + 1) + " 行：姓名、工号、身份证号、党组织为必填项！");
                    continue;
                }

                // 匹配党组织
                Long orgId = orgNameMap.get(orgName);
                if (orgId == null) {
                    // 模糊匹配组织
                    for (Map.Entry<String, Long> entry : orgNameMap.entrySet()) {
                        if (entry.getKey().contains(orgName) || orgName.contains(entry.getKey())) {
                            orgId = entry.getValue();
                            break;
                        }
                    }
                    if (orgId == null) {
                        result.setFailCount(result.getFailCount() + 1);
                        result.getErrorMessages().add("第 " + (rowNum + 1) + " 行：未找到匹配的党组织 [" + orgName + "]");
                        continue;
                    }
                }

                // 构建或更新实体
                PartyMember member = memberMapper.selectOne(
                    new LambdaQueryWrapper<PartyMember>().eq(PartyMember::getWorkNo, workNo)
                );
                boolean isNew = (member == null);
                if (isNew) {
                    member = new PartyMember();
                    member.setCreatedAt(LocalDateTime.now());
                }

                member.setRealName(name);
                member.setWorkNo(workNo);
                member.setIdCard(idCard);
                member.setOrgId(orgId);
                member.setDeptName(deptName);
                member.setJobTitle(jobTitle);
                member.setGender("女".equals(genderStr) ? 2 : 1);
                member.setEducation(education);
                member.setPartyPost(StrUtil.isNotBlank(partyPost) ? partyPost : "普通党员");
                member.setNationalPartyCode(nationalCode);
                member.setIsFrontline("是".equals(frontlineStr) || "1".equals(frontlineStr));
                member.setIsTechnicalTalent("是".equals(techStr) || "1".equals(techStr));
                member.setIsDualCultivate("是".equals(dualStr) || "1".equals(dualStr));
                member.setDuesStatus(1);
                member.setUpdatedAt(LocalDateTime.now());

                // 政治面貌映射
                int status = parsePartyStatus(partyStatusStr);
                member.setPartyStatus(status);
                if (status == 1) {
                    member.setCurrentStage(5);
                    member.setCurrentStep(25);
                } else if (status == 2) {
                    member.setCurrentStage(4);
                    member.setCurrentStep(20);
                } else if (status == 3) {
                    member.setCurrentStage(3);
                    member.setCurrentStep(13);
                } else if (status == 4) {
                    member.setCurrentStage(2);
                    member.setCurrentStep(7);
                } else {
                    member.setCurrentStage(1);
                    member.setCurrentStep(2);
                }

                if (StrUtil.isNotBlank(standingStr)) {
                    try {
                        member.setPartyStandingYears(Integer.parseInt(standingStr));
                    } catch (Exception ignored) {}
                }

                // 日期解析
                member.setBirthDate(parseDateSafe(birthStr));
                member.setJoinPartyDate(parseDateSafe(joinDateStr));
                member.setOfficialPartyDate(parseDateSafe(officialDateStr));

                if (isNew) {
                    memberMapper.insert(member);
                } else {
                    memberMapper.updateById(member);
                }

                result.setSuccessCount(result.getSuccessCount() + 1);
                result.getImportedNames().add(name + " (" + workNo + ")");
            }
        }

        return result;
    }

    @Override
    public PartyMember addSingleMember(PartyMember member) {
        if (StrUtil.isBlank(member.getRealName()) || StrUtil.isBlank(member.getWorkNo()) || StrUtil.isBlank(member.getIdCard())) {
            throw new IllegalArgumentException("姓名、员工工号和身份证号不能为空！");
        }
        if (member.getOrgId() == null) {
            throw new IllegalArgumentException("必须选择所属党组织！");
        }

        // 校验工号与身份证唯一性
        Long countNo = memberMapper.selectCount(
            new LambdaQueryWrapper<PartyMember>().eq(PartyMember::getWorkNo, member.getWorkNo())
        );
        if (countNo > 0) {
            throw new IllegalArgumentException("工号 [" + member.getWorkNo() + "] 已存在，请勿重复添加！");
        }

        Long countCard = memberMapper.selectCount(
            new LambdaQueryWrapper<PartyMember>().eq(PartyMember::getIdCard, member.getIdCard())
        );
        if (countCard > 0) {
            throw new IllegalArgumentException("身份证号 [" + member.getIdCard() + "] 已存在！");
        }

        // 自动计算当前阶段
        if (member.getPartyStatus() == null) {
            member.setPartyStatus(1);
        }
        if (member.getPartyStatus() == 1) {
            member.setCurrentStage(5);
            member.setCurrentStep(25);
        } else if (member.getPartyStatus() == 2) {
            member.setCurrentStage(4);
            member.setCurrentStep(20);
        } else if (member.getPartyStatus() == 3) {
            member.setCurrentStage(3);
            member.setCurrentStep(13);
        } else if (member.getPartyStatus() == 4) {
            member.setCurrentStage(2);
            member.setCurrentStep(7);
        } else {
            member.setCurrentStage(1);
            member.setCurrentStep(2);
        }

        member.setDuesStatus(1);
        member.setStatus(1);
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        memberMapper.insert(member);
        return member;
    }

    private int parsePartyStatus(String str) {
        if (StrUtil.isBlank(str)) return 1;
        if (str.contains("预备")) return 2;
        if (str.contains("发展对象")) return 3;
        if (str.contains("积极分子")) return 4;
        if (str.contains("申请人")) return 5;
        return 1; // 默认正式党员
    }

    private LocalDate parseDateSafe(String str) {
        if (StrUtil.isBlank(str)) return null;
        try {
            str = str.replace('/', '-').trim();
            if (str.length() == 10) {
                return LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
