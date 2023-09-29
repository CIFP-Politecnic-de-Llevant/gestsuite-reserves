package cat.politecnicllevant.gestsuitereserves.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitereserves.dto.ReservaDto;
import cat.politecnicllevant.gestsuitereserves.service.GoogleCalendarService;
import cat.politecnicllevant.gestsuitereserves.service.TokenManager;
import com.google.api.services.calendar.model.EventAttendee;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.services.calendar.model.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ReservaController {

    //@Autowired
    //private ReservaService reservaService;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private Gson gson;

    @Value("${google.calendar.aulamagna}")
    private String CALENDAR_AULA_MAGNA;


    @GetMapping("/myreserves")
    public ResponseEntity<List<ReservaDto>> getMyReserves(HttpServletRequest request) throws GeneralSecurityException, IOException {
        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        String nomUsuari = (String) claims.get("nom");

        List<Event> myEvents = googleCalendarService.findAll(this.CALENDAR_AULA_MAGNA,myEmail);

        List<ReservaDto> reserves = new ArrayList<>();

        for(Event event: myEvents){
            ReservaDto reservaDto = mapEvent(event);
            reserves.add(reservaDto);
        }

        return new ResponseEntity<>(reserves, HttpStatus.OK);
    }

    @GetMapping("/reserva/{id}")
    public ResponseEntity<ReservaDto> getReservaById(@PathVariable("id") String idReserva) throws GeneralSecurityException, IOException {

        Event event = googleCalendarService.getEventById(this.CALENDAR_AULA_MAGNA,idReserva);

        ReservaDto reservaDto = mapEvent(event);

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

        String idReserva = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()){
            idReserva = jsonObject.get("id").getAsString();
        }

        String descripcio = jsonObject.get("descripcio").getAsString();

        System.out.println("Date inici"+jsonObject.get("dataInici").getAsString());
        LocalDateTime dataInici = LocalDateTime.parse(jsonObject.get("dataInici").getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime dataFi = LocalDateTime.parse(jsonObject.get("dataFi").getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));


        //Reservem a l'agenda de Google Calendar
        Event event = null;

        if(idReserva != null) {
            event = googleCalendarService.getEventById(this.CALENDAR_AULA_MAGNA,idReserva);
        }

        //Comprovam disponibilitat
        if(googleCalendarService.isOverlap(this.CALENDAR_AULA_MAGNA,dataInici,dataFi,event)){
            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("Ja hi ha una reserva en aquesta franja horària");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }


        //Creem la reserva a Google Calendar
        if(idReserva != null) {
            googleCalendarService.updateEvent(event, CALENDAR_AULA_MAGNA,"Aula Magna",descripcio+" - "+nomUsuari,nomUsuari, myEmail, dataInici,dataFi);
        } else {
            googleCalendarService.createEvent(CALENDAR_AULA_MAGNA,"Aula Magna",descripcio+" - "+nomUsuari,nomUsuari, myEmail, dataInici,dataFi);
        }


        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Reserva desada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @DeleteMapping("/reserva/eliminar/{id}")
    public ResponseEntity<Notificacio> eliminarReserva(@PathVariable("id") String idReserva, HttpServletRequest request) throws GeneralSecurityException, IOException {

        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        String nomUsuari = (String) claims.get("nom");

        System.out.println("myEmail: "+myEmail);
        System.out.println("nomUsuari: "+nomUsuari);

        //Comprovem que té permisos per esborrar, és a dir, si l'esdeveniment és de l'usuari
        Event event = googleCalendarService.getEventById(this.CALENDAR_AULA_MAGNA,idReserva);
        List<EventAttendee> colaboradors = event.getAttendees();
        boolean trobat = colaboradors.stream().anyMatch(colaborador -> colaborador.getEmail().equals(myEmail));

        if(!trobat){
            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("No tens permisos per eliminar aquesta reserva");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }

        //Eliminam de l'agenda de Google Calendar
        googleCalendarService.deleteEventById(this.CALENDAR_AULA_MAGNA,idReserva);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Reserva eliminada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    private ReservaDto mapEvent(Event event) {
        ReservaDto reservaDto = new ReservaDto();
        reservaDto.setIdReserva(event.getId());

        String descripcio = event.getSummary();

        if(descripcio.lastIndexOf("-")>-1){
            descripcio = descripcio.substring(0, descripcio.lastIndexOf("-")).trim();
        }

        reservaDto.setDescripcio(descripcio);

        ZoneId madridZone = ZoneId.of("Europe/Madrid");
        Instant instantStart = Instant.parse( event.getStart().getDateTime().toStringRfc3339()) ;
        LocalDateTime dataIni = LocalDateTime.ofInstant(instantStart, madridZone);

        Instant instantEnd = Instant.parse( event.getEnd().getDateTime().toStringRfc3339()) ;
        LocalDateTime dataFi = LocalDateTime.ofInstant(instantEnd, madridZone);

        reservaDto.setDataInici(dataIni);
        reservaDto.setDataFi(dataFi);

        return reservaDto;
    }
}