$import("org.xidea.jsidoc.export:Exporter");
var exporter = new Exporter();
exporter.addFeatrue("mixTemplate");
exporter.addImport("org.xidea.jsidoc:JSIDoc");
alert (exporter.getXMLContent())

