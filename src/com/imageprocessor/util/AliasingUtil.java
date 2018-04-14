package com.imageprocessor.util;

import com.imageprocessor.core.Image;

public class AliasingUtil {

	public enum AAFilterOption {
		NoFilter, Filter1, Filter2
	}

	public static Image generateCircle(int M, int N) {
		Image image = new Image(512, 512);
		image.setFileName("Circle_" + M + "_" + N + ".ppm");
		for (int i = 0; i < 512; i++)
			for (int j = 0; j < 512; j++)
				image.setPixel(i, j, new int[] { 255, 255, 255 });

		for (int j = N; j < 256; j += N) {
			for (double i = 0; i <= M - 1; i += 0.5) {
				generateCircle(image, j + i);
			}
		}

		return image;
	}

	/**
	 * Plot circle pattern using Trigonometry. Not used. Tweaks needed.
	 * @param image
	 * @param radius
	 */
	public static void generateCircle2(Image image, double radius) {
		double circm = 2 * radius * Math.PI;
		circm = circm > 1440 ? circm : 360;
		double deg = 360 / circm;

		for (int i = 0; i < circm; i++) {
			try {
				image.setPixel(256 + (int) Math.round(((radius) * Math.cos(i * deg))),
						256 + (int) Math.round(((radius) * Math.sin(i * deg))), new int[] { 0, 0, 0 });
			} catch (Exception e) {

			}
		}
	}

	/**
	 * Plot circle pattern using Cartesian equation. Used for current assignment 
	 * @param image
	 * @param M
	 */
	public static void generateCircle(Image image, double M) {
		int cx = 256, cy = 256;
		int x1, x2, y1, y2;
		double r = M - 1;
		boolean flag = true;
		int i = 0;
		while (i >= 0) {
			if (flag) {
				if (i >= r) {
					// done with x-axis, now plot using y-axis to remove errors
					flag = false;
					continue;
				}
				x1 = i;
				y1 = (int) Math.round(Math.sqrt(r * r - i * i));
				x2 = -x1;
				y2 = -y1;
				i++;
				image.setPixel(cx + x1, cy + y1, new int[] { 0, 0, 0 });
				image.setPixel(cx + x1, cy + y2, new int[] { 0, 0, 0 });
				image.setPixel(cx + x2, cy + y1, new int[] { 0, 0, 0 });
				image.setPixel(cx + x2, cy + y2, new int[] { 0, 0, 0 });
			} else {
				y1 = i;
				x1 = (int) Math.round(Math.sqrt(r * r - i * i));
				y2 = -y1;
				x2 = -x1;
				i--;
				image.setPixel(cx + x1, cy + y1, new int[] { 0, 0, 0 });
				image.setPixel(cx + x1, cy + y2, new int[] { 0, 0, 0 });
				image.setPixel(cx + x2, cy + y1, new int[] { 0, 0, 0 });
				image.setPixel(cx + x2, cy + y2, new int[] { 0, 0, 0 });
			}
		}
	}

	/**
	 * Plot circle pattern using Cartesian equation with midpoint correction. Not giving proper results. Not used for current assignment 
	 * @param image
	 * @param M
	 */
	public static void generateCircle3(Image image, double M) {
		int cx = 256, cy = 256;
		int x1, x2, y1, y2;
		double r = M - 1;
		boolean flag = true;
		int i = 0;
		while (i >= 0) {
			if (flag) {
				double x = i;
				double y = Math.sqrt(r * r - i * i) + 1;

				x1 = (int) x;
				y1 = (int) Math.round(y);

				if (Math.pow(y, 2) + Math.pow(x - 0.5, 2) > r * r)
					x1 = (int) x - 1;

				x2 = -x1;
				y2 = -y1;
				if (x1 >= r) {
					flag = false;
					continue;
				}
				i++;
				image.setPixel(cx + x1, cy + y1, new int[] { 0, 0, 0 });
				image.setPixel(cx + x1, cy + y2, new int[] { 0, 0, 0 });
				image.setPixel(cx + x2, cy + y1, new int[] { 0, 0, 0 });
				image.setPixel(cx + x2, cy + y2, new int[] { 0, 0, 0 });
			} else {

				double y = i;
				double x = Math.sqrt(r * r - i * i) + 1;

				y1 = (int) y;
				x1 = (int) Math.round(x);

				if (Math.pow(x, 2) + Math.pow(y - 0.5, 2) > r * r)
					y1 = (int) y - 1;

				x2 = -x1;
				y2 = -y1;

				i--;
				image.setPixel(cx + x1, cy + y1, new int[] { 0, 0, 0 });
				image.setPixel(cx + x1, cy + y2, new int[] { 0, 0, 0 });
				image.setPixel(cx + x2, cy + y1, new int[] { 0, 0, 0 });
				image.setPixel(cx + x2, cy + y2, new int[] { 0, 0, 0 });
			}
		}
	}

	public static Image resizeByFactor(Image image, int k, AAFilterOption filterOption) {
		Image resized = new Image(image.getW() / k, image.getH() / k);
		double[][] filter;
		switch (filterOption) {
		case NoFilter:
			for (int i = 0; i < resized.getH(); i++)
				for (int j = 0; j < resized.getW(); j++) {
					int[] rgb = new int[3];
					image.getPixel(j * k, i * k, rgb);
					resized.setPixel(j, i, rgb);
				}
			break;

		case Filter1:
			filter = new double[][] { { 1 / 9d, 1 / 9d, 1 / 9d }, { 1 / 9d, 1 / 9d, 1 / 9d },
					{ 1 / 9d, 1 / 9d, 1 / 9d } };

			for (int y = 0; y < image.getH() - 1; y += k)
				for (int x = 0; x < image.getW() - 1; x += k)
					applyFilter(image, resized, x, y, filter);
			break;

		case Filter2:
			filter = new double[][] { { 1 / 16d, 2 / 16d, 1 / 16d }, { 2 / 16d, 4 / 16d, 2 / 16d },
					{ 1 / 16d, 2 / 16d, 1 / 16d } };

			for (int y = 0; y < image.getH() - 1; y += k)
				for (int x = 0; x < image.getW() - 1; x += k)
					applyFilter(image, resized, x, y, filter);
			break;

		}

		return resized;
	}

	private static void applyFilter(Image source, Image target, int x, int y, double[][] filter) {
		double w;
		int k = source.getH() / target.getH();

		// filter redistribution decision tree. For Boundary cases
		if (x > 0) {
			if (y > 0) {
				if (x < source.getW() - 1) {
					if (y < source.getH() - 1) {
						// inside
						// no filter modification needed
						w = 1;
					} else {
						// bottom
						w = filter[0][0] + filter[0][1] + filter[0][2] + filter[1][0] + filter[1][1] + filter[1][2];
					}
				} else {
					if (y < source.getH() - 1) {
						// right
						w = filter[0][0] + filter[0][1] + filter[1][0] + filter[1][1] + filter[2][0] + filter[2][1];
					} else {
						// bottom-right
						w = filter[0][0] + filter[0][1] + filter[1][0] + filter[1][1];
					}
				}
			} else {
				if (x < source.getW() - 1) {
					// top
					w = filter[1][0] + filter[1][1] + filter[1][2] + filter[2][0] + filter[2][1] + filter[2][2];
				} else {
					// top-right
					w = filter[1][0] + filter[1][1] + filter[2][0] + filter[2][1];
				}
			}
		} else {
			if (y > 0) {
				if (y < source.getH() - 1) {
					// left
					w = filter[0][1] + filter[0][2] + filter[1][1] + filter[1][2] + filter[2][1] + filter[2][2];
				} else {
					// bottom-left
					w = filter[0][1] + filter[0][2] + filter[1][1] + filter[1][2];
				}
			} else {
				// top-left
				w = filter[1][1] + filter[1][2] + filter[2][1] + filter[2][2];
			}
		}

		double r = 0;
		int R;

		int m = 0, n = 0;
		for (int i = x - 1; i <= x + 1; i++) {
			n = 0;
			for (int j = y - 1; j <= y + 1; j++) {
				try {
					r += source.getR(i, j) * filter[m][n] / w;		// add leftover weights for boundaries
				} catch (Exception e) {
					// edge cases
				}
				n++;
			}
			m++;
		}

		R = (int) Math.round(r);
		target.setPixel(x / k, y / k, new int[] { R, R, R });
	}
}
