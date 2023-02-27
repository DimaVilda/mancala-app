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
                                                           String gameCurrentParticipantId,
                                                           Integer pitIndexFrom,
                                                           Integer pitIndexTo,
                                                           String participantIdNextMove) {
        log.debug("Trying to setup current game {} table state response body info", mancalaGame.getId());

        MancalaBoardSetup mancalaBoardSetup = new MancalaBoardSetup();
        mancalaBoardSetup.setGameId(mancalaGame.getId());
        mancalaBoardSetup.setGameState(gameStatesEnumMapper.toGameStatusEnumViewModel(mancalaGame.getGameStatus()));

        mancalaBoardSetup.setParticipantIdCurrentMove(gameCurrentParticipantId);
        mancalaBoardSetup.setParticipantIdNextMove(participantIdNextMove);

        mancalaBoardSetup.setPitIndexFrom(pitIndexFrom);
        mancalaBoardSetup.setPitIndexTo(pitIndexTo);

        mancalaBoardSetup.setTableCurrentState(
                tableCurrentStateMapper.toTableCurrentStateViewModelList(mancalaGame.getTableCurrentStatesList()));
        return mancalaBoardSetup;
    }
}
