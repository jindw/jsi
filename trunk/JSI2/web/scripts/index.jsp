<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"
 isELIgnored="false"%>/**${param['path'] ==null?'':'/'}$JSI.cacheScript("${param['path']}".replace(/\/[^/]*$/,'').replace(/\//g,'.'),"${param['path']}".replace(/^.*\//,''),function(){eval(this.varText);<jsp:include
 page="${param['path'] != null?param['path']:'error'}"/>
})
${param['path'] == null?'<h3>no script specified!!</h3> <hr>REF:<a href=http://www.xidea.org/project/jsi/>JSI Home</a><script>if(confirm("这时一个JSI的代理程序，但是您未指定脚本路径，\\n跳转到JSI主页了解根多？ "))window.location="http://www.xidea.org/project/jsi/"</script>':''}
<!--这是一个JSP EL 版的 JSI 代理程序 如果你的配置不支持EL可以使用index-nel.jsp代替 -->/**/