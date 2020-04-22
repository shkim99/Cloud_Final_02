package BookRental;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookInfoService {
	
	@Autowired
	private BookInfoRepository bookInfoRepository;
	public void save(BookInfo bookInfo) {
		bookInfoRepository.save(bookInfo);
	}
}
