package ma.ychakir.rz.vlauncher.Runnables;

import com.google.gson.Gson;
import ma.ychakir.rz.vlauncher.Launcher;
import ma.ychakir.rz.vlauncher.Models.Pack;
import ma.ychakir.rz.vlauncher.Models.RzFile;
import ma.ychakir.rz.vlauncher.Utils.DataUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * @author Yassine
 */
public class UpdateRunnable implements Runnable {
    private static final Logger logger = Launcher.getLogger();
    private final String currentDirectory = System.getProperty("user.dir");
    private final String version;
    private DataUtil dataUtil = null;

    private Status status = Status.SEARSHING;
    private int totalCount;
    private int currentCount;
    private double totalProgress;
    private String currentName;
    private double currentProgress;

    public enum Status {
        SEARSHING,
        ALREADY_UPDATED,
        UPDATING,
        CLEANING,
        FINISHED
    }

    public int getCurrentCount() {
        return currentCount;
    }

    private void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public Status getStatus() {
        return status;
    }

    private void setStatus(Status status) {
        this.status = status;
    }

    public int getTotalCount() {
        return totalCount;
    }

    private void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public double getTotalProgress() {
        return totalProgress;
    }

    private void setTotalProgress(double totalProgress) {
        this.totalProgress = totalProgress;
    }

    public String getCurrentName() {
        return currentName;
    }

    private void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    public double getCurrentProgress() {
        return currentProgress;
    }

    private void setCurrentProgress(double currentProgress) {
        this.currentProgress = currentProgress;
    }

    public UpdateRunnable(String version) {
        this.version = version;
    }

    @Override
    public void run() {
        logger.debug("Update worker started.");
        Pack newPack = new Gson().fromJson(version, Pack.class);
        Map<String, List<RzFile>> updates = new LinkedHashMap<>();
        final String url = newPack.getUrl();
        final int totalCount = getNewUpdates(newPack, updates);
        int current = 0;

        if (totalCount > 0) {
            setStatus(Status.UPDATING);
            setTotalCount(totalCount);

            for (Map.Entry<String, List<RzFile>> map : updates.entrySet()) {
                String s = map.getKey();
                for (RzFile rzFile : map.getValue()) {
                    //download zip and extract file
                    if (download(url, rzFile) && install(s, rzFile)) {
                        setCurrentCount(++current);
                        setTotalProgress(((double) current * 100.0d) / totalCount);
                    } else {
                        logger.error("Failed to install new update " + rzFile.getName());
                    }
                }
            }

            try {
                if (dataUtil != null)
                    //save data.000
                    dataUtil.save();

                setStatus(Status.CLEANING);
                //delete temp directory
                FileUtils.deleteDirectory(new File(currentDirectory + "/Resource/temp"));

                //clean resource directory
                cleanResource(newPack);

                setStatus(Status.FINISHED);
                logger.debug("Update finished.");
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else {
            setStatus(Status.ALREADY_UPDATED);
        }
    }

    private int getNewUpdates(Pack newPack, Map<String, List<RzFile>> updates) {
        int totalCount = 0;
        //get files to download and install
        for (Map.Entry<String, List<RzFile>> map : newPack.getPatches().entrySet()) {
            String s = map.getKey();
            for (RzFile rzFile : map.getValue()) {
                try {
                    logger.debug("Check if exists: " + rzFile.getName());
                    String crc32 = null;
                    File file;
                    switch (s) {
                        case "<Data>":
                            dataUtil = dataUtil == null ? new DataUtil(currentDirectory) : dataUtil;
                            crc32 = dataUtil.getCRC32(rzFile.getName());
                            break;
                        case "<Resource>":
                            file = new File(currentDirectory + "/Resource/" + rzFile.getName());
                            if (file.exists())
                                crc32 = Long.toHexString(FileUtils.checksumCRC32(file)).toUpperCase();
                            break;
                        case "<Current>":
                            file = new File(currentDirectory + File.separator + rzFile.getName());
                            if (file.exists())
                                crc32 = Long.toHexString(FileUtils.checksumCRC32(file)).toUpperCase();
                            break;
                        default:
                            file = new File(currentDirectory + File.separator + s + File.separator + rzFile.getName());
                            if (file.exists())
                                crc32 = Long.toHexString(FileUtils.checksumCRC32(file)).toUpperCase();
                            break;
                    }

                    if (crc32 == null || !crc32.equals(rzFile.getSum())) {
                        logger.debug("Do not exists: " + rzFile.getSum() + "!=" + crc32);
                        updates.computeIfAbsent(s, k -> new ArrayList<>());
                        updates.get(s).add(rzFile);
                        totalCount++;
                    } else {
                        logger.debug("Already exists.");
                    }
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }
        return totalCount;
    }

    private boolean download(String url, RzFile rzFile) {
        RandomAccessFile raf = null;
        InputStream stream = null;
        try {
            URL rzFileUrl = new URL(url + "/patches/" + rzFile.getZip() + ".zip");
            logger.debug("Downloading: " + rzFile.getName() + " from: " + rzFileUrl);
            HttpURLConnection connection = (HttpURLConnection) rzFileUrl.openConnection();
            connection.connect();
            int response = connection.getResponseCode();
            if (response != 200) {
                logger.error("Failed to open connection to: " + rzFileUrl + " code: " + response);
                return false;
            }

            final int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                logger.error("Invalid file size: " + contentLength);
                return false;
            } else {
                setCurrentName(rzFile.getName());
                setCurrentProgress(0);
            }

            String tempPath = currentDirectory + "/Resource/temp";
            File temp = new File(tempPath);

            if (temp.exists() || temp.mkdirs()) {
                raf = new RandomAccessFile(tempPath + File.separator + rzFile.getZip(), "rw");
                stream = connection.getInputStream();

                byte buffer[] = new byte[1024];
                int read;
                double progress = 0;
                while ((read = stream.read(buffer)) != -1) {
                    raf.write(buffer, 0, read);
                    progress += read;
                    setCurrentProgress((progress * 100.0d) / contentLength);
                }

                if (progress == contentLength) {
                    logger.debug("Download succeeded.");
                } else {
                    logger.error("Download incomplete: " + progress + "/" + contentLength);
                }

                return true;
            } else {
                logger.error("Failed to find/create temp folder.");
                return false;
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            return false;
        } finally {
            IOUtils.closeQuietly(raf);
            IOUtils.closeQuietly(stream);
        }
    }

    private boolean install(String dest, RzFile rzFile) {
        String source = currentDirectory + "/Resource/temp/" + rzFile.getZip();
        String destination;
        try {
            switch (dest) {
                case "<Data>":
                    dataUtil = dataUtil == null ? new DataUtil(currentDirectory) : dataUtil;
                    destination = currentDirectory + "/Resource/temp/" + rzFile.getName();
                    if (unZip(source, destination)) {
                        logger.debug("Patcing to client data: " + rzFile.getName());
                        dataUtil.addFile(destination);
                    } else {
                        logger.error("Failed to extract file: source: " + source + " destination: " + dest);
                        return false;
                    }
                    break;
                case "<Resource>":
                    destination = currentDirectory + "/Resource/" + rzFile.getName();
                    if (!unZip(source, destination)) {
                        logger.error("Failed to extract file: source: " + source + " destination: " + dest);
                        return false;
                    }
                    break;
                case "<Current>":
                    destination = currentDirectory + File.separator + rzFile.getName();
                    if (!unZip(source, destination)) {
                        logger.error("Failed to extract file: source: " + source + " destination: " + dest);
                        return false;
                    }
                    break;
                default:
                    destination = currentDirectory + File.separator + dest + File.separator + rzFile.getName();
                    if (!unZip(source, destination)) {
                        logger.error("Failed to extract file: source: " + source + " destination: " + dest);
                        return false;
                    }
                    break;
            }
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }

    }

    private boolean unZip(String source, String dest) {
        OutputStream out = null;
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        ZipInputStream zin = null;
        try {
            logger.debug("Extracting " + dest + " from " + source);
            File parent = new File(dest).getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                logger.error("Failed to find/create " + dest);
                return false;
            }

            //extract file
            fin = new FileInputStream(source);
            out = new FileOutputStream(dest);
            bin = new BufferedInputStream(fin);
            zin = new ZipInputStream(bin);
            zin.getNextEntry();

            byte buffer[] = new byte[1024];
            int read;
            while ((read = zin.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fin);
            IOUtils.closeQuietly(zin);
            IOUtils.closeQuietly(bin);
        }
    }

    private void cleanResource(Pack newPack) {
        String resourcePath = currentDirectory + "/Resource";
        File resource = new File(resourcePath);
        String[] files = resource.list();
        List<RzFile> rzFiles = newPack.getPatches().get("<Resource>");

        logger.debug("Cleaning resource folder " + resourcePath);
        if ((rzFiles == null || rzFiles.size() <= 0) && resource.exists()) {
            try {
                FileUtils.deleteDirectory(resource);
                resource.mkdirs();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else if (files != null) {
            for (String name : files) {
                boolean exists = false;
                for (RzFile rzFile : rzFiles) {
                    if (name.equals(rzFile.getName())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists)
                    try {
                        File del = new File(resourcePath + File.separator + name);
                        if (del.isDirectory())
                            FileUtils.deleteDirectory(del);
                        else
                            FileUtils.deleteQuietly(del);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
            }
        } else {
            resource.mkdirs();
        }
    }
}