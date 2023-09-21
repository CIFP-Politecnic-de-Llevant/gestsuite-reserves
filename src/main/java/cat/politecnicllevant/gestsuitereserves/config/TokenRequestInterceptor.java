package cat.politecnicllevant.gestsuitereserves.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class TokenRequestInterceptor implements RequestInterceptor {
    private static final String AUTHORIZATION = "Authorization";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return;
        }
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader(AUTHORIZATION);
        if (token == null) {
            return;
        }
        requestTemplate.header(AUTHORIZATION, token);
    }
}
