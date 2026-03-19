package io.github.charlotte3517.resumeai.repository;

import io.github.charlotte3517.resumeai.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    List<Resume> findAllByOrderByCreatedAtDesc();
}
