package cats.net.core.connection.rsa;

import cats.net.core.buffer.Buffer;
import cats.net.core.utils.CoreUtils;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;

public class RSAPubKey {

    private PublicKey key;
    private RSAPublicKeySpec spec;

    public RSAPubKey(final KeyFactory factory, final PublicKey key){
        this.key = key;

        try{
            spec = factory.getKeySpec(key, RSAPublicKeySpec.class);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
    }

    public RSAPubKey(final BigInteger mod, final BigInteger exp){
        spec = new RSAPublicKeySpec(mod, exp);

        try{
            key = KeyFactory.getInstance("RSA").generatePublic(spec);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
    }

    public byte[] encrypt(final byte[] bytes){
        try{
            return new BigInteger(bytes).modPow(spec.getPublicExponent(), spec.getModulus()).toByteArray();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return bytes;
        }
    }

    public byte[] encrypt(final Buffer buffer){
        return encrypt(buffer.array());
    }

    public Buffer encryptToBuffer(final byte[] bytes){
        return Buffer.wrap(encrypt(bytes));
    }

    public Buffer encryptToBuffer(final Buffer buffer){
        return Buffer.wrap(encrypt(buffer));
    }

    public PublicKey key(){
        return key;
    }

    public RSAPublicKeySpec spec(){
        return spec;
    }
}
