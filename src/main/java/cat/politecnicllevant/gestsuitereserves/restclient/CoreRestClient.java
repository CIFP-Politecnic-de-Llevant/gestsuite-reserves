package cat.politecnicllevant.gestsuitereserves.restclient;

import cat.politecnicllevant.gestsuitereserves.dto.gestib.UsuariDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(name = "core")
public interface CoreRestClient {

    //USUARIS
    @GetMapping("/usuaris/profile/{id}")
    ResponseEntity<UsuariDto> getProfile(@PathVariable("id") String idUsuari) throws Exception;

    @GetMapping("/public/usuaris/profile/{id}")
    ResponseEntity<UsuariDto> getPublicProfile(@PathVariable("id") String idUsuari) throws Exception;

    @GetMapping("/usuaris/llistat/actius")
    ResponseEntity<List<UsuariDto>> getUsuarisActius();
}
