package cats.net.server.event;

import cats.net.core.connection.spot.event.SpotStateListener;
import cats.net.server.AbstractServer;

public interface ServerStateListener<T extends AbstractServer> extends SpotStateListener<T> {
}
