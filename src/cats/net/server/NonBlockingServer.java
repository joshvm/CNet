package cats.net.server;

import cats.net.core.data.Data;
import cats.net.core.utils.CoreUtils;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class NonBlockingServer extends AbstractServer{

    private Selector selector;
    private ServerSocketChannel channel;

    private final Map<SelectionKey, NonBlockingClientConnection> connected;

    protected NonBlockingServer(final InetSocketAddress address){
        super(address);

        connected = new HashMap<>();
    }

    protected NonBlockingServer(final String host, final int port){
        this(new InetSocketAddress(host, port));
    }

    protected NonBlockingServer(final int port){
        this("localhost", port);
    }

    public Collection<ClientConnection> getConnected(){
        return Collections.unmodifiableCollection(connected.values());
    }

    protected void connect() throws Exception{
        selector = Selector.open();
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(address);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void accept(){
        try{
            final SocketChannel client = channel.accept();
            client.configureBlocking(false);
            final SelectionKey key = client.register(selector, SelectionKey.OP_READ);
            final NonBlockingClientConnection connection = new NonBlockingClientConnection(this, key, client);
            connected.put(key, connection);
            if(isUsingRSA()){
                final RSAPublicKeySpec pub = RSAKeys().publicKey().spec();
                final Data data = new Data(Short.MIN_VALUE).put("mod", pub.getModulus()).put("exp", pub.getPublicExponent());
                connection.send(data);
            }
            fireOnJoin(connection);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
    }

    boolean disconnect(final ClientConnection connection){
        if(connection == null)
            return false;
        final SelectionKey key = connected.keySet().stream().filter(
                k -> connected.get(k).equals(connection)
        ).findFirst().orElse(null);
        if(key == null)
            return false;
        connected.remove(key);
        fireOnLeave(connection);
        return true;
    }

    private boolean read(final SelectionKey key){
        final NonBlockingClientConnection connection = connected.get(key);
        try{
            connection.read();
            return true;
        }catch(Exception ex){
            CoreUtils.print(ex);
            connection.disconnect();
            return false;
        }
    }

    public void disconnect(){
        try{
            channel.close();
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
    }

    public boolean isConnected(){
        return channel != null && (channel.isOpen() || channel.socket().isBound());
    }

    protected boolean canLoop(){
        return isConnected();
    }

    protected boolean loop() throws Exception{
        selector.select();
        final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while(keys.hasNext()){
            final SelectionKey key = keys.next();
            keys.remove();
            if(key.isAcceptable())
                accept();
            else if(key.isReadable())
                read(key);
        }
        return true;
    }
}
