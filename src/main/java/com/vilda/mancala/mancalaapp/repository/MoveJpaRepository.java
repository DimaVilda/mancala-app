package com.vilda.mancala.mancalaapp.repository;

import com.vilda.mancala.mancalaapp.domain.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MoveJpaRepository extends JpaRepository<Move, String> {

    boolean existsByParticipantMancalaGameIdAndParticipantId(String mancalaGameId, String participantId);

    @Query("select m.moveNumber from Move m " +
            "where m.participant.id = :participantId " +
            "and m.participant.mancalaGame.id = :mancalaGameId ")
    int findLastMoveNumberInCurrentGameByParticipantMancalaGameIdAndParticipantId(@Param("mancalaGameId") String mancalaGameId,
                                                                                  @Param("participantId") String participantId);
}
