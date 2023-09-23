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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class GoogleCalendarService {

    @Value("${gc.keyfile}")
    private String keyFile;

    @Value("${gc.adminUser}")
    private String adminUser;

    @Value("${gc.nomprojecte}")
    private String nomProjecte;

    public void createEvent(String idCalendar) throws IOException, GeneralSecurityException {
        String[] scopes = {CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_READONLY};
        GoogleCredentials credentials = null;

        credentials = GoogleCredentials.fromStream(new FileInputStream(this.keyFile)).createScoped(scopes).createDelegated(this.adminUser);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName(this.nomProjecte).build();

        Event event = new Event();
        event.setSummary("Google I/O 2015");
        event.setLocation("800 Howard St., San Francisco, CA 94103");
        event.setDescription("A chance to hear more about Google's developer products.");
        event.setStart(new EventDateTime().setDateTime(new DateTime("2023-09-28T09:00:00-07:00")).setTimeZone("America/Los_Angeles"));
        event.setEnd(new EventDateTime().setDateTime(new DateTime("2023-09-28T17:00:00-07:00")).setTimeZone("America/Los_Angeles"));

        service.events().insert(idCalendar, event).execute();
    }
}
