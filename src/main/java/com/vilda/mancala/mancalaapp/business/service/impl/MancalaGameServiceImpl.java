package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.MancalaGameService;
import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.MoveItem;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.*;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.*;
import com.vilda.mancala.mancalaapp.util.GameSetupResponseUtils;
import com.vilda.mancala.mancalaapp.util.MancalaBoardSetupUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MancalaGameServiceImpl implements MancalaGameService {

    private final MancalaJpaRepository mancalaJpaRepository;
    private final PitJpaRepository pitJpaRepository;
    private final PlayerAccountJpaRepository playerJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final TableCurrentStateRepository tableCurrentStateRepository;
    private final MoveJpaRepository moveJpaRepository;
    private final MancalaBoardSetupUtils mancalaBoardSetupUtils;
    private final GameSetupResponseUtils gameSetupResponseUtils;

    @Override
    @Transactional
    public GameSetupResponse startNewGame(NewGameSetup newGameSetup) {
        MancalaGame newMancalaGame = defineGameSetup(newGameSetup);
        return gameSetupResponseUtils.getNewGameSetupResponseBody(newMancalaGame);
    }

    private MancalaGame defineGameSetup(NewGameSetup newGameSetup) {
        log.debug("");

        PlayerAccount playerOne = defineGamePlayerAccount(newGameSetup.getPlayerOneName());
        PlayerAccount playerTwo = defineGamePlayerAccount(newGameSetup.getPlayerTwoName());

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);
        mancalaGame.setSecondTurn(0);
        mancalaGame.setLastParticipantIdMove("0");
        mancalaJpaRepository.save(mancalaGame);

        Participant participantOne = defineGameParticipant(playerOne, mancalaGame, 1);
        Participant participantTwo = defineGameParticipant(playerTwo, mancalaGame, 2);

        definePitsSetupAndTableCurrentStateForThisGame(mancalaGame, participantOne, participantTwo);
        return mancalaGame;
    }

    private Participant defineGameParticipant(PlayerAccount playerAccount, MancalaGame mancalaGame, int playerNumber) {
        Participant participant = new Participant();
        participant.setMancalaGame(mancalaGame);
        participant.setPlayerAccount(playerAccount);
        participant.setPlayerNumber(playerNumber);
        return participantJpaRepository.save(participant);
    }

    private PlayerAccount defineGamePlayerAccount(String playerData) {
        PlayerAccount playerOne = new PlayerAccount();
        playerOne.setPlayerName(playerData);
        return playerJpaRepository.save(playerOne);
    }

    private void definePitsSetupAndTableCurrentStateForThisGame(MancalaGame mancalaGame, Participant participantOne, Participant participantTwo) {
        for (int i = 0; i <= MANCALA_PITS_QUANTITY; i++) {

            Pit pit = new Pit();
            pit.setPitIndex(i);

            TableCurrentState tableCurrentState = new TableCurrentState();
            tableCurrentState.setMancalaGame(mancalaGame);
            tableCurrentState.setPit(pit);

            if (i == PLAYER_ONE_BIG_STONE_INDEX || i == PLAYER_TWO_BIG_STONE_INDEX) {
                pit.setIsBigPit(1);
                tableCurrentState.setStonesCountInPit(0);
            } else {
                pit.setIsBigPit(0);
                tableCurrentState.setStonesCountInPit(MANCALA_STONES_COUNT_IN_PIT);
            }

            if (i <= PLAYER_ONE_BIG_STONE_INDEX) {
                pit.setParticipant(participantOne);
            } else {
                pit.setParticipant(participantTwo);
            }

            pitJpaRepository.save(pit);
            tableCurrentStateRepository.save(tableCurrentState);
        }
    }

    @Override
    @Transactional//(readOnly = true)
    public MancalaBoardSetup makeMove(MoveItem moveItem, Integer pitIndex) {
        String gameId = moveItem.getGameId();
        String currentGameParticipantId = moveItem.getParticipantId();

        log.debug("Trying to get mancala game by id {}: ", gameId);

        checkIfInputPitIsBigPit(pitIndex);
        MancalaGame mancalaGame = getGameById(gameId);

        switch (mancalaGame.getGameStatus()) {
            case ENDED:
                throw new BadRequestException("A game with provided id was already ended " + gameId);
            case INITIALIZED:
                if (pitIndex > PLAYER_ONE_BIG_STONE_INDEX) {
                    throw new BadRequestException("Player 2 cannot start a new game, you should chose player 1 pits, " +
                            "provided pit has number " + pitIndex);
                }
                //TODO check is provided participant id is fisrt
                Participant firstParticipantByMancalaGameId = participantJpaRepository.findById(currentGameParticipantId)
                        .orElseThrow(() -> {
                            log.error("");

                            return new NotFoundException("There is no participant in this game by provided game id " + gameId);
                        });
                if (firstParticipantByMancalaGameId.getPlayerNumber() != 1) {
                    log.error("");

                    throw new BadRequestException("Game participant should be first to make a first move! " +
                            "Provided participant id " + currentGameParticipantId + " belongs to player number two!");
                }
/*                Participant firstParticipantByMancalaGameId = participantJpaRepository.findByMancalaGameIdAndPlayerNumber(gameId, 1)
                        .orElseThrow(() -> {
                            log.error("");

                            return new NotFoundException("There is no participant in this game by provided game id " + gameId);
                        });*/

                return makeMove(mancalaGame, gameId, pitIndex, firstParticipantByMancalaGameId);
            //mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
            //break;
            case IN_PROGRESS:
                String lastParticipantId = mancalaGame.getLastParticipantIdMove(); //participant id who did the last move
                boolean isPlayerOneTurn = false;
                isPlayerOneTurn = checkIfPlayerOneTurnNow(mancalaGame, lastParticipantId);

                checkIfPlayerSelectedCorrectPitRange(isPlayerOneTurn);
                //checkIfPlayerTwoTurnNow();

                //checkIfPlayerOneCanMoveOneMoreTime();
                //checkIfPlayerTwoCanMoveOneMoreTime();

/*                if (pitIndex <= 6 && mancalaGame.getGamerId() == 1) {
                    throw new BadRequestException("Player 1 already did a move, now it's player 2 turn");
                }
                if (pitIndex > 7 && mancalaGame.getGamerId() == 2) {
                    throw new BadRequestException("Player 2 already did a move, now it's player 1 turn");
                }*/
        }
/*        if (mancalaGame.getGameStatus() == GameStatesEnum.ENDED) {
            throw new BadRequestException("A game with provided id was already ended " + gameId);
        }
        if (mancalaGame.getGameStatus() == GameStatesEnum.INITIALIZED && pitId > 6) {
            throw new BadRequestException("Player 2 cannot start a new game, you should chose player 1 pits, " +
                    "provided pit has number " + pitId);
        }
        if (mancalaGame.getGameStatus() == GameStatesEnum.IN_PROGRESS && pitId <= 6 && mancalaGame.getGamerId() == 1) {
            throw new BadRequestException("Player 1 already did a move, now it's player 2 turn");
        }
        if (mancalaGame.getGameStatus() == GameStatesEnum.IN_PROGRESS && pitId > 7 && mancalaGame.getGamerId() == 2) {
            throw new BadRequestException("Player 2 already did a move, now it's player 1 turn");
        }*/
        return null;
    }

    private MancalaBoardSetup makeMove(MancalaGame mancalaGame, String gameId, Integer pitIndex, Participant gameCurrentParticipant) {
        //String mancalaGameId = mancalaGame.getId();
        log.debug("");
        boolean isCurrentParticipantIsFirst = gameCurrentParticipant.getPlayerNumber() == 1;
        boolean isParticipantIdNextMove = false;

        // boolean isSecondTurn = false;
        TableCurrentState tableCurrentStateByProvidedPit = findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, pitIndex);

        Integer currentStonesCountInPit = tableCurrentStateByProvidedPit.getStonesCountInPit(); //TODO try to find not whole object but already stones count in query above!!
        if (currentStonesCountInPit == 0) {
            log.error("");

            throw new BadRequestException("Chosen pit is empty, please chose pit with at least one stone inside!"); // TODO is it really bad request ?
        }
        tableCurrentStateByProvidedPit.setStonesCountInPit(0); //update current pits table state, put stones to 0 cause we took all stones from it
        tableCurrentStateRepository.save(tableCurrentStateByProvidedPit);

        Set<Integer> pitIndexesSetToPlaceOneStone = new HashSet<>();
        for (int i = 1; i <= currentStonesCountInPit; i++) {
            // check if we do not put stone in our game partner's big pit
            //if (pitIndex < 6) { //means that player one makes this move
            if (isCurrentParticipantIsFirst) {
                if (pitIndex + i != PLAYER_TWO_BIG_STONE_INDEX) { // player one cannot put stone in his partner's big pit
                    pitIndexesSetToPlaceOneStone.add(pitIndex + i);
                }
            } else {
                //if (pitIndex > 6) { //means that player two makes this move
                if (pitIndex + i != PLAYER_ONE_BIG_STONE_INDEX) { // player two cannot put stone in his partner's big pit
                    pitIndexesSetToPlaceOneStone.add(pitIndex + i);
                }
            }
        }

/*        // if last pit index in pit indexes set is big pit it means player has the second turn to make a next move
        isSecondTurn = pitIndexesSetToPlaceOneStone.stream().reduce((one, two) -> two).filter(two -> two == 6 || two == 13).isPresent();*/

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
            saveTableCurrentState(tableCurrentState, tableCurrentState.getStonesCountInPit() + 1);
/*            int pitNewStonesCount = tableCurrentState.getStonesCountInPit() + 1;
            tableCurrentState.setStonesCountInPit(pitNewStonesCount);
            tableCurrentStateRepository.save(tableCurrentState);*/
        }

        // if last stone you drop is in empty pit and this pit is NOT big pit - we capture that stone + all stones in opposite pit and move to his big pit
        if (tableCurrentStateForLastStone.getStonesCountInPit() == 0 && tableCurrentStateForLastStone.getPit().getIsBigPit() == 0) {
            log.debug("");

            int oppositePitIndex = MANCALA_PITS_QUANTITY - tableCurrentStateForLastStone.getPit().getPitIndex() - 1; //calculate opposite pit index based on mancala pits count
            TableCurrentState tableCurrentStateOfTheOppositePit = findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, oppositePitIndex);

            int allStonesInOppositePitPlusOne = tableCurrentStateOfTheOppositePit.getStonesCountInPit() + 1; //grab all stones from opposite pit PLUS last stone in the pit to drop all it to the big pit
            if (tableCurrentStateOfTheOppositePit.getStonesCountInPit() != 0) { //check if there is stones in opposite pit cause if there is no stones we will not waste performance to update it
/*                tableCurrentStateOfTheOppositePit.setStonesCountInPit(0);
                tableCurrentStateRepository.save(tableCurrentStateOfTheOppositePit);*/
                saveTableCurrentState(tableCurrentStateOfTheOppositePit, 0);
            }

            TableCurrentState tableCurrentStateOfPlayerBigPit;
            if (isCurrentParticipantIsFirst) { //if plays participant one, we should find his big pit under index 6
                log.debug("");

                tableCurrentStateOfPlayerBigPit = findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, PLAYER_ONE_BIG_STONE_INDEX);
            } else { //else if is second participant plays game, we should find his big pit on index 13
                log.debug("");

                tableCurrentStateOfPlayerBigPit = findTableCurrentStateByMancalaGameIdAndPitIndex(gameId, PLAYER_TWO_BIG_STONE_INDEX);
            }
            saveTableCurrentState(tableCurrentStateOfPlayerBigPit,
                    tableCurrentStateOfPlayerBigPit.getStonesCountInPit() + allStonesInOppositePitPlusOne);
/*            int bigPitNewStonesCount = tableCurrentStateOfPlayerBigPit.getStonesCountInPit() + allStonesInOppositePitPlusOne;
            tableCurrentStateOfPlayerBigPit.setStonesCountInPit(bigPitNewStonesCount);
            tableCurrentStateRepository.save(tableCurrentStateOfPlayerBigPit);*/

            mancalaGame.setLastParticipantIdMove(gameCurrentParticipant.getId());
            mancalaGame.setSecondTurn(0); //no second turn for this current participant
            isParticipantIdNextMove = true;

        } else if (tableCurrentStateForLastStone.getPit().getIsBigPit() == 1) { //if last stone in current state will be in big pit so the player has second turn to move
            saveTableCurrentState(tableCurrentStateForLastStone, tableCurrentStateForLastStone.getStonesCountInPit() + 1);
/*            int bigPitNewStonesCount = tableCurrentStateForLastStone.getStonesCountInPit() + 1;
            tableCurrentStateForLastStone.setStonesCountInPit(bigPitNewStonesCount);
            tableCurrentStateRepository.save(tableCurrentStateForLastStone);*/

            mancalaGame.setLastParticipantIdMove(gameCurrentParticipant.getId());
            mancalaGame.setSecondTurn(1); //set second turn for next move for the current game participant

        } else { //else it last stone in table current state is has no empty pit and this pit is NOT big - so it's simple stone and simple move
            saveTableCurrentState(tableCurrentStateForLastStone, tableCurrentStateForLastStone.getStonesCountInPit() + 1);

/*            int pitNewStonesCount = tableCurrentStateForLastStone.getStonesCountInPit() + 1;
            tableCurrentStateForLastStone.setStonesCountInPit(pitNewStonesCount);
            tableCurrentStateRepository.save(tableCurrentStateForLastStone);*/

            mancalaGame.setLastParticipantIdMove(gameCurrentParticipant.getId());
            mancalaGame.setSecondTurn(0); //no second turn for this current participant
            isParticipantIdNextMove = true;
        }

        //create Move entity for logging game purposes
        log.debug("");

        createMoveEntity(gameCurrentParticipant, 0, tableCurrentStateByProvidedPit.getPit().getId(),
                tableCurrentStateForLastStone.getPit().getId(), currentStonesCountInPit);

        // check if all pits are empty after move above in current participant game table setup
        if (mancalaGame.getGameStatus() == GameStatesEnum.IN_PROGRESS) {
            log.debug("");

            String gameCurrentParticipantId = gameCurrentParticipant.getId();

/*            List<Integer> currentGameParticipantTableCurrentStates = tableCurrentStateRepository.findStonesCountInPitsByMancalaGameIdAndParticipantId(gameId, gameParticipantId);
            if (currentGameParticipantTableCurrentStates.isEmpty()) {
                log.error("");

                throw new NotFoundException("Table current states was not found by provided gameId " + gameId + " and participantId " + gameParticipantId);
            }

            boolean allGameParticipantPitsPitsAreEmpty = currentGameParticipantTableCurrentStates.stream().allMatch(elem -> elem == 0);*/
            boolean allGameParticipantPitsPitsAreEmpty = tableCurrentStateRepository.arePitsEmptyByGameIdAndParticipantId(gameId, gameCurrentParticipantId); //another options

            if (allGameParticipantPitsPitsAreEmpty) { //if true, the player who still has stones in his pits keeps them and puts them in his big pit
                log.debug("");

                int currentGameParticipantStonesCountInBigPit = tableCurrentStateRepository.findStonesCountInPitByGameIdAndParticipantId(gameId, gameCurrentParticipantId, 1);

                //TODO check while running application if <> sign works correctly!
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
                    saveTableCurrentState(tableCurrentState, 0);
/*                    oppositeParticipantAllPitsStonesSum += tableCurrentState.getStonesCountInPit();
                    tableCurrentState.setStonesCountInPit(0);
                    tableCurrentStateRepository.save(tableCurrentState);*/
                }

                //update opposite game participant big pit by whole picked up stones from his small pits
                saveTableCurrentState(oppositeParticipantBigPitTableCurrentState,
                        oppositeParticipantBigPitTableCurrentState.getStonesCountInPit() + oppositeParticipantAllPitsStonesSum);
/*                int pitNewStonesCount = oppositeParticipantBigPitTableCurrentState.getStonesCountInPit() + oppositeParticipantAllPitsStonesSum;
                oppositeParticipantBigPitTableCurrentState.setStonesCountInPit(pitNewStonesCount);
                tableCurrentStateRepository.save(oppositeParticipantBigPitTableCurrentState);*/

                //calculate two players stones from their big pits to identify a winner
                int result = currentGameParticipantStonesCountInBigPit - oppositeParticipantBigPitTableCurrentState.getStonesCountInPit();
                if (result > 0) { //current game participant wins
                    mancalaGame.setGameStatus(
                            isCurrentParticipantIsFirst ? GameStatesEnum.PARTICIPANT_ONE_WINS : GameStatesEnum.PARTICIPANT_TWO_WINS);
                } else if (result < 0) { //his opposite wins
                    mancalaGame.setGameStatus(
                            isCurrentParticipantIsFirst ? GameStatesEnum.PARTICIPANT_TWO_WINS : GameStatesEnum.PARTICIPANT_ONE_WINS);
                } else {
                    mancalaGame.setGameStatus(GameStatesEnum.DRAW);
                }

                return mancalaBoardSetupUtils.getGameBoardSetupResponseBody(mancalaGame, gameCurrentParticipant, pitIndex,
                        tableCurrentStateForLastStone.getPit().getPitIndex(), "0"); //end of the game
            }

        }
        mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
        String participantIdNextMove = defineParticipantIdNextMove(isParticipantIdNextMove, isCurrentParticipantIsFirst,
                mancalaGame, gameCurrentParticipant.getId());
        return mancalaBoardSetupUtils.getGameBoardSetupResponseBody(mancalaGame, gameCurrentParticipant, pitIndex,
                tableCurrentStateForLastStone.getPit().getPitIndex(), participantIdNextMove);
    }

    private String defineParticipantIdNextMove(boolean isParticipantIdNextMove, boolean isCurrentParticipantIsFirst,
                                               MancalaGame mancalaGame, String gameCurrentParticipantId) {
        if (isParticipantIdNextMove) {
            return mancalaGame.getParticipantList().stream()
                    .filter(participant -> participant.getPlayerNumber() == (isCurrentParticipantIsFirst ? 2 : 1))
                    .findFirst()
                    .get()
                    .getId();
        } else {
            return gameCurrentParticipantId;
        }
    }

    private void createMoveEntity(Participant gameCurrentParticipant, int isFixed, String fromPitId, String toPitId, Integer currentStonesCountInPit) {
        Move move = new Move();
        move.setParticipant(gameCurrentParticipant);
        move.setIsFixed(isFixed);
        move.setFromPitId(fromPitId);
        move.setToPitId(toPitId);
        move.setStonesCountInHand(currentStonesCountInPit);
        moveJpaRepository.save(move);
    }

/*    private MancalaBoardSetup getGameBoardSetupResponseBody(MancalaGame mancalaGame,
                                                            Participant gameCurrentParticipant,
                                                            Integer pitIndexFrom,
                                                            Integer pitIndexTo,
                                                            String participantIdNextMove) {
        MancalaBoardSetup mancalaBoardSetup = new MancalaBoardSetup();
        mancalaBoardSetup.setGameId(mancalaGame.getId());
        mancalaBoardSetup.setGameState(gameStatesEnumMapper.toGameStatusEnumViewModel(mancalaGame.getGameStatus()));

        mancalaBoardSetup.setParticipantIdCurrentMove(gameCurrentParticipant.getId());
        mancalaBoardSetup.setParticipantIdNextMove(participantIdNextMove);

        mancalaBoardSetup.setPitIndexFrom(pitIndexFrom);
        mancalaBoardSetup.setPitIndexTo(pitIndexTo);

        mancalaBoardSetup.setTableCurrentState(
                tableCurrentStateMapper.toTableCurrentStateViewModelList(mancalaGame.getTableCurrentStatesList()));
        return mancalaBoardSetup;
    }*/

    private void saveTableCurrentState(TableCurrentState tableCurrentState, int pitStonesCount) {
        tableCurrentState.setStonesCountInPit(pitStonesCount);
        tableCurrentStateRepository.save(tableCurrentState);
    }

    private TableCurrentState findTableCurrentStateByMancalaGameIdAndPitIndex(String gameId, Integer pitIndex) {
        return tableCurrentStateRepository.findTableCurrentStateByMancalaGameIdAndPitPitIndex(gameId, pitIndex).orElseThrow(() -> { //TODO is it better to find by gameId and pitId cause it can be a lot table states if this game has several tables
            log.error("");

            return new NotFoundException("No table current state by provided gameId " + gameId + " and pitIndex " + pitIndex);
        });
    }


    private boolean checkIfPlayerOneTurnNow(MancalaGame mancalaGame, String lastParticipantId) {
        boolean isPlayerOneTurn = false;
        Pit byParticipantId = pitJpaRepository.findByParticipantId(lastParticipantId).orElseThrow(() -> {
            log.error("");
            return new NotFoundException("Pit was not found by provided participant_id " + lastParticipantId);
        });
        return isPlayerOneTurn;
    }

    private void checkIfPlayerSelectedCorrectPitRange(boolean isPlayerOneTurn) {
    }

    private MancalaGame getGameById(String gameId) {
        return mancalaJpaRepository.findById(gameId)
                .orElseThrow(() -> {
                    log.error("Mancala game with id {} does not exist", gameId);
                    return new NotFoundException("Mancala game not found by id " + gameId);
                });
    }

    private void checkIfInputPitIsBigPit(int pitIndex) {
        if (pitIndex == 6 || pitIndex == 13) {
            log.error("");

            throw new BadRequestException("You can't make a move from a big pit, pls chose correct pit, " +
                    "provided pit has number " + pitIndex);
        }
    }

/*    private void checkIfPlayerOneCanMakeMoveAgain(MancalaGame mancalaGame, int pitId) {
        if (pitId <= 6 && mancalaGame.getGamerId() == 1 && !mancalaGame.isLastStoneInBigPit()) {
            log.error("");

            throw new BadRequestException("Player 1 already did a move, now it's player 2 turn");
        }
    }

    private void checkIfPlayerTwoCanMakeMoveAgain(MancalaGame mancalaGame, int pitId) {
        if (pitId > 7 && mancalaGame.getGamerId() == 2 && !mancalaGame.isLastStoneInBigPit()) {
            log.error("");

            throw new BadRequestException("Player 2 already did a move, now it's player 1 turn");
        }
    }*/
}