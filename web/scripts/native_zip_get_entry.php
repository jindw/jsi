<?php
/**
 *  Load a zip file entry
 *  This function loads the files and dirs from a zip file from the harddrive.
 *
 *  @access                public
 *
 *  @param  string $file   The path to the zip file
 *  @return bool           Returns true if the file was loaded sucessfully
 *  @see http://svn.rubychan.de/coderay/trunk/test/scanners/php/test.in.php
 */
function zip_get_entry($file,$path){
    $zip = zip_open($file);
    if (!is_resource($zip)) {
    	 die(get_zip_error($zip));
    }
    while ($entry = zip_read($zip)) {
        if (zip_entry_name($entry) == $path && zip_entry_open($zip, $entry, "r")) {
            $result = zip_entry_read($entry, zip_entry_filesize($entry));
            zip_entry_close($entry);
            zip_close($zip);
            return $result;
        }else{
            zip_entry_close($entry);
        }
    }
    zip_close($zip);
}

function get_zip_error($errno) {
  // using constant name as a string to make this function PHP4 compatible
  $zipFileFunctionsErrors = array(
    'ZIPARCHIVE::ER_MULTIDISK' => 'Multi-disk zip archives not supported.',
    'ZIPARCHIVE::ER_RENAME' => 'Renaming temporary file failed.',
    'ZIPARCHIVE::ER_CLOSE' => 'Closing zip archive failed',
    'ZIPARCHIVE::ER_SEEK' => 'Seek error',
    'ZIPARCHIVE::ER_READ' => 'Read error',
    'ZIPARCHIVE::ER_WRITE' => 'Write error',
    'ZIPARCHIVE::ER_CRC' => 'CRC error',
    'ZIPARCHIVE::ER_ZIPCLOSED' => 'Containing zip archive was closed',
    'ZIPARCHIVE::ER_NOENT' => 'No such file.',
    'ZIPARCHIVE::ER_EXISTS' => 'File already exists',
    'ZIPARCHIVE::ER_OPEN' => 'Can\'t open file',
    'ZIPARCHIVE::ER_TMPOPEN' => 'Failure to create temporary file.',
    'ZIPARCHIVE::ER_ZLIB' => 'Zlib error',
    'ZIPARCHIVE::ER_MEMORY' => 'Memory allocation failure',
    'ZIPARCHIVE::ER_CHANGED' => 'Entry has been changed',
    'ZIPARCHIVE::ER_COMPNOTSUPP' => 'Compression method not supported.',
    'ZIPARCHIVE::ER_EOF' => 'Premature EOF',
    'ZIPARCHIVE::ER_INVAL' => 'Invalid argument',
    'ZIPARCHIVE::ER_NOZIP' => 'Not a zip archive',
    'ZIPARCHIVE::ER_INTERNAL' => 'Internal error',
    'ZIPARCHIVE::ER_INCONS' => 'Zip archive inconsistent',
    'ZIPARCHIVE::ER_REMOVE' => 'Can\'t remove file',
    'ZIPARCHIVE::ER_DELETED' => 'Entry has been deleted',
  );
  $errmsg = 'unknown';
  foreach ($zipFileFunctionsErrors as $constName => $errorMessage) {
    if (defined($constName) and constant($constName) === $errno) {
      return 'Zip File Function error: '.$errorMessage;
    }
  }
  return 'Zip File Function error: unknown';
}

?>