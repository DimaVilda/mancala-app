package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;

/**
 * Service for interacting with game.
 */
public interface MancalaGameService {

    /**
     * Start new game by defining game users
     *
     * @param newGameSetup -{@link NewGameSetup} - new game setup request
     * @return {@link GameSetupResponse} - defined fresh new game id and defined two game participants from requestBody
     */
    GameSetupResponse startNewGame(NewGameSetup newGameSetup);

    /**
     * Make move in defined game by certain game participant from certain pit belongs to this participant
     *
     * @param gameId - game where move will be made
     * @param participantId - one og the game's participants who will made a move
     * @param pitIndex - pit's number on the game table from which game participant will grab all stones
     *
     * @return {@link MancalaBoardSetup} - current game table state after move was made
     */
    MancalaBoardSetup makeMove(String gameId, String participantId, Integer pitIndex);
}
