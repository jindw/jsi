package org.xidea.template.parser;

import java.util.List;

public abstract interface Parser {
	/**
     * 给出文件内容或url，解析模版源文件。
     * 如果指定了base，当作url解析，无base，当作纯文本解析
     * @public
     * @abstract
     * @return <Array> result
     */
    public abstract List<Object> parse(Object node,ParseContext context);

}
