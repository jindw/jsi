function preparePackage(name){
	var pkg = $import(name+':');
//	$JSI.preload(name+'.test','',function(){
//		for(var file in pkg.scriptObjectMap){
//			this.addScript(file,"*",
//			    ["org/xidea/test/util.js",
//			    "org/xidea/test/jsmock.js",
//			    "org.xidea.test:TestSuite",
//			    "org.xidea.test:assertEquals",
//			    name.replace(/\.|$/g,'/')+file]);
//		}
//	});
	$import("org.xidea.test:assertEquals");
	$import("org.xidea.test:JSMock");
	return $import(name+'.test:')
}

function assertEquals(expectedValue,actualValue,message){
	if(expectedValue != actualValue){
		//TODO object equals
		throw new Error(expectedValue+"=>"+actualValue+":"+message);
	}
}

var running = false;
var consoleId ;
var packageIndex = 0;
var testPackages;
var testMap;
var testName;
var testNames
var TestSuite = {
	initialize:function(packages,consoleIdParam){
		consoleId = consoleIdParam;
		testPackages = [];
		testNames=null;
		for(var i =0;i<packages.length;i++){
			testPackages.push(preparePackage(packages[i]));
		}
	},
	log : function(className,content){
		var div = document.createElement("div");
        var msg = [];
        div.className=className;
        msg.push("<ul>");
        msg.push(encodeHTML(content));
        if(content instanceof Object){
	        for(var n in content){
	            msg.push("  ",n,"=",encodeHTML(content[n]));
	            msg.push("\n");
	        }
        }
        msg.push("</ul>");
        div.innerHTML = msg.join('');
        document.getElementById(consoleId).appendChild(div);
    },
	test:function(){
		if(running){
			return;
		}
		if(testNames && testNames.length >0){
			testName = testNames.pop();
			var beginDiv = document.createElement("div");
			beginDiv.innerHTML = "<b>"+testName+"</b>:";
		    document.getElementById(consoleId).appendChild(beginDiv);
			try{
				var objectName = testName.replace(/\..*/,'');
				var thisObject = objectName==testName?this:testMap[objectName];
			    var waitTime = testMap[testName].call(thisObject);
			    if(waitTime){
			    	wait(waitTime);
			    }else{
			    	TestSuite.resume();
			    }
			}catch(e){
				this.fail(e)
			    TestSuite.resume();
			}
	        running = false;
		}else{
			var pkg = testPackages[packageIndex++];
			if(pkg!=null){
				var objects = [];
				var beginDiv = document.createElement("div");
				beginDiv.innerHTML = "<b class='package'>"+pkg.name+"</b>:";
			    document.getElementById(consoleId).appendChild(beginDiv);
				testMap = {};
				var objectMap = {};
				$import(pkg.name+".*",objectMap)
				for(var n in objectMap){
					var item = objectMap[n];
					testMap[n] = item;
					if(item instanceof Function){
						if(item.length == 0){
						    objects.push(n);
						}
					}else{
						for(var n2 in item){
							var item2 = item[n2]
							if(item2 instanceof Function){
								if(item2.length == 0){
									n2 = n+"."+n2;
									testMap[n2] = item2;
									objects.push(n2);
								}
							}
						}
					}
				}
				testNames = objects;
		        running = false;
		        this.test();
			}else{
				alert(["測試完成：",
				"失敗:"+failed,
				"警告:"+warned,
				"通過:"+passed
				].join("\n"))
			}
		}
	},
	resume:function (){
		document.getElementById(consoleId).appendChild(document.createElement("hr"));
		if(waitTimeout){
			clearTimeout(waitTimeout);
			waitTimeout == null;
		}
		setTimeout(function(){
			TestSuite.test()
		},1);
	},
	pass:function(message){
		this.log("info",message);
		passed ++;
	},
	warn:function(message){
		this.log("warn",message);
		warned ++;
	},
	fail:function(message){
		this.log("error",message);
		failed ++;
	}
}
var passed = 0;
var failed = 0;
var warned = 0;
var waitTimeout;


function wait(waitTime){
	waitTimeout = setTimeout(function(){
		TestSuite.fail("当前测试超时");
		TestSuite.resume();
	},waitTime)
}