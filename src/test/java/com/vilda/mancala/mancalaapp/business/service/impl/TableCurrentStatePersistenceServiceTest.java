package com.vilda.mancala.mancalaapp.business.service.impl;

import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import com.vilda.mancala.mancalaapp.exceptions.NotFoundException;
import com.vilda.mancala.mancalaapp.repository.TableCurrentStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TableCurrentStatePersistenceServiceTest {

    @Mock
    private TableCurrentStateRepository tableCurrentStateRepository;
    @Captor
    private ArgumentCaptor<TableCurrentState> tableCurrentStateArgumentCaptor;
    @InjectMocks
    private TableCurrentStatePersistenceService tableCurrentStatePersistenceService;

    @Test
    void shouldSaveTableCurrentStateStonesCount() {
        int stonesCountInPit = 10;
        tableCurrentStatePersistenceService.saveTableCurrentStateStonesCount(new TableCurrentState(), stonesCountInPit);

        verify(tableCurrentStateRepository).save(tableCurrentStateArgumentCaptor.capture());
        assertThat(tableCurrentStateArgumentCaptor.getValue().getStonesCountInPit(), is(stonesCountInPit));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTableCurrentStateNotFound() {
        String testGameId = "testGameId";
        int pitIndex = 2;

        when(tableCurrentStateRepository.findTableCurrentStateByMancalaGameIdAndPitPitIndex(testGameId, pitIndex)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tableCurrentStatePersistenceService.findTableCurrentStateByMancalaGameIdAndPitIndex("testGameId", pitIndex));
        assertThat(exception.getMessage(), is("No table current state by provided gameId " + testGameId + " and pitIndex " + pitIndex));
    }
}
