package BookRental;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

// @RestController
 public class BookInfoController {

  @Autowired

  //MedicalRecordsService medicalRecordsService;
  BookInfoService bookInfoService;

  @RequestMapping(value = "/bookinfo_request",
          method = RequestMethod.GET,
          produces = "application/json;charset=UTF-8")

  public boolean bookinfoRequest(@RequestParam(name = "bookId") String bookId,
                                @RequestParam(name = "bookName") String bookName)
          throws Exception {
              System.out.println("##### /bookinfo_request called #####");

              BookInfo bookInfo = new BookInfo();

              bookInfo.setBookId(bookId);
              bookInfo.setBookName(bookName);

              bookInfoService.save(bookInfo);

              System.out.println("##### 저장 완료 #####");

   //Thread.sleep(3000); //2초 대기  부하테스트시  사용
   return true;
          }
}
