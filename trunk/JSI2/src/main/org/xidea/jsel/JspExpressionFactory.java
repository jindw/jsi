package org.xidea.jsel;
//package org.jside.jsel;
//
//import java.beans.FeatureDescriptor;
//import java.beans.PropertyDescriptor;
//import java.lang.reflect.Method;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Pattern;
//
//import javax.el.ELContext;
//import javax.el.ELException;
//import javax.el.ELResolver;
//import javax.el.FunctionMapper;
//import javax.el.MethodExpression;
//import javax.el.PropertyNotFoundException;
//import javax.el.PropertyNotWritableException;
//import javax.el.ValueExpression;
//import javax.el.VariableMapper;
//import javax.script.ScriptEngineManager;
//import javax.servlet.ServletContext;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletRequestWrapper;
//import javax.servlet.jsp.JspApplicationContext;
//import javax.servlet.jsp.JspFactory;
//import javax.servlet.jsp.PageContext;
//
//import org.jside.template.Expression;
//import org.jside.template.ExpressionFactory;
//import org.jside.template.PropertyExpression;
///**
// * 该程序必须在Java Servlet环境下运行。
// * @author ut
// *
// */
//public class JspExpressionFactory  implements ExpressionFactory{
//
//	private javax.el.ExpressionFactory factory;
//
//	public JspExpressionFactory(ServletContext context){
//		JspApplicationContext jspContext = JspFactory.getDefaultFactory().getJspApplicationContext(context);
//		factory =jspContext.getExpressionFactory();
//	}
//	public Expression createExpression(final String value){
//		return new Expression(){
//			ValueExpression el =null;
//			public Object evaluate(Map<Object, Object> context) {
//				try {
//					ELContext elcontext =  (ELContext) PropertyExpression.getValue(context,ELContext.class);
//					if(el == null){
//					    el = factory.createValueExpression(elcontext,value, Object.class);
//					}
//					return el.getValue(elcontext);
//				} catch (Exception e) {
//				}
//				return null;
//			}
//			
//		};
//	}
//}
//class ELContextImpl extends ELContext{
//	FunctionMapper functionMapper = new FunctionMapper(){
//		@Override
//		public Method resolveFunction(String arg0, String arg1) {
//			return null;
//		}
//	};
//	ELResolver resolver = new ELResolver(){
//		@Override
//		public Class<?> getCommonPropertyType(ELContext arg0, Object arg1) {
//			return null;
//		}
//		@Override
//		public Iterator<FeatureDescriptor> getFeatureDescriptors(
//				ELContext arg0, Object arg1) {
//			return null;
//		}
//
//		@Override
//		public Class<?> getType(ELContext arg0, Object arg1, Object arg2)
//				throws NullPointerException, PropertyNotFoundException,
//				ELException {
//			return null;
//		}
//
//		@Override
//		public Object getValue(ELContext arg0, Object arg1, Object arg2)
//				throws NullPointerException, PropertyNotFoundException,
//				ELException {
//			return null;
//		}
//
//		@Override
//		public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2)
//				throws NullPointerException, PropertyNotFoundException,
//				ELException {
//			return false;
//		}
//
//		@Override
//		public void setValue(ELContext arg0, Object arg1, Object arg2,
//				Object arg3) throws NullPointerException,
//				PropertyNotFoundException, PropertyNotWritableException,
//				ELException {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	};
//	@Override
//	public ELResolver getELResolver() {
//		return resolver;
//	}
//	@Override
//	public FunctionMapper getFunctionMapper() {
//		return functionMapper;
//	}
//	@Override
//	public VariableMapper getVariableMapper() {
//		return new VariableMapper(){
//			@Override
//			public ValueExpression resolveVariable(String arg0) {
//				return null;
//			}
//			@Override
//			public ValueExpression setVariable(String arg0, ValueExpression arg1) {
//				return null;
//			}
//			
//		};
//	}
//
//
//}