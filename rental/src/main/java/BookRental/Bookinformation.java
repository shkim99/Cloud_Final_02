package BookRental;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="Bookinformation_table")
public class Bookinformation {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

}
