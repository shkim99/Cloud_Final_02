# 02 - 김석환수석(07808) 과제 - 책배달 서비스 구축

![image](https://user-images.githubusercontent.com/48976696/79927995-84614600-847c-11ea-9937-55cbcffff6cd.jpg)

이 시스템은 MSA/DDD/Event Storming/EDA 를 포괄하여 분석/설계/구현/운영 전단계로 구성하였습니다.
본 문서는 Cloud App 개발과정  최종 Test 문서입니다.


# Table of contents

- [책배달 서비스]
  - [서비스 시나리오](#서비스-시나리오)
  - [분석/설계](#분석설계)
  - [구현](#구현)
    - [DDD 의 적용](#ddd-의-적용)
    <!--- [동기식 호출과 Fallback 처리](#동기식-호출과-Fallback-처리)
    - [비동기식 호출과 Eventual Consistency](#비동기식-호출과-Eventual-Consistency)
    - [API 게이트웨이](#API-게이트웨이)
    - [Oauth](#oauth)
    -->
  - [운영](#운영)
    - [CI/CD 설정](#cicd-설정)
    <!--
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출--서킷-브레이킹--장애격리)
    -->
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)

# 서비스 시나리오
기능적 요구사항
1. 사용자는 책을 선택하고 배달 주문하며, 비용은 무료이다.
2. 사용자는 배달 주문한것을 취소할 수 있으나 배송 시작된 건은 취소 할 수 없다.
3. 책 정보를 등록하며, 개인별 책 대여 현황을 조회할 수 있어야 한다.
4. 책 주문 및 배달서비스는 핵심 서비스이며, 책 등록이 되지 않을 경우에도 서비스 되어야 한다.

비기능적 요구사항
1. 트랜잭션
    1. 등록된 책이 없을 경우에는 등록이 불가능해야 한다. 
2. 장애격리
    1. 대여서비스(core)만 온전하면 시스템은 정상적으로 수행되어야 한다.  Async (event-driven), Eventual Consistency
    2. 책관리(bookinfo), 배달(delivery)에 장애가 생겨도 대여(core)시스템은 정상적으로 작동한다.
    3. 대여서비스가 과중되면 대여기능을 잠시후에 하도록 유도한다.  Circuit breaker, fallback
3. 성능
    1. 고객은 대여 및 배송 결과를 시스템에서 확인할 수 있어야 한다.(Lookup 시스템으로 구현, CQRS)
4. 확장성
    1. 다음 단계로 신규 책 등록 시 고객에게 알람 등의 홍보서비스를 하기 위한 아키텍처가 고려되어야 한다.

# 분석/설계

* 이벤트스토밍 결과: 
![MSA설계](https://user-images.githubusercontent.com/48976696/79943830-e9c92d00-84a4-11ea-99a6-153e96985c0c.PNG)
- Core Domain : 대여(rental) 및 배송 (delivery) 도메인
- Supporting Domain : Lookup(CQRS) 도메인, 도서관리(bookinfo)
- General Domain : 알림(notice) 시스템.
*bookinfo 서비스는 core 서비스로 rental 서비스와 통합을 고려할수 있음

## 헥사고날 아키텍처 다이어그램 도출
![헥사고널](https://user-images.githubusercontent.com/48976696/80046262-1896e000-8545-11ea-8191-72e52ee402a2.PNG)



# 구현:
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

동물병원 예약/진료 시스템은 아래의 7가지 마이크로 서비스로 구성되어 있다.

1. 게이트 웨이: [https://github.com/AnimalHospital2/gateway.git](https://github.com/AnimalHospital2/gateway.git)
1. Oauth 시스템: [https://github.com/AnimalHospital2/ouath.git](https://github.com/AnimalHospital2/ouath.git)
1. 예약 시스템: [https://github.com/AnimalHospital2/reservation.git](https://github.com/AnimalHospital2/reservation.git)
1. 진료 시스템: [https://github.com/AnimalHospital2/diagnosis.git](https://github.com/AnimalHospital2/diagnosis.git)
1. 수납 시스템: [https://github.com/AnimalHospital2/acceptance.git](https://github.com/AnimalHospital2/acceptance.git)
1. 알림 시스템: [https://github.com/AnimalHospital2/notice.git](https://github.com/AnimalHospital2/notice.git)

- 게이트웨이 시스템은 수업시간에 이용한 예제를 프로젝트에 맞게 설정을 변경하였다. 
- Oauth 시스템은 수업시간에 이용한 예제를 그대로 활용하였다.

모든 시스템은 Spring Boot로 구현하였고 `mvn spring-boot:run` 명령어로 실행할 수 있다.

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하며 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 사용하였음.

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다.
RDB로는 H2를 사용하였다. 
``` java
package BookRental;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface BookInfoRepository extends PagingAndSortingRepository<BookInfo, Long>{

}
```

httpie 프로그램을 사용하여 입력한다.
```
# 예약 서비스의 예약
http post localhost:8081/reservations reservatorName="Jackson" reservationDate="2020-04-30" phoneNumber="010-1234-5678"

# 예약 서비스의 예약 취소
http delete localhost:8081/reservations/1

# 예약 서비스의 예약 변경
http patch localhost:8081/reservations/1 reservationDate="2020-05-01"

# 진료 기록 리스트 확인
http localhost:8083/medicalRecords

```

## 동기식 호출과 Fallback 처리

분석단계에서의 조건 중 하나로 예약(reservation)->진료(diagnosis) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 진료서비스를 호출하기 위하여 FeignClient를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (app) 결제이력Service.java
package com.example.reservation.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "diagnosis", url = "http://diagnosis:8080")
  public interface MedicalRecordService {

    @RequestMapping(method = RequestMethod.POST, path = "/medicalRecords")
    public void diagnosis(@RequestBody MedicalRecord medicalRecord);
}

```

- 예약완료 직후(@PostPersist) 진단을 요청하도록 처리
```
# Reservation.java (Entity)
    @PostPersist
       public void publishReservationReservedEvent() {
   
           // 예약이 발생하면 바로 진료 진행.
           MedicalRecord medicalRecord = new MedicalRecord();
   
           medicalRecord.setReservationId(this.getId());
           medicalRecord.setDoctor("Brad pitt");
           medicalRecord.setMedicalOpinion("별 이상 없습니다.");
           medicalRecord.setTreatment("그냥 집에서 푹 쉬면 나을 것입니다.");
   
           ReservationApplication.applicationContext.getBean(MedicalRecordService.class).diagnosis(medicalRecord);
   
   
           // Reserved 이벤트 발생
           ObjectMapper objectMapper = new ObjectMapper();
           String json = null;
   
           try {
               json = objectMapper.writeValueAsString(new ReservationReserved(this));
           } catch (JsonProcessingException e) {
               throw new RuntimeException("JSON format exception", e);
           }
   
           Processor processor = ReservationApplication.applicationContext.getBean(Processor.class);
           MessageChannel outputChannel = processor.output();
   
           outputChannel.send(MessageBuilder
                   .withPayload(json)
                   .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                   .build());
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 진단 시스템이 장애가 나면 예약도 못받는다는 것을 확인.(비즈상 무리가 있음..) 

```
# 진료 (diagnosis) 서비스를 잠시 내려놓음 (ctrl+c)

# 예약 처리
http post localhost:8081/reservations reservatorName="Jackson" reservationDate="2020-04-30" phoneNumber="010-1234-5678" #Fail

#진료 서비스 재기동
cd diagnosis
mvn spring-boot:run

#예약처리
http post localhost:8081/reservations reservatorName="Jackson" reservationDate="2020-04-30" phoneNumber="010-1234-5678" #Success
```

## 클러스터 적용 후 REST API 의 테스트
- http://52.231.118.148:8080/medicalRecords/     		//diagnosis 조회
- http://52.231.118.148:8080/reservations/       		//reservation 조회 
- http://52.231.118.148:8080/reservations reservatorName="pdc" reservationDate="202002" phoneNumber="0103701" //reservation 요청 
- Delete http://52.231.118.148:8080/reservations/1 	//reservation Cancel  Sample
- http://52.231.118.148:8080/reservationStats/   	  //lookup  조회
- http://52.231.118.148:8080/financialManagements/ 	//acceptance 조회


- 또한 과도한 예약 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)


## 비동기식 호출과 Eventual Consistency


진료가 이루어진 후에 수납시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 수납 시스템의 처리를 위하여 예약/진료 시스템이 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 진료이력을 남긴 후에 곧바로 진료가 이루어졌다는 이벤트를를 카프카로 송출한다(Publish)
 
```
// package Animal.Hospital.MedicalRecord;

    @PrePersist
    public void onPrePersist(){
        Treated treated = new Treated();
        BeanUtils.copyProperties(this, treated);
        treated.publish();
    }

```

- 수납 서비스에서는 진료완료 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

``` java
@Service
public class KafkaListener {

    @Autowired
    FinancialManagementRepository financialManagementRepository;

    @StreamListener(Processor.INPUT)

    public void TreatedEvent(@Payload Treated treated) {
        if(treated.getEventType().equals("Treated")) {
            System.out.println("수납요청 되었습니다.");

            FinancialManagement financialManagement = new FinancialManagement();
            financialManagement.setReservationId(treated.getReservationId());
            financialManagement.setFee(10000L);
            financialManagementRepository.save(financialManagement);
        }
    }
}
```

알림 시스템은 실제로 문자를 보낼 수는 없으므로, 예약/변경/취소 이벤트에 대해서 System.out.println 처리 하였다.
  
``` java
package com.example.notice;

@Service
public class KafkaListener {
    @StreamListener(Processor.INPUT)
    public void onReservationReservedEvent(@Payload ReservationReserved reservationReserved) {
        if(reservationReserved.getEventType().equals("ReservationReserved")) {
            System.out.println("예약 되었습니다.");
        }
    }

    @StreamListener(Processor.INPUT)
    public void onReservationChangedEvent(@Payload ReservationChanged reservationChanged) {
        if(reservationChanged.getEventType().equals("ReservationChanged")) {
            System.out.println("예약 변경 되었습니다.");
        }
    }

    @StreamListener(Processor.INPUT)
    public void onReservationCanceledEvent(@Payload ReservationCanceled reservationCanceled) {
        if(reservationCanceled.getEventType().equals("ReservationCanceled")) {
            System.out.println("예약 취소 되었습니다.");
        }
    }
}

```

수납, Lookup(CQRS) 시스템은 예약/진료와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 수납/Lookup 시스템이 유지보수로 인해 잠시 내려간 상태라도 예약/진료를 하는데 문제가 없다:
```
# 수납 서비스 (acceptance) 를 잠시 내려놓음 (ctrl+c)

#예약처리
http post localhost:8081/reservations reservatorName="Jackson" reservationDate="2020-04-30" phoneNumber="010-1234-5678" #Success

#예약상태 확인
http localhost:8081/reservations     # 예약 추가 된 것 확인

#수납 서비스 기동
cd acceptance
mvn spring-boot:run

#수납상태 확인
http localhost:8085/financialManagements     # 모든 예약-진료에 대해서 요금이 청구되엇음을 확인.

```
## API 게이트웨이
- Local 테스트 환경에서는 localhost:8080에서 Gateway API 가 작동.
- Cloud 환경에서는 http://52.231.118.148:8080 에서 Gateway API가 작동.
- application.yml 파일에 프로파일 별로 Gateway 설정.
### Gateway 설정 파일
```yaml
server:
  port: 8088

---
spring:
  profiles: default
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8088/.well-known/jwks.json
  cloud:
    gateway:
      routes:
        - id: reservation
          uri: http://localhost:8081
          predicates:
            - Path=/reservations/**
        - id: diagnosis
          uri: http://localhost:8083
          predicates:
            - Path=/medicalRecords/**
        - id: lookup
          uri: http://localhost:8084
          predicates:
            - Path=/reservationStats/**
        - id: acceptance
          uri: http://localhost:8085
          predicates:
            - Path=/financialManagements/**
        - id: oauth
          uri: http://localhost:8090
          predicates:
            - Path=/oauth/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true


---
spring:
  profiles: docker
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8080/.well-known/jwks.json
  cloud:
    gateway:
      routes:
        - id: reservation
          uri: http://reservation:8080
          predicates:
            - Path=/reservations/**
        - id: diagnosis
          uri: http://diagnosis:8080
          predicates:
            - Path=/medicalRecords/**
        - id: lookup
          uri: http://lookup:8080
          predicates:
            - Path=/reservationStats/**
        - id: acceptance
          uri: http://acceptance:8080
          predicates:
            - Path=/financialManagements/**
        - id: oauth
          uri: http://oauth:8080
          predicates:
            - Path=/oauth/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```
<!--
## Oauth 인증 적용.
- Oauth 인증 적용. 
- But, 수업 중에 사용한 Oauth 프로젝트를 그대로 이용하여 Gateway에 붙이기만 함.
-->

# 운영

## Azure 및 GitHub 정보
#### Azure 서비스 생성
![azure_서비스생성](https://user-images.githubusercontent.com/48976696/80047159-a247ad00-8547-11ea-8b8d-3c37a20dc9cd.PNG)


#### GitHub Repository 생성
![github_Repository생성](https://user-images.githubusercontent.com/48976696/80047216-cc00d400-8547-11ea-9c2c-79fc3c98b87c.PNG)

#### Cloud 내 kafka 설치 및 토픽생성, 이벤트 확인
![kafka_확인](https://user-images.githubusercontent.com/48976696/80047285-fbafdc00-8547-11ea-8db6-4c681fea3cda.PNG)


## CI/CD 설정

각 구현체들은 각자의 Git을 통해 빌드되며, Git Master에 트리거 되어 있다. pipeline build script 는 각 프로젝트 폴더 이하에 azure_pipeline.yml 에 포함되었다.

azure_pipelist.yml 참고

kubernetes Service
```yaml
trigger:
- master

resources:
- repo: self

variables:
- group: common-value
  # containerRegistry: 'event.azurecr.io'
  # containerRegistryDockerConnection: 'acr'
  # environment: 'aks.default'
- name: imageRepository
  value: 'order'
- name: dockerfilePath
  value: '**/Dockerfile'
- name: tag
  value: '$(Build.BuildId)'
  # Agent VM image name
- name: vmImageName
  value: 'ubuntu-latest'
- name: MAVEN_CACHE_FOLDER
  value: $(Pipeline.Workspace)/.m2/repository
- name: MAVEN_OPTS
  value: '-Dmaven.repo.local=$(MAVEN_CACHE_FOLDER)'


stages:
- stage: Build
  displayName: Build stage
  jobs:
  - job: Build
    displayName: Build
    pool:
      vmImage: $(vmImageName)
    steps:
    - task: CacheBeta@1
      inputs:
        key: 'maven | "$(Agent.OS)" | **/pom.xml'
        restoreKeys: |
           maven | "$(Agent.OS)"
           maven
        path: $(MAVEN_CACHE_FOLDER)
      displayName: Cache Maven local repo
    - task: Maven@3
      inputs:
        mavenPomFile: 'pom.xml'
        options: '-Dmaven.repo.local=$(MAVEN_CACHE_FOLDER)'
        javaHomeOption: 'JDKVersion'
        jdkVersionOption: '1.8'
        jdkArchitectureOption: 'x64'
        goals: 'package'
    - task: Docker@2
      inputs:
        containerRegistry: $(containerRegistryDockerConnection)
        repository: $(imageRepository)
        command: 'buildAndPush'
        Dockerfile: '**/Dockerfile'
        tags: |
          $(tag)

- stage: Deploy
  displayName: Deploy stage
  dependsOn: Build

  jobs:
  - deployment: Deploy
    displayName: Deploy
    pool:
      vmImage: $(vmImageName)
    environment: $(environment)
    strategy:
      runOnce:
        deploy:
          steps:
          - task: Kubernetes@1
            inputs:
              connectionType: 'Kubernetes Service Connection'
              namespace: 'default'
              command: 'apply'
              useConfigurationFile: true
              configurationType: 'inline'
              inline: |
                apiVersion: apps/v1
                kind: Deployment
                metadata:
                  name: $(imageRepository)
                  labels:
                    app: $(imageRepository)
                spec:
                  replicas: 1
                  selector:
                    matchLabels:
                      app: $(imageRepository)
                  template:
                    metadata:
                      labels:
                        app: $(imageRepository)
                    spec:
                      containers:
                        - name: $(imageRepository)
                          image: $(containerRegistry)/$(imageRepository):$(tag)
                          ports:
                            - containerPort: 8080
                          readinessProbe:
                            httpGet:
                              path: /actuator/health
                              port: 8080
                            initialDelaySeconds: 10
                            timeoutSeconds: 2
                            periodSeconds: 5
                            failureThreshold: 10
                          livenessProbe:
                            httpGet:
                              path: /actuator/health
                              port: 8080
                            initialDelaySeconds: 120
                            timeoutSeconds: 2
                            periodSeconds: 5
                            failureThreshold: 5
              secretType: 'dockerRegistry'
              containerRegistryType: 'Azure Container Registry'
          - task: Kubernetes@1
            inputs:
              connectionType: 'Kubernetes Service Connection'
              namespace: 'default'
              command: 'apply'
              useConfigurationFile: true
              configurationType: 'inline'
              inline: |
                apiVersion: v1
                kind: Service
                metadata:
                  name: $(imageRepository)
                  labels:
                    app: $(imageRepository)
                spec:
                  ports:
                    - port: 8080
                      targetPort: 8080
                  selector:
                    app: $(imageRepository)
              secretType: 'dockerRegistry'
              containerRegistryType: 'Azure Container Registry'
```
##  Pipe Line 등록 후  Deploy 결과
![PipeLine_bookinfo](https://user-images.githubusercontent.com/48976696/80052286-48e67a80-8555-11ea-8117-7f68f2a164ce.PNG)


## 오토스케일 아웃
- BookInfo서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정 (CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려줌)
```
kubectl autoscale deploy bookinfo --min=1 --max=10 --cpu-percent=15
```

## 무정지 재배포
- 모든 프로젝트의 readiness probe 및 liveness probe 설정.
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 10
  timeoutSeconds: 2
  periodSeconds: 5
  failureThreshold: 10
livenessProbe:
  httpGet:
     path: /actuator/health
     port: 8080
  initialDelaySeconds: 120
  timeoutSeconds: 2
  periodSeconds: 5
  failureThreshold: 5
```
