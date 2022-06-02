package woowacourse.shoppingcart.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static woowacourse.auth.support.AuthorizationExtractor.AUTHORIZATION;
import static woowacourse.auth.support.AuthorizationExtractor.BEARER_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import woowacourse.auth.dto.TokenRequest;
import woowacourse.auth.dto.TokenResponse;
import woowacourse.shoppingcart.dto.CustomerResponse;
import woowacourse.shoppingcart.dto.SignUpRequest;

@DisplayName("회원 관련 기능")
public class CustomerAcceptanceTest extends AcceptanceTest {

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
    }

    @DisplayName("회원가입")
    @Test
    void addCustomer() {
        ExtractableResponse<Response> response = 회원_가입(
                회원_정보("example@example.com", "example123!", "http://gravatar.com/avatar/1?d=identicon",
                        "희봉", "male", "1998-08-07", "12345678910", "address", "detailAddress", "12345", true
                ));
        assertEquals(response.response().statusCode(), HttpStatus.CREATED.value());
    }

    @DisplayName("로그인")
    @Test
    void signIn() {
        회원_가입(회원_정보("example@example.com", "example123!", "http://gravatar.com/avatar/1?d=identicon",
                        "희봉", "male", "1998-08-07", "12345678910",
                "address", "detailAddress", "12345", true));

        TokenResponse response =
                로그인_후_토큰_발급(로그인_정보("example@example.com", "example123!"));

        assertAll(
                () -> assertThat(response.getAccessToken()).isNotNull(),
                () -> assertThat(response.getCustomerId()).isNotNull()
        );

    }


    @DisplayName("내 정보 조회")
    @Test
    void findMyProfile() {
        회원_가입(회원_정보("example@example.com", "example123!", "http://gravatar.com/avatar/1?d=identicon",
                "희봉", "male", "1998-08-07", "12345678910",
                "address", "detailAddress", "12345", true));

        TokenResponse signInResponse =
                로그인_후_토큰_발급(로그인_정보("example@example.com", "example123!"));

        ExtractableResponse<Response> response = 회원_조회(signInResponse.getAccessToken());
        final CustomerResponse customerResponse = response.body()
                .jsonPath()
                .getObject("", CustomerResponse.class);

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(customerResponse.getEmail()).isEqualTo("example@example.com")
        );
    }

    @Test
    void 사용자_정보_조회() {
        // given
        String account = "leo8842";
        String password = "leoLeo123!";

        String accessToken = 회원_가입_후_토큰_발급(account, password);

        // when
        ExtractableResponse<Response> response = 회원_조회(accessToken);

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(findValue(response, "account")).isEqualTo(account);
    }

    @DisplayName("내 정보 수정")
    @Test
    void updateMe() {
    }

    @DisplayName("회원탈퇴")
    @Test
    void deleteMe() {
    }

    private String findValue(ExtractableResponse<Response> response, String value) {
        return response.body().jsonPath().getString(value);
    }


    private ExtractableResponse<Response> createCustomer() throws JsonProcessingException {
        final SignUpRequest customerRequest = new SignUpRequest(
                "example@example.com", "example123!", "http://gravatar.com/avatar/1?d=identicon",
                "희봉", "male", "1998-08-07", "12345678910", "address", "detailAddress", "12345", true
        );
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(objectMapper.writeValueAsString(customerRequest))
                .post("/api/customers")
                .then().log().all()
                .extract();
    }

    private String 회원_가입_후_토큰_발급(String email, String password) {
        회원_가입(회원_정보("example@example.com", "example123!", "http://gravatar.com/avatar/1?d=identicon",
                "희봉", "male", "1998-08-07", "12345678910", "address", "detailAddress", "12345", true
        ));

        return findValue(로그인_후_토큰_발급(email, password), "accessToken");
    }

    private SignUpRequest 회원_정보(String email, String password, String profileImageUrl, String name, String gender,
                                String birthday, String contact, String address, String detailAddress,
                                String zoneCode,
                                boolean terms) {
        return new SignUpRequest(email, password, profileImageUrl, name, gender, birthday, contact, address,
                detailAddress, zoneCode, terms);
    }


    private ExtractableResponse<Response> 로그인_후_토큰_발급(String email, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);

        return RestAssured.given()
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/customer/authentication/sign-in")
                .then()
                .log().all()
                .extract();
    }

    private ExtractableResponse<Response> 회원_가입(SignUpRequest request) {
        return RestAssured.given()
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/customers")
                .then()
                .log().all()
                .extract();
    }

    private ExtractableResponse<Response> 회원_조회(String accessToken) {
        return RestAssured.given()
                .log().all()
                .header(AUTHORIZATION, BEARER_TYPE + accessToken)
                .when()
                .get("/api/customers")
                .then()
                .log().all()
                .extract();
    }

    private ExtractableResponse<Response> 토큰_발급(String email, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);

        return RestAssured.given()
                .log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/customer/authentication/sign-in")
                .then()
                .log().all()
                .extract();
    }

    private TokenRequest 로그인_정보(final String email, final String password) {
        return new TokenRequest(email, password);
    }

    private TokenResponse 로그인_후_토큰_발급(TokenRequest request) {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/api/customer/authentication/sign-in")
                .then().log().all()
                .extract();
        return response.body()
                .jsonPath()
                .getObject("", TokenResponse.class);
    }
}
