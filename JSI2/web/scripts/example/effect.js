/**
 * 滑动面板实现.
 * 当指定元素可见时，将其第一个子元素向上滑动至完全被遮掩（折叠）。
 * 当指定元素不可见时，将其第一个子元素向下滑动至完全显示（展开）。
 * @param panel 滑动面板id
 */
function slidePanel(panel){
  panel = document.getElementById(panel);
  if(panel.style.display=='none'){
    //调用Scriptaculous Effect的具体滑动展开实现
    new Effect.SlideDown(panel);
  }else{
    //调用Scriptaculous Effect的具体滑动闭合实现
    new Effect.SlideUp(panel);
  }
}
