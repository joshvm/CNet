package cats.net.client;

import cats.net.client.event.ClientListener;
import cats.net.client.event.ClientStateListener;
import cats.net.client.handler.ClientDataHandler;
import cats.net.core.Core;
import cats.net.core.connection.rsa.RSAPubKey;
import cats.net.core.connection.spot.AbstractConnectionSpot;
import cats.net.core.connection.spot.event.ConnectionSpotListener;
import cats.net.core.data.Data;
import cats.net.core.data.former.DataFormer;
import cats.net.core.data.former.DataFormerNotSetException;
import cats.net.core.data.handler.AbstractDataHandler;
import cats.net.core.utils.CoreUtils;
import java.math.BigInteger;
import java.net.InetSocketAddress;

public abstract class AbstractClient extends AbstractConnectionSpot<AbstractClient>{

    private RSAPubKey key;
    protected ClientDataHandler<AbstractClient> defaultHandler;

    AbstractClient(final InetSocketAddress address){
        super(address);

        defaultHandler = new ClientDataHandler<AbstractClient>() {

            public void handle(AbstractClient client, Data data) {
                CoreUtils.print("(%s) received data with opcode %d", client, data);
            }

            public short[] getOpcodes(){
                return new short[0];
            }
        };
    }

    void initRSAKey(final BigInteger mod, final BigInteger exp){
        CoreUtils.print("Initialising RSA: mod: %s exp: %s", mod, exp);
        key = new RSAPubKey(mod, exp);
    }

    public final RSAPubKey RSAKey(){
        return key;
    }

    public final boolean isUsingRSA(){
        return key != null;
    }

    void fireOnConnect(){
        listeners.stream().filter(
                l -> l instanceof ClientListener
        ).forEach(
                l -> ((ClientListener)l).onConnect(this)
        );
    }

    void fireOnDisconnect(){
        listeners.stream().filter(
                l -> l instanceof ClientListener
        ).forEach(
                l -> ((ClientListener)l).onDisconnect(this)
        );
    }

    public void addListener(final ClientListener listener){
        addListener((ConnectionSpotListener)listener);
    }

    public void addListener(final ClientStateListener listener){
        addListener((ConnectionSpotListener)listener);
    }

    public void addHandler(final ClientDataHandler handler){
        addHandler((AbstractDataHandler)handler);
    }

    abstract boolean send0(final Data data) throws Exception;

    public boolean send(final short opcode, final Object... args) throws DataFormerNotSetException {
        final DataFormer former = Core.getDataFormer(opcode);
        if(former == null)
            throw new DataFormerNotSetException(opcode);
        return send(former.form(opcode, args));
    }

    public boolean send(final int opcode, final Object... args) throws DataFormerNotSetException{
        return send((short)opcode, args);
    }

    public boolean send(final Data data){
        try{
            return send0(data);
        }catch(Exception ex){
            CoreUtils.print(ex);
            return false;
        }
    }

    public boolean reconnect(){
        if(isConnected())
            return true;
        try{
            connect();
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return isConnected();
    }
}
