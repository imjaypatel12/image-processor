import java.util.Scanner;

import com.imageprocessor.core.Image;
import com.imageprocessor.util.AliasingUtil;
import com.imageprocessor.util.ImageConversionUtil;
import com.imageprocessor.util.JPEGDCTUtil;

public class ImageProcessing3 {
	public static void main(String[] args) {
		// if there is no commandline argument, exit the program
		if (args.length != 1) {
			usage();
			System.exit(1);
		}
		
		System.out.println("--Welcome to Multimedia Software System--");

		boolean flag = true;
		while(flag) {
			
			System.out.println("Please enter the compression quality (0 to 5):");
			
			int n = new Scanner(System.in).nextInt();
			
			Image image = new Image(args[0]);
			image.display();
			
			JPEGDCTUtil jpeg = new JPEGDCTUtil(image);
			
			// E1
			Image resized = jpeg.resizeImage();

			// E2
			jpeg.colorSpaceTransformation(resized);
			jpeg.subSample();

			// E3
			double[][] dY = jpeg.applyDCT(jpeg.Y);
			double[][] dCb = jpeg.applyDCT(jpeg.sCb);
			double[][] dCr = jpeg.applyDCT(jpeg.sCr);

			// E4
			int[][] qY = jpeg.quantize(dY, jpeg.quantY, n);
			int[][] qCb = jpeg.quantize(dCb, jpeg.quantCbr, n);
			int[][] qCr = jpeg.quantize(dCr, jpeg.quantCbr, n);

			jpeg.calculateCompressionRatio(qY, qCb, qCr, n);
			
			// D2
			dY = jpeg.dequantize(qY, jpeg.quantY, n);
			dCb = jpeg.dequantize(qCb, jpeg.quantCbr, n);
			dCr = jpeg.dequantize(qCr, jpeg.quantCbr, n);
			
			// D3
			double[][] idY = jpeg.applyIDCT(dY);
			double[][] idCb = jpeg.applyIDCT(dCb);
			double[][] idCr = jpeg.applyIDCT(dCr);

			// D2
			double[][] sCb = jpeg.superSample(idCb, 2);
			double[][] sCr = jpeg.superSample(idCr, 2);
			Image invertColored = jpeg.inverseColorSpaceTransformation(idY, sCb, sCr);

			// D1
			Image output = jpeg.restoreImageSize(invertColored);
			output.display();
			output.write2PPM(output.getFileName());

			System.out.println("Press 0 to exit, any key to continue:");
			flag = new Scanner(System.in).nextInt()==0?false:true;
		}
		
		
		System.out.println("--Good Bye--");
		System.exit(0);
	}

	public static void usage() {
		System.out.println("\nUsage: java ImageProcessing [input_ppm_file]\n");
	}
}