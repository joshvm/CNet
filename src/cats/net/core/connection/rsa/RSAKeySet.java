package cats.net.core.connection.rsa;

import cats.net.core.utils.CoreUtils;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import javax.crypto.Cipher;

public class RSAKeySet {

    private RSAPubKey pub;
    private RSAPrivKey priv;

    public RSAKeySet(final int size){
        try{
            final KeyFactory factory = KeyFactory.getInstance("RSA");
            final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(size);
            final KeyPair pair = gen.generateKeyPair();
            pub = new RSAPubKey(factory, pair.getPublic());
            priv = new RSAPrivKey(factory, pair.getPrivate());
        }catch(Exception ex){
            CoreUtils.print(ex);
        }
    }

    public RSAPubKey publicKey(){
        return pub;
    }

    public RSAPrivKey privateKey(){
        return priv;
    }

    public static Cipher newCipher(){
        try{
            return Cipher.getInstance("RSA/ECB/PKCS1Padding");
        }catch(Exception ex){
            CoreUtils.print(ex);
            return null;
        }
    }
}
