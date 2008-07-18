/**<?php echo("/");
ob_clean();
function printEntry($path){
    if(file_exists(realpath("./$path"))){
        readfile(realpath("./$path"));
    }else{
        $base = realpath("../WEB-INF/lib/");
        $dir = dir($base); 
        while (false !== ($file = $dir->read())) {
            if(strtolower(preg_replace('/.*\./',".",$file)) == ".jar"){
                $zip = zip_open("$base\\$file");
                while ($entry = zip_read($zip)) {
                    if (zip_entry_name($entry) == $path && zip_entry_open($zip, $entry, "r")) {
                        $ext = strtolower(preg_replace('/.*\./',".",$path));
                        $contentType = "text/html";
                        if($ext == '.css'){
                            $contentType = "text/css";
                        }
                        header("Content-Type:$contentType;charset=UTF-8");
                        echo zip_entry_read($entry, zip_entry_filesize($entry));
                        zip_entry_close($entry);
                        zip_close($zip);
                        $dir->close();
                        return ;
                    }
                }
                zip_close($zip);
            }
        }
        $dir->close();
    }
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
?>
<?php
//
// PHP 版本的JSI 代理程序
//
?>
/**/

