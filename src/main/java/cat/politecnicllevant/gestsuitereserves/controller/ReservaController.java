package cat.politecnicllevant.gestsuitereserves.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.gestsuitereserves.dto.ReservaDto;
import cat.politecnicllevant.gestsuitereserves.model.Reserva;
import cat.politecnicllevant.gestsuitereserves.service.ReservaService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private Gson gson;

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

    @PostMapping("/categoria/desar")
    public ResponseEntity<Notificacio> desarCategoriaConvalidacio(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idCategoria = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()){
            idCategoria = jsonObject.get("id").getAsLong();
        }

        String descripcio = jsonObject.get("descripcio").getAsString();

        ReservaDto reservaDto;

        if(idCategoria != null) {
            reservaDto = reservaService.getReservaById(idCategoria);
        } else {
            reservaDto = new ReservaDto();
        }

        reservaDto.setDescripcio(descripcio);

        reservaService.save(reservaDto);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Reserva desada correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    /*@PostMapping("/categoria/esborrar")
    public ResponseEntity<Notificacio> esborrarCategoriaConvalidacio(@RequestBody String json, HttpServletRequest request) throws GeneralSecurityException, IOException {

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long idCategoria = null;
        if (jsonObject.get("id") != null && !jsonObject.get("id").isJsonNull()){
            idCategoria = jsonObject.get("id").getAsLong();
        }


        if(idCategoria != null) {
            Categoria categoria = categoriaService.getCategoriaConvalidacioById(idCategoria);
            categoriaService.esborrar(categoria);

            Notificacio notificacio = new Notificacio();
            notificacio.setNotifyMessage("Categoria esborrada correctament");
            notificacio.setNotifyType(NotificacioTipus.SUCCESS);
            return new ResponseEntity<>(notificacio, HttpStatus.OK);
        }

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("No s'ha pogut esborrar la categoria");
        notificacio.setNotifyType(NotificacioTipus.ERROR);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);

    }
*/


}