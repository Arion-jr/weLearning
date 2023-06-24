package com.stridi.weLearning.repository;

import com.stridi.weLearning.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscussionRepository extends JpaRepository<Lesson, Long> {

	Optional<Lesson> findByIdAndDeletedFalse(Long id);

	Page<Lesson> findAllByProfessorIdAndDeletedFalse(Long professorId, Pageable pageable);

}
