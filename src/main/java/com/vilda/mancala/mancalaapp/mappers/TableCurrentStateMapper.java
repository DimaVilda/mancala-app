package com.vilda.mancala.mancalaapp.mappers;

import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TableCurrentStateMapper {


    List<com.vilda.mancala.mancalaapp.client.spec.model.TableCurrentState> toTableCurrentStateViewModelList(
            List<TableCurrentState> tableCurrentStateList);

    @Mapping(target = "piId", source = "pit.id")
    @Mapping(target = "pitIndex", source = "pit.pitIndex")
    @Mapping(target = "isBigPit", source = "pit.isBigPit")
    com.vilda.mancala.mancalaapp.client.spec.model.TableCurrentState toTableCurrentStateViewModel(
            TableCurrentState tableCurrentState);

}