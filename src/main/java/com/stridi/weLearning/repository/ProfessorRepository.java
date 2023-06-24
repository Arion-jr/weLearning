package com.stridi.weLearning.repository;

import com.stridi.weLearning.entity.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

	Optional<Professor> findByAccountId(Long accountId);

	Page<Professor> findAll(Pageable pageable);

}