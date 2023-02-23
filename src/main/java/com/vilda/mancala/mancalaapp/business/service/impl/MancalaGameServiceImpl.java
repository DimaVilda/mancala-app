package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.MancalaGameService;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.*;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MancalaGameServiceImpl implements MancalaGameService {

    private final MancalaJpaRepository mancalaJpaRepository;
    private final PitJpaRepository pitJpaRepository;
    private final PlayerAccountJpaRepository playerJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;

    private final TableCurrentStateRepository tableCurrentStateRepository;

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
        for (int i = 0; i <= 13; i++) {

            Pit pit = new Pit();
            pit.setPitIndex(i);

            TableCurrentState tableCurrentState = new TableCurrentState();
            tableCurrentState.setMancalaGame(mancalaGame);
            tableCurrentState.setPit(pit);

            if (i == 6 || i == 13) {
                pit.setIsBigPit(1);
                tableCurrentState.setStonesCountInPit(0);
            } else {
                pit.setIsBigPit(0);
                tableCurrentState.setStonesCountInPit(6);
            }

            if (i <= 6) {
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
                if (pitIndex > 6) {
                    throw new BadRequestException("Player 2 cannot start a new game, you should chose player 1 pits, " +
                            "provided pit has number " + pitIndex);
                }
                Participant firstParticipantByMancalaGameId = participantJpaRepository.findByMancalaGameIdAndPlayerNumber(gameId, 1)
                        .orElseThrow(() -> {
                            log.error("");

                            return new NotFoundException("There is no participant in this game by provided game id " + gameId);
                        });

                makeMove(mancalaGame, gameId, pitIndex, firstParticipantByMancalaGameId);
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

    private void makeMove(MancalaGame mancalaGame, String gameId, Integer pitIndex, Participant gameParticipant) { //TODO maybe its better to make general move method, not first
        //String mancalaGameId = mancalaGame.getId();
        log.debug("");
        boolean isFirstParticipant = gameParticipant.getPlayerNumber() == 1;
        boolean isSecondTurn = false;
        TableCurrentState tableCurrentStateByProvidedPit = tableCurrentStateRepository.findTableCurrentStateByMancalaGameIdAndPitPitIndex(gameId, pitIndex).orElseThrow(() -> {
            log.error("");
            return new NotFoundException("No table current state by provided gameId " + gameId + " and pitIndex " + pitIndex);
        });

        Integer stonesCountInPit = tableCurrentStateByProvidedPit.getStonesCountInPit(); //TODO create Move entity!
        if (stonesCountInPit == 0) {
            log.error("");

            throw new BadRequestException("Chosen pit is empty, please chose pit with at least one stone inside!"); // TODO is it really bad request ?
        }
        Set<Integer> pitIndexesSetToPlaceOneStone = new HashSet<>();
        for (int i = 1; i <= stonesCountInPit; i++) {
            // check if we do not put stone in our game partner's big pit
            //if (pitIndex < 6) { //means that player one makes this move
            if (isFirstParticipant) {
                if (pitIndex + i != 13) { // player one cannot put stone in his partner's big pit
                    pitIndexesSetToPlaceOneStone.add(pitIndex + i);
                }
            } else {
                //if (pitIndex > 6) { //means that player two makes this move
                if (pitIndex + i != 6) { // player two cannot put stone in his partner's big pit
                    pitIndexesSetToPlaceOneStone.add(pitIndex + i);
                }
            }
        }

        // if last pit index in pit indexes set is big pit it means player has the second turn to make a next move
        isSecondTurn = pitIndexesSetToPlaceOneStone.stream().reduce((one, two) -> two).filter(two -> two == 6 || two == 13).isPresent();

        //batch select for less sql performance
        List<TableCurrentState> tableCurrentStatesByMancalaGameAndNextPitIndexes =
                tableCurrentStateRepository.findTableCurrentStatesByMancalaGameAndPitPitIndexIn(gameId, pitIndexesSetToPlaceOneStone);

        for (TableCurrentState tableCurrentState : tableCurrentStatesByMancalaGameAndNextPitIndexes) {
            int oldStonesCount = tableCurrentState.getStonesCountInPit();
            tableCurrentState.setStonesCountInPit(oldStonesCount + 1);
            //TODO check If the last piece you drop is in an empty pocket on your side, you capture that piece and any pieces in the pocket directly opposite and put to big pit
            tableCurrentStateRepository.save(tableCurrentState);
            //TODO check if game is not ended by all empty pits
        }
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