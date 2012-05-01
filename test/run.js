var rbs = require('rbs/test/run').rbs
var setupJSRequire = require('../lib/js-filter').setupJSRequire;
setupJSRequire(rbs,'/static/');


//var setupCSS = require('../lib/css-filter').setupCSS;
//setupCSS(env,'/static/');

exports.rbs = rbs;