import java.util.Scanner;

import com.imageprocessor.core.Image;
import com.imageprocessor.util.AliasingUtil;
import com.imageprocessor.util.ImageConversionUtil;
import com.imageprocessor.util.AliasingUtil.AAFilterOption;

public class ImageProcessing {
	public static void main(String[] args) {

		System.out.println("--Welcome to Multimedia Software System--");
		System.out.println("--Homework 2--");

		boolean flag = true;
		Scanner in = new Scanner(System.in);
		
		while (flag) {

			System.out.println("Main Menu-----------------------------------");
			System.out.println("1. Aliasing");
			System.out.println("2. Dictionary Coding");
			System.out.println("3. Quit");
			System.out.println("Please enter the task number [1-3]");

			Image output, index;
			switch (in.nextInt()) {
			case 1:

				/*
				 * M=1, N=20, K=2 M=1, N=20, K=4 M=3, N=20, K=2 M=3, N=20, K=4 M=5, N=40, K=2
				 * M=5, N=40, K=4
				 */

				int[][] values = new int[][] { 
					{ 1, 20, 2 }, 
					{ 1, 20, 4 }, 
					{ 3, 20, 2 }, 
					{ 3, 20, 4 }, 
					{ 5, 40, 2 },
					{ 5, 40, 4 } };

				for(int[] set : values) {
					
					System.out.println("Press Enter to view results for M="+set[0]+" N="+set[1]+" K="+set[2]+":");
					in.nextLine();		// hold until user keypress
					
					Image image = AliasingUtil.generateCircle(set[0], set[1]);
					image.write2PPM("resource/Circle_"+set[0]+"_"+set[1]+".ppm");
					image.display();

					Image imageNoFilter = AliasingUtil.resizeByFactor(image, set[2], AAFilterOption.NoFilter);
					imageNoFilter.setFileName("Circle_"+set[0]+"_"+set[1]+"_K"+set[2]+"_NoFilter.ppm");
					imageNoFilter.write2PPM("resource/Circle_"+set[0]+"_"+set[1]+"_K"+set[2]+"_NoFilter"+".ppm");
					imageNoFilter.display();

					Image imageFilter1 = AliasingUtil.resizeByFactor(image, set[2], AAFilterOption.Filter1);
					imageFilter1.setFileName("Circle_"+set[0]+"_"+set[1]+"_K"+set[2]+"_Filter1.ppm");
					imageFilter1.write2PPM("resource/Circle_"+set[0]+"_"+set[1]+"_K"+set[2]+"_Filter1"+".ppm");
					imageFilter1.display();

					Image imageFilter2 = AliasingUtil.resizeByFactor(image, set[2], AAFilterOption.Filter2);
					imageFilter2.setFileName("Circle_"+set[0]+"_"+set[1]+"_K"+set[2]+"_Filter2.ppm");
					imageFilter2.write2PPM("resource/Circle_"+set[0]+"_"+set[1]+"_K"+set[2]+"_Filter2"+".ppm");
					imageFilter2.display();

				}
				break;
			case 2:
				System.out.println("Enter N value (2, 4, 8, 16, ...):");
				int level = in.nextInt();
				break;

			case 3:
			default:
				System.exit(0);
			}
		}

		in.close();

		System.out.println("--Good Bye--");
	}

}