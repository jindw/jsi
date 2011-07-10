package org.jside.jsi.tools.ui;

import javax.swing.Icon;

import org.xidea.commons.i18n.swing.MessageBase;

public class Messages extends MessageBase {

	private static final String JSI_ITEM_PATH = "/org/xidea/jsidoc/styles/";

	// private static final String BUNDLE_NAME = "org.xidea.jsi.tools.messages";
	// //$NON-NLS-1$

	// private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
	// .getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static class ui {
		public static String saveAs = "另存为";

		public static String title = "JSI脚本分析工具";

		public static String file = "文件";

		public static String open = "打开";

		public static String save = "保存";

		public static String operation = "操作";

		public static String analyse = "代码分析";

		public static String compress = "压缩";

		public static String format = "格式化";

		public static String setting = "设置";

		public static String switchLanguage = "语言切换";

		public static String help = "帮助";

		public static String undo = "撤销";

		public static String redo = "重做";

		public static String about = "关于..";

		public static String helpContent = "帮助内容";

		public static String license = "使用许可";

		public static String homePage = "主页";

		public static String doSyntaxCompression = "执行语法压缩";

		public static String doTextCompression = "执行文本压缩";

		public static String compatible = "兼容IE5,NS3";

		public static String compressionOptions = "执行文本压缩的条件选项";

		public static String ratioCondition = "只有压缩比率小于该值时，才可采用文本压缩";

		public static String ratioBeFloat = "文本压缩比率要求必须位有效小数（0.0-1.0）";

		public static String sizeBeInteger = "文本压缩大小要求必须位有效整数";

		public static String sizeCondition = "只有文件大于该值时，才可采用文本压缩";

		public static String ratioConditionLabel = "低于比率(0-1)：";

		public static String sizeConditionLabel = "文件大于(字节)：";

		public static String clickToCopy = "点击确定拷贝网址";

		public static String openWebPageFailure = "直接打开网页失败";

		public static String unknowFile = "<未知脚本文件>";

		public static String selectCharset = "选择字符集";

		public static String multiProbableCharset = "请从下列可能选项，选择正确字符集";
		//
		public static String ok = "确定";
		public static String cancle = "取消";
		public static String globalVariableMap = "全局变量混淆映射";
		public static String unsafeLabel = "<包含非安全函数调用,无法混淆>";
		public static String protectedLabel = "<保护变量>";
		public static String variableNameLabel = "变量";
		public static String confusedValueLabel = "混淆为";
		public static String confuseSetting = "混淆设置";
		public static String analyserReport = "脚本分析报告";
		public static String autoConfuse = "自动混淆";
		public static String perfix = "前缀：";
		public static String perfixTooltip = "全局变量自动混淆的前缀设置";

		public static String server = "服务器";

		public static String openHome = "打开首页";
		public static String openWebRoot = "浏览目录";
		public static String openToolsHome = "工具首页";
		public static String openFrame = "打开窗口";
		public static String exit = "退出系统";

		public static String debugOptions = "调试处理选项";
		public static String debugCalls = "调试函数集";
		public static String debugCallsTooltip = "eg:$log.debug,alert";
		public static String debugCallsPattern = "调试函数集格式为：debugFn1,debugFn2,debugFn3";

		public static String feature = "附加特征";
		public static String featureTooltip = "附加特征集,踢除 if('<调试标记>'){/*踢除块*/....}else{/*保留块*/....}块eg:$debug";
		public static String featurePattern = "附加特征：:debug,namespace:label,com.xyz:label";

//		public static String toProject = "添加为JSI工程";
		public static String export = "导出(JSI)";
		
		public static String expand = "展开节点";
		public static String expandAll = "展开全部";
		public static String collapse = "收起节点";
		public static String collapseAll = "收起全部";

		public static Icon computerIcon = loadIcon(Messages.class,
				"FileView.computerIcon", "icon/computer.png");
		public static Icon diskIcon = loadIcon(Messages.class,
				"FileView.hardDriveIcon", "icon/disk.png");
		public static Icon dirIcon = loadIcon(Messages.class,
				"FileView.directoryIcon", "icon/dir.png");
		public static Icon fileIcon = loadIcon(Messages.class,
				"FileView.fileIcon");

		public static Icon projectOpenIcon = loadIcon(Messages.class,
				"icon/project-open.gif");
		public static Icon projectCloseIcon = loadIcon(Messages.class,
				"icon/project-close.gif");
		public static Icon sourceIcon = loadIcon(Messages.class,
				"icon/source.gif");
		public static Icon packageIcon = loadIcon(Messages.class, JSI_ITEM_PATH
				+ "item-package.gif");
		public static Icon packageRefIcon = loadIcon(Messages.class,
				JSI_ITEM_PATH + "item-package-ref.gif");
		public static Icon invalidPackageIcon = loadIcon(Messages.class,
				"icon/item-package-invalid.gif");
		public static Icon jsFileIcon = loadIcon(Messages.class, JSI_ITEM_PATH
				+ "item-file.gif");
		public static Icon xmlIcon = loadIcon(Messages.class,
				"icon/xml.gif");
		public static Icon functionIcon = loadIcon(Messages.class,
				JSI_ITEM_PATH + "item-function.gif");
		public static Icon objectIcon = loadIcon(Messages.class, JSI_ITEM_PATH
				+ "item-object.gif");

		public static String closeFile = "关闭文件";
		public static String closeOther = "关闭其他";
		public static String closeAll = "关闭所有";

		public static String refreshFile = "刷新目录";
		public static String explorerFile = "浏览目录";
		public static String createFile = "创建文件";
		public static String deleteFile = "删除文件";

		public static String createPackage = "创建脚本包";

		public static String openPage = "打开网页";


	}
}
