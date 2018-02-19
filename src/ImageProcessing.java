import java.util.Scanner;

import com.imageprocessor.core.Image;
import com.imageprocessor.util.ImageConversionUtil;

public class ImageProcessing {
	public static void main(String[] args) {
		// if there is no commandline argument, exit the program
		if (args.length != 1) {
			usage();
			System.exit(1);
		}
		
		System.out.println("--Welcome to Multimedia Software System--");
		Image input = new Image(args[0]);

		boolean flag = true;
		Scanner in = new Scanner(System.in);
		while (flag) {
			System.out.println("Main Menu-----------------------------------");
			System.out.println("1. Conversion to Gray-scale Image (24bits->8bits)");
			System.out.println("2. Conversion to N-level Image");
			System.out.println(
					"3. Conversion to 8bit Indexed Color Image using Uniform Color Quantization (24bits->8bits)");
			System.out
					.println("4. Conversion to 8bit Indexed Color Image using Hybrid Approach with Error Diffusion (24bits->8bits)");
			System.out.println("5. Quit");
			System.out.println("Please enter the task number [1-5]:");

			Image output, index;
			switch (in.nextInt()) {
			case 1:
				output = ImageConversionUtil.convert24ColorTo8Gray(input);
				output.display();
				output.write2PPM(output.getFileName());
				break;
			case 2:
				System.out.println("Enter N value (2, 4, 8, 16, ...):");
				int level = in.nextInt();

				output = ImageConversionUtil.convert24ColorToNLevelGrayThreshold(input, level);
				output.display();
				output.write2PPM(output.getFileName());

				Image output2 = ImageConversionUtil.convert24ColorToNLevelGrayED(input, level);
				output2.display();
				output2.write2PPM(output2.getFileName());
				break;

			case 3:
				ImageConversionUtil.generate8BitLUTforUCQ();
				index = ImageConversionUtil.convert24ColorTo8BitUCQLUTindex(input);
				index.write2PPM(index.getFileName());
				
				output = ImageConversionUtil.decode8bitIndexToColor(index);
				output.display();
				output.write2PPM(output.getFileName());				
				break;
			case 4:
				index = ImageConversionUtil.convert24ColorTo8Bit_ED_UCQ_LUTindex(input);
				index.write2PPM(index.getFileName());
				
				output = ImageConversionUtil.decode8bitIndexToColor(index, input.getFileName().split("\\.")[0] + "-QT8-2.ppm");
				output.display();
				output.write2PPM(output.getFileName());				
				break;
			case 5:
				flag=false;
				break;
			}
		}

		in.close();
		
		System.out.println("--Good Bye--");
	}

	public static void usage() {
		System.out.println("\nUsage: java ImageProcessing [input_ppm_file]\n");
	}
}