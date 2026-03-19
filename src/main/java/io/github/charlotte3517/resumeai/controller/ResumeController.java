package io.github.charlotte3517.resumeai.controller;

import io.github.charlotte3517.resumeai.dto.request.SubmitResumeRequest;
import io.github.charlotte3517.resumeai.dto.response.ResumeResponse;
import io.github.charlotte3517.resumeai.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<ResumeResponse> submit(@RequestBody @Valid SubmitResumeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.submitResume(request));
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> list() {
        return ResponseEntity.ok(resumeService.listResumes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(resumeService.getResume(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }
}
