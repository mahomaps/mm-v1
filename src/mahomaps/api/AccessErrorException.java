package mahomaps.api;

/**
 * Thrown on 401/403 error. Usually indicates outdated/broken access token.
 */
public class AccessErrorException extends Exception {
	public AccessErrorException() {
	}
}
