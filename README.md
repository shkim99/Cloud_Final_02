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
  - [운영](#운영)
    - [CI/CD 설정](#cicd-설정)
    - [무정지 재배포](#무정지-재배포)
    - [서비스 확인](#서비스확인)
        

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
- General Domain : 알림(notice) 시스템.  (추가 확장)
*bookinfo 서비스는 core 서비스로 rental 서비스와 통합을 고려할수 있음

## 헥사고날 아키텍처 다이어그램 도출
![헥사고널](https://user-images.githubusercontent.com/48976696/80046262-1896e000-8545-11ea-8191-72e52ee402a2.PNG)



# 구현:
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

책배달시스템은 아래의 5가지 마이크로 서비스로 구성되어 있다.

1. 게이트 웨이: [https://github.com/shkim99/book-gateway.git](https://github.com/shkim99/book-gateway.git)
1. 도서관리   : [https://github.com/shkim99/book-bookinfo.git](https://github.com/shkim99/book-bookinfo.git)
1. 대여관리   : [https://github.com/shkim99/book-rental.git](https://github.com/shkim99/book-rental.git)
1. 배송관리   : [https://github.com/shkim99/book-delivery.git](https://github.com/shkim99/book-delivery.git)
1. 현황조회   : [https://github.com/shkim99/book-lookup.git](https://github.com/shkim99/book-lookup.git)

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

## 클러스터 적용 후 REST API 의 테스트
- http http://52.231.117.105:8080/bookinfoes     		//bookinfo 조회


## API 게이트웨이
- Local 테스트 환경에서는 localhost:8080에서 Gateway API 가 작동.
- Cloud 환경에서는 http http://52.231.117.105:8080/ 에서 Gateway API가 작동.
- application.yml 파일에 프로파일 별로 Gateway 설정.
### Gateway 설정 

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
### 서비스확인
![서비스확인](https://user-images.githubusercontent.com/48976696/80066931-dfc42e80-8577-11ea-83eb-72252da4c96a.PNG)
