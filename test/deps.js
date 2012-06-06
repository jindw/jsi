var setupJSRequire = require('../lib/js-filter').setupJSRequire;
var RBS = require('rbs').RBS;

doDepTest('require("abs")',["abs"])
doDepTest('require("abs");/*require("abs2")*/require(\'abs\')',["abs"])
doDepTest('require("abs2")/*require("abs2")*/;require(\'abs\')',["abs2","abs"])
doDepTest('require("abs3")//require("abs2")*/;require(\'abs\')',["abs3"])
doDepTest("//var CS = require('cs').CS;CS.config();/*\n\ntest()\n//*/",[])
doDepTest('/*var CS = require("cs")\n//*/\n;/*\n\ntest()\n//*/',[])
function doDepTest(source,expect){
	var rbs2 = new RBS();
	rbs2.addBinaryBuilder(/.*/,function(resource,data){
		//console.log(testSource)
		return source;
	})
	new Function(source)
	setupJSRequire(rbs2,'/test/');
	var result = rbs2.getContentAsBinary('/test/a__define__.js').toString('utf-8');
//	console.log(dest)
	var deps =result.substring(result.indexOf('['),result.indexOf(']')+1);
	deps = eval(deps)
	deps = JSON.stringify(deps.sort())
	var expect = JSON.stringify(expect.sort()) ;
	var syntaxTestPostfix = '\n/*/\'\'""*/\n';
	console.assert(expect== deps,'\nexpect:'+expect,'\nresult:'+deps,'\n'+source,'\n'+source.replace(/require\(.*?\)/g,'$&'+syntaxTestPostfix))
}
console.log('success')
try{
	new Function('require("")/*require("abs2")/*\'\"*/*/')
}catch(e){}