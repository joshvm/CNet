package cats.net.server.event;

import cats.net.server.AbstractServer;

public class ServerStateAdapter<T extends AbstractServer> implements ServerStateListener<T> {

    public void onStart(final T server){}

    public void onFinish(final T server){}
}
