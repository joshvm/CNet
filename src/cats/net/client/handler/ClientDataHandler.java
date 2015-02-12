package cats.net.client.handler;

import cats.net.client.AbstractClient;
import cats.net.core.data.Data;
import cats.net.core.data.handler.AbstractDataHandler;
import cats.net.core.utils.CoreUtils;

public abstract class ClientDataHandler<T extends AbstractClient> extends AbstractDataHandler<T>{

    public abstract void handle(final T client, final Data data);

    public void handleException(final T client, final Data data, final Exception ex){
        CoreUtils.print(ex);
    }
}
