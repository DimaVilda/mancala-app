package com.vilda.mancala.mancalaapp.mappers;

import com.vilda.mancala.mancalaapp.client.spec.model.TableCurrentState;
import com.vilda.mancala.mancalaapp.domain.Pit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class TableCurrentStateMapperTest {

    private final TableCurrentStateMapper mapper = Mappers.getMapper(TableCurrentStateMapper.class);

    @Test
    void shouldMapTableCurrentStateListToTableCurrentStateViewModelList() {
        Pit pit = new Pit();
        pit.setId("pitId");
        pit.setPitIndex(1);
        pit.setIsBigPit(0);

        com.vilda.mancala.mancalaapp.domain.TableCurrentState target = new com.vilda.mancala.mancalaapp.domain.TableCurrentState();
        target.setStonesCountInPit(6);
        target.setPit(pit);

        List<TableCurrentState> tableCurrentStates = mapper.toTableCurrentStateViewModelList(Collections.singletonList(target));
        TableCurrentState source = tableCurrentStates.get(0);

        assertThat(source.getPitId(), is(target.getPit().getId()));
        assertThat(source.getStonesCountInPit(), is(target.getStonesCountInPit()));
        assertThat(source.getPitIndex(), is(target.getPit().getPitIndex()));
        assertThat(source.getIsBigPit(), is(target.getPit().getIsBigPit()));
    }
}
