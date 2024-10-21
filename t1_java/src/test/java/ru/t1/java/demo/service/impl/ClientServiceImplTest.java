package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.model.dto.ClientDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

//    @Spy
//    ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    @Spy
    ClientServiceImpl clientService;

    @Mock
    ClientServiceImpl clientServiceMock;

    @Test
    void parseJsonSpy() {
        when(clientService.parseJson()).thenReturn(List.of(ClientDto.builder()
                .build()));

        assertEquals(List.of(ClientDto.builder().build()), clientService.parseJson());
    }

    @Test
    void parseJsonMock() {

        when(clientServiceMock.parseJson()).thenReturn(List.of(ClientDto.builder().build()));

        assertEquals(List.of(ClientDto.builder().build()), clientServiceMock.parseJson());
    }

}