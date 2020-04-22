package BookRental;

public class RentalOrdered extends AbstractEvent {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long bookId;
    private String address;
    private String rentalId;

    public RentalOrdered(){
        super();
    }

    public RentalOrdered(String rentalId, Long customerId, String customerName, Long bookId, String address) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getRentalId() {
        return rentalId;
    }

    public void setRentalId(String rentalId) {
        this.rentalId = rentalId;
    }
}
