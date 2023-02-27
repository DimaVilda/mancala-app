package com.vilda.mancala.mancalaapp.util;

import com.vilda.mancala.mancalaapp.domain.Move;
import com.vilda.mancala.mancalaapp.repository.MoveJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MoveEntityUtilTest {
    @Mock
    private MoveJpaRepository moveJpaRepository;
    @InjectMocks
    private MoveEntityUtils moveEntityUtils;
    @Captor
    private ArgumentCaptor<Move> moveArgumentsCaptor;
    private static final String TEST_GAME_ID = "testGameId";
    private static final String CURR_GAME_PARTICIPANT_ID = "participantId";

    @Test
    void shouldCreateFirstMoveEntity() {
        when(moveJpaRepository.findLastMoveNumberInCurrentGameByParticipantMancalaGameIdAndParticipantId(
                TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID)).thenReturn(Optional.empty());
        moveEntityUtils.createMoveEntity(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID, 0, "fromPitId", "toPitId", 1);

        verify(moveJpaRepository).save(moveArgumentsCaptor.capture());
        assertThat(moveArgumentsCaptor.getValue().getMoveNumber(), is(1));
    }

    @Test
    void shouldCreateMoveEntityWithIncrementedMoveNumber() {
        int lastMoveNumber = 1;
        int incrementedMoveNumber = lastMoveNumber + 1;
        when(moveJpaRepository.findLastMoveNumberInCurrentGameByParticipantMancalaGameIdAndParticipantId(
                TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID)).thenReturn(Optional.of(lastMoveNumber));
        moveEntityUtils.createMoveEntity(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID, 0, "fromPitId", "toPitId", 1);

        verify(moveJpaRepository).save(moveArgumentsCaptor.capture());
        assertThat(moveArgumentsCaptor.getValue().getMoveNumber(), is(incrementedMoveNumber));
    }
}
