/**<%='/'%><%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"
 isELIgnored="false"%><%
    String path = request.getParameter("path");
    if(path == null){
        // 没有指定需要代理的路径，跳转到JSI项目主页 
        response.sendRedirect("http://www.xidea.org/project/jsi");
        return;
    }
    int pos = path.lastIndexOf('/');
    String packageName = path.substring(0,pos).replace('/', '.');
    String fileName = path.substring(pos+1);
    String url = request.getRequestURI().substring(request.getContextPath().length());
    url = url.substring(0,url.lastIndexOf('/')+1);
    java.io.InputStream in = application.getResourceAsStream(url + path);
    java.io.Reader reader = new java.io.InputStreamReader(in,response.getCharacterEncoding());
    StringBuffer source = new StringBuffer();
    char[] cbuf = new char[1024];
    int count;
    while((count = reader.read(cbuf))>=0){
        source.append(cbuf,0,count);
    }
    
%>$JSI.preload('<%=packageName%>','<%=fileName%>',function(){eval(this.varText);<%=source%>
})

<%--
您可以修改jsp的page指令，达到修改代理脚本编码的目的。
这是一个JSP 版本的 JSI 代理程序 
有些服务端可能禁用EL表达式或者脚本执行功能，所以，我们准备了三个版本的Java代理程序: JSP 版本、EL版本、和过滤器版本
--%>
/***/