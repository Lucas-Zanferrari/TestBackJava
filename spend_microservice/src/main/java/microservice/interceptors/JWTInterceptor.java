package microservice.interceptors;


import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import java.io.IOException;
import org.springframework.web.client.ResourceAccessException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import microservice.models.Message;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class JWTInterceptor extends HandlerInterceptorAdapter {

    @Value("${usr.auth.endpoint}")
    private String USR_AUTH_URL;

    @Value("${sys.auth.endpoint}")
    private String SYS_AUTH_URL;

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTInterceptor.class);


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, PATCH, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "6000");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String requestMethod = request.getMethod();
        String requestURI = request.getRequestURI();

        if (!requestMethod.equals("OPTIONS")) {
            String token = request.getHeader("Authorization");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            String url = requestURI.contains("/spends") && requestMethod.equals("POST") ? SYS_AUTH_URL : USR_AUTH_URL;
            try {
                ResponseEntity<Message> authResponse = restTemplate.exchange(url, HttpMethod.GET, entity, Message.class);
                Message msg = authResponse.getBody();

                if (!msg.getStatus().equals("success")) {
                    formatErrorResponse(response, msg, HttpStatus.UNAUTHORIZED.value());
                    LOGGER.error("[" + requestMethod + "] " + requestURI + " - " + msg.getContent());
                    return false;
                }

                LOGGER.info("[" + requestMethod + "] " + requestURI);
                request.setAttribute("userId", msg.getClientId());
            }
            catch (HttpClientErrorException e) {
                Message errorMsg;
                int status; 
                if (requestURI.contains("/error")) {
                    errorMsg = new Message("invalid request body. " + e.getMessage(), null, "failed");
                    status = HttpStatus.BAD_REQUEST.value();
                }
                else {
                    errorMsg = new Message("invalid access token. " + e.getMessage(), null, "failed");
                    status = HttpStatus.UNAUTHORIZED.value();
                }
                formatErrorResponse(response, errorMsg, status);
                LOGGER.error("[" + requestMethod + "] " + requestURI + " - " + errorMsg.getContent());
                return false;
            }
            catch (ResourceAccessException e) {
                Message errorMsg = new Message("authorization microservice is not available", null, "failed");
                formatErrorResponse(response, errorMsg, HttpStatus.UNAUTHORIZED.value());
                LOGGER.error("[" + requestMethod + "] " + requestURI + " - " + errorMsg.getContent());
                return false;
            }
        }
        return super.preHandle(request, response, handler);
    }

    
    private void formatErrorResponse(HttpServletResponse response, Message errorMsg, int status) throws IOException {
        response.setStatus(status);
        response.getWriter().write(errorMsg.toJSONString());
        response.getWriter().flush();
        response.getWriter().close();
    }

}
