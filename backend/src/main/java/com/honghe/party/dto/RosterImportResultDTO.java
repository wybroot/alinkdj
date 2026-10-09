package com.honghe.party.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RosterImportResultDTO {
    private int totalRows;
    private int successCount;
    private int failCount;
    private List<String> errorMessages = new ArrayList<>();
    private List<String> importedNames = new ArrayList<>();
}
