package ma.ychakir.rz.vlauncher.Exceptions;

/**
 * @author Yassine
 */
public class DateCorruptedException extends Exception {
    private String message;
    private Throwable cause;

    @Override
    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        if (message == null || "".equals(message))
            this.message = "The file Data.000 is corrupted.";
        else
            this.message = message;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    private void setCause(Throwable cause) {
        this.cause = cause;
    }

    public DateCorruptedException() {
        this(null, null);
    }

    public DateCorruptedException(Throwable cause) {
        this(cause, null);
    }

    public DateCorruptedException(String message) {
        this(null, message);
    }

    public DateCorruptedException(Throwable cause, String message) {
        setCause(cause);
        setMessage(message);
    }
}
