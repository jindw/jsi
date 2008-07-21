/**<?php
//
// PHP 版本的JSI 代理程序（开发期间使用）
// 功能：
// 1.即时编译
// 2.类库支持
// 3.JSIDoc系列工具支持
//
?>
<?php echo("/");
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
   header("HTTP/1.0 404 Not Found");
   return;
}

function printEntry($path){
    $classpath = array(
        "../WEB-INF/classes"
        //,"../../../JSI2/web/scripts/"
    );
    foreach ($classpath as $dir){
        if(file_exists(realpath("$dir$path"))){
	        header("Content-Type:".findMimiType($path).";charset=UTF-8");
	        readfile(realpath("$dir$path"));
	        return;
	    }
    }
    if(file_exists(realpath("./$path"))){
        header("Content-Type:".findMimiType($path).";charset=UTF-8");
        readfile(realpath("./$path"));
    }else{
        findFromLib(".",$path)||findFromLib("../WEB-INF/lib/",$path);
    }
}
function findFromLib($base,$path){
    $base = realpath($base);
    if($base){
        $miss_zip = false;
        $dir = dir($base); 
        while (false !== ($file = $dir->read())) {
            if(preg_match('/.*\.(?:jar|zip)$/i',$file)){
                if(function_exists("zip_open")){
	                if(findFromZip("$base\\$file",$path)){
	                    $dir->close();
	                    return true;
	                }
	            }else{
	                $miss_zip = true;
			    }
            }else if(preg_match('/.*\.xml$/i',$file)){
                //读取XML格式的类库
				if(findFromXML($file,$path)){
				    return true;
				}
            }
        }
        if($miss_zip){
            echo "//您的php没有安装zip扩展,无法遍历zip格式类库";
        }
        $dir->close();
    }
}
function findFromZip($file,$path){
    $zip = zip_open("$file");
    while ($entry = zip_read($zip)) {
        if (zip_entry_name($entry) == $path && zip_entry_open($zip, $entry, "r")) {
            //$contentType = mime_content_type($path);
            header("Content-Type:".findMimiType($path).";charset=UTF-8");
            echo zip_entry_read($entry, zip_entry_filesize($entry));
            zip_entry_close($entry);
            zip_close($zip);
            return true;
        }
    }
    zip_close($zip);
}
function findFromXML($file,$path){
    $xml = simplexml_load_file($file);
    $result = $xml->xpath("//script[@path='$path']");
    if($result){
        header("Content-Type:".findMimiType($path).";charset=UTF-8");
		while(list( $key, $node) = each($result)) {
		    if($node->attributes()->encoding=="base64"){
		        echo base64_decode($node);
		    }else{
		        echo $node;
		    }
		    
	    }
	    return true;
	}
}
function findMimiType($path){
    switch(strtolower(preg_replace('/.*\./',".",$path))){
    case '.css':
        return "text/css";
    case '.png':
        return "image/png";
    case '.gif':
        return "image/gif";
    case '.jpeg':
    case '.jpg':
        return "image/jpeg";
    default:
        return "text/html";
    }
}
function findPackageList($root) {
    $result = array();
    walkPackageTree($root, null, $result);
    $count = count($result);
    $buf= '';
    for($i=0;$i<$count;$i++){
        $buf=$buf.",".$result[$i];
    }
    return substr($buf,1);

}

function walkPackageTree($base, $prefix, &$result) {
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
                walkPackageTree("$base/$file", $subPrefix, $result);
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
        printEntry($filePath);
        echo("\n})");
    }else{
        printEntry($path);
    }
}else{
    //TODO:require
    if(array_key_exists('externalScript',$_GET)){
        $externalScript = $_GET['externalScript'];
    }else{
        $externalScript = findPackageList(realpath("."));
    }
    header("Content-Type:text/html;");
    echo("<html><frameset rows='100%'><frame src='index.php/org/xidea/jsidoc/index.html?group.All%20Scripts=$externalScript'></frame></html>");
}
return;
?>/**/