package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.MancalaGameService;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.domain.Pit;
import com.vilda.mancala.mancalaapp.domain.Player;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.MancalaJpaRepository;
import com.vilda.mancala.mancalaapp.repository.ParticipantJpaRepository;
import com.vilda.mancala.mancalaapp.repository.PitJpaRepository;
import com.vilda.mancala.mancalaapp.repository.PlayerJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MancalaGameServiceImpl implements MancalaGameService {

    private final MancalaJpaRepository mancalaJpaRepository;
    private final PitJpaRepository pitJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
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

        Player playerOne = new Player();
        playerOne.setPlayerName(newGameSetup.getPlayerOneName());

        Player playerTwo = new Player();
        playerTwo.setPlayerName(newGameSetup.getPlayerTwoName());

       // Player savedPlayerOne = playerJpaRepository.save(playerOne);
      //  Player savedPlayerTwo = playerJpaRepository.save(playerTwo);

        Participant participantOne = new Participant();
        participantOne.setPlayer(playerOne);
        participantOne.setLastStoneInBigPit(0);

      //  Participant savedOne = participantJpaRepository.save(participantOne);

        Participant participantTwo = new Participant();
        participantTwo.setPlayer(playerTwo);
        participantTwo.setLastStoneInBigPit(0);

      //  Participant savedTwo = participantJpaRepository.save(participantTwo);

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);
        mancalaGame.setGamePlayer(playerOne);


        participantOne.setMancalaGameList(Arrays.asList(mancalaGame));
        participantTwo.setMancalaGameList(Arrays.asList(mancalaGame));

        playerOne.setParticipantList(Arrays.asList(participantOne));
        playerTwo.setParticipantList(Arrays.asList(participantTwo));

        mancalaGame.setParticipantList(Arrays.asList(participantOne, participantTwo));

        Participant savedOne = participantJpaRepository.save(participantOne);
        Participant savedTwo = participantJpaRepository.save(participantTwo);


       // mancalaJpaRepository.save(mancalaGame);

/*        List<Pit> pitListPlayerOne = new ArrayList<>();
        List<Pit> pitListPlayerTwo = new ArrayList<>();

        for (int i = 1; i <= 14; i++) {
            Pit pit = new Pit();
            pit.setMancalaGame(mancalaGame);
            pit.setPitIndex(i);

            if (i == 7 || i == 14) {
                pit.setStonesCount(0);
            } else {
                pit.setStonesCount(6);
            }

            if (i <= 7) {
                pit.setPitPlayer(playerOne);
                pitListPlayerOne.add(pit);
            } else {
                pit.setPitPlayer(playerTwo);
                pitListPlayerTwo.add(pit);
            }
        }

        playerOne.setGameList(Arrays.asList(mancalaGame));
        playerTwo.setGameList(Arrays.asList(mancalaGame));

        playerOne.setPlayerPitList(pitListPlayerOne);
        playerTwo.setPlayerPitList(pitListPlayerTwo);


        List<Pit> commonPitList = Stream.concat(pitListPlayerOne.stream(), pitListPlayerTwo.stream())
                .collect(Collectors.toList());
        mancalaGame.setPitList(commonPitList);
        mancalaJpaRepository.save(mancalaGame);

        playerJpaRepository.save(playerOne);
        playerJpaRepository.save(playerTwo);*/
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