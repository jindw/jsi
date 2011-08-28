var impl = new Packages.org.jside.webserver.action.TemplateAction(null);
var Context = Packages.org.jside.webserver.RequestContext;
var af = new Packages.org.jside.webserver.sjs.AdapterFactory();
var lite = {
    render:function(path,data,out){
        var context = Context.get();
        data = data || getGlobalContext();
        out = out || new java.io.OutputStreamWriter(context.getOutputStream(),"UTF-8");
        context.setContentType("text/html;charset=UTF-8");
        impl.render(path,af.toJava(data),out);
    }
}

function getGlobalContext(){
	return Packages.org.jside.webserver.sjs.JSExcutor.getCurrentInstance().getGlobals();
}