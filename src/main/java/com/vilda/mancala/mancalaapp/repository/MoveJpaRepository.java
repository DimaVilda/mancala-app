package com.vilda.mancala.mancalaapp.repository;

import com.vilda.mancala.mancalaapp.domain.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MoveJpaRepository extends JpaRepository<Move, String> {

    @Query(value = "select max(m.moveNumber) from Move m " +
            "join Participant p ON m.participantId = p.id " +
            "where m.participantId = :participantId " +
            "and p.mancalaGame.id = :mancalaGameId")
    Optional<Integer> findLastMoveNumberInCurrentGameByParticipantMancalaGameIdAndParticipantId(@Param("mancalaGameId") String mancalaGameId,
                                                                                                @Param("participantId") String participantId);

}
