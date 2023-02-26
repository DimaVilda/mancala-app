package com.vilda.mancala.mancalaapp.repository;

import com.vilda.mancala.mancalaapp.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantJpaRepository extends JpaRepository<Participant, String> {

    @Query("select p.playerNumber from Participant p " +
            "where p.id = :participantId " +
            "and p.mancalaGame.id = :gameId")
    Optional<Integer> findParticipantNumberByIdAndGameId(@Param("participantId") String participantId,
                                                        @Param("gameId") String gameId);
}
