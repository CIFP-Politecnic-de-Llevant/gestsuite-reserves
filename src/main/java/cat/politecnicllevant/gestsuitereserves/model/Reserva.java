package cat.politecnicllevant.gestsuitereserves.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "reserva")
public @Data class Reserva {
    @Id
    @Column(name = "idreserva")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    @Column(name = "descripcio", nullable = false, length = 2048)
    private String descripcio;

    @Column(name = "data_inici", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataInici;

    @Column(name = "data_fi", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataFi;

    @Column(name = "usuari_email", nullable = false)
    private Long usuariEmail;

    @Column(name = "usuari_nom", nullable = false)
    private Long usuariNom;

    @Column(name = "idcalendar", nullable = false)
    private Long idCalendar;

    @Column(name = "idcalendar_event", nullable = false)
    private Long idCalendarEvent;
}
