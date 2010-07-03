//不能有依赖，否则boot文件太大
this.addScript("optimize.js",["optimizePackage","trimPath"]
			,'isBrowser');
this.addScript("parse.js",["parse"]
				,"trimPath");
this.addScript("require.js",["buildRequire"]);
this.addScript('browser-info.js',"isBrowser")
this.addScript('log.js',['$log'])
