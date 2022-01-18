var fs = require('fs');
// var path = require('path');

require('./console-wrapper').setup();


var help = ['jsi start				',
		'	--start a test webserver',
		'jsi export xmldom				',
		'	--export xmldom for browser and every exports variable as globals',
		'jsi export -o tmp.js xmldom lite		',
		'	--export xmldom and lite to tmp.js; and every exports variable as globals',
		'jsi export -o tmp.js -ns util xmldom lite	',
		'	--export xmldom and lite to tmp.js, every exports valiable as util property'

		,'\n\n'
		,'jsi help <command>'
		,'	-- for more infomation'
		].join('\n');


exports.cmd = function(args){
	command.execute(args);
}
function Command(){
	this.commands = {};
	this.helps = {};
	this.alisa = {};
}


Command.prototype = {
	execute : function(args){
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
		var cmdName = args.shift();
		if(cmdName == 'help'){
			var isHelp = true;
			cmdName = args.shift();
		}
		var realName = cmdName;
		var cmd = this.commands[cmdName];
		if(!cmd){
			realName = this.alisa[cmdName];
			cmd = this.commands[this.alisa[cmdName]];
			
		}
		if(isHelp){
			var subHelp = this.helps[realName];
			if(subHelp){
				console.log(subHelp)
			}else{
				console.log(help)
			}
		}else if(cmd){
			cmd(opt,args);
		}else{
			console.log('command: %s not found,[%s] avaliable!',cmdName,Object.keys(this.commands));
		}
	},
	on:function(cmds,impl,help){
		this.commands[cmds[0]] = impl;
		this.helps[cmds[0]] = help;
		for(var i = cmds.length;--i;){
			this.alisa[cmds[i]] = cmds[0];
		}
	}
}

var command = new Command();
command.on(['start','s'],
	function(opt,args){
		var md =opt.module_dir || opt.md;
		require('../test').start({md:md});
	},
	'jsi start[s] -source <file1>,<file2>')
command.on(['format','f'],
	function(opt,args){require('./uglify-merge').transform(args,opt['o'],true)},
	"jsi format[f] <files>");
command.on(['compress','c'],
	function(opt,args){require('./uglify-merge').transform(args,opt['o'],false)},
	"jsi compress[c] <files>");
command.on(['export','browserify','e'],
	function(opt,args){
		if(args.length == 0){
			//console.log('modules args is required!!');
			//isHelp = true;
			var list = getModuleList('./')
			console.log(list);
			args = list;
			return;
			
		}
		/*
		 * <ul>
		 *   <li>1: 全依赖导出
		 *     <p>该文件在全局域运行，变量全部是全局变量。&#32; 依赖模块可用 require 函数获得,多个导出个体先到优先。</p></li>
		 *   <li>2: 单库导出
		 *     <p>不自动运行，需要require，内部用 require 函数获得外部模块, 该方式不自动合并外部模块的内容（只合并模块内文件）。</p></li>
		 *   <li>3  匿名导出
		 *     <p>当前文件在全局域执行，子模块作为匿名闭包载入，不能再用require(module)方式获得</p></li>
		 * </ul>
		 */

		var out = opt.o;
		var ns = opt.ns;
		var format = opt.f || opt.format//raw,format,compressed
		//console.dir(opt)
		require('../lib/exports').exportScript('./',args,onComplete,format);
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
				//console.log(content)
			}
		}
	},
	"jsi export[browserify] <modules>");
	
command.on(['analyse','a'],
		function(opt,args){require('./uglify-analyse').runAnalyse(args)},
		"jsi analyse[a] <path1> <path2>");

// command.on(['merge','m'],
// 		(opt,args)=>{
// 			var result = require('./uglify-merge').runMerge(args,opt);
// 			var out = opt.output|| opt.o;
// 			if(out){
// 				fs.writeFileSync(out,result)
// 				console.info('merge files to:',out);
// 			}else{
// 				console.log('// please use jsi -o <output file> m <source file1> <source file2>\n\n\n');
// 				console.log(result)
// 			}
// 		},
// 		"merge <path1>#exportValue1,exportValue2 <path2>#* ..");

// 	if(cmd == 'install'){
// 		if(args.length == 0){
// 			console.log('modules args is required!!');
// 			isHelp = true;
// 		}
// 		if(isHelp){
// 			console.log('jsi install xmldom				',
// 			'--install from node packages...')
// 			console.log('jsi install ./xmldom				',
// 			'--install from local file system');
// 			console.log('jsi install http://github.com/jindw/xmldom	',
// 			'--install from github')
// 			console.log();
// 		}else{
// 			require('../lib/install').install('./',args,function(args,info){
// 				//console.info(args,info)
// 			});
// 		}
// 	}else if(cmd == 'start'){
// 		require('../test').start();
// 	}else if(cmd=='init'){
// 		if(isHelp){
// 			console.log('jsi init					',
// 			'--init a jsi loading root  \'-f\' flag is override the exist files');
// 			console.log();
// 		}else{
// 			require('../lib/install').init('./',opt['f']);
// 		}
// 	}else if(cmd == 'uninstall'){
// 		if(args.length == 0){
// 			console.log('modules args is required!!');
// 			isHelp = true;
// 		}
// 		if(isHelp){
// 			console.log('jsi uninstall xmldom				',
// 			'--uninstall xmldom')
// 		}else{
// 			console.log('on coding...')
// 		}
// 	}else if(cmd == 'list'){
// 		if(isHelp){
// 			console.log('jsi list					',
// 			'--list all packages installed')
// 		}else{
// 			console.log('on coding...')
// 		}
// 	}else if(cmd == 'example'){
// 		if(isHelp){
// 			console.log('jsi example					',
// 			'--go to the scriptBase dir, and the command will deplay a example base on jquery and xmldom!')
// 		}else{
// 			example()
// 		}
// 	}else{
// 		if(!isHelp){
// 			console.log('command is not support!',cmd)
// 		}
// 		console.log('jsi <command> <option> <args>');
// 		console.log('	command: start install init export(browserify) uninstall list example');
// 		console.log('	options: -o,-f,-ns');
// 		console.log('	args: @see jsi help <command>')
// 	}
// }




function example(){			
	require('./file-util').writeFileMap("./scripts/jquery/",{
			"jquery.js":fs.readFileSync(require.resolve('../assets/resource/jquery_1_9_1.js')),
			"package.json":JSON.stringify({name:'jquery',main:"./jquery.js"})
		},function(errors){
			if(errors){
				console.error('save jquery lib error',errors)
			}else{
				console.info('jquery lib saved:./scripts/jquery/')
			}
			
			
			require('./file-util').writeFileMap("./scripts/test-tpl/",{
					"tpl.js":fs.readFileSync(require.resolve('../assets/test-tpl/tpl.js')),
					//"package.json":fs.readFileSync(require.resolve('../assets/test-tpl/package.json'))
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

