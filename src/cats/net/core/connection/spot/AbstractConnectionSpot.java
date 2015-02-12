package cats.net.core.connection.spot;

import cats.net.core.connection.spot.event.ConnectionSpotListener;
import cats.net.core.connection.spot.event.SpotStateListener;
import cats.net.core.connection.utils.ConnectionUtils;
import cats.net.core.data.handler.AbstractDataHandler;
import cats.net.core.utils.CoreUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractConnectionSpot<T extends AbstractConnectionSpot> extends Thread implements Runnable{

    protected final Map<Short, AbstractDataHandler<T>> handlers;
    protected final List<ConnectionSpotListener<T>> listeners;

    protected final InetSocketAddress address;

    protected AbstractConnectionSpot(final InetSocketAddress address){
        setPriority(MAX_PRIORITY);
        this.address = address;

        handlers = new HashMap<>();

        listeners = new LinkedList<>();
    }

    protected AbstractConnectionSpot(final String host, final int port){
        this(new InetSocketAddress(host, port));
    }

    protected AbstractConnectionSpot(final int port){
        this("localhost", port);
    }

    public InetSocketAddress getAddress(){
        return address;
    }

    public void addListener(final ConnectionSpotListener<T> listener){
        listeners.add(listener);
    }

    public void removeListener(final ConnectionSpotListener<T> listener){
        listeners.remove(listener);
    }

    public void removeAllListeners(){
        listeners.clear();
    }

    public void addHandler(final AbstractDataHandler<T> handler){
        for(final short s : handler.getOpcodes())
            handlers.put(s, handler);
        CoreUtils.print("Registered handler %s with opcodes: %s", handler.getClass(), Arrays.toString(handler.getOpcodes()));
    }

    public AbstractDataHandler<T> getHandler(final short opcode){
        return handlers.get(opcode);
    }

    public AbstractDataHandler<T> getHandler(final int opcode){
        return getHandler((short)opcode);
    }

    public boolean addHandlers(final InputStream input){
        try{
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(input);
            document.getDocumentElement().normalize();
            final NodeList handlers = document.getElementsByTagName("handler");
            for(int i = 0; i < handlers.getLength(); i++){
                final Node node = handlers.item(i);
                if(node.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                final Element e = (Element)node;
                final Class<? extends AbstractDataHandler<T>> clazz = (Class<? extends AbstractDataHandler<T>>)Class.forName(e.getTextContent());
                addHandler(clazz.newInstance());
            }
            ConnectionUtils.close(input);
            return true;
        }catch(Exception ex){
            CoreUtils.print(ex);
            return false;
        }
    }

    public boolean addHandlers(final File xml){
        try{
            return addHandlers(new FileInputStream(xml));
        }catch(Exception ex){
            CoreUtils.print(ex);
            return false;
        }
    }

    protected void fireOnStart(){
        listeners.stream().filter(
                l -> l instanceof SpotStateListener
        ).forEach(
                l -> ((SpotStateListener<T>)l).onStart((T)this)
        );
    }

    protected void fireOnFinish(){
        listeners.stream().filter(
                l -> l instanceof SpotStateListener
        ).forEach(
                l -> ((SpotStateListener<T>)l).onFinish((T)this)
        );
    }

    public void run(){
        while(canLoop()){
            try{
                if(!loop())
                    break;
            }catch(Exception ex){
                CoreUtils.print(ex);
            }
        }
        fireOnFinish();
    }

    public void start(){
        try{
            init();
            fireOnStart();
            connect();
            super.start();
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
    }

    public abstract void init();

    public abstract boolean isConnected();

    public abstract void disconnect();

    protected abstract boolean canLoop();

    protected abstract boolean loop() throws Exception;

    protected abstract void connect() throws Exception;
}
