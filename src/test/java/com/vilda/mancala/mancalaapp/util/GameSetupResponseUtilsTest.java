package com.vilda.mancala.mancalaapp.util;

import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class GameSetupResponseUtilsTest {

    @InjectMocks
    private GameSetupResponseUtils gameSetupResponseUtils;
    private static final String TEST_GAME_ID = "testGameId";
    private static final String CURR_GAME_PARTICIPANT_ID_ONE = "participantIdOne";
    private static final String CURR_GAME_PARTICIPANT_ID_TWO = "participantIdTwo";

    @Test
    void shouldGetNewGameSetupResponseBodyWithTwoParticipantIds() {
        Participant participantOne = new Participant();
        participantOne.setPlayerNumber(1);
        participantOne.setId(CURR_GAME_PARTICIPANT_ID_ONE);

        Participant participantTwo = new Participant();
        participantTwo.setPlayerNumber(2);
        participantTwo.setId(CURR_GAME_PARTICIPANT_ID_TWO);

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setId(TEST_GAME_ID);
        mancalaGame.setParticipantSet(Sets.set(participantOne, participantTwo));

        GameSetupResponse response = gameSetupResponseUtils.getNewGameSetupResponseBody(mancalaGame);
        assertThat(response.getGameId(), is(TEST_GAME_ID));
        assertThat(response.getParticipantOneId(), is(CURR_GAME_PARTICIPANT_ID_ONE));
        assertThat(response.getParticipantTwoId(), is(CURR_GAME_PARTICIPANT_ID_TWO));
    }
}
