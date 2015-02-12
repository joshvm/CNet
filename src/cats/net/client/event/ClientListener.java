package cats.net.client.event;

import cats.net.client.AbstractClient;
import cats.net.core.connection.spot.event.ConnectionSpotListener;

public interface ClientListener<T extends AbstractClient> extends ConnectionSpotListener<T> {

    public void onConnect(final T client);

    public void onDisconnect(final T client);
}
