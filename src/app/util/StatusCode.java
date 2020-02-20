package app.util;

import java.util.HashMap;

public class StatusCode {
	private static HashMap<Integer, String> hm = new HashMap<>();

	static {
		hm.put(100, "100 Continue");
		hm.put(101, "101 Switching Protocols");
		hm.put(102, "102 Processing");
		hm.put(103, "103 Checkpoint");
		hm.put(200, "200 OK");
		hm.put(201, "201 Created");
		hm.put(202, "202 Accepted");
		hm.put(203, "203 Non-Authoritative Information");
		hm.put(204, "204 No Content");
		hm.put(205, "205 Reset Content");
		hm.put(206, "206 Partial Content");
		hm.put(207, "207 Multi-Status");
		hm.put(208, "208 Already Reported");
		hm.put(226, "226 IM Used");
		hm.put(300, "300 Multiple Choices");
		hm.put(301, "301 Moved Permanently");
		hm.put(302, "302 Found");
		hm.put(302, "302 Moved Temporarily");
		hm.put(303, "303 See Other");
		hm.put(304, "304 Not Modified");
		hm.put(305, "305 Use Proxy");
		hm.put(307, "307 Temporary Redirect");
		hm.put(308, "308 Permanent Redirect");
		hm.put(400, "400 Bad Request");
		hm.put(401, "401 Unauthorized");
		hm.put(402, "402 Payment Required");
		hm.put(403, "403 Forbidden");
		hm.put(404, "404 Not Found");
		hm.put(405, "405 Method Not Allowed");
		hm.put(406, "406 Not Acceptable");
		hm.put(407, "407 Proxy Authentication Required");
		hm.put(408, "408 Request Timeout");
		hm.put(409, "409 Conflict");
		hm.put(410, "410 Gone");
		hm.put(411, "411 Length Required");
		hm.put(412, "412 Precondition Failed");
		hm.put(413, "413 Payload Too Large");
		hm.put(413, "413 Request Entity Too Large");
		hm.put(414, "414 URI Too Long");
		hm.put(414, "414 Request-URI Too Long");
		hm.put(415, "415 Unsupported Media Type");
		hm.put(416, "416 Requested range not satisfiable");
		hm.put(417, "417 Expectation Failed");
		hm.put(418, "418 I'm a teapot");
		hm.put(419, "419 Insufficient Space On Resource");
		hm.put(420, "420 Method Failure");
		hm.put(421, "421 Destination Locked");
		hm.put(422, "422 Unprocessable Entity");
		hm.put(423, "423 Locked");
		hm.put(424, "424 Failed Dependency");
		hm.put(426, "426 Upgrade Required");
		hm.put(428, "428 Precondition Required");
		hm.put(429, "429 Too Many Requests");
		hm.put(431, "431 Request Header Fields Too Large");
		hm.put(451, "451 Unavailable For Legal Reasons");
		hm.put(500, "500 Internal Server Error");
		hm.put(501, "501 Not Implemented");
		hm.put(502, "502 Bad Gateway");
		hm.put(503, "503 Service Unavailable");
		hm.put(504, "504 Gateway Timeout");
		hm.put(505, "505 HTTP Version not supported");
		hm.put(506, "506 Variant Also Negotiates");
		hm.put(507, "507 Insufficient Storage");
		hm.put(508, "508 Loop Detected");
		hm.put(509, "509 Bandwidth Limit Exceeded");
		hm.put(510, "510 Not Extended");
		hm.put(511, "511 Network Authentication Required");
	}

	public static String get(int code) {
		return hm.get(code);
	}
}