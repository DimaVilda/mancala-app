package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;

/**
 * Service responsible for setup game table for the new game
 */
public interface GameStartService {

    /**
     * Define game participants, setup pits and stones
     *
     * @param newGameSetup - {@link NewGameSetup} - request body with game participant names to start a new game
     * @return - persisted {@link MancalaGame} entity with defined data
     */
    MancalaGame defineGameSetup(NewGameSetup newGameSetup);
}
