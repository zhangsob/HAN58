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
        String src0 = "http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可" ;
        System.out.println("src0["+src0.length()+"]:" + src0) ;
        String an62__tmp0 = AN62.encode(src0) ;
        System.out.println("an62__tmp0:" + an62__tmp0) ;
        String an62__out0 = AN62.decode(an62__tmp0) ;
        System.out.println("an62__out0:" + an62__out0) ;
        String base64_tmp = java.util.Base64.getEncoder().encodeToString(src0.getBytes("utf8")) ;
        System.out.println("base64_tmp:" + base64_tmp) ;
        String base64_out = new String(java.util.Base64.getDecoder().decode(base64_tmp), "utf8") ;
        System.out.println("base64_out:" + base64_out) ;

        // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
		String src1 = "http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可🐘" ;
		System.out.println("src1["+src1.length()+"]:" + src1) ;		// String.length()은 문자갯수가 아니라, UTF16의 길이다. 
		String tmp1 = AN62.encode(src1) ;
		System.out.println("tmp1:" + tmp1) ;
		String out1 = AN62.decode(tmp1) ;
		System.out.println("out1:" + out1) ;

        if(src1.equals(out1))	System.out.println("src1.equals(out1)") ;

    } catch(Exception e) {
        e.printStackTrace();
    }
}
```
-----------------------------------------------------------------------------------
```
src0[43]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
an62__tmp0:QJPMSGcDBxKqT59pP30lEfGUE9WZOXhdCdieS1KqOXeRFbUNWTlJcWWwfKzvXQYGXQk6WQfhvp39
an62__out0:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
base64_tmp:aHR0cDovL3Rlc3QuY29tOjgwODAvYW42Mi5kbz9uYW1lPeqwgOuCmOuLpCDjhLHjhLTigLsK5Y+v
base64_out:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
src1[45]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可🐘
tmp1:QJPMSGcDBxKqT59pP30lEfGUE9WZOXhdCdieS1KqOXeRFbUNWTlJcWWwfKzvXQYGXQk6WQfhvp39ybpT2S
out1:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可🐘
src1.equals(out1)
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
    var src0 = "http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可" ;
    print('src0['+src0.length+']:' + src0) ;
    var tmp0 = AN62.encode(src0) ;
    print("tmp0:" + tmp0) ;
    var out0 = AN62.decode(tmp0) ;
    print("out0:" + out0) ;

    // [ 코끼리 = Unicode : 01F418, UTF16 : D83D DC18, UTF8 : F0 9F 90 98 ]
    var src1 = "http://test.com:8080/an62.do?name=가나다 ㄱㄴ※\n可🐘" ;
    print('src1['+src1.length+']:' + src1) ;
    var tmp1 = AN62.encode(src1) ;
    print("tmp1:" + tmp1) ;
    var out1 = AN62.decode(tmp1) ;
    print("out1:" + out1) ;

    if(src1 === out1)   print("src1 === out1") ;
} catch(e) {
    print(e) ;
}
```
-----------------------------------------------------------------------------------
```
src0[43]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
tmp0:QJPMSGcDBxKqT59pP30lEfGUE9WZOXhdCdieS1KqOXeRFbUNWTlJcWWwfKzvXQYGXQk6WQfhvp39
out0:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可
src1[45]:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可🐘
tmp1:QJPMSGcDBxKqT59pP30lEfGUE9WZOXhdCdieS1KqOXeRFbUNWTlJcWWwfKzvXQYGXQk6WQfhvp39ybpT2S
out1:http://test.com:8080/an62.do?name=가나다 ㄱㄴ※
可🐘
src1 === out1
```
