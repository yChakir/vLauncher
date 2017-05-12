package ma.ychakir.rz.vlauncher.Exceptions;

/**
 * @author Yassine
 */
public class NoPatchException extends Exception {
    private String message;
    private Throwable cause;

    public NoPatchException() {
        this(null, null);
    }

    public NoPatchException(Throwable cause) {
        this(cause, null);
    }

    public NoPatchException(String message) {
        this(null, message);
    }

    public NoPatchException(Throwable cause, String message) {
        setCause(cause);
        setMessage(message);
    }

    @Override
    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        if (message == null || "".equals(message))
            this.message = "The pack have no patches.";
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
