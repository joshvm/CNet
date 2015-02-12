package cats.net.server;

import cats.net.core.Core;
import cats.net.core.buffer.Buffer;
import cats.net.core.buffer.BufferBuilder;
import cats.net.core.connection.utils.ConnectionUtils;
import cats.net.core.data.Data;
import cats.net.core.utils.CoreUtils;
import cats.net.server.handler.ServerDataHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Socket;

final class BlockingClientConnection extends ClientConnection implements Runnable{

    private final Socket socket;

    private final DataOutputStream out;
    private final DataInputStream in;

    BlockingClientConnection(final AbstractServer server, final Socket socket){
        super(server);
        this.socket = socket;

        out = new DataOutputStream(ConnectionUtils.out(socket));

        in = new DataInputStream(ConnectionUtils.in(socket));
    }

    public boolean send(final Data data){
        try{
            final Buffer buf = new BufferBuilder().putBytes(data.toBuffer().array()).create();
            return ConnectionUtils.write(out, buf);
        }catch(Exception ex){
            CoreUtils.print(ex);
            disconnect();
            return false;
        }
    }

    protected void read() throws Exception{
        final byte[] buffer = new byte[Core.bufferSize];
        if(in.read(buffer) < 0)
            throw new EOFException();
        Buffer buf = Buffer.wrap(buffer);
        if(spot.isUsingRSA())
            buf = spot.RSAKeys().privateKey().decryptToBuffer(buf);
        byte[] bytes = buf.getBytes();
        while(bytes.length != 0){
            final Buffer readBuf = Buffer.wrap(bytes);
            final Data data = Data.fromBuffer(readBuf);
            CoreUtils.print("received data with opcode %d", data.opcode);
            final ServerDataHandler handler = (ServerDataHandler)spot.getHandler(data.opcode);
            if(handler != null){
                try{
                    handler.handle(spot, this, data);
                }catch(Exception ex){
                    try{
                        handler.handleException(spot, this, data, ex);
                    }catch(Exception e){
                        CoreUtils.print(new Exception("error handling exception " + e.getMessage(), e));
                    }
                }
            }else if(spot.defaultHandler != null){
                try{
                    spot.defaultHandler.handle(spot, this, data);
                }catch(Exception ex){
                    try{
                        spot.defaultHandler.handleException(spot, this, data, ex);
                    }catch(Exception e){
                        CoreUtils.print(new Exception("default handler: error handling exception " + e.getMessage(), e));
                    }
                }
            }
            bytes = buf.getBytes();
        }
    }

    public boolean isConnected(){
        return socket.isConnected();
    }

    public void disconnect(){
        ConnectionUtils.close(out, in, socket);
        spot.disconnect(this);
    }

    public void run(){
        while(isConnected()){
            try{
                read();
            }catch(Exception ex){
                CoreUtils.print(ex);
                break;
            }
        }
        disconnect();
    }

    void thread(){
        final Thread t = new Thread(this);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    public String toString(){
        return socket.toString();
    }
}
