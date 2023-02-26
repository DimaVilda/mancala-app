package com.vilda.mancala.mancalaapp.util;

import com.vilda.mancala.mancalaapp.domain.Move;
import com.vilda.mancala.mancalaapp.repository.MoveJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class MoveEntityUtils {

    private final MoveJpaRepository moveJpaRepository;

    public void createMoveEntity(String gameId, String gameCurrentParticipantId, int isFixed, String fromPitId, String toPitId, Integer currentStonesCountInPit) {
        Move move = new Move();


          Optional<Integer> lastMoveNumber = moveJpaRepository.findLastMoveNumberInCurrentGameByParticipantMancalaGameIdAndParticipantId(
                gameId, gameCurrentParticipantId);

          if (lastMoveNumber.isPresent()) {
              move.setMoveNumber(lastMoveNumber.get() + 1);
          } else {
              move.setMoveNumber(1);
          }

        move.setParticipantId(gameCurrentParticipantId);
        move.setIsFixed(isFixed);
        move.setFromPitId(fromPitId);
        move.setToPitId(toPitId);
        move.setStonesCountInHand(currentStonesCountInPit);
        moveJpaRepository.save(move);
    }
}
