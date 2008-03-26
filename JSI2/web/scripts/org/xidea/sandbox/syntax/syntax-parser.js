/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: syntax-parser.js,v 1.6 2008/02/25 13:51:19 jindw Exp $
 */

/**
 * @internal
 * 构建分区正则
 */
function buildPartitioner(ps){
    if(ps.exec){//is RegExp
        return ps;
    }else{
        if(ps instanceof Array){
            var ps = ps.join('|');
        }
        return new RegExp(ps,'m');
    }
}
/**
 * @internal
 * 构建渲染函数
 */
function buildRenderer(regexp,replaceText){
    if(!(regexp instanceof RegExp)){
        regexp = new RegExp(regexp,'gm');
    }
    return function(text){
        return text.replace(regexp,replaceText);
    };
}

/**
 * 抽象语法分析器,用于扩展语法解析基类.
 * <p>可以通过这个函数直接创建prototype子链,实现子解析器继承</p>
 * @public
 * @abstract
 * @constructor
 * @param <Object> partitionerMap 新解析器的分区正则表
 * @param <Object> rendererMap 新解析器的分区渲染表
 * fixbug for preview version
 */
var SyntaxParser = function(partitionerMap,rendererMap){
    /**
     * 解析器的源码数据
     * <p>在子实现中,必须实现该属性,以便parse方法调用</p>
     * @public
     * @typeof string
     * @id SyntaxParser.this.source
     */
    //this.source = null;
    //this.initialize(source);
    /**
     * 新解析器的分区正则表
     * @public
     * @typeof object
     * @id SyntaxParser.prototype.partitionerMap
     */
    /**
     * 新解析器的分区渲染表
     * @public
     * @typeof object
     * @id SyntaxParser.prototype.rendererMap
     */
    if(partitionerMap){
        this.partitionerMap = {};
        for(var n in partitionerMap){
            this.partitionerMap[n] = buildPartitioner(partitionerMap[n])
        }
    }
    if(rendererMap){
        this.rendererMap = {};
        for(var n in rendererMap){
            var renderer = rendererMap[n];
            if(renderer instanceof Function){//is Function
                this.rendererMap[n] = renderer;
            }else{
                if(renderer instanceof Array){
                    renderer = '(\\b'+renderer.join('\\b|\\b') + '\\b)';
                }
                var reg =  new RegExp(renderer,'g');
                this.rendererMap[n] = buildRenderer(reg,"<b class='keyword- keyword-$1-'>$1</b>");
            }
        }
    }
}

SyntaxParser.prototype = {
    /**
     * 解析原码内容,并返回解析结果(行迭代起)
     * @public 
     * @owner SyntaxParser.prototype
     * @return <LineIterator>
     */
    parse : function(){
        if(!this.lines){
            /**
             * 深度信息
             */
            this.depths = [];
            /**
             * 描点信息
             */
            this.anchors = [];
            /**
             * 行位置信息（迭代起用）
             */
            this.lines = [];
            
            var lineRegExp = /\r\n|\n|\r/g;
                //new RegExp("\\r\\n|\\n|\\r",'g');
            var begin = 0,lineMatch;
            while(lineMatch=lineRegExp.exec(this.source)){
                this.lines.push({begin:begin,end:lineMatch.index});
                begin = lineMatch.index+lineMatch[0].length;
            }
            if(begin<this.source.length){
                this.lines.push({begin:begin,end:this.source.length});
            }
            //alert(this.lines.length)
            this.partitions = buildPartitions(this);
        }
        return new LineIterator(this);
    },
    
    /**
     * 默认的分区类型
     * @public
     * @owner SyntaxParser.prototype
     */
    defaultType : "code",
    /**
     * 类别鉴别方法,子类中可用更加高效的方法代替
     * @owner SyntaxParser.prototype
     * @public
     */
    guessType : function(p){
        for(var n in this.partitionerMap){
            if(this.partitionerMap[n].test(p)){
                return n;
            }
        }
        return this.defaultType;
    },

    /**
     * 深度计算方法
     * @owner SyntaxParser.prototype
     * compute depths.
     * default implements is scan the code for '{'|'}',if '{' <b>++depth</b> else <b>depth--</b>
     * @protected
     */
    computeDepth : function(partition,dep){
        switch(partition.type){
            case 'code':
                var reg = new RegExp("\{|\}",'g');
                var match, code = partition.value;
                while(match = reg.exec(code)){
                    if(match[0] == '{'){
                        this.depths.push([
                            partition.begin+match.index,
                            dep++,dep]);
                    }else{
                        this.depths.push([partition.begin+match.index,
                            dep--,dep]);
                    }
                }
                break;
            case 'muti-comment':
            case 'document':
                this.depths.push([
                    partition.begin,
                    dep++,dep]);
                this.depths.push([
                    partition.end-1,
                    dep--,dep]);
                break;
        }
        return dep;
    }
};

function Partition(value,type,begin,end){
    this.value = value;
    this.type = type;
    this.begin = begin;
    this.end = end;
}
if("$debug"){
    Partition.prototype.toString = function(){
        return '{value:'+this.value+
                     ',type:'+this.type+
                     ',begin:'+this.begin+
                     ',end:'+this.end+'}\n';
                     
    }
}
/**
 * 行迭代器.
 * <p>用于当语法解析器的分析结果逐行渲染</p>
 * @protected
 * @constructor
 * @param <SyntaxParser> sourceParser
 */
function LineIterator(sourceParser){
    /**
     * 对应的代码解析器
     */
    this.parser = sourceParser;
    /**
     * 源代码
     */
    this.source = sourceParser.source;
    /**
     * 分区表
     */
    this.partitions = sourceParser.partitions;
    this.lines = sourceParser.lines;
    this.depths = sourceParser.depths;
    this.anchors = sourceParser.anchors;
    this.depth = 0;
    this.nextDepth = 0;
    this.depthStack = [-1];
    this.depthStart = -1
    this.partitionIndex = 0;
    this.lineIndex = 0;
    this.depthIndex = 0;
    this.anchorsIndex = 0;
}
LineIterator.prototype = {
    /**
     * 是否还有未读行
     * @owner LineIterator.prototype
     */
    hasNext : function(){
        return (this.partitions[this.partitionIndex] && this.lines[this.lineIndex]);
    },
    /**
     * 获取全部内容
     * @owner LineIterator.prototype
     */
    getContent : function(){
        var buf = [];
        while(true){
            var item = this.next();
            if(item!=null){
                buf.push(item)
            }else{
                return buf.join('\n');
            }
        }
        ;
    },
    
    /**
     * {position:partition.begin+match.index,
                            preDepth:dep,minDepth:--dep,nextDepth:dep,
                            type:'close'});
     * 下一行数据
     * @owner LineIterator.prototype
     */
    next : function(){
        var partition = this.partitions[this.partitionIndex];
        var line = this.lines[this.lineIndex];
        //alert(line.begin+"/"+line.end)
        if(!partition || !line){
            return null
        }
        try{
            //dell with depths
            var depth = this.depths[this.depthIndex];
            if(depth != null){
                if(depth[0]<line.end){
                    this.depth = Math.min(depth[1],depth[2]);
                    while(depth = this.depths[++this.depthIndex]){
                        if(depth[0]<line.end){
                            this.depth = Math.min(this.depth,depth[1],depth[2]);
                        }else{
                            break;
                        }
                    }
                    if(depth){
                        this.nextDepth = depth[1];
                    }else{
                        this.nextDepth = 0;
                        this.depth = 0;
                        this.depthStack.length = 0;
                        this.depthStart = -2;
                    }
                }else{
                    this.nextDepth = this.depth = depth[1];
                }
            }
            //dell with depth stack
            var i = this.depth - this.depthStack.length+1;
            if(i>0){
                while(i-->0){
                    this.depthStack.push(this.lineIndex);
                    this.depthStart = this.lineIndex;
                }
            }else if(i<0){
                this.depthStack.length = this.depth+1;
                this.depthStart = this.depthStack[this.depth];
            }
            //dell with anchors
            var anchor = this.anchors[this.anchorsIndex];
            this.anchor = "";
            if(anchor && anchor.position<line.end){
                do{
                    this.anchor += "<a name=\""+anchor.name+"\" />";
                    anchor = this.anchors[++this.anchorsIndex];
                }while(anchor && anchor.position<line.end)
            }
            //dell with line;
            if(partition.end>=line.end){
                return this.render(line.begin,line.end,partition.type);
            }else{
                 var buf = [];
                 var i = line.begin;
                 while(partition.end<line.end){
                     buf.push(this.render(i,i=partition.end,partition.type));
                     partition = this.partitions[++this.partitionIndex];
                 }
                 buf.push(this.render(i,line.end,partition.type));
                 return buf.join('');
            }
        }finally{
            this.lineIndex++; 
        }
    },
    
    /**
     * 渲染指定文本段
     * @owner LineIterator.prototype
     */
    render : function(begin,end,type){
        if(end>begin){
            var text = this.source.substring(begin,end);
            text = encodeText(text);
            //.replace(/ /g,"&nbsp;").replace(/\t/g,"&nbsp;&nbsp;&nbsp;&nbsp;");
            var renderer = this.parser.rendererMap[type];
            if(renderer){
                text = renderer.call(this.parser,text);
                //text.replace(/@([a-zA-Z\-]+)/,this.tagReplacer);
            }
            return "<span class='type-"+type+"-'>"+text+"</span>";
        }else{
            return '';
        }
    }
};

/*
 * build partitions(fill blank part and compute depths)
 * @protected
 */
function buildPartitions(parser){
    var pattern = [];
    var partitions=[];
    var match;
    var source = parser.source;
    var newPartitions = [];
    var pos = 0;
    var depth = 0;
    for(var n in parser.partitionerMap){
        pattern.push(parser.partitionerMap[n].source);
    }
    pattern = new RegExp(pattern.join('|'),"gm");
    while(match = pattern.exec(source)){
        var token = match[0];
        var type = parser.guessType(token);
        partitions.push(new Partition(token,type,match.index,match.index + token.length));
    }
    for(var i = 0;i<partitions.length;i++){
        var partition = partitions[i];
        if(partition.begin >pos){
            var bp = new Partition(parser.source.substring(pos,partition.begin),parser.defaultType,pos,partition.begin);
            depth = parser.computeDepth(bp,depth);
            newPartitions.push(bp);
        }
        depth = parser.computeDepth(partition,depth);
        newPartitions.push(partition);
        pos = partition.end;
    }
    if(pos<parser.source.length){
        var partition = new Partition(parser.source.substr(pos),parser.defaultType,pos,parser.source.length);
        depth = parser.computeDepth(partition,depth);
        newPartitions.push(partition);
    }
    return newPartitions;
};

function encodeText(text){
    if(text){
        return text.replace(/[\r\n]/g,'').
            replace(/&/g,'&amp;').
            replace(/>/g,'&gt;').
            replace(/</g,'&lt;');
    }
    return text;
};








function ECMAParser(source){
    this.source = source;
}
ECMAParser.prototype = new SyntaxParser(
    {
        'document':'/\\*\\*(?:[^\\*]|\\*[^/])*\\*/'
        ,'muti-comment':'/\\*(?:[^\\*]|\\*[^/])*\\*/'//muti-comment
        ,'comment':'//.*$'                //single-comment
        ,'regexp':'/(?:\\\\.|[^/\\n\\r])+/'    //regexp
        ,'string':['"(?:\\\\(?:.|\\r|\\n|\\r\\n)|[^"\\n\\r])*"',
                   "'(?:\\\\(?:.|\\r|\\n|\\r\\n)|[^'\\n\\r])*'"]    //string
        ,'preprocessor':'^\\s*#.*'                         //process
    },
    {
        /**
         * keywords Regexp.
         * default is for javascript 
         * @protected
         */
        'code':['abstract','boolean','break','byte','case','catch','char','class','const','continue','debugger',
            'default','delete','do','double','else','enum','export','extends','false','final','finally','float',
            'for','function','goto','if','implements','im3port','in','instanceof','int','interface','long','native',
            'new','null','package','private','protected','prototype','public','return','short','static','super','switch',
            'synchronized','this','throw','throws','transient','true','try','typeof','var','void','volatile','while','with'],
        'document':buildRenderer(/@([\w-_\.\d]+)/g,"<b class='tag- tag-$1-'>@$1</b>")
    }
);



/**
 * 针对JavaScript优化
 * guess the type of given partition.
 * default is for javascript 
 * @protected
 */
ECMAParser.prototype.guessType = function(partition){
    var type = "";
    switch(partition.charAt(0)){
        case '/':
            var secondChar = partition.charAt(1);
            if(secondChar == '/'){
                type = "comment";
            }else if(secondChar == '*'){
                if(partition.charAt(2) == '*' && partition.charAt(3) != '/'){
                    type = "document";
                }else{
                    type = "muti-comment";
                }
            }else{
                type = "regexp";
            }
            break;
        case '\'':
        case '"':
            type = "string";
            break;
        case ' ':
        case '#':
            type = "preprocessor";
            break;
    }
    return type;
};
