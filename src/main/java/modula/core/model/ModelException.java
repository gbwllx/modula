package modula.core.model;

/**
 * @description: ModelException
 * @author: gubing.gb
 * @date: 2017/1/1.
 */
public class ModelException extends Exception {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see java.lang.Exception#Exception()
     */
    public ModelException() {
        super();
    }

    /**
     * @param message the detail message
     * @see java.lang.Exception#Exception(java.lang.String)
     */
    public ModelException(final String message) {
        super(message);
    }

    /**
     * @param cause the cause
     * @see java.lang.Exception#Exception(java.lang.Throwable)
     */
    public ModelException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message the detail message
     * @param cause   the cause
     * @see java.lang.Exception#Exception(String, java.lang.Throwable)
     */
    public ModelException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
