var rbs = require('rbs/test').rbs
var setupJSRequire = require('../lib/js-filter').setupJSRequire;
setupJSRequire(rbs,'/static/');

exports.rbs = rbs;
