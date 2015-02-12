package cats.net.core;

import cats.net.core.buffer.Buffer;
import cats.net.core.buffer.BufferBuilder;
import cats.net.core.connection.utils.ConnectionUtils;
import cats.net.core.data.Data;
import cats.net.core.data.former.DataFormer;
import cats.net.core.codec.Decoder;
import cats.net.core.codec.Encoder;
import cats.net.core.utils.CoreUtils;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Core {

    private static final Map<String, Encoder> ENCODERS = new HashMap<>();
    private static final Map<String, Decoder> DECODERS = new HashMap<>();

    private static final Map<Short, DataFormer> DATA_FORMERS = new HashMap<>();

    public static boolean verbose = false;
    public static boolean verboseExceptions = false;

    public static int bufferSize = 1024;

    static{
        addCodec(Boolean.class,
                (Encoder<Boolean>) BufferBuilder::putBoolean,
                (Decoder<Boolean>) Buffer::getBoolean);
        addCodec(Byte.class,
                (Encoder<Byte>) BufferBuilder::putByte,
                (Decoder<Byte>) Buffer::getByte);
        addCodec(byte[].class,
                (Encoder<byte[]>) BufferBuilder::putBytes,
                (Decoder<byte[]>) Buffer::getBytes);
        addCodec(Character.class,
                (Encoder<Character>) BufferBuilder::putChar,
                (Decoder<Character>) Buffer::getChar);
        addCodec(Short.class,
                (Encoder<Short>) BufferBuilder::putShort,
                (Decoder<Short>) Buffer::getShort);
        addCodec(Integer.class,
                (Encoder<Integer>) BufferBuilder::putInt,
                (Decoder<Integer>) Buffer::getInt);
        addCodec(Float.class,
                (Encoder<Float>) BufferBuilder::putFloat,
                (Decoder<Float>) Buffer::getFloat);
        addCodec(Double.class,
                (Encoder<Double>) BufferBuilder::putDouble,
                (Decoder<Double>) Buffer::getDouble);
        addCodec(Long.class,
                (Encoder<Long>) BufferBuilder::putLong,
                (Decoder<Long>) Buffer::getLong);
        addCodec(String.class,
                (Encoder<String>) BufferBuilder::putString,
                (Decoder<String>) Buffer::getString);
        addCodec(BufferedImage.class,
                (Encoder<BufferedImage>) (bldr, img) -> {
                    final int width = img.getWidth();
                    final int height = img.getHeight();
                    bldr.putInt(width).putInt(height).putInt(img.getType());
                    for (int x = 0; x < width; x++)
                        for (int y = 0; y < height; y++)
                            bldr.putInt(img.getRGB(x, y));
                },
                (Decoder<BufferedImage>) buf -> {
                    final int width = buf.getInt();
                    final int height = buf.getInt();
                    final BufferedImage img = new BufferedImage(width, height, buf.getInt());
                    for (int x = 0; x < width; x++)
                        for (int y = 0; y < height; y++)
                            img.setRGB(x, y, buf.getInt());
                    return img;
                }
        );
        addCodec(Color.class,
                (Encoder<Color>) (bldr, col) -> bldr.putInt(col.getRGB()),
                (Decoder<Color>) buf -> new Color(buf.getInt())
        );
        addCodec(Point.class,
                (Encoder<Point>) (bldr, pt) -> bldr.putInt(pt.x).putInt(pt.y),
                (Decoder<Point>) buf -> new Point(buf.getInt(), buf.getInt())
        );
        addCodec(Rectangle.class,
                (Encoder<Rectangle>) (bldr, rect) ->
                        bldr.putInt(rect.x).putInt(rect.y).putInt(rect.width).putInt(rect.height),
                (Decoder<Rectangle>) buf -> new Rectangle(buf.getInt(), buf.getInt(), buf.getInt(), buf.getInt())
        );
        addCodec(Date.class,
                (Encoder<Date>) (bldr, date) -> bldr.putLong(date.getTime()),
                (Decoder<Date>) buf -> new Date(buf.getLong())
        );
        addCodec(BigInteger.class,
                (Encoder<BigInteger>) (bldr, bi) -> bldr.putString(bi.toString()),
                (Decoder<BigInteger>) buf -> new BigInteger(buf.getString())
        );
        addCodec(File.class,
                (Encoder<File>) (bldr, file) -> {
                    bldr.putString(file.getName());
                    try {
                        final byte[] bytes = Files.readAllBytes(file.toPath());
                        bldr.putBoolean(true);
                        bldr.putBytes(bytes);
                    } catch (Exception ex) {
                        bldr.putBoolean(false);
                    }
                },
                (Decoder<File>) buf -> {
                    final String full = buf.getString();
                    if (!buf.getBoolean())
                        return null;
                    final int i = full.lastIndexOf('.');
                    final String name = full.substring(0, i);
                    final String extension = full.substring(i + 1);
                    try {
                        final File file = File.createTempFile(name, extension);
                        Files.write(file.toPath(), buf.getBytes());
                        return file;
                    } catch (Exception ex) {
                        return null;
                    }
                }
        );
        addCodec(Data.class,
                (Encoder<Data>) (bldr, data) -> {
                    bldr.putShort(data.opcode);
                    bldr.putInt(data.entryCount());
                    data.map().entrySet().forEach(
                            entry -> {
                                bldr.putString(entry.getKey());
                                bldr.putObject(entry.getValue());
                            }
                    );
                },
                (Decoder<Data>) buf -> {
                    final Data data = new Data(buf.getShort());
                    IntStream.range(0, buf.getInt()).forEach(
                            i -> data.map().put(buf.getString(), buf.getObject())
                    );
                    return data;
                }
        );
    }

    private Core(){}

    public static <T> void addCodec(final Class<T> clazz, final Encoder<T> encoder, final Decoder<T> decoder){
        addCodec(clazz.getName(), encoder, decoder);
    }

    public static <T> void addCodec(final String name, final Encoder<T> encoder, final Decoder<T> decoder){
        ENCODERS.put(name, encoder);
        DECODERS.put(name, decoder);
    }

    public static void addDataFormer(final DataFormer former){
        for(final short s : former.getOpcodes())
            DATA_FORMERS.put(s, former);
        CoreUtils.print("Registered former %s at opcodes: %s", former.getClass(), Arrays.toString(former.getOpcodes()));
    }

    public static boolean addDataFormers(final InputStream input){
        try{
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(input);
            document.getDocumentElement().normalize();
            final NodeList handlers = document.getElementsByTagName("former");
            for(int i = 0; i < handlers.getLength(); i++){
                final Node node = handlers.item(i);
                if(node.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                final Element element = (Element)node;
                final Class<? extends DataFormer> clazz = (Class<? extends DataFormer>)Class.forName(element.getTextContent());
                addDataFormer(clazz.newInstance());
            }
            ConnectionUtils.close(input);
            return true;
        }catch(Exception ex){
            CoreUtils.print(ex);
            return false;
        }
    }

    public static boolean addDataFormers(final File xml){
        try{
            return addDataFormers(new FileInputStream(xml));
        }catch(Exception ex){
            CoreUtils.print(ex);
            return false;
        }
    }

    public static <T extends DataFormer> T getDataFormer(final short opcode){
        return (T) DATA_FORMERS.get(opcode);
    }

    public static DataFormer getDataFormer(final int opcode){
        return getDataFormer((short)opcode);
    }

    public static Encoder getEncoder(final Class clazz){
        return getEncoder(clazz.getName());
    }

    public static Encoder getEncoder(final String name){
        return ENCODERS.get(name);
    }

    public static Decoder getDecoder(final Class clazz){
        return getDecoder(clazz.getName());
    }

    public static Decoder getDecoder(final String name){
        return DECODERS.get(name);
    }
}
