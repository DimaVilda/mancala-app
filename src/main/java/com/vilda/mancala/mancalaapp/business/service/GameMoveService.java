package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;

/**
 * Service responsible for updating game table state while move in progress
 */
public interface GameMoveService {

    /**
     * Define a game logic for move making
     *
     * @param mancalaGame - current game entity
     * @param pitIndex - pit index from which all stones was grabbed to be placed in the next pits
     * @param currentGameParticipantId - game participant who makes a move
     * @param isCurrentParticipantFirst - marker is current move maker is first
     *
     * @return - {@link MancalaBoardSetup} - updated current game table state after move was made
     */
    MancalaBoardSetup makeMove(MancalaGame mancalaGame, Integer pitIndex,
                               String currentGameParticipantId, boolean isCurrentParticipantFirst);
}
