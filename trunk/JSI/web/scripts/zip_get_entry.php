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
function zip_get_entry($file,$path) {
	// Check whether the file exists
	if(!file_exists($file)){
		return null;//false;
	}
	// Load the files content
	$content = @file_get_contents($file);

	// Return false if the file cannot be opened
	if(!$content){
		return null;//false;
	}

	// Get the starting position of the end of central directory record
	$start = strpos($content, "\x50\x4b\x05\x06");

	// Error
	if($start === false){
		die('Could not find the end of central directory record');
	}
	// Get the ecdr
	$eof_cd = substr($content, $start+4, 18);

	// Unpack the ecdr infos
	$eof_cd = unpack('vdisc1/'.
                     'vdisc2/'.
                     'ventries1/'.
                     'ventries2/'.
                     'Vsize/'.
                     'Voffset/'.
                     'vcomment_lenght', $eof_cd);

	// Do not allow multi disc zips
	if($eof_cd['disc1'] != 0){
		die('multi disk stuff is not yet implemented :/');
	}

	// Save the interesting values
	$cd_entries = $eof_cd['entries1'];
	$cd_size    = $eof_cd['size'];
	$cd_offset  = $eof_cd['offset'];

	// Get the central directory record
	$cdr = substr($content, $cd_offset, $cd_size);

	// init the position and the list of the entries
	$pos     = 0;
	$cdrlen  = strlen($cdr);
	$entry = null;

	// Handle cdr
	
	while($pos < $cdrlen) {
		// Check header signature
		// Digital signature
		if(substr($cdr, $pos, 4) == "\x50\x4b\x05\x05") {
			// Get digital signature size
			$tmp_info = unpack('vsize', substr($cdr, $pos + 4, 2));
			// Read out the digital signature
			$digital_sig = substr($header, $pos + 6, $tmp_info['size']);
			break;
		}

		// Get file header
		$header = substr($cdr, $pos, 46);

		// Unpack the header information
		$header_info = @unpack('Vheader/'.
                             'vversion_made_by/'.
                             'vversion_needed/'.
                             'vgeneral_purpose/'.
                             'vcompression_method/'.
                             'vlast_mod_time/'.
                             'vlast_mod_date/'.
                             'Vcrc32/'.
                             'Vcompressed_size/'.
                             'Vuncompressed_size/'.
                             'vname_length/'.
                             'vextra_length/'.
                             'vcomment_length/'.
                             'vdisk_number/'.
                             'vinternal_attributes/'.
                             'Vexternal_attributes/'.
                             'Voffset',
		$header);

		// Valid header?
		if($header_info['header'] != 33639248){
			return null;//false;
		}

		// New position
		$pos += 46;

		// Read out the file name
		$header_info['name'] = substr($cdr, $pos, $header_info['name_length']);

		// New position
		$pos += $header_info['name_length'];

		// Read out the extra stuff
		$header_info['extra'] = substr($cdr, $pos, $header_info['extra_length']);

		// New position
		$pos += $header_info['extra_length'];

		// Read out the comment
		$header_info['comment'] = substr($cdr, $pos, $header_info['comment_length']);

		// New position
		$pos += $header_info['comment_length'];

		// Append this file/dir to the entry list
		if(!strcmp($header_info['name'], $path)){
			$entry = $header_info;
		}
	}

	// Check whether all entries where read sucessfully
//	if(count($entries) != $cd_entries){
//		return null;//false;
//	}
	// Handle files/dirs
	if($entry != null) {
		// Is a dir?
		if($entry['external_attributes'] & 16) {
			continue;
		}

		// Get local file header
		$header = substr($content, $entry['offset'], 30);

		// Unpack the header information
		$header_info = @unpack('Vheader/'.
                             'vversion_needed/'.
                             'vgeneral_purpose/'.
                             'vcompression_method/'.
                             'vlast_mod_time/'.
                             'vlast_mod_date/'.
                             'Vcrc32/'.
                             'Vcompressed_size/'.
                             'Vuncompressed_size/'.
                             'vname_length/'.
                             'vextra_length',
		$header);

		// Valid header?
		if($header_info['header'] != 67324752){
			return null;//false;
		}

		// Get content start position
		$start = $entry['offset'] + 30 + $header_info['name_length'] + $header_info['extra_length'];

		// Get the compressed data
		$data = substr($content, $start, $header_info['compressed_size']);

		
		// Detect compression type
		switch($header_info['compression_method']) {
			// No compression
			case 0:
				// Ne decompression needed
				$content = $data;
				break;

				// Gzip
			case 8:
				if(!function_exists('gzinflate')){
					return null;//false;
				}
				// Uncompress data
				$content = gzinflate($data);
				break;
				// Bzip2
			case 12:
				if(!function_exists('bzdecompress')){
					return null;//false;
				}
				// Decompress data
				$content = bzdecompress($data);
				break;
				// Compression not supported -> error
			default:
				return null;//false;
		}
		
		// Try to add file
		if($entry['name'] == $path){
			return $content;
		}
	}
	return null;//true;
}
//$result = zip_get_entry("D:/workspace/JSI2/build/dest/JSI.jar","boot.js");
?>