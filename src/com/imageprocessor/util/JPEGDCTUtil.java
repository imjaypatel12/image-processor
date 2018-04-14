package com.imageprocessor.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.imageprocessor.core.Image;

public class JPEGDCTUtil {

	private Image original, output;

	private final int w_pad, h_pad;

	public double[][] Y, Cb, Cr, sCb, sCr;

	public int[][] quantY, quantCbr;

	public JPEGDCTUtil(Image image) {
		this.original = image;
		w_pad = original.getW() % 8 == 0 ? 0 : (8 - original.getW() % 8);
		h_pad = original.getH() % 8 == 0 ? 0 : (8 - original.getH() % 8);
		Y = new double[original.getH() + h_pad][original.getW() + w_pad];
		Cb = new double[original.getH() + h_pad][original.getW() + w_pad];
		Cr = new double[original.getH() + h_pad][original.getW() + w_pad];

		quantY = new int[][] { { 4, 4, 4, 8, 8, 16, 16, 32 }, { 4, 4, 4, 8, 8, 16, 16, 32 },
				{ 4, 4, 8, 8, 16, 16, 32, 32 }, { 8, 8, 8, 16, 16, 32, 32, 32 }, { 8, 8, 16, 16, 32, 32, 32, 32 },
				{ 16, 16, 16, 32, 32, 32, 32, 32 }, { 16, 16, 32, 32, 32, 32, 32, 32 },
				{ 32, 32, 32, 32, 32, 32, 32, 32 } };

		quantCbr = new int[][] { { 8, 8, 8, 16, 32, 32, 32, 32 }, { 8, 8, 8, 16, 32, 32, 32, 32 },
				{ 8, 8, 16, 32, 32, 32, 32, 32 }, { 16, 16, 32, 32, 32, 32, 32, 32 },
				{ 32, 32, 32, 32, 32, 32, 32, 32 }, { 32, 32, 32, 32, 32, 32, 32, 32 },
				{ 32, 32, 32, 32, 32, 32, 32, 32 }, { 32, 32, 32, 32, 32, 32, 32, 32 } };
	}

	public Image resizeImage() {
		Image resized = new Image(original.getW() + w_pad, original.getH() + h_pad);
		resized.setFileName("resized_" + original.getImageName());

		for (int x = 0; x < original.getW(); x++)
			for (int y = 0; y < original.getH(); y++) {
				byte[] rgb = new byte[3];
				original.getPixel(x, y, rgb);
				resized.setPixel(x, y, rgb);
			}

		for (int y = 0; y < original.getH(); y++)
			for (int x = original.getW(); x < resized.getW(); x++)
				resized.setPixel(x, y, new int[] { 0, 0, 0 });

		for (int y = original.getH(); y < resized.getH(); y++)
			for (int x = 0; x < resized.getW(); x++)
				resized.setPixel(x, y, new int[] { 0, 0, 0 });

		return resized;
	}

	public Image restoreImageSize(Image image) {
		Image output = new Image(original.getW(), original.getH());
		output.setFileName("output_" + image.getImageName());

		for (int x = 0; x < output.getW(); x++)
			for (int y = 0; y < output.getH(); y++) {
				byte[] rgb = new byte[3];
				image.getPixel(x, y, rgb);
				output.setPixel(x, y, rgb);
			}
		return output;
	}

	public static int ensureRange(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	public static double ensureRange(double value, double min, double max) {
		return Math.min(Math.max(value, min), max);
	}

	public void colorSpaceTransformation(Image image) {
		// 0.2990 0.5870 0.1140
		// -0.1687 -0.3313 0.5000
		// 0.5000 -0.4187 -0.0813

		for (int y = 0; y < image.getH(); y++)
			for (int x = 0; x < image.getW(); x++) {
				int[] rgb = new int[3];
				image.getPixel(x, y, rgb);
				double yy = (rgb[0] * 0.2990) + (rgb[1] * 0.5870) + (rgb[2] * 0.1140);
				double cb = (rgb[0] * -0.1687) + (rgb[1] * -0.3313) + (rgb[2] * 0.5000);
				double cr = (rgb[0] * 0.5000) + (rgb[1] * -0.4187) + (rgb[2] * -0.0813);

				cb = ensureRange(cb, -127.5, 127.5);
				cr = ensureRange(cr, -127.5, 127.5);

				this.Y[y][x] = yy - 128;
				this.Cb[y][x] = cb - 0.5;
				this.Cr[y][x] = cr - 0.5;
			}
	}

	public Image inverseColorSpaceTransformation(double[][] Y, double[][] Cb, double[][] Cr) {
		// 1.0000 0.0000 1.4020
		// 1.0000 -0.3441 -0.7141
		// 1.0000 1.7720 0.0000
		Image inverted = new Image(original.getW() + w_pad, original.getH() + h_pad);
		inverted.setImageName(original.getImageName());

		for (int y = 0; y < inverted.getH(); y++)
			for (int x = 0; x < inverted.getW(); x++) {
				int[] rgb = new int[3];
				// image.getPixel(x, y, rgb);

				rgb[0] = (int) ensureRange((Y[y][x] + 128) + (Cr[y][x] + 0.5) * 1.4020, 0, 255);

				rgb[1] = (int) ensureRange((Y[y][x] + 128) + (Cb[y][x] + 0.5) * -0.3441 + (Cr[y][x] + 0.5) * -0.7141, 0,
						255);

				rgb[2] = (int) ensureRange((Y[y][x] + 128) + (Cb[y][x] + 0.5) * 1.7720, 0, 255);

				inverted.setPixel(x, y, rgb);
			}
		return inverted;
	}

	public void subSample() {
		int x_pad = (Cb[0].length / 2) % 8 == 0 ? 0 : (8 - (Cb[0].length / 2) % 8);
		int y_pad = (Cb.length / 2) % 8 == 0 ? 0 : (8 - (Cb.length / 2) % 8);

		sCb = new double[Cb.length / 2 + y_pad][Cb[0].length / 2 + x_pad];
		sCr = new double[Cr.length / 2 + y_pad][Cr[0].length / 2 + x_pad];

		for (int y = 0; y < Cb.length / 2; y++)
			for (int x = 0; x < Cb[0].length / 2; x++) {
				sCb[y][x] = this.getAverage(Cb, x * 2, y * 2, 2);
				sCr[y][x] = this.getAverage(Cr, x * 2, y * 2, 2);
			}

		// add 0 bits for padding bits
		for (int y = 0; y < sCb.length - y_pad; y++)
			for (int x = sCb[0].length - x_pad; x < sCb[0].length; x++)
				sCb[y][x] = sCr[y][x] = 0.0;

		for (int y = sCb.length - y_pad; y < sCb.length; y++)
			for (int x = 0; x < sCb[0].length; x++)
				sCb[y][x] = sCr[y][x] = 0.0;

	}

	public double[][] superSample(double[][] target, int factor) {
		double[][] result = new double[target.length * factor][target[0].length * factor];

		for (int y = 0; y < result.length; y++)
			for (int x = 0; x < result[0].length; x++)
				result[y][x] = target[y / factor][x / factor];

		return result;
	}

	private double getAverage(double[][] space, int x, int y, int range) {
		double avg = 0.0;
		for (int i = 0; i < range; i++)
			for (int j = 0; j < range; j++)
				avg += space[y + i][x + j];

		return avg / (range * range);
	}

	public double[][] applyDCT(double[][] target) {
		double[][] DCTed = new double[target.length][target[0].length];

		for (int y = 0; y < target.length; y += 8)
			for (int x = 0; x < target[0].length; x += 8) {
				applyBlockDCT(y, x, DCTed, target);
			}

		return DCTed;
	}

	private void applyBlockDCT(int U, int V, double[][] dct, double[][] source) { // u-> y ; v-> x
		for (int u = 0; u < 8; u++)
			for (int v = 0; v < 8; v++) {
				double val = 1 / 4d;
				val = u == 0 ? val * 1 / Math.sqrt(2) : val;
				val = v == 0 ? val * 1 / Math.sqrt(2) : val;

				double sum = 1d;
				for (int y = 0; y < 8; y++)
					for (int x = 0; x < 8; x++) {
						sum += source[y + U][x + V] * Math.cos((2 * x + 1) * v * Math.PI / 16)
								* Math.cos((2 * y + 1) * u * Math.PI / 16);
					}

				val *= sum;

				dct[u + U][v + V] = ensureRange(val, -1024, 1024);
			}
	}

	public double[][] applyIDCT(double[][] target) {
		double[][] iDCTed = new double[target.length][target[0].length];

		for (int y = 0; y < target.length; y += 8)
			for (int x = 0; x < target[0].length; x += 8) {
				applyBlockIDCT(y, x, iDCTed, target);
			}

		return iDCTed;
	}

	private void applyBlockIDCT(int U, int V, double[][] idct, double[][] source) { // u-> y ; v-> x
		for (int Y = 0; Y < 8; Y++)
			for (int X = 0; X < 8; X++) {
				double val = 1 / 4d;

				double sum = 0d;
				for (int u = 0; u < 8; u++)
					for (int v = 0; v < 8; v++) {
						double expr = 1d;
						expr = u == 0 ? expr * 1 / Math.sqrt(2) : expr;
						expr = v == 0 ? expr * 1 / Math.sqrt(2) : expr;
						expr *= source[u + U][v + V] * Math.cos((2 * X + 1) * v * Math.PI / 16)
								* Math.cos((2 * Y + 1) * u * Math.PI / 16);
						sum += expr;
					}

				val *= sum;

				idct[Y + U][X + V] = val;
			}
	}

	public int[][] quantize(double[][] target, int[][] table, int quality) {
		int[][] quantized = new int[target.length][target[0].length];
		for (int y = 0; y < quantized.length; y++)
			for (int x = 0; x < quantized[0].length; x++)
				quantized[y][x] = (int) Math.round(target[y][x] / (table[y % 8][x % 8] * Math.pow(2, quality)));
		return quantized;
	}

	public double[][] dequantize(int[][] target, int[][] table, int quality) {
		double[][] dequantized = new double[target.length][target[0].length];
		for (int y = 0; y < dequantized.length; y++)
			for (int x = 0; x < dequantized[0].length; x++)
				dequantized[y][x] = target[y][x] * table[y % 8][x % 8] * Math.pow(2, quality);
		return dequantized;
	}

	public void calculateCompressionRatio(int[][] qY, int[][] qCb, int[][] qCr, int n) {

		List<List<Entry<Integer, Integer>>> Yblocks = new ArrayList<List<Entry<Integer, Integer>>>();
		List<List<Entry<Integer, Integer>>> Cbblocks = new ArrayList<List<Entry<Integer, Integer>>>();
		List<List<Entry<Integer, Integer>>> Crblocks = new ArrayList<List<Entry<Integer, Integer>>>();

		for (int y = 0; y < qY.length; y += 8)
			for (int x = 0; x < qY[0].length; x += 8)
				Yblocks.add(this.getDCACpairs(qY, x, y));

		for (int y = 0; y < qCb.length; y += 8)
			for (int x = 0; x < qCb[0].length; x += 8)
				Cbblocks.add(this.getDCACpairs(qCb, x, y));

		for (int y = 0; y < qCr.length; y += 8)
			for (int x = 0; x < qCr[0].length; x += 8)
				Crblocks.add(this.getDCACpairs(qCr, x, y));

		long size = 0l;
		size += 28 - (3 * n); // 3 DCs

		for (List<Entry<Integer, Integer>> list : Yblocks) {
			size += ((16 - n) * (list.size() - 1)); // first element is DC
		}

		for (List<Entry<Integer, Integer>> list : Cbblocks) {
			size += ((15 - n) * (list.size() - 1)); // first element is DC
		}

		for (List<Entry<Integer, Integer>> list : Crblocks) {
			size += ((15 - n) * (list.size() - 1)); // first element is DC
		}

		double ratio = (original.getH() * original.getW() * 24) / (double) size;

		System.out.println("Compression ratio for quality " + n + " is:" + ratio);
	}

	private List<Entry<Integer, Integer>> getDCACpairs(int[][] source, int X, int Y) {
		List<Entry<Integer, Integer>> list = new ArrayList<>();

		int[] result = new int[64];
		int t = 0;

		for (int i = 0; i < 16 - 1; i++) {
			if (i % 2 == 1) {
				int y = i < 8 ? 0 : i - 8 + 1;
				int x = i < 8 ? i : 8 - 1;
				while (y < 8 && x >= 0) {
					result[t++] = source[Y + y++][X + x--];
				}
			} else {
				int y = i < 8 ? i : 8 - 1;
				int x = i < 8 ? 0 : i - 8 + 1;
				while (y >= 0 && x < 8) {
					result[t++] = source[Y + y--][X + x++];
				}
			}
		}

		// DC
		list.add(new AbstractMap.SimpleEntry<Integer, Integer>(result[0], 1));

		// AC runs
		for (int i = 1; i < result.length; i++) {
			int val = result[i];
			int len = 1;
			while (i + 1 < result.length && result[i] == result[i + 1]) {
				len++;
				i++;
			}
			list.add(new AbstractMap.SimpleEntry<Integer, Integer>(val, len));
		}

		return list;
	}
}
