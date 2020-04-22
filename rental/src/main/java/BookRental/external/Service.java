
package BookRental.external;

import BookRental.RentalManagement;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * Created by uengine on 2018. 11. 21..
 */

//@FeignClient(name="rental", url="http://:8080")
//public interface Service {
//shkim  오류 주석 처리
//    @RequestMapping(method= RequestMethod.GET, path="/")
//    public void deliveryStart(@RequestBody  );

//}


@FeignClient(name = "rental", url = "http://localhost:8082")
public interface Service {

    @RequestMapping(method = RequestMethod.POST, path = "/rentals")
    public void setRentalManagement(@RequestBody RentalManagement rentalManagement);
}