package com.stridi.weLearning.repository;

import com.stridi.weLearning.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

	Optional<Student> findByAccountId(Long accountId);

}
