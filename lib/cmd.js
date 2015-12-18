var fs = require('fs');
var path = require('path');
require('./console-wrapper').setup();
exports.cmd= function cmd(args){
	var exec = args.shift();
	var script = args.shift();
	
	var i = args.length;
	var opt = {};
	while(i--){
		var a = args[i];
		if(/^\-/.test(a)){
			opt[a.substr(1)] =  args[Math.min(i+1,args.length-1)];
			args.splice(i,2);
		}
	}
	var cmd = args.shift();
	if(cmd == 'help'){
		var isHelp = true;
		cmd = args.shift();
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
			require('../lib/install').init('./',opt['f']);
		}
	}else if(cmd=='browserify' || cmd=='export'){
		if(args.length == 0){
			//console.log('modules args is required!!');
			//isHelp = true;
			var list = getModuleList('./')
			console.log(list);
			args = list;
			
		}
		var isSingleJSFile = args.length == 1 && /\.js$/.test(args[0]);
		var isSingleHTMLFile = args.length == 1 && /\.html$/.test(args[0]);
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
			var out = opt['o'];
			var ns = opt['ns'];
			var mode = opt['mode'];
			var needRequire = mode != 3;
			console.log('export as simple browser runable javascript :'+args+"->"+(out||'.stdout'));
			function onComplete(content,externalDeps){
				
				if(externalDeps instanceof Array && externalDeps.length >0){
					var error = 'has externalDeps not loaded!!, you must add all of the dependence modules in the exports list:'+externalDeps
					console.warn(error)
				}
				if(out){
					fs.writeFile(out,content,function(err){
						if(err){
							console.log('export failed! write file error',err)
						}else{
							console.log('write complete:',out);
						}
					})
				}else{
					console.log('result output: \n-----\n\n')
					console.log(content)
				}
			}
			
			//exportHTML(root,path,callback)
			if(isSingleHTMLFile){
				if(mode && mode!=1){
					console.warn('html export support mode 1 only!!')
				}
				require('../lib/export-html').exportHTML('./',args[0], onComplete)
			}else if(isSingleJSFile){
				exports.exportSingleFile('./',args[0],mode || 1, onComplete)
			}else{
				exports.exportModules('./',args,mode,onComplete)
			}
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
	}else if(cmd == 'analyse' || cmd == 'a'){
		if(isHelp){
			console.log('jsi a <path1> <path2> ')
			console.log('jsi analyse <path1>')
			return;
		}
		require('./uglify-merge').runAnalyse(args);
	}else if(cmd == 'merge' || cmd == 'm'){
		if(isHelp){
			console.log('jsi merge <path1>#exportValue1,exportValue2 <path2> ..')
			console.log('jsi merge <path1>#* <path2> ..')
			return;
		}
		var result = require('./uglify-merge').runMerge(args,opt);
		var out = opt.output|| opt.o;
		if(out){
			fs.writeFile(out,
				result,
				function(){
					console.info('merge files to:',out);
				})
		}else{
			console.log('// please use jsi -o <output file> m <source file1> <source file2>\n\n\n');
			console.log(result)
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















