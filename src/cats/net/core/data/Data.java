package cats.net.core.data;

import cats.net.core.Core;
import cats.net.core.buffer.Buffer;
import cats.net.core.buffer.BufferBuilder;
import cats.net.core.connection.rsa.RSAPrivKey;
import cats.net.core.connection.rsa.RSAPubKey;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

public final class Data {

    public final short opcode;
    private final Map<String, Object> map;

    public Data(final short opcode){
        this.opcode = opcode;

        map = new LinkedHashMap<>();
    }

    public Data(final int opcode){
        this((short) opcode);
    }

    public Data put(final String key, final Object value){
        if(value != null)
            map.put(key, value);
        return this;
    }

    public <T> T get(final String key, final Class<T> clazz){
        return clazz.cast(map.get(key));
    }

    public Object get(final String key){
        return map.get(key);
    }

    public Boolean getBoolean(final String key){
        return get(key, Boolean.class);
    }

    public Byte getByte(final String key){
        return get(key, Byte.class);
    }

    public Character getChar(final String key){
        return get(key, Character.class);
    }

    public Short getShort(final String key){
        return get(key, Short.class);
    }

    public Integer getInt(final String key){
        return get(key, Integer.class);
    }

    public Float getFloat(final String key){
        return get(key, Float.class);
    }

    public Double getDouble(final String key){
        return get(key, Double.class);
    }

    public Long getLong(final String key){
        return get(key, Long.class);
    }

    public String getString(final String key){
        return get(key, String.class);
    }

    public Map<String, Object> map(){
        return map;
    }

    public int entryCount(){
        return map.size();
    }

    public Buffer toBuffer(){
        final BufferBuilder builder = new BufferBuilder();
        Core.getEncoder(Data.class).encode(builder, this);
        return builder.create();
    }

    public Buffer toBuffer(final RSAPubKey key){
        return key == null ? toBuffer() : key.encryptToBuffer(toBuffer());
    }

    public static Data fromBuffer(final Buffer buffer){
        return (Data) Core.getDecoder(Data.class).decode(buffer);
    }

    public static Data fromBuffer(final Buffer buffer, final RSAPrivKey key){
        return fromBuffer(key.decryptToBuffer(buffer));
    }
}
