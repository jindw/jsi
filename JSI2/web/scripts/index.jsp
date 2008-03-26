<%
    String path = request.getParameter("path");
    if(path == null){
        // 没有指定需要代理的路径，跳转到JSI项目主页 
        response.sendRedirect("http://www.xidea.org/project/jsi");
        return;
    }
    int pos = path.lastIndexOf('/');
    String packageName = path.substring(pos+1);
    String fileName = path.substring(0,pos).replace('/', '.');
%>$JSI.cacheScript('<%=packageName%>','<%=fileName%>',function(){eval(this.varText);<jsp:include page="<%=path%>"/>
})

<%--
这是一个JSP 版本的 JSI 代理程序 
有些服务段可能禁用EL表达式或者脚本执行功能，所以，我们准备了两个版本的JSP代理程序: JSP 版本和EL版本
--%>