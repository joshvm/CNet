package cats.net.server.event;

import cats.net.server.AbstractServer;
import cats.net.server.ClientConnection;

public class ServerAdapter<T extends AbstractServer> implements ServerListener<T> {

    public void onJoin(final T server, final ClientConnection con){}

    public void onLeave(final T server, final ClientConnection con){}
}
