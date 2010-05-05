//不能有依赖，否则boot文件太大
this.addScript("optimize.js",["optimizePackage","trimPath"]);
this.addScript("parse.js",["parse"]
				,"trimPath");
this.addScript("require.js",["buildRequire"]);
