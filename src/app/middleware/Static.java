package app.middleware;

import app.core.MiddleWare;
import app.res.Contents;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;

import javax.activation.MimetypesFileTypeMap;

public class Static implements MiddleWare {
	private final static MimetypesFileTypeMap mftm = new MimetypesFileTypeMap();

	String path;
	String indexFile;

	public Static(String path) {
		this(path, "index.html");
	}

	public Static(String path, String indexFile) {
		this.path = new File(path).getAbsolutePath();
		this.indexFile = "/" + indexFile;
	}

	@Override
	public void handle(Contents ctx) {
		if (ctx.getBody() != null || ctx.getStatus() != 404)
			return;
		String p = path + ctx.getPath();
		File file = new File(p);
		if (!file.exists())
			return;
		if (file.isDirectory()) {
			file = new File(p + indexFile);
		}
		if (!file.canRead()) {
			ctx.setStatus(403);
			return;
		}
		String s = ctx.getReqHeader("if-modified-since");
		String date = new Date(file.lastModified()).toString();
		ctx.setResHeader("last-modified", date);
		if (s != null && s.equals(date)) {
			ctx.setStatus(304);
			return;
		}
		try {
			ctx.setBody(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			ctx.setStatus(404);
			ctx.delResHeader("last-modified");
			return;
		}
		ctx.setResHeader("content-length", Long.toString(file.length()));
		ctx.setResHeader("content-type", mftm.getContentType(file));
	}

}