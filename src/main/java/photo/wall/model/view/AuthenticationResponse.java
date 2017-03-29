package photo.wall.model.view;

public class AuthenticationResponse {

    private final String token;

    public AuthenticationResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
