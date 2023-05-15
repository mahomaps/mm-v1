package org.json.me;

public class JSONException extends RuntimeException {
	private Throwable cause;

	public JSONException(String var1) {
		super(var1);
	}

	public JSONException(Throwable var1) {
		super(var1.getMessage());
		this.cause = var1;
	}

	public Throwable getCause() {
		return this.cause;
	}

}
