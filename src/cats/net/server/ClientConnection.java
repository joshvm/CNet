package cats.net.server;

import cats.net.core.connection.AbstractConnection;

public abstract class ClientConnection extends AbstractConnection<AbstractServer>{

    ClientConnection(final AbstractServer server){
        super(server);
    }

    protected abstract void read() throws Exception;
}
