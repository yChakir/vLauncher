package ma.ychakir.rz.vlauncher.Utils;

/**
 * @author Yassine
 */
public class EncodingUtil {

    private static char[] encryptChar = "^&T_Nsd{xo5v`rOYV+,iIU#kCJq8$\'~L0P]FeBn-Au(pXHZhwDy2}agWG7K=bQ;SRt)46l@jE%9!c1[3fmMz".toCharArray();
    private static char[] decryptChar = new char[]{'T', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000',
            '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000',
            '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', 'K', '\u0000',
            '\u0016', '\u001c', 'I', '\u0001', '\u001d', '*', 'B', '\u0000', '\u0011', '\u0012', '\'', '\u0000', '\u0000', ' ', 'M', '3',
            'O', 'C', '\n', 'D', '9', '\u001b', 'J', '\u0000', '>', '\u0000', ';', '\u0000', '\u0000', 'F', '(', '%', '\u0018', '1', 'H',
            '#', '8', '-', '\u0014', '\u0019', ':', '\u001f', 'R', '\u0004', '\u000e', '!', '=', '@', '?', '\u0002', '\u0015', '\u0010',
            '7', ',', '\u000f', '.', 'N', '\u0000', '"', '\u0000', '\u0003', '\f', '5', '<', 'L', '\u0006', '$', 'P', '6', '/', '\u0013',
            'G', '\u0017', 'E', 'Q', '&', '\t', '+', '\u001a', '\r', '\u0005', 'A', ')', '\u000b', '0', '\b', '2', 'S', '\u0007', '\u0000',
            '4', '\u001e', '\u0000'};

    private static char[] encryptTable = new char[]{'\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000',
            '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000',
            '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', 'g', ' ',
            '\u0000', '&', 'w', ',', 'l', 'N', 'X', 'O', '\u0000', '7', '.', '%', 'e', '\u0000', '8', '_', ']', '#', 'P', '1', '-',
            '$', 'V', '[', '\u0000', 'Y', '\u0000', '^', '\u0000', '\u0000', 'K', '}', 'j', '0', '@', 'G', 'S', ')', 'A', 'x', 'y',
            '6', '9', 'E', 'F', '{', 'W', 'b', '=', 'R', 'v', 't', 'h', '2', '4', 'M', '(', 'k', '\u0000', 'm', 'a', '+', '~', 'D',
            '\'', 'C', '!', 'J', 'I', 'd', 'B', 'U', '`', 'q', 'f', 'p', 'H', 'Q', '3', 'L', 'n', 'o', 'Z', 'i', 'r', 's', 'u', ';',
            'z', 'c', '\u0000', 'T', '5', '\u0000'};
    private static char[] decryptTable = new char[]{'\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000',
            '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000',
            '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '!', 'd',
            '\u0000', '3', '7', '-', '#', 'b', 'Z', 'G', '\u0000', '_', '%', '6', ',', '\u0000', 'C', '5', 'W', 'p', 'X', '~', 'K',
            '+', '0', 'L', '\u0000', 'y', '\u0000', 'R', '\u0000', '\u0000', 'D', 'H', 'h', 'c', 'a', 'M', 'N', 'E', 'n', 'f', 'e',
            '@', 'q', 'Y', '\'', ')', '4', 'o', 'S', 'F', '}', 'i', '8', 'P', '(', ';', 't', '9', '\u0000', '2', '=', '1', 'j', '^',
            'Q', '{', 'g', '.', 'l', ' ', 'V', 'u', 'B', '[', '&', ']', 'r', 's', 'm', 'k', 'v', 'w', 'U', 'x', 'T', '$', 'I', 'J',
            'z', 'O', '\u0000', 'A', '`', '\u0000'};


    public static boolean isEncoded(String hash) {
        return encode(decode(hash)).equals(hash);
    }

    public static String encode(String name) {
        if (name != null && name.length() > 0) {
            char[] chars;
            String hash;
            int i = 0, encodeSeed;

            int i1;
            for (i1 = 0; i < name.length(); ++i) {
                i1 = name.charAt(i) * 17 + i1 + 1;
            }

            i1 = i + i1 & 31;
            encodeSeed = i1 == 0 ? 32 : i1;

            if (encodeSeed < 0) return null;

            int encodeLoop = encodeSeed;

            chars = name.toCharArray();

            for (i = 0; i < chars.length; ++i) {
                char computeVar = chars[i];

                for (int j = 0; j < encodeLoop; ++j) {
                    computeVar = encryptTable[computeVar];
                }

                encodeLoop = 1 + encodeLoop + 17 * chars[i] & 31;
                if (encodeLoop == 0) {
                    encodeLoop = 32;
                }

                chars[i] = computeVar;
            }

            int medianPt13 = (int) (0.33D * (double) chars.length);
            int medianPt23 = (int) (0.66D * (double) chars.length);
            char val1 = chars[medianPt23];
            char val2 = chars[medianPt13];
            chars[medianPt23] = chars[0];
            chars[medianPt13] = chars[1];
            chars[0] = val1;
            chars[1] = val2;

            int computeVar;
            for (i = 0, computeVar = 0; i < chars.length; ++i) {
                computeVar += chars[i];
            }

            hash = String.valueOf(encryptChar[computeVar % 84]);
            hash += String.copyValueOf(chars);
            hash += String.valueOf(encryptChar[encodeSeed]);

            return hash;
        }
        return null;
    }

    public static String decode(String hash) {
        if (hash != null && hash.length() != 0) {
            char[] chars;
            String name;

            chars = hash.substring(1, hash.length() - 1).toCharArray();

            int medianPt13 = (int) (0.33D * (double) chars.length);
            int medianPt23 = (int) (0.66D * (double) chars.length);
            char val1 = chars[medianPt23];
            char val2 = chars[medianPt13];
            chars[medianPt23] = chars[0];
            chars[medianPt13] = chars[1];
            chars[0] = val1;
            chars[1] = val2;

            int computeLoop = decryptChar[hash.charAt(hash.length() - 1)];
            int hashSize = chars.length;

            for (int i = 0; i < hashSize; ++i) {
                char computeVar = chars[i];

                for (int j = 0; j < computeLoop; ++j) {
                    computeVar = decryptTable[computeVar];
                    if (computeVar == 0) {
                        computeVar = 255;
                    }
                }

                chars[i] = computeVar;
                computeLoop = 1 + computeLoop + 17 * computeVar & 31;
                if (computeLoop == 0) {
                    computeLoop = 32;
                }
            }

            name = String.copyValueOf(chars);

            return name;
        }
        return null;
    }
}
