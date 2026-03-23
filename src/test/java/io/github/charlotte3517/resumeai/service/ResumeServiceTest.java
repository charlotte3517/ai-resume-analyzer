package io.github.charlotte3517.resumeai.service;

import io.github.charlotte3517.resumeai.dto.request.SubmitResumeRequest;
import io.github.charlotte3517.resumeai.dto.response.ResumeResponse;
import io.github.charlotte3517.resumeai.entity.Resume;
import io.github.charlotte3517.resumeai.exception.ResourceNotFoundException;
import io.github.charlotte3517.resumeai.repository.ResumeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private ResumeService resumeService;

    @Test
    void submitResume_success() {
        SubmitResumeRequest request = new SubmitResumeRequest("My Resume", "5 years of experience...");

        Resume saved = Resume.builder()
                .id(UUID.randomUUID())
                .title("My Resume")
                .content("5 years of experience...")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(resumeRepository.saveAndFlush(any(Resume.class))).thenReturn(saved);

        ResumeResponse response = resumeService.submitResume(request);

        assertThat(response.title()).isEqualTo("My Resume");
        assertThat(response.content()).isEqualTo("5 years of experience...");
        verify(resumeRepository).saveAndFlush(any(Resume.class));
    }

    @Test
    void getResume_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(resumeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeService.getResume(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void listResumes_returnsMappedList() {
        List<Resume> resumes = List.of(
                Resume.builder().id(UUID.randomUUID()).title("Resume A").content("...").
                        createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                Resume.builder().id(UUID.randomUUID()).title("Resume B").content("...").
                        createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        );
        when(resumeRepository.findAllByOrderByCreatedAtDesc()).thenReturn(resumes);

        List<ResumeResponse> responses = resumeService.listResumes();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).title()).isEqualTo("Resume A");
        assertThat(responses.get(1).title()).isEqualTo("Resume B");
    }

    @Test
    void deleteResume_notFound_throwsExceptionAndDoesNotDelete() {
        UUID id = UUID.randomUUID();
        when(resumeRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> resumeService.deleteResume(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(resumeRepository, never()).deleteById(any());
    }
}
