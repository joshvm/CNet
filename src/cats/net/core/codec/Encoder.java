package cats.net.core.codec;

import cats.net.core.buffer.BufferBuilder;

public interface Encoder<T> {

    public void encode(final BufferBuilder builder, final T obj);
}
