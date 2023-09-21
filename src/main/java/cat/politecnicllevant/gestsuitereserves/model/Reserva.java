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

    //Microservei CORE
    @Column(name = "usuari_idusuari", nullable = false)
    private Long usuari;
}
