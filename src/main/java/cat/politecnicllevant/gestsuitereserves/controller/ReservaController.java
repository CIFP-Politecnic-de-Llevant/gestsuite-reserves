package cat.politecnicllevant.gestsuitereserves.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitereserves.dto.ReservaDto;
import cat.politecnicllevant.gestsuitereserves.service.GoogleCalendarService;
import cat.politecnicllevant.gestsuitereserves.service.ReservaService;
import cat.politecnicllevant.gestsuitereserves.service.TokenManager;
import com.google.api.services.calendar.model.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private Gson gson;

    private final String CALENDAR_AULA_MAGNA = "c_fde278d64cd5f4b1b2d54ce1e07a948ab1fdfdf92d411a68433043bab8f17f3a@group.calendar.google.com";

    @GetMapping("/reserves")
    public ResponseEntity<List<ReservaDto>> getReserves() {

        List<ReservaDto> reserves = reservaService.findAll();

        return new ResponseEntity<>(reserves, HttpStatus.OK);
    }

    @GetMapping("/reserva/{id}")
    public ResponseEntity<ReservaDto> getReservaById(@PathVariable("id") String idReserva) {

        ReservaDto reservaDto = reservaService.getReservaById(Long.valueOf(idReserva));

        return new ResponseEntity<>(reservaDto, HttpStatus.OK);
    }

    @PostMapping("/reserva/desar")
    public ResponseEntity<Notificacio> desarReserva(@RequestBody String json, HttpServletRequest request) throws Exception {

        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        String nomUsuari = (String) claims.get("nom");

        System.out.println("myEmail: "+myEmail);
        System.out.println("nomUsuari: "+nomUsuari);


        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idReserva = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()){
            idReserva = jsonObject.get("id").getAsLong();
        }

        String descripcio = jsonObject.get("descripcio").getAsString();

        System.out.println("Date inici"+jsonObject.get("dataInici").getAsString());
        LocalDateTime dataInici = LocalDateTime.parse(jsonObject.get("dataInici").getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime dataFi = LocalDateTime.parse(jsonObject.get("dataFi").getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        //Comprovam disponibilitat
        if(googleCalendarService.isOverlap(this.CALENDAR_AULA_MAGNA,dataInici,dataFi)) {
            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("Ja hi ha una reserva en aquesta franja horària");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }

        //Reservem a l'agenda de Google Calendar
        Event event;


        ReservaDto reservaDto;

        if(idReserva != null) {
            reservaDto = reservaService.getReservaById(idReserva);
            event = googleCalendarService.getEventById(reservaDto.getIdCalendar(),reservaDto.getIdCalendarEvent());
            event = googleCalendarService.updateEvent(event, CALENDAR_AULA_MAGNA,"Aula Magna",descripcio+" - "+nomUsuari,dataInici,dataFi);
        } else {
            reservaDto = new ReservaDto();
            event = googleCalendarService.createEvent(CALENDAR_AULA_MAGNA,"Aula Magna",descripcio+" - "+nomUsuari,dataInici,dataFi);
        }

        reservaDto.setDescripcio(descripcio);
        reservaDto.setDataInici(dataInici);
        reservaDto.setDataFi(dataFi);
        reservaDto.setUsuariEmail(myEmail);
        reservaDto.setUsuariNom(nomUsuari);

        reservaDto.setIdCalendar(event.getICalUID());
        reservaDto.setIdCalendarEvent(event.getId());

        reservaService.save(reservaDto);


        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Reserva desada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }


}