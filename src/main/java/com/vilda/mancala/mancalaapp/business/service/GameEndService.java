package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;

/**
 * Service responsible for end of the game cases
 */
public interface GameEndService {

    /**
     * Define a game winner
     *
     * @param mancalaGame - current game entity
     * @param gameCurrentParticipantId - game participant who makes a move
     * @param isCurrentParticipantFirst - marker is current move maker is first
     * @param pitIndexFrom - pit index from which all stones was grabbed to be placed in the next pits
     * @param pitIndexTo - pit index to which the last stone will be placed
     *
     * @return - {@link MancalaBoardSetup} - updated current game table state by defined winner
     */
    MancalaBoardSetup defineGameWinner(MancalaGame mancalaGame, String gameCurrentParticipantId,
                                       boolean isCurrentParticipantFirst, int pitIndexFrom, int pitIndexTo);
}
