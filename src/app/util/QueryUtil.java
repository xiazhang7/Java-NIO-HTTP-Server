package app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map.Entry;

import app.res.HttpQuery;

public class QueryUtil {
	public static boolean parse(String queryString, HttpQuery query) {
		return parse(queryString, query, "&");
	}

	public static boolean parse(String queryString, HttpQuery query, String separator) {
		for (String s : queryString.split(separator)) {
			String q[] = s.split("=");
			try {
				if (q.length != 2)
					throw new IllegalArgumentException();
				query.put(URLDecoder.decode(q[0], "UTF-8"), URLDecoder.decode(q[1], "UTF-8"));
			} catch (Exception e) {
				query.clear();
				return false;
			}
		}
		return true;
	}

	public static String build(HttpQuery query) throws UnsupportedEncodingException {
		return build(query, "&");
	}

	public static String build(HttpQuery query, String separator) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (Entry<String, String> entry : query.entrySet()) {
			if (isFirst)
				isFirst = false;
			else
				sb.append(separator);
			sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			sb.append("=");
			sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return sb.toString();
	}
}