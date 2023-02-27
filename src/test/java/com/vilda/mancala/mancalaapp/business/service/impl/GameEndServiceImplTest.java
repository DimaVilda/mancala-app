package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Pit;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.repository.TableCurrentStateRepository;
import com.vilda.mancala.mancalaapp.util.MancalaBoardSetupUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameEndServiceImplTest {

    @Mock
    private TableCurrentStateRepository tableCurrentStateRepository;
    @Mock
    private MancalaBoardSetupUtils mancalaBoardSetupUtils;
    @Mock
    private TableCurrentStatePersistenceService tableCurrentStatePersistenceService;
    @Captor
    private ArgumentCaptor<MancalaGame> mancalaGameArgumentCaptor;
    @InjectMocks
    private GameEndServiceImpl gameEndService;
    private static final String TEST_GAME_ID = "testGameId";
    private static final String CURR_GAME_PARTICIPANT_ID_ONE = "participantIdOne";
    private static final String CURR_GAME_PARTICIPANT_ID_TWO = "participantIdTwo";
    private static final int OPPOSITE_PARTICIPANT_STONES_BIG_PIT = 10;

    @ParameterizedTest
    @ValueSource(ints = {10, 20, 9})
    void shouldDefineDifferentGameWinners(int currentGameParticipantStonesCountInBigPit) {
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setId(TEST_GAME_ID);
        mancalaGame.setLastParticipantIdMove(CURR_GAME_PARTICIPANT_ID_TWO);

        List<TableCurrentState> oppositePlayerCurrentStates = tableCurrentStatesTestSetup();
        when(tableCurrentStateRepository.findStonesCountInPitByGameIdAndParticipantId(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID_ONE, 1))
                .thenReturn(currentGameParticipantStonesCountInBigPit);
        when(tableCurrentStateRepository.findTableCurrentStatesByMancalaGameIdAndNotParticipantId(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID_ONE))
                .thenReturn(oppositePlayerCurrentStates);

        gameEndService.defineGameWinner(mancalaGame, CURR_GAME_PARTICIPANT_ID_ONE, true, 5, 10);
        verify(mancalaBoardSetupUtils).getGameBoardSetupResponseBody(mancalaGameArgumentCaptor.capture(), any(), any(), any(), any());
        switch (currentGameParticipantStonesCountInBigPit) {
            case 10:
                assertThat(mancalaGameArgumentCaptor.getValue().getGameStatus(), is(GameStatesEnum.DRAW));
                break;
            case 20:
                assertThat(mancalaGameArgumentCaptor.getValue().getGameStatus(), is(GameStatesEnum.PARTICIPANT_ONE_WINS));
                break;
            case 9:
                assertThat(mancalaGameArgumentCaptor.getValue().getGameStatus(), is(GameStatesEnum.PARTICIPANT_TWO_WINS));
                break;
        }
    }

    private List<TableCurrentState> tableCurrentStatesTestSetup() {
        List<TableCurrentState> tableCurrentStates = new ArrayList<>();
        List<Pit> pitList = pitListTestSetup();
        for (Pit pit : pitList) {
            TableCurrentState tableCurrentState = new TableCurrentState();
            tableCurrentState.setPit(pit);
            tableCurrentState.setStonesCountInPit(pit.getIsBigPit() == 1 ? OPPOSITE_PARTICIPANT_STONES_BIG_PIT : 3);
            tableCurrentStates.add(tableCurrentState);
        }
        return tableCurrentStates;
    }

    private List<Pit> pitListTestSetup() {
        List<Pit> pitList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Pit pit = new Pit();
            pit.setPitIndex(i);
            pit.setIsBigPit(i == 6 ? 1 : 0);
            pitList.add(pit);
        }
        return pitList;
    }
}
