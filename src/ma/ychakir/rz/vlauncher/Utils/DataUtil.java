package ma.ychakir.rz.vlauncher.Utils;

import ma.ychakir.rz.vlauncher.Exceptions.DateCorruptedException;
import ma.ychakir.rz.vlauncher.Models.DataIndex;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Yassine
 */
public class DataUtil {
    private Map<String, DataIndex> data;
    private String clientPath;

    private static char[] cipherTable = new char[]{'w', 'è', '^', 'ì', '·', 'N', 'Á', '\u0087', 'O', 'æ', 'õ', '<', '\u001f', '³', '\u0015', 'C', 'j',
            'I', '0', '¦', '¿', 'S', '¨', '5', '[', 'å', '\u009e', '\u000e', 'A', 'ì', '"', '¸', 'Ô', '\u0080', '¤', '\u008c', 'Î', 'e',
            '\u0013', '\u001d', 'K', '\b', 'Z', 'j', '»', 'o', '\u00ad', '%', '¸', 'Ý', 'Ì', 'w', '0', 't', '¬', '\u008c', 'Z', 'J',
            '\u009a', '\u009b', '6', '¼', 'S', '\n', '<', 'ø', '\u0096', '\u000b', ']', 'ª', '(', '©', '²', '\u0082', '\u0013', 'n',
            'ñ', 'Á', '\u0093', '©', '\u009e', '_', ' ', 'Ï', 'Ô', 'Ì', '[', '.', '\u0016', 'õ', 'É', 'L', '²', '\u001c', 'W', 'î',
            '\u0014', 'í', 'ù', 'r', '\u0097', '"', '\u001b', 'J', '¤', '.', '¸', '\u0096', 'ï', 'K', '?', '\u008e', '«', '`', ']',
            '\u007f', ',', '¸', '\u00ad', 'C', '\u00ad', 'v', '\u008f', '_', '\u0092', 'æ', 'N', '§', 'Ô', 'G', '\u0019', 'k', 'i',
            '4', 'µ', '\u000e', 'b', 'm', '¤', 'R', '¹', 'ã', 'à', 'd', 'C', '=', 'ã', 'p', 'õ', '\u0090', '³', '¢', '\u0006', 'B',
            '\u0002', '\u0098', ')', 'P', '?', 'ý', '\u0097', 'X', 'h', '\u0001', '\u008c', '\u001e', '\u000f', 'ï', '\u008b', '³',
            'A', 'D', '\u0096', '!', '¨', 'Ú', '^', '\u008b', 'J', 'S', '\u001b', 'ý', 'õ', '!', '?', '÷', 'º', 'h', 'G', 'ù', 'e',
            'ß', 'R', 'Î', 'à', 'Þ', 'ì', 'ï', 'Í', 'w', '¢', '\u000e', '¼', '8', '/', 'd', '\u0012', '\u008d', 'ð', '\\', 'à', '\u000b',
            'Y', 'Ö', '-', '\u0099', 'Í', 'ç', '\u0001', '\u0015', 'à', 'g', 'ô', '2', '5', 'Ô', '\u0011', '!', 'Ã', 'Þ', '\u0098', 'e',
            'í', 'T', '\u009d', '\u001c', '¹', '°', 'ª', '©', '\f', '\u008a', '´', 'f', '`', 'á', 'ÿ', '.', 'È', '\u0000', 'C', '©', 'g',
            '7', 'Û', '\u009c'};

    public DataUtil(String clientPath) {
        try {
            data = new LinkedHashMap<>();
            open(clientPath + File.separator + "data.000");
        } catch (Exception e) {
            data = new LinkedHashMap<>();
        } finally {
            this.clientPath = clientPath;
        }
    }

    @Override
    public void finalize() {
        this.clientPath = null;
        this.data = null;
    }

    private boolean isEncrypted(String name) {
        return !name.endsWith(".dds") && !name.endsWith(".cob") && !name.endsWith(".naf") && !name.endsWith(".nx3") && !name.endsWith(".nfm") && !name.endsWith(".tga");
    }

    private void cipher(ByteBuffer buffer, byte index) {
        for (int i = 0; i < buffer.limit(); i++, ++index) {
            buffer.put(i, (byte) (buffer.get(i) ^ cipherTable[unsignedToBytes(index)]));
        }
    }

    private byte[] cipher(byte[] buffer, byte index) {
        return cipher(buffer, index, buffer.length);
    }

    private byte[] cipher(byte[] buffer, byte index, int length) {
        for (int i = 0; i < length; i++, ++index) {
            buffer[i] ^= cipherTable[unsignedToBytes(index)];
        }
        return buffer;
    }

    private byte cipher(byte buffer, byte index) {
        buffer ^= cipherTable[unsignedToBytes(index)];
        return buffer;
    }

    private int toInt(byte[] b) {
        int MASK = 0xFF;
        int result;
        result = b[0] & MASK;
        result = result + ((b[1] & MASK) << 8);
        result = result + ((b[2] & MASK) << 16);
        result = result + ((b[3] & MASK) << 24);
        return result;
    }

    private int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    private int getDataID(String hash) {
        byte[] bytes = hash.toLowerCase().getBytes();
        int num = 0;

        for (byte aByte : bytes) {
            num = (num << 5) - num + aByte;
        }

        if (num < 0) {
            num *= -1;
        }

        return num % 8 + 1;
    }

    private void open(String dataPath) throws DateCorruptedException, IOException {
        RandomAccessFile reader = new RandomAccessFile(dataPath, "r");
        int cipherIndex = 0;
        byte strlen;
        byte[] buffer = new byte[255];
        while (cipherIndex < reader.length()) {
            DataIndex index = new DataIndex();
            strlen = cipher(reader.readByte(), (byte) cipherIndex++);
            reader.read(buffer, 0, strlen + 8);
            buffer = cipher(buffer, (byte) cipherIndex, strlen + 8);

            cipherIndex += strlen + 8;

            index.setHash(new String(buffer, 0, strlen));
            index.setName(EncodingUtil.decode(index.getHash()));
            index.setOffset(ByteBuffer.wrap(buffer, strlen, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            index.setSize(ByteBuffer.wrap(buffer, strlen + 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            index.setId(getDataID(index.getHash()));

            data.put(index.getName(), index);
        }
        IOUtils.closeQuietly(reader);
    }

    public void save() throws IOException {
        save(clientPath);
    }

    public void save(String clientPath) throws IOException {
        File client = new File(clientPath);
        if (client.isDirectory() && (client.exists() || client.mkdirs())) {
            RandomAccessFile writer = new RandomAccessFile(clientPath + File.separator + "data.000", "rw");
            byte index = 0;
            for (Map.Entry<String, DataIndex> element : data.entrySet()) {
                DataIndex dataIndex = element.getValue();

                int hashLength = dataIndex.getHash().length();
                ByteBuffer buffer = ByteBuffer.allocate(9 + unsignedToBytes((byte) hashLength))
                        .order(ByteOrder.LITTLE_ENDIAN);

                buffer.put((byte) dataIndex.getHash().length());
                buffer.put(dataIndex.getHash().getBytes());
                buffer.putInt(dataIndex.getOffset());
                buffer.putInt(dataIndex.getSize());

                cipher(buffer, index);
                index += 9 + hashLength;
                writer.write(buffer.array());
                buffer.clear();
            }
            IOUtils.closeQuietly(writer);
        }
    }

    public void addFile(String filePath) throws IOException {
        addFile(filePath, clientPath);
    }

    public void addFile(String filePath, String clientPath) throws IOException {
        File file = new File(filePath);
        File client = new File(clientPath);

        if (file.exists() && client.isDirectory() && (client.exists() || client.mkdirs())) {
            String hash = EncodingUtil.isEncoded(file.getName()) ? file.getName() : EncodingUtil.encode(file.getName());
            String name = EncodingUtil.decode(hash);

            DataIndex index;
            boolean bIsNew = !data.containsKey(name);
            if (bIsNew) {
                index = new DataIndex();
                index.setHash(hash);
                index.setName(name);
                index.setId(getDataID(hash));
            } else {
                index = data.get(name);
                data.remove(name, index);
            }

            File dataFile = new File(clientPath + File.separator + "data.00" + index.getId());

            if (bIsNew || file.length() > index.getSize() || index.getOffset() > dataFile.length() - file.length()) {
                index.setOffset((int) dataFile.length());
            }
            index.setSize((int) file.length());

            RandomAccessFile writer = new RandomAccessFile(dataFile, "rw");
            FileInputStream reader = new FileInputStream(file);
            writer.seek(index.getOffset());

            byte[] buffer = new byte[1024];
            int read;
            if (isEncrypted(name)) {
                int dataIndex = 0;
                while ((read = reader.read(buffer)) != -1) {
                    writer.write(cipher(buffer, (byte) dataIndex), 0, read);
                    dataIndex += buffer.length;
                }
            } else {
                while ((read = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, read);
                }
            }

            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(reader);
            data.put(name, index);
        }
    }

    public String getCRC32(String name) throws IOException {
        String sum = null;
        String hash = EncodingUtil.isEncoded(name) ? name : EncodingUtil.encode(name);
        name = EncodingUtil.decode(hash);

        int dataID = getDataID(hash);
        File dataFile = new File(clientPath + File.separator + "data.00" + dataID);

        if (data.containsKey(name) && dataFile.exists()) {
            Checksum checksum = new CRC32();
            DataIndex index = data.get(name);
            byte[] buffer = new byte[1024];

            RandomAccessFile reader = new RandomAccessFile(dataFile, "r");
            reader.seek(index.getOffset());

            int read, indexData = 0, size = index.getSize();
            if (isEncrypted(name)) {
                do {
                    read = reader.read(buffer);
                    checksum.update(cipher(buffer, (byte) indexData), 0, indexData + read <= size ? read : size - indexData);
                    indexData += read;
                } while (read == 1024 && indexData < size);
            } else {
                do {
                    read = reader.read(buffer);
                    checksum.update(buffer, 0, indexData + read <= size ? read : size - indexData);
                    indexData += read;
                } while (read == 1024 && indexData < size);
            }

            reader.close();

            sum = Long.toHexString(checksum.getValue()).toUpperCase();
        }
        return sum;
    }

}
