package authorization;

public interface AuthService {
    void connect ();
    void disconnect ();
    String getNickByLoginPass ( String login , String pass );
}