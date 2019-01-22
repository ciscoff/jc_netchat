package authorization;

import java.util.ArrayList;

public class BaseAuthService implements AuthService {
    private class Entry {
        private String login ;
        private String pass ;
        private String nick ;
        public Entry ( String login , String pass , String nick ) {
            this . login = login ;
            this . pass = pass ;
            this . nick = nick ;
        }
    }
    private ArrayList < Entry > entries ;

    @Override
    public void connect () { }
    @Override
    public void disconnect () { }

    public BaseAuthService () {
        entries = new ArrayList<>();
        entries . add ( new Entry ( "login1" , "pass1" , "nick1" ));
        entries . add ( new Entry ( "login2" , "pass2" , "nick2" ));
        entries . add ( new Entry ( "login3" , "pass3" , "nick3" ));
    }
    @Override
    public String getNickByLoginPass ( String login , String pass ) {
        for ( Entry o : entries ) {
            if ( o . login . equals ( login ) && o . pass . equals ( pass )) return o . nick ;
        }
        return null ;
    }
}