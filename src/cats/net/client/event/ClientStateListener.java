package cats.net.client.event;

import cats.net.client.AbstractClient;
import cats.net.core.connection.spot.event.SpotStateListener;

public interface ClientStateListener<T extends AbstractClient> extends SpotStateListener<T> {
}
