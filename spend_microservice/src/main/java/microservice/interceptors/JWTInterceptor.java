package microservice.interceptors;


import com.auth0.jwt.JWTVerifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import microservice.models.Message;


@Component
public class JWTInterceptor extends HandlerInterceptorAdapter {

    @Value("${jwt.issuer}")
    private String ISSUER;

    @Value("${jwt.secret}")
    private String SECRET;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, PATCH, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "6000");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");

        String requestMethod = request.getMethod();
        
        if (!requestMethod.equals("OPTIONS")) {
            String token = request.getHeader("Authorization");
            try {
                Algorithm algorithm = Algorithm.HMAC256(SECRET);
                JWTVerifier verifier = JWT.require(algorithm)
                                          .withIssuer(ISSUER)
                                          .build();

                DecodedJWT jwt = verifier.verify(token);
            }
            catch (JWTVerificationException e) {
                formatErrorResponse(response, "invalid token within the Authorization header");
                return false;
            }
            catch (NullPointerException e) {
                formatErrorResponse(response, "no Authorization header");
                return false;
            }
            
        }
        return super.preHandle(request, response, handler);
    }

    
    private void formatErrorResponse(HttpServletResponse response, String messageContent) throws IOException {
        Message errorMsg = new Message(messageContent, false);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(errorMsg.toJSONString());
        response.getWriter().flush();
        response.getWriter().close();
    }

}