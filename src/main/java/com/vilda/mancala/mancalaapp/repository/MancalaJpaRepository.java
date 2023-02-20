package com.vilda.mancala.mancalaapp.repository;

import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MancalaJpaRepository extends JpaRepository<MancalaGame, String> {
}
