//eval(
function C(i){
  return i<62?
    String.fromCharCode(i+=
      i<26?65
        :i<52?71//97-26
          :-4//48-26-26
    )
      :i<63?'_'
        :i<64?'$'
          :C(i>>6)+C(i&63)
}
//test
for(var i =0;i<100;i++){
  document.write(C(i));
}
function C(i){//i<=63
  return i<62?String.fromCharCode(i+=
      i<26?65
        :i<52?71//97-26
          :-4//48-26-26
            
    ):i<63?'_':'$';
}
function C(i){//i<=62
  return String.fromCharCode(i+=
      i<26?65
        :i<52?71//97-26
          :i<62?-4//48-26-26
            :33
    )
}
function C(i){//i<=61
  return String.fromCharCode(i+=
      i<26?65
        :i<52?71
          :-4
    )
}
function C(i){//i<=51
  return String.fromCharCode(i+=
      i<26?65
        :71
    )
}
function C(i){//i<=25
  return String.fromCharCode(i+65)
}

function(code,keylist,index,maxindex,regexp,map,empty,buf){
  C();//for replace
  while(index>0){
    map[C(maxindex--)]=keylist[--index];
  }
  //index ==0 maxindex = pre start
  function replacer(key){
    return map[key] == empty[key]?key:map[key];
  }
  if(''.replace(/^/,String)){
    var matchs = code.match(regexp);
    var token = matchs[0];
    var ops = code.split(regexp);
    var i=0;
    //print(matchs);
    //print(ops);
    if(code.indexOf(ops[0])){//非ops打头
      ops = [''].concat(ops);
    }
    //print(ops);
    //ops.length-code.length = 1|0;
    do{
      buf[index++] = ops[i++];
      buf[index++] = replacer(token);
    }while(token = matchs[i]);
    buf[index++] = ops[i]||'';
    return buf.join('');
  }
  return code.replace(regexp,replacer);
}(_code,_keylist,_index,_maxindex,/[\w\$]+/g,{},{},[])


//无优势
function(code,tokens,tokenIndex,tokenCode,lastIndex){
  //code=code.split(',').join(',');
  //code=code.match(/[\w\$]+|[^\w\$]+/g).join("");
  code=code.match(/[\w\$]+|[^\w\$]+/g);
  lastIndex = code.length-2;
  function C(A){return A<62?String.fromCharCode(A+=A<26?65:A<52?71:-4):A<63?'_':A<64?'$':C(A>>6)+C(A&63)};
  //var C="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_$".split("");
  var x={};
  var y ={};
//341,349
while(tokenIndex)x[C(tokenCode--)]=tokens[--tokenIndex];

//alert(tokenCode + C(tokenCode)+x['K'])
  while(lastIndex>=0) {
    var c = code[lastIndex] ;
    //if(lastIndex<5){alert(x[c])}
    if(x[c] != y[c]){
      code[lastIndex] = x[c];
    }
    lastIndex-=2;
  }
  return code.join('');
}


function(code,keylist,index,maxindex,map,empty){
  C();//for replace
  while(index>0){
    map[C(maxindex--)]=keylist[--index];
  }
  return code.replace(/[\w\$]+/g,function(key){
    return map[key] == empty[key]?key:map[key];
  });
}(_code,_keylist,_index,_maxindex,{},{})
//);