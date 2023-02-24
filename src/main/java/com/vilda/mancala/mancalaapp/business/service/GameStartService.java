package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;

public interface GameStartService {

    MancalaGame defineGameSetup(NewGameSetup newGameSetup);
}
