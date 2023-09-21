package cat.politecnicllevant.gestsuitereserves.service;

import cat.politecnicllevant.gestsuitereserves.model.Reserva;
import cat.politecnicllevant.gestsuitereserves.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservaService {
    @Autowired
    private ReservaRepository reservaRepository;

    @Transactional
    public Reserva save(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public Reserva getReservaById(Long id){
        //Ha de ser findById i no getById perquè getById és Lazy
        return reservaRepository.findById(id).get();
        //return itemRepository.getById(id);
    }

    public List<Reserva> findAll(){
        return reservaRepository.findAll();
    }

    @Transactional
    public void esborrar(Reserva reserva){
        reservaRepository.delete(reserva);
    }
}

