package mahomaps.route;

import java.util.Stack;

import mahomaps.map.Geopoint;

public class RouteDecoder {
	private static final byte[] DECODE_ALPHABET = { -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9,
			-9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62,
			-9, -9, -9, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7,
			8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28,
			29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9,
			-9 };

	public static byte[] Base64ToBlob(String s) {
		if (s == null) {
			return null;
		}
		byte[] source = s.getBytes();
		int len34 = source.length * 3 / 4;
		byte[] outBuff = new byte[len34];
		int outBuffPosn = 0;

		byte[] b4 = new byte[4];
		int b4Posn = 0;
		int i = 0;
		byte sbiCrop = 0;
		byte sbiDecode = 0;
		for (i = 0; i < source.length; i++) {
			sbiCrop = (byte) (source[i] & 0x7F);
			sbiDecode = DECODE_ALPHABET[sbiCrop];
			if (sbiDecode >= -5) {
				if (sbiDecode >= -1) {
					b4[(b4Posn++)] = sbiCrop;
					if (b4Posn > 3) {
						outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
						b4Posn = 0;
						if (sbiCrop == 61) {
							break;
						}
					}
				}
			} else {
				return null;
			}
		}
		if (outBuffPosn == 0) {
			return null;
		}
		byte[] out = new byte[outBuffPosn];
		System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
		return out;
	}

	private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset) {
		if (source[(srcOffset + 2)] == 61) {
			int outBuff = (DECODE_ALPHABET[source[srcOffset]] & 0xFF) << 18
					| (DECODE_ALPHABET[source[(srcOffset + 1)]] & 0xFF) << 12;

			destination[destOffset] = ((byte) (outBuff >>> 16));
			return 1;
		}
		if (source[(srcOffset + 3)] == 61) {
			int outBuff = (DECODE_ALPHABET[source[srcOffset]] & 0xFF) << 18
					| (DECODE_ALPHABET[source[(srcOffset + 1)]] & 0xFF) << 12
					| (DECODE_ALPHABET[source[(srcOffset + 2)]] & 0xFF) << 6;

			destination[destOffset] = ((byte) (outBuff >>> 16));
			destination[(destOffset + 1)] = ((byte) (outBuff >>> 8));
			return 2;
		}
		try {
			int outBuff = (DECODE_ALPHABET[source[srcOffset]] & 0xFF) << 18
					| (DECODE_ALPHABET[source[(srcOffset + 1)]] & 0xFF) << 12
					| (DECODE_ALPHABET[source[(srcOffset + 2)]] & 0xFF) << 6
					| DECODE_ALPHABET[source[(srcOffset + 3)]] & 0xFF;

			destination[destOffset] = ((byte) (outBuff >> 16));
			destination[(destOffset + 1)] = ((byte) (outBuff >> 8));
			destination[(destOffset + 2)] = ((byte) outBuff);
			return 3;
		} catch (Exception e) {
		}
		return -1;
	}

	public static Geopoint[] DecodeRoutePath(final String data) {
		double o = 1000000d;
		byte[] t = Base64ToBlob(data.replace('-', '+').replace('_', '/'));

		Stack stack = new Stack();
		double[] n = new double[2];
		for (int i = 0; i < t.length - 8; i += 8) {
			int c1 = 0;
			int c2 = 0;
			for (int j = 0; j < 4; j++) {
				c1 |= (t[i + j] & 0xFF) << (8 * j);
				c2 |= (t[i + j + 4] & 0xFF) << (8 * j);
			}
			double d1 = c1 / o;
			double d2 = c2 / o;
			double[] l = new double[] { d1 + n[0], d2 + n[1] };
			n = l;

			Geopoint g = new Geopoint(l[1], l[0]);
			g.type = Geopoint.ROUTE_VERTEX;
			stack.push(g);
		}
		Geopoint[] arr = new Geopoint[stack.size()];
		stack.copyInto(arr);
		System.out.println("Route points count: " + arr.length);
		return arr;
	}
}
