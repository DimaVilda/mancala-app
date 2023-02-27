package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.TableCurrentStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TableCurrentStatePersistenceService {

    private final TableCurrentStateRepository tableCurrentStateRepository;

    public void saveTableCurrentStateStonesCount(TableCurrentState tableCurrentState, int pitStonesCount) {
        tableCurrentState.setStonesCountInPit(pitStonesCount);
        tableCurrentStateRepository.save(tableCurrentState);
    }

    public TableCurrentState findTableCurrentStateByMancalaGameIdAndPitIndex(String gameId, Integer pitIndex) {
        return tableCurrentStateRepository.findTableCurrentStateByMancalaGameIdAndPitPitIndex(gameId, pitIndex).orElseThrow(() -> {
            log.error("No pit table state was fround by game {} and pit index {}", gameId, pitIndex);

            return new NotFoundException("No table current state by provided gameId " + gameId + " and pitIndex " + pitIndex);
        });
    }
}
