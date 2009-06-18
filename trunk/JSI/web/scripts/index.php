/**<?php
//
// PHP 版本的JSI 代理程序（开发期间使用）
// 功能：
// 1.即时编译
// 2.类库支持
// 3.JSIDoc系列工具支持
//
$encoding = "UTF-8";
$export_service = "http://litecompiler.appspot.com/scripts/export.action";
?>
<?php
echo("/");
ob_clean();
if(array_key_exists('path',$_GET)){
   $path = $_GET['path'];
}else if(array_key_exists('PATH_INFO',$_SERVER)){
   $path = $_SERVER['PATH_INFO'] ;
   $path = substr($path, 1);
}else{
   $path = null;
}
if($path == 'export.action'){
    //转发到指定jsa服务器
    if($export_service){
		$postdata = http_build_query(
		    $_POST
		);
		$opts = array('http' =>
		    array(
		        'method'  => 'POST',
		        'header'  => 'Content-type: application/x-www-form-urlencoded',
		        'content' => $postdata
		    )
		);
		$context  = stream_context_create($opts);
		header('Content-type: text/plain');
		echo file_get_contents($export_service, false, $context);
    }else{
        header("HTTP/1.0 404 Not Found");
    }
    return;
}
function print_entry($path){
    if(print_from_dir('.',$path)){
        return;
    }
    if(print_from_dir('../WEB-INF/classes',$path)){
        return;
    }
    if(print_from_zip("../WEB-INF/lib/",$path)){
        return;
    }
    if(print_from_zip(".",$path)){
        return;
    }
    header("HTTP/1.0 404 Not Found");
}
function print_from_dir($dir,$path){
    if(file_exists(realpath("$dir/$path")) && is_valid_file($dir,$path)){
        print_content_type($path);
        readfile(realpath("$dir/$path"));
        return true;
    }
}

function print_from_zip($base,$path){
    $base = realpath($base);
    if($base){
        $dir = dir($base); 
        while (false !== ($file = $dir->read())) {
		    if(preg_match('/.*\.(?:jar|zip)$/i',$file)){
		    	$result = zip_get_entry("$base/$file",$path);
			    if($result != null){
	                $dir->close();
	                print_content_type($path);
			        echo $result;
	                return true;
	            }
	        }
        }
        $dir->close();
    }
}
//zip 函数有漏洞
//if(function_exists("zip_open"))require_once("native_zip_get_entry.php");else
	require_once("zip_get_entry.php");

//function zip_get_entry(){return null;}
//我自己也忘了为啥？
function is_valid_file($dir,$path){
    if(preg_match('/\\\\|\\//',$path) || $path == "lazy-trigger.js"
         || filesize(realpath("$dir/$path"))>200){
        return true;///[\/\\]/
    }
    return false;
}
function print_content_type($path){
    global $encoding;
    $ext = strtolower(preg_replace('/.*\./',"",$path));
    switch($ext){
    case 'png':
    case 'gif':
    case 'jpeg':
    case 'jpg':
        header("Content-Type:image/$ext");
        return;
    case 'css':
        $contentType = "text/css";
        break;
    default:
        $contentType = "text/html";
    }
    header("Content-Type:$contentType;charset=$encoding");

			        
}
function find_package_list($root) {
    $result = array();
    walk_package_tree($root, null, $result);
    $count = count($result);
    $buf= '';
    for($i=0;$i<$count;$i++){
        $buf=$buf.",".$result[$i];
    }
    return substr($buf,1);

}

function walk_package_tree($base, $prefix, &$result) {
    if ($prefix) {
        $subPrefix = $prefix .'.' . basename($base);
    } else {
	    if ($prefix === null) {
	        $subPrefix = "";
	    } else {
	        $subPrefix = basename($base);
        }
    }
    if ($subPrefix && file_exists($base."/__package__.js")){
        array_push($result,$subPrefix);
    }
    $dir = dir($base);
    while (false !== ($file = $dir->read())) {
        if (is_dir("$base/$file")) {
            if (substr($file,0,1) != ".") {
                walk_package_tree("$base/$file", $subPrefix, $result);
            }
        }
    }
    $dir->close();

}


if($path != null){
    $filePath = preg_replace("/__preload__\.js$/",".js",$path);
    $pos = strrpos($path, '/');
    $fileName = substr($filePath, $pos + 1);
    $packageName = preg_replace("/\//", "." ,substr($path, 0, $pos));
    if($filePath!=$path){
        echo("\$JSI.preload('$packageName','$fileName',function(){eval(this.varText);");
        print_entry($filePath);
        echo("\n})");
    }else{
        print_entry($path);
    }
}else{
    //TODO:require
    if(array_key_exists('externalScript',$_GET)){
        $externalScript = $_GET['externalScript'];
    }else{
        $externalScript = find_package_list(realpath("."));
    }
    header("Content-Type:text/html;charset=$encoding");
    echo("<html><frameset rows='100%'><frame src='index.php/org/xidea/jsidoc/index.html?group.All%20Scripts=$externalScript'></frame></frameset></html>");
}
return;
?>/**/