package cat.politecnicllevant.gestsuitereserves.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GoogleCalendarService {

    @Value("${gc.keyfile}")
    private String keyFile;

    @Value("${gc.adminUser}")
    private String adminUser;

    @Value("${gc.nomprojecte}")
    private String nomProjecte;

    public Event createEvent(String idCalendar, String espai, String descripcio, LocalDateTime ini, LocalDateTime fi) throws IOException, GeneralSecurityException {
        String[] scopes = {CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_READONLY};
        GoogleCredentials credentials = null;

        credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        String dateIniStr = ini.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String dateFiStr = fi.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        Event event = new Event();
        event.setSummary(descripcio);
        event.setLocation(espai);
        event.setDescription(descripcio);
        event.setStart(new EventDateTime().setDateTime(new DateTime(dateIniStr)).setTimeZone("Europe/Madrid"));
        event.setEnd(new EventDateTime().setDateTime(new DateTime(dateFiStr)).setTimeZone("Europe/Madrid"));

        return service.events().insert(idCalendar, event).execute();
    }

    public boolean isOverlap(String idCalendar, LocalDateTime ini, LocalDateTime fi) throws IOException, GeneralSecurityException {
        String[] scopes = {CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_READONLY};
        GoogleCredentials credentials = null;

        credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        String dateIniStr = ini.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String dateFiStr = fi.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        return !service.events().list(idCalendar).setTimeMin(new DateTime(dateIniStr)).setTimeMax(new DateTime(dateFiStr)).execute().getItems().isEmpty();
    }
}
