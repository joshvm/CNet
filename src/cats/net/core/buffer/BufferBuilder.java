package cats.net.core.buffer;

import cats.net.core.Core;
import cats.net.core.connection.rsa.RSAPubKey;
import cats.net.core.utils.CoreUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public final class BufferBuilder {

    private final DataOutputStream out;
    private final ByteArrayOutputStream baos;

    public BufferBuilder(){
        baos = new ByteArrayOutputStream();

        out = new DataOutputStream(baos);
    }

    public Buffer create(){
        return Buffer.wrap(baos.toByteArray());
    }

    public Buffer create(final RSAPubKey key){
        return key == null ? create() : key.encryptToBuffer(baos.toByteArray());
    }

    public int size(){
        return baos.size();
    }

    public BufferBuilder putString(final String string){
        putInt(string.length());
        for(final char c : string.toCharArray())
            putByte((byte)c);
        return this;
    }

    public BufferBuilder putBoolean(final boolean v){
        try{
            out.writeBoolean(v);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return this;
    }

    public BufferBuilder putByte(final byte v){
        try{
            out.writeByte(v);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return this;
    }

    public BufferBuilder putBytes(final byte[] b){
        putInt(b.length);
        try{
            out.write(b);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return this;
    }

    public BufferBuilder putChar(final char v){
        try{
            out.writeChar(v);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return this;
    }

    public BufferBuilder putShort(final short v){
        try{
            out.writeShort(v);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return this;
    }

    public BufferBuilder putInt(final int v){
        try{
            out.writeInt(v);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return this;
    }

    public BufferBuilder putFloat(final float v){
        try{
            out.writeFloat(v);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return this;
    }

    public BufferBuilder putDouble(final double v){
        try{
            out.writeDouble(v);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return this;
    }

    public BufferBuilder putLong(final long v){
        try{
            out.writeLong(v);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
        return this;
    }

    public BufferBuilder putObject(final Object o){
        putString(o.getClass().getName());
        Core.getEncoder(o.getClass()).encode(this, o);
        return this;
    }

}
