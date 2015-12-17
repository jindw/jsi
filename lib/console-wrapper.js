try{
	var colors = require('colors/safe');
	// set theme 
	colors.setTheme({
	  silly: 'rainbow',
	  input: 'grey',
	  verbose: 'cyan',
	  prompt: 'grey',
	  info: 'green',
	  data: 'grey',
	  help: 'cyan',
	  warn: 'yellow',
	  debug: 'blue',
	  error: 'red'
	});
	
	var prefixMap = {
		//'trace':              colors.white(colors.bgCyan('TRACE ')),
		//'debug':              colors.white(colors.bgCyan('DEBUG')),
		'info' :              colors.gray(colors.bgWhite('INFO ')),
		//'info' :              colors.white(colors.bgGreen('INFO ')),
		'warn' : colors.bold(colors.gray(colors.bgYellow('WARN '))),
		'error': colors.bold(colors.white(colors.bgRed(   'ERROR'))),
	}
	var backupMap = {
		//trace : console.trace||console.debug,
		//debug : console.debug||console.info,
		info : console.info,
		warn : console.warn,
		error : console.error
	}
		
}catch(e){
	console.log(e)
}

exports.setup = function(console_){
	console_ = console_||console;
	if(backupMap){
		for(var type in backupMap){
			(console_[type] = function(arg1){
				try{
				var type = arguments.callee.type;
				arg1 =  prefixMap[type] +' '+ arg1
				backupMap[type].apply(this,arguments);
				}catch(e){
					console.log(type,e)
				}
			}).type = type;
		}
	}
	//for(var type in backupMap){console_[type]('test '+type);}
}