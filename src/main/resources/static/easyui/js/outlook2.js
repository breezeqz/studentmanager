//入口函数，$(function(){})等同于$(document).ready(function())，意思很简单，就是等页面加载完毕之后，才开始执行函数。
$(function(){
    InitLeftMenu();//初始化左侧菜单
    tabClose();//双击关闭选项卡并为右键绑定菜单
    tabCloseEven();//关闭选项卡


})

//初始化左侧
function InitLeftMenu() {
    $("#nav").accordion({animate:false});

    $.each(_menus.menus, function(i, n) {
        var menulist ='';
        menulist +='<ul>';//ul li无序列表，ol li有序列表
        $.each(n.menus, function(j, o) {
            menulist += '<li><div><a ref="'+o.menuid+'" href="#" rel="' + o.url + '" ><span class="icon '+o.icon+'" >&nbsp;</span><span class="nav">' + o.menuname + '</span></a></div></li> ';
        })
        menulist += '</ul>';

        $('#nav').accordion('add', {
            title: n.menuname,
            content: menulist,
            iconCls: 'icon ' + n.icon,
            selected: false
        });

    });
    //点击链接事件
    $('.easyui-accordion li a').click(function(){
        var tabTitle = $(this).children('.nav').text();//获取子元素的内容，即menuname
        var url = $(this).attr("rel");//获取rel属性，url
        var menuid = $(this).attr("ref");//获取ref属性，menuid
        var icon = getIcon(menuid,icon);

        addTab(tabTitle,url,icon);
        $('.easyui-accordion li div').removeClass("selected");//删除class属性值，selected属性为easyui的class属性
        $(this).parent().addClass("selected");
    }).hover(function(){//鼠标悬停事件
        $(this).parent().addClass("hover");//添加class属性，hover属性为easyui的class属性
    },function(){
        $(this).parent().removeClass("hover");
    });

    //选中第一个，默认打开第一个折叠面板
    var panels = $('#nav').accordion('panels');//panels方法获取所有的面板（panles）
    var t = panels[0].panel('options').title;//option方法返回选项（options）属性（property）
    $('#nav').accordion('select', t);
}
//获取左侧导航的图标
function getIcon(menuid){
    var icon = 'icon ';
    $.each(_menus.menus, function(i, n) {
        $.each(n.menus, function(j, o) {
            if(o.menuid==menuid){
                icon += o.icon;
            }
        })
    })
    return icon;
}

function addTab(subtitle,url,icon){
    //如果subtitle标签不存在，就添加标签
    if(!$('#tabs').tabs('exists',subtitle)){
        $('#tabs').tabs('add',{
            title:subtitle,
            content:createFrame(url),
            closable:true,
            icon:icon
        });
    }else{
        $('#tabs').tabs('select',subtitle);
        $('#mm-tabupdate').click();
    }
    tabClose();
}

function createFrame(url)
{   //调用src指定controller，src规定在iframe中显示的文档的URL。
    var s = '<iframe scrolling="auto" frameborder="0"  src="'+url+'" style="width:100%;height:100%;"></iframe>';
    return s;
}

function tabClose()
{
    /*双击关闭TAB选项卡*/
    $(".tabs-inner").dblclick(function(){
        var subtitle = $(this).children(".tabs-closable").text();
        $('#tabs').tabs('close',subtitle);
    })
    /*为选项卡绑定右键菜单*/
    $(".tabs-inner").bind('contextmenu',function(e){
        $('#mm').menu('show', {
            left: e.pageX,
            top: e.pageY
        });

        var subtitle =$(this).children(".tabs-closable").text();

        $('#mm').data("currtab",subtitle);
        $('#tabs').tabs('select',subtitle);
        return false;
    });
}
//绑定右键菜单事件
function tabCloseEven()
{
//刷新
    $('#mm-tabupdate').click(function(){
        var currTab = $('#tabs').tabs('getSelected');
        var url = $(currTab.panel('options').content).attr('src');
        $('#tabs').tabs('update',{
            tab:currTab,
            options:{
                content:createFrame(url)
            }
        })
    })
//关闭当前
    $('#mm-tabclose').click(function(){
        var currtab_title = $('#mm').data("currtab");
        $('#tabs').tabs('close',currtab_title);
    })
//全部关闭
    $('#mm-tabcloseall').click(function(){
        $('.tabs-inner span').each(function(i,n){
            var t = $(n).text();
            $('#tabs').tabs('close',t);
        });
    });
//关闭除当前之外的TAB
    $('#mm-tabcloseother').click(function(){
        $('#mm-tabcloseright').click();
        $('#mm-tabcloseleft').click();
    });
//关闭当前右侧的TAB
    $('#mm-tabcloseright').click(function(){
        var nextall = $('.tabs-selected').nextAll();
        if(nextall.length==0){
//msgShow('系统提示','后边没有啦~~','error');
            alert('后边没有啦~~');
            return false;
        }
        nextall.each(function(i,n){
            var t=$('a:eq(0) span',$(n)).text();
            $('#tabs').tabs('close',t);
        });
        return false;
    });
//关闭当前左侧的TAB
    $('#mm-tabcloseleft').click(function(){
        var prevall = $('.tabs-selected').prevAll();
        if(prevall.length==0){
            alert('到头了，前边没有啦~~');
            return false;
        }
        prevall.each(function(i,n){
            var t=$('a:eq(0) span',$(n)).text();
            $('#tabs').tabs('close',t);
        });
        return false;
    });

//退出
    $("#mm-exit").click(function(){
        $('#mm').menu('hide');
    })
}

//弹出信息窗口 title:标题 msgString:提示信息 msgType:信息类型 [error,info,question,warning]
function msgShow(title, msgString, msgType) {
    $.messager.alert(title, msgString, msgType);
}