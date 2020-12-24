package templating;

/**
 * A runtime exception.
 * @author ralph
 *
 */
public class TemplatingException extends RuntimeException {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public TemplatingException() {
	}

	/**
	 * Constructor.
	 * @param message - message for exception
	 */
	public TemplatingException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param cause - root cause of exception
	 */
	public TemplatingException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * @param message - message for exception
	 * @param cause - root cause of exception
	 */
	public TemplatingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * @param message - message for exception
	 * @param cause - root cause of exception
	 * @param enableSuppression - whether or not suppression is enabled or disabled
	 * @param writableStackTrace - whether or not the stack trace should be writable
	 */
	public TemplatingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
