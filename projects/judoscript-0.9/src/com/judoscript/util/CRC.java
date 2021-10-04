/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 10-17-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.util;

import java.io.*;

public class CRC
{
  public static int crc32 (Object src) throws IOException {
    InputStream is;
    if (src instanceof InputStream) is = (InputStream)src;
    else if (src instanceof File)   is = new FileInputStream((File)src);
    else                            is = new FileInputStream(src.toString());
    byte[] buf = new byte[4196];
    int len;
    int crc = 0;
    while (true) {
      len = is.read(buf);
      if (len > 0) crc = crc32(buf,0,len,crc);
      if (len < buf.length) break;
    }
    return crc;
  }

  public static int crc32 (byte[] data) { return crc32(data,0,-1,0); }
  public static int crc32 (byte[] data, int offset) { return crc32(data,offset,-1,0); }
  public static int crc32 (byte[] data, int offset, int len) { return crc32(data,offset,len,0); }

  public static int crc32 (byte[] data, int offset, int len, int oldCRC) {
    if (len <= 0) len = data.length - offset;
    int end = offset + len;
    if (end > data.length) end = data.length;

    for (; offset < end; ++offset)
      oldCRC = (oldCRC >> 8) ^ crc32table[ data[offset] ^ (oldCRC & 0x0ff) ];

    return oldCRC;
  }

  static int crc32table[] = {
    0x000000000, 0x077073096, 0x0EE0E612C, 0x0990951BA,
    0x0076DC419, 0x0706AF48F, 0x0E963A535, 0x09E6495A3,
    0x00EDB8832, 0x079DCB8A4, 0x0E0D5E91E, 0x097D2D988,
    0x009B64C2B, 0x07EB17CBD, 0x0E7B82D07, 0x090BF1D91,
    0x01DB71064, 0x06AB020F2, 0x0F3B97148, 0x084BE41DE,
    0x01ADAD47D, 0x06DDDE4EB, 0x0F4D4B551, 0x083D385C7,
    0x0136C9856, 0x0646BA8C0, 0x0FD62F97A, 0x08A65C9EC,
    0x014015C4F, 0x063066CD9, 0x0FA0F3D63, 0x08D080DF5,
    0x03B6E20C8, 0x04C69105E, 0x0D56041E4, 0x0A2677172,
    0x03C03E4D1, 0x04B04D447, 0x0D20D85FD, 0x0A50AB56B,
    0x035B5A8FA, 0x042B2986C, 0x0DBBBC9D6, 0x0ACBCF940,
    0x032D86CE3, 0x045DF5C75, 0x0DCD60DCF, 0x0ABD13D59,
    0x026D930AC, 0x051DE003A, 0x0C8D75180, 0x0BFD06116,
    0x021B4F4B5, 0x056B3C423, 0x0CFBA9599, 0x0B8BDA50F,
    0x02802B89E, 0x05F058808, 0x0C60CD9B2, 0x0B10BE924,
    0x02F6F7C87, 0x058684C11, 0x0C1611DAB, 0x0B6662D3D,

    0x076DC4190, 0x001DB7106, 0x098D220BC, 0x0EFD5102A,
    0x071B18589, 0x006B6B51F, 0x09FBFE4A5, 0x0E8B8D433,
    0x07807C9A2, 0x00F00F934, 0x09609A88E, 0x0E10E9818,
    0x07F6A0DBB, 0x0086D3D2D, 0x091646C97, 0x0E6635C01,
    0x06B6B51F4, 0x01C6C6162, 0x0856530D8, 0x0F262004E,
    0x06C0695ED, 0x01B01A57B, 0x08208F4C1, 0x0F50FC457,
    0x065B0D9C6, 0x012B7E950, 0x08BBEB8EA, 0x0FCB9887C,
    0x062DD1DDF, 0x015DA2D49, 0x08CD37CF3, 0x0FBD44C65,
    0x04DB26158, 0x03AB551CE, 0x0A3BC0074, 0x0D4BB30E2,
    0x04ADFA541, 0x03DD895D7, 0x0A4D1C46D, 0x0D3D6F4FB,
    0x04369E96A, 0x0346ED9FC, 0x0AD678846, 0x0DA60B8D0,
    0x044042D73, 0x033031DE5, 0x0AA0A4C5F, 0x0DD0D7CC9,
    0x05005713C, 0x0270241AA, 0x0BE0B1010, 0x0C90C2086,
    0x05768B525, 0x0206F85B3, 0x0B966D409, 0x0CE61E49F,
    0x05EDEF90E, 0x029D9C998, 0x0B0D09822, 0x0C7D7A8B4,
    0x059B33D17, 0x02EB40D81, 0x0B7BD5C3B, 0x0C0BA6CAD,

    0x0EDB88320, 0x09ABFB3B6, 0x003B6E20C, 0x074B1D29A,
    0x0EAD54739, 0x09DD277AF, 0x004DB2615, 0x073DC1683,
    0x0E3630B12, 0x094643B84, 0x00D6D6A3E, 0x07A6A5AA8,
    0x0E40ECF0B, 0x09309FF9D, 0x00A00AE27, 0x07D079EB1,
    0x0F00F9344, 0x08708A3D2, 0x01E01F268, 0x06906C2FE,
    0x0F762575D, 0x0806567CB, 0x0196C3671, 0x06E6B06E7,
    0x0FED41B76, 0x089D32BE0, 0x010DA7A5A, 0x067DD4ACC,
    0x0F9B9DF6F, 0x08EBEEFF9, 0x017B7BE43, 0x060B08ED5,
    0x0D6D6A3E8, 0x0A1D1937E, 0x038D8C2C4, 0x04FDFF252,
    0x0D1BB67F1, 0x0A6BC5767, 0x03FB506DD, 0x048B2364B,
    0x0D80D2BDA, 0x0AF0A1B4C, 0x036034AF6, 0x041047A60,
    0x0DF60EFC3, 0x0A867DF55, 0x0316E8EEF, 0x04669BE79,
    0x0CB61B38C, 0x0BC66831A, 0x0256FD2A0, 0x05268E236,
    0x0CC0C7795, 0x0BB0B4703, 0x0220216B9, 0x05505262F,
    0x0C5BA3BBE, 0x0B2BD0B28, 0x02BB45A92, 0x05CB36A04,
    0x0C2D7FFA7, 0x0B5D0CF31, 0x02CD99E8B, 0x05BDEAE1D,

    0x09B64C2B0, 0x0EC63F226, 0x0756AA39C, 0x0026D930A,
    0x09C0906A9, 0x0EB0E363F, 0x072076785, 0x005005713,
    0x095BF4A82, 0x0E2B87A14, 0x07BB12BAE, 0x00CB61B38,
    0x092D28E9B, 0x0E5D5BE0D, 0x07CDCEFB7, 0x00BDBDF21,
    0x086D3D2D4, 0x0F1D4E242, 0x068DDB3F8, 0x01FDA836E,
    0x081BE16CD, 0x0F6B9265B, 0x06FB077E1, 0x018B74777,
    0x088085AE6, 0x0FF0F6A70, 0x066063BCA, 0x011010B5C,
    0x08F659EFF, 0x0F862AE69, 0x0616BFFD3, 0x0166CCF45,
    0x0A00AE278, 0x0D70DD2EE, 0x04E048354, 0x03903B3C2,
    0x0A7672661, 0x0D06016F7, 0x04969474D, 0x03E6E77DB,
    0x0AED16A4A, 0x0D9D65ADC, 0x040DF0B66, 0x037D83BF0,
    0x0A9BCAE53, 0x0DEBB9EC5, 0x047B2CF7F, 0x030B5FFE9,
    0x0BDBDF21C, 0x0CABAC28A, 0x053B39330, 0x024B4A3A6,
    0x0BAD03605, 0x0CDD70693, 0x054DE5729, 0x023D967BF,
    0x0B3667A2E, 0x0C4614AB8, 0x05D681B02, 0x02A6F2B94,
    0x0B40BBE37, 0x0C30C8EA1, 0x05A05DF1B, 0x02D02EF8D
  };

} // end of class CRC.
