package com.imageprocessor.util;

import com.imageprocessor.core.Image;

public class ImageConversionUtil {

	/**
	 * Convert 24-bit Color to 8-bit Gray scale
	 * @return
	 * @author jaypatel
	 */
	public static Image convert24ColorTo8Gray(Image input, String... filename) {
		// setup blank image file
		Image output = new Image(input.getW(), input.getH());
		output.setFileName(
				filename.length > 0 ? filename[0] : (input.getFileName().split("\\.")[0] + "_24ColorTo8Gray.ppm"));

		for (int y = 0; y < input.getH(); y++) {
			for (int x = 0; x < input.getW(); x++) {
				int[] rgb = new int[3];
				input.getPixel(x, y, rgb);
				int grayVal = (int) Math.round((0.299 * rgb[0]) + (0.587 * rgb[1]) + (0.114 * rgb[2]));
				output.setPixel(x, y, new int[] { grayVal, grayVal, grayVal });
			}
		}
		return output;
	}

	/**
	 * Convert 24-bit Color to N-Level Gray Scale
	 * @return
	 * @author jaypatel
	 */
	public static Image convert24ColorToNLevelGrayThreshold(Image input, int level, String... fname) {
		// convert to gray
		Image grayImage = convert24ColorTo8Gray(input);

		// setup blank image file
		Image outputThreshold = grayImage;
		outputThreshold.setFileName(fname.length > 0 ? fname[0]
				: (input.getFileName().split("\\.")[0] + "_24ColorTo" + level + "LevelThresholdGray.ppm"));

		// using threshold value
		for (int y = 0; y < grayImage.getH(); y++) {
			for (int x = 0; x < grayImage.getW(); x++) {
				int[] rgb = new int[3];
				grayImage.getPixel(x, y, rgb);
				int divisor = 256 / level;
				int new_value = (int) ((Math.floor(rgb[0] / divisor) * 255) / (level - 1));
				outputThreshold.setPixel(x, y, new int[] { new_value, new_value, new_value });
			}
		}
		return outputThreshold;
	}

	/**
	 * Convert 24-bit Color to N-Level Gray Scale using Error Diffusion
	 * @return
	 * @author jaypatel
	 */
	public static Image convert24ColorToNLevelGrayED(Image input, int level, String... fname) {
		// convert to gray
		Image grayImage = convert24ColorTo8Gray(input);

		// setup blank image file
		Image outputED = grayImage;
		outputED.setFileName(fname.length > 0 ? fname[0]
				: (input.getFileName().split("\\.")[0] + "_24ColorTo" + level + "LevelErrorDiffusionGray.ppm"));

		// using Error Diffusion
		for (int y = 0; y < outputED.getH(); y++) {
			for (int x = 0; x < outputED.getW(); x++) {
				int[] rgb = new int[3];
				outputED.getPixel(x, y, rgb);
				int divisor = 256 / level;
				int new_value = (int) ((Math.floor(rgb[0] / divisor) * 255) / (level - 1));
				outputED.setPixel(x, y, new int[] { new_value, new_value, new_value });

				// perform error diffusion
				int error = rgb[0] - new_value;					// diffuse same error in all channels

				errorDiffusion(outputED, x, y, new int[] {error, error, error});
			}
		}
		return outputED;
	}

	/**
	 * Diffuse error matrix into adjacent pixels using Floyd-Steinberg Weights
	 * @return
	 * @author jaypatel
	 */
	private static void errorDiffusion(Image image, int x, int y, int[] error) {
		if ((x>0) && (x < (image.getW() - 1)) && (y < (image.getH() - 1))) {
			image.diffuseError(x-1, y+1, error, 3/16d);
			image.diffuseError(x, y+1, error, 5/16d);
			image.diffuseError(x+1, y+1, error, 1/16d);
			image.diffuseError(x+1, y, error ,7/16d);
		} else if((x>0) && (y < (image.getH() - 1))) {
			image.diffuseError(x-1, y+1, error, 3/16d);
			image.diffuseError(x, y+1, error, 5/16d);
		} else if(y < (image.getH() - 1)){
			image.diffuseError(x, y+1, error, 5/16d);
			image.diffuseError(x+1, y+1, error, 1/16d);
			image.diffuseError(x+1, y, error, 7/16d);
		}
	}
	
	/**
	 * Generate 8-bit LUT
	 * @return
	 * @author jaypatel
	 */
	public static void generate8BitLUTforUCQ() {
		System.out.println("Look Up Table for Uniform Color Quantization");
		System.out.println("Index\t\tR\tG\tB");
		int index = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				for (int k = 0; k < 4; k++) {
					System.out.println(index+"\t\t"+((i*32)+16)+"\t"+((j*32)+16)+"\t"+((k*64)+32));
					index++;
				}
			}
		}
	}
	
	/**
	 * Calculate Color from LUT index
	 * @return
	 * @author jaypatel
	 */
	private static int[] calculateColorFromLUTIndex(int index) {
		// calculate LUT block from index value, all 3 channel have same index value
		int[] lut_block = new int[3];
		lut_block[0]=index/32;
		lut_block[1]=(index%32)/4;
		lut_block[2]=(index%32)%4;
		
		// calculate color from LUT block number
		int[] rgb = new int[3];
		rgb[0] = lut_block[0]*32 + 16;
		rgb[1] = lut_block[1]*32 + 16;
		rgb[2] = lut_block[2]*64 + 32;

		return rgb;
	}
	
	/**
	 * Calculate LUT index from Color
	 * @return
	 * @author jaypatel
	 */
	private static int calculateLUTIndexFromColor(int[] rgb) {
		// calculate LUT block number
		int[] rgb_block = new int[3];
		rgb_block[0]=rgb[0]/32;
		rgb_block[1]=rgb[1]/32;
		rgb_block[2]=rgb[2]/64;
		
		// calculate LUT index
		return rgb_block[2] + rgb_block[1]*4 + rgb_block[0]*32;
	}
	
	/**
	 * Convert 24-bit Color to 8-bit UCQ LUT index
	 * @return
	 * @author jaypatel
	 */
	public static Image convert24ColorTo8BitUCQLUTindex(Image input, String... fname) {
		// setup blank image file
		Image outputIndex = new Image(input.getW(), input.getH());
		outputIndex.setFileName(fname.length > 0 ? fname[0]
				: (input.getFileName().split("\\.")[0] + "-index.ppm"));

		// construct index file
		for (int y = 0; y < outputIndex.getH(); y++) {
			for (int x = 0; x < outputIndex.getW(); x++) {
				int[] rgb = new int[3];
				input.getPixel(x, y, rgb);

				// calculate LUT index
				int index = calculateLUTIndexFromColor(rgb);
				
				// set pixels into index file
				outputIndex.setPixel(x, y, new int[] { index, index, index });
			}
		}
		return outputIndex;
	}


	/**
	 * Convert 24-bit Color to 8-bit UCQ LUT index using ED
	 * @return
	 * @author jaypatel
	 */
	public static Image convert24ColorTo8Bit_ED_UCQ_LUTindex(Image input, String... fname) {
		
		Image image = new Image(input.getFileName());
		
		// setup blank image file
		Image outputIndex = new Image(input.getW(), input.getH());
		outputIndex.setFileName(fname.length > 0 ? fname[0]
				: (input.getFileName().split("\\.")[0] + "-index2.ppm"));

		// construct index file
		for (int y = 0; y < outputIndex.getH(); y++) {
			for (int x = 0; x < outputIndex.getW(); x++) {
				int[] rgb = new int[3];
				image.getPixel(x, y, rgb);
				
				// calculate LUT index
				int index = calculateLUTIndexFromColor(rgb);

				// set pixels into index file
				outputIndex.setPixel(x, y, new int[] { index, index, index });

				// Error Diffusion
				// recalculate color from index
				int[] new_rgb = calculateColorFromLUTIndex(index);
				
				// calculate error matrix
				int[] error = new int[3];
				error[0] = rgb[0] - new_rgb[0];
				error[1] = rgb[1] - new_rgb[1];
				error[2] = rgb[2] - new_rgb[2];

				// perform error diffusion
				errorDiffusion(image, x, y, error);
			}
		}
		return outputIndex;
	}

	/**
	 * Decode 8-bit LUT index to Color
	 * @return
	 * @author jaypatel
	 */
	public static Image decode8bitIndexToColor(Image input, String... fname) {
		// setup blank image file
		Image outputQT8 = new Image(input.getW(), input.getH());
		outputQT8.setFileName(fname.length > 0 ? fname[0]
				: (input.getFileName().split("\\.")[0] + "-QT8.ppm"));

		// using threshold value
		for (int y = 0; y < outputQT8.getH(); y++) {
			for (int x = 0; x < outputQT8.getW(); x++) {
				int[] index = new int[3];
				input.getPixel(x, y, index);

				// set pixels into index file
				outputQT8.setPixel(x, y, calculateColorFromLUTIndex(index[0]));
			}
		}
		return outputQT8;
	}
}
