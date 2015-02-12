package cats.net.core.connection.rsa;

import cats.net.core.buffer.Buffer;
import cats.net.core.utils.CoreUtils;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import javax.crypto.Cipher;

public class RSAPrivKey {

    private PrivateKey key;
    private RSAPrivateKeySpec spec;

    public RSAPrivKey(final KeyFactory factory, final PrivateKey key){
        this.key = key;

        try{
            spec = factory.getKeySpec(key, RSAPrivateKeySpec.class);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
    }

    public RSAPrivKey(final BigInteger mod, final BigInteger exp){
        spec = new RSAPrivateKeySpec(mod, exp);

        try{
            key = KeyFactory.getInstance("RSA").generatePrivate(spec);
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
    }

    public byte[] decrypt(final byte[] bytes){
        try{
            return new BigInteger(bytes).modPow(spec.getPrivateExponent(), spec.getModulus()).toByteArray();
        }catch(Exception ex){
            CoreUtils.print(ex);
            return bytes;
        }
    }

    public byte[] decrypt(final Buffer buffer){
        return decrypt(buffer.array());
    }

    public Buffer decryptToBuffer(final byte[] bytes){
        return Buffer.wrap(decrypt(bytes));
    }

    public Buffer decryptToBuffer(final Buffer buffer){
        return Buffer.wrap(decrypt(buffer));
    }

    public PrivateKey key(){
        return key;
    }

    public RSAPrivateKeySpec spec(){
        return spec;
    }
}
