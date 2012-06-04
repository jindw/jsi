var setupJSRequire = require('../lib/js-filter').setupJSRequire;
var RBS = require('rbs').RBS;

doDepTest('require("abs")',["abs"])
doDepTest('require("abs");/*require("abs2")*/require(\'abs\')',["abs"])
doDepTest('require("abs2")/*require("abs2")*/;require(\'abs\')',["abs2","abs"])
function doDepTest(source,expect){
	var rbs2 = new RBS();
	rbs2.addBinaryBuilder(/.*/,function(resource,data){
		//console.log(testSource)
		return source;
	})
	setupJSRequire(rbs2,'/test/');
	var dest = rbs2.getContentAsBinary('/test/a__define__.js').toString('utf-8');
//	console.log(dest)
	var dest =dest.substring(dest.indexOf('['),dest.indexOf(']')+1);
	dest = eval(dest)
	dest = JSON.stringify(dest)
	console.log(dest)
	console.assert(JSON.stringify(expect) == dest,dest)
}
console.log('success')
try{
	new Function('require("")/*require("abs2")/*\'\"*/*/')
}catch(e){}