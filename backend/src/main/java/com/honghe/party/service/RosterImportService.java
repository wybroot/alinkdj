package com.honghe.party.service;

import com.honghe.party.dto.RosterImportResultDTO;
import com.honghe.party.entity.PartyMember;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface RosterImportService {
    /**
     * 生成并下载党员花名册标准导入 Excel 模板
     */
    byte[] generateTemplateExcel() throws IOException;

    /**
     * 解析上传的 Excel 文件并批量录入党员花名册
     */
    RosterImportResultDTO importRosterFromExcel(MultipartFile file) throws IOException;

    /**
     * 单条录入新增党员信息
     */
    PartyMember addSingleMember(PartyMember member);
}
