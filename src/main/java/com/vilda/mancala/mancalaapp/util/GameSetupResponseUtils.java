package com.vilda.mancala.mancalaapp.util;

import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.domain.MancalaGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GameSetupResponseUtils {

    public GameSetupResponse getNewGameSetupResponseBody(MancalaGame mancalaGame) {
        GameSetupResponse gameSetupResponse = new GameSetupResponse();
        gameSetupResponse.setGameId(mancalaGame.getId());
        mancalaGame.getParticipantSet().forEach(item -> {
            if (item.getPlayerNumber() == 1) {
                gameSetupResponse.setParticipantOneId(item.getId());
            } else {
                gameSetupResponse.setParticipantTwoId(item.getId());
            }
        });
        return gameSetupResponse;
    }
}
