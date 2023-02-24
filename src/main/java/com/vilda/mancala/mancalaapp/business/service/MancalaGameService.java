package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.MoveItem;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;

public interface MancalaGameService {

    GameSetupResponse startNewGame(NewGameSetup newGameSetup);

    MancalaBoardSetup makeMove(MoveItem moveItem, Integer pitIndex);
}
