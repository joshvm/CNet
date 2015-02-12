package cats.net.client.event;

import cats.net.client.AbstractClient;

public class ClientAdapter<T extends AbstractClient> implements ClientListener<T> {

    public void onConnect(final T client){}

    public void onDisconnect(final T client){}
}
