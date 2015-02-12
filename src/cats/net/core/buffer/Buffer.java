package cats.net.core.buffer;

import cats.net.core.Core;
import cats.net.core.connection.rsa.RSAPrivKey;
import cats.net.core.utils.CoreUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;

public final class Buffer{

    private final DataInputStream in;
    private final byte[] bytes;

    private Buffer(final byte[] bytes){
        this.bytes = bytes;

        in = new DataInputStream(new ByteArrayInputStream(bytes));
    }

    public byte[] array(){
        return bytes;
    }

    public byte[] array(final RSAPrivKey key){
        return key == null ? bytes : key.decrypt(bytes);
    }

    public int size(){
        return bytes.length;
    }

    public ByteBuffer toByteBuffer(){
        return ByteBuffer.wrap(bytes);
    }

    public String getString(){
        final int length = getInt();
        final StringBuilder builder = new StringBuilder(length);
        for(int i = 0; i < length; i++)
            builder.append((char)getByte());
        return builder.toString();
    }

    public boolean getBoolean(){
        try{
            return in.readBoolean();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return false;
        }
    }

    public byte getByte(){
        try{
            return in.readByte();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return -1;
        }
    }

    public byte[] getBytes(){
        try{
            final int size = getInt();
            if(size <= 0)
                return new byte[0];
            final byte[] bytes = new byte[size];
            in.readFully(bytes);
            return bytes;
        }catch(Exception ex){
            CoreUtils.print(ex);
            return new byte[0];
        }
    }

    public char getChar(){
        try{
            return in.readChar();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return (char)-1;
        }
    }

    public short getShort(){
        try{
            return in.readShort();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return -1;
        }
    }

    public int getInt(){
        try{
            return in.readInt();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return -1;
        }
    }

    public float getFloat(){
        try{
            return in.readFloat();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return -1;
        }
    }

    public double getDouble(){
        try{
            return in.readDouble();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return -1;
        }
    }

    public long getLong(){
        try{
            return in.readLong();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return -1;
        }
    }

    public <T> T getObject(){
        return (T) Core.getDecoder(getString()).decode(this);
    }

    public static Buffer wrap(final byte[] bytes){
        return new Buffer(bytes);
    }

    public static Buffer wrap(final byte[] bytes, final RSAPrivKey key){
        return key.decryptToBuffer(bytes);
    }
}
