package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.GameEndService;
import com.vilda.mancala.mancalaapp.business.service.GameMoveService;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.TableCurrentStateRepository;
import com.vilda.mancala.mancalaapp.util.MancalaBoardSetupUtils;
import com.vilda.mancala.mancalaapp.util.MoveEntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameMoveServiceImpl implements GameMoveService {

    private final TableCurrentStateRepository tableCurrentStateRepository;
    private final MancalaBoardSetupUtils mancalaBoardSetupUtils;
    private final MoveEntityUtils moveEntityUtils;
    private final GameEndService gameEndService;

    @Override
    public MancalaBoardSetup makeMove(MancalaGame mancalaGame, String gameId, Integer pitIndex, Participant gameCurrentParticipant,
                                      boolean isCurrentParticipantFirst) {
        log.debug("");

        boolean isParticipantIdNextMove = false;
        String gameCurrentParticipantId = gameCurrentParticipant.getId();
        TableCurrentState tableCurrentStateByProvidedPit = findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, pitIndex);

        Integer currentStonesCountInPit = tableCurrentStateByProvidedPit.getStonesCountInPit();
        if (currentStonesCountInPit == 0) {
            log.error("");

            throw new BadRequestException("Chosen pit is empty, please chose pit with at least one stone inside!"); // TODO is it really bad request ?
        }
        tableCurrentStateByProvidedPit.setStonesCountInPit(0); //update current pits table state, put stones to 0 cause we took all stones from it
        tableCurrentStateRepository.save(tableCurrentStateByProvidedPit);

        Set<Integer> pitIndexesSetToPlaceOneStone = new HashSet<>();
        for (int i = 1; i <= currentStonesCountInPit; i++) {
            // check if we do not put stone in our game partner's big pit
            if (isCurrentParticipantFirst) {
                if (pitIndex + i != PLAYER_TWO_BIG_STONE_INDEX) { // player one cannot put stone in his partner's big pit
                    pitIndexesSetToPlaceOneStone.add(pitIndex + i);
                }
            } else { //means that player two makes this move
                if (pitIndex + i != PLAYER_ONE_BIG_STONE_INDEX) { // player two cannot put stone in his partner's big pit
                    pitIndexesSetToPlaceOneStone.add(pitIndex + i);
                }
            }
        }

        //batch select for less sql performance
        List<TableCurrentState> tableCurrentStatesByMancalaGameAndNextPitIndexes =
                tableCurrentStateRepository.findTableCurrentStatesByMancalaGameAndPitPitIndexIn(gameId, pitIndexesSetToPlaceOneStone);
        if (tableCurrentStatesByMancalaGameAndNextPitIndexes.isEmpty()) {
            log.error("");

            throw new NotFoundException("Table current states was not found by provided gameId " + gameId + " and pitIndexes " + pitIndexesSetToPlaceOneStone);
        }

        int lastTableCurrentStateIndex = tableCurrentStatesByMancalaGameAndNextPitIndexes.size() - 1;
        TableCurrentState tableCurrentStateForLastStone = tableCurrentStatesByMancalaGameAndNextPitIndexes.get(lastTableCurrentStateIndex); // to check If the last piece player drop
        // is in an empty pocket on your side, you capture that piece and any pieces in the pocket directly opposite and put to big pit
        tableCurrentStatesByMancalaGameAndNextPitIndexes.remove(lastTableCurrentStateIndex);

        for (TableCurrentState tableCurrentState : tableCurrentStatesByMancalaGameAndNextPitIndexes) {
            saveTableCurrentStateStonesCount(tableCurrentState, tableCurrentState.getStonesCountInPit() + 1);
        }

        // if last stone you drop is in empty pit and this pit is NOT big pit - we capture that stone + all stones in opposite pit and move to his big pit
        if (tableCurrentStateForLastStone.getStonesCountInPit() == 0 && tableCurrentStateForLastStone.getPit().getIsBigPit() == 0) {
            log.debug("");

            int oppositePitIndex = MANCALA_PITS_QUANTITY - tableCurrentStateForLastStone.getPit().getPitIndex() - 1; //calculate opposite pit index based on mancala pits count
            TableCurrentState tableCurrentStateOfTheOppositePit = findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, oppositePitIndex);

            int allStonesInOppositePitPlusOne = tableCurrentStateOfTheOppositePit.getStonesCountInPit() + 1; //grab all stones from opposite pit PLUS last stone in the pit to drop all it to the big pit
            if (tableCurrentStateOfTheOppositePit.getStonesCountInPit() != 0) { //check if there is stones in opposite pit cause if there is no stones we will not waste performance to update it
                saveTableCurrentStateStonesCount(tableCurrentStateOfTheOppositePit, 0);
            }

            TableCurrentState tableCurrentStateOfPlayerBigPit;
            if (isCurrentParticipantFirst) { //if plays participant one, we should find his big pit under index 6
                log.debug("");

                tableCurrentStateOfPlayerBigPit = findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, PLAYER_ONE_BIG_STONE_INDEX);
            } else { //else if is second participant plays game, we should find his big pit on index 13
                log.debug("");

                tableCurrentStateOfPlayerBigPit = findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, PLAYER_TWO_BIG_STONE_INDEX);
            }
            saveTableCurrentStateStonesCount(tableCurrentStateOfPlayerBigPit,
                    tableCurrentStateOfPlayerBigPit.getStonesCountInPit() + allStonesInOppositePitPlusOne);

            mancalaGame.setLastParticipantIdMove(gameCurrentParticipantId);
            mancalaGame.setSecondTurn(0); //no second turn for this current participant
            isParticipantIdNextMove = true;

        } else if (tableCurrentStateForLastStone.getPit().getIsBigPit() == 1) { //if last stone in current state will be in big pit so the player has second turn to move
            saveTableCurrentStateStonesCount(tableCurrentStateForLastStone, tableCurrentStateForLastStone.getStonesCountInPit() + 1);

            mancalaGame.setLastParticipantIdMove(gameCurrentParticipantId);
            mancalaGame.setSecondTurn(1); //set second turn for next move for the current game participant

        } else { //else it last stone in table current state is has no empty pit and this pit is NOT big - so it's simple stone and simple move
            saveTableCurrentStateStonesCount(tableCurrentStateForLastStone, tableCurrentStateForLastStone.getStonesCountInPit() + 1);

            mancalaGame.setLastParticipantIdMove(gameCurrentParticipantId);
            mancalaGame.setSecondTurn(0); //no second turn for this current participant
            isParticipantIdNextMove = true;
        }

        log.debug("");
        //create Move entity for logging game purposes
        moveEntityUtils.createMoveEntity(gameId, gameCurrentParticipant, 0, tableCurrentStateByProvidedPit.getPit().getId(),
                tableCurrentStateForLastStone.getPit().getId(), currentStonesCountInPit);

        //TODO test purposes so delete after!
        //set table states to 0 in current player
        if (mancalaGame.getGameStatus() == GameStatesEnum.IN_PROGRESS) {
            if (!tableCurrentStateRepository.arePitsEmptyByGameIdAndParticipantId(gameId, gameCurrentParticipantId)) { //do it once only when pits are not empty
                List<TableCurrentState> testTableStatesOfCurrentPlayer =
                        tableCurrentStateRepository.testTableStatesByMancalaGameIdAndParticipantId(gameId, gameCurrentParticipantId);
                for (TableCurrentState t : testTableStatesOfCurrentPlayer) {
                    if (t.getPit().getIsBigPit() != 1) {
                        t.setStonesCountInPit(0);
                    }
                    tableCurrentStateRepository.save(t);
                }
            }
        }
        //TODO test purposes so delete after!

        // check if all pits are empty after move above in current participant game table setup
        if (mancalaGame.getGameStatus() == GameStatesEnum.IN_PROGRESS) {
            log.debug("");

            boolean allGameParticipantPitsPitsAreEmpty = tableCurrentStateRepository.arePitsEmptyByGameIdAndParticipantId(gameId, gameCurrentParticipantId); //another options
            if (allGameParticipantPitsPitsAreEmpty) { //if true, the player who still has stones in his pits keeps them and puts them in his big pit
                log.debug("");

                gameEndService.defineGameWinner(mancalaGame, gameCurrentParticipant,
                        isCurrentParticipantFirst, tableCurrentStateForLastStone, pitIndex);
                return mancalaBoardSetupUtils.getGameBoardSetupResponseBody(mancalaGame, gameCurrentParticipant, pitIndex,
                        tableCurrentStateForLastStone.getPit().getPitIndex(), "0"); //end of the game
            }

        }
        mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
        String participantIdNextMove = defineParticipantIdNextMove(isParticipantIdNextMove, isCurrentParticipantFirst,
                mancalaGame, gameCurrentParticipantId);
        return mancalaBoardSetupUtils.getGameBoardSetupResponseBody(mancalaGame, gameCurrentParticipant, pitIndex,
                tableCurrentStateForLastStone.getPit().getPitIndex(), participantIdNextMove);
    }

    private String defineParticipantIdNextMove(boolean isParticipantIdNextMove, boolean isCurrentParticipantIsFirst,
                                               MancalaGame mancalaGame, String gameCurrentParticipantId) {
        if (isParticipantIdNextMove) {
            return mancalaGame.getParticipantSet().stream()
                    .filter(participant -> participant.getPlayerNumber() == (isCurrentParticipantIsFirst ? 2 : 1))
                    .findFirst()
                    .get()
                    .getId();
        } else {
            return gameCurrentParticipantId;
        }
    }

    private void saveTableCurrentStateStonesCount(TableCurrentState tableCurrentState, int pitStonesCount) {
        tableCurrentState.setStonesCountInPit(pitStonesCount);
        tableCurrentStateRepository.save(tableCurrentState);
    }

    private TableCurrentState findTableCurrentStateByMancalaGameIdAndPitIndex(String gameId, Integer pitIndex) {
        return tableCurrentStateRepository.findTableCurrentStateByMancalaGameIdAndPitPitIndex(gameId, pitIndex).orElseThrow(() -> { //TODO is it better to find by gameId and pitId cause it can be a lot table states if this game has several tables
            log.error("");

            return new NotFoundException("No table current state by provided gameId " + gameId + " and pitIndex " + pitIndex);
        });
    }
}
