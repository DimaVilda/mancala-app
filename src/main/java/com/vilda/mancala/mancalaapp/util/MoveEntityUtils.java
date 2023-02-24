package com.vilda.mancala.mancalaapp.util;

import com.vilda.mancala.mancalaapp.domain.Move;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.repository.MoveJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MoveEntityUtils {

    private final MoveJpaRepository moveJpaRepository;

    public void createMoveEntity(String gameId, Participant gameCurrentParticipant, int isFixed, String fromPitId, String toPitId, Integer currentStonesCountInPit) {
        Move move = new Move();

        if (!moveJpaRepository.existsByParticipantMancalaGameIdAndParticipantId(gameId, gameCurrentParticipant.getId())) { //if no moves at all
            move.setMoveNumber(1); //so we define first move in curren tgame story
        } else { //is some moves exist by gameId so we should find the last move number and increment it
            int lastMoveNumberForParticipantInCurrentGame =
                    moveJpaRepository.findLastMoveNumberInCurrentGameByParticipantMancalaGameIdAndParticipantId(
                            gameId, gameCurrentParticipant.getId());
            move.setMoveNumber(lastMoveNumberForParticipantInCurrentGame + 1);
        }
        move.setParticipant(gameCurrentParticipant);
        move.setIsFixed(isFixed);
        move.setFromPitId(fromPitId);
        move.setToPitId(toPitId);
        move.setStonesCountInHand(currentStonesCountInPit);
        moveJpaRepository.save(move);
    }
}
