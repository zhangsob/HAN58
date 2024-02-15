import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * HAN58(Hangul 특화 AlphaNumeric58)이란.. AN62(<a href='https://github.com/zhangsob/AN62'>https://github.com/zhangsob/AN62</a>에서 Hangul부분을 특화함.<br/>
 * <br/>
 * 원리 : UTF8은 아래와 같다..<br/>
 *       UTF8은 아래와 같은 Byte범위를 갖는다.
 *       그럼, 0x000000 ~ 0x00007F --> 0xxx xxxx                               --> ASCII<br/>
 *            0x000080 ~ 0x0007FF --> 110x xxxx 10xx xxxx                     --> 서유럽<br/>
 *            0x000800 ~ 0x00FFFF --> 1110 xxxx 10xx xxxx 10xx xxxx           --> 한글, 한자<br/>
 *            0x010000 ~ 0x10FFFF --> 1111 0zzz 10zz xxxx 10xx xxxx 10xx xxxx --> 기타<br/>
 *       여기서, 한글은 0xAC00 ~ 0xD7A3 = 0x2BA4(11,172)자 = 19(초성) * 21(중성) * 28(종성)이다.<br/>
 *             한글을 2의 14승(2^14)은 16,384로 최소 14bit가 필요하다.<br/>
 *             즉, 10xx xxxx xxxx xxxx로 표현한다면.<br/>
 *       다시, 0x000000 ~ 0x00007F --> 0xxx xxxx                      0x00 ~ 0x7F --> ASCII<br/>
 *            0x00AC00 ~ 0x00D7A3 --> 10xx xxxx yyyy yyyy            0x80 ~ 0xBF, 0x00 ~ 0xBF --> 한글<br/>
 *            0x000080 ~ 0x10FFFF --> 110x xxxx yyyy yyyy yyyy yyyy  0xC0 ~ 0xDF, 0x00 ~ 0xBF, 0x00 ~ 0xBF--> 그외<br/>
 *            여기에서 yyyy yyyy는 0000 0000 ~ 1011 1111(192가지)로<br/>
 *            64 * 192 = 12,288 > 11,172(한글)<br/>
 *            32 * 192 * 192 = 1,179,648 = 0x120000 > 10FFFF(그외)<br/>
 *        <br/>
 *        
 * @author zhangsob@gmail.com
 * 
 * @history 2024-02-05 encode(), decode() 만듦.<br/>             
 */
public class HAN58 {
	private static final char[] toBase58= {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v'
	};
	
	private static int str2bytes(byte[] ret, String text) {
		if(ret == null)
			ret = new byte[text.length() * 3] ;

		int ri = 0 ;
		int surrogate = 0 ;
		for(int utf16 : text.toCharArray()) {
			if(utf16 < 0x80) {
				ret[ri++] = (byte)utf16 ;
			}
			else if(0xAC00 <= utf16 && utf16 <= 0xD7FF) {
				utf16 -= 0xAC00 ;
				ret[ri++] = (byte)(0x80 | (utf16 / 0xC0)) ;
				ret[ri++] = (byte)(utf16 % 0xC0) ;
			}
			else {
				if((utf16 & 0xF800) == 0xD800) {
					if(utf16 < 0xDC00) {
						surrogate = utf16 - 0xD800 ;
						continue ;
					}
					else
						utf16 = (surrogate << 10) + (utf16 - 0xDC00) + 0x010000 ;
				}

				ret[ri++] = (byte)(0xC0 | (utf16 / 0xC0 / 0xC0)) ;
				utf16 %= 0xC0 * 0xC0 ;
				ret[ri++] = (byte)(utf16 / 0xC0) ;
				ret[ri++] = (byte)(utf16 % 0xC0) ;
			}
		}
		return ri ;
	}
	
	public static String encode(String text) throws UnsupportedEncodingException {
		byte[] utf8 = new byte[text.length() * 3] ;
		int len = str2bytes(utf8, text) ;
		char[] ret_buffer = new char[((len + 2) / 3) * 4] ;
		int value = 0 ;
		int ri = 0;
		for(int i = 0; i < len; ++i) {
			value = value * 0xE0 + (utf8[i] & 0xFF);
			if(i % 3 == 2) {
				ret_buffer[ri + 3] = toBase58[value % 58] ;
				value /= 58 ;
				ret_buffer[ri + 2] = toBase58[value % 58] ;
				value /= 58 ;
				ret_buffer[ri + 1] = toBase58[value % 58] ;
				value /= 58 ;
				ret_buffer[ri] = toBase58[value] ;

				value = 0 ;
				ri += 4 ;
			}
		}
		
		len = len % 3 ;
		if(len > 0) {
			for(int j = len; j >= 0; --j, value /= 58)
				ret_buffer[ri + j] = toBase58[value % 58] ;

			ri += len + 1 ;
		}
		
		return new String(ret_buffer, 0, ri) ;
	}

	private static final int[] fromBase58 = new int[128] ;
	static {
		Arrays.fill(fromBase58, -1);
		for (int i = 0, len = toBase58.length; i < len; i++)
			fromBase58[toBase58[i]] = i;
	}

	public static String decode(String text) throws UnsupportedEncodingException {
		char[] chs = text.toCharArray() ;
		int len = chs.length ;
		if(len % 4 == 1)	throw new IllegalArgumentException("invalid HAN58 length") ;

		byte[] dst = new byte[len / 4 * 3 + ((len % 4 > 0) ? len%4 - 1 : 0)] ;
		byte[] tmp = new byte[3] ;
		int value = 0 ;
		int val = 0 ;

		int bi = 0 ;
		int ii = 0; 
		for(char ch : chs) {
			if(ch >= 0x80)
				throw new IllegalArgumentException("invalid HAN58 character " + ch) ;
			
			val = fromBase58[ch] ;
			if(val < 0)
				throw new IllegalArgumentException("invalid HAN58 character " + ch) ;
			
			value = value * 58 + val;
			if(++ii % 4 == 0) {
				dst[bi + 2] = (byte)(value % 0xE0) ;
				value /= 0xE0 ;
				dst[bi + 1] = (byte)(value % 0xE0) ;
				value /= 0xE0 ;
				dst[bi] = (byte)value ;

				value = 0 ;
				bi += 3 ;
			}
		}

		len = len % 4 ;
		if(len > 0) {
			len -= 1 ;
			for(int j = len-1; j >= 0; --j, value /= 0xE0)
				tmp[j] = (byte)(value % 0xE0) ;

			System.arraycopy(tmp, 0, dst, bi, len);
			bi += len ;
		}
		
		char[] ret = new char[bi] ;
		int ri = 0 ;
		for(byte i = 0; i < bi; ++i) {
			val = dst[i] & 0xFF;
			if(val < 0x80) {
				ret[ri++] = (char)val ;
			}
			else if(val < 0xC0) {
				ret[ri++] = (char)(0xAC00 + (val & 0x3F) * 0xC0 + (dst[++i] & 0xFF)) ;
			}
			else {
				val = val & 0x1F ;
				val*= 0xC0 ;
				val+= (dst[++i] & 0xFF) ;
				val*= 0xC0 ;
				val+= (dst[++i] & 0xFF) ;
				if(val > 0xFFFF) {
					val -= 0x10000;
					ret[ri++] = (char)(0xD800 + (val >> 10)) ;
					ret[ri++] = (char)(0xDC00 + (val & 0x03FF)) ;
				}
				else
					ret[ri++] = (char)val ;
			}
		}
		
		return new String(ret, 0, ri) ;
	}

	public static void main(String[] args) {
		try {
			if(true)
			{
				String src = "http://test.com:8080/han58.do?name=가나다 ㄱㄴ※\n可" ;
				long encode = 0, decode = 0, encode64 = 0, decode64 = 0 ;
				long start_time = 0 ;
				String tmp = "", out = "", tmp64 = "", out64 = "" ;
				
				Encoder base64encoder = java.util.Base64.getEncoder() ;
				Decoder base64decoder = java.util.Base64.getDecoder() ;
				{
					start_time = System.nanoTime() ;
					for(int i = 0; i < 1000; ++i)
						tmp = HAN58.encode(src) ;
					encode += System.nanoTime() - start_time ;

					start_time = System.nanoTime() ;
					for(int i = 0; i < 1000; ++i)
						tmp64 = base64encoder.encodeToString(src.getBytes("utf8")) ;
					encode64 += System.nanoTime() - start_time ;
					
					start_time = System.nanoTime() ;
					for(int i = 0; i < 1000; ++i)
						out = HAN58.decode(tmp) ;
					decode += System.nanoTime() - start_time ;

					start_time = System.nanoTime() ;
					for(int i = 0; i < 1000; ++i)
						out64 = new String(base64decoder.decode(tmp64), "utf8") ;
					decode64 += System.nanoTime() - start_time ;
					
					assert src.equals(out) : "src.equals(out) == false" ;
					assert src.equals(out64) : "src.equals(out64) == false" ;
				}
				System.out.println("encode = " + encode/1000 + " nano sec") ;
				System.out.println("    64 = " + encode64/1000 + " nano sec") ;
				System.out.println("decode = " + decode/1000 + " nano sec") ;
				System.out.println("    64 = " + decode64/1000 + " nano sec") ;
				System.out.printf("encode / decode : %5.2f %%%n", encode * (float)100 / (float)decode);
				System.out.printf("encode /     64 : %5.2f %%%n", encode * (float)100 / (float)encode64);
				System.out.printf("decode /     64 : %5.2f %%%n", decode * (float)100 / (float)decode64);
				System.out.println("---------------------------------") ;
			}
			
			{
				String src = "http://test.com:8080/han58.do?name=가나다 ㄱㄴ※\n可" ;
				System.out.println("src["+src.length()+"]:" + src) ;
				String han58__tmp = HAN58.encode(src) ;
				System.out.println("han58__tmp:" + han58__tmp) ;
				String han58__out = HAN58.decode(han58__tmp) ;
				System.out.println("han58__out:" + han58__out) ;
				String base64_tmp = java.util.Base64.getEncoder().encodeToString(src.getBytes("utf8")) ;
				System.out.println("base64_tmp:" + base64_tmp) ;
				String base64_out = new String(java.util.Base64.getDecoder().decode(base64_tmp), "utf8") ;
				System.out.println("base64_out:" + base64_out) ;
				if(src.equals(han58__out) == false)	System.err.println("src.equals(han58__out) == false") ;
			}
			System.out.println("---------------------------------") ;
			{
				// [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
				String src = "http://test.com:8080/han58.do?name=가나다 ㄱㄴ※\n可🐘1" ;
				//String src = "http://test.com:8080/han58.do?name=가나다 ㄱㄴ※\n可" ;
				//String src = "" ;
				//src += (char)0xD83D ;
				//src += (char)0xDC18 ;
				//src += (char)0x31 ;
				System.out.println("src:" + src) ;
				String tmp = HAN58.encode(src) ;
				System.out.println("tmp:" + tmp) ;
				String out = HAN58.decode(tmp) ;
				System.out.println("out:" + out) ;
				if(src.equals(out) == false)	System.err.println("src.equals(out) == false") ;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
