package cat.politecnicllevant.gestsuitereserves.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleCalendarService {

    @Value("${gc.keyfile}")
    private String keyFile;

    @Value("${gc.adminUser}")
    private String adminUser;

    @Value("${gc.nomprojecte}")
    private String nomProjecte;

    private final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    public List<Event> findAll(String idCalendar,String email) throws GeneralSecurityException, IOException {
        String[] scopes = {CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_READONLY};
        GoogleCredentials credentials = null;

        credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        List<Event> allEvents = service.events().list(idCalendar).execute().getItems();

        //Filtrem els esdeveniments que estam com a col·laboradors
        return allEvents.stream().filter(e->{
            List<EventAttendee> assistents = e.getAttendees();
            if(assistents==null){
                return false;
            }
            System.out.println(e.getId());
            System.out.println(assistents.get(0).getEmail());
            return assistents.stream().anyMatch(a->a.getEmail().equals(email));
        }).toList();
    }

    public Event createEvent(String idCalendar, String espai, String descripcio, String usuari, String email, LocalDateTime ini, LocalDateTime fi) throws IOException, GeneralSecurityException {
        String[] scopes = {CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_READONLY};
        GoogleCredentials credentials = null;

        credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        //Ajustem l'hora a la zona horaria de Madrid
        ZoneId madridZone = ZoneId.of("Europe/Madrid");
        int secondMadrid = madridZone.getRules().getOffset(LocalDateTime.now()).getTotalSeconds();
        String dateIniStr = ini.minusSeconds(secondMadrid).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        String dateFiStr = fi.minusSeconds(secondMadrid).format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        Event event = new Event();
        event.setSummary(descripcio);
        event.setLocation(espai);
        event.setDescription("IMPORTANT! Tots els col·laboradors tenen permisos per editar l'esdeveniment.");
        event.setStart(new EventDateTime().setDateTime(new DateTime(dateIniStr)).setTimeZone("Europe/Madrid"));
        event.setEnd(new EventDateTime().setDateTime(new DateTime(dateFiStr)).setTimeZone("Europe/Madrid"));

        EventAttendee attendee = new EventAttendee();
        attendee.setEmail(email);
        attendee.setDisplayName(usuari);
        attendee.setResponseStatus("accepted");

        List<EventAttendee> attendees = new ArrayList<>();
        attendees.add(attendee);

        event.setAttendees(attendees);

        return service.events().insert(idCalendar, event).execute();
    }

    public Event updateEvent(Event event,  String idCalendar, String espai, String descripcio, String usuari, String email, LocalDateTime ini, LocalDateTime fi) throws IOException, GeneralSecurityException {
        String[] scopes = {CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_READONLY};
        GoogleCredentials credentials = null;

        credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        //Ajustem l'hora a la zona horaria de Madrid
        ZoneId madridZone = ZoneId.of("Europe/Madrid");
        int secondMadrid = madridZone.getRules().getOffset(LocalDateTime.now()).getTotalSeconds();
        String dateIniStr = ini.minusSeconds(secondMadrid).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        String dateFiStr = fi.minusSeconds(secondMadrid).format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        event.setSummary(descripcio);
        event.setLocation(espai);
        event.setDescription("IMPORTANT! Tots els col·laboradors tenen permisos per editar l'esdeveniment.");
        event.setStart(new EventDateTime().setDateTime(new DateTime(dateIniStr)).setTimeZone("Europe/Madrid"));
        event.setEnd(new EventDateTime().setDateTime(new DateTime(dateFiStr)).setTimeZone("Europe/Madrid"));

        EventAttendee attendee = new EventAttendee();
        attendee.setEmail(email);
        attendee.setDisplayName(usuari);
        attendee.setOrganizer(true);
        attendee.setResponseStatus("accepted");

        List<EventAttendee> attendees = new ArrayList<>();
        attendees.add(attendee);

        event.setAttendees(attendees);

        return service.events().update(idCalendar, event.getId(), event).execute();
    }

    public Event getEventById(String idCalendar, String idEvent) throws IOException, GeneralSecurityException {
        String[] scopes = {CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_READONLY};
        GoogleCredentials credentials = null;

        credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        return service.events().get(idCalendar, idEvent).execute();
    }

    public boolean isOverlap(String idCalendar, LocalDateTime ini, LocalDateTime fi,Event event) throws IOException, GeneralSecurityException {
        String[] scopes = {CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_READONLY};
        GoogleCredentials credentials = null;

        credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        //Ajustem l'hora a la zona horaria de Madrid
        ZoneId madridZone = ZoneId.of("Europe/Madrid");
        int secondMadrid = madridZone.getRules().getOffset(LocalDateTime.now()).getTotalSeconds();
        String dateIniStr = ini.minusSeconds(secondMadrid).format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        String dateFiStr = fi.minusSeconds(secondMadrid).format(DateTimeFormatter.ofPattern(DATE_PATTERN));


        List<Event> events = service.events().list(idCalendar).setTimeMin(new DateTime(dateIniStr)).setTimeMax(new DateTime(dateFiStr)).execute().getItems();

        //Si hi ha un event ja creat, el solapament NO ha de tenir en compte aquest event
        if(event!=null){
            //Filtrem l'event que ja tenim
            events = events.stream().filter(e -> !e.getId().equals(event.getId())).toList();
        }

        return !events.isEmpty();
    }
}
