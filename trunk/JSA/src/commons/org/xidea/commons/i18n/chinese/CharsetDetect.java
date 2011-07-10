//package org.xidea.commons.i18n.chinese;
//
//
//public class CharsetDetect {
//       /* Support for Chinese(GB2312) characters */
//       // #define isgb2312head(c) (0xa1<=(uchar)(c) && (uchar)(c)<=0xf7)
//       // #define isgb2312tail(c) (0xa1<=(uchar)(c) && (uchar)(c)<=0xfe)
//       public static boolean isGB2312(byte head, byte tail) {
//              int iHead = head & 0xff;
//              int iTail = tail & 0xff;
//              return ((iHead >= 0xa1 && iHead <= 0xf7 && iTail >= 0xa1 && iTail <= 0xfe) ? true
//                            : false);
//       }
// 
//       /* Support for Chinese(GBK) characters */
//       // #define isgbkhead(c) (0x81<=(uchar)(c) && (uchar)(c)<=0xfe)
//       // #define isgbktail(c) ((0x40<=(uchar)(c) && (uchar)(c)<=0x7e)
//       // || (0x80<=(uchar)(c) && (uchar)(c)<=0xfe))
//       public static boolean isGBK(byte head, byte tail) {
//              int iHead = head & 0xff;
//              int iTail = tail & 0xff;
//              return ((iHead >= 0x81 && iHead <= 0xfe && (iTail >= 0x40
//                            && iTail <= 0x7e || iTail >= 0x80 && iTail <= 0xfe)) ? true
//                            : false);
//       }
// 
//       /* Support for Chinese(BIG5) characters */
//       // #define isbig5head(c) (0xa1<=(uchar)(c) && (uchar)(c)<=0xf9)
//       // #define isbig5tail(c) ((0x40<=(uchar)(c) && (uchar)(c)<=0x7e)
//       // || (0xa1<=(uchar)(c) && (uchar)(c)<=0xfe))
//       public static boolean isBIG5(byte head, byte tail) {
//              int iHead = head & 0xff;
//              int iTail = tail & 0xff;
//              return ((iHead >= 0xa1 && iHead <= 0xf9 && (iTail >= 0x40
//                            && iTail <= 0x7e || iTail >= 0xa1 && iTail <= 0xfe)) ? true
//                            : false);
//       }
// 
//       public static void main(String[] args) {
//              String sGB = "爱";
//              String sGBK = "愛";
//              String sBIG5 = "稲";
//              byte[] sChars = null;
//              sChars = sGB.getBytes();
//              System.out.println(sGB + " is "
//                            + CharsetDetect.isGB2312(sChars[0], sChars[1])
//                            + " for GB2312;" + CharsetDetect.isGBK(sChars[0], sChars[1])
//                            + " for GBK," + CharsetDetect.isBIG5(sChars[0], sChars[1])
//                            + " for BIG5");
//              sChars = sGBK.getBytes();
//              System.out.println(sGBK + " is "
//                            + CharsetDetect.isGB2312(sChars[0], sChars[1])
//                            + " for GB2312;" + CharsetDetect.isGBK(sChars[0], sChars[1])
//                            + " for GBK," + CharsetDetect.isBIG5(sChars[0], sChars[1])
//                            + " for BIG5");
//              sChars = sBIG5.getBytes();
//              System.out.println(sBIG5 + " is "
//                            + CharsetDetect.isGB2312(sChars[0], sChars[1])
//                            + " for GB2312;" + CharsetDetect.isGBK(sChars[0], sChars[1])
//                            + " for GBK," + CharsetDetect.isBIG5(sChars[0], sChars[1])
//                            + " for BIG5");
//       }
//}