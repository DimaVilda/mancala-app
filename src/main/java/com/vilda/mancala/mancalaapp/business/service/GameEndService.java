package com.vilda.mancala.mancalaapp.business.service;

import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.domain.TableCurrentState;

public interface GameEndService {
    MancalaBoardSetup defineGameWinner(MancalaGame mancalaGame, Participant gameCurrentParticipant,
                                       boolean isCurrentParticipantFirst, TableCurrentState tableCurrentStateForLastStone,
                                       int pitIndex);
}
