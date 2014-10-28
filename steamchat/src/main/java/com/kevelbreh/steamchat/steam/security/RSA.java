package com.kevelbreh.steamchat.steam.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by kevin on 2014/08/12.
 */
public class RSA {
    Cipher cipher;
    RSAPublicKey RSAkey;

    public RSA(byte[] key) {
        try {
            final List<Byte> list = new ArrayList<Byte>();
            for (final byte b : key) {
                list.add(b);
            }
            final AsnKeyParser keyParser = new AsnKeyParser(list);
            final BigInteger[] keys = keyParser.parseRSAPublicKey();
            init(keys[0], keys[1], true);
        } catch (final BerDecodeException e) {
            e.printStackTrace();
        }
    }

    public RSA(BigInteger mod, BigInteger exp) {
        this(mod, exp, true);
    }

    public RSA(BigInteger mod, BigInteger exp, boolean oaep) {
        init(mod, exp, oaep);
    }

    private void init(BigInteger mod, BigInteger exp, boolean oaep) {
        try {
            final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(mod, exp);

            final KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAkey = (RSAPublicKey) factory.generatePublic(publicKeySpec);

            Security.addProvider(new BouncyCastleProvider());
            if (oaep) {
                cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
            } else {
                cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            }
            cipher.init(Cipher.ENCRYPT_MODE, RSAkey);
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (final NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (final InvalidKeyException e) {
            e.printStackTrace();
        } catch (final InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (final NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public byte[] encrypt(byte[] input) {
        try {
            return cipher.doFinal(input);
        } catch (final IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (final BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
