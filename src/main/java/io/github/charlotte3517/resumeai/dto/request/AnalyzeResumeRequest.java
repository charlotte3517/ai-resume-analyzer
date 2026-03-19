package io.github.charlotte3517.resumeai.dto.request;

public record AnalyzeResumeRequest(
        String jobDescription,
        String customInstructions
) {}
