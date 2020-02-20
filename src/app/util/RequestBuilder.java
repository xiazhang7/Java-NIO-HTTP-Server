package app.util;

import java.nio.ByteBuffer;

import app.res.HttpHeader;
import app.res.HttpQuery;

public class RequestBuilder {
	private StringBuilder sb;
	private String method;
	private String uri;
	private String version;
	private HttpHeader header;
	private HttpQuery query;
	private String postData;
	private int stat;

	private int i, offset;
	private byte[] bytes;
	private ByteBuffer bb;
	private String boundary;
	private int len;

	public final static int SUCCESS = 0, DONE = 20, ERROR = 21;
	private final static int INIT = 0, METHOD = 1, FIRST_SPACE = 2, QUERY = 3, SECOND_SPACE = 4, VERSION = 5, COLON = 6,
			SPACE = 7, KEY = 8, VALUE = 9, CR = 10, LF = 11, CR2 = 12, HEADER_DONE = 13, FORM_BODY = 14,
			FBODY_DONE = 15, MULTI_BODY = 16, OTHER_BODY = 17, OBODY_DONE = 18;

	public RequestBuilder(byte[] bytes, HttpHeader header, HttpQuery query) {
		this.bytes = bytes;
		this.header = header;
		this.query = query;
		stat = INIT;
		sb = new StringBuilder();
		bb = ByteBuffer.wrap(bytes);
	}

	public void init() {
		stat = INIT;
		header.clear();
	}

	public int build(int length) {
		if (!buildHeader(length))
			return stat = ERROR;
		if (stat == HEADER_DONE) {
			// System.out.println(method + " " + uri + " " + version);
			String ct = header.get("content-type");
			if (ct != null) {
				if (ct.equals("application/x-www-form-urlencoded")) {
					stat = FORM_BODY;
				} else if (ct.startsWith("multipart/form-data")) {
					stat = MULTI_BODY;
					boundary = ct.split("boundary=", 2)[1];
				} else
					stat = OTHER_BODY;
			} else
				stat = OTHER_BODY;
			offset = 0;
			String len = header.get("content-length");
			if (stat != MULTI_BODY) {
				if (len == null || (this.len = Integer.parseInt(len)) == 0)
					return stat = DONE;
				if (this.len != sb.length())
					return SUCCESS;
			}
		}
		switch (stat) {
		case MULTI_BODY:
			System.out.println("Unsupport multipart/form-data, the boundary is: " + boundary);
			return stat = ERROR;
		case FORM_BODY:
		case OTHER_BODY:
			int remainLength = len - sb.length();
			if (length > remainLength) {
				length = remainLength;
				stat++;
			} else if (remainLength == 0) {
				stat++;
				break;
			}
		case METHOD:
		case QUERY:
		case VERSION:
		case KEY:
		case VALUE:
			sb.append(new String(bytes, offset, length - offset));
		}
		if (stat == FBODY_DONE) {
			QueryUtil.parse(postData = sb.toString(), query);
			return stat = DONE;
		} else if (stat == OBODY_DONE) {
			postData = sb.toString();
			return stat = DONE;
		}
		return SUCCESS;
	}

	private boolean buildHeader(int length) {
		if (stat >= HEADER_DONE)
			return true;
		String key = null;
		for (i = 0; i < length; i++) {
			if (bytes[i] == '\n') {
				if (stat == CR2) {
					stat = HEADER_DONE;
					sb.setLength(0);
					if (++i < length)
						sb.append(new String(bytes, i, length - i));
					return true;
				}
				if (stat != CR)
					return false;
				stat = LF;
			} else if (stat != CR && stat != CR2) {
				if (bytes[i] == '\r') {
					if (stat == LF) {
						stat = CR2;
					} else {
						if (stat == VERSION)
							version = getString();
						else if (stat == VALUE)
							header.put(key, getString());
						else
							return false;
						stat = CR;
					}
				} else if (bytes[i] == ':') {
					if (stat == KEY) {
						key = getString();
						stat = COLON;
					} else
						handleCommonChar();
				} else if (bytes[i] == ' ') {
					if (stat == METHOD) {
						method = getString();
						stat = FIRST_SPACE;
					} else if (stat == QUERY) {
						uri = getString();
						stat = SECOND_SPACE;
					} else if (stat == COLON) {
						stat = SPACE;
					} else
						handleCommonChar();
				} else
					handleCommonChar();
			} else
				return false;
		}
		switch (stat) {
		case METHOD:
		case QUERY:
		case VERSION:
		case KEY:
		case VALUE:
			offset = 0;
		}
		return true;
	}

	private String getString() {
		String string = new String(bytes, offset, i - offset);
		if (sb.length() > 0) {
			sb.append(string);
			String s = sb.toString();
			sb.setLength(0);
			return s;
		} else
			return string;
	}

	private void handleCommonChar() {
		switch (stat) {
		case INIT:
		case FIRST_SPACE:
		case SECOND_SPACE:
			stat++;
			offset = i;
			break;
		case LF:
			stat = KEY;
			offset = i;
			break;
		case SPACE:
			stat = VALUE;
			offset = i;
		}
	}

	public String getMethod() {
		return method;
	}

	public String getURI() {
		return uri;
	}

	public String getVersion() {
		return version;
	}

	public int getStat() {
		return stat;
	}

	public String getPostData() {
		return postData;
	}

	public ByteBuffer getByteBuffer() {
		bb.clear();
		return bb;
	}
}