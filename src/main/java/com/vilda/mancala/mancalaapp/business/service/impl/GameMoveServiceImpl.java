package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.GameEndService;
import com.vilda.mancala.mancalaapp.business.service.GameMoveService;
import com.vilda.mancala.mancalaapp.business.service.NextMoveDefinitionService;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.repository.TableCurrentStateRepository;
import com.vilda.mancala.mancalaapp.util.MancalaBoardSetupUtils;
import com.vilda.mancala.mancalaapp.util.MoveEntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.PLAYER_ONE_BIG_STONE_INDEX;
import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.PLAYER_TWO_BIG_STONE_INDEX;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameMoveServiceImpl implements GameMoveService {

    private final TableCurrentStateRepository tableCurrentStateRepository;
    private final MancalaBoardSetupUtils mancalaBoardSetupUtils;
    private final MoveEntityUtils moveEntityUtils;
    private final GameEndService gameEndService;
    private final NextMoveDefinitionService nextMoveDefinitionService;
    private final TableCurrentStatePersistenceService tableCurrentStatePersistenceService;

    @Override
    public MancalaBoardSetup makeMove(MancalaGame mancalaGame, Integer pitIndex, String gameCurrentParticipantId,
                                      boolean isCurrentParticipantFirst) {
        String gameId = mancalaGame.getId();
        log.debug("Trying to make a move in game {} from index {} by {}", gameId, pitIndex, gameCurrentParticipantId);

        TableCurrentState tableCurrentStateByProvidedPit =
                tableCurrentStatePersistenceService.findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, pitIndex);

        Integer currentStonesCountInPit = tableCurrentStateByProvidedPit.getStonesCountInPit();
        if (currentStonesCountInPit == 0) {
            log.error("No stones in pit {}", pitIndex);

            throw new BadRequestException("Chosen pit is empty, please chose pit with at least one stone inside!");
        }
        tableCurrentStateByProvidedPit.setStonesCountInPit(0); //update current pits table state, put stones to 0 cause we took all stones from it
        tableCurrentStateRepository.save(tableCurrentStateByProvidedPit);

        List<Integer> pitIndexesListToPlaceOneStone = getPitIndexList(currentStonesCountInPit, pitIndex, isCurrentParticipantFirst);
        int lastPitIndex = pitIndexesListToPlaceOneStone.get(pitIndexesListToPlaceOneStone.size() - 1);
        //boolean isLastIsBig = lastPitIndex == PLAYER_ONE_BIG_STONE_INDEX || lastPitIndex == PLAYER_TWO_BIG_STONE_INDEX;

        //batch select for less sql performance
        List<TableCurrentState> tableCurrentStatesByMancalaGameAndNextPitIndexes =
                tableCurrentStateRepository.findTableCurrentStatesByMancalaGameAndPitPitIndexIn(gameId, pitIndexesListToPlaceOneStone);

        TableCurrentState tableCurrentStateForLastStone = tableCurrentStatesByMancalaGameAndNextPitIndexes.stream()
                .filter(tcs -> tcs.getPit().getPitIndex() == lastPitIndex)
                .findFirst()
                .get(); // to check If the last piece player drop

        // is in an empty pocket on your side, you capture that piece and any pieces in the pocket directly opposite and put to big pit
        tableCurrentStatesByMancalaGameAndNextPitIndexes.remove(tableCurrentStateForLastStone);

        for (TableCurrentState tableCurrentState : tableCurrentStatesByMancalaGameAndNextPitIndexes) {
            tableCurrentStatePersistenceService.saveTableCurrentStateStonesCount(tableCurrentState, tableCurrentState.getStonesCountInPit() + 1);
        }

        boolean currentGameParticipantNextMove = nextMoveDefinitionService.isCurrentGameParticipantNextMove(tableCurrentStateForLastStone,
                mancalaGame, gameId, isCurrentParticipantFirst, gameCurrentParticipantId);
        //create Move entity for logging game purposes
        moveEntityUtils.createMoveEntity(gameId, gameCurrentParticipantId, 0, tableCurrentStateByProvidedPit.getPit().getId(),
                tableCurrentStateForLastStone.getPit().getId(), currentStonesCountInPit);

        // check if all pits are empty after move above in current participant game table setup
        if (mancalaGame.getGameStatus() == GameStatesEnum.IN_PROGRESS) {
            log.debug("Mancala game status is IN_PROGRESS so let's check if pits are empty after updating game table state entities");

            boolean allGameParticipantPitsPitsAreEmpty = tableCurrentStateRepository.arePitsEmptyByGameIdAndParticipantId(gameId, gameCurrentParticipantId); //another options
            if (allGameParticipantPitsPitsAreEmpty) { //if true, the player who still has stones in his pits keeps them and puts them in his big pit
                log.debug("All pit of the game participant {} are empty", gameCurrentParticipantId);

                gameEndService.defineGameWinner(mancalaGame, gameCurrentParticipantId,
                        isCurrentParticipantFirst, pitIndex, tableCurrentStateForLastStone.getPit().getPitIndex());
                return mancalaBoardSetupUtils.getGameBoardSetupResponseBody(mancalaGame, gameCurrentParticipantId, pitIndex,
                        tableCurrentStateForLastStone.getPit().getPitIndex(), "0"); //end of the game
            }

        }
        mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
        String participantIdNextMove = defineParticipantIdNextMove(currentGameParticipantNextMove, isCurrentParticipantFirst,
                mancalaGame, gameCurrentParticipantId);
        return mancalaBoardSetupUtils.getGameBoardSetupResponseBody(mancalaGame, gameCurrentParticipantId, pitIndex,
                tableCurrentStateForLastStone.getPit().getPitIndex(), participantIdNextMove);
    }

    private List<Integer> getPitIndexList(Integer currentStonesCountInPit, int pitIndex, boolean isCurrentParticipantFirst) {
        List<Integer> pitIndexesList = new ArrayList<>();
        for (int i = 1; i <= currentStonesCountInPit; i++) {
            //if index is greater than last pit but some stones still remains to put, we start from beginning
            pitIndex++; //we start define pits from next pit
            if (pitIndex > PLAYER_TWO_BIG_STONE_INDEX) {
                pitIndex = 0;
            }
            if (isCurrentParticipantFirst) {
                if (pitIndex != PLAYER_TWO_BIG_STONE_INDEX) {
                    pitIndexesList.add(pitIndex);
                } else {
                    pitIndex = 0;
                    pitIndexesList.add(pitIndex);
                }
            } else {
                if (pitIndex != PLAYER_ONE_BIG_STONE_INDEX) {
                    pitIndexesList.add(pitIndex);
                } else {
                    pitIndex++;
                    pitIndexesList.add(pitIndex);
                }
            }
        }
        return pitIndexesList;
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
}
