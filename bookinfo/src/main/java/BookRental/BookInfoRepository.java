package BookRental;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface BookInfoRepository extends PagingAndSortingRepository<BookInfo, Long>{

}