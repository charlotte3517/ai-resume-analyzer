package io.github.charlotte3517.resumeai.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitResumeRequest(
        @NotBlank(message = "title is required") String title,
        @NotBlank(message = "content is required") String content
) {}
