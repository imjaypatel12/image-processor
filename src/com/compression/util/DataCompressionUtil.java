package com.compression.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataCompressionUtil {

	public static void encodeLZW(File file) throws IOException {
		Map<String, Integer> dictionary = new HashMap<String, Integer>();
		byte[] bytes = Files.readAllBytes(file.toPath());

		Set<Byte> initialVocab = new HashSet<Byte>();
		for (byte b : bytes) {
			initialVocab.add(b);
		}
		
		Integer index=0;
		for(Byte b : initialVocab) {
			dictionary.put(Byte.toString(b), index);
			index++;
		}
		
		for(int i=0; i<bytes.length; i++) {
			
		}
	}
}
