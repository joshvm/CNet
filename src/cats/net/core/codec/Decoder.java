package cats.net.core.codec;

import cats.net.core.buffer.Buffer;

public interface Decoder<T> {

    public T decode(final Buffer buffer);
}
