package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.NextMoveDefinitionService;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NextMoveDefinitionServiceImpl implements NextMoveDefinitionService {

    private final TableCurrentStatePersistenceService tableCurrentStatePersistenceService;

    // TODO test return value!!
    @Override
    public boolean isCurrentGameParticipantNextMove(TableCurrentState tableCurrentStateForLastStone,
                                                    MancalaGame mancalaGame, String gameId,
                                                    boolean isCurrentParticipantFirst, String gameCurrentParticipantId) {

        // if last stone you drop is in your empty pit and this pit is NOT big pit - we capture that stone + all stones in opposite pit and move to his big pit
        if (tableCurrentStateForLastStone.getStonesCountInPit() == 0 && tableCurrentStateForLastStone.getPit().getIsBigPit() == 0 &&
                tableCurrentStateForLastStone.getPit().getParticipant().getPlayerNumber() == (isCurrentParticipantFirst ? 1 : 2)) { //and if last table state pit index belongs to current player number
            log.debug("Last pit state {} stones count is 0 and pit is not big so no chance for next move again now",
                    tableCurrentStateForLastStone);

            int oppositePitIndex = MANCALA_PITS_QUANTITY - tableCurrentStateForLastStone.getPit().getPitIndex() - 1; //calculate opposite pit index based on mancala pits count
            log.debug("opposite pit index number is {}", oppositePitIndex);

            TableCurrentState tableCurrentStateOfTheOppositePit =
                    tableCurrentStatePersistenceService.findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, oppositePitIndex);

            int allStonesInOppositePitPlusOne = tableCurrentStateOfTheOppositePit.getStonesCountInPit(); //grab all stones from opposite pit already updated by + 1 above
            if (tableCurrentStateOfTheOppositePit.getStonesCountInPit() != 0) { //check if there is stones in opposite pit cause if there is no stones we will not waste performance to update it
                tableCurrentStatePersistenceService.saveTableCurrentStateStonesCount(tableCurrentStateOfTheOppositePit, 0);
            }

            TableCurrentState tableCurrentStateOfPlayerBigPit;
            if (isCurrentParticipantFirst) { //if plays participant one, we should find his big pit under index 6
                log.debug("Current game participant is first");

                tableCurrentStateOfPlayerBigPit =
                        tableCurrentStatePersistenceService.findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, PLAYER_ONE_BIG_STONE_INDEX);
            } else { //else if is second participant plays game, we should find his big pit on index 13
                log.debug("Current game participant is second");

                tableCurrentStateOfPlayerBigPit =
                        tableCurrentStatePersistenceService.findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, PLAYER_TWO_BIG_STONE_INDEX);
            }
            tableCurrentStatePersistenceService.saveTableCurrentStateStonesCount(tableCurrentStateOfPlayerBigPit,
                    tableCurrentStateOfPlayerBigPit.getStonesCountInPit() + allStonesInOppositePitPlusOne);

            mancalaGame.setLastParticipantIdMove(gameCurrentParticipantId);
            mancalaGame.setSecondTurn(0); //no second turn for this current participant
            return true;

        } else if (tableCurrentStateForLastStone.getPit().getIsBigPit() == 1) { //if last stone in current state will be in big pit so the player has second turn to move
            log.debug("Current last pit in table state {} is big so current game participant can make next move again",
                    tableCurrentStateForLastStone);

            tableCurrentStatePersistenceService
                    .saveTableCurrentStateStonesCount(tableCurrentStateForLastStone, tableCurrentStateForLastStone.getStonesCountInPit() + 1);

            mancalaGame.setLastParticipantIdMove(gameCurrentParticipantId);
            mancalaGame.setSecondTurn(1); //set second turn for next move for the current game participant
            return false;

        } else { //else it last stone in table current state is has no empty pit and this pit is NOT big - so it's simple stone and simple move
            log.debug("Current last pit in table state {} is not big so no chance for next move again now",
                    tableCurrentStateForLastStone);

            tableCurrentStatePersistenceService
                    .saveTableCurrentStateStonesCount(tableCurrentStateForLastStone, tableCurrentStateForLastStone.getStonesCountInPit() + 1);

            mancalaGame.setLastParticipantIdMove(gameCurrentParticipantId);
            mancalaGame.setSecondTurn(0); //no second turn for this current participant
            return true;
        }
    }
}
