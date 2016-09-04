/*
Version 11.01.18
	- v2.0 rewrite
*/

Ajax=
{
	nocache: true,
	timeout: 180000,
	_active_requests: 0,
	onrequestbegin: null,
	onrequestend: null,
	
	Type:
	{
		TEXT: 'text/plain',
		XML: 'text/xml',
		JSON: 'application/json'
	},
	
	Xhr: function(type, onready)
	{
		this.type=type;
		this._timeout=null;
		
		try
		{
			this._xhr=new XMLHttpRequest();
		}
		catch(err)
		{
			try
			{
				this._xhr=new ActiveXObject('Msxml2.XMLHTTP');
			}
			catch(err)
			{
				try
				{
					this._xhr=new ActiveXObject('Microsoft.XMLHTTP');
				}
				catch(err)
				{
					throw('Could not create XML HTTP Request object.');
				}
			}
		}
		if(this._xhr.overrideMimeType)
			this._xhr.overrideMimeType(type);

		this._xhr.onreadystatechange=function()
		{
			var response, headers, header_strings, i, parts;
			
			if(this._xhr.readyState==4)
			{
				if(this._timeout!=null)
				{
					clearTimeout(this._timeout);
					this._timeout=null;
				}
				
				switch(this.type)
				{
					case Ajax.Type.JSON:
						try
						{
							response=JSON.parse(this._xhr.responseText);
						}
						catch(err)
						{
							response={success: false, message: err};
						}
					break;
					case Ajax.Type.XML:
						response=this._xhr.responseXml;
					break;
					default:
						response=this._xhr.responseText;
					break;
				}
				
				headers={};
				try
				{
					header_strings=this._xhr.getAllResponseHeaders().split(/\r*\n/);
					for(i=0; i<header_strings.length; i++)
					{
						parts=header_strings[i].split(/:\s+/);
						headers[parts[0].toLowerCase()]=parts[1];
					}
				}
				catch(err)
				{}
				
				if(!!onready.all)
					onready.all(response, headers);
				if(!!onready[this._xhr.status])
					onready[this._xhr.status](response, headers);
				else if(!!onready.other)
					onready.other(response, headers, this._xhr.status);
				
				Ajax._active_requests--;
				if(!!Ajax.onrequestend)
					Ajax.onrequestend(Ajax._active_requests);
			}
		}.bind(this);

		Ajax._active_requests++;
		if(!!Ajax.onrequestbegin)
			Ajax.onrequestbegin(Ajax._active_requests);
	},
	
	get: function(type, url, onready)
	{
		var xhr;

		xhr=new this.Xhr(type, onready);
		if(this.nocache)
			url=url+(url.indexOf('?')!=-1? '&' : '?')+'ts='+new Date().getTime();
		xhr._xhr.open('GET', url, true);
		xhr._timeout=setTimeout(function(){this.abort();}.bind(xhr._xhr), Ajax.timeout);
		xhr._xhr.send(null);
	},

	post: function(type, url, parameters, onready)
	{
		var xhr;

		xhr=new this.Xhr(type, onready);
		xhr._xhr.open('POST', url, true);
		xhr._xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
		xhr._xhr.setRequestHeader('Content-length', parameters.length);
		xhr._xhr.setRequestHeader('Connection', 'close');
		xhr._timeout=setTimeout(function(){this.abort();}.bind(xhr._xhr), Ajax.timeout);
		xhr._xhr.send(parameters);
	},
	
	getActiveRequests: function()
	{
		return this._active_requests;
	},
	
	request: function(type, request_method, url, parameters, onready)
	{
		switch(request_method.toLowerCase())
		{
			case 'get':
				if(parameters!='')
					url=url+(url.indexOf('?')==-1? '?' : '&')+parameters;
				this.get(type, url, onready);
			break;
			case 'post':
				this.post(type, url, parameters, onready);
			break;
			default:
				throw('Unknown method');
			break;
		}
	},
	
	submit: function(form, onsuccess, onfailure, onconnectionerror)
	{
		var i, use_aim=false, ajax, params, resp;
		for(i=0; i < form.elements.length; i++)
		{
			if(form.elements[i].type=='file')
				use_aim=true;
		}

		if(use_aim)
		{
			return Ajax.Aim.submit(form,
			{
				onComplete: function(response)
				{
					resp=JSON.parse(response);
					if(!!resp.success)
					{
						if(!!onsuccess)
							onsuccess(resp);
					}
					else
					{
						if(!!onfailure)
							onfailure(resp);
					}
					// TODO: onconnectionerror
				}
			});
		}
		else
		{
			params='';
			for(i=0; i < form.elements.length; i++)
			{
				if(form.elements[i].name!='')
				{
					if(form.elements[i].type=='checkbox' || form.elements[i].type=='radio')
					{
						if(!form.elements[i].checked)
							continue;
					}
					if(form.elements[i].type=='select-multiple')
					{
						if(form.elements[i].name.indexOf('[]')==-1)
							throw 'Put brackets on form element '+form.elements[i].name+' so it can submit multiple items';

						for(var j=0; j < form.elements[i].options.length; j++)
						{
							if(form.elements[i].options[j].selected)
								params+=encodeURIComponent(form.elements[i].name)+'='+encodeURIComponent(form.elements[i].options[j].value).replace(/\+/g, '%2B')+'&';
						}
						continue;
					}
					params+=encodeURIComponent(form.elements[i].name)+'='+encodeURIComponent(form.elements[i].value).replace(/\+/g, '%2B')+'&';
				}
			}

			Ajax.request(Ajax.Type.JSON, form.method, form.action, params,
			{
				200: function(resp)
				{
					if(!!resp.success)
					{
						if(!!onsuccess)
							onsuccess(resp);
					}
					else
					{
						if(!!onfailure)
							onfailure(resp);
					}
				},
				403: function(resp, headers)
				{
					if(!!headers['Location'])
						location.href=ajax.getHeader('Location');
				},
				0: function()
				{
					if(!!onconnectionerror)
						onconnectionerror();
				},
				other: function(resp)
				{
					if(!!onfailure)
						onfailure(resp);
				}
			});
		}

		return false;
	},
	
	Aim:
	{
		frame: function(c)
		{
			var name, div, iframe;
			
			name='Aim' + Math.floor(Math.random() * 99999);
			div=document.createElement('div');
			div.innerHTML = '<iframe style="display:none" src="about:blank" id="'+name+'" name="'+name+'" onload="Aim.onload(\''+name+'\')"></iframe>';
			document.body.appendChild(div);
			
			iframe = document.getElementById(name);
			if (c && typeof(c.onComplete) == 'function')
				iframe.onComplete = c.onComplete;
			
			return name;
		},
		
		form: function(form, name)
		{
			form.setAttribute('target', name);
		},
	 
		submit: function(form, c)
		{
			Ajax.Aim.form(form, Ajax.Aim.frame(c));
			if (c && typeof(c.onStart) == 'function')
				return c.onStart();
			else
				return true;
		},
		
		onload: function(id)
		{
			var doc, iframe;
			
			iframe=document.getElementById(id);
			if(iframe.contentDocument)
				doc = iframe.contentDocument;
			else if(iframe.contentWindow)
				doc = iframe.contentWindow.document;
			else 
				doc = window.frames[id].document;
			
			if(doc.location.href == 'about:blank') 
				return;
		
			if(typeof(iframe.onComplete) == 'function') 
				iframe.onComplete(doc.body.innerHTML);
		}
	}
};

// Minified json2.js
if(!this.JSON){this.JSON={}}(function(){"use strict";function f(n){return n<10?'0'+n:n}if(typeof Date.prototype.toJSON!=='function'){Date.prototype.toJSON=function(key){return isFinite(this.valueOf())?this.getUTCFullYear()+'-'+f(this.getUTCMonth()+1)+'-'+f(this.getUTCDate())+'T'+f(this.getUTCHours())+':'+f(this.getUTCMinutes())+':'+f(this.getUTCSeconds())+'Z':null};String.prototype.toJSON=Number.prototype.toJSON=Boolean.prototype.toJSON=function(key){return this.valueOf()}}var cx=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,escapable=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,gap,indent,meta={'\b':'\\b','\t':'\\t','\n':'\\n','\f':'\\f','\r':'\\r','"':'\\"','\\':'\\\\'},rep;function quote(string){escapable.lastIndex=0;return escapable.test(string)?'"'+string.replace(escapable,function(a){var c=meta[a];return typeof c==='string'?c:'\\u'+('0000'+a.charCodeAt(0).toString(16)).slice(-4)})+'"':'"'+string+'"'}function str(key,holder){var i,k,v,length,mind=gap,partial,value=holder[key];if(value&&typeof value==='object'&&typeof value.toJSON==='function'){value=value.toJSON(key)}if(typeof rep==='function'){value=rep.call(holder,key,value)}switch(typeof value){case'string':return quote(value);case'number':return isFinite(value)?String(value):'null';case'boolean':case'null':return String(value);case'object':if(!value){return'null'}gap+=indent;partial=[];if(Object.prototype.toString.apply(value)==='[object Array]'){length=value.length;for(i=0;i<length;i+=1){partial[i]=str(i,value)||'null'}v=partial.length===0?'[]':gap?'[\n'+gap+partial.join(',\n'+gap)+'\n'+mind+']':'['+partial.join(',')+']';gap=mind;return v}if(rep&&typeof rep==='object'){length=rep.length;for(i=0;i<length;i+=1){k=rep[i];if(typeof k==='string'){v=str(k,value);if(v){partial.push(quote(k)+(gap?': ':':')+v)}}}}else{for(k in value){if(Object.hasOwnProperty.call(value,k)){v=str(k,value);if(v){partial.push(quote(k)+(gap?': ':':')+v)}}}}v=partial.length===0?'{}':gap?'{\n'+gap+partial.join(',\n'+gap)+'\n'+mind+'}':'{'+partial.join(',')+'}';gap=mind;return v}}if(typeof JSON.stringify!=='function'){JSON.stringify=function(value,replacer,space){var i;gap='';indent='';if(typeof space==='number'){for(i=0;i<space;i+=1){indent+=' '}}else if(typeof space==='string'){indent=space}rep=replacer;if(replacer&&typeof replacer!=='function'&&(typeof replacer!=='object'||typeof replacer.length!=='number')){throw new Error('JSON.stringify')}return str('',{'':value})}}if(typeof JSON.parse!=='function'){JSON.parse=function(text,reviver){var j;function walk(holder,key){var k,v,value=holder[key];if(value&&typeof value==='object'){for(k in value){if(Object.hasOwnProperty.call(value,k)){v=walk(value,k);if(v!==undefined){value[k]=v}else{delete value[k]}}}}return reviver.call(holder,key,value)}text=String(text);cx.lastIndex=0;if(cx.test(text)){text=text.replace(cx,function(a){return'\\u'+('0000'+a.charCodeAt(0).toString(16)).slice(-4)})}if(/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,'@').replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,']').replace(/(?:^|:|,)(?:\s*\[)+/g,''))){j=eval('('+text+')');return typeof reviver==='function'?walk({'':j},''):j}throw new SyntaxError('JSON.parse')}}}());
