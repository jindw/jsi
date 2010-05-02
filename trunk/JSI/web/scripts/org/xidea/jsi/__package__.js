this.addScript("parse.js",["parse"]
                ,['org.xidea.jsidoc.util.loadText']);
this.addScript("optimize.js",["beforeAddScript",'beforeAddDependence']
                ,['org.xidea.jsidoc.util.loadText','org.xidea.jsidoc.util.findGlobals','parse']);