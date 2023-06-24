package com.stridi.weLearning.service.authentication;

import com.stridi.weLearning.repository.AccountRepository;
import com.stridi.weLearning.service.jwt.JWTAuthenticationService;
import com.stridi.weLearning.service.jwt.JWTService;
import com.stridi.weLearning.utils.object.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProfessorAuthenticationService {
	private final JWTService jwtService;
	private final AccountRepository accountRepository;
	private final JWTAuthenticationService authenticationService;

	public String login(String username, String password) {
		return accountRepository
				.findByRoleAndUsernameAndPassword(Role.Professor, username, password)
				.map(user -> jwtService.create(Role.Professor, username, password))
				.orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
	}

	public void reset(String username) {
		authenticationService.reset(Role.Professor, username);
	}

	public void changePassword(String username, String oldPassword, String newPassword) {
		authenticationService.changePassword(Role.Professor, username, oldPassword, newPassword);
	}

}
