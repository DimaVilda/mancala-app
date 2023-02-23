package com.vilda.mancala.mancalaapp.repository;

import com.vilda.mancala.mancalaapp.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantJpaRepository extends JpaRepository<Participant, String> {

    Optional<Participant> findByMancalaGameIdAndPlayerNumber(String gameId, Integer playerNumber);
}
