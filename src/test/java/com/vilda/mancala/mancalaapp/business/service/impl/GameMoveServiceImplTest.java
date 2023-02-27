package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.GameEndService;
import com.vilda.mancala.mancalaapp.business.service.NextMoveDefinitionService;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.domain.Pit;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.repository.TableCurrentStateRepository;
import com.vilda.mancala.mancalaapp.util.MancalaBoardSetupUtils;
import com.vilda.mancala.mancalaapp.util.MoveEntityUtils;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
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

import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.MANCALA_STONES_COUNT_IN_PIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameMoveServiceImplTest {

    @Mock
    private TableCurrentStateRepository tableCurrentStateRepository;
    @Mock
    private MancalaBoardSetupUtils mancalaBoardSetupUtils;
    @Mock
    private MoveEntityUtils moveEntityUtils;
    @Mock
    private GameEndService gameEndService;
    @Mock
    private NextMoveDefinitionService nextMoveDefinitionService;
    @Mock
    private TableCurrentStatePersistenceService tableCurrentStatePersistenceService;
    @Captor
    private ArgumentCaptor<MancalaGame> mancalaGameArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> participantIdNextMove;
    @InjectMocks
    private GameMoveServiceImpl gameMoveServiceImpl;

    private static final String TEST_GAME_ID = "testGameId";
    private static final String CURR_GAME_PARTICIPANT_ID_ONE = "participantIdOne";
    private static final String CURR_GAME_PARTICIPANT_ID_TWO = "participantIdTwo";

    @Test
    void shouldThrowBadRequestExceptionWhenProvidedPitIsEmpty() {
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setId(TEST_GAME_ID);

        TableCurrentState tableCurrentStateWithZeroStones = new TableCurrentState();
        tableCurrentStateWithZeroStones.setStonesCountInPit(0);

        when(tableCurrentStatePersistenceService.findTableCurrentStateByMancalaGameIdAndPitIndex(any(), any()))
                .thenReturn(tableCurrentStateWithZeroStones);
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> gameMoveServiceImpl.makeMove(mancalaGame, 1, "anyId", true));
        assertThat(exception.getMessage(), is("Chosen pit is empty, please chose pit with at least one stone inside!"));
        verifyNoInteractions(tableCurrentStateRepository);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void shouldMakeFirstMoveAndPutGameStatusInProgressAndNotFinishGame(int pitIndexInPath) {
        Participant participantOne = new Participant();
        participantOne.setPlayerNumber(1);
        participantOne.setId(CURR_GAME_PARTICIPANT_ID_ONE);

        Participant participantTwo = new Participant();
        participantTwo.setPlayerNumber(2);
        participantTwo.setId(CURR_GAME_PARTICIPANT_ID_TWO);

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setId(TEST_GAME_ID);
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);
        mancalaGame.setLastParticipantIdMove("0");
        mancalaGame.setParticipantSet(Sets.set(participantOne, participantTwo));

        Pit pit = new Pit();
        pit.setIsBigPit(0);
        pit.setPitIndex(pitIndexInPath);

        TableCurrentState tableCurrentStateByProvidedPit = new TableCurrentState();
        tableCurrentStateByProvidedPit.setPit(pit);
        tableCurrentStateByProvidedPit.setMancalaGame(mancalaGame);
        tableCurrentStateByProvidedPit.setStonesCountInPit(MANCALA_STONES_COUNT_IN_PIT);

        List<TableCurrentState> tableCurrentStateList = defineTableCurrentStatesForNextPitIndexes(pitIndexInPath);
        when(tableCurrentStatePersistenceService.findTableCurrentStateByMancalaGameIdAndPitIndex(any(), any()))
                .thenReturn(tableCurrentStateByProvidedPit);
        when(tableCurrentStateRepository.findTableCurrentStatesByMancalaGameAndPitPitIndexIn(any(), any())).thenReturn(tableCurrentStateList);

        if (pitIndexInPath == 0) {
            when(nextMoveDefinitionService.isCurrentGameParticipantNextMove(
                    any(TableCurrentState.class), any(MancalaGame.class), anyString(), anyBoolean(), anyString())).thenReturn(false);
        } else {
            when(nextMoveDefinitionService.isCurrentGameParticipantNextMove(
                    any(TableCurrentState.class), any(MancalaGame.class), anyString(), anyBoolean(), anyString())).thenReturn(true);
        }

        gameMoveServiceImpl.makeMove(mancalaGame, pitIndexInPath, CURR_GAME_PARTICIPANT_ID_ONE, true);
        verifyNoInteractions(gameEndService);
        verify(mancalaBoardSetupUtils).getGameBoardSetupResponseBody(mancalaGameArgumentCaptor.capture(), any(), any(), any(), participantIdNextMove.capture());

        MancalaGame changedMancalaGame = mancalaGameArgumentCaptor.getValue();
        String definedParticipantIdNextMove = participantIdNextMove.getValue();

        assertThat(changedMancalaGame.getGameStatus(), is(GameStatesEnum.IN_PROGRESS));
        if (pitIndexInPath == 0) {
            assertThat(definedParticipantIdNextMove, is(CURR_GAME_PARTICIPANT_ID_ONE));
        } else {
            assertThat(definedParticipantIdNextMove, is(CURR_GAME_PARTICIPANT_ID_TWO));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeMoveWByPlayerTwoWhenGameStatusAlreadyInProgress(boolean allGameParticipantPitsPitsAreEmpty) {
        int pitIndexInPath = 7;

        Participant participantOne = new Participant();
        participantOne.setPlayerNumber(1);
        participantOne.setId(CURR_GAME_PARTICIPANT_ID_ONE);

        Participant participantTwo = new Participant();
        participantTwo.setPlayerNumber(2);
        participantTwo.setId(CURR_GAME_PARTICIPANT_ID_TWO);

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setId(TEST_GAME_ID);
        mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
        mancalaGame.setLastParticipantIdMove(CURR_GAME_PARTICIPANT_ID_ONE);
        mancalaGame.setParticipantSet(Sets.set(participantOne, participantTwo));

        Pit pit = new Pit();
        pit.setIsBigPit(0);
        pit.setPitIndex(pitIndexInPath);

        TableCurrentState tableCurrentStateByProvidedPit = new TableCurrentState();
        tableCurrentStateByProvidedPit.setPit(pit);
        tableCurrentStateByProvidedPit.setMancalaGame(mancalaGame);
        tableCurrentStateByProvidedPit.setStonesCountInPit(MANCALA_STONES_COUNT_IN_PIT);

        List<TableCurrentState> tableCurrentStateList = defineTableCurrentStatesForNextPitIndexes(pitIndexInPath);
        when(tableCurrentStatePersistenceService.findTableCurrentStateByMancalaGameIdAndPitIndex(any(), any()))
                .thenReturn(tableCurrentStateByProvidedPit);
        when(tableCurrentStateRepository.findTableCurrentStatesByMancalaGameAndPitPitIndexIn(any(), any())).thenReturn(tableCurrentStateList);
        when(tableCurrentStateRepository.arePitsEmptyByGameIdAndParticipantId(anyString(), anyString())).thenReturn(allGameParticipantPitsPitsAreEmpty);

        gameMoveServiceImpl.makeMove(mancalaGame, pitIndexInPath, CURR_GAME_PARTICIPANT_ID_TWO, false);
        if (allGameParticipantPitsPitsAreEmpty) {
            verify(gameEndService).defineGameWinner(any(MancalaGame.class), anyString(), anyBoolean(), anyInt(), anyInt());
        } else {
            verifyNoInteractions(gameEndService);
        }

        verify(mancalaBoardSetupUtils).getGameBoardSetupResponseBody(
                any(), any(), any(), any(), participantIdNextMove.capture());

        String definedParticipantIdNextMove = participantIdNextMove.getValue();
        if (allGameParticipantPitsPitsAreEmpty) {
            assertThat(definedParticipantIdNextMove, is("0"));
        } else {
            assertThat(definedParticipantIdNextMove, is(CURR_GAME_PARTICIPANT_ID_TWO));
        }
    }

    private List<TableCurrentState> defineTableCurrentStatesForNextPitIndexes(int pitIndexInPath) {
        List<TableCurrentState> tableCurrentStateList = new ArrayList<>();

        for (int i = pitIndexInPath + 1; i <= pitIndexInPath + MANCALA_STONES_COUNT_IN_PIT; i++) {
            Pit pit = new Pit();
            pit.setPitIndex(i);

            TableCurrentState tableCurrentState = new TableCurrentState();
            tableCurrentState.setPit(pit);
            tableCurrentState.setStonesCountInPit((i != 6) ? MANCALA_STONES_COUNT_IN_PIT : 0); //check for index 6 - big bit

            tableCurrentStateList.add(tableCurrentState);
        }
        return tableCurrentStateList;
    }
}
