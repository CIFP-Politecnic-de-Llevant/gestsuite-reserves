package cat.politecnicllevant.gestsuitereserves.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

public @Data class ReservaDto {
    private Long idReserva;
    private String descripcio;
    private LocalDateTime dataInici;
    private LocalDateTime dataFi;
    private String usuariEmail;
    private String usuariNom;
    private String idCalendar;
    private String idCalendarEvent;
}
