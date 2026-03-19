package io.github.charlotte3517.resumeai.controller;

import io.github.charlotte3517.resumeai.dto.request.AnalyzeResumeRequest;
import io.github.charlotte3517.resumeai.dto.response.AnalysisResponse;
import io.github.charlotte3517.resumeai.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes/{resumeId}/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(
            @PathVariable("resumeId") UUID resumeId,
            @RequestBody(required = false) AnalyzeResumeRequest request
    ) {
        AnalyzeResumeRequest req = Optional.ofNullable(request)
                .orElse(new AnalyzeResumeRequest(null, null));

        AnalysisResponse response = analysisService.analyze(resumeId, req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AnalysisResponse>> list(
            @PathVariable("resumeId") UUID resumeId
    ) {
        return ResponseEntity.ok(analysisService.listAnalyses(resumeId));
    }
}
