/*
 * 这个包中，我们演示JSI脚本依赖的定义。
 * 
 * 脚本依赖在很多编程语言中表现为import或者use指令。
 * 但是js不提供类似语法支持，而且由于js的下载阻塞和解释型特征。依赖描述比编译型语言更加困难
 * 在JSI中，对于类库的依赖定义，我们采用一种无侵入依赖描述方式，代替传统的import指令。
 * 而页面的导入，依然采用传统的$import函数
 */
this.addScript('show-detail.js','showDetail'
               //装载前需要先装载JSON类库（事实上此处可以定义为装载后，先不理会那些复杂的优化手法吧^_^）
               ,"org.xidea.jsidoc.util:JSON"
               );