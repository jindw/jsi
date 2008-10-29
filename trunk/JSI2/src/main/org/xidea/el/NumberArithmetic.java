package org.xidea.el;

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
		return clazz.isInstance(n1) && clazz.isInstance(n2);
	}

	protected double toDouble(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			return Double.parseDouble(String.valueOf(value));
		}
	}

	protected float toFloat(Object value) {
		if (value instanceof Number) {
			return ((Number) value).floatValue();
		} else {
			return Float.parseFloat(String.valueOf(value));
		}
	}

	protected float toInt(Object value) {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		} else {
			return Integer.parseInt(String.valueOf(value));
		}
	}

	protected float toLong(Object value) {
		if (value instanceof Number) {
			return ((Number) value).longValue();
		} else {
			return Long.parseLong(String.valueOf(value));
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
		} else if (isType(Integer.class, n1, n2)) {
			return n1.intValue() + n2.intValue();
		} else if (isType(Short.class, n1, n2)) {
			return n1.shortValue() + n2.shortValue();
		} else if (isType(Byte.class, n1, n2)) {
			return n1.byteValue() + n2.byteValue();
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
		} else if (isType(Integer.class, n1, n2)) {
			return n1.intValue() - n2.intValue();
		} else if (isType(Short.class, n1, n2)) {
			return n1.shortValue() - n2.shortValue();
		} else if (isType(Byte.class, n1, n2)) {
			return n1.byteValue() - n2.byteValue();
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
		} else if (isType(Integer.class, n1, n2)) {
			return n1.intValue() * n2.intValue();
		} else if (isType(Short.class, n1, n2)) {
			return n1.shortValue() * n2.shortValue();
		} else if (isType(Byte.class, n1, n2)) {
			return n1.byteValue() * n2.byteValue();
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
			long left = n1.longValue() % n2.longValue();
			if(left == 0){
				if (isType(Long.class, n1, n2)) {
					return n1.longValue() / n2.longValue();
				} else if (isType(Integer.class, n1, n2)) {
					return n1.intValue() / n2.intValue();
				} else if (isType(Short.class, n1, n2)) {
					return n1.shortValue() / n2.shortValue();
				} else if (isType(Byte.class, n1, n2)) {
					return n1.byteValue() / n2.byteValue();
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
		} else if (isType(Integer.class, n1, n2)) {
			return n1.intValue() % n2.intValue();
		} else if (isType(Short.class, n1, n2)) {
			return n1.shortValue() % n2.shortValue();
		} else if (isType(Byte.class, n1, n2)) {
			return n1.byteValue() % n2.byteValue();
		}
		return n1.doubleValue() % n2.doubleValue();
	}

}
