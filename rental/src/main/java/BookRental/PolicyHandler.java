package BookRental;

import BookRental.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_대여상태변경(@Payload DeliveryStarted deliveryStarted){

        if(deliveryStarted.isMe()){
            System.out.println("##### listener 대여상태변경 : " + deliveryStarted.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryFinished_대여상태변경(@Payload DeliveryFinished deliveryFinished){

        if(deliveryFinished.isMe()){
            System.out.println("##### listener 대여상태변경 : " + deliveryFinished.toJson());
        }
    }

}
