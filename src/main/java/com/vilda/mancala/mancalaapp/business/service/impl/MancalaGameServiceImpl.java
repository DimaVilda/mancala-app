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
        return definedGameId;
    }

    private String defineGameSetup(NewGameSetup newGameSetup) {
        log.debug("");

        PlayerAccount playerOne = defineGamePlayerAccount(newGameSetup.getPlayerOneName());
        PlayerAccount playerTwo = defineGamePlayerAccount(newGameSetup.getPlayerTwoName());

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);
        mancalaJpaRepository.save(mancalaGame);

        Participant participantOne = defineGameParticipant(playerOne, mancalaGame);
        Participant participantTwo = defineGameParticipant(playerTwo, mancalaGame);

        definePitsSetupAndTableCurrentStateForThisGame(mancalaGame, participantOne, participantTwo);
        return mancalaGame.getId();
    }

    private Participant defineGameParticipant(PlayerAccount playerAccount, MancalaGame mancalaGame) {
        Participant participant = new Participant();
        participant.setPlayerAccount(playerAccount);
        participant.setMancalaGame(mancalaGame);
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
    @Transactional(readOnly = true)
    public MancalaBoardSetup makeMove(String gameId, Integer pitId) {
        log.debug("Trying to get mancala game by id {}: ", gameId);

        checkIfInputPitIsBigPit(pitId);
        MancalaGame mancalaGame = getGameById(gameId);

        switch (mancalaGame.getGameStatus()) {
            case ENDED:
                throw new BadRequestException("A game with provided id was already ended " + gameId);
            case INITIALIZED:
                if (pitId > 6) {
                    throw new BadRequestException("Player 2 cannot start a new game, you should chose player 1 pits, " +
                            "provided pit has number " + pitId);
                }

/*            case IN_PROGRESS:
                if (pitId <= 6 && mancalaGame.getGamerId() == 1) {
                    throw new BadRequestException("Player 1 already did a move, now it's player 2 turn");
                }
                if (pitId > 7 && mancalaGame.getGamerId() == 2) {
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

    private MancalaGame getGameById(String gameId) {
        return mancalaJpaRepository.findById(gameId)
                .orElseThrow(() -> {
                    log.error("Mancala game with id {} does not exist", gameId);
                    return new NotFoundException("Mancala game not found by id " + gameId);
                });
    }

    private void checkIfInputPitIsBigPit(int pitId) {
        if (pitId == 7 || pitId == 14) {
            log.error("");

            throw new BadRequestException("You can't make a move from a big pit, pls chose correct pit, " +
                    "provided pit has number " + pitId);
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

    private void checkIfPlayerOneSelectedCorrectPitRange(int pitId) {

    }

    private void checkIfPlayerTwoSelectedCorrectPitRange(int pitId) {

    }
}