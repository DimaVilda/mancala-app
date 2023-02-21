package com.vilda.mancala.mancalaapp.repository;


import com.vilda.mancala.mancalaapp.domain.PlayerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerAccountJpaRepository extends JpaRepository<PlayerAccount, String> {
}
