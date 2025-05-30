-- TimescaleDB 확장 기능 활성화 (데이터베이스 당 한 번만 실행)
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- 'updated_at' 컬럼 자동 업데이트 함수
-- 이 함수는 'updated_at' 컬럼이 있는 모든 테이블에서 재사용됩니다.
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 기존 테이블, 함수, 트리거, ENUM 타입 삭제 (안전하게 모든 객체 삭제 후 재생성을 위해)
-- 의존성 역순으로 삭제하거나 CASCADE를 사용하여 모두 삭제합니다.
-- 이 순서는 의존성을 고려하여 수동으로 정렬되었습니다.
DROP TABLE IF EXISTS device_control_command_log CASCADE;
DROP TABLE IF EXISTS device_geofence_config_log CASCADE;
DROP TABLE IF EXISTS device_config_response_log CASCADE;
DROP TABLE IF EXISTS device_config_request_log CASCADE;
DROP TABLE IF EXISTS device_token_request_log CASCADE;
DROP TABLE IF EXISTS device_auth_token CASCADE;
DROP TABLE IF EXISTS vehicle_policy_assignment CASCADE;
DROP TABLE IF EXISTS vehicle_driver_map CASCADE;
DROP TABLE IF EXISTS vehicle_event_log CASCADE;
DROP TABLE IF EXISTS role_permission_map CASCADE;
DROP TABLE IF EXISTS employee_role_map CASCADE;
DROP TABLE IF EXISTS employee_info CASCADE;
DROP TABLE IF EXISTS geofence_policy_item_map CASCADE;
DROP TABLE IF EXISTS geofence_policy CASCADE;
DROP TABLE IF EXISTS geofence_item CASCADE;
DROP TABLE IF EXISTS organization_unit_info CASCADE;
DROP TABLE IF EXISTS permission_info CASCADE;
DROP TABLE IF EXISTS role_info CASCADE;
DROP TABLE IF EXISTS vehicle_info CASCADE;
DROP TABLE IF EXISTS company_info CASCADE;
DROP TABLE IF EXISTS raw_request_log CASCADE;
DROP TABLE IF EXISTS token CASCADE;
DROP TABLE IF EXISTS device CASCADE;
DROP TABLE IF EXISTS maintenance_log CASCADE;
DROP TABLE IF EXISTS alert_log CASCADE;
DROP TABLE IF EXISTS policy CASCADE;
DROP TABLE IF EXISTS vehicle_policy CASCADE;
DROP TABLE IF EXISTS "user" CASCADE; -- "user" 테이블도 삭제
DROP TABLE IF EXISTS department CASCADE; -- department 테이블도 삭제
DROP TABLE IF EXISTS setting_info CASCADE; -- setting_info 테이블도 삭제
DROP TABLE IF EXISTS control_info CASCADE; -- control_info 테이블도 삭제
DROP TABLE IF EXISTS geo_fence CASCADE; -- geo_fence 테이블도 삭제
DROP TABLE IF EXISTS vehicle_assignment CASCADE; -- vehicle_assignment 테이블도 삭제


-- 기존에 정의된 ENUM 타입도 삭제 (재정의를 위해)
DROP TYPE IF EXISTS fuel_type_enum CASCADE;
DROP TYPE IF EXISTS vehicle_event_type_enum CASCADE;
DROP TYPE IF EXISTS gps_condition_enum CASCADE;
DROP TYPE IF EXISTS geofence_trigger_type_enum CASCADE;
DROP TYPE IF EXISTS geofence_store_type_enum CASCADE;
DROP TYPE IF EXISTS org_unit_type_enum CASCADE;
DROP TYPE IF EXISTS user_status_enum CASCADE;

-- ENUM 타입 정의
CREATE TYPE fuel_type_enum AS ENUM ('gasoline', 'diesel', 'hybrid', 'electric', 'lpg');


CREATE TYPE geofence_trigger_type_enum AS ENUM ('ENTER', 'EXIT', 'BOTH');
CREATE TYPE geofence_store_type_enum AS ENUM ('1', '2'); -- 1:추가, 2:삭제 (규격서 기준)
CREATE TYPE org_unit_type_enum AS ENUM ('COMPANY', 'DIVISION', 'DEPARTMENT', 'TEAM');
CREATE TYPE user_status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED');


-- 0. 회사 정보 테이블 (company_info)
-- 시스템을 사용하는 회사 정보를 관리하는 최상위 조직 단위입니다.
CREATE TABLE IF NOT EXISTS company_info (
                                            company_id BIGSERIAL PRIMARY KEY,                    -- 회사 고유 ID
                                            company_name VARCHAR(100) NOT NULL UNIQUE,           -- 회사명
    business_registration_number VARCHAR(20) UNIQUE,     -- 사업자 등록 번호
    company_code VARCHAR(20) UNIQUE,                     -- 회사 코드 (필요 시)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()    -- 레코드 최종 수정 일시
    );

COMMENT ON TABLE company_info IS '시스템을 사용하는 회사 정보를 관리하는 테이블';
COMMENT ON COLUMN company_info.company_id IS '회사 고유 ID';
COMMENT ON COLUMN company_info.company_name IS '회사명';
COMMENT ON COLUMN company_info.business_registration_number IS '사업자 등록 번호';
COMMENT ON COLUMN company_info.company_code IS '회사 코드 (필요 시)';
COMMENT ON COLUMN company_info.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN company_info.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_company_info_updated_at
    BEFORE UPDATE ON company_info
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- 0-1. 조직 단위 정보 테이블 (organization_unit_info)
-- 회사 내 부서, 팀 등 조직 단위를 관리합니다. (계층 구조 제거)
CREATE TABLE IF NOT EXISTS organization_unit_info (
                                                      org_unit_id BIGSERIAL PRIMARY KEY,                   -- 조직 단위 고유 ID
                                                      company_id BIGINT NOT NULL,                          -- 소속 회사 ID
                                                      org_unit_name VARCHAR(100) NOT NULL,                 -- 조직 단위명 (예: 영업1팀, 물류부)
    org_unit_type org_unit_type_enum NOT NULL,           -- 조직 단위 타입 (예: 'DIVISION', 'DEPARTMENT', 'TEAM')
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),    -- 레코드 최종 수정 일시
    FOREIGN KEY (company_id) REFERENCES company_info (company_id) ON DELETE CASCADE,
    UNIQUE (company_id, org_unit_name)                   -- 한 회사 내 조직 단위 이름 중복 불가
    );

COMMENT ON TABLE organization_unit_info IS '회사 내 부서, 팀 등 조직 단위를 관리하는 테이블';
COMMENT ON COLUMN organization_unit_info.org_unit_id IS '조직 단위 고유 ID';
COMMENT ON COLUMN organization_unit_info.company_id IS '소속 회사 ID';
COMMENT ON COLUMN organization_unit_info.org_unit_name IS '조직 단위명 (예: 영업1팀, 물류부)';
COMMENT ON COLUMN organization_unit_info.org_unit_type IS '조직 단위 타입 (예: ''DIVISION'', ''DEPARTMENT'', ''TEAM'')';
COMMENT ON COLUMN organization_unit_info.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN organization_unit_info.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_organization_unit_info_updated_at
    BEFORE UPDATE ON organization_unit_info
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- 0-2. 지오펜스 아이템 정의 테이블 (geofence_item)
-- 개별 지오펜스의 물리적/논리적 속성을 정의합니다.
CREATE TABLE IF NOT EXISTS geofence_item (
                                             geofence_item_id BIGSERIAL PRIMARY KEY,              -- 지오펜스 아이템 고유 ID
                                             geofence_name VARCHAR(100) NOT NULL,                 -- 지오펜스 명칭 (예: "본사 출입 지역", "제1물류창고")
    latitude NUMERIC(9,6) NOT NULL,                      -- 지오펜스 중심 위도
    longitude NUMERIC(9,6) NOT NULL,                     -- 지오펜스 중심 경도
    radius_meters INTEGER NOT NULL,                      -- 지오펜스 반경 (미터 단위)
    event_trigger_type geofence_trigger_type_enum NOT NULL, -- 이벤트 발생 타입 (예: 'ENTER':진입, 'EXIT':이탈, 'BOTH':모두)
    description TEXT,                                    -- 상세 설명
    is_active BOOLEAN DEFAULT TRUE,                      -- 지오펜스 활성화 여부
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()    -- 레코드 최종 수정 일시
    );

COMMENT ON TABLE geofence_item IS '개별 지오펜스의 물리적/논리적 속성을 정의하는 테이블';
COMMENT ON COLUMN geofence_item.geofence_item_id IS '지오펜스 아이템 고유 ID';
COMMENT ON COLUMN geofence_item.geofence_name IS '지오펜스 명칭 (예: "본사 출입 지역", "제1물류창고")';
COMMENT ON COLUMN geofence_item.latitude IS '지오펜스 중심 위도';
COMMENT ON COLUMN geofence_item.longitude IS '지오펜스 중심 경도';
COMMENT ON COLUMN geofence_item.radius_meters IS '지오펜스 반경 (미터 단위)';
COMMENT ON COLUMN geofence_item.event_trigger_type IS '이벤트 발생 타입 (예: ''ENTER'':진입, ''EXIT'':이탈, ''BOTH'':모두)';
COMMENT ON COLUMN geofence_item.description IS '상세 설명';
COMMENT ON COLUMN geofence_item.is_active IS '지오펜스 활성화 여부';
COMMENT ON COLUMN geofence_item.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN geofence_item.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_geofence_item_updated_at
    BEFORE UPDATE ON geofence_item
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- 0-3. 지오펜스 정책 정의 테이블 (geofence_policy)
-- 여러 지오펜스 아이템을 묶어 하나의 지오펜스 정책을 정의합니다. 회사에 종속됩니다.
CREATE TABLE IF NOT EXISTS geofence_policy (
                                               policy_id BIGSERIAL PRIMARY KEY,                     -- 정책 고유 ID
                                               company_id BIGINT NOT NULL,                          -- 소속 회사 ID
                                               policy_name VARCHAR(100) NOT NULL,                   -- 정책 명칭 (예: "영업팀 기본 지오펜스 정책", "물류 차량 정책")
    description TEXT,                                    -- 정책 설명
    is_active BOOLEAN DEFAULT TRUE,                      -- 정책 활성화 여부
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),    -- 레코드 최종 수정 일시
    FOREIGN KEY (company_id) REFERENCES company_info (company_id) ON DELETE CASCADE,
    UNIQUE (company_id, policy_name)                     -- 한 회사 내 정책 이름 중복 불가
    );

COMMENT ON TABLE geofence_policy IS '여러 지오펜스 아이템을 묶어 하나의 지오펜스 정책을 정의하는 테이블. 회사에 종속됩니다.';
COMMENT ON COLUMN geofence_policy.policy_id IS '정책 고유 ID';
COMMENT ON COLUMN geofence_policy.company_id IS '소속 회사 ID';
COMMENT ON COLUMN geofence_policy.policy_name IS '정책 명칭 (예: "영업팀 기본 지오펜스 정책", "물류 차량 정책")';
COMMENT ON COLUMN geofence_policy.description IS '정책 설명';
COMMENT ON COLUMN geofence_policy.is_active IS '정책 활성화 여부';
COMMENT ON COLUMN geofence_policy.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN geofence_policy.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_geofence_policy_updated_at
    BEFORE UPDATE ON geofence_policy
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- 0-4. 지오펜스 정책-아이템 매핑 테이블 (geofence_policy_item_map)
-- 지오펜스 정책이 어떤 지오펜스 아이템들을 포함하는지 매핑합니다.
CREATE TABLE IF NOT EXISTS geofence_policy_item_map (
                                                        policy_id BIGINT NOT NULL,                           -- 정책 ID
                                                        geofence_item_id BIGINT NOT NULL,                    -- 지오펜스 아이템 ID
                                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    PRIMARY KEY (policy_id, geofence_item_id),           -- 복합 기본 키
    FOREIGN KEY (policy_id) REFERENCES geofence_policy (policy_id) ON DELETE CASCADE,
    FOREIGN KEY (geofence_item_id) REFERENCES geofence_item (geofence_item_id) ON DELETE CASCADE
    );

COMMENT ON TABLE geofence_policy_item_map IS '지오펜스 정책이 어떤 지오펜스 아이템들을 포함하는지 매핑하는 테이블';
COMMENT ON COLUMN geofence_policy_item_map.policy_id IS '지오펜스 정책 ID';
COMMENT ON COLUMN geofence_policy_item_map.geofence_item_id IS '지오펜스 아이템 ID';
COMMENT ON COLUMN geofence_policy_item_map.created_at IS '레코드 생성 일시';


-- 0-5. 직원/사용자 정보 테이블 (employee_info)
-- 시스템을 사용하는 회사 직원들의 기본 정보를 관리합니다.
CREATE TABLE IF NOT EXISTS employee_info (
                                             employee_id BIGSERIAL PRIMARY KEY,                   -- 직원 고유 ID (사번 등)
                                             company_id BIGINT NOT NULL,                          -- 소속 회사 ID
                                             org_unit_id BIGINT,                                  -- 소속 조직 단위 ID (FK to organization_unit_info, Optional)
                                             user_name VARCHAR(100) NOT NULL,                     -- 직원 이름
    login_id VARCHAR(50) NOT NULL UNIQUE,                -- 로그인 ID (UNIQUE)
    password_hash VARCHAR(255) NOT NULL,                 -- 비밀번호 해시 (보안상 평문 저장 금지)
    email VARCHAR(100) UNIQUE,                           -- 이메일 주소
    phone_number VARCHAR(20),                            -- 전화번호
    status user_status_enum NOT NULL DEFAULT 'ACTIVE',   -- 계정 상태 (예: 'ACTIVE', 'INACTIVE', 'LOCKED')
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),    -- 레코드 최종 수정 일시
    FOREIGN KEY (company_id) REFERENCES company_info (company_id) ON DELETE CASCADE,
    FOREIGN KEY (org_unit_id) REFERENCES organization_unit_info (org_unit_id) ON DELETE SET NULL
    );

COMMENT ON TABLE employee_info IS '시스템을 사용하는 회사 직원들의 기본 정보를 관리하는 테이블';
COMMENT ON COLUMN employee_info.employee_id IS '직원 고유 ID (사번 등)';
COMMENT ON COLUMN employee_info.company_id IS '소속 회사 ID';
COMMENT ON COLUMN employee_info.org_unit_id IS '소속 조직 단위 ID';
COMMENT ON COLUMN employee_info.user_name IS '직원 이름';
COMMENT ON COLUMN employee_info.login_id IS '로그인 ID';
COMMENT ON COLUMN employee_info.password_hash IS '비밀번호 해시 (보안상 평문 저장 금지)';
COMMENT ON COLUMN employee_info.email IS '이메일 주소';
COMMENT ON COLUMN employee_info.phone_number IS '전화번호';
COMMENT ON COLUMN employee_info.status IS '계정 상태 (예: ''ACTIVE'', ''INACTIVE'', ''LOCKED'')';
COMMENT ON COLUMN employee_info.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN employee_info.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_employee_info_updated_at
    BEFORE UPDATE ON employee_info
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- 0-6. 권한 정보 테이블 (permission_info)
-- 시스템 내의 개별 권한들을 정의합니다.
CREATE TABLE IF NOT EXISTS permission_info (
                                               permission_id BIGSERIAL PRIMARY KEY,                 -- 권한 고유 ID
                                               permission_code VARCHAR(50) NOT NULL UNIQUE,         -- 권한 코드 (예: 'VEHICLE_VIEW', 'DRIVE_LOG_READ')
    description TEXT,                                    -- 권한 설명
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()    -- 레코드 최종 수정 일시
    );

COMMENT ON TABLE permission_info IS '시스템 내의 개별 권한들을 정의하는 테이블';
COMMENT ON COLUMN permission_info.permission_id IS '권한 고유 ID';
COMMENT ON COLUMN permission_info.permission_code IS '권한 코드 (예: ''VEHICLE_VIEW'', ''DRIVE_LOG_READ'', ''DRIVE_LOG_PRINT'', ''GEOFENCE_MANAGE'', ''USER_MANAGE'')';
COMMENT ON COLUMN permission_info.description IS '권한 설명';
COMMENT ON COLUMN permission_info.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN permission_info.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_permission_info_updated_at
    BEFORE UPDATE ON permission_info
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- 0-7. 역할 정보 테이블 (role_info)
-- 시스템 내에서 정의된 역할들을 관리합니다.
CREATE TABLE IF NOT EXISTS role_info (
                                         role_id BIGSERIAL PRIMARY KEY,                       -- 역할 고유 ID
                                         role_name VARCHAR(50) NOT NULL UNIQUE,               -- 역할 명칭 (예: '시스템 관리자', '차량 운전자', '운행일지 담당자')
    description TEXT,                                    -- 역할 설명
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()    -- 레코드 최종 수정 일시
    );

COMMENT ON TABLE role_info IS '시스템 내에서 정의된 역할들을 관리하는 테이블';
COMMENT ON COLUMN role_info.role_id IS '역할 고유 ID';
COMMENT ON COLUMN role_info.role_name IS '역할 명칭 (예: ''시스템 관리자'', ''차량 운전자'', ''운행일지 담당자'')';
COMMENT ON COLUMN role_info.description IS '역할 설명';
COMMENT ON COLUMN role_info.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN role_info.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_role_info_updated_at
    BEFORE UPDATE ON role_info
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- 0-8. 역할-권한 매핑 테이블 (role_permission_map)
-- 각 역할이 어떤 구체적인 권한을 가지는지 매핑합니다.
CREATE TABLE IF NOT EXISTS role_permission_map (
                                                   role_id BIGINT NOT NULL,                             -- 역할 ID
                                                   permission_id BIGINT NOT NULL,                       -- 권한 ID
                                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    PRIMARY KEY (role_id, permission_id),                -- 복합 기본 키
    FOREIGN KEY (role_id) REFERENCES role_info (role_id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission_info (permission_id) ON DELETE CASCADE
    );

COMMENT ON TABLE role_permission_map IS '각 역할이 어떤 구체적인 권한을 가지는지 매핑하는 테이블';
COMMENT ON COLUMN role_permission_map.role_id IS '역할 ID';
COMMENT ON COLUMN role_permission_map.permission_id IS '권한 ID';
COMMENT ON COLUMN role_permission_map.created_at IS '레코드 생성 일시';


-- 0-9. 사용자-역할 매핑 테이블 (employee_role_map)
-- 각 직원에게 어떤 역할이 할당되었는지 매핑합니다. (N:M 관계)
CREATE TABLE IF NOT EXISTS employee_role_map (
                                                 employee_id BIGINT NOT NULL,                         -- 직원 ID
                                                 role_id BIGINT NOT NULL,                             -- 역할 ID
                                                 assigned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),  -- 역할 할당 일시
    PRIMARY KEY (employee_id, role_id),                  -- 복합 기본 키
    FOREIGN KEY (employee_id) REFERENCES employee_info (employee_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role_info (role_id) ON DELETE CASCADE
    );

COMMENT ON TABLE employee_role_map IS '각 직원에게 어떤 역할이 할당되었는지 매핑하는 테이블';
COMMENT ON COLUMN employee_role_map.employee_id IS '직원 ID';
COMMENT ON COLUMN employee_role_map.role_id IS '역할 ID';
COMMENT ON COLUMN employee_role_map.assigned_at IS '역할 할당 일시';


-- 1. 차량 기본 정보 테이블 (vehicle_info)
-- 차량 단말의 고유 정보 및 마지막 상태를 저장합니다.
CREATE TABLE IF NOT EXISTS vehicle_info (
                                            mdn VARCHAR(20) PRIMARY KEY,                             -- 차량 번호 (단말 식별 key)
    company_id BIGINT NOT NULL,                              -- 소속 회사 ID (차량은 회사에 종속)
    name VARCHAR(100),                                       -- 차량명
    plate_no VARCHAR(20) UNIQUE NOT NULL,                    -- 차량 번호판
    vin VARCHAR(50),                                         -- 차량 식별번호 (VIN)
    model VARCHAR(50),                                       -- 차량 모델
    year INT,                                                -- 연식
    fuel_type fuel_type_enum,                                -- 연료 유형 (ENUM)
    is_active BOOLEAN DEFAULT TRUE,                          -- 운행 여부
    last_latitude NUMERIC(9,6),                              -- 마지막 GPS 위도
    last_longitude NUMERIC(9,6),                             -- 마지막 GPS 경도
    total_accumulated_distance BIGINT DEFAULT 0,             -- 시동 OFF 시 최종 누적 주행 거리 (미터)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),       -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),       -- 레코드 최종 수정 일시
    FOREIGN KEY (company_id) REFERENCES company_info (company_id) ON DELETE RESTRICT
    );

COMMENT ON TABLE vehicle_info IS '차량 단말의 기본 정보 및 마지막 상태를 저장하는 테이블';
COMMENT ON COLUMN vehicle_info.mdn IS '차량 번호 (단말 식별 key)';
COMMENT ON COLUMN vehicle_info.company_id IS '소속 회사 ID';
COMMENT ON COLUMN vehicle_info.name IS '차량명';
COMMENT ON COLUMN vehicle_info.plate_no IS '차량 번호판';
COMMENT ON COLUMN vehicle_info.vin IS '차량 식별번호 (VIN)';
COMMENT ON COLUMN vehicle_info.model IS '차량 모델';
COMMENT ON COLUMN vehicle_info.year IS '연식';
COMMENT ON COLUMN vehicle_info.fuel_type IS '연료 유형 (ENUM)';
COMMENT ON COLUMN vehicle_info.is_active IS '운행 여부';
COMMENT ON COLUMN vehicle_info.last_latitude IS '시동 OFF 시 마지막 GPS 위도 (미수신 시 직전값)';
COMMENT ON COLUMN vehicle_info.last_longitude IS '시동 OFF 시 마지막 GPS 경도 (미수신 시 직전값)';
COMMENT ON COLUMN vehicle_info.total_accumulated_distance IS '최종 누적 주행 거리 (미터, 시동 OFF 시 최종 거리 업데이트)';
COMMENT ON COLUMN vehicle_info.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN vehicle_info.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_vehicle_info_updated_at
    BEFORE UPDATE ON vehicle_info
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- 1-1. 차량-지오펜스 정책 할당 테이블 (vehicle_policy_assignment)
-- 각 차량은 하나의 지오펜스 정책에 1:1로 매핑됩니다.
CREATE TABLE IF NOT EXISTS vehicle_policy_assignment (
                                                         mdn VARCHAR(20) PRIMARY KEY,                             -- 차량 번호 (PK이자 vehicle_info의 FK, 1:1 매핑 강제)
    policy_id BIGINT NOT NULL,                               -- 할당된 지오펜스 정책 ID
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),      -- 정책 할당 일시
    FOREIGN KEY (mdn) REFERENCES vehicle_info (mdn) ON DELETE CASCADE,
    FOREIGN KEY (policy_id) REFERENCES geofence_policy (policy_id) ON DELETE RESTRICT
    );

COMMENT ON TABLE vehicle_policy_assignment IS '각 차량이 어떤 지오펜스 정책을 적용받는지 1:1 매핑하는 테이블';
COMMENT ON COLUMN vehicle_policy_assignment.mdn IS '차량 번호 (PK이자 vehicle_info의 FK, 1:1 매핑 강제)';
COMMENT ON COLUMN vehicle_policy_assignment.policy_id IS '할당된 지오펜스 정책 ID';
COMMENT ON COLUMN vehicle_policy_assignment.assigned_at IS '정책 할당 일시';


-- 1-2. 차량 운전자 매핑 테이블 (vehicle_driver_map)
-- 특정 차량의 주 운전자를 지정하거나, 특정 기간 동안 차량을 운전한 운전자를 기록합니다.
CREATE TABLE IF NOT EXISTS vehicle_driver_map (
                                                  map_id BIGSERIAL PRIMARY KEY,                        -- 매핑 고유 ID
                                                  mdn VARCHAR(20) NOT NULL,                            -- 차량 번호
    employee_id BIGINT NOT NULL,                         -- 운전자 직원 ID
    is_primary_driver BOOLEAN DEFAULT FALSE,             -- 해당 차량의 주 운전자 여부
    assigned_start_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(), -- 할당 시작 일시
    assigned_end_at TIMESTAMP WITH TIME ZONE,            -- 할당 종료 일시 (NULL이면 현재 유효)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    UNIQUE (mdn, employee_id, assigned_start_at),        -- 동일 차량-운전자-시작 시간 조합 중복 불가
    FOREIGN KEY (mdn) REFERENCES vehicle_info (mdn) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employee_info (employee_id) ON DELETE RESTRICT
    );

COMMENT ON TABLE vehicle_driver_map IS '특정 차량의 주 운전자를 지정하거나, 특정 기간 동안 차량을 운전한 운전자를 기록하는 테이블';
COMMENT ON COLUMN vehicle_driver_map.map_id IS '매핑 고유 ID';
COMMENT ON COLUMN vehicle_driver_map.mdn IS '차량 번호';
COMMENT ON COLUMN vehicle_driver_map.employee_id IS '운전자 직원 ID';
COMMENT ON COLUMN vehicle_driver_map.is_primary_driver IS '해당 차량의 주 운전자 여부';
COMMENT ON COLUMN vehicle_driver_map.assigned_start_at IS '할당 시작 일시';
COMMENT ON COLUMN vehicle_driver_map.assigned_end_at IS '할당 종료 일시 (NULL이면 현재 유효)';
COMMENT ON COLUMN vehicle_driver_map.created_at IS '레코드 생성 일시';


-- 1-3. 단말 정보 테이블 (device)
-- 차량에 연결된 단말의 고유 정보 및 설정 값을 저장합니다.
CREATE TABLE IF NOT EXISTS device (
                                        id BIGSERIAL PRIMARY KEY,                            -- 단말 고유 ID
                                        vehicle_id BIGINT REFERENCES vehicle_info(company_id) UNIQUE, -- 연결된 차량 (FK to vehicle_info.mdn)
    mdn VARCHAR(20) UNIQUE NOT NULL,                     -- 단말 전화번호 (식별자)
    terminal_id VARCHAR(10) NOT NULL DEFAULT 'A001',     -- 터미널 ID
    manufacturer_id VARCHAR(10) NOT NULL DEFAULT '6',    -- 제조사 ID
    packet_version VARCHAR(5) NOT NULL DEFAULT '5',      -- 패킷 버전
    device_type VARCHAR(5) NOT NULL DEFAULT '1',         -- 장비 유형
    firmware_version VARCHAR(20),                        -- 펌웨어 버전
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()    -- 레코드 최종 수정 일시
    );

COMMENT ON TABLE device IS '차량에 연결된 단말의 고유 정보 및 설정 값을 저장하는 테이블';
COMMENT ON COLUMN device.id IS '단말 고유 ID';
COMMENT ON COLUMN device.vehicle_id IS '연결된 차량 ID (vehicle_info.mdn 참조)';
COMMENT ON COLUMN device.mdn IS '단말 전화번호 (식별자)';
COMMENT ON COLUMN device.terminal_id IS '터미널 ID (규격서 A001 고정)';
COMMENT ON COLUMN device.manufacturer_id IS '제조사 ID (규격서 6 고정)';
COMMENT ON COLUMN device.packet_version IS '패킷 버전 (규격서 5 고정)';
COMMENT ON COLUMN device.device_type IS '장비 유형 (규격서 1 고정)';
COMMENT ON COLUMN device.firmware_version IS '펌웨어 버전';
COMMENT ON COLUMN device.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN device.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_device_updated_at
    BEFORE UPDATE ON device
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- 1-4. 단말 인증 토큰 테이블 (device_auth_token)
-- 단말의 현재 유효한 인증 토큰 정보를 저장합니다. (mdn당 1개)
CREATE TABLE IF NOT EXISTS device_auth_token (
                                                 mdn VARCHAR(20) PRIMARY KEY,                             -- 차량 번호 (단말 식별 key)
    token_value TEXT NOT NULL,                               -- 인증 토큰
    expiration_period_hours INTEGER NOT NULL,                -- 만료 기간 (시간 단위)
    issued_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),        -- 발급 시각
    expires_at TIMESTAMP WITH TIME ZONE GENERATED ALWAYS AS (issued_at + (expiration_period_hours || ' hours')::interval) STORED, -- 만료 시각 (계산 필드)
    result_code VARCHAR(10) NOT NULL,                        -- 결과 코드
    result_message VARCHAR(100),                             -- 결과 메시지
    response_data JSONB,                                     -- 서버가 전송한 원본 응답 데이터 (JSON)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),       -- 레코드 생성 일시
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),       -- 레코드 최종 수정 일시
    FOREIGN KEY (mdn) REFERENCES device (mdn) ON DELETE CASCADE -- device.mdn 참조
    );

COMMENT ON TABLE device_auth_token IS '단말의 현재 유효한 인증 토큰 정보를 저장하는 테이블 (mdn당 1개)';
COMMENT ON COLUMN device_auth_token.mdn IS '단말 식별자 (device.mdn 참조)';
COMMENT ON COLUMN device_auth_token.token_value IS '인증 토큰';
COMMENT ON COLUMN device_auth_token.expiration_period_hours IS '만료 기간 (시간 단위)';
COMMENT ON COLUMN device_auth_token.issued_at IS '발급 시각';
COMMENT ON COLUMN device_auth_token.expires_at IS '만료 시각 (issued_at + expiration_period_hours)';
COMMENT ON COLUMN device_auth_token.result_code IS '결과 코드 (rstCd)';
COMMENT ON COLUMN device_auth_token.result_message IS '결과 메시지 (rstMsg)';
COMMENT ON COLUMN device_auth_token.response_data IS '서버가 전송한 원본 응답 데이터 (JSONB)';
COMMENT ON COLUMN device_auth_token.created_at IS '레코드 생성 일시';
COMMENT ON COLUMN device_auth_token.updated_at IS '레코드 최종 수정 일시';

CREATE TRIGGER update_device_auth_token_updated_at
    BEFORE UPDATE ON device_auth_token
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


CREATE TYPE vehicle_event_type_enum AS ENUM ('car_start', 'car_stop', 'geofence', 'periodic_report', 'ignition_on', 'ignition_off', 'sos', 'battery_low');
CREATE TYPE gps_condition_enum AS ENUM ('A', 'V', '0', 'P'); -- A:정상, V:비정상, 0:미장착, P:시동 ON/OFF 시 GPS 비정상

-- 2. 차량 이벤트 로그 테이블 (vehicle_event_log) - TimescaleDB 하이퍼테이블
-- 시동 ON/OFF, 지오펜싱 등 모든 차량 이벤트를 시계열 데이터로 저장합니다.
CREATE TABLE IF NOT EXISTS vehicle_event_log (
     event_timestamp_utc TIMESTAMP WITH TIME ZONE NOT NULL,   -- 이벤트 발생 UTC 시간 (TimescaleDB 시계열 키)
     mdn VARCHAR(20) NOT NULL,                                -- 단말 식별자 (device.mdn 참조)
    event_type vehicle_event_type_enum NOT NULL,             -- 이벤트 타입 (ENUM 사용)
    event_second INTEGER,                                    -- 발생시간 중 '초' (0-59)
    gps_status gps_condition_enum,                           -- GPS 상태 (ENUM 사용)
    latitude NUMERIC(9,6),                                   -- GPS 위도
    longitude NUMERIC(9,6),                                  -- GPS 경도
    angle INTEGER,                                           -- 방향 (0-365도)
    speed INTEGER,                                           -- 속도 (km/h, 0-255)
    current_accumulated_distance BIGINT,                     -- 현재 시점의 누적 주행 거리 (km)
    battery_volt INTEGER,                                    -- 배터리 전압 (실제값X10, V)
    on_time TIMESTAMP WITH TIME ZONE,                        -- 시동 ON 이벤트 발생 시 시간
    ignition_off_time TIMESTAMP WITH TIME ZONE,              -- 시동 OFF 이벤트 발생 시 시간
    geofence_group_id VARCHAR(20),                           -- 지오펜스 그룹 아이디 (연동규격서 필드 유지)
    geofence_point_id VARCHAR(20),                           -- 지오펜스 아이디 (연동규격서 필드 유지)
    event_value VARCHAR(10),                                 -- 이벤트 값 (1:진입, 2:이탈 - 지오펜스 이벤트 시)
    raw_json_data JSONB,                                     -- 원본 JSON 전문 (스키마 변경 시 유연성 확보)
    PRIMARY KEY (event_timestamp_utc, mdn),                  -- 복합 기본 키 (하이퍼테이블용)

    );

COMMENT ON TABLE vehicle_event_log IS '차량의 주기 정보, 시동 ON/OFF, 지오펜스 등 모든 이벤트를 시계열 데이터로 저장하는 테이블';
COMMENT ON COLUMN vehicle_event_log.event_timestamp_utc IS '이벤트 발생 UTC 시간 (TimescaleDB 하이퍼테이블의 시계열 키)';
COMMENT ON COLUMN vehicle_event_log.mdn IS '단말 식별자 (device.mdn 참조)';
COMMENT ON COLUMN vehicle_event_log.event_type IS '이벤트 타입 (예: ''PERIODIC_REPORT'', ''IGNITION_ON'', ''IGNITION_OFF'', ''GEOFENCE'')';
COMMENT ON COLUMN vehicle_event_log.event_second IS '이벤트 발생 시간 중 ''초'' (0-59)';
COMMENT ON COLUMN vehicle_event_log.gps_status IS 'GPS 상태 (A:정상, V:비정상, 0:미장착, P:시동 ON/OFF 시 GPS 비정상)';
COMMENT ON COLUMN vehicle_event_log.latitude IS 'GPS 위도';
COMMENT ON COLUMN vehicle_event_log.longitude IS 'GPS 경도';
COMMENT ON COLUMN vehicle_event_log.angle IS '차량 방향 (0-365도)';
COMMENT ON COLUMN vehicle_event_log.speed IS '차량 속도 (km/h, 0-255)';
COMMENT ON COLUMN vehicle_event_log.current_accumulated_distance IS '현재 시점의 누적 주행 거리 (미터, 0-9999999)';
COMMENT ON COLUMN vehicle_event_log.battery_volt IS '배터리 전압 (실제값X10, V)';
COMMENT ON COLUMN vehicle_event_log.on_time IS '시동 ON 이벤트 발생 시 시간';
COMMENT ON COLUMN vehicle_event_log.ignition_off_time IS '시동 OFF 이벤트 발생 시 시간';
COMMENT ON COLUMN vehicle_event_log.geofence_group_id IS '지오펜스 그룹 아이디 (연동규격서 필드 유지)';
COMMENT ON COLUMN vehicle_event_log.geofence_point_id IS '지오펜스 아이디 (연동규격서 필드 유지)';
COMMENT ON COLUMN vehicle_event_log.event_value IS '이벤트 값 (1:진입, 2:이탈 - 지오펜스 이벤트 시)';
COMMENT ON COLUMN vehicle_event_log.raw_json_data IS '원본 JSON 전문 (향후 분석 및 유연성 확보용)';

-- 'vehicle_event_log' 테이블을 하이퍼테이블로 변환 (event_timestamp_utc 기준, 1일 간격 청크)
SELECT create_hypertable('vehicle_event_log', 'event_timestamp_utc', chunk_time_interval => INTERVAL '1 day');
-- mdn + event_timestamp_utc 역순 인덱스 생성 (차량별 최신 이벤트 조회 효율화)
CREATE INDEX IF NOT EXISTS idx_vehicle_event_log_mdn_time ON vehicle_event_log (mdn, event_timestamp_utc DESC);


-- 2-1. 60초 주기 주행기록 테이블 (driving_log) - TimescaleDB 하이퍼테이블
-- 단말로부터 60초 주기마다 수신되는 주행 기록을 저장합니다.
CREATE TABLE IF NOT EXISTS driving_log (
    record_time TIMESTAMP WITH TIME ZONE NOT NULL,   -- 기록 시간 (초 단위, TimescaleDB 시계열 키)
    mdn VARCHAR(20) NOT NULL,                        -- 단말 식별자 (device.mdn 참조)
    gps_condition gps_condition_enum NOT NULL,       -- GPS 상태 (ENUM 사용)
    latitude NUMERIC(9,6),                           -- 위도
    longitude NUMERIC(9,6),                          -- 경도
    angle INTEGER,                                   -- 방향 (0-365도)
    speed INTEGER,                                   -- 속도 (km/h)
    total_distance BIGINT,                           -- 누적 거리 (km)
    battery_volt INTEGER,                            -- 배터리 전압 (실제값X10)
    PRIMARY KEY (record_time, mdn)                   -- 복합 기본 키 (하이퍼테이블용)
);

COMMENT ON TABLE driving_log IS '단말로부터 60초 주기마다 수신되는 주행 기록을 저장하는 테이블';
COMMENT ON COLUMN driving_log.record_time IS '기록 시간 (초 단위, TimescaleDB 하이퍼테이블의 시계열 키)';
COMMENT ON COLUMN driving_log.mdn IS '단말 식별자 (device.mdn 참조)';
COMMENT ON COLUMN driving_log.gps_condition IS 'GPS 상태 (A, V, 0, P)';
COMMENT ON COLUMN driving_log.latitude IS '위도';
COMMENT ON COLUMN driving_log.longitude IS '경도';
COMMENT ON COLUMN driving_log.angle IS '방향';
COMMENT ON COLUMN driving_log.speed IS '속도';
COMMENT ON COLUMN driving_log.total_distance IS '누적 거리';
COMMENT ON COLUMN driving_log.battery_volt IS '배터리 전압 (실제값X10)';

-- 'driving_log' 테이블을 하이퍼테이블로 변환 (record_time 기준, 1일 간격 청크)
SELECT create_hypertable('driving_log', 'record_time', chunk_time_interval => INTERVAL '1 day');
-- mdn + record_time 역순 인덱스 생성 (단말별 최신 기록 조회 효율화)
CREATE INDEX IF NOT EXISTS idx_driving_log_mdn_time ON driving_log (mdn, record_time DESC);


-- 3. 원본 요청 로그 테이블 (raw_request_log)
-- 단말로부터 수신된 원본 HTTP 요청 전문을 저장합니다.
CREATE TABLE IF NOT EXISTS raw_request_log (
                                               id BIGSERIAL PRIMARY KEY,                        -- 로그 ID
                                               mdn VARCHAR(20) REFERENCES device(mdn) ON DELETE SET NULL, -- 단말 번호 (NULL 허용, 단말 정보 없어도 로그 기록)
    service_method VARCHAR(50),                      -- 요청된 서비스 메서드 이름 (예: 'getToken', 'getSetInfo', 'sendRmInfo')
    received_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(), -- 수신 시각
    raw_body JSONB NOT NULL,                         -- 원본 요청 JSON
    processed BOOLEAN DEFAULT FALSE,                 -- 처리 여부
    process_result TEXT,                             -- 처리 결과 메시지
    headers JSONB,                                   -- HTTP 헤더 정보
    source_ip INET,                                  -- 요청 보낸 IP
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() -- 레코드 생성 일시 (received_at과 동일하게 사용)
    );

COMMENT ON TABLE raw_request_log IS '단말로부터 수신된 원본 HTTP 요청 전문을 저장하는 테이블';
COMMENT ON COLUMN raw_request_log.id IS '로그 ID';
COMMENT ON COLUMN raw_request_log.mdn IS '단말 번호 (device.mdn 참조, 단말 정보가 없어도 로그 기록 가능)';
COMMENT ON COLUMN raw_request_log.service_method IS '요청된 서비스 메서드 이름';
COMMENT ON COLUMN raw_request_log.received_at IS '수신 시각';
COMMENT ON COLUMN raw_request_log.raw_body IS '원본 요청 JSON';
COMMENT ON COLUMN raw_request_log.processed IS '처리 여부';
COMMENT ON COLUMN raw_request_log.process_result IS '처리 결과 메시지';
COMMENT ON COLUMN raw_request_log.headers IS 'HTTP 헤더 정보';
COMMENT ON COLUMN raw_request_log.source_ip IS '요청 보낸 IP';
COMMENT ON COLUMN raw_request_log.created_at IS '레코드 생성 일시';


-- 4. 단말 설정 요청 로그 테이블 (device_config_request_log)
-- 단말이 서버로부터 제어 및 지오펜싱 설정 정보를 요청한 이력을 저장합니다.
CREATE TABLE IF NOT EXISTS device_config_request_log (
                                                         request_id BIGSERIAL PRIMARY KEY,                        -- 고유 요청 ID
                                                         mdn VARCHAR(20) NOT NULL,                                -- 차량 번호 (device.mdn 참조)
    http_header_timestamp VARCHAR(20),                       -- HTTP Header Timestamp (API 로그 분석 시 수신 시간 체크용)
    transaction_unique_id VARCHAR(36),                       -- Transaction Unique ID (HTTP Header TUID)
    key_version VARCHAR(10),                                 -- Aria 128 암복호화 Key Version (HTTP Header Key-Version)
    request_data JSONB,                                      -- 단말이 보낸 원본 요청 데이터 (JSON)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),       -- 레코드 생성 일시
    FOREIGN KEY (mdn) REFERENCES device (mdn) ON DELETE CASCADE
    );

COMMENT ON TABLE device_config_request_log IS '단말이 서버로부터 제어 및 지오펜싱 설정 정보를 요청한 이력을 저장하는 테이블';
COMMENT ON COLUMN device_config_request_log.request_id IS '고유 요청 ID';
COMMENT ON COLUMN device_config_request_log.mdn IS '단말 식별자 (device.mdn 참조)';
COMMENT ON COLUMN device_config_request_log.http_header_timestamp IS 'HTTP Header의 Timestamp (API 로그 분석 시 수신 시간 체크용)';
COMMENT ON COLUMN device_config_request_log.transaction_unique_id IS 'HTTP Header의 TUID (Transaction Unique ID)';
COMMENT ON COLUMN device_config_request_log.key_version IS 'HTTP Header의 Key-Version (Aria 128 암복호화 Key Version)';
COMMENT ON COLUMN device_config_request_log.request_data IS '단말이 보낸 원본 요청 데이터 (JSONB)';
COMMENT ON COLUMN device_config_request_log.created_at IS '레코드 생성 일시';


-- 5. 단말 설정 응답 로그 테이블 (device_config_response_log)
-- 서버가 단말에게 전송한 제어 및 지오펜싱 설정 응답 정보를 저장합니다.
CREATE TABLE IF NOT EXISTS device_config_response_log (
                                                          response_id BIGSERIAL PRIMARY KEY,                       -- 고유 응답 ID
                                                          mdn VARCHAR(20) NOT NULL,                                -- 차량 번호 (device.mdn 참조)
    result_code VARCHAR(10) NOT NULL,                        -- 결과 코드
    result_message VARCHAR(100),                             -- 결과 메시지
    ignition_on_event_time TIMESTAMP WITH TIME ZONE,         -- 발생일시 (차량 시동 On 시간)
    control_command_count INTEGER,                           -- 제어명령 개수
    geofence_setting_count INTEGER,                          -- 지오펜싱 설정 개수
    response_data JSONB,                                     -- 서버가 전송한 원본 응답 데이터 (JSON)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),       -- 레코드 생성 일시
    FOREIGN KEY (mdn) REFERENCES device (mdn) ON DELETE CASCADE
    );

COMMENT ON TABLE device_config_response_log IS '서버가 단말에게 전송한 제어 및 지오펜싱 설정 응답 정보를 저장하는 테이블';
COMMENT ON COLUMN device_config_response_log.response_id IS '고유 응답 ID';
COMMENT ON COLUMN device_config_response_log.mdn IS '단말 식별자 (device.mdn 참조)';
COMMENT ON COLUMN device_config_response_log.result_code IS '결과 코드 (rstCd)';
COMMENT ON COLUMN device_config_response_log.result_message IS '결과 메시지 (rstMsg)';
COMMENT ON COLUMN device_config_response_log.ignition_on_event_time IS '발생일시 (차량 시동 On 시간)';
COMMENT ON COLUMN device_config_response_log.control_command_count IS '제어명령 개수 (ctrCnt)';
COMMENT ON COLUMN device_config_response_log.geofence_setting_count IS '지오펜싱 설정 개수 (geoCnt)';
COMMENT ON COLUMN device_config_response_log.response_data IS '서버가 전송한 원본 응답 데이터 (JSONB)';
COMMENT ON COLUMN device_config_response_log.created_at IS '레코드 생성 일시';


-- 6. 단말 제어 명령 상세 로그 테이블 (device_control_command_log)
-- '단말 설정 응답 로그' 테이블의 개별 제어 명령 상세를 저장합니다. (1:N 관계)
CREATE TABLE IF NOT EXISTS device_control_command_log (
                                                          command_id BIGSERIAL PRIMARY KEY,                        -- 고유 명령 ID
                                                          config_response_id BIGINT NOT NULL,                      -- device_config_response_log 테이블의 외래 키
                                                          control_item_id VARCHAR(20),                             -- 제어 아이디 (ctrId)
    control_code VARCHAR(10),                                -- 제어 코드 (ctrCd: '05': RM전송주기, '09': 지오펜싱 정보 주기 등)
    control_value VARCHAR(100),                              -- 제어 값 (ctrVal: 예: 주기정보 전송 주기값)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),       -- 레코드 생성 일시
    FOREIGN KEY (config_response_id) REFERENCES device_config_response_log (response_id) ON DELETE CASCADE
    );

COMMENT ON TABLE device_control_command_log IS '개별 제어 명령 상세를 저장하는 테이블 (device_config_response_log와 1:N 관계)';
COMMENT ON COLUMN device_control_command_log.command_id IS '고유 명령 ID';
COMMENT ON COLUMN device_control_command_log.config_response_id IS 'device_config_response_log 테이블의 외래 키';
COMMENT ON COLUMN device_control_command_log.control_item_id IS '제어 아이디 (ctrId)';
COMMENT ON COLUMN device_control_command_log.control_code IS '제어 코드 (ctrCd: ''05'': RM전송주기, ''09'': 지오펜싱 정보 주기 등)';
COMMENT ON COLUMN device_control_command_log.control_value IS '제어 값 (ctrVal: 예: 주기정보 전송 주기값)';
COMMENT ON COLUMN device_control_command_log.created_at IS '레코드 생성 일시';


-- 7. 단말 지오펜스 설정 상세 로그 테이블 (device_geofence_config_log)
-- '단말 설정 응답 로그' 테이블의 개별 지오펜스 설정 상세를 저장합니다. (1:N 관계)
CREATE TABLE IF NOT EXISTS device_geofence_config_log (
                                                          setting_id BIGSERIAL PRIMARY KEY,                        -- 고유 설정 ID
                                                          config_response_id BIGINT NOT NULL,                      -- device_config_response_log 테이블의 외래 키
                                                          geofence_control_id VARCHAR(20),                         -- 지오펜스 아이디 (geoCtrId)
    update_value VARCHAR(5),                                 -- 전체 업데이트 상태값 (upVal: '0':전체 업데이트 미설정, '1':전체 업데이트 설정)
    geofence_group_id VARCHAR(20),                           -- 지오펜스 그룹 아이디 (geoGrpId)
    geofence_event_type VARCHAR(5),                          -- 이벤트 타입 (geoEvtTp: '1':진입만, '2':이탈만, '3':모두)
    geofence_radius_meters VARCHAR(10),                      -- 지오펜스 반경 (geoRange)
    latitude NUMERIC(9,6),                                   -- 위도
    longitude NUMERIC(9,6),                                  -- 경도
    start_time TIMESTAMP WITH TIME ZONE,                     -- 시작 시간 (onTime)
    end_time TIMESTAMP WITH TIME ZONE,                       -- 종료 시간 (offTime)
    store_type geofence_store_type_enum,                     -- 저장 타입 (ENUM 사용)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),       -- 레코드 생성 일시
    FOREIGN KEY (config_response_id) REFERENCES device_config_response_log (response_id) ON DELETE CASCADE
    );

COMMENT ON TABLE device_geofence_config_log IS '개별 지오펜스 설정 상세를 저장하는 테이블 (device_config_response_log와 1:N 관계)';
COMMENT ON COLUMN device_geofence_config_log.setting_id IS '고유 설정 ID';
COMMENT ON COLUMN device_geofence_config_log.config_response_id IS 'device_config_response_log 테이블의 외래 키';
COMMENT ON COLUMN device_geofence_config_log.geofence_control_id IS '지오펜스 아이디 (geoCtrId)';
COMMENT ON COLUMN device_geofence_config_log.update_value IS '전체 업데이트 상태값 (upVal: ''0'':미설정, ''1'':설정)';
COMMENT ON COLUMN device_geofence_config_log.geofence_group_id IS '지오펜스 그룹 아이디 (geoGrpId)';
COMMENT ON COLUMN device_geofence_config_log.geofence_event_type IS '이벤트 타입 (geoEvtTp: ''1'':진입만, ''2'':이탈만, ''3'':모두)';
COMMENT ON COLUMN device_geofence_config_log.geofence_radius_meters IS '지오펜스 반경 (geoRange)';
COMMENT ON COLUMN device_geofence_config_log.latitude IS '위도';
COMMENT ON COLUMN device_geofence_config_log.longitude IS '경도';
COMMENT ON COLUMN device_geofence_config_log.start_time IS '시작 시간 (onTime)';
COMMENT ON COLUMN device_geofence_config_log.end_time IS '종료 시간 (offTime)';
COMMENT ON COLUMN device_geofence_config_log.store_type IS '저장 타입 (storeTp: ''1'':추가, ''2'':삭제)';
COMMENT ON COLUMN device_geofence_config_log.created_at IS '레코드 생성 일시';


-- 8. 단말 토큰 요청 이력 테이블 (device_token_request_log)
-- 단말이 인증 토큰을 서버로 요청한 이력을 저장합니다.
CREATE TABLE IF NOT EXISTS device_token_request_log (
                                                        request_id BIGSERIAL PRIMARY KEY,                        -- 고유 요청 ID
                                                        mdn VARCHAR(20) NOT NULL,                                -- 단말 식별자 (device.mdn 참조)
    http_header_timestamp VARCHAR(20),                       -- HTTP Header Timestamp (API 로그 분석 시 수신 시간 체크용)
    transaction_unique_id VARCHAR(36),                       -- Transaction Unique ID (HTTP Header TUID)
    key_version VARCHAR(10),                                 -- Aria 128 암복호화 Key Version (HTTP Header Key-Version)
    request_data JSONB,                                      -- 단말이 보낸 원본 요청 데이터 (JSON)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),       -- 레코드 생성 일시
    FOREIGN KEY (mdn) REFERENCES device (mdn) ON DELETE CASCADE
    );

COMMENT ON TABLE device_token_request_log IS '단말이 인증 토큰을 서버로 요청한 이력을 저장하는 테이블';
COMMENT ON COLUMN device_token_request_log.request_id IS '고유 요청 ID';
COMMENT ON COLUMN device_token_request_log.mdn IS '단말 식별자 (device.mdn 참조)';
COMMENT ON COLUMN device_token_request_log.http_header_timestamp IS 'HTTP Header의 Timestamp (API 로그 분석 시 수신 시간 체크용)';
COMMENT ON COLUMN device_token_request_log.transaction_unique_id IS 'HTTP Header의 TUID (Transaction Unique ID)';
COMMENT ON COLUMN device_token_request_log.key_version IS 'HTTP Header의 Key-Version (Aria 128 암복호화 Key Version)';
COMMENT ON COLUMN device_token_request_log.request_data IS '단말이 보낸 원본 요청 데이터 (JSONB)';
COMMENT ON COLUMN device_token_request_log.created_at IS '레코드 생성 일시';


-- 9. 정비 이력 테이블 (maintenance_log)
-- 차량의 정비 이력을 기록합니다.
CREATE TABLE IF NOT EXISTS maintenance_log (
                                               id BIGSERIAL PRIMARY KEY,                            -- 정비 이력 ID
                                               mdn VARCHAR(20) NOT NULL,                            -- 단말 식별자 (device.mdn 참조)
    log_date DATE NOT NULL,                              -- 정비 일자
    description TEXT NOT NULL,                           -- 정비 내용
    cost INTEGER,                                        -- 비용
    mileage_at_maintenance BIGINT,                       -- 정비 시 주행 거리
    created_by BIGINT REFERENCES employee_info(employee_id) ON DELETE RESTRICT, -- 작성자 (employee_info.employee_id 참조)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),   -- 작성 시간
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),    -- 최종 수정 일시
    FOREIGN KEY (mdn) REFERENCES device (mdn) ON DELETE CASCADE -- vehicle_id 대신 device.mdn 참조
    );

COMMENT ON TABLE maintenance_log IS '차량의 정비 이력을 기록하는 테이블';
COMMENT ON COLUMN maintenance_log.id IS '정비 이력 ID';