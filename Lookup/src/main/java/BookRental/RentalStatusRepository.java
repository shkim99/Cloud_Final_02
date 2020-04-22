package BookRental;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RentalStatusRepository extends CrudRepository<RentalStatus, Long> {


}