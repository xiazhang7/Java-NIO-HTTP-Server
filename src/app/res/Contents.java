package app.res;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import app.core.MiddleWare;
import app.res.HttpHeader;
import app.res.HttpQuery;
import app.util.QueryUtil;
import app.util.RequestBuilder;
import app.util.ResponseBuilder;

public class Contents {
	public final static int BUF_SIZE = 4096;

	private String method;
	private String uri;
	private String path;
	private String queryString;
	private HttpQuery getQuery;
	private HttpQuery postQuery;
	private String postData;
	private String reqVersion;
	private HttpHeader reqHeaders;

	private String resVersion;
	private int status;
	private HttpHeader resHeaders;
	private boolean useStreamBody;
	private Object body;

	private RequestBuilder requestBuilder;
	private ResponseBuilder responseBuilder;
	private SocketChannel socketChannel;
	private byte[] bytes;

	private List<MiddleWare> middleWareList;
	private static List<Contents> contentsPool = Collections.synchronizedList(new ArrayList<>());

	private Contents(List<MiddleWare> middleWareList) {
		this.middleWareList = middleWareList;
		bytes = new byte[BUF_SIZE];
		getQuery = new HttpQuery();
		postQuery = new HttpQuery();
		reqHeaders = new HttpHeader();
		resHeaders = new HttpHeader();
		requestBuilder = new RequestBuilder(bytes, reqHeaders, postQuery);
		responseBuilder = new ResponseBuilder(bytes, resHeaders);
	}

	public static Contents get(List<MiddleWare> middleWareList, SocketChannel socketChannel) {
		Contents contents;
		if (contentsPool.isEmpty()) {
			contents = new Contents(middleWareList);
		} else {
			contents = contentsPool.get(0);
			contentsPool.remove(0);
		}
		contents.socketChannel = socketChannel;
		return contents;
	}

	public void back() {
		if (useStreamBody) {
			try {
				((InputStream) body).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		contentsPool.add(this);
	}

	private void init() {
		method = requestBuilder.getMethod();
		uri = requestBuilder.getURI();
		postData = requestBuilder.getPostData();
		reqVersion = requestBuilder.getVersion();
		resVersion = reqVersion;
		getQuery.clear();
		resHeaders.clear();
		body = null;
		useStreamBody = false;
		String u[] = uri.split("\\?", 2);
		try {
			path = URLDecoder.decode(u[0], "UTF-8");
			status = 404;
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			status = 400;
			path = "";
		}
		if (u.length == 2) {
			queryString = u[1];
			QueryUtil.parse(queryString, getQuery);
		} else
			queryString = null;
		String string = reqHeaders.getOrDefault("connection", "");
		if (string.equals("keep-alive") || (reqVersion.equals("HTTP/1.1") && !string.equals("close")))
			resHeaders.put("connection", "keep-alive");
		else
			resHeaders.put("connection", "close");
		processMiddleWare();
	}

	private void processMiddleWare() {
		Iterator<MiddleWare> it = middleWareList.iterator();
		while (it.hasNext())
			it.next().handle(this);
	}

	public RequestBuilder getRequestBuilder() {
		switch (requestBuilder.getStat()) {
		case RequestBuilder.DONE:
		case RequestBuilder.ERROR:
			requestBuilder.init();
		}
		return requestBuilder;
	}

	public ResponseBuilder getResponseBuilder() {
		switch (responseBuilder.getStat()) {
		case ResponseBuilder.DONE:
		case ResponseBuilder.CONTINUE:
			init();
			responseBuilder.init(resVersion, status, body, useStreamBody);
		}
		return responseBuilder;
	}

	public SocketChannel channel() {
		return socketChannel;
	}

	public void setBody(InputStream file) {
		setBody(file, true);
	}

	public void setBody(InputStream file, boolean setStatus) {
		useStreamBody = true;
		body = file;
		if (setStatus)
			status = 200;
	}

	public void setBody(String s) {
		setBody(s, true);
	}

	public void setBody(String s, boolean setStatus) {
		useStreamBody = false;
		body = s;
		if (setStatus)
			status = 200;
	}

	public Object getBody() {
		return body;
	}

	public boolean isUseStreamBody() {
		return useStreamBody;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getReqHeader(String key) {
		return reqHeaders.get(key);
	}

	public HttpHeader getReqHeader() {
		return reqHeaders;
	}

	public String getResHeader(String key) {
		return resHeaders.get(key);
	}

	public HttpHeader getResHeader() {
		return resHeaders;
	}

	public void setResHeader(String key, String value) {
		resHeaders.put(key, value);
	}

	public void delResHeader(String key) {
		resHeaders.remove(key);
	}

	public String getMethod() {
		return method;
	}

	public String getReqVersion() {
		return reqVersion;
	}

	public String getResVersion() {
		return resVersion;
	}

	public void setResVersion(String version) {
		this.resVersion = version;
	}

	public String getGetQuery(String key) {
		return getQuery.get(key);
	}

	public HttpQuery getGetQuery() {
		return getQuery;
	}

	public String getPostQuery(String key) {
		return postQuery.get(key);
	}

	public HttpQuery getPostQuery() {
		return postQuery;
	}

	public String getURI() {
		return uri;
	}

	public String getPath() {
		return path;
	}

	public String getQueryString() {
		return queryString;
	}

	public String getPostData() {
		return postData;
	}
}