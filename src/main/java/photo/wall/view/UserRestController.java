package photo.wall.view;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import photo.wall.config.security.jwt.JwtTokenHandler;

@RestController
public class UserRestController {

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private JwtTokenHandler jwtTokenHandler;

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public ResponseEntity<?> getAuthenticatedUser(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);

        if (!jwtTokenHandler.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT invalid");
        } else {
            return ResponseEntity.ok(jwtTokenHandler.parseToken(token));
        }
    }
}
