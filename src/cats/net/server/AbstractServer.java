package cats.net.server;

import cats.net.core.connection.rsa.RSAKeySet;
import cats.net.core.connection.spot.AbstractConnectionSpot;
import cats.net.core.connection.spot.event.ConnectionSpotListener;
import cats.net.core.data.Data;
import cats.net.core.data.handler.AbstractDataHandler;
import cats.net.core.utils.CoreUtils;
import cats.net.server.event.ServerListener;
import cats.net.server.event.ServerStateListener;
import cats.net.server.handler.ServerDataHandler;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractServer extends AbstractConnectionSpot<AbstractServer> {

    public static final int RSA_SIZE = 1024;

    private RSAKeySet keys;
    protected ServerDataHandler<AbstractServer> defaultHandler;

    AbstractServer(final InetSocketAddress address){
        super(address);

        defaultHandler = new ServerDataHandler<AbstractServer>(){

            public void handle(AbstractServer server, ClientConnection connection, Data data){
                CoreUtils.print("(%s) received data from %s with opcode %d", server, connection, data);
            }

            public short[] getOpcodes() {
                return new short[0];
            }
        };
    }

    public void setDefaultHandler(final ServerDataHandler<AbstractServer> defaultHandler){
        this.defaultHandler = defaultHandler;
    }

    public ServerDataHandler<AbstractServer> getDefaultHandler(){
        return defaultHandler;
    }

    public final boolean initRSAKeys(final int size){
        if(isConnected())
            return false;
        keys = new RSAKeySet(size);
        return true;
    }

    public final boolean initRSAKeys(){
        return initRSAKeys(RSA_SIZE);
    }

    public final RSAKeySet RSAKeys(){
        return keys;
    }

    public final boolean isUsingRSA(){
        return keys != null;
    }

    void fireOnJoin(final ClientConnection connection){
        listeners.stream().filter(
                l -> l instanceof ServerListener
        ).forEach(
                l -> ((ServerListener)l).onJoin(this, connection)
        );
    }

    void fireOnLeave(final ClientConnection connection){
        listeners.stream().filter(
                l -> l instanceof ServerListener
        ).forEach(
                l -> ((ServerListener)l).onLeave(this, connection)
        );
    }

    public void addListener(final ServerListener listener){
        addListener((ConnectionSpotListener)listener);
    }

    public void addListener(final ServerStateListener listener){
        addListener((ConnectionSpotListener)listener);
    }

    public void addHandler(final ServerDataHandler handler){
        addHandler((AbstractDataHandler)handler);
    }
    public void sendToAll(final Data... datas){
        getConnected().forEach(c -> Arrays.stream(datas).forEach(c::send));
    }

    public void sendToAll(final short opcode, final Object... args){
        getConnected().forEach(c -> c.send(opcode, args));
    }

    public void sendToAll(final int opcode, final Object... args){
        sendToAll((short) opcode, args);
    }

    public void sendToAllExcept(final ClientConnection exception, final Data... datas){
        getFilteredConnections(exception).forEach(c -> Arrays.stream(datas).forEach(c::send));
    }

    public void sendToAllExcept(final Data data, final ClientConnection... exceptions){
        getFilteredConnections(exceptions).forEach(c -> c.send(data));
    }

    public void sendToAllExcept(final ClientConnection exception, final short opcode, final Object... args){
        getFilteredConnections(exception).forEach(c -> c.send(opcode, args));
    }

    public void sendToAllExcept(final ClientConnection exception, final int opcode, final Object... args){
        sendToAllExcept(exception, (short)opcode, args);
    }

    public <J> List<J> getConnectedAttachments(){
        return getConnected().stream().filter(c -> c != null).map(c -> (J)c).collect(Collectors.toList());
    }

    public List<ClientConnection> getFilteredConnections(final ClientConnection... clientConnections){
        final List<ClientConnection> connections = Arrays.asList(clientConnections);
        return getConnected().stream().filter(c -> !connections.contains(c)).collect(Collectors.toList());
    }

    public ClientConnection getConnectionByAttachment(final Object attachment){
        return getConnected().stream().filter(c -> Objects.equals(c.attachment(), attachment)).findFirst().orElse(null);
    }

    public abstract Collection<ClientConnection> getConnected();

    abstract boolean disconnect(final ClientConnection connection);

}
