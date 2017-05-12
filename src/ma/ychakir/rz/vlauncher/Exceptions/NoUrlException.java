package ma.ychakir.rz.vlauncher.Exceptions;

/**
 * @author Yassine
 */
public class NoUrlException extends Exception {
    private String message;
    private Throwable cause;

    public NoUrlException() {
        this(null, null);
    }

    public NoUrlException(Throwable cause) {
        this(cause, null);
    }

    public NoUrlException(String message) {
        this(null, message);
    }

    public NoUrlException(Throwable cause, String message) {
        setCause(cause);
        setMessage(message);
    }

    @Override
    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        if (message == null || "".equals(message))
            this.message = "The pack have no download url.";
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
}
