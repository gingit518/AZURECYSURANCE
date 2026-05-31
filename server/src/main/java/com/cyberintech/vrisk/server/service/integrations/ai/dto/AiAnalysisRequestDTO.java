package com.cyberintech.vrisk.server.service.integrations.ai.dto;

import lombok.Data;

/**
 * AI Analysis Request DTO
 *
 * @author   Ariel Evans <Ariel@risk-q.com>
 * @version  0.1.0
 * @since    2026-05-31
 */
@Data
public class AiAnalysisRequestDTO {

    private Long organizationId;
    private String analysisType;
    private String sourceTable;
    private Long sourceRecordId;
    private String inputText;
}