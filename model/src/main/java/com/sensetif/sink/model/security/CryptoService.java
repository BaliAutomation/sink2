package com.sensetif.sink.model.security;

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.spicter.curtis.api.injection.scope.This;
import com.spicter.curtis.api.configuration.Configuration;
import com.spicter.curtis.api.mixin.Mixins;
import com.spicter.curtis.api.property.Property;

@Mixins( { CryptoService.Mixin.class } )
public interface CryptoService
{
    String encrypt( String clear );

    String decrypt( String encrypted );

    class Mixin
        implements CryptoService
    {
        private SecretKeySpec key;
        private IvParameterSpec ivSpec;
        private Cipher cipher;
        private MessageDigest digest;

        public Mixin( @This Configuration<CryptoConfiguration> configuration)
            throws Exception
        {
            CryptoConfiguration config = configuration.get();
            String digestAlgorithm = config.digestAlgorithm().get();
            if( digestAlgorithm == null )
            {
                digestAlgorithm = "SHA-1";
            }
            digest = MessageDigest.getInstance( digestAlgorithm );
            byte[] keyBytes = hashed( config.secret1().get() );
            byte[] ivBytes = hashed( config.secret2().get() );

            key = new SecretKeySpec(keyBytes, "DES");
            ivSpec = new IvParameterSpec(ivBytes);

            String encryptionAlgorithm = config.encryptionAlgorithm().get();
            if( encryptionAlgorithm == null )
            {
                encryptionAlgorithm = "AES";
            }
            cipher = Cipher.getInstance( encryptionAlgorithm );
        }

        public String encrypt( String clear )
            throws CryptoException
        {
            try
            {
                cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

                byte[] input = clear.getBytes();
                byte[] encrypted= new byte[cipher.getOutputSize(input.length)];
                int enc_len = cipher.update(input, 0, input.length, encrypted, 0);
                enc_len += cipher.doFinal(encrypted, enc_len);
                return new String( encrypted, 0, enc_len );

            } catch( Exception e ) {
                throw new CryptoException( "Unable to encrypt.", e );
            }
        }

        public String decrypt( String encryptedString )
            throws CryptoException
        {
            try
            {
                cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

                byte[] encrypted = encryptedString.getBytes();
                int enc_len = encrypted.length;
                byte[] decrypted = new byte[cipher.getOutputSize(encrypted.length)];
                int dec_len = cipher.update(encrypted, 0, enc_len, decrypted, 0);
                dec_len += cipher.doFinal(decrypted, dec_len);
                return new String( decrypted, 0, dec_len );
            } catch( Exception e ) {
                throw new CryptoException( "Unable to decrypt.", e );
            }
        }

        private byte[] hashed( String data )
        {
            digest.reset();
            return digest.digest( data.getBytes() );
        }
    }
}