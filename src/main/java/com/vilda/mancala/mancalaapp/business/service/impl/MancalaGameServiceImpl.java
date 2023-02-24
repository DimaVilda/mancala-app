package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.business.service.GameMoveService;
import com.vilda.mancala.mancalaapp.business.service.GameStartService;
import com.vilda.mancala.mancalaapp.business.service.MancalaGameService;
import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.exceptions.BadRequestException;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.MancalaJpaRepository;
import com.vilda.mancala.mancalaapp.repository.ParticipantJpaRepository;
import com.vilda.mancala.mancalaapp.util.GameSetupResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.vilda.mancala.mancalaapp.util.constants.MancalaGameConstants.PLAYER_ONE_BIG_STONE_INDEX;

@Service
@RequiredArgsConstructor
@Slf4j
public class MancalaGameServiceImpl implements MancalaGameService {

    private final MancalaJpaRepository mancalaJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final GameSetupResponseUtils gameSetupResponseUtils;
    private final GameStartService gameStartService;
    private final GameMoveService gameMoveService;

    @Override
    @Transactional
    public GameSetupResponse startNewGame(NewGameSetup newGameSetup) {
        MancalaGame newMancalaGame = gameStartService.defineGameSetup(newGameSetup);
        return gameSetupResponseUtils.getNewGameSetupResponseBody(newMancalaGame);
    }

    //TODO hibernate show sql
    @Override
    @Transactional
    public MancalaBoardSetup makeMove(String gameId, String currentGameParticipantId, Integer pitIndex) { //TODO check test pit indexes in put path, if validation for min(1) and max(14) really works
        log.debug("Trying to get mancala game by id {}: ", gameId);

        checkIfInputPitIsBigPit(pitIndex);
        MancalaGame mancalaGame = getGameById(gameId);

        switch (mancalaGame.getGameStatus()) {
            case ENDED:
            case PARTICIPANT_ONE_WINS:
            case PARTICIPANT_TWO_WINS:
            case DRAW:
                throw new BadRequestException("A game with provided id was already ended " + gameId);
            case INITIALIZED:
                checkProvidedPitIndexRangeForFirstPlayer(pitIndex);

                Participant firstParticipantByMancalaGameId = findParticipantById(currentGameParticipantId);
                checkIfProvidedGameParticipantIdIsFirst(firstParticipantByMancalaGameId.getId(), currentGameParticipantId);

                return gameMoveService.makeMove(mancalaGame, gameId, pitIndex, firstParticipantByMancalaGameId, true);
            case IN_PROGRESS:
                //check if its really turn of provided in request body game participant
                checkProvidedParticipantIdTurn(mancalaGame, currentGameParticipantId);

                //check if player chosed correct pit range
                Participant gameCurrentParticipant = findParticipantById(currentGameParticipantId);
                boolean isCurrentParticipantFirst = gameCurrentParticipant.getPlayerNumber() == 1;

                checkPitIndexRangeForCurrentParticipant(pitIndex, isCurrentParticipantFirst);
                return gameMoveService.makeMove(mancalaGame, gameId, pitIndex, gameCurrentParticipant, isCurrentParticipantFirst);
        }
        return null;
    }

    private void checkProvidedPitIndexRangeForFirstPlayer(Integer pitIndex) {
        if (pitIndex > PLAYER_ONE_BIG_STONE_INDEX) {
            throw new BadRequestException("Player 2 cannot start a new game, you should chose player 1 pits, " +
                    "provided pit has number " + pitIndex);
        }
    }

    private void checkIfProvidedGameParticipantIdIsFirst(String firstGameParticipantId, String currentGameParticipantId) {
        //check is provided participant id is first
        if (!firstGameParticipantId.equals(currentGameParticipantId)) {
            log.error("");

            throw new BadRequestException("Game participant should be first to make a first move! " +
                    "Provided participant id " + currentGameParticipantId + " belongs to player number two!");
        }
    }

    private void checkProvidedParticipantIdTurn(MancalaGame mancalaGame, String currentMoveMakerParticipantId) {
        String lastParticipantId = mancalaGame.getLastParticipantIdMove(); //participant id who did the last move

        if (mancalaGame.getSecondTurn() == 1) { //if last move maker has a second turn so current participant id should be equal to last
            if (!currentMoveMakerParticipantId.equals(lastParticipantId)) {
                log.error("");

                throw new BadRequestException("Game participant " + mancalaGame.getLastParticipantIdMove() + " has a second move turn !" +
                        "Please provide his id in request body to make a move");
            }

        } else { //if no second turn for last move maker id
            if (currentMoveMakerParticipantId.equals(lastParticipantId)) { //if no second turn but provided same id as last move maker - throw exception
                log.error("");

                throw new BadRequestException("Provided game participant " + currentMoveMakerParticipantId + " already did move last time!" +
                        "It's another player turn now");
            }
        }
    }

    private void checkPitIndexRangeForCurrentParticipant(Integer pitIndex, boolean isCurrentParticipantFirst) {
        if (isCurrentParticipantFirst) {
            if (pitIndex > PLAYER_ONE_BIG_STONE_INDEX) {
                log.error("");

                throw new BadRequestException("Pit index should be in range from 0 to 5 for first participant!" +
                        "Provided pit index is " + pitIndex);
            }
        } else {
            if (pitIndex < PLAYER_ONE_BIG_STONE_INDEX) {
                log.error("");

                throw new BadRequestException("Pit index should be in range from 7 to 12 for second participant!" +
                        "Provided pit index is " + pitIndex);
            }
        }
    }

    private Participant findParticipantById(String participantId) {
        return participantJpaRepository.findById(participantId)
                .orElseThrow(() -> {
                    log.error("");

                    return new NotFoundException("There is no participant in this game by provided id " + participantId);
                });
    }

    private MancalaGame getGameById(String gameId) {
        return mancalaJpaRepository.findById(gameId)
                .orElseThrow(() -> {
                    log.error("Mancala game with id {} does not exist", gameId);
                    return new NotFoundException("Mancala game not found by id " + gameId);
                });
    }

    private void checkIfInputPitIsBigPit(int pitIndex) {
        if (pitIndex == 6 || pitIndex == 13) {
            log.error("");

            throw new BadRequestException("You can't make a move from a big pit, pls chose correct pit, " +
                    "provided pit has number " + pitIndex);
        }
    }
}