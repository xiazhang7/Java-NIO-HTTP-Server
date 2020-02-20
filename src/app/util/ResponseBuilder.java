package app.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map.Entry;

import app.res.Contents;
import app.res.HttpHeader;

public class ResponseBuilder {
	private StringBuilder sb;
	private HttpHeader header;
	private Object body;
	private boolean useStreamBody;
	private boolean chunked;
	private int stat;

	private ByteBuffer bb;
	private byte[] bytes;

	private final static int reserve = Integer.toHexString(Contents.BUF_SIZE).length() + 2;
	public final static int INIT = 0, DONE = 1, CONTINUE = 2;
	private final static int NO_CONTENT = 3, HEADER_DONE = 4, CONTENT_INIT = 5, CONTENT_DONE = 6;

	public ResponseBuilder(byte[] bytes, HttpHeader header) {
		this.bytes = bytes;
		this.header = header;
		sb = new StringBuilder();
		stat = DONE;
	}

	public void init(String version, int status, Object body, boolean useStreamBody) {
		this.useStreamBody = useStreamBody;
		chunked = false;
		stat = INIT;
		sb.setLength(0);
		sb.append(version).append(' ').append(StatusCode.get(status)).append("\r\n");
		bb = ByteBuffer.wrap(sb.toString().getBytes());
		if (body == null) {
			header.put("content-length", "0");
			stat = NO_CONTENT;
		} else if (!useStreamBody) {
			try {
				body = ((String) body).getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			header.put("content-length", Integer.toString(((byte[]) body).length));
		} else if (!header.containsKey("content-length")) {
			chunked = true;
			header.put("transfer-encoding", "chunked");
		}
		String s = header.getOrDefault("content-type", "text/plain");
		if (s.startsWith("text") && !s.contains("charset"))
			s += "; charset=utf-8";
		header.put("content-type", s);
		this.body = body;
	}

	public ByteBuffer build() throws IOException {
		if (bb.hasRemaining() || buildHeader() || buildBody())
			return bb;
		if (header.getOrDefault("connection", "").equals("keep-alive"))
			stat = CONTINUE;
		else
			stat = DONE;
		return null;
	}

	private boolean buildHeader() {
		if (stat >= HEADER_DONE)
			return false;
		sb.setLength(0);
		for (Entry<String, String> entry : header.entrySet())
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
		sb.append("\r\n");
		bb = ByteBuffer.wrap(sb.toString().getBytes());
		if (stat == NO_CONTENT)
			stat = CONTENT_DONE;
		else
			stat = HEADER_DONE;
		return true;
	}

	private boolean buildBody() throws IOException {
		if (stat == CONTENT_DONE)
			return false;
		if (useStreamBody) {
			InputStream body = (InputStream) this.body;
			if (stat == HEADER_DONE) {
				stat = CONTENT_INIT;
				if (chunked) {
					bytes[reserve - 2] = '\r';
					bytes[reserve - 1] = '\n';
				}
				bb = ByteBuffer.wrap(bytes);
			}
			int count;
			if (chunked) {
				if ((count = body.read(bytes, reserve, bytes.length - reserve - 2)) > 0) {
					bb.rewind();
					String s = Integer.toHexString(count);
					int i, j;
					for (i = reserve - 3, j = s.length() - 1; j >= 0; i--, j--)
						bytes[i] = (byte) s.charAt(j);
					bb.position(i + 1);
					count += reserve + 2;
					bytes[count - 2] = '\r';
					bytes[count - 1] = '\n';
				} else {
					bytes[0] = '0';
					bytes[1] = bytes[3] = '\r';
					bytes[2] = bytes[4] = '\n';
					count = 5;
					bb.rewind();
					body.close();
					stat = CONTENT_DONE;
				}
				bb.limit(count);
				return true;
			} else if ((count = body.read(bytes)) > 0) {
				bb.rewind();
				bb.limit(count);
				return true;
			}
			body.close();
			return false;
		} else {
			boolean ret = true;
			byte[] body = (byte[]) this.body;
			if (body.length == 0) {
				ret = false;
			} else {
				bb = ByteBuffer.wrap(body);
				stat = CONTENT_DONE;
			}
			return ret;
		}
	}

	public int getStat() {
		return stat;
	}
}