package io.github.charlotte3517.resumeai.service;

import io.github.charlotte3517.resumeai.client.AiClient;
import io.github.charlotte3517.resumeai.client.AiResponse;
import io.github.charlotte3517.resumeai.dto.request.AnalyzeResumeRequest;
import io.github.charlotte3517.resumeai.dto.response.AnalysisResponse;
import io.github.charlotte3517.resumeai.entity.Analysis;
import io.github.charlotte3517.resumeai.entity.Resume;
import io.github.charlotte3517.resumeai.exception.ResourceNotFoundException;
import io.github.charlotte3517.resumeai.repository.AnalysisRepository;
import io.github.charlotte3517.resumeai.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private AiClient aiClient;

    @InjectMocks
    private AnalysisService analysisService;

    private Resume resume;

    @BeforeEach
    void setUp() {
        resume = Resume.builder()
                .id(UUID.randomUUID())
                .title("My Resume")
                .content("5 years of Java experience...")
                .build();
    }

    @Test
    void analyze_success_callsAiAndSavesResult() {
        AnalyzeResumeRequest request = new AnalyzeResumeRequest(null, null);

        Analysis savedAnalysis = Analysis.builder()
                .id(UUID.randomUUID())
                .resume(resume)
                .content("Strong Java background. Suggest adding metrics.")
                .tokensUsed(150)
                .createdAt(LocalDateTime.now())
                .build();

        when(resumeRepository.findById(resume.getId())).thenReturn(Optional.of(resume));
        when(aiClient.chat(anyList()))
                .thenReturn(new AiResponse("Strong Java background. Suggest adding metrics.", 150));
        when(analysisRepository.saveAndFlush(any(Analysis.class))).thenReturn(savedAnalysis);

        AnalysisResponse response = analysisService.analyze(resume.getId(), request);

        assertThat(response.content()).isEqualTo("Strong Java background. Suggest adding metrics.");
        assertThat(response.tokensUsed()).isEqualTo(150);

        verify(aiClient).chat(anyList());
        verify(analysisRepository).saveAndFlush(any(Analysis.class));
    }

    @Test
    void analyze_withJobDescriptionAndCustomInstructions_includesThemInPrompt() {
        AnalyzeResumeRequest request = new AnalyzeResumeRequest(
                "Looking for a backend engineer with Java and Spring Boot experience",
                "Focus on leadership skills"
        );

        Analysis savedAnalysis = Analysis.builder()
                .id(UUID.randomUUID())
                .resume(resume)
                .content("Leadership analysis...")
                .tokensUsed(120)
                .createdAt(LocalDateTime.now())
                .build();

        when(resumeRepository.findById(resume.getId())).thenReturn(Optional.of(resume));
        when(aiClient.chat(anyList())).thenReturn(new AiResponse("Leadership analysis...", 120));
        when(analysisRepository.saveAndFlush(any(Analysis.class))).thenReturn(savedAnalysis);

        AnalysisResponse response = analysisService.analyze(resume.getId(), request);

        assertThat(response.content()).isEqualTo("Leadership analysis...");

        verify(aiClient).chat(argThat(messages ->
                messages.stream().anyMatch(m ->
                        m.content().contains("Looking for a backend engineer with Java and Spring Boot experience")
                                && m.content().contains("Focus on leadership skills")
                                && m.content().contains("5 years of Java experience...")
                )
        ));
    }

    @Test
    void analyze_withNullRequest_usesDefaultPrompt() {
        Analysis savedAnalysis = Analysis.builder()
                .id(UUID.randomUUID())
                .resume(resume)
                .content("General resume review...")
                .tokensUsed(100)
                .createdAt(LocalDateTime.now())
                .build();

        when(resumeRepository.findById(resume.getId())).thenReturn(Optional.of(resume));
        when(aiClient.chat(anyList())).thenReturn(new AiResponse("General resume review...", 100));
        when(analysisRepository.saveAndFlush(any(Analysis.class))).thenReturn(savedAnalysis);

        AnalysisResponse response = analysisService.analyze(resume.getId(), null);

        assertThat(response.content()).isEqualTo("General resume review...");

        verify(aiClient).chat(argThat(messages ->
                messages.stream().anyMatch(m ->
                        m.content().contains("Candidate Resume:")
                                && m.content().contains("5 years of Java experience...")
                )
        ));
    }

    @Test
    void analyze_resumeNotFound_throwsException() {
        UUID resumeId = UUID.randomUUID();
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analysisService.analyze(resumeId, new AnalyzeResumeRequest(null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(resumeId.toString());

        verify(aiClient, never()).chat(any());
        verify(analysisRepository, never()).saveAndFlush(any());
    }

    @Test
    void listAnalyses_returnsAllForResume() {
        List<Analysis> analyses = List.of(
                Analysis.builder()
                        .id(UUID.randomUUID())
                        .resume(resume)
                        .content("Analysis 1")
                        .tokensUsed(100)
                        .createdAt(LocalDateTime.now())
                        .build(),
                Analysis.builder()
                        .id(UUID.randomUUID())
                        .resume(resume)
                        .content("Analysis 2")
                        .tokensUsed(200)
                        .createdAt(LocalDateTime.now().minusMinutes(1))
                        .build()
        );

        when(resumeRepository.findById(resume.getId())).thenReturn(Optional.of(resume));
        when(analysisRepository.findAllByResumeOrderByCreatedAtDesc(resume)).thenReturn(analyses);

        List<AnalysisResponse> responses = analysisService.listAnalyses(resume.getId());

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).content()).isEqualTo("Analysis 1");
        assertThat(responses.get(1).content()).isEqualTo("Analysis 2");
    }
}