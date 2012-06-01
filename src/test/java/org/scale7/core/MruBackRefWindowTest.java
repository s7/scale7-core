package org.scale7.core;

import org.junit.Test;

import org.scale7.networking.compression.MruBackRefWindowCodec;

public class MruBackRefWindowTest {
	@Test
	public void doTest() {
		for (int window = 1; window < 5; window++) {
			MruBackRefWindowCodec<String> encoder = new MruBackRefWindowCodec<String>(window, true);
			MruBackRefWindowCodec<String> decoder = new MruBackRefWindowCodec<String>(window, false);
			encodeDecode(encoder, decoder, "one");
			encodeDecode(encoder, decoder, "two");
			encodeDecode(encoder, decoder, "four");
			encodeDecode(encoder, decoder, "one");
			encodeDecode(encoder, decoder, "three");
			encodeDecode(encoder, decoder, "one");
			encodeDecode(encoder, decoder, "two");
			encodeDecode(encoder, decoder, "five");
			encodeDecode(encoder, decoder, "two");
			encodeDecode(encoder, decoder, "two");
			encodeDecode(encoder, decoder, "six");
			encodeDecode(encoder, decoder, "seven");
			encodeDecode(encoder, decoder, "eight");
			encodeDecode(encoder, decoder, "nine");
			encodeDecode(encoder, decoder, "ten");
			encodeDecode(encoder, decoder, "eleven");
			encodeDecode(encoder, decoder, "one");
			encodeDecode(encoder, decoder, "one");
		}
	}
	
	public void encodeDecode(MruBackRefWindowCodec<String> encoder, MruBackRefWindowCodec<String> decoder,
			String value) {
		int ref = encoder.encodeRefOrValue(value);
		if (ref == -1)
			decoder.touchValue(value);
		else
			assert decoder.touchRef(ref).equals(value);
	}
}
