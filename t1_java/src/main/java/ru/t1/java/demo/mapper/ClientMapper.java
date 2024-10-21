package ru.t1.java.demo.mapper;

import org.mapstruct.*;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.ClientDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {
    Client toEntity(ClientDto clientDto);

    ClientDto toDto(Client client);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Client partialUpdate(ClientDto clientDto, @MappingTarget Client client);
}