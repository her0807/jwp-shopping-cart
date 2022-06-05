package woowacourse.auth.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static woowacourse.auth.support.AuthorizationExtractor.AUTHORIZATION;
import static woowacourse.auth.support.AuthorizationExtractor.BEARER_TYPE;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import woowacourse.auth.dto.TokenRequest;
import woowacourse.auth.dto.TokenResponse;
import woowacourse.shoppingcart.acceptance.AcceptanceTest;
import woowacourse.shoppingcart.dto.ModifiedCustomerRequest;
import woowacourse.shoppingcart.dto.SignUpRequest;

@DisplayName("인증 관련 기능")
public class AuthAcceptanceTest extends AcceptanceTest {
    @DisplayName("Bearer Auth 로그인 성공")
    @Test
    void myInfoWithBearerAuth() {

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

    private SignUpRequest 회원_정보(String email, String password, String profileImageUrl, String name, String gender,
                                String birthday, String contact, String address, String detailAddress,
                                String zoneCode,
                                boolean terms) {
        return new SignUpRequest(email, password, profileImageUrl, name, gender, birthday, contact, address,
                detailAddress, zoneCode, terms);
    }

    private ModifiedCustomerRequest 회원_수정_정보(String email, String password, String profileImageUrl, String name,
                                             String gender,
                                             String birthday, String contact, String address, String detailAddress,
                                             String zoneCode,
                                             boolean terms) {
        return new ModifiedCustomerRequest(email, password, profileImageUrl, name, gender, birthday, contact, address,
                detailAddress, zoneCode, terms);
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

    private ExtractableResponse<Response> 회원_조회(String accessToken, Long customerId) {
        return RestAssured.given()
                .log().all()
                .header(AUTHORIZATION, BEARER_TYPE + accessToken)
                .when()
                .get("/api/customers/" + customerId)
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
