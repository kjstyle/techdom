# Vehicle Event Management System

이 프로젝트는 차량의 다양한 이벤트(시동 ON/OFF, 지오펜싱, 주기적 보고 등)를 기록하고 관리하는 시스템입니다.  
타임스탬프 기반의 시계열 데이터 저장, 이벤트 핸들러, 테스트 케이스를 포함하여 유지보수와 확장성을 고려한 설계가 이루어졌습니다.

---

## 주요 기술 스택

- **Backend Framework**: Jakarta EE, Spring MVC, Spring Data JPA
- **Programming Language**: Java 21
- **Database**: PostgreSQL (TimescaleDB 확장)
- **Testing Framework**: JUnit 5, Mockito
- **Object-Relational Mapping**: Hibernate
- **Utilities**: Lombok, Jackson, Jakarta Persistence

---

## 주요 기능

### 1. 차량 이벤트 로그 기록
- 모든 차량 이벤트(시동 ON/OFF, 지오펜스 진입/이탈 등)를 **TimescaleDB**를 활용해 시계열 데이터로 관리합니다.
- 주요 이벤트 유형:
  - `IGNITION_ON`: 시동 ON 이벤트
  - `IGNITION_OFF`: 시동 OFF 이벤트
  - `GEOFENCE_IN`: 지오펜스 진입 이벤트
  - `GEOFENCE_OUT`: 지오펜스 이탈 이벤트
  - `PERIODIC_REPORT`: 주기적 차량 상태 보고

### 2. 고도로 확장 가능한 테이블 및 ENUM 정의
- 차량, 지오펜스, 정책 등의 관리를 위한 다수의 관계형 테이블과 **ENUM** 기반 데이터 타입 제공.
- `GpsCondition`, `VehicleEventType` 등 명확한 데이터 유형 관리.

### 3. 이벤트 핸들러
- 실제 차량 이벤트 처리 로직을 담당하는 핸들러로 구성.
  - 각 이벤트 타입별: 예를 들어 `IgnitionOnEventHandler`, `IgnitionOffEventHandler`
  - 비즈니스 규칙 적용 및 예외 처리.

### 4. 테스트
- **Unit Test**와 **Mock Test**로 다양한 이벤트 처리 로직을 검증.
- 주요 테스트 케이스:
  - 시동 ON 핸들러 (`IgnitionOnEventHandlerTest`)
  - 시동 OFF 핸들러 (`IgnitionOffEventHandlerTest`)

---

## 데이터베이스 설계

### 주요 테이블
1. **`vehicle_event_log`**
   - 차량 이벤트 정보를 시계열로 저장.
   - 주요 컬럼:
     - `event_timestamp_utc`: 이벤트 발생 UTC 시간.
     - `event_type`: 이벤트 종류(`IGNITION_ON`, `IGNITION_OFF` 등).
     - `gps_status`: GPS 상태(`NORMAL`, `ABNORMAL`, `NOT_INSTALLED` 등).
     - `latitude`/`longitude`: 차량 위치 데이터.

2. **`vehicle_info`**
   - 차량 기본 정보 저장.
   - 주요 컬럼:
     - `mdn`: 차량 ID.
     - `last_latitude`/`last_longitude`: 마지막 위치 정보.
     - `total_accumulated_distance`: 누적 주행 거리.

3. **ENUM 타입**
   - `GpsCondition` (`A`, `V`, `P` 등).
   - `VehicleEventType` (`IGNITION_ON`, `GEOFENCE_IN` 등).

---

## 이벤트 핸들러

### 1. IgnitionOnEventHandler
- 시동 ON 이벤트 처리 로직.
- 주요 로직:
  - 이전 시동 OFF 이벤트 조회.
  - GPS 상태에 따라 알림 로깅.

### 2. IgnitionOffEventHandler
- 시동 OFF 이벤트 처리 로직.
- 주요 로직:
  - 누적 주행 거리 업데이트.
  - 직전 시동 ON 이벤트 확인 및 로그.

---

## 테스트

### 주요 테스트 케이스
1. **IgnitionOnEventHandlerTest**
   - 일반적인 시동 ON 이벤트 처리.
   - GPS 상태 이상 시 경고 로그 처리.
   - 최초 시동 ON 이벤트 처리 로직 검증.

2. **IgnitionOffEventHandlerTest**
   - 일반적인 시동 OFF 이벤트 처리.
   - 직전 시동 ON 이벤트 확인 로직 검증.
   - 최초 시동 OFF 이벤트 규격 확인.

---


## 사용법

### 1. 프로젝트 실행
1. **데이터베이스 준비**:
   - PostgreSQL 및 TimescaleDB 확장 설치.
   - `ddl.sql`을 실행하여 기본 테이블과 트리거 생성.

2. **애플리케이션 실행**:
   - IDE (예: IntelliJ IDEA)에서 실행.
   - Spring Boot 실행 명령:
     ```bash
     ./mvnw spring-boot:run
     ```

### 2. 테스트 실행
- `mvn test` 명령을 통해 모든 테스트 실행.
- 개별 클래스 테스트:
  - `IgnitionOnEventHandlerTest`
  - `IgnitionOffEventHandlerTest`

---
