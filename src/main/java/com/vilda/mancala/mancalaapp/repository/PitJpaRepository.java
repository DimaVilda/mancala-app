package com.vilda.mancala.mancalaapp.repository;

import com.vilda.mancala.mancalaapp.domain.Pit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitJpaRepository extends JpaRepository<Pit, String> {
}
