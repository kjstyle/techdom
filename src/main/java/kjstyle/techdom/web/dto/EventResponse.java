package kjstyle.techdom.web.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class EventResponse {

    // 결과 코드 (규격서: rstCd)
    private String rstCd;
    // 결과 메시지 (규격서: rstMsg)
    private String rstMsg;
    // 차량 번호 (규격서: mdn)
    private String mdn;

    // 유효성 검사 실패 시 상세 에러 메시지 목록 (추가)
    private List<String> errors;

    public EventResponse(String rstCd, String rstMsg, String mdn) {
        this.rstCd = rstCd;
        this.rstMsg = rstMsg;
        this.mdn = mdn;
    }

    public EventResponse(String rstCd, String rstMsg, String mdn, List<String> errors) {
        this.rstCd = rstCd;
        this.rstMsg = rstMsg;
        this.mdn = mdn;
        this.errors = errors;
    }
}