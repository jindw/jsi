var action = {
  sort:function(list, inc){
    $("sort").className = inc?"down":"up";
    list.sort(function(a, b){
      var f = a.name.localeCompare(b.name);
      return inc?f:-f;
    });
    render(data);
  },
  create : function(name){
    data.list.push({id: +new Date(),name: name});
    render(data);
  },
  edit : function(id){
    each(data.list, function(value, i){
      data.list[i].state = value.id == id ? "edit" : "normal";
    });
    render(data);
  },
  del : function(id){
    each(data.list, function(value, i){
      if(value.id == id){
        data.list.splice(i,1);
      }
    })
    render(data);
  },
  save : function(id){
    each(data.list, function(value, i){
      if(value.id == id){
        value.name = $("g_" + id).value;
        value.state = "normal";
      }
    });
    render(data);
  },
  cancel : function(id){
    each(data.list, function(value, i){
      data.list[i].state = "normal";
    });
    render(data);
  }
}
function $(id){
  return document.getElementById(id);
}
function each(obj, fn){
  for (var i = 0, length = obj.length; i < length; i++) {
     fn.call(obj[i], obj[i], i); 
  }
}
function render(data){
  $("container").innerHTML = teamList(data);
}

var model = {
  "list" : [
    {id:1,name:"分组1"},
    {id:2,name:"分组2"},
    {id:3,name:"分组3"}
  ]
}

render(data);

