/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @author ShiRongjiu(TryDoFor)[trydofor.com]
 * @version $Id: chinese-calendar.js,v 1.2 2008/02/19 13:30:12 jindw Exp $
 */

//from ShiRongjiu(TryDoFor)[trydofor.com]
// chinese lunar
var TG      = ["甲","乙","丙","丁","戊","己","庚","辛","壬","癸"];
var DZ      = ["子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"];
var SX      = ["鼠","牛","虎","兔","龙","蛇","马","羊","猴","鸡","狗","猪"];
var NUM_CN  = ["一","二","三","四","五","六","七","八","九","十"];
var MON_CN  = ["正","二","三","四","五","六","七","八","九","十","冬","腊"];
var DAY_CN  = ["日","一","二","三","四","五","六"];

var MON_POS = [0,31,59,90,120,151,181,212,243,273,304,334];
var LUNAR_TABLE = [
    0xA4B,0x5164B,0x6A5,0x6D4,0x415B5,0x2B6,0x957,0x2092F,0x497,0x60C96,    // 1921-1930
    0xD4A,0xEA5,0x50DA9,0x5AD,0x2B6,0x3126E, 0x92E,0x7192D,0xC95,0xD4A,     // 1931-1940
    0x61B4A,0xB55,0x56A,0x4155B, 0x25D,0x92D,0x2192B,0xA95,0x71695,0x6CA,   // 1941-1950
    0xB55,0x50AB5,0x4DA,0xA5B,0x30A57,0x52B,0x8152A,0xE95,0x6AA,0x615AA,    // 1951-1960
    0xAB5,0x4B6,0x414AE,0xA57,0x526,0x31D26,0xD95,0x70B55,0x56A,0x96D,      // 1961-1970
    0x5095D,0x4AD,0xA4D,0x41A4D,0xD25,0x81AA5, 0xB54,0xB6A,0x612DA,0x95B,   // 1971-1980
    0x49B,0x41497,0xA4B,0xA164B, 0x6A5,0x6D4,0x615B4,0xAB6,0x957,0x5092F,   // 1981-1990
    0x497,0x64B, 0x30D4A,0xEA5,0x80D65,0x5AC,0xAB6,0x5126D,0x92E,0xC96,     // 1991-2000
    0x41A95,0xD4A,0xDA5,0x20B55,0x56A,0x7155B,0x25D,0x92D,0x5192B,0xA95,    // 2001-2010
    0xB4A,0x416AA,0xAD5,0x90AB5,0x4BA,0xA5B, 0x60A57,0x52B,0xA93,0x40E95    // 2011-2020
    ];
function getLunarString(year,month,date)
{
    var year_begin = 1921;
    var total,m,n,k;
    var isEnd=false;
    var cYear,cMonth,cDay;

    total=(year-year_begin)*365+
      //Math.floor((year-year_begin)/4)+
      ((year-year_begin)>>>2)+
      MON_POS[month]+date-38;
    if (year%4==0 && month>1)
    {
        total++;
    }

    for(m=0;;m++)
    {
        k=(LUNAR_TABLE[m]<0xfff)?11:12;
        for(n=k;n>=0;n--)
        {
            if(total<=29+(LUNAR_TABLE[m]>>n&1))
            {
            isEnd=true; break;
            }
            total=total-29-(LUNAR_TABLE[m]>>n&1);
        }
        if(isEnd) break;
    }

    cYear=year_begin + m;
    cMonth=k-n+1;
    cDay=total;

    if(k==12)
    {
        if(cMonth==Math.floor(LUNAR_TABLE[m]/0x10000)+1) { cMonth=1-cMonth; }
        if(cMonth>Math.floor(LUNAR_TABLE[m]/0x10000)+1)  { cMonth--; }
    }

    //----------------------------------------
    var str =TG[(cYear-4)%10];   //年干
    str+=DZ[(cYear-4)%12];   //年支
    str+="["+SX[(cYear-4)%12]+"] 年\n";

    if(cMonth<1)
    {
        str+="[闰]"; str+=MON_CN[-cMonth-1];
    }
    else
    {
        str+=MON_CN[cMonth-1];
    }

    str +="月 ";
    if (cDay<11)
        str += "初";
    else if (cDay>10 && cDay<20)
        str += "十";
    else if (cDay>20 && cDay<30)
        str += "廿";
    else if (cDay>30)
        str += "三十";
    else if (cDay==20)
        str += "二十";
    else if (cDay==30)
        str += "三十";
    else
        str += "卅";

    //str+=(cDay<11)?"初":((cDay<20)?"十":((cDay<30)?"廿":"三十"));
    if (cDay%10!=0||cDay==10)
    {
        str+=NUM_CN[(cDay-1)%10];
    }
    return str;
}