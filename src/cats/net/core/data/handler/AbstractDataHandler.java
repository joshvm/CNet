package cats.net.core.data.handler;

import cats.net.core.connection.spot.AbstractConnectionSpot;

public abstract class AbstractDataHandler<T extends AbstractConnectionSpot> {

    public abstract short[] getOpcodes();
}
