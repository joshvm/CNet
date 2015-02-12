package cats.net.core.data.former;

public class DataFormerNotSetException extends RuntimeException{

    public DataFormerNotSetException(final short opcode){
        super("No data former found for opcode " + opcode);
    }
}
