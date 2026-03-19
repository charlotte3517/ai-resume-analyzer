package io.github.charlotte3517.resumeai.repository;

import io.github.charlotte3517.resumeai.entity.Analysis;
import io.github.charlotte3517.resumeai.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    List<Analysis> findAllByResumeOrderByCreatedAtDesc(Resume resume);
}
