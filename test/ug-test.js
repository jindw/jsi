var fs = require('fs')
var exportRequireExample = fs.readFileSync(require.resolve('./ug-example.js')).toString();
var exportRequireExample = (function updateColorTab(updateCurrentTheme){
	var sampleBottom = height/2.5;
	
	
	var cell = tabFolderHeight*0.618;
	var themeRank = [sampleBottom,sampleBottom+cell];
	var colorRank = [sampleBottom + cell*1.618, height-cell*2.618]
	var childRank = [ height-cell*.618, height-cell*1.618]
	
	colorEventMap.length = 0;
	//drawNode
	drawNode(tabFolderHeight,sampleBottom);
	//drawTheme
	themeRank.push(drawTheme.apply(this,themeRank))
	colorEventMap.push(themeRank);
	//drawColors
	
	colorRank.push(drawColors.apply(this,colorRank))
	colorEventMap.push(colorRank);
	//colorEventMap
	
	
	childRank.push(drawChildrenColors.apply(this,childRank))
	colorEventMap.push(childRank);
	alert('🙊')
	
}).toString();


try{
	var UglifyJS = require("uglify-js");
}catch(e){
	try{
		var UglifyJS = require("uglifyjs");
	}catch(e){
	}
}

if(UglifyJS){
	//var compressJS0 = compressJS;
	compressJS = function(source){
		var ast = UglifyJS.parse(source);
		ast.figure_out_scope();
		compressor = UglifyJS.Compressor({});
		//ast.mangle_names();
		ast = ast.transform(compressor);
		return ast.print_to_string({beautify:true},true); 
	}
}
console.log(compressJS(exportRequireExample))