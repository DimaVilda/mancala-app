package com.vilda.mancala.mancalaapp.util;

import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import com.vilda.mancala.mancalaapp.domain.Participant;
import com.vilda.mancala.mancalaapp.mappers.GameStatesEnumMapper;
import com.vilda.mancala.mancalaapp.mappers.TableCurrentStateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MancalaBoardSetupUtils {

    private final GameStatesEnumMapper gameStatesEnumMapper;
    private final TableCurrentStateMapper tableCurrentStateMapper;

    public MancalaBoardSetup getGameBoardSetupResponseBody(MancalaGame mancalaGame,
                                                           Participant gameCurrentParticipant,
                                                           Integer pitIndexFrom,
                                                           Integer pitIndexTo,
                                                           String participantIdNextMove) {
        MancalaBoardSetup mancalaBoardSetup = new MancalaBoardSetup();
        mancalaBoardSetup.setGameId(mancalaGame.getId());
        mancalaBoardSetup.setGameState(gameStatesEnumMapper.toGameStatusEnumViewModel(mancalaGame.getGameStatus()));

        mancalaBoardSetup.setParticipantIdCurrentMove(gameCurrentParticipant.getId());
        mancalaBoardSetup.setParticipantIdNextMove(participantIdNextMove);

        mancalaBoardSetup.setPitIndexFrom(pitIndexFrom);
        mancalaBoardSetup.setPitIndexTo(pitIndexTo);

        mancalaBoardSetup.setTableCurrentState(
                tableCurrentStateMapper.toTableCurrentStateViewModelList(mancalaGame.getTableCurrentStatesList()));
        return mancalaBoardSetup;
    }
}
