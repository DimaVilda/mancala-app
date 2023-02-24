package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;

public interface GameMoveService {

    MancalaBoardSetup makeMove(MancalaGame mancalaGame, String gameId, Integer pitIndex,
                               Participant gameCurrentParticipant, boolean isCurrentParticipantFirst);
}
