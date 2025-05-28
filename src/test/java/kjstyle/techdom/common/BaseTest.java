package kjstyle.techdom.common;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 모든 테스트 클래스의 기본이 되는 추상 클래스입니다.
 * Spring Boot 테스트에 필요한 기본 설정을 포함하고 있습니다.
 *
 * @ExtendWith(SpringExtension.class) - JUnit5에서 Spring 테스트 기능을 사용하기 위한 확장 설정
 * @SpringBootTest - 통합 테스트를 위한 스프링 부트 테스트 환경 제공
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public abstract class BaseTest {

}