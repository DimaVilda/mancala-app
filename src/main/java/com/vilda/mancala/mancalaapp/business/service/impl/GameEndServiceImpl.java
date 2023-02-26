package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.GameEndService;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.TableCurrentStateRepository;
import com.vilda.mancala.mancalaapp.util.MancalaBoardSetupUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameEndServiceImpl implements GameEndService {

    private final TableCurrentStateRepository tableCurrentStateRepository;
    private final MancalaBoardSetupUtils mancalaBoardSetupUtils;
    private final TableCurrentStatePersistenceService tableCurrentStatePersistenceService;
    @Override
    public MancalaBoardSetup defineGameWinner(MancalaGame mancalaGame, String gameCurrentParticipantId,
                                              boolean isCurrentParticipantFirst, TableCurrentState tableCurrentStateForLastStone,
                                              int pitIndex) {
        String gameId = mancalaGame.getId();

        int currentGameParticipantStonesCountInBigPit = tableCurrentStateRepository.findStonesCountInPitByGameIdAndParticipantId(gameId, gameCurrentParticipantId, 1);

        List<TableCurrentState> oppositeParticipantTableCurrentStates = tableCurrentStateRepository.findTableCurrentStatesByMancalaGameIdAndNotParticipantId(gameId, gameCurrentParticipantId);
        if (oppositeParticipantTableCurrentStates.isEmpty()) {
            log.error("");

            throw new NotFoundException("Table current states was not found by provided gameId " + gameId + " and opposite participantId");
        }

        int oppositeParticipantAllPitsStonesSum = 0;
        TableCurrentState oppositeParticipantBigPitTableCurrentState =
                oppositeParticipantTableCurrentStates.stream().filter(tblState -> tblState.getPit().getIsBigPit() == 1).findFirst().get();

        //remove big pit from opposite game participant table states cause it will serve only for keep stones
        oppositeParticipantTableCurrentStates.remove(oppositeParticipantBigPitTableCurrentState);

        //loop opposite player table current states and grab all stones from each pit and put them to his big pit
        //update all opposite player pits
        for (TableCurrentState tableCurrentState : oppositeParticipantTableCurrentStates) {
            oppositeParticipantAllPitsStonesSum += tableCurrentState.getStonesCountInPit();
            tableCurrentStatePersistenceService.saveTableCurrentStateStonesCount(tableCurrentState, 0);
        }

        //update opposite game participant big pit by whole picked up stones from his small pits
        tableCurrentStatePersistenceService.saveTableCurrentStateStonesCount(oppositeParticipantBigPitTableCurrentState,
                oppositeParticipantBigPitTableCurrentState.getStonesCountInPit() + oppositeParticipantAllPitsStonesSum);

        //calculate two players stones from their big pits to identify a winner
        int result = currentGameParticipantStonesCountInBigPit - oppositeParticipantBigPitTableCurrentState.getStonesCountInPit();
        if (result > 0) { //current game participant wins
            mancalaGame.setGameStatus(
                    isCurrentParticipantFirst ? GameStatesEnum.PARTICIPANT_ONE_WINS : GameStatesEnum.PARTICIPANT_TWO_WINS);
        } else if (result < 0) { //his opposite wins
            mancalaGame.setGameStatus(
                    isCurrentParticipantFirst ? GameStatesEnum.PARTICIPANT_TWO_WINS : GameStatesEnum.PARTICIPANT_ONE_WINS);
        } else {
            mancalaGame.setGameStatus(GameStatesEnum.DRAW);
        }
        return mancalaBoardSetupUtils.getGameBoardSetupResponseBody(mancalaGame, gameCurrentParticipantId, pitIndex,
                tableCurrentStateForLastStone.getPit().getPitIndex(), "0"); //end of the game
    }
}
