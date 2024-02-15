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
 * @history 2024-02-06 encode(), decode() 만듦.<br/>
 */
var HAN58 = (function() {
/*********
	function num2hex(num, len) {
		var i = 0, str = '', hex_tab = '0123456789ABCDEF';
		for(i = 0; i < len; ++i) {
			str = hex_tab.charAt(num & 0x0F) + str;
			//num >>= 4 ;   // 32bit 미만에서만 사용가능
			num /= 16 ;     // 32bit 초과시에도 사용가능
		}
		return str;
	}

	function print(bin) {
		var line = '', i = 0, len = bin.length ;
		for(; i < len; ++i) {
			line += num2hex(bin[i], 2) + ' ' ;
			if(i % 16 == 15) {
				console.log(line) ;
				line = '' ;
			}
		}
		console.log(line) ;
	}
*********/
	
	var toBase58 = [
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v'
	];
	
	var toUTF8 = function(text) {
		var i = 0, utf16 = 0, ret = [], j = 0, len = text.length, surrogate = 0 ;
		ret.length = len * 3 ;
		for(i = 0; i < len; ++i) {
			utf16 = text.charCodeAt(i) ;
			//console.log("utf16:0x" + num2hex(utf16, 4)) ;
			if(utf16 < 0x80) {
				ret[j++] = utf16 ;
			}
			else if(0xAC00 <= utf16 && utf16 <= 0xD7FF) {
				utf16 -= 0xAC00 ;
				ret[j++] = 0x80 | Math.floor(utf16 / 0xC0) ;
				ret[j++] = utf16 % 0xC0 ;
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

				ret[j++] = 0xC0 |  Math.floor(utf16 / 0x9000) ;	// 0xC0 * 0xC0 = 0x9000
				utf16 %= 0x9000 ;
				ret[j++] = Math.floor(utf16 / 0xC0) ;
				ret[j++] = utf16 % 0xC0 ;
			}
		}
		ret.length = j ;
		return ret ;
	} ;

	var encode = function(text) {
		var utf8 = toUTF8(text) ;
		var len = utf8.length ;
		var ret = [] ;
		ret.length = Math.floor(len / 3) * 4 + ((len % 3 > 0) ? len % 3 + 1 : 0) ;
		var value = 0 ;
		
		var i = 0, j = 0, ri = 0 ;
		for(i = 0; i < len; ++i) {
			value = value * 0xE0 + utf8[i] ;

			if(i % 3 == 2) {
				ret[ri + 3] = toBase58[value % 58];
				value = Math.floor(value / 58);
				ret[ri + 2] = toBase58[value % 58];
				value = Math.floor(value / 58);
				ret[ri + 1] = toBase58[value % 58];
				value = Math.floor(value / 58);
				ret[ri] = toBase58[value];

				value = 0 ;
				ri += 4 ;
			}
		}
		
		len = utf8.length % 3 ;
		if(len > 0) {
			for(j = len; j >= 0; --j, value = Math.floor(value / 58))
				ret[ri + j] = toBase58[value % 58] ;
		}
		
		return ret.join('') ;
	} ;

	var fromBase58 = Array(128) ;
	for (i = 0; i < fromBase58.length; ++i)
		fromBase58[i] = -1 ;
	for (i = 0; i < toBase58.length; ++i)
		fromBase58[toBase58[i].charCodeAt(0)] = i ;

	var fromUTF8 = function(utf8) {
		var val = 0, i = 0, len = utf8.length, ret = [], ri = 0 ;
		ret.length = len ;
		for(i = 0; i < len; ++i) {
			val = utf8[i] & 0xFF ;
			if(val < 0x80) {
				ret[ri++] = val ;
			}
			else if(val < 0xC0) {
				ret[ri++] = 0xAC00 + (val & 0x3F) * 0xC0 + (utf8[++i] & 0xFF) ;
			}
			else {
				val = val & 0x1F ;
				val*= 0xC0 ;
				val+= utf8[++i] ;
				val*= 0xC0 ;
				val+= utf8[++i] ;
				if(val > 0xFFFF) {
					val -= 0x10000;
					ret[ri++] = 0xD800 | (val >> 10) ;
					ret[ri++] = 0xDC00 | (val & 0x03FF) ;
				}
				else
					ret[ri++] = val ;
			}
		}
		ret.length = ri ;
		return String.fromCharCode.apply(null, ret) ;
	} ;

	var decode = function(text) {
		var len = text.length ;
		if(len % 4 == 1)    throw "invalid HAN58 length" ;
		
		var dst = [] ;
		dst.length = Math.floor(len / 4) * 3 + ((len % 4 > 0) ? len % 4 - 1 : 0) ;
		var value = 0 ;
		var ch = 0 ;
		
		var bi = 0 ;
		var i = 0, j = 0;
		for(i = 0; i < len; ++i) {
			ch = text.charCodeAt(i) ;
			if(ch >= 0x80)
				throw "invalid HAN58 character " + ch ;
			
			value = value * 58 + fromBase58[ch] ;

			if(i % 4 == 3) {
				dst[bi + 2] = value % 0xE0 ;
				value = Math.floor(value / 0xE0) ;
				dst[bi + 1] = value % 0xE0 ;
				value = Math.floor(value / 0xE0) ;
				dst[bi] = value ;

				value = 0 ;
				bi += 3 ;
			}
		}
		
		len = len % 4 ;
		if(len > 0) {
			len -= 1 ;
			for(j = len-1; j >= 0; --j, value = Math.floor(value / 0xE0))
				dst[bi + j] = value % 0xE0 ;
		}

		return fromUTF8(dst) ;
	} ;

	return {
		encode : encode,
		decode : decode,
	} ;
}()) ;

// for node
if(typeof module === 'object')
	module.exports = HAN58 ;
