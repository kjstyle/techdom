package kjstyle.techdom.common;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * MockMvc를 사용하는 테스트를 위한 기본 설정을 제공하는 추상 클래스
 * 모든 MockMvc 테스트 클래스들은 이 클래스를 상속받아 사용합니다.
 */
@AutoConfigureMockMvc // MockMvc를 자동으로 설정해주는 어노테이션입니다. JUnit5에서 MockMvc 주입을 위해 필요합니다.
public abstract class BaseMockMvcTest extends BaseTest {
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    public void setup() {
        // 각 테스트 실행 전에 MockMvc를 초기화합니다.
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // UTF-8 인코딩 필터를 추가하여 한글 깨짐을 방지합니다.
                // .alwaysExpect(MockMvcResultMatchers.status().isOk()) // HTTP 상태코드 200 이외의 응답도 테스트해야 하므로 주석 처리했습니다.
                .alwaysDo(print()) // 모든 테스트의 요청/응답 내용을 콘솔에 출력합니다.
                .build();
    }
}