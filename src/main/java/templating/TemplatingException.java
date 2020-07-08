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
	 * @param message
	 */
	public TemplatingException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param cause
	 */
	public TemplatingException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * @param message
	 * @param cause
	 */
	public TemplatingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TemplatingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
