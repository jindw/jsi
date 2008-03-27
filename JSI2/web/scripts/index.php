/**<?php echo("/");
    $path = $_GET['path'];
    $pos = strrpos($path, '/');
    $fileName = substr($path, $pos + 1);
    $packageName = preg_replace("/\//", "." ,substr($path, 0, $pos));
    
    echo("\$JSI.preload('$packageName','$fileName',function(){eval(this.varText);");
    readfile($path)
    //require($path)
    echo("\n})");
?>
<?php
//
// PHP 版本的JSI 代理程序
//
?>
/**/

