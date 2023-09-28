package cat.politecnicllevant.gestsuitereserves.dto;

import lombok.Data;

import java.time.LocalDateTime;

public @Data class ReservaDto {
    private String idReserva;
    private String descripcio;
    private LocalDateTime dataInici;
    private LocalDateTime dataFi;
}
