package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.MancalaGameService;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.*;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.*;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MancalaGameServiceImpl implements MancalaGameService {

    private final MancalaJpaRepository mancalaJpaRepository;
    private final PitJpaRepository pitJpaRepository;
    private final PlayerAccountJpaRepository playerJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final TableCurrentStateRepository tableCurrentStateRepository;
    private static final int MANCALA_PITS_QUANTITY = 13; //from 0
    private static final int MANCALA_STONES_COUNT_IN_PIT = 6; //from 1
    private static final int PLAYER_ONE_BIG_STONE_INDEX = 6;
    private static final int PLAYER_TWO_BIG_STONE_INDEX = 13;

    @Override
    @Transactional
    public String startNewGame(NewGameSetup newGameSetup) {
        String definedGameId = defineGameSetup(newGameSetup);
        // return definedGameId;
        return "game-test-id";
    }

    private String defineGameSetup(NewGameSetup newGameSetup) {
        log.debug("");

        PlayerAccount playerOne = defineGamePlayerAccount(newGameSetup.getPlayerOneName());
        PlayerAccount playerTwo = defineGamePlayerAccount(newGameSetup.getPlayerTwoName());

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);
        mancalaGame.setSecondTurn(0);
        mancalaJpaRepository.save(mancalaGame);

        Participant participantOne = defineGameParticipant(playerOne, mancalaGame, 1);
        Participant participantTwo = defineGameParticipant(playerTwo, mancalaGame, 2);

        definePitsSetupAndTableCurrentStateForThisGame(mancalaGame, participantOne, participantTwo);
        return mancalaGame.getId();
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
    public MancalaBoardSetup makeMove(String gameId, Integer pitIndex) {
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
                Participant firstParticipantByMancalaGameId = participantJpaRepository.findByMancalaGameIdAndPlayerNumber(gameId, 1)
                        .orElseThrow(() -> {
                            log.error("");

                            return new NotFoundException("There is no participant in this game by provided game id " + gameId);
                        });

                MancalaBoardSetup mancalaBoardSetup = makeMove(mancalaGame, gameId, pitIndex, firstParticipantByMancalaGameId);
                mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
                break;
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

    private MancalaBoardSetup makeMove(MancalaGame mancalaGame, String gameId, Integer pitIndex, Participant gameParticipant) { //TODO maybe its better to make general move method, not first
        //String mancalaGameId = mancalaGame.getId();
        log.debug("");
        boolean isFirstParticipant = gameParticipant.getPlayerNumber() == 1;
       // boolean isSecondTurn = false;
        TableCurrentState tableCurrentStateByProvidedPit = tableCurrentStateRepository.findTableCurrentStateByMancalaGameIdAndPitPitIndex(gameId, pitIndex).orElseThrow(() -> { //TODO is it better to find by gameId and pitId cause it can be a lot table states if this game has several tables
            log.error("");
            return new NotFoundException("No table current state by provided gameId " + gameId + " and pitIndex " + pitIndex);
        });

        Integer stonesCountInPit = tableCurrentStateByProvidedPit.getStonesCountInPit(); //TODO create Move entity! And find not whole object but already stones count in query above!!
        if (stonesCountInPit == 0) {
            log.error("");

            throw new BadRequestException("Chosen pit is empty, please chose pit with at least one stone inside!"); // TODO is it really bad request ?
        }
        Set<Integer> pitIndexesSetToPlaceOneStone = new HashSet<>();
        for (int i = 1; i <= stonesCountInPit; i++) {
            // check if we do not put stone in our game partner's big pit
            //if (pitIndex < 6) { //means that player one makes this move
            if (isFirstParticipant) {
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
            int pitNewStonesCount = tableCurrentState.getStonesCountInPit() + 1;
            tableCurrentState.setStonesCountInPit(pitNewStonesCount);
            tableCurrentStateRepository.save(tableCurrentState);
        }

        // if last stone you drop is in empty pit and this pit is NOT big pit - we capture that stone + all stones in opposite pit and move to his big pit
        if (tableCurrentStateForLastStone.getStonesCountInPit() == 0 && tableCurrentStateForLastStone.getPit().getIsBigPit() == 0) {
            log.debug("");

            int oppositePitIndex = MANCALA_PITS_QUANTITY - tableCurrentStateForLastStone.getPit().getPitIndex() - 1; //calculate opposite pit index based on mancala pits count
            TableCurrentState tableCurrentStateOfTheOppositePit = tableCurrentStateRepository.findTableCurrentStateByMancalaGameIdAndPitPitIndex(gameId, oppositePitIndex).orElseThrow(() -> { //find table current state of the opposite pit to get all stones from it
                log.error("");

                return new NotFoundException("No table current state by provided gameId " + gameId + " and pitIndex " + oppositePitIndex);
            });
            int allStonesInOppositePitPlusOne = tableCurrentStateOfTheOppositePit.getStonesCountInPit() + 1; //grab all stones from opposite pit PLUS last stone in the pit to drop all it to the big pit
            if (tableCurrentStateOfTheOppositePit.getStonesCountInPit() != 0) { //check if there is stones in opposite pit cause if there is no stones we will not waste performance to update it
                tableCurrentStateOfTheOppositePit.setStonesCountInPit(0);
                tableCurrentStateRepository.save(tableCurrentStateOfTheOppositePit);
            }

            TableCurrentState tableCurrentStateOfPlayerBigPit;
            if (isFirstParticipant) { //if plays participant one, we should find his big pit under index 6
                log.debug("");

                tableCurrentStateOfPlayerBigPit = tableCurrentStateRepository.findTableCurrentStateByMancalaGameIdAndPitPitIndex(gameId, PLAYER_ONE_BIG_STONE_INDEX).orElseThrow(() -> {
                    log.error("");

                    return new NotFoundException("No table current state by provided gameId " + gameId + " and pitIndex " + oppositePitIndex);
                });
            } else { //else if is second participant plays game, we should find his big pit on index 13
                log.debug("");

                tableCurrentStateOfPlayerBigPit = tableCurrentStateRepository.findTableCurrentStateByMancalaGameIdAndPitPitIndex(gameId, PLAYER_TWO_BIG_STONE_INDEX).orElseThrow(() -> {
                    log.error("");

                    return new NotFoundException("No table current state by provided gameId " + gameId + " and pitIndex " + oppositePitIndex);
                });
            }
            int bigPitNewStonesCount = tableCurrentStateOfPlayerBigPit.getStonesCountInPit() + allStonesInOppositePitPlusOne;
            tableCurrentStateOfPlayerBigPit.setStonesCountInPit(bigPitNewStonesCount);
            tableCurrentStateRepository.save(tableCurrentStateOfPlayerBigPit);

            mancalaGame.setLastParticipantIdMove(gameParticipant.getId());
            mancalaGame.setSecondTurn(0); //no second turn for this current participant

        } else if (tableCurrentStateForLastStone.getPit().getIsBigPit() == 1) { //if last stone in current state will be in big pit so the player has second turn to move
            int bigPitNewStonesCount = tableCurrentStateForLastStone.getStonesCountInPit() + 1;
            tableCurrentStateForLastStone.setStonesCountInPit(bigPitNewStonesCount);
            tableCurrentStateRepository.save(tableCurrentStateForLastStone);

            mancalaGame.setLastParticipantIdMove(gameParticipant.getId());
            //set second turn for next move for the current game participant
            mancalaGame.setSecondTurn(1);
        } else { //else it last stone in table current state is has no empty pit and this pit is NOT big - so it's simple stone and simple move
            int pitNewStonesCount = tableCurrentStateForLastStone.getStonesCountInPit() + 1;
            tableCurrentStateForLastStone.setStonesCountInPit(pitNewStonesCount);
            tableCurrentStateRepository.save(tableCurrentStateForLastStone);

            mancalaGame.setLastParticipantIdMove(gameParticipant.getId());
            mancalaGame.setSecondTurn(0); //no second turn for this current participant
        }

        // check if all pits are empty in current participant game table setup
        if (mancalaGame.getGameStatus() == GameStatesEnum.IN_PROGRESS) {
            log.debug("");

            String gameParticipantId = gameParticipant.getId();;
/*            List<Integer> currentGameParticipantTableCurrentStates = tableCurrentStateRepository.findStonesCountInPitsByMancalaGameIdAndParticipantId(gameId, gameParticipantId);
            if (currentGameParticipantTableCurrentStates.isEmpty()) {
                log.error("");

                throw new NotFoundException("Table current states was not found by provided gameId " + gameId + " and participantId " + gameParticipantId);
            }

            boolean allGameParticipantPitsPitsAreEmpty = currentGameParticipantTableCurrentStates.stream().allMatch(elem -> elem == 0);*/
            boolean allGameParticipantPitsPitsAreEmpty = tableCurrentStateRepository.arePitsEmptyByGameIdAndParticipantId(gameId, gameParticipantId); //another options

            if (allGameParticipantPitsPitsAreEmpty) { //if true, the player who still has stones in his pits keeps them and puts them in his big pit
                int currentGameParticipantStonesCountInBigPit = tableCurrentStateRepository.findStonesCountInPitByGameIdAndParticipantId(gameId, gameParticipantId, 1);

                //TODO check while running application if <> sign works correctly!
                List<TableCurrentState> oppositeParticipantTableCurrentStates = tableCurrentStateRepository.findTableCurrentStatesByMancalaGameIdAndNotParticipantId(gameId, gameParticipantId);
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
                for (TableCurrentState tableCurrentState: oppositeParticipantTableCurrentStates) {
                    oppositeParticipantAllPitsStonesSum += tableCurrentState.getStonesCountInPit();
                    tableCurrentState.setStonesCountInPit(0);
                    tableCurrentStateRepository.save(tableCurrentState);
                }

                //update opposite game participant big pit by whole picked up stones from his small pits
                int pitNewStonesCount = oppositeParticipantBigPitTableCurrentState.getStonesCountInPit() + oppositeParticipantAllPitsStonesSum;
                oppositeParticipantBigPitTableCurrentState.setStonesCountInPit(pitNewStonesCount);
                tableCurrentStateRepository.save(oppositeParticipantBigPitTableCurrentState);

                //calculate two players stones from their big pits to identify a winner
                int result = currentGameParticipantStonesCountInBigPit - oppositeParticipantBigPitTableCurrentState.getStonesCountInPit();

                if (result > 0) { //current game participant wins
                    mancalaGame.setGameStatus(isFirstParticipant ? GameStatesEnum.PARTICIPANT_ONE_WINS : GameStatesEnum.PARTICIPANT_TWO_WINS);
                } else if (result < 0) { //his opposite wins
                    mancalaGame.setGameStatus(isFirstParticipant ? GameStatesEnum.PARTICIPANT_TWO_WINS : GameStatesEnum.PARTICIPANT_ONE_WINS);
                } else {
                    mancalaGame.setGameStatus(GameStatesEnum.DRAW);
                }

                return getGameBoardSetupResponseBody();
            }

        }

        return getGameBoardSetupResponseBody();
    }

    private MancalaBoardSetup getGameBoardSetupResponseBody() {
        return new MancalaBoardSetup();
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