package ru.t1.java.demo.model.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.t1.java.demo.model.enums.OperationEnum;
import ru.t1.java.demo.model.enums.TypeEnum;

import javax.annotation.Nullable;
import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationInfoAbstractDto {

    @JsonProperty("client_id")
    private Long clientId;

    @JsonProperty("operation")
    private OperationEnum operation;

    @JsonProperty("card_type")
    private TypeEnum cardType;

    @Nullable
    @JsonProperty("amount")
    private BigDecimal amount;

}
