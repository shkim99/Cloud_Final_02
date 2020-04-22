
package BookRental;

public class DeliveryStarted extends AbstractEvent {

    private Long id;
    private String RentalId;
    private String RentalStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getRentalId() {
        return RentalId;
    }

    public void setRentalId(String RentalId) {
        this.RentalId = RentalId;
    }
    public String getRentalStatus() {
        return RentalStatus;
    }

    public void setRentalStatus(String RentalStatus) {
        this.RentalStatus = RentalStatus;
    }
}
