package cat.politecnicllevant.gestsuitereserves.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitereserves.dto.CalendariDto;
import cat.politecnicllevant.gestsuitereserves.dto.ReservaDto;
import cat.politecnicllevant.gestsuitereserves.dto.google.CalendariRolDto;
import cat.politecnicllevant.gestsuitereserves.dto.google.CalendariTipusUsuariDto;
import cat.politecnicllevant.gestsuitereserves.service.GoogleCalendarService;
import cat.politecnicllevant.gestsuitereserves.service.TokenManager;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
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
import java.util.Arrays;
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

    @Value("${google.calendars}")
    private String[] CALENDARS;

    @Value("${gc.adminUser}")
    private String adminUser;


    @GetMapping("/calendaris")
    public ResponseEntity<List<CalendariDto>> findAllCalendaris(HttpServletRequest request) throws GeneralSecurityException, IOException {
        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        String nomUsuari = (String) claims.get("nom");

        //List<CalendarListEntry> calendarList = googleCalendarService.getCalendars();

        List<CalendariDto> calendaris = new ArrayList<>();

        for(String idCalendar: CALENDARS){
            if(!googleCalendarService.hasCalendar(idCalendar,this.adminUser)){
                System.out.println("El calendari "+idCalendar+" no està compartit per "+this.adminUser);
            } else {
                CalendarListEntry calenarListEntry = googleCalendarService.getCalendar(idCalendar);
                CalendariDto calendariDto = mapCalendar(calenarListEntry);
                calendaris.add(calendariDto);
            }
        }

        return new ResponseEntity<>(calendaris, HttpStatus.OK);
    }

    @GetMapping("/calendari/{idCalendari}")
    public ResponseEntity<CalendariDto> getCalendariById(@PathVariable("idCalendar") String idCalendar,HttpServletRequest request) throws GeneralSecurityException, IOException {
        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        String nomUsuari = (String) claims.get("nom");

        if(!googleCalendarService.hasCalendar(idCalendar,this.adminUser)){
            System.out.println("El calendari "+idCalendar+" no està compartit per "+this.adminUser);
        } else {
            CalendarListEntry calenarListEntry = googleCalendarService.getCalendar(idCalendar);
            CalendariDto calendariDto = mapCalendar(calenarListEntry);
            return new ResponseEntity<>(calendariDto, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{idCalendar}/myreserves")
    public ResponseEntity<List<ReservaDto>> getMyReserves(@PathVariable("idCalendar") String idCalendar,HttpServletRequest request) throws GeneralSecurityException, IOException {
        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        String nomUsuari = (String) claims.get("nom");

        //Comprovació de seguretat. El calendari és un dels permesos per GestSuite
        if(!Arrays.asList(this.CALENDARS).contains(idCalendar)){
            return null;
        }

        List<Event> myEvents = googleCalendarService.findAllEvents(idCalendar,myEmail);

        List<ReservaDto> reserves = new ArrayList<>();

        for(Event event: myEvents){
            ReservaDto reservaDto = mapEvent(event);
            reserves.add(reservaDto);
        }

        return new ResponseEntity<>(reserves, HttpStatus.OK);
    }

    @GetMapping("/{idCalendar}/reserva/{id}")
    public ResponseEntity<ReservaDto> getReservaById(@PathVariable("idCalendar") String idCalendar,@PathVariable("id") String idReserva) throws GeneralSecurityException, IOException {

        //Comprovació de seguretat. El calendari és un dels permesos per GestSuite
        if(!Arrays.asList(this.CALENDARS).contains(idCalendar)){
            return null;
        }

        Event event = googleCalendarService.getEventById(idCalendar,idReserva);

        ReservaDto reservaDto = mapEvent(event);

        return new ResponseEntity<>(reservaDto, HttpStatus.OK);
    }

    @PostMapping("/{idCalendar}/reserva/desar")
    public ResponseEntity<Notificacio> desarReserva(@PathVariable("idCalendar") String idCalendar,@RequestBody String json, HttpServletRequest request) throws Exception {

        //Comprovació de seguretat. El calendari és un dels permesos per GestSuite
        if(!Arrays.asList(this.CALENDARS).contains(idCalendar)){
            return null;
        }

        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        String nomUsuari = (String) claims.get("nom");

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
            event = googleCalendarService.getEventById(idCalendar,idReserva);
        }

        //Comprovam disponibilitat
        if(googleCalendarService.isOverlap(idCalendar,dataInici,dataFi,event)){
            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("Ja hi ha una reserva en aquesta franja horària");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }


        //Creem la reserva a Google Calendar
        if(idReserva != null) {
            googleCalendarService.updateEvent(event, idCalendar,"Aula Magna",descripcio+" - "+nomUsuari,nomUsuari, myEmail, dataInici,dataFi);
        } else {
            googleCalendarService.createEvent(idCalendar,"Aula Magna",descripcio+" - "+nomUsuari,nomUsuari, myEmail, dataInici,dataFi);
        }


        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Reserva desada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @DeleteMapping("/{idCalendar}/reserva/eliminar/{id}")
    public ResponseEntity<Notificacio> eliminarReserva(@PathVariable("idCalendar") String idCalendar,@PathVariable("id") String idReserva, HttpServletRequest request) throws GeneralSecurityException, IOException {

        //Comprovació de seguretat. El calendari és un dels permesos per GestSuite
        if(!Arrays.asList(this.CALENDARS).contains(idCalendar)){
            return null;
        }

        Claims claims = tokenManager.getClaims(request);
        String myEmail = (String) claims.get("email");
        String nomUsuari = (String) claims.get("nom");

        //Comprovem que té permisos per esborrar, és a dir, si l'esdeveniment és de l'usuari
        Event event = googleCalendarService.getEventById(idCalendar,idReserva);
        List<EventAttendee> colaboradors = event.getAttendees();
        boolean trobat = colaboradors.stream().anyMatch(colaborador -> colaborador.getEmail().equals(myEmail));

        if(!trobat){
            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("No tens permisos per eliminar aquesta reserva");
            notificacio.setNotifyType(NotificacioTipus.ERROR);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }

        //Eliminam de l'agenda de Google Calendar
        googleCalendarService.deleteEventById(idCalendar,idReserva);

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

    private CalendariDto mapCalendar(CalendarListEntry calendarListEntry){
        CalendariDto calendariDto = new CalendariDto();
        calendariDto.setIdCalendari(calendarListEntry.getId());
        calendariDto.setDescripcio(calendarListEntry.getSummary());
        return calendariDto;
    }
}