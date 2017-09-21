package com.innovathon.sideways.util;

public interface Process 
{
	void launch();
	void beginning();
	void start();  // start after it has been stopped, but not for launching
	void stop();
	void pause();
	void resume();
	void ending();    // not to end the process, but what to do after it has ended.
}
