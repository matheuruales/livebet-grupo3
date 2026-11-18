package com.grupo3.betting.api;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
