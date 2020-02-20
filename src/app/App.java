package app;

import app.middleware.Static;
import app.res.Contents;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import app.core.MiddleWare;
import app.core.Server;

public class App {
	public static void main(String[] args) throws Exception {
		Server s = new Server(8080);
		s.use(new Static("./static"));
		s.use(new MiddleWare() {  // Just for test
			@Override
			public void handle(Contents ctx) {
				switch (ctx.getPath()) {
				case "/info":
					ctx.setBody("version: " + ctx.getReqVersion() + "\nmethod: " + ctx.getMethod() + "\nuri: "
							+ ctx.getURI() + "\npath: " + ctx.getPath() + "\nquery string: " + ctx.getQueryString()
							+ "\nget query: " + ctx.getGetQuery() + "\npost: " + ctx.getPostData() + "\npost query: "
							+ ctx.getPostQuery() + "\nrequest headers: " + ctx.getReqHeader());
					break;
				case "/filetest":  // chunked test
					try {
						ctx.setBody(new FileInputStream("static/test.jpg"));
						ctx.setResHeader("content-type", "image/jpeg");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		});
		s.listen(true);
	}
}