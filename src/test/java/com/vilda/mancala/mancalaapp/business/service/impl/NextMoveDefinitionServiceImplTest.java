package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.domain.Pit;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NextMoveDefinitionServiceImplTest {

    @Mock
    private TableCurrentStatePersistenceService tableCurrentStatePersistenceService;
    @InjectMocks
    private NextMoveDefinitionServiceImpl nextMoveDefinitionService;
    private static final String TEST_GAME_ID = "testGameId";
    private static final String CURR_GAME_PARTICIPANT_ID = "participantId";

    @ParameterizedTest
    @CsvSource({"0, 0", "1, 10", "0, 10"})
    void shouldDefineIsNextParticipantWillMoveNext(int isBigPit, int stonesCountInPit) {
        Participant participant = new Participant();
        participant.setPlayerNumber(1);

        Pit pit = new Pit();
        pit.setIsBigPit(isBigPit);
        pit.setPitIndex(definePitIndex(isBigPit));
        pit.setParticipant(participant);

        TableCurrentState tableCurrentStateForLastStone = new TableCurrentState();
        tableCurrentStateForLastStone.setStonesCountInPit(stonesCountInPit);
        tableCurrentStateForLastStone.setPit(pit);

        TableCurrentState tableCurrentStateOfTheOppositePit = new TableCurrentState();
        tableCurrentStateOfTheOppositePit.setStonesCountInPit(10);
        if (isBigPit == 0 && stonesCountInPit == 0) { // this mock is only for the first case
            when(tableCurrentStatePersistenceService.findTableCurrentStateByMancalaGameIdAndPitIndex(any(), any()))
                    .thenReturn(tableCurrentStateOfTheOppositePit);
        }

        boolean isNextParticipantMoveNext = nextMoveDefinitionService.isCurrentGameParticipantNextMove(tableCurrentStateForLastStone, new MancalaGame(),
                TEST_GAME_ID, true, CURR_GAME_PARTICIPANT_ID);
        if (isBigPit == 0 && stonesCountInPit == 0) {
            assertTrue(isNextParticipantMoveNext);
        } else if (isBigPit == 1) {
            assertFalse(isNextParticipantMoveNext);
        } else { //third case
            assertTrue(isNextParticipantMoveNext);
        }
    }

    private Integer definePitIndex(int isBigPit) {
        if (isBigPit == 1) {
            if (true) {
                return 6;
            } else {
                return 13;
            }
        } else {
            if (true) {
                return 5;
            } else {
                return 10;
            }
        }
    }
}
