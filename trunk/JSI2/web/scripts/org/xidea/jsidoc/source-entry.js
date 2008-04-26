/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: source-entry.js,v 1.4 2008/02/24 08:58:15 jindw Exp $
 */


/**
 * @public 
 * @constructor
 * @param <String> source 
 */
function SourceEntry(source){
    var timestamp = new Date().getTime();
    this.source = source;
    this.parse();
    this.docEntries = [];
    this.docIds = [];
    this.docMap = {};
    this.filedocs = [];
    for(var i=0;i<this.partitions.length;i++){
        var p = this.partitions[i];
        if(p.type == 'document'){
            this.docEntries.push(new DocEntry(this,p.begin,p.end));
        }
    }
    for(var i=0;i<this.docEntries.length;i++){
        var doc = this.docEntries[i];
        if(doc.isFiledoc()){
            this.filedocs.push(doc);
        }else{
            var id = doc.getId();
            this.docMap[id] = doc;
            this.docIds.push(id);
        }
    }
    this.timeSpent = new Date().getTime()-timestamp;
    //$log.info(this.timeSpent);
}
var sourceEntryMap = {};
SourceEntry.require = function(file,packageName){
    if(packageName){
        var file = packageName.replace(/\.|$/g,"/")+file;
    }
    var source = sourceEntryMap[file];
    if(!source){
        var text = JSIDoc.getSource(file);
        source = sourceEntryMap[file] = new SourceEntry(text||'/* empty */');
    }
    return source;
}




SourceEntry.prototype = new ECMAParser();
SourceEntry.prototype.getDescription = function(id){
    if(!("_description" in  this)){
        var infos = [];
        for(var i = 0;i<this.filedocs.length;i++){
            infos.push(this.filedocs[i].description || '');
        }
        this._description = infos.join('\r\n');
        if(infos.length == 0 && this.partitions.length){
            var t = this.partitions[0];
            if(t.type == "comment"){
                this._description = t.value.replace(/^\s*\/\//,'');
            }else if(t.type == "muti-comment"){
                this._description = t.value.replace(/(?:^\s*\/\*)|(?:\*\/\s*$)/g,'').replace(/^\s*\*\s?/gm,'');
            }
        }
    }
    return this._description;
}
SourceEntry.prototype.getDocEntry = function(id){
    return this.docMap[id];
}
SourceEntry.prototype.getTopDocEntries = function(id){
    var rtv = [];
    for(var i = 0;i<this.docEntries.length;i++){
        if(this.docEntries[i].isTop()){
            rtv.push(this.docEntries[i]);
        }
    }
    return rtv;
}
