package BookRental;

public class DeliveryFinished extends AbstractEvent {

    private Long id;

    public DeliveryFinished(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
