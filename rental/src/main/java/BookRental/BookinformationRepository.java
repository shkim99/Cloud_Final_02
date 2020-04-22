package BookRental;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookinformationRepository extends CrudRepository<Bookinformation, Long> {


}