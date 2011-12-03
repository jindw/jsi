function byteArrayToBase64(byteArray){
	return btoa(String.fromCharCode.apply(null,byteArray))
}