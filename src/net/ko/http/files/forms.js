/**
 * Forms
 * Utilitaires javascript pour KObject
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2012
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: koLibrary-1.0.0.21-beta1-js3$
 */


var $A = function(iterable) {
  if (!iterable) return [];
  if (iterable.toArray) {
    return iterable.toArray();
  } else {
    var results = [];
    for (var i = 0, length = iterable.length; i < length; i++)
      results.push(iterable[i]);
    return results;
  }
};

Object.extend = function(destination, source) {
  for (var property in source)
    destination[property] = source[property];
  return destination;
};

Object.extend(Object, {
  inspect: function(object) {
    try {
      if (object === undefined) return 'undefined';
      if (object === null) return 'null';
      return object.inspect ? object.inspect() : object.toString();
    } catch (e) {
      if (e instanceof RangeError) return '...';
      throw e;
    }
  },

  toJSON: function(object) {
    var type = typeof object;
    switch (type) {
      case 'undefined':
      case 'function':
      case 'unknown': return;
      case 'boolean': return object.toString();
    }

    if (object === null) return 'null';
    if (object.toJSON) return object.toJSON();
    if (Object.isElement(object)) return;

    var results = [];
    for (var property in object) {
      var value = Object.toJSON(object[property]);
      if (value !== undefined)
        results.push(property.toJSON() + ': ' + value);
    }

    return '{' + results.join(', ') + '}';
  },

  toQueryString: function(object) {
    return $H(object).toQueryString();
  },

  toHTML: function(object) {
    return object && object.toHTML ? object.toHTML() : String.interpret(object);
  },

  keys: function(object) {
    var keys = [];
    for (var property in object)
      keys.push(property);
    return keys;
  },

  values: function(object) {
    var values = [];
    for (var property in object)
      values.push(object[property]);
    return values;
  },

  clone: function(object) {
    return Object.extend({ }, object);
  },

  isElement: function(object) {
    return object && object.nodeType == 1;
  },

  isArray: function(object) {
    return object && object.constructor === Array;
  },

  isHash: function(object) {
    return object instanceof Hash;
  },

  isFunction: function(object) {
    return typeof object == "function";
  },

  isString: function(object) {
    return typeof object == "string";
  },

  isNumber: function(object) {
    return typeof object == "number";
  },

  isUndefined: function(object) {
    return typeof object == "undefined";
  }
});

Object.extend(String, {
  interpret: function(value) {
    return value == null ? '' : String(value);
  },
  specialChar: {
    '\b': '\\b',
    '\t': '\\t',
    '\n': '\\n',
    '\f': '\\f',
    '\r': '\\r',
    '\\': '\\\\'
  }
});

Object.extend(String.prototype, {
  capitalize: function() {
    return this.charAt(0).toUpperCase() + this.substring(1).toLowerCase();
  },	
   isJSON: function() {
    var str = this.replace(/\\./g, '@').replace(/"[^"\\\n\r]*"/g, '');
    return (/^[,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]*$/).test(str);
  } 
});


var Class={};
Class.create=function(classFille,classParent){
	if ( typeof classFille.initialized == "undefined" ) {
		for (var element in classParent.prototype ) {
			classFille.prototype[element] = classParent.prototype[element];}
	}
};
var keyCodes=new Array();
var Forms = {};//nameSpace
(function(){
	Forms.MessageDialog=function(id,title,content,submitFunction){
		Class.create(Forms.MessageDialog, Forms.LightBox);
		Forms.LightBox.call(this,id);		
		this.init(title,content,submitFunction);
	};
	Forms.MessageDialog.prototype={
		submitFunction:null,
		innerSubmit:null,
		init:function(title,content,submitFunction){
			this.title=title;
			this.content=content;
			this.setSubmitFunction(submitFunction);
			
		},
		setSubmitFunction:function(submitFunction){
			this.submitFunction=submitFunction;
			var me=this;
			this.innerSubmit=function(obj){
				canclose=true;
				if(me.submitFunction!=null) {
					try{ canclose=me.submitFunction(me);}catch(ex){canclose=true;}
				}
				return canclose;
			};			
		},
		addButtons:function(buttons){
			if(typeof(buttons)=='string'){
				try{
					buttons=JSON.parse(buttons);
				}catch(ex){}
			}
			if(typeof(buttons)=='object')
			for(var bt in buttons){
				this.addAction(buttons[bt].caption,this.innerSubmit,buttons[bt].keycode);
			}			
		},
		addButton:function(caption,keycode){
			this.addAction(caption,this.innerSubmit,keycode);
		}
	};
	
})();
(function(){ // début scope local
// déclaration de la classe LightBox
	Forms.LightBox = function(id){
		this.id=id;
		this.setAllContent();
		return this;
	};
	Forms.LightBox.prototype={
		id:'',
		title:'',
		content:'',
		modalResult: '',
		func: null,
		onclose: true,
		hasCloseButton:true,
		minWidth:'500px',
		modal: true,
		data:null,
		results:new Array(),
			
		waitFor: function(mr,func){
			this.results[mr]=func;
		},
		submit: function(mr){
			this.modalResult=mr;
			if(this.results[mr]!=undefined){
				try{
					this.onclose=this.results[mr](this);
				}
				catch(ex){}
			}
			if(this.onclose)
				this.hide();
		},
		addCloseButton:function(){
			if($(this.id+"-closeButton")==null){
				var divClose=document.createElement("div");
				divClose.title="Fermeture de la boîte de dialogue";
				divClose.id=this.id+"-closeButton";
				divClose.className="closeButton";
				$(this.id+"-box").appendChild(divClose);
				var self=this;
				Forms.Utils.addEventToElement(divClose, "click", function(){self.hide();}, false);
			}
		},
		innerAddButton: function(caption,keycode){
			var btn=$(this.id+'.'+caption+'-boxBtn');
			if(btn==null){
				btn=document.createElement("input");
				btn.id=this.id+'.'+caption+"-boxBtn";
				btn.type="button";					
				$(this.id+"-boxButtons").appendChild(btn);
			} 						
			var me=this;
			var myfunction=function(){
				me.submit(caption);
			};
			btn.value=caption;
			btn.className="btn";
			btn.onclick=myfunction;
			if(keycode!=undefined)
				keyCodes[eval(keycode)]=myfunction;	
		},
		addActions:function(actions){
			if(typeof(actions)=='string'){
				try{
					actions=JSON.parse(actions);
				}catch(ex){}
			}
			if(typeof(actions)=='object')
			for(var a in actions){
				this.addAction(actions[a].caption,actions[a].action,actions[a].keycode);
			}
		},
		addAction:function(caption,action,keyCode){
			this.innerAddButton(caption,keyCode);
			this.waitFor(caption,action);			
		},
		deleteAction:function(action){
			delete this.results[action];
			var element=$(this.id+"."+action+"-boxBtn");
			if(element!=null){
				$(this.id+"-boxButtons").removeChild(element);
			}
		},
		clearActions:function(){
			for(var actKey in this.results)
				this.deleteAction(this.results[actKey]);
			this.results=new Array();
		},
		setBtnCaption:function(btn,newCaption){
			var element=$(this.id+"."+btn+"-boxBtn");
			if(element!=null)
				element.value=newCaption;
		},
		onkeydown:function(e){
			var keycode=null;
			if (window.event) keycode = window.event.keyCode;
				else if (e) keycode = e.which;
			if(keyCodes[keycode]!=undefined){
				void(0);
				keyCodes[keycode](this);
				return false;
			}
		},
		hide: function(){
			try{
				document.onkeydown=null;
				//keyCodes=new Array();
				if(this.modal)
					$('filter').style.display='none';
				$(this.id+'-box').style.display='none';
			}
			catch(ex){}
		},
		show: function(){
			try{
				//if($(this.id+'-box')==null)
				this.setAllContent();
				document.onkeydown=this.onkeydown;
				if(this.modal)
					$('filter').style.display='block';
				else
					$('filter').style.display='none';
				$(this.id+'-box').style.display='table';}
			catch(ex){}
		},
		showModal:function(){
			this.modal=true;
			this.show();
		},
		createStructure:function(){
			if($('filter')==null){
				var filter=document.createElement("div");
				filter.style.display='none';
				filter.id='filter';
				document.body.appendChild(filter);
			}else
				filter=$('filter');
			if($(this.id+'-box')==null){
				box=document.createElement("div");
				document.body.appendChild(box);
				box.id=this.id+'-box';
				Forms.Utils.addDrag(box);
				box.className='box';
				box.innerHTML="<div id='"+this.id+"-boxheader' class='boxheader'>\n"
				+"		<div id='"+this.id+"-boxtitle' class='boxtitle'>"+this.title+"</div>\n"
				+"	</div>\n"
				+"	<div id='"+this.id+"-boxcontent' class='boxcontent'>"+this.content+"</div>\n"
				+"	<div id='"+this.id+"-boxButtons' class='boxButtons'></div>\n";
				if(this.hasCloseButton)
					this.addCloseButton();
			}				
		},
		setAllContent:function(){
			this.setContent(this.content);
		},
		setContent: function(content){
			this.content=content;
			this.createStructure();
			$(this.id+"-boxtitle").innerHTML=this.title;
			Forms.Utils.setInnerHTML($(this.id+"-boxcontent"), content);
		},
		setTitle:function(title){
			this.title=title;
			if($(this.id+"-boxtitle")!=null)
				$(this.id+"-boxtitle").innerHTML=title;
		}
	};
	var self = Forms.LightBox;
})();
(function(){
	Forms.KObject={
		refreshContent: function(idElement){
			if(idElement==undefined) idElement='ajxContent';
			element=$(idElement);
			if(element!=undefined){
				var script_name = window.location.href;
				var filename = script_name.replace(/^.*(\\|\/)/, '');
				var pathname = script_name.replace(filename, '');
				ajx=new Forms.Ajax(idElement,pathname+'ajx'+filename);
				ajx.get();
			}
		}
	};
	
	var self = Forms.KObject;	
})();
(function(){
	Forms.Selector=function(container,event,allowNull,functionName,selectedStyle){
		if(allowNull!==undefined)
			this.allowNull=allowNull;
		if(functionName!==undefined)
			this.functionName=functionName;
		if(selectedStyle!==undefined)
			this.selectedStyle=selectedStyle;
		this.container=container;
		if(this.container.selected==null)
			this.container.selected=new Array();
		if(event!==undefined)
			this.event=event;
		return this;
	};
	Forms.Selector.prototype={
		functionName:null,
		allowNull:false,
		event:"click",
		startIndex:1,
		selectedStyle:{"backgroundColor":"#E6E6E6","color":"black"},
		container:null,
		addEvents:function(objects){
			var self=this;
			for ( var i = this.startIndex; i < objects.length; i++ ) {
				objects[i].selectorIndex=i;
				Forms.Utils.addEventToElement(objects[i],this.event,function(){self.select(this);},false);
				if(this.functionName!=null)
					Forms.Utils.addEventToElement(objects[i],this.event,this.functionName,false);
			}			
		},
		attachByName:function(elementName,startIndex){
			if(startIndex!==undefined)
				this.startIndex=startIndex;
			var objects=this.container.getElementsByName(elementName);
			this.addEvents(objects);
		},
		attachByTagName:function(tagName,startIndex){
			if(startIndex!==undefined)
				this.startIndex=startIndex;
			var objects=this.container.getElementsByTagName(tagName);
			this.addEvents(objects);
		},
		getSelected:function(){
			return this.container.selected[this.event];
		},
		setSelected:function(id){
			this.container.selected[this.event]=id;
		},
		select:function(element){
			Forms.Utils.setStyles(element,this.selectedStyle);
			selected=this.getSelected();
			if(selected!=undefined&&(selected!==element.id||this.allowNull))
				this.clearStyles($(selected));
			if(selected===element.id&&this.allowNull)
				this.setSelected(undefined);
			else
				this.setSelected(element.id);	
		},
		clearStyles:function (element){
		    for(var s in this.selectedStyle) {
		        try{element.style[s] = "";}
		        catch(e){}
		    }
		}
	};
})();
(function(){
	Forms.Accordion=function(element,speed,refresh){
		this.element=element;
		if(refresh!=undefined) this.refresh=refresh;
		if(speed!=undefined) this.speed=speed;
		this.element.style.display='block';
		this.maxh =this.height(this.element);
		return this;
	};
	Forms.Accordion.prototype={
		titleElement:null,
		element:null,
		speed:5,
		refresh:10,
		maxh:'60',
		timer:null,
		display:function(element,value){
			if(value==undefined){
				return element.style.display;
			}else{
				element.style.display=value;
			}
		},
		height:function(element,value){
			if(value==undefined){
				if(this.display(element)!='none'&& this.display(element)!=''){
					return element.clientHeight;
				}
				viz = element.style.visibility;
				element.style.visibility = 'hidden';
				o = this.display(element);
				this.display(element,'block');
				this.element.style.height='auto';
				r = parseInt(element.clientHeight);
				this.display(element,o);
				element.style.visibility = viz;
				return r;
			}else{
				element.style.height=value;
			}
		},
		collapseTimer:function(d){
			if(this.height(d)>0){
				var v = Math.round(this.height(d)/this.speed);
				if(v<1) {v=1;}
				v = (this.height(d)-v);
				this.height(d,v+'px');
				d.style.opacity = (v/this.maxh);
				if(d.style.filter){
					d.style.filter= 'alpha(opacity='+(v*100/this.maxh)+');';}
			}else{
				this.height(d,0);
				this.display(d,'none');
				clearInterval(this.timer);
			}
		},
		expandTimer:function(d){
			if(this.height(d)<this.maxh){
				var v = Math.round((this.maxh-this.height(d))/this.speed);
				if(v<1) {v=1;}
				v = (this.height(d)+v);
				this.height(d,v+'px');
				d.style.opacity = (v/this.maxh);
				if(d.style.filter){
					d.style.filter= 'alpha(opacity='+(v*100/this.maxh)+');';}
			}else{
				this.height(d,this.maxh);
				clearInterval(this.timer);
			}
		},
		collapse:function(){
			var d=this.element;
			var self=this;
			if(this.display(d)=='block'){
				clearInterval(this.timer);
				this.timer=setInterval(function(){self.collapseTimer(d);},this.refresh);
			}
		},
		expand: function(){
			var d=this.element;
			this.maxh =this.height(this.element);
			var self=this;
			if(this.display(d)=='none'){
				this.display(d,'block');
				d.style.height='0px';
				clearInterval(this.timer);
				this.timer=setInterval(function(){self.expandTimer(d);},this.refresh);
			}
		},
		attach:function(titleElement){
			var self=this;
			if(titleElement!=undefined){
				this.titleElement=titleElement;
				Forms.Utils.addEventToElement(titleElement, "click", function(){self.click();});
			}
		},
		click:function(){
			var d=this.element;
			if(this.display(d)=='none')
				this.expand();
			else
				this.collapse();
		}
			
	};
})();
(function(){
	Forms.List=function(id,allowSelectNone){
		this.id=id;
		if(allowSelectNone!=undefined)
			this.allowSelectNone=allowSelectNone;
		this.init();
	};
	Forms.List.prototype={
		id:"",
		height:"70px",
		searchElement:null,
		searchInitialText:"Rechercher...",
		allowSelectNone:true,
		ckListAjax:null,
		ckListAjaxLoader:null,
		ckList:null,
		ckListInner:null,
		ckListInfo:null,
		originalValue:"",
		timer:null,
		accordion:null,
		init:function(){
			var self=this;
			//Forms.Utils.addEventToElement($("ckListCaption-"+this.id), "click", function(){self.show();});
			//Forms.Utils.addEventToElement($("ckListCaption-"+this.id), "dblclick", function(){self.toogle();});
			if($("ckListSearchText-"+this.id)!=null){
				this.searchElement=$("ckListSearchText-"+this.id);
				Forms.Utils.addEventToElement(this.searchElement, "keyup", function(){self.searchFor();});
				Forms.Utils.addEventToElement(this.searchElement, "blur", function(){self.onTextBlur();});
				Forms.Utils.addEventToElement(this.searchElement, "focus", function(){self.onTextFocus();});
				Forms.Utils.addEventToElement(this.searchElement, "click", function(event){event.cancelBubble=true;if (event.stopPropagation) event.stopPropagation();});
			}
			this.ckListAjax=$("ckList-ajax-"+this.id);
			this.ckListAjaxLoader=$("ckList-ajax-loader-"+this.id);
			this.ckList=$("ckList-"+this.id);
			this.ckListInner=$("ckList-inner-"+this.id);
			this.ckListInfo=$("ckListInfo-"+this.id);
			this.originalValue=$$(this.id);
			$(this.id).ckList=this;
			$(this.id).eventClick=function(){self.show();};
			Forms.Utils.addEventToElement($("ckListCaption-"+this.id), "click", $(this.id).eventClick);
			this.accordion=new Forms.Accordion(this.ckListInner, 5, 24);
		},
		limitSizeTo:function(height){
			this.height=height;
			var self=this;
			Forms.Utils.removeEventFromElement($("ckListCaption-"+this.id), "click", $(this.id).eventClick);
			//Forms.Utils.addEventToElement($("ckListCaption-"+this.id), "click", function(){self.toogle();});
			this.toogle();
		},
		show:function(ckElement){
			var visible=this.ckListInner.style.display!="none";
			//Forms.Utils.show(this.ckListInner.id, !visible);
			if(!visible)
				this.accordion.expand();
			else
				this.accordion.collapse();
			this.stopTimer();
		},
		runTimer:function(){
			var visible=this.ckListInner.style.display!="none";
			if(!visible && this.timer==null){
				var sel=this;
				var showUpdateInList=function(){
					if(visible)
						sel.stopTimer();
					else{
						var caption=$("ckListCaption-"+sel.id);
						caption.style.fontWeight=(caption.style.fontWeight=='bold')?'normal':'bold';
					}
				};
				this.timer=setInterval(showUpdateInList, 1000);
			}
		},
		stopTimer:function(){
			var visible=this.ckListInner.style.display!="none";
			if(visible && this.timer!=null){
				clearInterval(this.timer);
				this.timer=null;
				$("ckListCaption-"+this.id).style.fontWeight='normal';
			}
		},
		toogle:function(ckElement){
			var allVisible=this.ckListInner.style.overflow!="auto";
			if(!allVisible){
				Forms.Utils.show(this.ckListInner.id, true);
				this.ckListInner.style.overflow="hidden";
				this.height=this.ckListInner.style.height;
				this.ckListInner.style.height="auto";
			}else{
				this.ckListInner.style.overflow="auto";
				this.ckListInner.style.maxHeight=this.height;				
			}
			this.stopTimer();
		},
		onTextBlur:function(){
			if(this.searchElement!=null)
				if(this.searchElement.value=="")
					this.searchElement.value=this.searchInitialText;
		},
		onTextFocus:function(){
			if(this.searchElement!=null)
				if(this.searchElement.value==this.searchInitialText)
					this.searchElement.value="";
		},
		isSelected:function(element){
			return element.className.indexOf("selected")!=-1;
		},
		getElements:function(){
			var elements;
			elements=this.ckList.getElementsByTagName("div");
			elements=Array.prototype.slice.call(elements);
			if(this.ckListAjax!=null){
				elements=elements.concat(Array.prototype.slice.call(this.ckListAjax.getElementsByTagName("div")));
			}
			return elements;
		},
		getElementValue:function(element){
			return element.id.replace("ckItem-"+this.id+"-","");
		},
		isElementInOriginalSelection:function(element){
			var eValue=this.getElementValue(element);
			return (";"+this.originalValue+";").indexOf(";"+eValue+";")!=-1;
		},
		searchFor:function(){
			var searchText="";
			var okToSearch=false;
			if(this.searchElement!=null){
				searchText=this.searchElement.value;
				if(searchText.length>1 || searchText=="")
					okToSearch=true;
			}
			var self=this;
			var searchFunction=function(){
				if(okToSearch){
						if(self.ckListAjax!=null)
							self.addSelect(self.ckListAjax.id);
						searchText=searchText.replace(new RegExp("([(.*)])","g"),"\\$1");
						var regExp=new RegExp("("+searchText+")","gi");
						var elements=self.getElements();
						for(var i=0;i<elements.length;i++){
							var innerText=Forms.Utils.innerText(elements[i]);
							if(regExp.test(innerText) || self.isSelected(elements[i])){
									elements[i].innerHTML=innerText.replace(regExp,"<span class='ckListFindedText'>$1<\/span>");
								Forms.Utils.show(elements[i].id, true);
								if(innerText==searchText){
									allowSelectNone=this.allowSelectNone;
									self.allowSelectNone=false;
									Forms.Utils.click(elements[i]);
									self.allowSelectNone=allowSelectNone;
								}
							}
							else{
								Forms.Utils.show(elements[i].id, (elements[i].parentNode==self.ckListAjax) || self.isElementInOriginalSelection(elements[i]));
								elements[i].innerHTML=innerText;
							}
						}
						if(elements.length>0)
							self.runTimer();
						if(self.ckListInner.style.overflow!="auto") self.ckListInner.style.height="auto";
					}
			};
			if(okToSearch){
				if(this.ckListAjax!=null){
					var params="_oValue="+this.originalValue+"&_cls="+$$("ckList-ajax-cls-"+this.id)+"&_clsList="+$$("ckList-ajax-clsList-"+this.id)+"&_field="+this.id+"&_value="+$$(this.id)+"&_search="+this.searchElement.value;
					var ajxRequest=new Forms.Ajax(this.ckListAjax.id, "d.KAjaxListContent", params, searchFunction, this.ckListAjaxLoader);
					ajxRequest.get();
				}else
					searchFunction();
			}
		}
	};
})();
(function(){
	Forms.CheckedList=function(id,allowSelectNone){
		Class.create(Forms.CheckedList, Forms.List);
		Forms.List.call(this,id,allowSelectNone);
		var self=this;
		Forms.Utils.addEventToElement($("ckListCaption-checkall-"+this.id), "click", function(){self.checkAll();});
		this.addSelect("ckList-"+this.id);
	};
	Forms.CheckedList.prototype={
		allChecked:false,
		checkAll:function(){
			var elements=this.getElements();
			this.allChecked=!this.allChecked;
			for(var i=0;i<elements.length;i++){
				if(this.isSelected(elements[i])!=this.allChecked){
					Forms.Utils.click(elements[i]);
				}
			}
			if(!this.allChecked)
				$(this.id).value="";
		},
		getSelSize:function(){
			var elements=this.getElements();
			var size=0;
			for(var i=0;i<elements.length;i++){
				if(elements[i].className.indexOf("selected")!=-1)
					size++;
			}
			return size;
		},
		addSelect:function(containerId){
			var element=$(this.id);
			var sel=this;
			this.updateInfo();
			var onclick=function(){
				var newValue=this.id.replace("ckItem-"+element.id+"-","");
				var value=element.value.replace(newValue,"");
				value=value.replace(/;;/g,";");
				if(this.className.indexOf("selected")!=-1){
					this.className="ckItem";
					element.value=value;
				}else{
					this.className="ckItem-selected";
					element.value=value+";"+newValue;
				}
				sel.updateInfo();
			};
			Forms.Utils.addEvent(containerId, "div", "click", onclick, true);
		},
		updateInfo:function(){
			if(this.ckListInfo!=null){
				this.ckListInfo.innerHTML="("+this.getSelSize()+")";
			}
			Forms.Utils.fireEvent($(this.id), "change");
		}
	};
})();
(function(){
	Forms.RadioList=function(id,allowSelectNone){
		Class.create(Forms.RadioList, Forms.List);
		Forms.List.call(this,id,allowSelectNone);
		this.addSelect("ckList-"+this.id);
		if(this.allowSelectNone==false){
			elem=this.getElements()[0];
			if(elem!=null&&$$(this.id)==="")
				Forms.Utils.click(elem);
		}
	};
	Forms.RadioList.prototype={
		addSelect:function(containerId){
			var element=$(this.id);
			var self=this;
			this.updateInfo();
			var onclick=function(){
				var newValue=this.id.replace("ckItem-"+element.id+"-","");
				var oldElementSelected=$("ckItem-"+element.id+"-"+element.value);
				if(oldElementSelected!=null)
						oldElementSelected.className="rItem";
				if(newValue!=element.value){
					this.className="rItem-selected";
					element.value=newValue;
					Forms.Utils.fireEvent(element,'change');
				}else{
					if(self.allowSelectNone===true){
						this.className="rItem";
						element.value="";
					}
					else
						this.className="rItem-selected";
				}
				if(self.searchElement!=null)
					self.searchElement.value=self.getValueText();
				self.updateInfo();
				self.searchInitialText=self.getValueText();
			};
			
			Forms.Utils.addEvent(containerId, "div", "click", onclick, true);
		},
		updateInfo:function(){
			if(this.ckListInfo!=null)
				this.ckListInfo.innerHTML=this.getValueText();	
			Forms.Utils.fireEvent($(this.id), "change");
		},
		getValueText:function(){
			var selElement=$("ckItem-"+this.id+"-"+$$(this.id));
			if(selElement!=null)
				return Forms.Utils.innerText(selElement);
			else
				return "";
		}
	};
})();
(function(){
	Forms.Private={
			drag_start:function(event) {
				event.dataTransfer.effectAllowed='move';
				event.dataTransfer.setData("text/html",event.target.getAttribute('id'));
			    pos=Forms.Private.getPosition(event.target);
			    mousePos=Forms.Private.getMousePos(event);
			    event.dataTransfer.setData("text/plain",
			    		(mousePos.x-pos.x) + ',' + (mousePos.y-pos.y));
			    //event.dataTransfer.setData("text/plain",
			    //(parseInt(style.getPropertyValue("left"),10) - event.clientX) + ',' + (parseInt(style.getPropertyValue("top"),10) - event.clientY));
			},
			drag_over: function(event) { 
			    event.preventDefault(); 
			    return false; 
			}, 
			drop: function(event) { 
			    var offset = event.dataTransfer.getData("text/plain").split(',');
			    var dm = $(event.dataTransfer.getData("text/html"));
			    mousePos=Forms.Private.getMousePos(event);
			    newx=(mousePos.x - offset[0]);
			    newy=(mousePos.y - offset[1]);			    
			    if(newx>0 && newy>0){
			    	dm.style.left = newx + 'px';
			    	dm.style.top = newy + 'px';
			    	dm.style.marginLeft='0px';
			    }
			    event.preventDefault();
			    return false;
			},
			handleDrop:function(event) {
				  if (event.stopPropagation) {
				    event.stopPropagation();
				  }
				  return false;
				},
			getPosition:function(div){
			        var left = 0;
			        var top  = 0;
			        while (div.offsetParent && div.style.position != 'absolute'){
			                left += div.offsetLeft;
			                top  += div.offsetTop;
			                div     = div.offsetParent;
			        }
			        left += div.offsetLeft;
			        top  += div.offsetTop;
			        return {x:left, y:top};
			},
			getMousePos:function(pos){
				if(pos.pageX || pos.pageY){
	                return {x:pos.pageX, y:pos.pageY};
				}
				return {
	                x:pos.clientX + document.body.scrollLeft - document.body.clientLeft,
	                y:pos.clientY + document.body.scrollTop  - document.body.clientTop
				};

			},
			submitForm:function(caption,obj){
				obj.submit(caption);
			}
			
	};
	var self=Forms.Private;
}());
(function(){
	Forms.Controls={
			addCloseButton:function(parentElement,idButton){
				if($(idButton)==null){
					var divClose=document.createElement("div");
					divClose.id=idButton;
					divClose.title="Fermeture";
					divClose.className="closeButton";
					parentElement.appendChild(divClose);
					Forms.Utils.addEventToElement(divClose, "click", function(){Forms.Utils.show(parentElement.id, false);}, false);
				}
			}	
	};
})();
(function(){
	Forms.Utils={
			executeFunctionByName:function (functionName, context, args ) {
				  var args = Array.prototype.slice.call(arguments).splice(2);
				  var namespaces = functionName.split(".");
				  var func = namespaces.pop();
				  for(var i = 0; i < namespaces.length; i++) {
				    context = context[namespaces[i]];
				  }
				  return context[func].apply(this, args);
				},
			addDrag: function(dragged){
				dragged.draggable=true;
				dragged.addEventListener('dragstart',Forms.Private.drag_start,false); 
				document.body.addEventListener('dragover',Forms.Private.drag_over,false); 
				document.body.addEventListener('drop',Forms.Private.drop,false); 
				document.body.addEventListener('handledrop',Forms.Private.handleDrop,false);
			},
			displays: new Array(),
			setDisplay:function(element,display){
				this.displays[element.id]=element.style.display;
				element.style.display=display;
			},
			restoreDisplay:function(element){
				if(this.displays[element.id]!=undefined)
					element.style.display=this.displays[element.id];
				else
					element.style.display="";	
			},
			ajxlightForm: function(id,title,urlRequest,urlResponse,idResponse,params,onload){
				var lb=new Forms.LightForm(id,title,urlRequest,urlResponse,idResponse,params,onload);
				return lb;
			},
			ajxlightBox: function(id,title,url,params,onload){
				return new Forms.AjaxLightBox(id,title,url,params,onload); 
			},
			lightBox: function(id,title,content){
				lb=new Forms.LightBox(id);
				lb.title=title;
				lb.content=content;
				lb.setAllContent();
				return lb;
			},
			showMessage: function(title,content){
				var sm=new Forms.MessageDialog("sm", title, content,null);
				sm.addButton("Okay",13);
				sm.show();
				return sm;
			},
			insertAfter:function(newElement,targetElement){
				var parent = targetElement.parentNode;
			 	if(parent.lastchild == targetElement) {
					parent.appendChild(newElement);
				} else {
					parent.insertBefore(newElement, targetElement.nextSibling);
				}
			},
			insertIn:function(newElementId,targetElement){
				newElement=$(newElementId);
				if(newElement==null){
					newElement=document.createElement("div");
					newElement.id=newElementId;					
					targetElement.appendChild(newElement);
				}
				return newElement;
			},
			toogle:function(img,elementId){
				var element=$(elementId);
				var reg=/^(.*)(hide.)(.*)$/i;
				var isVisible=reg.test(img.src);
				if(!isVisible){
					show(elementId,true);
					img.src=img.src.replace(/^(.*)(show.)(.*)$/i,"$1hide.$3");
				}else
				{
					show(elementId,false);
					img.src=img.src.replace(/^(.*)(hide.)(.*)$/i,"$1show.$3");					
				}
				return !isVisible;
	
			},
			toogleText:function(text,elementId){
				var isVisible=(text.innerHTML=="↑");
				if(!isVisible){
					show(elementId,true);
					text.innerHTML="↑";
				}else{
					show(elementId,false);
					text.innerHTML="↓";
				}
				return !isVisible;
			},
			getParent:function(element,tagName){
				if(typeof element ==="string")
					element=$(element);
				if (element!=null){
					if(tagName==undefined)
						return element.parentNode;
					else{
						p=element.parentNode;
						if(p.tagName.toLowerCase()==tagName)
							return p;
						else{
							if(p!=document) return Forms.Utils.getParent(p,tagName);	
						}
					}
				}
			},			
			getFileContent:function (url) {
			       var Xhr=self.getXmlHttpRequest();
			       Xhr.open("GET",url,false);
			       Xhr.send(null);
			       return Xhr.responseText;
			    },
		    getXmlHttpRequest:function () {
		    	var GetXmlHttpRequest_AXO=null;
			    if (window.XMLHttpRequest) {
				    return new XMLHttpRequest();
			    }
			    else if (window.ActiveXObject) {
				    if (!GetXmlHttpRequest_AXO) {
					    GetXmlHttpRequest_AXO=pickRecentProgID(["Msxml2.XMLHTTP.5.0", "Msxml2.XMLHTTP.4.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "Microsoft.XMLHTTP"]);
				    }
				    return new ActiveXObject(GetXmlHttpRequest_AXO);
			    }
			    return false;
		    },			    
			schowDiv:function(element,method){
				if (method && method.toLowerCase() == 'show' )
					this.restoreDisplay(element);
				else
					this.setDisplay(element,"none");
			} ,
			show:function(id,method){
				obj=$(id);
				if(obj!=undefined){
					if (method==true)
						obj.style.display="";
					else
						obj.style.display="none";
				}
			},
			$:function(elementId){
				if(elementId!==undefined&&""!==elementId){
					if(elementId.toLowerCase()=="body")
						return document.body;
					else
						return document.getElementById(elementId);
				}else
					return "";
			},
			$$:function(elementId){
				element=$(elementId);
				if(element!=undefined){
					if(element.type!=undefined){
						var type=element.type.toLowerCase();
						switch (type) {
						case 'text': case 'hidden': case 'file' : case 'select-one' : case 'select-multiple' :
							return element.value;
							break;
						case 'checkbox':
							return element.checked;
							break;
						default:
							return element.innerHTML;
							break;
						}
					}
					else
						return  element.innerHTML;
				}
				else
					return "";
			},
			thisQueryString:function(obj){
				var qs="";
				var sep="";
				if(Object.isString(obj))
					obj=$(obj);
				if(obj.id!==""){
					value=$$(obj.id);
					qs="request.id="+obj.id;
					sep="&";
					//if(obj.name&&obj.name!=null){
					//	qs+=sep+"request.name="+obj.name;
					//}
					//if(value)
						//qs+=sep+"request.value="+encodeURI(value);
				}
				return qs;
					
			},
			addEvent:function(container_id,tagName,event,func,before){
				if(document.getElementById(container_id)!==null){
					var events=event.split(',');
					var objects = document.getElementById(container_id).getElementsByTagName(tagName);
				    for ( var i = 0; i < objects.length; i++ ) {
				    	for ( var j=0; j < events.length; j++)
				    		Forms.Utils.addEventToElement(objects[i],events[j],func,before);   	
				    }
				}
			},
			addEventToElements:function(elementName,event,func,before){
				var objects=document.getElementsByName(elementName);
				for ( var i = 0; i < objects.length; i++ ) {
					Forms.Utils.addEventToElement(objects[i],event,func,before);
				}
			},
			addEventToElement:function(element,event,func,before){
				if(element!=null){
					if(typeof(func)=='string')
						func=eval(func);
					if(event.substring(0, 2)=='on')
						event=event.substring(2,event.length);
					if(element.addEventListener){
						element.addEventListener(event,func,false);}
					else if(element.attachEvent){
						element.attachEvent('on'+event,func);}
					else{
						event='on'+event;
						if(typeof element[event]=='function'){
							var oldListener=objectRef[event];
							if(before==true){
								element[event] = function(){
									func();
									return oldListener();
								};							
							}else{
								element[event] = function(){
									oldListener();
									return func();
								};
							}
						}else{
							element[event] = func;}
					}
					   if(element.eventListeners==undefined){  element.eventListeners = new Array();};
					   element.eventListeners.push({event : event, func : func , before : before});
				}
			},
			removeEventFromElement:function(element,event,func){
				if(element!=null){
					if(typeof(func)=='string')
						func=eval(func);
					if(event.substring(0, 2)=='on')
						event=event.substring(2,event.length);
					if(element.removeEventListener){
						element.removeEventListener(event,func,false);}
					else if(element.detachEvent){
						element.detachEvent('on'+event,func);}
					else{
						event='on'+event;
						if(typeof element[event]=='function'){
							element[event]='';
						}
					}
				}
			},
			serialize: function(aform,isArray,fromId) {
				if(Object.isString(aform))
					aform=$(aform);
			  var arrayS="";
			  if(isArray)
				  arrayS="[]";
			  if (!aform || !aform.elements) return;
			
			  var serial = [], i, j, first;
			  var add = function (name, value) {
			    serial.push(encodeURIComponent(name)+arrayS+'='+encodeURIComponent(value));
			  };
			  var getElement=function(element,fromId){
				if (fromId)
					result=element.id;
				else
					result=element.name;
				return result;
			  };
			
			  var elems = aform.elements;
			  for (i = 0; i < elems.length; i += 1, first = false) {
				  elementId=getElement(elems[i],fromId);
			  	if(elementId!=undefined){
				    if (elementId.length > 0) {
				      switch (elems[i].type) {
				        case 'select-one': first = true;
				        case 'select-multiple':
				          for (j = 0; j < elems[i].options.length; j += 1)
				            if (elems[i].options[j].selected) {
				              add(elementId, elems[i].options[j].value);
				              if (first) break;
				            }
				          break;
				        case 'checkbox':
				        case 'radio': if (!elems[i].checked) break;
				        default: if (elems[i].value!='') add(elementId, elems[i].value); break;
				      }
				    }
			  	}
			  }
			
			  return serial.join('&');
			},
			getQueryString:function () {
  				var result = {}, queryString = location.search.substring(1), re = /([^&=]+)=([^&]*)/g, m;
				while (m = re.exec(queryString)) {
    			result[decodeURIComponent(m[1])] = decodeURIComponent(m[2]);
  				}
				return result;
			},			
			setInnerHTML:function(divContent, HTML) {
				if(Object.isString(divContent))
					divContent=$(divContent);
				if(divContent!=null){
					if(divContent.type!=undefined){
						var type=divContent.type.toLowerCase();
						switch (type) {
						case 'text': case 'hidden': case 'file' : case 'select-one' : case 'select-multiple' :
							divContent.value=HTML;
						default:
							divContent.innerHTML=HTML;
							break;
						}
					}else
						divContent.innerHTML=HTML;
					
					var All=divContent.getElementsByTagName("*");
					for (var i=0; i<All.length; i++) {
						All[i].id=All[i].getAttribute("id");
						All[i].name=All[i].getAttribute("name");
						All[i].className=All[i].getAttribute("class");
					}
					var AllScripts=divContent.getElementsByTagName("script");
					for (var i=0; i<AllScripts.length; i++) {
						var s=AllScripts[i];
						if (s.src && s.src!="")
							eval(self.getFileContent(s.src));
						else
							eval(s.innerHTML);
					}
				}
			},
			innerText:function(element){
				var result="";
				if(document.all)
					result=element.innerText;
				else
					result=element.textContent;
				return result;
			},
			setStyles:function (element, styles)
			{
			    for(var s in styles) {
			        try{element.style[s] = styles[s];}
			        catch(e){}
			    }
			},
			setClassName:function(className,containerId,tagName){
				var element=$(containerId);
				if(element!=null){
					var elements;
					if(tagName!=undefined)
						elements=element.getElementsByTagName(tagName);
					else
						elements=element.getElementsByTagName('*');
					for(var i=0;i<elements.length;i++)
						elements[i].className=className;
				}
				
			},
			addClassName:function(className,containerId,tagName){
				var element=$(containerId);
				if(element!=null){
					var elements;
					if(tagName!=undefined)
						elements=element.getElementsByTagName(tagName);
					else
						elements=element.getElementsByTagName('*');
					for(var i=0;i<elements.length;i++)
						elements[i].className=elements[i].className +" "+className;
				}
				
			},
			removeClassName:function(className,containerId,tagName){
				var element=$(containerId);
				if(element!=null){
					var elements;
					if(tagName!=undefined)
						elements=element.getElementsByTagName(tagName);
					else
						elements=element.getElementsByTagName('*');
					for(var i=0;i<elements.length;i++)
						elements[i].className=elements[i].className.replace(new RegExp("(\s*)"+className+"(\s*)"),"");
				}
				
			},
			clearStyles:function (element, styles){
			    for(var s in styles) {
			        try{element.style[s] = "";}
			        catch(e){}
			    }
			},
			getElementInForm:function(formId,elementId){
				var ret;
				form=$(formId);
				if(form!=undefined)
					if(form.tagName.toLowerCase()=='form'){
						for(var i=0;i<form.elements.length;i++){
							if(form.elements[i].id==elementId){
								ret=form.elements[i];
								break;
							}
						}
					}
				return ret;
			},
			click:function(element){
				if(element!=null){
					try {element.click();}
					catch(e) {
						var evt = document.createEvent("MouseEvents");
						evt.initMouseEvent("click",true,true,window,0,0,0,0,0,false,false,false,false,0,null);
						element.dispatchEvent(evt);
					}
				}
			},
			fireEvent:function(element,event){
				if(event.substring(0, 2)=='on')
					event=event.substring(2);
			    if (document.createEventObject){
			        var evt = document.createEventObject();
			        return element.fireEvent('on'+event,evt);
			     }
			     else{
			        var evt = document.createEvent("HTMLEvents");
			        evt.initEvent(event, true, true );
			        return !element.dispatchEvent(evt);
			     }
			},
			execOnKeyUp:function(e,key,execFunc){
				var keycode=null;
				if (window.event) keycode = window.event.keyCode;
					else if (e) keycode = e.which;
				if(keycode==key){
					void(0);
					execFunc(this);
					return false;
				}	
			},
			onKeyUpFireEvent:function(e,key,fireEvent,element){
				Forms.Utils.execOnKeyUp(e, key, function(){Forms.Utils.fireEvent(element, fireEvent);});
			},
			postOnKeyUp:function(e,key,id,url,params){
				var execFunction=function(){
					var ajx=new Forms.Ajax(id, url, params);
					ajx.post();
				};
				Forms.Utils.execOnKeyUp(e, key, execFunction);
			},
			getOnKeyUp:function(e,key,id,url,params){
				var execFunction=function(){
					var ajx=new Forms.Ajax(id, url, params);
					ajx.post();
				};
				Forms.Utils.execOnKeyUp(e, key, execFunction);
			},
			get:function(id,url,params,func){
				var ajx=new Forms.Ajax(id, url, params,func);
				ajx.get();				
			},
			post:function(id,url,params,func){
				var ajx=new Forms.Ajax(id, url, params,func);
				ajx.post();				
			},
			getForm:function(formName,id,url,params,func){
				var ajx=new Forms.Ajax(id, url, params,func);
				ajx.getForm(formName);				
			},
			postForm:function(formName,id,url,params,func){
				var ajx=new Forms.Ajax(id, url, params,func);
				ajx.postForm(formName);				
			},
			onblurText:function(element,text){
				if(element!=null){
					element.onblurText=text;
					element.eType=element.type;
					var _element=element;
					Forms.Utils.addEventToElement(element, "blur", function(){if(_element.value=="") {_element.value=_element.onblurText;_element.type="text";}}, true);
					Forms.Utils.addEventToElement(element, "focus", function(){_element.type=_element.eType;if(_element.value==_element.onblurText) _element.value="";}, true);
				};
			},
			getIdNum:function(obj){
				var result="0";
				if(obj!=null){
					pos=obj.id.lastIndexOf("-");
					if(pos!=-1)
						result=obj.id.substring(pos+1);
				}
				return result;
			}
	};
	var self = Forms.Utils;	
})();
(function(){
	Forms.DatePicker=function(inputId, dtpId,dtpType,alwaysVisible){
		this.init(inputId,dtpId,dtpType,alwaysVisible);
	};
	Forms.DatePicker.prototype={
			inputId:"",
			dtpId:"",
			dtpType:"date",
			hSelect:false,
			mSelect:false,
			isDate:false,
			isTime:false,
			date:"",
			hours:"",
			minutes:"",
			dateFormat:"dd/MM/yyyy",
			timeFormat:"HH:mm",
			valueFormat:"",
			noHide:false,
			alwaysVisible:true,
			init:function(id,idDtp,dtpType,alwaysVisible){
				if(alwaysVisible!=undefined)
					this.alwaysVisible=alwaysVisible;
				this.inputId=id;
				this.dtpId=idDtp;
				this.dtpType=dtpType;
				this.nbclick=0;
				var sep="";
				if(dtpType.indexOf("date")!=-1){
					this.valueFormat=this.dateFormat;
					sep=" ";
					this.isDate=true;
				}
				if(dtpType.indexOf("time")!=-1){
					this.valueFormat+=sep+this.timeFormat;
					this.isTime=true;
				}
				if($(idDtp)==undefined){
					var idDtpElement=document.createElement("div");
					idDtpElement.id=idDtp;
					idDtpElement.className='dtp';
					Forms.Utils.insertAfter(idDtpElement, $(id));
					idDtpElement.style.display="none";
				}
				var idi=id;
				var self=this;
				var maj=function(){
					if(self.noHide!=true){
					$(idi).focused=true;self.updateDtp();self.nbclick=0;
					}
				};
				Forms.Utils.addEventToElement($(id),"onfocus",maj);
				Forms.Utils.addEventToElement($(id),"onkeyup",maj);
				Forms.Utils.addEventToElement($(id),"onchange",maj);
				$(id).dtp=this;
				if(this.alwaysVisible===false)
					Forms.Utils.addEventToElement($(id),"onblur",function(){if(self.noHide!=true) self.hide();});
				else
					maj();
			},
			requestDtp:function(d){
				this.noHide=true;
				this.setDtp();
				var self=this;
				var func=function(){
					self.show();
					$(self.inputId).focus();
					self.noHide=false;
					$(self.inputId).focused=true;
				};
				var ajx=new Forms.Ajax(this.dtpId, "d.KDatePicker", "type="+this.dtpType+"&sd="+$$(this.inputId)+"&d="+d+"&id="+this.dtpId+"&inputId="+this.inputId, func);
				ajx.get();
				
			},
			setDtp:function(){
				var oId=$(this.inputId);
				var oIdDtp=$(this.dtpId);
				if(oId!=null && oIdDtp!=null){
					oIdDtp.style.left=oId.style.left;
					oIdDtp.style.top=parseInt(oId.style.top)+parseInt(oId.style.height)+" px";
				}
			},
			updateDtp:function(){
				if($(this.inputId).focused==true){
					this.setDtp();
					var self=this;
					var func=function(){self.show();};
					var ajx=new Forms.Ajax(this.dtpId, "d.KDatePicker", "type="+this.dtpType+"&d="+$$(this.inputId)+"&id="+this.dtpId+"&inputId="+this.inputId, func);
					ajx.get();
				}
				$(this.inputId).focused=true;
				this.noHide=false;
			},
			show:function(){
				Forms.Utils.show(this.dtpId,true);
			},
			hide:function(){
				Forms.Utils.show(this.dtpId,false);
			},
			deselect:function(valueType){
				var container=$("dtp-"+this.dtpId+"-"+valueType);
				if(valueType==="d")
					container=$("dtp-"+this.dtpId);
				if(container!=null){
					var elements=container.getElementsByTagName("div");
					for(var i=0;i<elements.length;i++){
						if(elements[i].className=="dateActive")
							elements[i].className="day";
					}
				}
			},
			setInnerValue:function(d){
				$(this.inputId).value=Forms.Date.formatDate(d, this.valueFormat);
				if(this.alwaysVisible===false)
					this.hide();
				this.noHide=false;
			},
			setValue:function(value,type,obj){
				this.deselect(type);
				obj.className="dateActive";
				this.noHide=true;
				//var d=Forms.Date.getDateFromFormat($$(this.inputId), this.valueFormat);
				var d=Forms.Date.parseDate($$(this.inputId),true);
				try{this.date=Forms.Date.formatDate(d, this.dateFormat);}
				catch(ex){
					d=new Date();
					this.date=Forms.Date.formatDate(d, this.dateFormat);}
				
				if(type=="d"){
					this.date=Forms.Date.getDateFromFormat(value, this.dateFormat);
					d.setMonth(this.date.getMonth());
					d.setDate(this.date.getDate());
					d.setYear(this.date.getYear()+1900);
				}
				if(type=="h"){
					this.hours=value;
					d.setHours(value);
					if(this.minutes!="")
						d.setMinutes(this.minutes);
				}
				if(type=="m"){
					this.minutes=value;
					d.setMinutes(value);
					if(this.hours!="")
						d.setHours(this.hours);
				}
				
				if(this.isDate){
					if(this.isTime){
						if(this.date!=""&&this.hours!=""&&this.minutes!=""){
							this.setInnerValue(d);
						}
					}else{
						if(this.date!=""){
							this.setInnerValue(d);
						}
					}
				}else{
					if(this.hours!=""&&this.minutes!=""){
						this.setInnerValue(d);
					}
				}	
			}
	};
})();
(function(){
	Forms.Date={
			MONTH_NAMES:new Array('January','February','March','April','May','June','July','August','September','October','November','December','Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'),
			DAY_NAMES:new Array('Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sun','Mon','Tue','Wed','Thu','Fri','Sat'),
			LZ:function(x) {return(x<0||x>9?"":"0")+x;},
			_isInteger:function(val) {
				var digits="1234567890";
				for (var i=0; i < val.length; i++) {
					if (digits.indexOf(val.charAt(i))==-1) { return false; }
					}
				return true;
				},
			_getInt:function(str,i,minlength,maxlength) {
				for (var x=maxlength; x>=minlength; x--) {
					var token=str.substring(i,i+x);
					if (token.length < minlength) { return null; }
					if (Forms.Date._isInteger(token)) { return token; }
					}
				return null;
				},
			isDate:function(val,format) {
				var date=getDateFromFormat(val,format);
				if (date==0) { return false; }
				return true;
				},
			compareDates:function(date1,dateformat1,date2,dateformat2) {
				var d1=getDateFromFormat(date1,dateformat1);
				var d2=getDateFromFormat(date2,dateformat2);
				if (d1==0 || d2==0) {
					return -1;
					}
				else if (d1 > d2) {
					return 1;
					}
				return 0;
				},
			getDateFromFormat:function(val,format) {
				val=val+"";
				format=format+"";
				var i_val=0;
				var i_format=0;
				var c="";
				var token="";
				var token2="";
				var x,y;
				var now=new Date();
				var year=now.getYear();
				var month=now.getMonth()+1;
				var date=1;
				var hh=now.getHours();
				var mm=now.getMinutes();
				var ss=now.getSeconds();
				var ampm="";
				
				while (i_format < format.length) {
					c=format.charAt(i_format);
					token="";
					while ((format.charAt(i_format)==c) && (i_format < format.length)) {
						token += format.charAt(i_format++);
						}
					if (token=="yyyy" || token=="yy" || token=="y") {
						if (token=="yyyy") { x=4;y=4; }
						if (token=="yy")   { x=2;y=2; }
						if (token=="y")    { x=2;y=4; }
						year=Forms.Date._getInt(val,i_val,x,y);
						if (year==null) { return 0; }
						i_val += year.length;
						if (year.length==2) {
							if (year > 70) { year=1900+(year-0); }
							else { year=2000+(year-0); }
							}
						}
					else if (token=="MMM"||token=="NNN"){
						month=0;
						for (var i=0; i<Forms.Date.MONTH_NAMES.length; i++) {
							var month_name=Forms.Date.MONTH_NAMES[i];
							if (val.substring(i_val,i_val+month_name.length).toLowerCase()==month_name.toLowerCase()) {
								if (token=="MMM"||(token=="NNN"&&i>11)) {
									month=i+1;
									if (month>12) { month -= 12; }
									i_val += month_name.length;
									break;
									}
								}
							}
						if ((month < 1)||(month>12)){return 0;}
						}
					else if (token=="EE"||token=="E"){
						for (var i=0; i<Forms.Date.DAY_NAMES.length; i++) {
							var day_name=Forms.Date.DAY_NAMES[i];
							if (val.substring(i_val,i_val+day_name.length).toLowerCase()==day_name.toLowerCase()) {
								i_val += day_name.length;
								break;
								}
							}
						}
					else if (token=="MM"||token=="M") {
						month=Forms.Date._getInt(val,i_val,token.length,2);
						if(month==null||(month<1)||(month>12)){return 0;}
						i_val+=month.length;}
					else if (token=="dd"||token=="d") {
						date=Forms.Date._getInt(val,i_val,token.length,2);
						if(date==null||(date<1)||(date>31)){return 0;}
						i_val+=date.length;}
					else if (token=="hh"||token=="h") {
						hh=Forms.Date._getInt(val,i_val,token.length,2);
						if(hh==null||(hh<1)||(hh>12)){return 0;}
						i_val+=hh.length;}
					else if (token=="HH"||token=="H") {
						hh=Forms.Date._getInt(val,i_val,token.length,2);
						if(hh==null||(hh<0)||(hh>23)){return 0;}
						i_val+=hh.length;}
					else if (token=="KK"||token=="K") {
						hh=Forms.Date._getInt(val,i_val,token.length,2);
						if(hh==null||(hh<0)||(hh>11)){return 0;}
						i_val+=hh.length;}
					else if (token=="kk"||token=="k") {
						hh=Forms.Date._getInt(val,i_val,token.length,2);
						if(hh==null||(hh<1)||(hh>24)){return 0;}
						i_val+=hh.length;hh--;}
					else if (token=="mm"||token=="m") {
						mm=Forms.Date._getInt(val,i_val,token.length,2);
						if(mm==null||(mm<0)||(mm>59)){return 0;}
						i_val+=mm.length;}
					else if (token=="ss"||token=="s") {
						ss=Forms.Date._getInt(val,i_val,token.length,2);
						if(ss==null||(ss<0)||(ss>59)){return 0;}
						i_val+=ss.length;}
					else if (token=="a") {
						if (val.substring(i_val,i_val+2).toLowerCase()=="am") {ampm="AM";}
						else if (val.substring(i_val,i_val+2).toLowerCase()=="pm") {ampm="PM";}
						else {return 0;}
						i_val+=2;}
					else {
						if (val.substring(i_val,i_val+token.length)!=token) {return 0;}
						else {i_val+=token.length;}
						}
					}

				if (i_val != val.length) { return 0; }
				if (month==2) {
					if ( ( (year%4==0)&&(year%100 != 0) ) || (year%400==0) ) { 
						if (date > 29){ return 0; }
						}
					else { if (date > 28) { return 0; } }
					}
				if ((month==4)||(month==6)||(month==9)||(month==11)) {
					if (date > 30) { return 0; }
					}

				if (hh<12 && ampm=="PM") { hh=hh-0+12; }
				else if (hh>11 && ampm=="AM") { hh-=12; }
				var newdate=new Date(year,month-1,date,hh,mm,ss);
				return newdate;
				},
			formatDate:function(date,format) {
				format=format+"";
				var result="";
				var i_format=0;
				var c="";
				var token="";
				var y=date.getYear()+"";
				var M=date.getMonth()+1;
				var d=date.getDate();
				var E=date.getDay();
				var H=date.getHours();
				var m=date.getMinutes();
				var s=date.getSeconds();
				var yyyy,yy,MMM,MM,dd,hh,h,mm,ss,ampm,HH,H,KK,K,kk,k;
				var value=new Object();
				if (y.length < 4) {y=""+(y-0+1900);}
				value["y"]=""+y;
				value["yyyy"]=y;
				value["yy"]=y.substring(2,4);
				value["M"]=M;
				value["MM"]=Forms.Date.LZ(M);
				value["MMM"]=Forms.Date.MONTH_NAMES[M-1];
				value["NNN"]=Forms.Date.MONTH_NAMES[M+11];
				value["d"]=d;
				value["dd"]=Forms.Date.LZ(d);
				value["E"]=Forms.Date.DAY_NAMES[E+7];
				value["EE"]=Forms.Date.DAY_NAMES[E];
				value["H"]=H;
				value["HH"]=Forms.Date.LZ(H);
				if (H==0){value["h"]=12;}
				else if (H>12){value["h"]=H-12;}
				else {value["h"]=H;}
				value["hh"]=Forms.Date.LZ(value["h"]);
				if (H>11){value["K"]=H-12;} else {value["K"]=H;}
				value["k"]=H+1;
				value["KK"]=Forms.Date.LZ(value["K"]);
				value["kk"]=Forms.Date.LZ(value["k"]);
				if (H > 11) { value["a"]="PM"; }
				else { value["a"]="AM"; }
				value["m"]=m;
				value["mm"]=Forms.Date.LZ(m);
				value["s"]=s;
				value["ss"]=Forms.Date.LZ(s);
				while (i_format < format.length) {
					c=format.charAt(i_format);
					token="";
					while ((format.charAt(i_format)==c) && (i_format < format.length)) {
						token += format.charAt(i_format++);
						}
					if (value[token] != null) { result=result + value[token]; }
					else { result=result + token; }
					}
				return result;
				},
				parseDate:function(val) {
					var preferEuro=(arguments.length==2)?arguments[1]:false;
					generalFormats=new Array('y-M-d','MMM d, y','MMM d,y','y-MMM-d','d-MMM-y','MMM d');
					monthFirst=new Array('M/d/y','M-d-y','M.d.y','MMM-d','M/d','M-d');
					dateFirst =new Array('dd/MM/yyyy','dd/MM/yyyy HH:mm','d/M/y','d-M-y','d.M.y','d-MMM','d/M','d-M');
					var checkList=new Array('generalFormats',preferEuro?'dateFirst':'monthFirst',preferEuro?'monthFirst':'dateFirst');
					var d=null;
					for (var i=0; i<checkList.length; i++) {
						var l=window[checkList[i]];
						for (var j=0; j<l.length; j++) {
							d=Forms.Date.getDateFromFormat(val,l[j]);
							if (d!=0) { return new Date(d); }
							}
						}
					return null;
					}
			
	};
})();
(function(){
	Forms.AjaxLightBox=function(id, title, url, params,onload){
		Class.create(Forms.AjaxLightBox, Forms.LightBox);
		Forms.LightBox.call(this,id);
		this.initLB(title,url,params,onload);
	};
	Forms.AjaxLightBox.prototype={
			ajax:null,
			onLoad:null,
			initLB:function(title,url,params,onload){
				this.onLoad=onload;
				
				this.title=title;
				this.ajax=new Forms.Ajax('',url);
				this.ajax.params=params;
				var self=this;
				this.ajax.func=function(){
					self.content=self.ajax.content;
					self.show();
					if (onload!=null) {try{ onload();} catch(e){alert(e+' : '+onload);}}
				};
			},
			get:function(args){
				this.ajax.get(args);
			},
			setParams:function(params){
				this.ajax.params=params;
			}
	};
	
})();
(function(){
	Forms.LightForm=function(id,title,urlRequest,urlResponse,idResponse,params,validCaption,cancelCaption){
		if(validCaption!=undefined)
			this.validCaption=validCaption;
		if(cancelCaption!=undefined)
			this.cancelCaption=cancelCaption;
		this.initLB(id, title, urlRequest, params);
		this.idResponse=idResponse;
		this.urlResponse=urlResponse;
		
	};
	Forms.LightForm.prototype={
			ajxLightBox: null,
			validator: null,
			idResponse :'',
			urlResponse:'',
			validatorEvents:null,
			validatorElements:null,
			validatorFormId:'',
			useValidator:false,
			postFormFunction:function(){return true;},
			cancelFunction:null,
			postParams:'',
			frmName:0,
			validCaption:'Okay',
			cancelCaption:'Annuler',

			initLB:function(id, title, urlRequest, params){
				var onload=function(){
					data.createValidator();
				};
				var lb=new Forms.AjaxLightBox('ajx'+id, title, urlRequest, params,onload);
				lb.innerAddButton(this.validCaption,13);
				lb.innerAddButton(this.cancelCaption,27);
				var data=this;
				this.ajxLightBox=lb;
			},
			setOkay:function(validator,idResponse,urlResponse,postParams,postFormFunction,frmName){
				var respFunc=(function(){
					var valid=true;
					if(validator!=null)
							valid=validator.validate();
					if(valid){
						var ajaxResponse=new Forms.Ajax(idResponse,urlResponse,postParams,postFormFunction);
						ajaxResponse.postForm(document.forms[frmName]);
					}
					return valid;
				});
				this.ajxLightBox.waitFor(this.validCaption,respFunc);
				this.ajxLightBox.waitFor(this.cancelCaption,this.cancelFunction);	
			},
			
			addValidator:function(events,elements,id){
				this.validatorEvents=events;
				this.validatorElements=elements;
				this.validatorFormId=id;
				this.useValidator=true;
			},
			createValidator:function(){
				if(this.useValidator){
					this.validator=new Forms.Validator(this.validatorEvents,this.validatorElements,this.validatorFormId,true);
				}
				this.setOkay(this.validator,this.idResponse,this.urlResponse,this.postParams,this.postFormFunction,this.frmName);
			},
			show:function(){
				this.get();
			},
			get:function(){
				this.ajxLightBox.get();
			},
			submit:function(mr){
				return this.ajxLightBox.submit(mr);
			},
			setParams:function(params){
				this.ajxLightBox.setParams(params);
			},
			setModal:function(modal){
				this.ajxLightBox.modal=modal;
			},
			deleteAction:function(action){
				if(this.ajxLightBox!=null)
					this.ajxLightBox.deleteAction(action);
			},
			setBtnCaption:function(btn,newCaption){
				if(this.ajxLightBox!=null)
					this.ajxLightBox.setBtnCaption(btn,newCaption);
			}
	};
})();
(function(){
	Forms.Validator=function(events,elements,id,inForm){
		this.init();
		if(true!=inForm)
		this.controlOn(events, elements, id);
	};
	Forms.Validator.prototype={
		infoElementId:'',
		forms: new Array(),
		elementsToControl: new Array(),
		controlsOnElements: null,
		classNames: new Array(),
		message: new Array(),
		id: '',
		ER: new Array(),
		options:{"showErrorMessage":true,"showId":true,"onErrorFunction":null,"onControlFunction":null},
		init:function(){
			if(_message==null){
				this.message["@"]="Saisie obligatoire";
				this.message["min"]="Caractère(s) requi(s) au minimum";
				this.message["max"]="Caractère(s) requi(s) au maximum";
				this.message["num"]="Valeurs numériques uniquement";
				this.message["integer"]="Nombre entier uniquement";
				this.message["boolean"]="Booléen attendu";
				this.message["mail"]="Adresse mail non conforme"; 
				this.message["text"]="Caractères uniquement";
				this.message["color"]="Couleur non conforme";
				this.message["alpha"]="Caractères alphanumériques uniquement";
				this.message["date"]="Date au format jj/mm/aaaa requis";
				this.message["datet"]="Date et/ou heure au format jj/mm/aaaa hh:mn requis";
				this.message["datetime"]="Date et heure au format jj/mm/aaaa hh:mn requis";
				this.message["time"]="Heure au format hh:mn requis";
				this.message["url"]="Url commençant par le protocole";
				this.message["tel"]="Téléphone à 10 chiffres";
				this.message["path"]="Chemin invalide";
				this.message["variable"]="Variable invalide";
				this.message["file"]="Nom de fichier incorrect";
				this.message["compare"]="La confirmation de %d n'est pas valide";
			}
			else
				this.message=_message;
			
			if(_ER==null){
				this.ER["@"]=/^.+$/;
				this.ER["num"]=/^[+-]?[0-9]*[\.\,]?[0-9]*$/;
				this.ER["integer"]=/^[\+\-]?[0-9]*$/;
				this.ER["boolean"]=/^(0|1|true|false){1}$/i;
				this.ER["mail"]=/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/;
				this.ER["text"]=/^([a-z]|[A-Z])*$/;
				this.ER["color"]=/^#[0-9A-F]+$/;
				this.ER["date"]=/^[0-9]{2,4}(\/|-|\.)[0-9]{1,2}(\/|-|\.)[0-9]{2,4}$/;
				this.ER["time"]=/^[0-9]{1,2}(:)[0-9]{1,2}(:?)[0-9]{0,2}$/;
				this.ER["datet"]=/^[0-9]{2,4}(\/|-|\.)[0-9]{1,2}(\/|-|\.)[0-9]{2,4}([ ][0-9]{1,2}(:)[0-9]{1,2}(:?)[0-9]{0,2})?$/;
				this.ER["alpha"]=/^[0-9A-Za-z]+$/;
				this.ER["datetime"]=/^[0-9]{2,4}(\/|-|\.)[0-9]{1,2}(\/|-|\.)[0-9]{2,4}[ ][0-9]{1,2}(:)[0-9]{1,2}(:?)[0-9]{0,2}$/;
				this.ER["url"]=/(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
				this.ER["tel"]=/^\d\d(-|\.|\s){0,1}\d\d(-|\.|\s){0,1}\d\d(-|\.|\s){0,1}\d\d(-|\.|\s){0,1}\d\d(-|\.|\s){0,1}$/;
				this.ER["path"]=/^[a-z0-9\-_\/\.]+$/i;
				this.ER["variable"]=/^[a-z0-9\-_\.]+$/i;
				this.ER["file"]=/^[a-z0-9\-_\/\.]+$/i;	
				this.ER["string"]=/^.*$/;
			}else
				this.ER=_ER;
		},
		setOptions:function(options){
			this.options=options;
		},
		addOptions:function(options){
			for(var k in options)
				this.options[k]=options[k];
		},
		setOption:function(optionName,value){
			this.options[optionName]=value;
		},
		getOption:function(optionName){
			ret=false;
			if(this.options!=null && this.options[optionName]!=undefined)
				ret=this.options[optionName];
			return ret;
		},
		addControlMessage:function(id,message,ER){
			if(ER!=undefined && ER!="")
				this.ER[id]=ER;
			this.message[id]=message;
		},
		addControlMessages:function(messages){
			if(Object.isArray(messages)){
				for(var i=0;i<messages.length;i++)
					this.addJSONMessage(messages[i]);
			}else if (typeof(messages)=='object')
				this.addJSONMessage(messages);		
		},
		addJSONMessage:function(message){
			if(typeof(message)=='object'){
				idMb="";messageMb="";erMb="";
				for(var member in message){
					if(member.toLowerCase()=='id')
						idMb=member;
					if(member.toLowerCase()=='er')
						erMb=member;
					if(member.toLowerCase()=='message')
						messageMb=member;
					if(idMb!=""&&messageMb!=""){
						if(erMb!="")
							this.addControlMessage(message[idMb],message[messageMb],message[erMb]);
						else
							this.addControlMessage(message[idMb],message[messageMb]);	
					}											
				}
			}	
		},
		getControl: function(elementId){
			if(this.controlsOnElements!=null){
				if(this.controlsOnElements[elementId]!=undefined)
					return this.controlsOnElements[elementId];
			}
			if(document.getElementById(elementId)!=undefined)
				return document.getElementById(elementId).alt;
			else
				return "";
		},
		parseFromJSON: function(json){
			this.controlsOnElements=new Array();
			this.elementsToControl=new Array();
			for(var id in json){
				var element=document.getElementById(id);
				if(this.id!='')
					element=Forms.Utils.getElementInForm(this.id, id);
				if(document.getElementById(id)!=undefined){
					if(element!=undefined){
						this.elementsToControl.push(element);
						this.controlsOnElements[id]=json[id];
					}
				}
			}
		},
		addElementsToControl: function(elements){
			this.elementsToControl=new Array();
			var getAllElementsOfAllForms=function(forms){
				var ret=new Array();
				for(var i=0;i<forms.length;i++){
					for(var j=0;j<forms[i].elements.length;j++){
						ret.push(forms[i].elements[j]);
					}
				}
				return ret;
			};
			if(elements!=undefined){
				if(typeof(elements)=='string'){
					try{
						var json=JSON.parse(elements);
						this.parseFromJSON(json);
						}
					catch(ex){}
					
				}
				else if(elements.tagName && elements.tagName.toLowerCase()=='form')
					this.elementsToControl=elements.elements;
				else if(typeof(elements)=='object'){
					try{
							this.parseFromJSON(elements);
						}
					catch(ex){}				
				}else
					this.elementsToControl=getAllElementsOfAllForms(document.forms);
			}else
				this.elementsToControl=getAllElementsOfAllForms(document.forms);		
		},
		validate:function (){
			var valide=true;
			for(var i=0;i<this.elementsToControl.length;i++){
				try {
					if (!this.scontrol(this.elementsToControl[i])) {valide=false;}
				}
				catch(ex){}
			}
			if(!valide){
				if(this.getOption('onErrorFunction')!=null && typeof(this.getOption('onErrorFunction'))=='function'){
					try{
						this.getOption('onErrorFunction')(this);
					}
					catch(ex){}
				}				
			}
			return valide;
		},
		//--------------------------------------------------------------------	
		controlOn: function(events,elements,id){
			if(id!=undefined && id!=null)
				this.id=id;
			if(events=='')
					events=['onkeyup','onchange'];
			this.addElementsToControl(elements);
			var evts=events;
			if(!Object.isArray(events))
				evts=events.split(',');
			for(var i=0;i<evts.length;i++){
				this.controlOnOneEvent(evts[i]);
			}		
		},
		//--------------------------------------------------------------------	
		controlOnOneEvent: function(event){
			for(var i=0;i<this.elementsToControl.length;i++){
				var self=this;
				Forms.Utils.addEventToElement(this.elementsToControl[i],event,function () {self.scontrol(this);});
			}		
		},
		sControlOne:function(pMyObject,control){
			msg="";
			valeur=pMyObject.value;
			if(valeur!=undefined && control!="" && control!=undefined){
				if(this.iswhat(control,"integer")){
					valMinMax=Math.abs(control);
					if(control<0){
						if(pMyObject.value.length>valMinMax) 
							msg=" [" + valMinMax + " " + this.message["max"]+"]";
					}else{
						if(pMyObject.value.length<valMinMax) 
							msg=" [" + valMinMax + " " + this.message["min"]+"]";					
					}			
				}else if(control==="@"){
					if(!this.iswhat(valeur,"@")){
						if (typeof(this.message["@"])!="undefined")
							msg=" ["+ this.message["@"]+"]";
					}						
				}else if(control.search(/^\{(.+?)\}$/) != -1){
					control=control.replace("{","").replace("}","");
					ctrl=$(control);
					if(ctrl!=null){
						if(ctrl.value!=valeur)
							msg=" ["+ this.message["compare"].replace("%d",control)+"]";
					}
				}
				else{
					if(!this.iswhat(valeur,control)&&!(valeur.length==0)){
						if (typeof(this.message[control])!="undefined")
							msg=" ["+ this.message[control]+"]";
					}		
				}
			}
			return msg;
		},
	//--------------------------------------------------------------------
		scontrol:function(pMyObject){
			pMyObject = (document.all)?event.srcElement : pMyObject;
			var nom=this.getControl(pMyObject.id);
			var valeur=pMyObject.value;
			var caption=pMyObject.name;
			var pos0=0;
			var msg="";
			this.cleanmsg(pMyObject);
			if(nom!="" && nom!=undefined && typeof(nom)!="undefined"){
				noms=nom.split(".");
				msg="";
				for(var i=0;i<noms.length;i++)
					msg+=this.sControlOne(pMyObject,noms[i]);
			}
			var isInvalidControl=(msg!="" && typeof(msg)!="undefined" && msg!=undefined);
			if(this.getOption('showErrorMessage'))
				this.displayErrorMessage(pMyObject,msg,isInvalidControl);
			if(this.getOption('onControlFunction')!=null && typeof(this.getOption('onControlFunction'))=='function'){
				try{
					this.getOption('onControlFunction')(pMyObject,msg,isInvalidControl);
				}
				catch(ex){}
			}
			return !isInvalidControl;	
		},
		displayErrorMessage:function(pMyObject,msg,isInvalidControl){
			if (isInvalidControl)
				{
					if(pMyObject.className!=''&&pMyObject.className!='error'){
						this.classNames[pMyObject.id]=pMyObject.className;
					}
					showId="";
					if(this.getOption('showId'))
						showId=pMyObject.id+" :";
					pMyObject.className="error";
					this.errormsg(pMyObject,showId+" " + msg);
				}
				else
				{
					if(pMyObject.className=="error"){
						if(this.classNames[pMyObject.id]!=undefined){
							pMyObject.className=this.classNames[pMyObject.id];
							try{
							this.classNames.splice(this.classNames.indexOf(pMyObject.className), 1);}
							catch(ex){}
						}
						else
							pMyObject.className="";
					}
				}
				if($(this.getErrorId(pMyObject.id))!=undefined){
					show(this.getErrorId(pMyObject.id),isInvalidControl);
				}	
				return !isInvalidControl;	
		},
		//--------------------------------------------------------------------	
		iswhat:function(pval,pwhat){
			if (pval.search(this.ER[pwhat]) != -1)
				return true ;
			else
		        return false ;
		},
		
		//--------------------------------------------------------------------	
		errormsg: function(pObject,pmsg){
			idError=this.getErrorId(pObject.id);
			if($(idError)==undefined){
				var err=document.createElement("div");
				err.id=idError;
				err.className='errMessage';
				if(this.infoElementId!='' && $(this.infoElementId)!=undefined)
					$(this.infoElementId).appendChild(err);
				else
					Forms.Utils.insertAfter(err, pObject);
			}
			$(idError).innerHTML=pmsg;
		},

		//--------------------------------------------------------------------	
		cleanmsg: function(pObject){
			if($(this.getErrorId(pObject.id))!=undefined)
				$(this.getErrorId(pObject.id)).innerHTML='';
		},
		getErrorId:function(id){
			if(this.id=='')
				return 'error-'+id;
			else
				return 'error-'+this.id+'-'+id;
		}
	};
})();
(function(){ // début scope local
	//Forms.utils = Forms.utils || {};
// déclaration de la classe de Validation
	Forms.Validation = {
    // déclaration variables statiques
	forms: new Array(),
	elementsToControl: new Array(),
	controlsOnElements: null,
	classNames: new Array(),
	message: new Array(),
	id: '',
	ER: new Array(),
	options:{"showErrorMessage":true,"showId":true,"onErrorFunction":null},
	init:function(){
		this.message["@"]="Saisie obligatoire";
		this.message["min"]="Caractère(s) requi(s) au minimum";
		this.message["max"]="Caractère(s) requi(s) au maximum";
		this.message["num"]="Valeurs numériques uniquement";
		this.message["integer"]="Nombre entier uniquement";
		this.message["boolean"]="Booléen attendu";		
		
		this.message["mail"]="Adresse mail non conforme"; 
		this.message["text"]="Caractères uniquement";
		this.message["color"]="Couleur non conforme";
		this.message["alpha"]="Caractères alphanumériques uniquement";
		this.message["date"]="Date au format jj/mm/aaaa requis";
		this.message["datet"]="Date et/ou heure au format jj/mm/aaaa hh:mn requis";
		this.message["datetime"]="Date et heure au format jj/mm/aaaa hh:mn requis";
		this.message["time"]="Heure au format hh:mn requis";
		this.message["url"]="Url commençant par le protocole";
		this.message["tel"]="Téléphone à 10 chiffres";
		this.message["path"]="Chemin invalide";
		this.message["variable"]="Variable invalide";
		this.message["file"]="Nom de fichier incorrect";
		
		this.ER["@"]=/^.+$/;
		this.ER["num"]=/^[+-]?[0-9]*[\.\,]?[0-9]*$/;
		this.ER["integer"]=/^[\+\-]?[0-9]*$/;
		this.ER["boolean"]=/^(0|1|true|false){1}$/i;
		
		this.ER["mail"]=/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/;
		this.ER["text"]=/^([a-z]|[A-Z])*$/;
		this.ER["color"]=/^#[0-9A-F]+$/;
		this.ER["date"]=/^[0-9]{2,4}(\/|-|\.)[0-9]{1,2}(\/|-|\.)[0-9]{2,4}$/;
		this.ER["time"]=/^[0-9]{1,2}(:)[0-9]{1,2}(:?)[0-9]{0,2}$/;
		this.ER["datet"]=/^[0-9]{2,4}(\/|-|\.)[0-9]{1,2}(\/|-|\.)[0-9]{2,4}([ ][0-9]{1,2}(:)[0-9]{1,2}(:?)[0-9]{0,2})?$/;
		this.ER["alpha"]=/^[0-9A-Za-z]+$/;
		this.ER["datetime"]=/^[0-9]{2,4}(\/|-|\.)[0-9]{1,2}(\/|-|\.)[0-9]{2,4}[ ][0-9]{1,2}(:)[0-9]{1,2}(:?)[0-9]{0,2}$/;
		this.ER["url"]=/(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
		this.ER["tel"]=/^\d\d(-|\.|\s){0,1}\d\d(-|\.|\s){0,1}\d\d(-|\.|\s){0,1}\d\d(-|\.|\s){0,1}\d\d(-|\.|\s){0,1}$/;
		this.ER["path"]=/^[a-z0-9\-_\/\.]+$/i;
		this.ER["variable"]=/^[a-z0-9\-_\.]+$/i;
		this.ER["file"]=/^[a-z0-9\-_\/\.]+$/i;	
		this.ER["string"]=/^.*$/;			
	},
	setOptions:function(options){
		this.options=options;
	},
	addOptions:function(options){
		for(var k in options)
			this.options[k]=options[k];
	},
	setOption:function(optionName,value){
		this.options[optionName]=value;
	},
	getOption:function(optionName){
		ret=false;
		if(this.options!=null && this.options[optionName]!=undefined)
			ret=this.options[optionName];
		return ret;
	},
	addControlMessage:function(id,message,ER){
		if(ER!=undefined && ER!="")
			this.ER[id]=ER;
		this.message[id]=message;
	},
	addControlMessages:function(messages){
		if(Object.isArray(messages)){
			for(var i=0;i<messages.length;i++)
				this.addJSONMessage(messages[i]);
		}else if (typeof(messages)=='object')
			this.addJSONMessage(messages);		
	},
	addJSONMessage:function(message){
		if(typeof(message)=='object'){
			idMb="";messageMb="";erMb="";
			for(var member in message){
				if(member.toLowerCase()=='id')
					idMb=member;
				if(member.toLowerCase()=='er')
					erMb=member;
				if(member.toLowerCase()=='message')
					messageMb=member;
				if(idMb!=""&&messageMb!=""){
					if(erMb!="")
						this.addControlMessage(message[idMb],message[messageMb],message[erMb]);
					else
						this.addControlMessage(message[idMb],message[messageMb]);	
				}											
			}
		}	
	},
	getControl: function(elementId){
		if(this.controlsOnElements!=null){
			if(this.controlsOnElements[elementId]!=undefined)
				return this.controlsOnElements[elementId];
		}
		if(document.getElementById(elementId)!=undefined)
			return document.getElementById(elementId).alt;
		else
			return "";
	},
	parseFromJSON: function(json){
		this.controlsOnElements=new Array();
		this.elementsToControl=new Array();
		for(var id in json){
			var element=document.getElementById(id);
			if(this.id!='')
				element=Forms.Utils.getElementInForm(this.id, id);
			if(document.getElementById(id)!=undefined){
				if(element!=undefined){
					this.elementsToControl.push(element);
					this.controlsOnElements[id]=json[id];
				}
			}
		}
	},
	addElementsToControl: function(elements){
		this.elementsToControl=new Array();
		var getAllElementsOfAllForms=function(forms){
			var ret=new Array();
			for(var i=0;i<forms.length;i++){
				for(var j=0;j<forms[i].elements.length;j++){
					ret.push(forms[i].elements[j]);
				}
			}
			return ret;
		};
		if(elements!=undefined){
			if(typeof(elements)=='string'){
				try{
					var json=JSON.parse(elements);
					this.parseFromJSON(json);
					}
				catch(ex){}
				
			}
			else if(elements.tagName && elements.tagName.toLowerCase()=='form')
				this.elementsToControl=elements.elements;
			else if(typeof(elements)=='object'){
				try{
					this.parseFromJSON(elements);
					}
				catch(ex){}
			}else
				this.elementsToControl=getAllElementsOfAllForms(document.forms);
		}else
			this.elementsToControl=getAllElementsOfAllForms(document.forms);		
	},
	validate:function (){
		var valide=true;
		for(var i=0;i<this.elementsToControl.length;i++){
			try {
				if (!self.scontrol(this.elementsToControl[i])) {valide=false;}
			}
			catch(ex){}
		}
		return valide;
	},
	//--------------------------------------------------------------------	
	controlOn: function(events,elements,id){
		if(id!=undefined)
			this.id=id;
		if(events=='')
				events=['onkeyup','onchange'];
		this.init();
		this.addElementsToControl(elements);
		var evts=events;
		if(!Object.isArray(events))
			evts=events.split(',');
		for(var i=0;i<evts.length;i++){
			this.controlOnOneEvent(evts[i]);
		}		
	},
	//--------------------------------------------------------------------	
	controlOnOneEvent: function(event){
		for(var i=0;i<this.elementsToControl.length;i++){
			Forms.Utils.addEventToElement(this.elementsToControl[i],event,function () {Forms.Validation.scontrol(this);});
		}		
	},
	sControlOne:function(pMyObject,control){
		msg="";
		valeur=pMyObject.value;
		if(valeur!=undefined && control!="" && control!=undefined){
			if(this.iswhat(control,"integer")){
				valMinMax=Math.abs(control);
				if(control<0){
					if(pMyObject.value.length>valMinMax) 
						msg=" [" + valMinMax + " " + this.message["max"]+"]";
				}else{
					if(pMyObject.value.length<valMinMax) 
						msg=" [" + valMinMax + " " + this.message["min"]+"]";					
				}			
			}else if(control==="@"){
				if(!this.iswhat(valeur,"@")){
					if (typeof(this.message["@"])!="undefined")
						msg=" ["+ this.message["@"]+"]";
				}						
			}else{
				if(!this.iswhat(valeur,control)&&!(valeur.length==0)){
					if (typeof(this.message[control])!="undefined")
						msg=" ["+ this.message[control]+"]";
				}			
			}
		}
		return msg;
	},
//--------------------------------------------------------------------
	scontrol:function(pMyObject){
		pMyObject = (document.all)?event.srcElement : pMyObject;
		var nom=this.getControl(pMyObject.id);
		var valeur=pMyObject.value;
		var caption=pMyObject.name;
		var pos0=0;
		var msg="";
		this.cleanmsg(pMyObject);
		if(nom!="" && nom!=undefined && typeof(nom)!="undefined"){
			noms=nom.split(".");
			msg="";
			for(var i=0;i<noms.length;i++)
				msg+=this.sControlOne(pMyObject,noms[i]);
		}
		var isInvalidControl=(msg!="" && typeof(msg)!="undefined" && msg!=undefined);
		if(this.getOption('showErrorMessage'))
			this.displayErrorMessage(pMyObject,msg,isInvalidControl);
		if(this.getOption('onErrorFunction')!=null && typeof(this.getOption('onErrorFunction'))=='function'){
			try{
				this.getOption('onErrorFunction')(pMyObject,msg,isInvalidControl);
			}
			catch(ex){}
		}
		return !isInvalidControl;	
	},
	displayErrorMessage:function(pMyObject,msg,isInvalidControl){
		if (isInvalidControl)
			{
				if(pMyObject.className!=''&&pMyObject.className!='error'){
					this.classNames[pMyObject.id]=pMyObject.className;
				}
				showId="";
				if(this.getOption('showId'))
					showId=pMyObject.id+" :";
				pMyObject.className="error";
				this.errormsg(pMyObject,showId+" " + msg);
			}
			else
			{
				if(pMyObject.className=="error"){
					if(this.classNames[pMyObject.id]!=undefined){
						pMyObject.className=this.classNames[pMyObject.id];
						try{
						this.classNames.splice(this.classNames.indexOf(pMyObject.className), 1);}
						catch(ex){}
					}
					else
						pMyObject.className="";
				}
			}
			if($(this.getErrorId(pMyObject.id))!=undefined){
				show(this.getErrorId(pMyObject.id),isInvalidControl);
			}	
			return !isInvalidControl;	
	},
	//--------------------------------------------------------------------	
	iswhat:function(pval,pwhat){
		if (pval.search(this.ER[pwhat]) != -1)
			return true ;
		else
	        return false ;
	},
	
	//--------------------------------------------------------------------	
	errormsg: function(pObject,pmsg){
		errId=this.getErrorId(pObject.id);
		if($(errId)==undefined){
			var err=document.createElement("div");
			err.id=errId;
			err.className='errMessage';
			Forms.Utils.insertAfter(err, pObject);
		}
		$(errId).innerHTML=pmsg;
	},

	//--------------------------------------------------------------------	
	cleanmsg: function(pObject){
		if($(this.getErrorId(pObject.id))!=undefined)
			$(this.getErrorId(pObject.id)).innerHTML='';
	}
};

var self = Forms.Validation;
})();

(function(){
	Forms.Ajax=function(id,url,params,func,indicator){
		this.id=id;
		this.url=url;
		this.params=params;
		this.func=func;
		this.indicator=indicator;
		return this;
	};
	Forms.Ajax.prototype={
		id:'',
		url:'',
		params:'_ajx',
		indicator:null,
		func:null,
		method:'GET',
		content:'',
		onload: null,
		load:function(args){
			var xObj;
			var _this=this;
			var _args=args;
			if(this.params==undefined)
				this.params="_ajx";
			if(this.params.indexOf("_ajx")==-1)
				this.params+="&_ajx";
			if (this.indicator!=null)
				Forms.Utils.schowDiv(this.indicator,'show');
			this.method=this.method.toUpperCase();
			xObj=this.getXhrequest();
			xObj.onreadystatechange=function()
		    { 
		        if(xObj.readyState  == 4)
		        {
		        	if (_this.indicator!=null)
						Forms.Utils.schowDiv(_this.indicator); 
		             if(xObj.status  == 200){
		             	 _this.content=xObj.responseText;
		             	 if(_this.id!='')
		             		 if(document.getElementById(_this.id)!=undefined)
		             			 Forms.Utils.setInnerHTML(document.getElementById(_this.id),xObj.responseText);
							if (_this.func!=null) {
								if(_this.func.constructor === Array){
									for(var i=0;i<_this.func.length;i++){
										try{ _this.func[i](_this.id,_args);} catch(e){alert(e);}
									}
										
								}else{
									try{ _this.func(_this.id,_args);} catch(e){alert(e);}
								}
							}
						 }
		             else 
		            	 {
		            	 if(document.getElementById(_this.id)!=undefined)
		            		 document.getElementById(_this.id).innerHTML="Erreur " + xObj.status+"->"+_this.url;
		            	 }
				}
		   };
		   if (this.method=='GET')
			   xObj.open( this.method, this.url+"?"+this.params,  true);
		   else{
			   xObj.open( this.method, this.url,  true);
			   this.setPostHeader(xObj);
		   }
		   xObj.send(this.params);
		},		
		get:function(args){
			this.method='GET';
			this.load(args);
		},
		post:function(args){
			this.method='POST';
			this.load(args);
		},
		postForm:function(form,args){
			this.method='POST';
			this.params+="&"+Forms.Utils.serialize(form);
			this.load(args);
		},
		getForm:function(form,args){
			this.method='GET';
			this.params+="&"+Forms.Utils.serialize(form);
			this.load(args);
		},
		getXhrequest:function(){
		    var xhrequest;
			try {xhrequest = new XMLHttpRequest(); }                 
		    catch(e) 
		    {xhrequest = new ActiveXObject("Microsoft.XMLHTTP");}
		    return xhrequest;
		},
	   setPostHeader:function(xObj){
		   xObj.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
		   xObj.setRequestHeader("Content-length", this.params.length);
		   xObj.setRequestHeader("Connection", "close");	   
	   }		
			
	};
	//Membres privés
	var self=Forms.Ajax;
})();

//----------------------------------------------------------------------------------------------------------
getCheckedValue=function(radioObj) {
	if(!radioObj)
		return "";
	var radioLength = radioObj.length;
	if(radioLength == undefined)
		if(radioObj.checked)
			return radioObj.value;
		else
			return "";
	for(var i = 0; i < radioLength; i++) {
		if(radioObj[i].checked) {
			return radioObj[i].value;
		}
	}
	return "";
};
getCheckedValues=function(checkedObj) {
	var ret="";
	if(!checkedObj)
		ret= "";
	else{
		var checkedLength = checkedObj.length;
		if(checkedLength == undefined){
			if(checkedObj.checked)
				ret= checkedObj.value;
			else
				ret="";
		}
		else{
			for(var i = 0; i < checkedLength; i++) {
				if(checkedObj[i].checked) {
					ret+=checkedObj[i].value+",";
				}
			}
			if(ret.length>0)
				ret=ret.substr(0,ret.length-1);
		}
	}
	return ret;
};
(function(){
	Forms.Framework={
		insertRow:function(idTable){
			tableElement=$(idTable);
			if(tableElement!=null){
				var row=tableElement.insertRow(-1);
				num=tableElement.rows.length;
				row.innerHTML=tableElement.rows[1].innerHTML.replace(/-0/g,"-"+num);
				var All=tableElement.rows[1].getElementsByTagName("*");
				for (var i=0; i<All.length; i++) {
					e=All[i];
					if(e.id!==""){
						newId=e.id.replace(/-0/g,"-"+num);
						other=$(newId);
						if(other!=null){
							if(e.eventListeners!=undefined){
								for(var j=0;j<e.eventListeners.length;j++){
									Forms.Utils.addEventToElement(other, e.eventListeners[j].event, e.eventListeners[j].func,false);
								}
							}
						}
					}
				}
				row.id='line-'+num;
				return row;
			}
		},
		updateRow:function(idTable,num){
			updatedElement=$("updated-"+num);
			if(updatedElement.value=="0"){
				updatedElement.value="1";
				row=Forms.Framework.insertRow(idTable);
				var func=function(){var numLigne=Forms.Utils.getIdNum(this);Forms.Framework.updateRow(idTable,numLigne);};
				Forms.Utils.addEvent(row.id,'input','change',func);
			}else
				updatedElement.value=eval(updatedElement.value)+1;
		},
		checkRowToDelete:function(obj){
			var classname="deleteRow";
			var parent=Forms.Utils.getParent(obj.id, "tr");
			if(parent!=null){
				if(obj.checked)
					Forms.Utils.addClassName(classname, parent.id);
				else
					Forms.Utils.removeClassName(classname, parent.id);
			}
				
		}
	};
})();
//----------------------------------------------------------------------------------------------------------
$=Forms.Utils.$;
$$=Forms.Utils.$$;
$addEvt=Forms.Utils.addEventToElement;
$addEvts=Forms.Utils.addEventToElements;
$addEvtByTN=Forms.Utils.addEvent;
$set=Forms.Utils.setInnerHTML;
$get=Forms.Utils.get;
$post=Forms.Utils.post;
$postForm=Forms.Utils.postForm;
$getForm=Forms.Utils.getForm;
$selector=Forms.Selector;
$qs=Forms.Utils.thisQueryString;
show=Forms.Utils.show;
Ajx=Forms.Ajax;
_message=null;
_ER=null;