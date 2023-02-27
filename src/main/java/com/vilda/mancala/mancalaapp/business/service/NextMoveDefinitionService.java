package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;

/**
 * Service to define is the NEXT participant has right to make a move or not
 */
public interface NextMoveDefinitionService {

    /***
     * Define is next participant can perform a move.
     * Attention ! It returns boolean decision not for current but for the NEXT game participant
     *
     * @param tableCurrentStateForLastStone - tale state for last pit where participant should put his LAST stone
     * @param mancalaGame - current game entity
     * @param gameId - current game id
     * @param isCurrentParticipantFirst - marker is current game participant has first number in game
     * @param gameCurrentParticipantId - game current move maker id
     *
     * @return - move possibility marker for the NEXT game participant
     */
    boolean isCurrentGameParticipantNextMove(TableCurrentState tableCurrentStateForLastStone,
                                             MancalaGame mancalaGame, String gameId,
                                             boolean isCurrentParticipantFirst, String gameCurrentParticipantId);
}
