package com.vilda.mancala.mancalaapp.clientapi.controller;

import com.vilda.mancala.mancalaapp.business.service.MancalaGameService;
import com.vilda.mancala.mancalaapp.client.spec.api.MancalaClientApi;
import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MancalaGameController implements MancalaClientApi {

    private final MancalaGameService mancalaGameService;

    @Override
    public ResponseEntity<MancalaBoardSetup> makeMoveByPitId(@PathVariable String gameId,
                                                             @PathVariable String participantId,
                                                             @PathVariable Integer pitIndex) {
        return new ResponseEntity<>(mancalaGameService.makeMove(gameId, participantId, pitIndex), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GameSetupResponse> startGame(NewGameSetup newGameSetup) {
        log.debug("");

        return ResponseEntity.ok(mancalaGameService.startNewGame(newGameSetup));
    }
}
