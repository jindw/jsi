package org.xidea.el.operation;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 模拟ECMA262行为，保持基本一至，但迫于简单原则，略有偷懒行为^_^
 * 
 * @author jindw
 * @see org.mozilla.javascript.NativeGlobal
 */
public class ECMA262Global implements Invocable {

	public static final int ID_DECODEURI = 1, ID_DECODEURICOMPONENT = 2,
			ID_ENCODEURI = 3, ID_ENCODEURICOMPONENT = 4, ID_ISFINITE = 7,
			ID_ISNAN = 8, ID_PARSEFLOAT = 10, ID_PARSEINT = 11;

	private static final Object[] IDMAP = { ID_DECODEURI, "decodeURI",
			ID_DECODEURICOMPONENT, "decodeURIComponent", ID_ENCODEURI,
			"encodeURI", ID_ENCODEURICOMPONENT, "encodeURIComponent",
			ID_ISFINITE, "isFinite", ID_ISNAN, "isNaN", ID_PARSEFLOAT,
			"parseFloat", ID_PARSEINT, "parseInt" };

	public static void appendTo(Map<String, Invocable> globalInvocableMap) {
		for (int i = 0; i < IDMAP.length; i += 2) {
			globalInvocableMap.put((String) IDMAP[i + 1],
					new ECMA262Global((Integer) IDMAP[i]));
		}
	}
	private final int type;

	public ECMA262Global(int type) {
		this.type = type;
	}

	private static Object getArg(Object[] args, int index, Object defaultValue) {
		if (index >= 0 && index < args.length) {
			return args[index];
		} else {
			return defaultValue;
		}
	}

	public Object invoke(Object... args) throws Exception {
		switch (type) {
		case ID_ENCODEURICOMPONENT:
			return URLEncoder.encode(String.valueOf(getArg(args, 0, null)),
					String.valueOf(getArg(args, 1, "utf-8")));
		case ID_DECODEURICOMPONENT:
			return URLDecoder.decode(String.valueOf(getArg(args, 0, null)),
					String.valueOf(getArg(args, 1, "utf-8")));
		case ID_ISFINITE:
		case ID_ISNAN:
			Object o = getArg(args, 0, Double.NaN);
			Number number = ECMA262Util.ToNumber(o);
			if (number instanceof Double) {
				double d = number.doubleValue();
				if (type == ID_ISNAN) {
					return d != d;
				} else {
					return d == d && !Double.isInfinite(d);
				}
			}
			return true;
		case ID_PARSEFLOAT:
			return Double.valueOf(String.valueOf(getArg(args, 0, null)));
		case ID_PARSEINT:
			return Double.valueOf(String.valueOf(getArg(args, 0, null)));
		case ID_ENCODEURI:
		case ID_DECODEURI:
			
		}
		throw new UnsupportedOperationException(toString());
	}
	public String toString(){
		for (int i = 0; i < IDMAP.length; i += 2) {
			if((Integer) IDMAP[i] == type){
				return (String) IDMAP[i + 1];
			}
		}
		return null;
	}

}
