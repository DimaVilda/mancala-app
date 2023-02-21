package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.MancalaGameService;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.domain.PlayerAccount;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.MancalaJpaRepository;
import com.vilda.mancala.mancalaapp.repository.ParticipantJpaRepository;
import com.vilda.mancala.mancalaapp.repository.PitJpaRepository;
import com.vilda.mancala.mancalaapp.repository.PlayerAccountJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class MancalaGameServiceImpl implements MancalaGameService {

    private final MancalaJpaRepository mancalaJpaRepository;
    private final PitJpaRepository pitJpaRepository;
    private final PlayerAccountJpaRepository playerJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;

    @Override
    @Transactional
    public String startNewGame(NewGameSetup newGameSetup) {
        MancalaGame mancalaGame = defineGameSetup(newGameSetup);
        return "1";//mancalaJpaRepository.save(mancalaGame).getId();
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

    private MancalaGame defineGameSetup(NewGameSetup newGameSetup) {
        log.debug("");

        PlayerAccount playerOne = new PlayerAccount();
        playerOne.setPlayerName(newGameSetup.getPlayerOneName());
      //  playerJpaRepository.save(playerOne);

        PlayerAccount playerTwo = new PlayerAccount();
        playerTwo.setPlayerName(newGameSetup.getPlayerTwoName());
     //   playerJpaRepository.save(playerTwo);

        PlayerAccount savedPlayerOne = playerJpaRepository.save(playerOne);
        PlayerAccount savedPlayerTwo = playerJpaRepository.save(playerTwo);

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);
        MancalaGame savedMancalaGame = mancalaJpaRepository.save(mancalaGame);

        Participant participantOne = new Participant();
        //participantOne.setMancalaGame();
        participantOne.setPlayerAccount(savedPlayerOne);
        participantOne.setMancalaGame(savedMancalaGame);

        Participant participantTwo = new Participant();
        //participantOne.setMancalaGame();
        participantTwo.setPlayerAccount(savedPlayerTwo);
        participantTwo.setMancalaGame(savedMancalaGame);

        participantJpaRepository.save(participantOne);
        participantJpaRepository.save(participantTwo);
        return mancalaGame;
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