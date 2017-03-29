package photo.wall.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import photo.wall.config.security.jwt.JwtTokenHandler;
import photo.wall.config.security.user.AuthenticatedUser;
import photo.wall.config.security.user.AuthenticationInfoRepository;
import photo.wall.domain.AuthenticationService;
import photo.wall.model.view.AuthenticationRequest;
import photo.wall.model.view.AuthenticationResponse;

@RestController
public class UserAuthenticationController {

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private JwtTokenHandler jwtTokenHandler;

    @Autowired
    private AuthenticationInfoRepository authenticationInfoRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @RequestMapping(value = "${jwt.route.authentication.path}", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws AuthenticationException {
        // Authenticate User
        authenticationService.authenticateUser(authenticationRequest);

        // Generate User's JWT token
        AuthenticatedUser authenticatedUser = authenticationInfoRepository.loadUserByUsername(authenticationRequest.getUsername());
        String token = jwtTokenHandler.generateToken(authenticatedUser);

        // Return User's token
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }

    /*@RequestMapping(value = "${jwt.route.authentication.refresh}", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        String username = jwtTokenHandler.getUsernameFromToken(token);
        JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);


        // TODO: This will have to be fixed
        if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
            String refreshedToken = jwtTokenUtil.refreshToken(token);
            return ResponseEntity.ok(new JwtAuthenticationResponse(refreshedToken));
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }*/

}
