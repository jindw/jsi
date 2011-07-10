package org.jside.jsi.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.el.impl.CommandParser;
import org.xidea.el.json.JSONEncoder;

public class CompressAction {
	private static final Log log = LogFactory.getLog(CompressAction.class);
	private static final Pattern PRE_WHITESPACE = Pattern
			.compile("^(?://.*|/\\*[\\s\\S]+?\\*/|\\s+)+");
	private static final Pattern POST_WHITESPACE = Pattern.compile("\\s+$");
	private JSAToolkit toolkit = JSAToolkit.getInstance();
	private String output;
	private String encoding = "utf-8";
	private boolean format;
	private List<String> source;
	private JavaScriptCompressorConfig config = toolkit
			.createJavaScriptCompressorConfig();

	public JavaScriptCompressorConfig getConfig() {
		return config;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setSource(List<String> source) {
		this.source = source;
	}

	public void setFormat(boolean format) {
		this.format = format;
	}

	public CompressAction(String[] args) {
		CommandParser.setup(this, args);
		log.info("config:"+JSONEncoder.encode(config));
	}

	public static void main(String[] args) throws Exception {
		if(args == null || args.length==0){
			args = new String[]{"-output","C:\\Users\\jindw\\workspace\\JSI2/build/dest/jsidoc/boot.js","-source","C:\\Users\\jindw\\workspace\\JSI2/web/scripts/boot.js","-config.features",":Debug","org.xidea.jsi:Block","org.xidea.jsi:Server","org.xidea.jsi:COL","org.xidea.jsi:PackageOptimize"};
		}
		log.info("Args:"+JSONEncoder.encode(args));
		new CompressAction(args).execute();
	}

	public void execute() throws Exception {
		JavaScriptCompressor compressor = toolkit.createJavaScriptCompressor();
		compressor.setCompressorConfig(config);

		PrintStream out = System.out;
		if (output != null) {
			log.info("结果输出至：" + output);
			File outputFile = new File(output);
			if(!outputFile.exists()){
				outputFile.createNewFile();
			}
			out = new PrintStream(new FileOutputStream(outputFile));
		} else {
			log.error("请通过参数 -output <file> 指定输出路径");
		}

		boolean first = true;
		Iterator<String> iterator = source.iterator();
		boolean endCharQute = false;
		while (iterator.hasNext()) {
			String file = iterator.next();
			InputStreamReader in = new InputStreamReader(new FileInputStream(
					file), encoding);
			char[] cbuf = new char[1024];
			int count;
			StringBuilder buf = new StringBuilder();
			while ((count = in.read(cbuf)) > -1) {
				buf.append(cbuf, 0, count);
			}
			String text;
			if (format) {
				log.info("format:"+file+config);
				text = compressor.format(buf.toString());
			} else {
				log.info("compress:"+file+config);
				text = compressor.compress(buf.toString(), null);
			}

			if (first) {
				first = false;
			} else {
				text = PRE_WHITESPACE.matcher(text).replaceAll("");
				if (endCharQute && text.trim().startsWith("(")) {
					out.print(";");
				}
				out.print(System.getProperty("line.separator"));
			}
			out.print(text);
			text = POST_WHITESPACE.matcher(text).replaceAll("");
			endCharQute = text.length() > 0 && text.endsWith(")");
		}
		out.flush();
		out.close();
	}
}
