package cats.net.client.event;

import cats.net.client.AbstractClient;

public class ClientStateAdapter<T extends AbstractClient> implements ClientStateListener<T> {

    public void onStart(final T client){}

    public void onFinish(final T client){}
}
