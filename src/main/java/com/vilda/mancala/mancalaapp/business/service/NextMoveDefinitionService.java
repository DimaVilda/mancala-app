package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;

public interface NextMoveDefinitionService {

    boolean isCurrentGameParticipantNextMove(TableCurrentState tableCurrentStateForLastStone,
                                             MancalaGame mancalaGame, String gameId,
                                             boolean isCurrentParticipantFirst, String gameCurrentParticipantId);
}
