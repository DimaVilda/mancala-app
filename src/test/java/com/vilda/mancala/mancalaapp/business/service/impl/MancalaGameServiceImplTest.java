package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.GameMoveService;
import com.vilda.mancala.mancalaapp.business.service.GameStartService;
import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.MancalaJpaRepository;
import com.vilda.mancala.mancalaapp.repository.ParticipantJpaRepository;
import com.vilda.mancala.mancalaapp.util.GameSetupResponseUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.PLAYER_ONE_BIG_STONE_INDEX;
import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.PLAYER_TWO_BIG_STONE_INDEX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MancalaGameServiceImplTest {

    @Mock
    private MancalaJpaRepository mancalaJpaRepository;
    @Mock
    private ParticipantJpaRepository participantJpaRepository;
    @Mock
    private GameSetupResponseUtils gameSetupResponseUtils;
    @Mock
    private GameStartService gameStartService;
    @Mock
    private GameMoveService gameMoveService;
    @InjectMocks
    private MancalaGameServiceImpl mancalaGameServiceImpl;
    private static final String TEST_GAME_ID = "testGameId";
    private static final String CURR_GAME_PARTICIPANT_ID_ONE = "participantIdOne";
    private static final String CURR_GAME_PARTICIPANT_ID_TWO = "participantIdTwo";

    @Test
    void shouldStartNewGameAndCallResponseBosyBuildMethod() {
        NewGameSetup requestBody = new NewGameSetup();
        requestBody.setPlayerOneName("playerOneName");
        requestBody.setPlayerTwoName("playerTwoName");

        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setId(TEST_GAME_ID);

        GameSetupResponse gameSetupResponse = new GameSetupResponse();
        gameSetupResponse.setGameId(TEST_GAME_ID);
        when(gameStartService.defineGameSetup(requestBody)).thenReturn(mancalaGame);
        mancalaGameServiceImpl.startNewGame(requestBody);

        verify(gameSetupResponseUtils).getNewGameSetupResponseBody(mancalaGame);
        assertThat(gameSetupResponse.getGameId(), is(mancalaGame.getId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {PLAYER_ONE_BIG_STONE_INDEX, PLAYER_TWO_BIG_STONE_INDEX})
    void shouldThrowBadRequestExceptionWhenProvidedPitIndexIsBigPit(Integer pitIndex) {

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> mancalaGameServiceImpl.makeMove(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID_ONE, pitIndex));
        assertThat(exception.getMessage(), is("You can't make a move from a big pit, pls chose correct pit, " +
                "provided pit has number " + pitIndex));
        verifyNoInteractions(mancalaJpaRepository);

    }

    @Test
    void shouldThrowNotFoundExceptionWhenGameWithProvidedIdDoesNotExist() {
        Integer pitIndex = 1;
        String notExistGameId = "notExistGameId";

        when(mancalaJpaRepository.findById(notExistGameId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> mancalaGameServiceImpl.makeMove(notExistGameId, CURR_GAME_PARTICIPANT_ID_ONE, pitIndex));
        assertThat(exception.getMessage(), is("Mancala game not found by id " + notExistGameId));
    }

    @ParameterizedTest
    @EnumSource(value = GameStatesEnum.class, names = {"PARTICIPANT_ONE_WINS", "PARTICIPANT_TWO_WINS", "DRAW"})
    void shouldThrowBadRequestExceptionWhenMakeMoveWithEndGameStatuses(GameStatesEnum gameStatesEnum) {
        Integer pitIndex = 1;
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(gameStatesEnum);

        when(mancalaJpaRepository.findById(TEST_GAME_ID)).thenReturn(Optional.of(mancalaGame));
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> mancalaGameServiceImpl.makeMove(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID_ONE, pitIndex));
        assertThat(exception.getMessage(), is("A game with provided id was already ended " + TEST_GAME_ID));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenParticipantByIdAndMancalaGameIdWasNotFound() {
        Integer pitIndex = 1;
        String notExistParticipantId = "notExistParticipant";
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);

        when(mancalaJpaRepository.findById(TEST_GAME_ID)).thenReturn(Optional.of(mancalaGame));
        when(participantJpaRepository.findParticipantNumberByIdAndGameId(notExistParticipantId, TEST_GAME_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> mancalaGameServiceImpl.makeMove(TEST_GAME_ID, notExistParticipantId, pitIndex));
        assertThat(exception.getMessage(), is("There is no participant in this game by provided id " + notExistParticipantId
                + " and provided game id " + TEST_GAME_ID));
        verifyNoInteractions(gameMoveService);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenParticipantIsNotFirstOnGameFirstMove() {
        Integer pitIndex = 1;
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);

        when(mancalaJpaRepository.findById(TEST_GAME_ID)).thenReturn(Optional.of(mancalaGame));
        when(participantJpaRepository.findParticipantNumberByIdAndGameId(CURR_GAME_PARTICIPANT_ID_TWO, TEST_GAME_ID)).thenReturn(Optional.of(2));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> mancalaGameServiceImpl.makeMove(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID_TWO, pitIndex));
        assertThat(exception.getMessage(), is("Game participant should be first to make a first move! " +
                "Provided participant id " + CURR_GAME_PARTICIPANT_ID_TWO + " belongs to player number two!"));
        verifyNoInteractions(gameMoveService);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenProvidedPitIndexNotInCorrectRangeForPlayerOne() {
        Integer incorrectPitIndexForPlayerOne = 7;
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);

        when(mancalaJpaRepository.findById(TEST_GAME_ID)).thenReturn(Optional.of(mancalaGame));
        when(participantJpaRepository.findParticipantNumberByIdAndGameId(CURR_GAME_PARTICIPANT_ID_ONE, TEST_GAME_ID)).thenReturn(Optional.of(1));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> mancalaGameServiceImpl.makeMove(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID_ONE, incorrectPitIndexForPlayerOne));
        assertThat(exception.getMessage(), is("Player 2 pits could not start a new game, you should chose player 1 pits, " +
                "provided pit has number " + incorrectPitIndexForPlayerOne));
        verifyNoInteractions(gameMoveService);
    }

    @Test
    void shouldMakeFirstMoveByFirstGameParticipantAndReturnUpdatedMancalaBoardSetup() {
        Integer pitIndex = 1;
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.INITIALIZED);
        mancalaGame.setId(TEST_GAME_ID);

        MancalaBoardSetup mancalaBoardSetup = new MancalaBoardSetup();
        mancalaBoardSetup.setGameId(TEST_GAME_ID);

        when(mancalaJpaRepository.findById(TEST_GAME_ID)).thenReturn(Optional.of(mancalaGame));
        when(participantJpaRepository.findParticipantNumberByIdAndGameId(CURR_GAME_PARTICIPANT_ID_ONE, TEST_GAME_ID)).thenReturn(Optional.of(1));
        when(gameMoveService.makeMove(mancalaGame, pitIndex, CURR_GAME_PARTICIPANT_ID_ONE, true)).thenReturn(mancalaBoardSetup);

        MancalaBoardSetup responseBody =
                mancalaGameServiceImpl.makeMove(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID_ONE, pitIndex);

        assertThat(responseBody, is(mancalaBoardSetup));
        verify(gameMoveService).makeMove(mancalaGame, pitIndex, CURR_GAME_PARTICIPANT_ID_ONE, true);
    }

    @ParameterizedTest
    @CsvSource({"1,2,participantTwo", "0,1,participantOne"})
    void shouldThrowBadRequestExceptionWhenGameInProgressButNotProvidedParticipantTurn(int secondTurn, int participantNumber,
                                                                                       String currentGameParticipantIdInPath) {
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
        mancalaGame.setSecondTurn(secondTurn);
        mancalaGame.setLastParticipantIdMove(CURR_GAME_PARTICIPANT_ID_ONE);

        when(mancalaJpaRepository.findById(TEST_GAME_ID)).thenReturn(Optional.of(mancalaGame));
        when(participantJpaRepository.findParticipantNumberByIdAndGameId(currentGameParticipantIdInPath, TEST_GAME_ID)).thenReturn(Optional.of(participantNumber));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> mancalaGameServiceImpl.makeMove(TEST_GAME_ID, currentGameParticipantIdInPath, anyInt())); //TODO run test to check any()_
        if (secondTurn == 1) {
            assertThat(exception.getMessage(), is("Game participant " + mancalaGame.getLastParticipantIdMove() + " has a second move turn!" +
                    " Please provide his id in request body to make a move"));
        } else {
            assertThat(exception.getMessage(), is("Provided game participant " + currentGameParticipantIdInPath + " already did move last time!" +
                    "It's another player turn now"));
        }
        verifyNoInteractions(gameMoveService);
    }

    @ParameterizedTest
    @CsvSource({"1,10,1,participantOne", "2,3,0,participantTwo"})
    void shouldThrowBadRequestExceptionWhenProvidedPitIndexIsNotInRangeForProvidedParticipant(int participantNumber,
                                                                                              int incorrectPitIndex,
                                                                                              int secondTurn,
                                                                                              String currentGameParticipantIdInPath) {
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
        mancalaGame.setSecondTurn(secondTurn);
        mancalaGame.setLastParticipantIdMove(CURR_GAME_PARTICIPANT_ID_ONE);

        when(mancalaJpaRepository.findById(TEST_GAME_ID)).thenReturn(Optional.of(mancalaGame));
        when(participantJpaRepository.findParticipantNumberByIdAndGameId(currentGameParticipantIdInPath, TEST_GAME_ID))
                .thenReturn(Optional.of(participantNumber));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> mancalaGameServiceImpl.makeMove(TEST_GAME_ID, currentGameParticipantIdInPath, incorrectPitIndex));
        if (participantNumber == 1) {
            assertThat(exception.getMessage(), is("Pit index should be in range from 0 to 5 for first participant!" +
                    "Provided pit index is " + incorrectPitIndex));
        } else {
            assertThat(exception.getMessage(), is("Pit index should be in range from 7 to 12 for second participant!" +
                    "Provided pit index is " + incorrectPitIndex));
        }
        verifyNoInteractions(gameMoveService);
    }

    @Test
    void shouldMakeMoveByGameParticipantAndReturnUpdatedMancalaBoardSetup() {
        Integer pitIndex = 7;
        MancalaGame mancalaGame = new MancalaGame();
        mancalaGame.setGameStatus(GameStatesEnum.IN_PROGRESS);
        mancalaGame.setSecondTurn(0);
        mancalaGame.setLastParticipantIdMove(CURR_GAME_PARTICIPANT_ID_ONE);

        MancalaBoardSetup mancalaBoardSetup = new MancalaBoardSetup();
        mancalaBoardSetup.setGameId(TEST_GAME_ID);

        when(mancalaJpaRepository.findById(TEST_GAME_ID)).thenReturn(Optional.of(mancalaGame));
        when(participantJpaRepository.findParticipantNumberByIdAndGameId(CURR_GAME_PARTICIPANT_ID_TWO, TEST_GAME_ID)).thenReturn(Optional.of(2));
        when(gameMoveService.makeMove(mancalaGame, pitIndex, CURR_GAME_PARTICIPANT_ID_TWO, false)).thenReturn(mancalaBoardSetup);

        MancalaBoardSetup responseBody = mancalaGameServiceImpl.makeMove(TEST_GAME_ID, CURR_GAME_PARTICIPANT_ID_TWO, pitIndex);
        verify(gameMoveService).makeMove(mancalaGame, pitIndex, CURR_GAME_PARTICIPANT_ID_TWO, false);
        assertThat(responseBody, is(mancalaBoardSetup));
    }
}
