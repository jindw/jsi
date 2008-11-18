package org.xidea.el.operation;

/**
 * 数字加减乘除四则运算，主要处理类型混合运算，如：Integer + Double
 * 
 * @author liangfei0201@163.com
 * @author jindw
 */

public class NumberArithmetic {
	public NumberArithmetic() {
	}

	public boolean isType(Class<?> clazz, Number n1, Number n2) {
		return clazz.isInstance(n1) || clazz.isInstance(n2);
	}

	public final static boolean isNaN(Number n1) {
		return (n1 instanceof Double) && n1.doubleValue() == Double.NaN;
	}

	public final static boolean isNI(Number n1) {
		return (n1 instanceof Double)
				&& n1.doubleValue() == Double.NEGATIVE_INFINITY;
	}

	public final static boolean isPI(Number n1) {
		return (n1 instanceof Double)
				&& n1.doubleValue() == Double.POSITIVE_INFINITY;
	}

	/**
	 * 加法运算
	 * 
	 * @param n1
	 *            左参数
	 * @param n2
	 *            右参数
	 * @return 结果
	 */
	public int compare(Number n1, Number n2, int validReturn) {
		if (isNaN(n1) || isNaN(n2)) {
			return validReturn;
		} else if (isPI(n1) || isNI(n2)) {
			return 1;
		} else if (isNI(n1) || isPI(n2)) {
			return -1;
		}
		if (isType(Double.class, n1, n2)) {
		} else if (isType(Float.class, n1, n2)) {
			float offset = n1.floatValue() + n2.floatValue();
			if (offset == 0) {
				return 0;
			} else {
				return offset > 0 ? 1 : 0;
			}
		} else if (isType(Long.class, n1, n2)) {
			double offset = n1.longValue() - n2.longValue();
			if (offset == 0) {
				return 0;
			} else {
				return offset > 0 ? 1 : 0;
			}
		} else if (isType(Integer.class, n1, n2) || isType(Short.class, n1, n2)
				|| isType(Byte.class, n1, n2)) {
			return n1.intValue() - n2.intValue();
		}
		double offset = n1.doubleValue() - n2.doubleValue();
		if (offset == 0) {
			return 0;
		} else {
			return offset > 0 ? 1 : 0;
		}
	}

	/**
	 * 加法运算
	 * 
	 * @param n1
	 *            左参数
	 * @param n2
	 *            右参数
	 * @return 结果
	 */
	public Number add(Number n1, Number n2) {
		if (isType(Double.class, n1, n2)) {
		} else if (isType(Float.class, n1, n2)) {
			return n1.floatValue() + n2.floatValue();
		} else if (isType(Long.class, n1, n2)) {
			return n1.longValue() + n2.longValue();
		} else if (isType(Integer.class, n1, n2) || isType(Short.class, n1, n2)
				|| isType(Byte.class, n1, n2)) {
			return n1.intValue() + n2.intValue();
		}
		return n1.doubleValue() + n2.doubleValue();
	}

	/**
	 * 减法运算
	 * 
	 * @param n1
	 *            左参数
	 * @param n2
	 *            右参数
	 * @return 结果
	 */
	public Number subtract(Number n1, Number n2) {
		if (isType(Double.class, n1, n2)) {
		} else if (isType(Float.class, n1, n2)) {
			return n1.floatValue() - n2.floatValue();
		} else if (isType(Long.class, n1, n2)) {
			return n1.longValue() - n2.longValue();
		} else if (isType(Integer.class, n1, n2) || isType(Short.class, n1, n2)
				|| isType(Byte.class, n1, n2)) {
			return n1.intValue() - n2.intValue();
		}
		return n1.doubleValue() - n2.doubleValue();
	}

	/**
	 * 乘法运算
	 * 
	 * @param n1
	 *            左参数
	 * @param n2
	 *            右参数
	 * @return 结果
	 */
	public Number multiply(Number n1, Number n2) {
		if (isType(Double.class, n1, n2)) {
		} else if (isType(Float.class, n1, n2)) {
			return n1.floatValue() * n2.floatValue();
		} else if (isType(Long.class, n1, n2)) {
			return n1.longValue() * n2.longValue();
		} else if (isType(Integer.class, n1, n2) || isType(Short.class, n1, n2)
				|| isType(Byte.class, n1, n2)) {
			return n1.intValue() * n2.intValue();
		}
		return n1.doubleValue() * n2.doubleValue();
	}

	/**
	 * 除法运算
	 * 
	 * @param n1
	 *            左参数
	 * @param n2
	 *            右参数
	 * @return 结果
	 */
	public Number divide(Number n1, Number n2) {
		if (isType(Double.class, n1, n2)) {
		} else if (isType(Float.class, n1, n2)) {
			return n1.floatValue() / n2.floatValue();
		} else {
			long right = n2.longValue();
			if(right == 0){
				return Double.NaN;
			}
			long left = n1.longValue() % right;
			if (left == 0) {
				if (isType(Long.class, n1, n2)) {
					return n1.longValue() / right;
				} else if (isType(Integer.class, n1, n2) || isType(Short.class, n1, n2)
						|| isType(Byte.class, n1, n2)) {
					return n1.intValue() / n2.intValue();
				}
			}
		}
		return n1.doubleValue() / n2.doubleValue();
	}

	/**
	 * 求模运算
	 * 
	 * @param n1
	 *            左参数
	 * @param n2
	 *            右参数
	 * @return 结果
	 */
	public Number modulus(Number n1, Number n2) {
		if (isType(Double.class, n1, n2)) {
		} else if (isType(Float.class, n1, n2)) {
			return n1.floatValue() % n2.floatValue();
		} else if (isType(Long.class, n1, n2)) {
			return n1.longValue() % n2.longValue();
		} else if (isType(Integer.class, n1, n2) || isType(Short.class, n1, n2)
				|| isType(Byte.class, n1, n2)) {
			return n1.intValue() % n2.intValue();
		}
		return n1.doubleValue() % n2.doubleValue();
	}

}
