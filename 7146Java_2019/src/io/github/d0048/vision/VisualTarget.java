package io.github.d0048.vision;

import org.opencv.core.Point;
import org.opencv.core.Size;


public class VisualTarget {
	// in view,deviation from center

	public Point center = new Point(0, 0);
	public Size size = new Size();

	public VisualTarget(Point center, Size size) {
		this.center = center;
		this.size = size;
	}

}
