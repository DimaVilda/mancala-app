package com.vilda.mancala.mancalaapp.util;

import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.mappers.GameStatesEnumMapper;
import com.vilda.mancala.mancalaapp.mappers.TableCurrentStateMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MancalaBoardSetupUtilsTest {

    @Mock
    private GameStatesEnumMapper gameStatesEnumMapper;
    @Mock
    private TableCurrentStateMapper tableCurrentStateMapper;
    @InjectMocks
    private MancalaBoardSetupUtils mancalaBoardSetupUtils;
    private static final String TEST_GAME_ID = "testGameId";
    private static final String CURR_GAME_PARTICIPANT_ID = "participantId";
    private static final String PARTICIPANT_ID_NEXT_MOVE = "participantIdNextMove";

    @Test
    void shouldGetGameBoardSetupResponseBody() {
        TableCurrentState tableCurrentState = new TableCurrentState();
        tableCurrentState.setStonesCountInPit(2);
        tableCurrentState.setId("stateId");

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setId(TEST_GAME_ID);
        mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
        mancalaGame.setTableCurrentStatesList(Collections.singletonList(tableCurrentState));

        when(gameStatesEnumMapper.toGameStatusEnumViewModel(mancalaGame.getGameStatus())).
                thenReturn(com.vilda.mancala.mancalaapp.client.spec.model.GameStatesEnum.INITIALIZED);
        when(tableCurrentStateMapper.toTableCurrentStateViewModelList(mancalaGame.getTableCurrentStatesList()))
                .thenReturn(anyList());

        mancalaBoardSetupUtils.getGameBoardSetupResponseBody(mancalaGame, CURR_GAME_PARTICIPANT_ID,
                0, 7, PARTICIPANT_ID_NEXT_MOVE);
    }
}
