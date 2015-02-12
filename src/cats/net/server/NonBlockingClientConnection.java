package cats.net.server;

import cats.net.core.Core;
import cats.net.core.buffer.Buffer;
import cats.net.core.buffer.BufferBuilder;
import cats.net.core.connection.utils.ConnectionUtils;
import cats.net.core.data.Data;
import cats.net.core.utils.CoreUtils;
import cats.net.server.handler.ServerDataHandler;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

final class NonBlockingClientConnection extends ClientConnection {

    private final SelectionKey key;
    private final SocketChannel channel;

    NonBlockingClientConnection(final AbstractServer server, final SelectionKey key, final SocketChannel channel){
        super(server);
        this.key = key;
        this.channel = channel;
    }

    public boolean send(final Data data){
        try{
            final Buffer buf = new BufferBuilder().putBytes(data.toBuffer().array()).create();
            return ConnectionUtils.write(channel, buf);
        }catch(Exception ex){
            CoreUtils.print(ex);
            disconnect();
            return false;
        }
    }

    protected void read() throws Exception{
        final ByteBuffer buffer = ByteBuffer.allocate(Core.bufferSize);
        if(channel.read(buffer) < 0)
            throw new EOFException();
        buffer.flip();
        Buffer buf = Buffer.wrap(buffer.array());
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
        return channel.isConnected() || channel.isOpen();
    }

    public void disconnect(){
        key.cancel();
        if(!isConnected())
            return;
        try{
            channel.close();
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        spot.disconnect(this);
    }

    public String toString(){
        return channel.toString();
    }
}
