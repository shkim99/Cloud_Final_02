package BookRental;

public class BookDeleted extends AbstractEvent {

    private Long id;

    public BookDeleted(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
