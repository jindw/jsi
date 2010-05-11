/**
 */
var base64 = 'base64.js.js';
if(this.isBrowser('ie',8) || this.isBrowser('Gecko') || this.isBrowser('WebKit')){
	base64 = 'base64.atob.js';
}

this.addScript(base64,'byteArrayToBase64');
this.addScript('zip.js','Zip'
				,'byteArrayToBase64');

if(this.isBrowser('ie') && !this.isBrowser('ie7')){
	this.addDependence('zip.js','org.xidea.jsidoc.util:XMLHttpRequest');
}
