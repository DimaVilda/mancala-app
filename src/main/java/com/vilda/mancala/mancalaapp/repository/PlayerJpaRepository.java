package com.vilda.mancala.mancalaapp.repository;

import com.vilda.mancala.mancalaapp.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerJpaRepository extends JpaRepository<Player, String> {
}
