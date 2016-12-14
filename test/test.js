//#include 'ug-test.js'


var path = require('path')
var exportScript = require('jsi/lib/exports').exportScript;

var rtv = exportScript(path.join(__dirname,'../'),['./test/test-source.js']);
//console.log(rtv)