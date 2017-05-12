package ma.ychakir.rz.vlauncher.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import ma.ychakir.rz.vlauncher.Exceptions.NoUrlException;

import java.util.List;
import java.util.Map;

/**
 * @author Yassine
 */
public class Pack {

    @SerializedName("Url")
    @Expose
    private String url;

    @SerializedName("Patches")
    @Expose
    private Map<String, List<RzFile>> patches;

    public Pack(String url, Map<String, List<RzFile>> patches) throws NoUrlException {
        setUrl(url);
        setPatches(patches);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) throws NoUrlException {
        if (url == null || "".equals(url))
            throw new NoUrlException();
        else
            this.url = url;
    }

    public Map<String, List<RzFile>> getPatches() {
        return patches;
    }

    public void setPatches(Map<String, List<RzFile>> patches) {
        this.patches = patches;
    }
}
