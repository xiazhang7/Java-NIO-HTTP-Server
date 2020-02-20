package app.res;

import java.util.HashMap;

public class HttpHeader extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	@Override
	public String put(String key, String value) {
		return super.put(key.toLowerCase(), value);
	}

	public String get(String key) {
		return super.get(key.toLowerCase());
	}

	public void remove(String key) {
		super.remove(key.toLowerCase());
	}
}