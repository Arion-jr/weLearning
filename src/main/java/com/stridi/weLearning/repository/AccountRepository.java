package com.stridi.weLearning.repository;

import com.stridi.weLearning.entity.Account;
import com.stridi.weLearning.utils.object.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findByUsername(String username);

	Optional<Account> findByRoleAndUsername(Role role, String username);

	Optional<Account> findByRoleAndUsernameAndPassword(Role role, String username, String password);

}