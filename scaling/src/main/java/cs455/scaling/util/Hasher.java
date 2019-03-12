package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {
    public static String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException{
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] hash = digest.digest(data);
            BigInteger hashInt = new BigInteger(1, hash);
            String hashString = hashInt.toString(16);
            StringBuilder padded = new StringBuilder();

            for (int i = 0; i < 40 - hashString.length() ; ++i) {
                padded.append("0");
            }

            padded.append(hashString);

            return padded.toString();
    }
}
