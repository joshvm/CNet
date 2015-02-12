package cats.net.client;

import cats.net.client.handler.ClientDataHandler;
import cats.net.core.Core;
import cats.net.core.buffer.Buffer;
import cats.net.core.buffer.BufferBuilder;
import cats.net.core.connection.utils.ConnectionUtils;
import cats.net.core.data.Data;
import cats.net.core.utils.CoreUtils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class BlockingClient extends AbstractClient{

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    protected BlockingClient(final InetSocketAddress address){
        super(address);
    }

    protected BlockingClient(final String host, final int port){
        this(new InetSocketAddress(host, port));
    }

    protected BlockingClient(final int port){
        this("localhost", port);
    }

    protected void connect() throws Exception{
        socket = new Socket();
        socket.connect(address);

        out = new DataOutputStream(socket.getOutputStream());
        out.flush();

        in = new DataInputStream(socket.getInputStream());

        fireOnConnect();
    }

    public void disconnect(){
        ConnectionUtils.close(out, in, socket);
        if(!isConnected())
            fireOnDisconnect();
    }

    public boolean isConnected(){
        return socket.isConnected();
    }

    boolean send0(final Data data) throws Exception{
        if(data == null)
            return false;
        final Buffer buf = new BufferBuilder().putBytes(data.toBuffer(RSAKey()).array()).create();
        return ConnectionUtils.write(out, buf);
    }

    protected void read() throws Exception{
        final byte[] buffer = new byte[Core.bufferSize];
        if(in.read(buffer) < 0)
            throw new EOFException();
        final Buffer buf = Buffer.wrap(buffer);
        byte[] bytes = buf.getBytes();
        while(bytes.length != 0){
            final Data data = Data.fromBuffer(Buffer.wrap(bytes));
            CoreUtils.print("received data with opcode %d", data.opcode);
            if(data.opcode == Short.MIN_VALUE){
                initRSAKey(data.get("mod", BigInteger.class), data.get("exp", BigInteger.class));
                bytes = buf.getBytes();
                continue;
            }
            final ClientDataHandler handler = (ClientDataHandler)handlers.get(data.opcode);
            if(handler != null){
                try{
                    handler.handle(this, data);
                }catch(Exception ex){
                    try{
                        handler.handleException(this, data, ex);
                    }catch(Exception e){
                        CoreUtils.print(new Exception("error handling exception " + e.getMessage(), e));
                    }
                }
            }else if(defaultHandler != null){
                try{
                    defaultHandler.handle(this, data);
                }catch(Exception ex){
                    try{
                        defaultHandler.handleException(this, data, ex);
                    }catch(Exception e){
                        CoreUtils.print(new Exception("default handler error handling exception " + e.getMessage(), e));
                    }
                }
            }
            bytes = buf.getBytes();
        }
    }

    protected boolean canLoop(){
        return isConnected();
    }

    protected boolean loop() throws Exception{
        try{
            read();
            return true;
        }catch(Exception ex){
            CoreUtils.print(ex);
            disconnect();
            return false;
        }
    }
}
