package com.vilda.mancala.mancalaapp.clientapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilda.mancala.mancalaapp.business.service.MancalaGameService;
import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MancalaGameController.class)
public class MancalaGameControllerTest {

    @MockBean
    private MancalaGameService mancalaGameService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String GAME_ID = "c2d247bc-b2a1-4ab9-b1c0-df226584497f";
    private static final String PARTICIPANT_ID = "c2d247bc-b2a1-4ab9-b1c0-df226584497f";
    private static final Integer PIT_INDEX = 1;
    private static final String START_GAME_URL = "/client-api/v1/games";
    private static final String MAKE_MOVE_URL = "/client-api/v1/games/{gameId}/participants/{participantId}/pits/{pitIndex}";


    @Test
    void shouldStartGame() throws Exception {
        NewGameSetup newGameSetupRequestBody = new NewGameSetup();
        newGameSetupRequestBody.setPlayerOneName("playerOneName");
        newGameSetupRequestBody.setPlayerTwoName("playerTwoName");


        GameSetupResponse expectedResponse = new GameSetupResponse();
        expectedResponse.setGameId(GAME_ID);

        when(mancalaGameService
                .startNewGame(newGameSetupRequestBody)).thenReturn(expectedResponse);

        mockMvc.perform(post(START_GAME_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGameSetupRequestBody)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMakeMoveByPitId() throws Exception {
        MancalaBoardSetup expectedResponse = new MancalaBoardSetup();
        expectedResponse.setGameId(GAME_ID);

        when(mancalaGameService
                .makeMove(GAME_ID, PARTICIPANT_ID, PIT_INDEX)).thenReturn(expectedResponse);

        mockMvc.perform(patch(MAKE_MOVE_URL, GAME_ID, PARTICIPANT_ID, PIT_INDEX)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
