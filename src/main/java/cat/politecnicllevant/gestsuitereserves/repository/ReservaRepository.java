package cat.politecnicllevant.gestsuitereserves.repository;

import cat.politecnicllevant.gestsuitereserves.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findAllByUsuariEmail(String email);
}
