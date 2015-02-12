package cats.net.core.connection.spot.event;

import cats.net.core.connection.spot.AbstractConnectionSpot;

public interface SpotStateListener<T extends AbstractConnectionSpot> extends ConnectionSpotListener<T>{

    public void onStart(final T spot);

    public void onFinish(final T spot);
}
