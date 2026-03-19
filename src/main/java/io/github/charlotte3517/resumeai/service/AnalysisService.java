package io.github.charlotte3517.resumeai.service;

import io.github.charlotte3517.resumeai.client.AiClient;
import io.github.charlotte3517.resumeai.client.AiResponse;
import io.github.charlotte3517.resumeai.client.ChatMessage;
import io.github.charlotte3517.resumeai.dto.request.AnalyzeResumeRequest;
import io.github.charlotte3517.resumeai.dto.response.AnalysisResponse;
import io.github.charlotte3517.resumeai.entity.Analysis;
import io.github.charlotte3517.resumeai.entity.Resume;
import io.github.charlotte3517.resumeai.exception.ResourceNotFoundException;
import io.github.charlotte3517.resumeai.repository.AnalysisRepository;
import io.github.charlotte3517.resumeai.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final String SYSTEM_PROMPT = """
            You are an experienced recruiter and resume reviewer.
            Compare the candidate resume with the job description if provided.
            Be concise, actionable, and professional.
            """;

    private final ResumeRepository resumeRepository;
    private final AnalysisRepository analysisRepository;
    private final AiClient aiClient;

    @Transactional
    public AnalysisResponse analyze(UUID resumeId, AnalyzeResumeRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId));

        AnalyzeResumeRequest safeRequest =
                request != null ? request : new AnalyzeResumeRequest(null, null);

        String userPrompt = buildPrompt(
                resume.getContent(),
                safeRequest.jobDescription(),
                safeRequest.customInstructions()
        );

        AiResponse aiResponse = aiClient.chat(List.of(
                new ChatMessage("system", SYSTEM_PROMPT),
                new ChatMessage("user", userPrompt)
        ));

        Analysis analysis = Analysis.builder()
                .resume(resume)
                .content(aiResponse.content())
                .tokensUsed(aiResponse.tokensUsed())
                .build();

        return toResponse(analysisRepository.saveAndFlush(analysis));
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> listAnalyses(UUID resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId));

        return analysisRepository.findAllByResumeOrderByCreatedAtDesc(resume).stream()
                .map(this::toResponse)
                .toList();
    }

    private String buildPrompt(String resumeContent, String jobDescription, String customInstructions) {
        StringBuilder sb = new StringBuilder();

        if (jobDescription != null && !jobDescription.isBlank()) {
            sb.append("Job Description:\n")
                    .append(jobDescription)
                    .append("\n\n");

            sb.append("Candidate Resume:\n")
                    .append(resumeContent)
                    .append("\n\n");

            sb.append("Tasks:\n")
                    .append("1. Give a match score (0-100)\n")
                    .append("2. List key matching skills\n")
                    .append("3. List missing skills\n")
                    .append("4. Provide improvement suggestions\n");
        } else {
            sb.append("Candidate Resume:\n")
                    .append(resumeContent)
                    .append("\n\n");

            sb.append("Tasks:\n")
                    .append("1. Provide an overall assessment\n")
                    .append("2. List key strengths\n")
                    .append("3. List areas for improvement\n")
                    .append("4. Provide improvement suggestions\n");
        }

        if (customInstructions != null && !customInstructions.isBlank()) {
            sb.append("\nAdditional instructions:\n")
                    .append(customInstructions)
                    .append("\n");
        }

        return sb.toString();
    }

    private AnalysisResponse toResponse(Analysis a) {
        return new AnalysisResponse(
                a.getId(),
                a.getResume().getId(),
                a.getContent(),
                a.getTokensUsed(),
                a.getCreatedAt()
        );
    }
}