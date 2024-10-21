package ru.t1.java.demo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.ClientDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ClientMapperTest {

    @Spy
    ClientMapper mapper;

    @Test
    void toEntity() {

        ClientDto dto = ClientDto.builder()
                .firstName("John")
                .lastName("Doe")
                .middleName("Smith")
                .build();

        Client client = new Client();
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setMiddleName("Smith");

        doReturn(client)
                .when(mapper)
                .toEntityWithId(dto);

        assertThat(mapper.toEntityWithId(dto)).isEqualTo(client);


    }

    @Test
    void toEntityWhen() {

        ClientDto dto = new ClientDto();
        dto.setMiddleName("MiddleName");
        Client client = new Client();
        client.setMiddleName("MiddleName");
        ClientDto dto2 = new ClientDto();
        dto2.setMiddleName("MiddleName");
//       when(mapper.toEntityWithId(dto))
//               .thenReturn(client);

//        assertThat(mapper.toEntityWithId(dto).getMiddleName()).isEqualTo(client.getMiddleName());
        assertThat(mapper.toDto(client)).isEqualTo(dto);

    }
}