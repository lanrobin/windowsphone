package com.axen.launcher.system;

public interface CallListener {
	public void updateCallLog(int missing, int out, int received, int rejected);
}
