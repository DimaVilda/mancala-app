package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.domain.PlayerAccount;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameStartServiceImplTest {

    @Mock
    private MancalaJpaRepository mancalaJpaRepository;
    @Mock
    private PitJpaRepository pitJpaRepository;
    @Mock
    private PlayerAccountJpaRepository playerJpaRepository;
    @Mock
    private ParticipantJpaRepository participantJpaRepository;
    @Mock
    private TableCurrentStateRepository tableCurrentStateRepository;
    @InjectMocks
    private GameStartServiceImpl gameStartService;
    private static final String CURR_GAME_PARTICIPANT_ID_ONE = "participantIdOne";
    private static final String CURR_GAME_PARTICIPANT_ID_TWO = "participantIdTwo";

    @Test
    void shouldDefineGameSetup() {
        NewGameSetup newGameSetup = new NewGameSetup();
        newGameSetup.setPlayerOneName("playerOneName");
        newGameSetup.setPlayerTwoName("playerTwoName");

        PlayerAccount playerOne = new PlayerAccount();
        playerOne.setPlayerName(newGameSetup.getPlayerOneName());
        when(playerJpaRepository.save(playerOne)).thenReturn(playerOne);

        PlayerAccount playerTwo = new PlayerAccount();
        playerTwo.setPlayerName(newGameSetup.getPlayerTwoName());
        when(playerJpaRepository.save(playerTwo)).thenReturn(playerTwo);

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);
        mancalaGame.setSecondTurn(0);
        mancalaGame.setLastParticipantIdMove("0");
        when(mancalaJpaRepository.save(any(MancalaGame.class))).thenReturn(mancalaGame);

        Participant participantOne = new Participant();
        participantOne.setId(CURR_GAME_PARTICIPANT_ID_ONE);

        Participant participantTwo = new Participant();
        participantTwo.setId(CURR_GAME_PARTICIPANT_ID_TWO);

        mancalaGame.getParticipantSet().add(participantOne);
        mancalaGame.getParticipantSet().add(participantTwo);
        MancalaGame definedGameSetup = gameStartService.defineGameSetup(newGameSetup);

        assertThat(definedGameSetup.getGameStatus(), is(GameStatesEnum.INITIALIZED));
        assertThat(definedGameSetup.getTableCurrentStatesList().size(), is(14));
        assertThat(definedGameSetup.getParticipantSet().size(), is(2));
        assertThat(definedGameSetup.getTableCurrentStatesList().stream().map(TableCurrentState::getPit).count(), is(14L));

        assertThat(definedGameSetup.getParticipantSet(), containsInAnyOrder(
                allOf(
                        hasProperty("playerAccount", is(playerOne)),
                        hasProperty("playerNumber", is(1)),
                        hasProperty("playerPitList", hasSize(7))
                ),
                allOf(
                        hasProperty("playerAccount", is(playerTwo)),
                        hasProperty("playerNumber", is(2)),
                        hasProperty("playerPitList", hasSize(7))
                )
        ));
    }
}
