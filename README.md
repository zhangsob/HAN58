# HAN58(Hangul 특화 AlphaNumeric58)
[AN62](https://github.com/zhangsob/AN62)에서 Hangul부분을 특화함.

## 원리
|        Unicode값       |          UTF-8 변형           |              Byte단위 값의 범위                |  비고 |
|------------------------|:------------------------------|:-----------------------------------------------|-------|
| 0x000000&#126;0x00007F | 0xxx xxxx                     | 0x00&#126;0x7F                                 | ASCII |
| 0x00AC00&#126;0x00D7FF | 10xx xxxx yyyy yyyy           | 0x80&#126;0xBF, 0x00&#126;0xBF                 | 한글  |
| 0x000080&#126;0x10FFFF | 110x xxxx yyyy yyyy yyyy yyyy | 0xC0&#126;0xDF, 0x00&#126;0xBF, 0x00&#126;0xBF | 그외  |

그래서, 0x00&#126;0x7F, 0x80&#126;0xBF, 0xC0&#126;0xDF 즉, 0x00&#126;0xDF(224가지).

224<sup>3</sup> &lt; 58<sup>4</sup> (11,239,424 < 11,316,496) 이다. (즉, 224가지 3덩어리를 58가지 4덩어리로 표현가능하다.)  

## 본 자료는 [gitbook](https://zhangsob.gitbook.io/an62/principle) 으로 볼 수 있습니다.

## 지원언어
아래 언어로 소스코드를 올립니다. 
- java : [Java 예](#java)
- javascript : [JavaScript 예](#javascript)

<a name='java'></a>
## Java 예
```java
public static void main(String[] args) {
    try {
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
```
-----------------------------------------------------------------------------------
```
src[44]:http://test.com:8080/han58.do?name=가나다 ㄱㄴ※
可
han58__tmp:QouiSoONCCjZTf1ePY3lEun6EQRvQnfGDeFWPmu7SNBpQ2XE08rsZFminQ8nnQ8qnObH2juR31
han58__out:http://test.com:8080/han58.do?name=가나다 ㄱㄴ※
可
base64_tmp:aHR0cDovL3Rlc3QuY29tOjgwODAvaGFuNTguZG8/bmFtZT3qsIDrgpjri6Qg44Sx44S04oC7CuWPrw==
base64_out:http://test.com:8080/han58.do?name=가나다 ㄱㄴ※
可
---------------------------------
src:http://test.com:8080/han58.do?name=가나다 ㄱㄴ※
可🐘1
tmp:QouiSoONCCjZTf1ePY3lEun6EQRvQnfGDeFWPmu7SNBpQ2XE08rsZFminQ8nnQ8qnObH2juRjDDiA7p
out:http://test.com:8080/han58.do?name=가나다 ㄱㄴ※
可🐘1
```


<a name="javascript"></a>
## JavaScript 예
```javascript
function print(msg) {
    if(typeof document !== 'undefined' && typeof msg === 'string')
        document.write(msg.replace(/\n/g,'<br/>') + '<br/>') ;
    console.log(msg) ;
}

try {
    var src0 = "http://test.com:8080/han58.do?name=가나다 ㄱㄴ※\n可" ;
    print('src0['+src0.length+']:' + src0) ;
    var tmp0 = HAN58.encode(src0) ;
    print("tmp0:" + tmp0) ;
    var out0 = HAN58.decode(tmp0) ;
    print("out0:" + out0) ;

    // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
    var src1 = "http://test.com:8080/han58.do?name=가나다 ㄱㄴ※\n可🐘1" ;
    print('src1['+src1.length+']:' + src1) ;
    var tmp1 = HAN58.encode(src1) ;
    print("tmp1:" + tmp1) ;
    var out1 = HAN58.decode(tmp1) ;
    print("out1:" + out1) ;

    if(src1 === out1)   print("src1 === out1") ;
} catch(e) {
    print(e) ;
}
```
-----------------------------------------------------------------------------------
```
src0[44]:http://test.com:8080/han58.do?name=가나다 ㄱㄴ※
可
tmp0:QouiSoONCCjZTf1ePY3lEun6EQRvQnfGDeFWPmu7SNBpQ2XE08rsZFminQ8nnQ8qnObH2juR31
out0:http://test.com:8080/han58.do?name=가나다 ㄱㄴ※
可
src1[47]:http://test.com:8080/han58.do?name=가나다 ㄱㄴ※
可🐘1
tmp1:QouiSoONCCjZTf1ePY3lEun6EQRvQnfGDeFWPmu7SNBpQ2XE08rsZFminQ8nnQ8qnObH2juRjDDiA7p
out1:http://test.com:8080/han58.do?name=가나다 ㄱㄴ※
可🐘1
src1 === out1
```
