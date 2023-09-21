package cat.politecnicllevant.gestsuitereserves.service;

import cat.politecnicllevant.gestsuitereserves.dto.ReservaDto;
import cat.politecnicllevant.gestsuitereserves.model.Reserva;
import cat.politecnicllevant.gestsuitereserves.repository.ReservaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {
    @Autowired
    private ReservaRepository reservaRepository;

    @Transactional
    public ReservaDto save(ReservaDto reservaDto) {
        ModelMapper modelMapper = new ModelMapper();
        Reserva reserva = modelMapper.map(reservaDto, Reserva.class);
        Reserva reservaSaved = reservaRepository.save(reserva);
        return modelMapper.map(reservaSaved, ReservaDto.class);
    }

    public ReservaDto getReservaById(Long id){
        //Ha de ser findById i no getById perquè getById és Lazy
        Reserva reserva = reservaRepository.findById(id).get();

        ModelMapper modelMapper = new ModelMapper();
        ReservaDto reservaDto = modelMapper.map(reserva, ReservaDto.class);
        return reservaDto;
    }

    public List<ReservaDto> findAll(){
        ModelMapper modelMapper = new ModelMapper();
        return reservaRepository.findAll().stream().map(c->modelMapper.map(c,ReservaDto.class)).collect(Collectors.toList());

    }

    @Transactional
    public void esborrar(ReservaDto reservaDto){
        ModelMapper modelMapper = new ModelMapper();
        Reserva reserva = modelMapper.map(reservaDto, Reserva.class);
        reservaRepository.delete(reserva);
    }
}

