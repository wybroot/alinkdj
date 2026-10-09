package com.honghe.party.compliance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceCheckResult {
    private boolean passed;
    private String alertType; // "success", "warning", "danger"
    private String ruleTitle;
    private String description;
    private String referenceClause; // 政策条例出处

    public static ComplianceCheckResult pass(String msg) {
        return new ComplianceCheckResult(true, "success", "合规校验通过", msg, "《中国共产党发展党员工作细则》");
    }

    public static ComplianceCheckResult warn(String title, String msg, String clause) {
        return new ComplianceCheckResult(true, "warning", title, msg, clause);
    }

    public static ComplianceCheckResult block(String title, String msg, String clause) {
        return new ComplianceCheckResult(false, "danger", title, msg, clause);
    }
}
