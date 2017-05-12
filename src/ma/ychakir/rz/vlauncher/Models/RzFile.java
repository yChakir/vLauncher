package ma.ychakir.rz.vlauncher.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * @author Yassine
 */
public class RzFile {

    @SerializedName("Name")
    @Expose
    private String name;

    @SerializedName("Sum")
    @Expose
    private String sum;

    @SerializedName("Zip")
    @Expose
    private String zip;

    public RzFile() {
    }

    public RzFile(String name, String sum, String zip) {
        setName(name);
        setSum(sum);
        setZip(zip);
    }

    public RzFile(RzFile rzFile) {
        this(rzFile.name, rzFile.sum, rzFile.zip);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (!(obj instanceof RzFile))
            return false;

        RzFile other = (RzFile) obj;

        return Objects.equals(this.getSum(), other.getSum()) &&
                Objects.equals(this.getName(), other.getName());
    }

    @Override
    public String toString() {
        return String.format("Name: %s%n" +
                        "Sum: %s%n" +
                        "Zip: %s%n",
                getName(),
                getSum(),
                getZip());
    }


}
