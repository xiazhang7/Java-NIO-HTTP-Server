package app.core;

import app.res.Contents;

public interface MiddleWare {
	public void handle(Contents ctx);
}