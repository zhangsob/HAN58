// for node
const HAN58 = require('./HAN58.js') ;

function print(msg) {
	if(typeof document !== 'undefined' && typeof msg === 'string')
		document.write(msg.replace(/\n/g,'<br/>') + '<br/>') ;
	console.log(msg) ;
}

try {
if (true) {
	var src = "http://test.com:8080/han58.do?name=가나다 ㄱㄴ※\n可" ;
	var encode = 0.0, decode = 0.0 ;
	var tmp, out, start ;
	{
		start = performance.now();
		for(var i = 0; i < 1000; ++i)
			tmp = HAN58.encode(src) ;
		encode += performance.now() - start ;

		start = performance.now();
		for(var i = 0; i < 1000; ++i)
			out = HAN58.decode(tmp) ;
		decode += performance.now() - start ;

		if(src !== out)   print("src !== out") ;
	}
	print("encode:" + (encode / 1000) + " millisec") ;
	print("decode:" + (decode / 1000) + " millisec") ;
	print("encode / decode : " + Math.floor(encode * 100.0 / decode) + " %");
}
	var src0 = "http://test.com:8080/han58.do?name=가나다 ㄱㄴ※\n可" ;
	print('src0['+src0.length+']:' + src0) ;
	var tmp0 = HAN58.encode(src0) ;
	print("tmp0:" + tmp0) ;
	var out0 = HAN58.decode(tmp0) ;
	print("out0:" + out0) ;
	if(src0 !== out0)   print("src0 !== out0") ;

	// [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
	var src1 = "http://test.com:8080/han58.do?name=가나다 ㄱㄴ※\n可🐘1" ;
	print('src1['+src1.length+']:' + src1) ;
	var tmp1 = HAN58.encode(src1) ;
	print("tmp1:" + tmp1) ;
	var out1 = HAN58.decode(tmp1) ;
	print("out1:" + out1) ;

	if(src1 !== out1)   print("src1 !== out1") ;
} catch(e) {
	print(e) ;
}
