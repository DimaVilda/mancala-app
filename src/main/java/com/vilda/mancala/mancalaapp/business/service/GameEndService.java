package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;

public interface GameEndService {
    MancalaBoardSetup defineGameWinner(MancalaGame mancalaGame, String gameCurrentParticipantId,
                                       boolean isCurrentParticipantFirst, int pitIndexFrom, int pitIndexTo);
}
