package com.honghe.party.compliance;

import com.honghe.party.entity.PartyMember;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class PartyComplianceEngine {

    /**
     * 对成员当前状态及目标推进步骤进行党务合规体检
     */
    public ComplianceCheckResult auditStepAdvance(PartyMember member, int targetStep) {
        LocalDate today = LocalDate.now();

        // 规则 1: 申请人谈话红线 (递交后 1 个月内完成谈话)
        if (targetStep == 2 && member.getApplyDate() != null) {
            long daysPassed = ChronoUnit.DAYS.between(member.getApplyDate(), today);
            if (daysPassed > 30) {
                return ComplianceCheckResult.warn(
                    "申请谈话超期预警",
                    "入党申请书已递交 " + daysPassed + " 天，已超过《细则》规定的 1 个月内谈话时限，请立即组织支部谈话并补齐说明！",
                    "《中国共产党发展党员工作细则》第八条"
                );
            }
        }

        // 规则 2: 【硬阻断】积极分子培养考察期必须满 1 年 (365天)，未满严禁确定为发展对象 (步骤9及以后)
        if (targetStep >= 9 && member.getCurrentStage() <= 2) {
            if (member.getActivistDate() == null) {
                return ComplianceCheckResult.block(
                    "考察期硬阻断",
                    "该同志尚未记录确定为入党积极分子的备案日期，无法推进至发展对象阶段！",
                    "《中国共产党发展党员工作细则》第十三条"
                );
            }
            long cultivateDays = ChronoUnit.DAYS.between(member.getActivistDate(), today);
            if (cultivateDays < 365) {
                return ComplianceCheckResult.block(
                    "考察不足1年强制锁死",
                    "该同志作为入党积极分子培养考察仅 " + cultivateDays + " 天（未满法定 365 天）。依据法规，考察期不足一年不得列为发展对象，系统强行阻断流转！",
                    "《中国共产党发展党员工作细则》第十三条：入党积极分子经过一年以上培养教育和考察、基本具备党员条件的，方可列为发展对象。"
                );
            }
        }

        // 规则 3: 发展对象必须通过政治审查与纪检廉洁从业把关 (步骤13及以后)
        if (targetStep >= 14) {
            if (member.getDisciplineCheckPass() == null || member.getDisciplineCheckPass() != 1) {
                return ComplianceCheckResult.block(
                    "纪检把关未通过阻断",
                    "该同志尚未取得集团纪检监察部门出具的《廉洁从业审查意见书》或廉洁审核未通过，一票否决，不得进入集中培训和接收程序！",
                    "《中国共产党国有企业基层组织工作条例（试行）》"
                );
            }
        }

        // 规则 4: 集中短期培训必须考核合格 (不少于24学时)
        if (targetStep >= 15 && member.getExamPassDate() == null) {
            return ComplianceCheckResult.block(
                "培训未合格阻断",
                "该同志尚未完成不少于 3 天或 24 学时的党校发展对象集中短期培训，或未上传考核合格凭证，不得列入支委会预审与公示！",
                "《中国共产党发展党员工作细则》第十七条"
            );
        }

        // 规则 5: 预备期必须满 1 年方可讨论转正 (步骤24)
        if (targetStep >= 24) {
            if (member.getProbationaryDate() == null) {
                return ComplianceCheckResult.block(
                    "预备期未满阻断",
                    "尚未登记支部大会接收为预备党员日期，无法进入转正程序！",
                    "《中国共产党章程》第七条"
                );
            }
            long probationDays = ChronoUnit.DAYS.between(member.getProbationaryDate(), today);
            if (probationDays < 365) {
                return ComplianceCheckResult.block(
                    "预备期未满 1 年阻断",
                    "预备期仅 " + probationDays + " 天（未满整 1 年），不得提前召开支部大会讨论表决转正！",
                    "《中国共产党章程》第七条：预备党员的预备期为一年。"
                );
            }
        }

        return ComplianceCheckResult.pass("该步骤各项前置时限与合规材料审核通过，准予流转。");
    }
}
