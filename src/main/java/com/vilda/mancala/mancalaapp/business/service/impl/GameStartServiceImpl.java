package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.GameStartService;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.*;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameStartServiceImpl implements GameStartService {

    private final MancalaJpaRepository mancalaJpaRepository;
    private final PitJpaRepository pitJpaRepository;
    private final PlayerAccountJpaRepository playerJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final TableCurrentStateRepository tableCurrentStateRepository;

    @Override
    public MancalaGame defineGameSetup(NewGameSetup newGameSetup) {
        log.debug("Trying to define a game setup by providing request {}", newGameSetup);

        PlayerAccount playerOne = defineGamePlayerAccount(newGameSetup.getPlayerOneName());
        PlayerAccount playerTwo = defineGamePlayerAccount(newGameSetup.getPlayerTwoName());

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);
        mancalaGame.setSecondTurn(0);
        mancalaGame.setLastParticipantIdMove("0");
        mancalaJpaRepository.save(mancalaGame);

        Participant participantOne = defineGameParticipant(playerOne, mancalaGame, 1);
        playerOne.getParticipantSet().add(participantOne);

        Participant participantTwo = defineGameParticipant(playerTwo, mancalaGame, 2);
        playerTwo.getParticipantSet().add(participantTwo);

        mancalaGame.getParticipantSet().add(participantOne);
        mancalaGame.getParticipantSet().add(participantTwo);

        participantJpaRepository.save(participantOne);
        participantJpaRepository.save(participantTwo);

        definePitsSetupAndTableCurrentStateForThisGame(mancalaGame, participantOne, participantTwo);
        return mancalaGame;
    }


    private Participant defineGameParticipant(PlayerAccount playerAccount, MancalaGame mancalaGame, int playerNumber) {
        Participant participant = new Participant();
        participant.setMancalaGame(mancalaGame);
        participant.setPlayerAccount(playerAccount);
        participant.setPlayerNumber(playerNumber);
        return participant;
    }

    private PlayerAccount defineGamePlayerAccount(String playerData) {
        PlayerAccount playerAccount = new PlayerAccount();
        playerAccount.setPlayerName(playerData);
        return playerJpaRepository.save(playerAccount);
    }

    private void definePitsSetupAndTableCurrentStateForThisGame(MancalaGame mancalaGame, Participant participantOne, Participant participantTwo) {
        for (int i = 0; i <= MANCALA_PITS_QUANTITY; i++) {

            Pit pit = new Pit();
            pit.setPitIndex(i);

            TableCurrentState tableCurrentState = new TableCurrentState();
            tableCurrentState.setMancalaGame(mancalaGame);
            tableCurrentState.setPit(pit);

            if (i == PLAYER_ONE_BIG_STONE_INDEX || i == PLAYER_TWO_BIG_STONE_INDEX) { //define is pit big
                pit.setIsBigPit(1);
                tableCurrentState.setStonesCountInPit(0);
            } else {
                pit.setIsBigPit(0);
                tableCurrentState.setStonesCountInPit(MANCALA_STONES_COUNT_IN_PIT);
            }

            if (i <= PLAYER_ONE_BIG_STONE_INDEX) { //define participants for pit, if less 6 - participant one, more - second
                pit.setParticipant(participantOne);
                participantOne.getPlayerPitList().add(pit);
            } else {
                pit.setParticipant(participantTwo);
                participantTwo.getPlayerPitList().add(pit);
            }

            mancalaGame.getTableCurrentStatesList().add(tableCurrentState);

            pitJpaRepository.save(pit);
            tableCurrentStateRepository.save(tableCurrentState);
        }
    }
}
