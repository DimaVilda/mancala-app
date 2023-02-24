package com.vilda.mancala.mancalaapp.mappers;

import com.vilda.mancala.mancalaapp.domain.enums.GameStatesEnum;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameStatesEnumMapper {

    com.vilda.mancala.mancalaapp.client.spec.model.GameStatesEnum toGameStatusEnumViewModel(GameStatesEnum gameStatesEnum);
}
