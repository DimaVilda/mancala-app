package com.vilda.mancala.mancalaapp.repository;

import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TableCurrentStateRepository extends JpaRepository<TableCurrentState, String> {

    //same as below but writen manually
/*    @Query("select tcs from TableCurrentState tcs " +
            "join tcs.pit.participant part " +
            "WHERE tcs.mancalaGame.id = :gameId " +
            "AND tcs.pit.pitIndex = :pitIndex")
    Optional<TableCurrentState> findByMancalaGameIdAndPitIndex(@Param("gameId") String gameId,
                                                                       @Param("pitIndex") Integer pitIndex);*/


    Optional<TableCurrentState> findTableCurrentStateByMancalaGameIdAndPitPitIndex(@Param("gameId") String gameId,
                                                                                   @Param("pitIndex") Integer pitIndex);

    @Query("select tcs from TableCurrentState tcs " +
            "join tcs.pit.participant part " +
            "WHERE tcs.mancalaGame.id = :gameId " +
            "AND tcs.pit.pitIndex in :pitIndexesList")
    List<TableCurrentState> findTableCurrentStatesByMancalaGameAndPitPitIndexIn(@Param("gameId") String gameId,
                                                                                @Param("pitIndexesList") Collection<Integer> pitIndexesList);

    @Query("select tcs from TableCurrentState tcs " +
            "join tcs.pit.participant part " +
            "WHERE tcs.mancalaGame.id = :gameId " +
            "AND part.id <> :participantId")
    List<TableCurrentState> findTableCurrentStatesByMancalaGameIdAndNotParticipantId(@Param("gameId") String gameId,
                                                                                     @Param("participantId") String participantId);

    //TODO remove after tests ot leave for it test
    @Query("select tcs from TableCurrentState tcs " +
            "join tcs.pit.participant part " +
            "WHERE tcs.mancalaGame.id = :gameId " +
            "AND part.id = :participantId")
    List<TableCurrentState> testTableStatesByMancalaGameIdAndParticipantId(@Param("gameId") String gameId,
                                                                        @Param("participantId") String participantId);
    //TODO remove after tests ot leave for it test


    @Query("select case when sum(tcs.stonesCountInPit) = 0 then true else false end " +
            "from TableCurrentState tcs " +
            "join tcs.pit.participant part " +
            "WHERE tcs.mancalaGame.id = :gameId " +
            "AND part.id = :participantId " +
            "AND tcs.pit.isBigPit = 0")
    boolean arePitsEmptyByGameIdAndParticipantId(@Param("gameId") String gameId,
                                                 @Param("participantId") String participantId);

    @Query("select tcs.stonesCountInPit from TableCurrentState tcs " +
            "join tcs.pit.participant part " +
            "WHERE tcs.mancalaGame.id = :gameId " +
            "AND part.id = :participantId " +
            "AND tcs.pit.isBigPit = :isBigPit")
    int findStonesCountInPitByGameIdAndParticipantId(@Param("gameId") String gameId,
                                                     @Param("participantId") String participantId,
                                                     @Param("isBigPit") int isBigPit);
}
