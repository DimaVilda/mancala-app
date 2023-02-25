package com.vilda.mancala.mancalaapp.clientapi.controller;

import com.vilda.mancala.mancalaapp.business.service.MancalaGameService;
import com.vilda.mancala.mancalaapp.client.spec.api.MancalaClientApi;
import com.vilda.mancala.mancalaapp.client.spec.model.GameSetupResponse;
import com.vilda.mancala.mancalaapp.client.spec.model.MancalaBoardSetup;
import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.util.validation.sequences.UserGroupSequence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MancalaGameController implements MancalaClientApi {

    private final MancalaGameService mancalaGameService;

    @Override
    public ResponseEntity<MancalaBoardSetup> makeMoveByPitId(String gameId,
                                                             String participantId,
                                                             Integer pitIndex) {
        return new ResponseEntity<>(mancalaGameService.makeMove(gameId, participantId, pitIndex), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GameSetupResponse> startGame(@RequestBody @Validated(UserGroupSequence.class)
                                                       NewGameSetup newGameSetup) {
        log.debug("");

        return ResponseEntity.ok(mancalaGameService.startNewGame(newGameSetup));
    }
}
