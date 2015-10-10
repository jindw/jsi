var fs = require('fs');
var path = require('path');
exports.cmd= function cmd(args){
	var exec = args.shift();
	var script = args.shift();
	var cmd = args.shift();
	if(cmd == 'help'){
		var isHelp = true;
		cmd = args.shift();
	}
	var i = args.length;
	var opt = {};
	while(i--){
		var a = args[i];
		if(/^\-/.test(a)){
			opt[a] =  args[Math.min(i+1,args.length-1)];
			args.splice(i,2);
		}
	}
	//console.log(args,opt)
	if(cmd == 'install'){
		if(args.length == 0){
			console.log('modules args is required!!');
			isHelp = true;
		}
		if(isHelp){
			console.log('jsi install xmldom				',
			'--install from node packages...')
			console.log('jsi install ./xmldom				',
			'--install from local file system');
			console.log('jsi install http://github.com/jindw/xmldom	',
			'--install from github')
			console.log();
		}else{
			require('../lib/install').install('./',args,function(args,info){
				//console.info(args,info)
			});
		}
	}else if(cmd == 'start'){
		require('../test').start();
	}else if(cmd=='init'){
		if(isHelp){
			console.log('jsi init					',
			'--init a jsi loading root  \'-f\' flag is override the exist files');
			console.log();
		}else{
			require('../lib/install').init('./',opt['-f']);
		}
	}else if(cmd=='browserify' || cmd=='export'){
		if(args.length == 0){
			//console.log('modules args is required!!');
			//isHelp = true;
			var list = getModuleList('./')
			console.log(list);
			args = list;
			
		}
		if(args.length == 1){
			if(/\.js$/.test(args[0])){
				args[0] = args[0].replace(/\.js$/,'')
				var isSingleFile = true;
			}
		}
		if(isHelp){
			console.log('jsi start				',
			'--start a test webserver');
			console.log('jsi export xmldom				',
			'--export xmldom for browser and every exports variable as globals');
			console.log('jsi export -o tmp.js xmldom lite		',
			'--export xmldom and lite to tmp.js; and every exports variable as globals');
			console.log('jsi export -o tmp.js -ns util xmldom lite	',
			'--export xmldom and lite to tmp.js, every exports valiable as util property');
			console.log();
			
		}else{
			var exports =  require('../lib/exports');
			var out = opt['-o'];
			var ns = opt['-ns'];
			console.log('export as simple browser runable javascript :'+args+"->"+(out||'.stdout'));
			exports.export4web('./',args,function(impls,idIndex,internalDeps,externalDeps){
				var compressJS= require('../lib/js-token').compressJS;
				if(isSingleFile){
					var mainSource = impls[0].replace(/^.*?\{([\s\S]*)\}\s*$/,'$1')
					exports.export4web('./',idIndex.slice(1).concat(externalDeps),function(impls,idIndex,internalDeps,externalDeps){
						if(idIndex.indexOf(args[0])>=0){
							console.log('single export file can not be required!\n\t export failed!!\n\n');
							return;
						}else if(externalDeps.length >0){
							console.warn('has externalDeps not loaded!!, you must add all of the dependence modules in the exports list:',externalDeps)
							console.info('export failed!\n		try the commond: jsi ',process.argv.slice(2).concat(externalDeps).join(' '))
							return;
						}
						impls = impls.map(compressJS)
						var source = exports.buildExportSource("'./'",impls,idIndex,'',args.length);
						var result = source + '\n'+compressJS(mainSource);
						
						//console.log(mainSource)
						result = (result);
						//console.log("####"+result.length)
						fs.writeFile(out,result,function(err){
							console.log('package exported:',idIndex,internalDeps,externalDeps)
							if(err){
								console.log('export failed! write file error',err)
							}else{
								console.log('write complete:',out);
							}
						})
					})
				}else if(externalDeps.length >0){
					console.warn('has externalDeps not loaded!!, you must add all of the dependence modules in the exports list:',externalDeps)
					console.info('export failed!\n		try the commond: jsi ',process.argv.slice(2).concat(externalDeps).join(' '))
				}else {
					var source = exports.buildExportSource("'./'",impls,idIndex,ns,args.length);
					var result = source;
					result = require('../lib/js-token').compressJS(result);
					fs.writeFile(out,result,function(err){
						console.log('package exported:',idIndex,internalDeps,externalDeps)
						if(err){
							console.log('export failed! write file error',err)
						}else{
							console.log('write complete:',out);
						}
					})
				}
			});
		}
	}else if(cmd == 'uninstall'){
		if(args.length == 0){
			console.log('modules args is required!!');
			isHelp = true;
		}
		if(isHelp){
			console.log('jsi uninstall xmldom				',
			'--uninstall xmldom')
		}else{
			console.log('on coding...')
		}
	}else if(cmd == 'list'){
		if(isHelp){
			console.log('jsi list					',
			'--list all packages installed')
		}else{
			console.log('on coding...')
		}
	}else if(cmd == 'example'){
		if(isHelp){
			console.log('jsi example					',
			'--go to the scriptBase dir, and the command will deplay a example base on jquery and xmldom!')
		}else{
/*			require('../lib/install').install('./scripts/',['jquery'],function(args,info){
				console.info("jquery installed:");
				var source = fs.readFileSync(require.resolve('../example/jquery.html')).toString()
					.replace(/(<script\s+src=").*?(?=")/,"$1./scripts/boot.js");
				var dest = path.resolve('./jquery-hello-world.html');
				fs.writeFile(dest,source,function(){
					console.log('jquery test file saved:',dest);
				})
			});
*/
			
			
			require('./file-util').writeFileMap("./scripts/jquery/",{
					"jquery.js":fs.readFileSync(require.resolve('../assets/test/jquery_1_9_1.js')),
					"package.json":JSON.stringify({name:'jquery',main:"./jquery.js"})
				},function(errors){
					if(errors){
						console.error('save jquery lib error',errors)
					}else{
						console.info('jquery lib saved:./scripts/jquery/')
					}
					
					
					require('./file-util').writeFileMap("./scripts/test-tpl/",{
							"tpl.js":fs.readFileSync(require.resolve('../assets/test-tpl/tpl.js')),
							"package.json":fs.readFileSync(require.resolve('../assets/test-tpl/package.json'))
						},function(errors){
							if(errors){
								console.error('save tpl lib error',errors)
							}else{
								console.info('template lib saved:./scripts/test-tpl/')
							}
						})
					fs.writeFile('./example-template.html',
						fs.readFileSync(require.resolve('../example/example-template.html')).toString()
							.replace(/(<script\s+src=").*?(?=")/,"$1./scripts/boot.js"),
						function(){
							console.info('template test file saved:./example-template.html');
						})
				})
			fs.writeFile('./example-jquery.html',
				fs.readFileSync(require.resolve('../example/example-jquery.html')).toString()
					.replace(/(<script\s+src=").*?(?=")/,"$1./scripts/boot.js"),
				function(){
					console.info('jquery test file saved:./example-jquery.html');
				})
				
			require('../test').start();
		}
	}else{
		if(!isHelp){
			console.log('command is not support!',cmd)
		}
		console.log('jsi <command> <option> <args>');
		console.log('	command: start install init export(browserify) uninstall list example');
		console.log('	options: -o,-f,-ns');
		console.log('	args: @see jsi help <command>')
	}
}

function getModuleList(root){
	root = root.replace(/[\\\/]?$/,'/');
	var files = fs.readdirSync('./');
	var modules = [];
	for(var i=0;i<files.length;i++){
		var f = files[i];
		if(f == 'o'){continue;}
		var s = fs.statSync(root+f)
		if(s.isFile() && /\.js$/.test(f)){
			modules.push(f.replace(/\.js$/,''));
		}else if(s.isDirectory()){
			if(fs.existsSync(root+f+'/package.json')){
				modules.push(f+'/')
			}else{
				_addSubList(modules,root,f);
			}
		}
	}
}


function _addSubList(modules,root,path){
	var files = fs.readdirSync(root+path);
	for(var i=0;i<files.length;i++){
		var f = files[i];
		var s = fs.statSync(root+f)
		if(s.isFile() && /\.js$/.test(f)){
			modules.push(path+'/'+f.replace(/\.js$/,''));
		}else if(s.isDirectory()){
			_addSubList(modules,root,path+'/'+f)
		}
	}
}














