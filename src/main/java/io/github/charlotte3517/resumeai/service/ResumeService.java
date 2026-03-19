package io.github.charlotte3517.resumeai.service;

import io.github.charlotte3517.resumeai.dto.request.SubmitResumeRequest;
import io.github.charlotte3517.resumeai.dto.response.ResumeResponse;
import io.github.charlotte3517.resumeai.entity.Resume;
import io.github.charlotte3517.resumeai.exception.ResourceNotFoundException;
import io.github.charlotte3517.resumeai.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Transactional
    public ResumeResponse submitResume(SubmitResumeRequest request) {
        Resume resume = Resume.builder()
                .title(request.title())
                .content(request.content())
                .build();
        return toResponse(resumeRepository.saveAndFlush(resume));
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(UUID id) {
        return resumeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> listResumes() {
        return resumeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteResume(UUID id) {
        if (!resumeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resume not found: " + id);
        }
        resumeRepository.deleteById(id);
    }

    private ResumeResponse toResponse(Resume r) {
        return new ResumeResponse(r.getId(), r.getTitle(), r.getContent(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
