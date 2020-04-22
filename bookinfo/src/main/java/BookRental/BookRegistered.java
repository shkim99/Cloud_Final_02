package BookRental;

public class BookRegistered extends AbstractEvent {

    private Long id;

    public BookRegistered(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
