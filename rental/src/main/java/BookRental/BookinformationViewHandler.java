package BookRental;

import BookRental.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BookinformationViewHandler {


    @Autowired
    private BookinformationRepository bookinformationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookRegistered_then_CREATE_1 (@Payload BookRegistered bookRegistered) {
        try {
            if (bookRegistered.isMe()) {
                // view 객체 생성
                Bookinformation bookinformation = new Bookinformation();
                // view 객체에 이벤트의 Value 를 set 함
                // view 레파지 토리에 save
                bookinformationRepository.save(bookinformation);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookDeleted_then_UPDATE_1(@Payload BookDeleted bookDeleted) {
        try {
            if (bookDeleted.isMe()) {
                // view 객체 조회
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}