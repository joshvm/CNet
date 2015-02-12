package cats.net.server;

import cats.net.core.data.Data;
import cats.net.core.utils.CoreUtils;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class BlockingServer extends AbstractServer{

    private ServerSocket socket;

    private final List<BlockingClientConnection> connections;

    protected BlockingServer(final InetSocketAddress address){
        super(address);

        connections = new LinkedList<>();
    }

    protected BlockingServer(final String host, final int port){
        this(new InetSocketAddress(host, port));
    }

    protected BlockingServer(final int port){
        this("localhost", port);
    }

    public Collection<ClientConnection> getConnected(){
        return Collections.unmodifiableList(connections);
    }

    boolean disconnect(final ClientConnection connection){
        if(connection == null || !connections.contains(connection))
            return false;
        connections.remove(connection);
        fireOnLeave(connection);
        return true;
    }

    protected void connect() throws Exception{
        socket = new ServerSocket();
        socket.bind(address);
    }

    public void disconnect(){
        try{
            socket.close();
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
    }

    public boolean isConnected(){
        return socket != null && socket.isBound();
    }

    protected boolean canLoop(){
        return isConnected();
    }

    protected boolean loop() throws Exception{
        final Socket client = socket.accept();
        final BlockingClientConnection connection = new BlockingClientConnection(this, client);
        connections.add(connection);
        if(isUsingRSA()){
            final RSAPublicKeySpec pub = RSAKeys().publicKey().spec();
            final Data data = new Data(Short.MIN_VALUE).put("mod", pub.getModulus()).put("exp", pub.getPublicExponent());
            connection.send(data);
        }
        fireOnJoin(connection);
        connection.thread();
        return true;
    }
}
