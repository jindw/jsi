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
function printEntry($path){
    if(file_exists(realpath("./$path"))){
        header("Content-Type:".findMimiType($path).";charset=UTF-8");
        readfile(realpath("./$path"));
    }else 
    if(function_exists("zip_open")){
        findFromLib(".",$path)||findFromLib("../WEB-INF/lib/",$path);
    }else{
        echo "//您的php没有安装zip扩展";
    }
}
function findFromLib($base,$path){
    $base = realpath($base);
    if($base){
        $dir = dir($base); 
        while (false !== ($file = $dir->read())) {
            if(strtolower(preg_replace('/.*\./',".",$file)) == ".jar"){
                $zip = zip_open("$base\\$file");
                while ($entry = zip_read($zip)) {
                    if (zip_entry_name($entry) == $path && zip_entry_open($zip, $entry, "r")) {
                        //$contentType = mime_content_type($path);
                        header("Content-Type:".findMimiType($path).";charset=UTF-8");
                        echo zip_entry_read($entry, zip_entry_filesize($entry));
                        zip_entry_close($entry);
                        zip_close($zip);
                        $dir->close();
                        return true;
                    }
                }
                zip_close($zip);
            }
        }
        $dir->close();
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
    return $result;

}

function walkPackageTree($dir, $prefix,
        $result) {
    if ($prefix == null) {
        $subPrefix = "";
    } else if ($prefix.length() == 0) {
        $subPrefix = dir.getName();
    } else {
        $subPrefix = $prefix + '.' + dir.getName();
    }
    if (file_exists("__package__.js")){
        result.add($subPrefix);
    }
    while (false !== ($file = $dir->read())) {
        if (is_dir($file)) {
            $name = file.getName();
            if (!$name.startsWith(".")) {
                walkPackageTree($file, subPrefix, result);
            }
        }
    }
    $dir->close();

}



if(array_key_exists('path',$_GET)){
   $path = $_GET['path'];
}else if(array_key_exists('PATH_INFO',$_SERVER)){
   $path = $_SERVER['PATH_INFO'] ;
   $path = substr($path, 1);
}else{
   $path = null;
}

if($path != null){
    $pos = strrpos($path, '/');
    $fileName = substr($path, $pos + 1);
    $packageName = preg_replace("/\//", "." ,substr($path, 0, $pos));
    if(array_key_exists('preload',$_GET)){
        echo("\$JSI.preload('$packageName','$fileName',function(){eval(this.varText);");
        printEntry($path);
        //require($path)
        echo("\n})");
    }else{
        printEntry($path);
    }
}else{
    //TODO:require
    if(array_key_exists('externalScript',$_GET)){
        $externalScript = $_GET['externalScript'];
    }else{
        $externalScript = "";
    }
    header("Content-Type:text/html;");
    echo("<html><frameset rows='100%'><frame src='index.php/org/xidea/jsidoc/index.html?externalScript=$externalScript'></frame></html>");
}
return;
?>/**/