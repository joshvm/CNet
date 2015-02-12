package cats.net.server.event;

import cats.net.core.connection.spot.event.ConnectionSpotListener;
import cats.net.server.AbstractServer;
import cats.net.server.ClientConnection;

public interface ServerListener<T extends AbstractServer> extends ConnectionSpotListener<T> {

    public void onJoin(final T server, final ClientConnection connection);

    public void onLeave(final T server, final ClientConnection connection);
}
