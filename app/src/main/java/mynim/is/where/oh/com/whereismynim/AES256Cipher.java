package mynim.is.where.oh.com.whereismynim;

        import javax.crypto.BadPaddingException;
        import javax.crypto.Cipher;
        import javax.crypto.IllegalBlockSizeException;
        import javax.crypto.NoSuchPaddingException;
        import javax.crypto.SecretKey;
        import javax.crypto.spec.IvParameterSpec;
        import javax.crypto.spec.SecretKeySpec;
        import java.security.InvalidKeyException;
        import java.security.NoSuchAlgorithmException;
        import java.security.InvalidAlgorithmParameterException;
        import org.apache.commons.codec.binary.Base64;

public class AES256Cipher {

    private static volatile AES256Cipher INSTANCE;

    public String secretKey = ""; //32bit
    public String IV = ""; //16bit

    public AES256Cipher(String s){
        secretKey = s;
        IV = secretKey.substring(0,16);
    }

    public String fillText(String key){
        StringBuffer temp = new StringBuffer(key);
        if(temp.length() < 32)
            for(int i = 0; 32 != temp.length(); i++) temp.append("0");
        return temp.toString();
    }

    // text.setText(new AES256Cipher(fillText("암호")).AES_Encode("내용"));

    public String AES_Encode(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
        byte[] keyData = secretKey.getBytes();
        SecretKey secureKey = new SecretKeySpec(keyData, "AES");
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes()));
        byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
        String enStr = new String(Base64.encodeBase64(encrypted));

        return enStr;
    }

    public String AES_Decode(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
        byte[] keyData = secretKey.getBytes();
        SecretKey secureKey = new SecretKeySpec(keyData, "AES");
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes("UTF-8")));
        byte[] byteStr = Base64.decodeBase64(str.getBytes());

        return new String(c.doFinal(byteStr),"UTF-8");
    }
}
