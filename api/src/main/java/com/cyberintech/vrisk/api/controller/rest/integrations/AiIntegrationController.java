package com.cyberintech.vrisk.api.controller.rest.integrations;

import com.cyberintech.vrisk.server.service.integrations.ai.AiIntegrationService;
import com.cyberintech.vrisk.server.service.integrations.ai.dto.AiAnalysisRequestDTO;
import com.cyberintech.vrisk.server.service.integrations.ai.dto.AiAnalysisResultDTO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;

/**
 * AI Integration Controller - exposes AI analysis endpoints
 *
 * @author   Ariel Evans <Ariel@risk-q.com>
 * @version  0.1.0
 * @since    2026-05-31
 */
@RestController
@RequestMapping(
        value = AiIntegrationController.CONTROLLER_URI,
        produces = MediaType.APPLICATION_JSON,
        name = "AI Integration Controller"
)
@Tag(name = "AI Integration")
@Slf4j
@RequiredArgsConstructor
public class AiIntegrationController {

    static final String CONTROLLER_URI = "/api/ai";

    private final AiIntegrationService aiIntegrationService;

    /**
     * Summarize a finding and get remediation steps
     */
    @RequestMapping(method = RequestMethod.POST, value = "/summarize-finding",
            name = "Summarize finding and generate remediation steps",
            consumes = {MediaType.APPLICATION_JSON})
    @Parameters({
            @Parameter(name = "authorization", description = "oAuth Access token for API calls",
                    example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
    })
    public AiAnalysisResultDTO summarizeFinding(
            @Parameter(description = "AI Analysis Request", required = true)
            @RequestBody AiAnalysisRequestDTO request
    ) {
        return aiIntegrationService.summarizeFinding(request);
    }

    /**
     * Generate executive risk report