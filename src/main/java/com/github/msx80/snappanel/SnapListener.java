package com.github.msx80.snappanel;

public interface SnapListener 
{
	/**
	 * Called when two panels snaps together
	 * @param top
	 * @param bottom
	 */
	void snapped(SnapPanel top, SnapPanel bottom);

	/**
	 * Called when two linked panels disconnect from each other
	 * @param top
	 * @param bottom
	 */
	void unlinked(SnapPanel top, SnapPanel bottom);
}
