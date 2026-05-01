package kaito.jlpt.ktjlpt.repository.httpclient;

import kaito.jlpt.ktjlpt.dto.response.ExchangeTokenResponse;
import kaito.jlpt.ktjlpt.dto.response.OutboundUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "outbound-user-client", url = "https://www.googleapis.com")
public interface OutboundUserClient {
    //https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=${accessToken}
    @GetMapping(value = "/oauth2/v1/userinfo")
    OutboundUserResponse getUserInfo(@RequestParam("alt") String alt, @RequestParam("access_token") String accessToken);
}
